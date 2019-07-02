/*******************************************************************************************************
 *
 * msi.gama.util.file.GamaOsmFile.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.util.file;

import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.splitByWholeSeparatorPreserveAllTokens;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import msi.gama.common.geometry.Envelope3D;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.metamodel.shape.IShape;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.file;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IList;
import msi.gama.util.TOrderedHashMap;
import msi.gama.util.file.osmosis_copy.Bound;
import msi.gama.util.file.osmosis_copy.Entity;
import msi.gama.util.file.osmosis_copy.EntityContainer;
import msi.gama.util.file.osmosis_copy.Node;
import msi.gama.util.file.osmosis_copy.OsmHandler;
import msi.gama.util.file.osmosis_copy.OsmosisReader;
import msi.gama.util.file.osmosis_copy.Relation;
import msi.gama.util.file.osmosis_copy.RelationMember;
import msi.gama.util.file.osmosis_copy.RunnableSource;
import msi.gama.util.file.osmosis_copy.Sink;
import msi.gama.util.file.osmosis_copy.Tag;
import msi.gama.util.file.osmosis_copy.Way;
import msi.gama.util.file.osmosis_copy.WayNode;
import msi.gaml.operators.Strings;
import msi.gaml.operators.fastmaths.FastMath;
import msi.gaml.types.GamaGeometryType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import ummisco.gama.dev.utils.DEBUG;

@file (
		name = "osm",
		extensions = { "osm", "pbf", "bz2", "gz" },
		buffer_type = IType.LIST,
		buffer_content = IType.GEOMETRY,
		buffer_index = IType.INT,
		concept = { IConcept.OSM, IConcept.FILE },
		doc = @doc ("Represents files that contain OSM GIS information. The internal representation is a list of geometries. See https://en.wikipedia.org/wiki/OpenStreetMap for more information"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaOsmFile extends GamaGisFile {

	public static class OSMInfo extends GamaFileMetaData {

		int itemNumber;
		CoordinateReferenceSystem crs;
		final double width;
		final double height;
		final Map<String, String> attributes = new LinkedHashMap();

		public OSMInfo(final URL url, final long modificationStamp) {
			super(modificationStamp);
			CoordinateReferenceSystem crs = null;
			ReferencedEnvelope env2 = new ReferencedEnvelope();

			int number = 0;
			try {
				final File f = new File(url.toURI());
				final GamaOsmFile osmfile = new GamaOsmFile(null, f.getAbsolutePath());
				attributes.putAll(osmfile.getOSMAttributes(GAMA.getRuntimeScope()));

				final SimpleFeatureType TYPE = DataUtilities.createType("geometries", "geom:LineString");
				final ArrayList<SimpleFeature> list = new ArrayList<>();
				for (final IShape shape : osmfile.iterable(null)) {
					list.add(SimpleFeatureBuilder.build(TYPE, new Object[] { shape.getInnerGeometry() }, null));
				}
				final SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, list);
				final SimpleFeatureSource featureSource = DataUtilities.source(collection);

				env2 = featureSource.getBounds();
				number = osmfile.nbObjects;
				crs = osmfile.getOwnCRS(null);
			} catch (final Exception e) {
				DEBUG.ERR("Error in reading metadata of " + url);
				hasFailed = true;

			} finally {

				// approximation of the width and height in meters.
				width = env2 != null ? env2.getWidth() * (FastMath.PI / 180) * 6378137 : 0;
				height = env2 != null ? env2.getHeight() * (FastMath.PI / 180) * 6378137 : 0;
				itemNumber = number;
				this.crs = crs;
			}

		}

		public CoordinateReferenceSystem getCRS() {
			return crs;
		}

		public OSMInfo(final String propertiesString) throws NoSuchAuthorityCodeException, FactoryException {
			super(propertiesString);
			if (!hasFailed) {
				final String[] segments = split(propertiesString);
				itemNumber = Integer.parseInt(segments[1]);
				final String crsString = segments[2];
				if ("null".equals(crsString)) {
					crs = null;
				} else {
					crs = CRS.parseWKT(crsString);
				}
				width = Double.parseDouble(segments[3]);
				height = Double.parseDouble(segments[4]);
				if (segments.length > 5) {
					final String[] names = splitByWholeSeparatorPreserveAllTokens(segments[5], SUB_DELIMITER);
					final String[] types = splitByWholeSeparatorPreserveAllTokens(segments[6], SUB_DELIMITER);
					for (int i = 0; i < names.length; i++) {
						attributes.put(names[i], types[i]);
					}
				}
			} else {
				itemNumber = 0;
				width = 0.0;
				height = 0.0;
				crs = null;
			}
		}

		/**
		 * Method getSuffix()
		 *
		 * @see msi.gama.util.file.GamaFileMetaInformation#getSuffix()
		 */
		@Override
		public String getSuffix() {
			return hasFailed ? "error: decompress the file to a .osm file"
					: "" + itemNumber + " objects | " + Math.round(width) + "m x " + Math.round(height) + "m";
		}

		@Override
		public void appendSuffix(final StringBuilder sb) {
			if (hasFailed) {
				sb.append("error: decompress the file to a .osm file");
				return;
			}
			sb.append(itemNumber).append(" object");
			if (itemNumber > 1) {
				sb.append("s");
			}
			sb.append(SUFFIX_DEL);
			sb.append(Math.round(width)).append("m x ");
			sb.append(Math.round(height)).append("m");
		}

		@Override
		public String getDocumentation() {
			final StringBuilder sb = new StringBuilder();
			if (hasFailed) {
				sb.append("Unreadable OSM file").append(Strings.LN)
						.append("Decompress the file to an .osm file and retry");
			} else {
				sb.append("OSM file").append(Strings.LN);
				sb.append(itemNumber).append(" objects").append(Strings.LN);
				sb.append("Dimensions: ").append(FastMath.round(width) + "m x " + FastMath.round(height) + "m")
						.append(Strings.LN);
				sb.append("Coordinate Reference System: ").append(crs == null ? "No CRS" : crs.getName().getCode())
						.append(Strings.LN);
				if (!attributes.isEmpty()) {
					sb.append("Attributes: ").append(Strings.LN);
					for (final Map.Entry<String, String> entry : attributes.entrySet()) {
						sb.append("<li>").append(entry.getKey()).append(" (" + entry.getValue() + ")").append("</li>");
					}
				}
			}
			return sb.toString();
		}

		public Map<String, String> getAttributes() {
			return attributes;
		}

		@Override
		public String toPropertyString() {
			final String attributeNames = join(attributes.keySet(), SUB_DELIMITER);
			final String types = join(attributes.values(), SUB_DELIMITER);
			final Object[] toSave = new Object[] { super.toPropertyString(), itemNumber,
					crs == null ? "null" : crs.toWKT(), width, height, attributeNames, types };
			return join(toSave, DELIMITER);
		}
	}

	GamaMap<String, GamaList> filteringOptions;
	Map<String, String> attributes = new HashMap<>();

	final Map<String, List<IShape>> layers = GamaMapFactory.create(Types.STRING, Types.LIST);
	final static List<String> featureTypes = Arrays.asList("aerialway", "aeroway", "amenity", "barrier", "boundary",
			"building", "craft", "emergency", "geological", "highway", "historic", "landuse", "leisure", "man_made",
			"military", "natural", "office", "place", "power", "public_transport", "railway", "route", "shop", "sport",
			"tourism", "waterway");

	int nbObjects;

	/**
	 * @throws GamaRuntimeException
	 * @param scope
	 * @param pathName
	 */
	@doc (
			value = "This file constructor allows to read a osm (.osm, .pbf, .bz2, .gz) file (using WGS84 coordinate system for the data)",
			examples = { @example (
					value = "file f <- osm_file(\"file\");",
					isExecutable = false) })
	public GamaOsmFile(final IScope scope, final String pathName) {
		super(scope, pathName, (Integer) null);
	}

	@doc (
			value = "This file constructor allows to read an osm (.osm, .pbf, .bz2, .gz) file (using WGS84 coordinate system for the data)"
					+ "The map is used to filter the objects in the file according their attributes: for each key (string) of the map, only the objects that have a value for the  attribute "
					+ "contained in the value set are kept."
					+ " For an exhaustive list of the attibute of OSM data, see: http://wiki.openstreetmap.org/wiki/Map_Features",

			examples = { @example (
					value = "file f <- osm_file(\"file\", map([\"highway\"::[\"primary\", \"secondary\"], \"building\"::[\"yes\"], \"amenity\"::[]]));",
					equals = "f will contain all the objects of file that have the attibute 'highway' with the value 'primary' or 'secondary', and the objects that have the attribute 'building' with the value 'yes', "
							+ "and all the objects that have the attribute 'aminity' (whatever the value).",
					isExecutable = false) })

	public GamaOsmFile(final IScope scope, final String pathName, final GamaMap<String, GamaList> filteringOptions) {
		super(scope, pathName, (Integer) null);
		this.filteringOptions = filteringOptions;
	}

	@Override
	protected String fetchFromURL(final IScope scope) {
		String pathName = super.fetchFromURL(scope);
		if (pathName.endsWith(".osm.xml")) {
			pathName = pathName.replace(".xml", "");
		}
		return pathName;
	}

	public void getFeatureIterator(final IScope scope, final boolean returnIt) {
		final TLongObjectHashMap<GamaShape> nodesPt = new TLongObjectHashMap<>();
		final List<Node> nodes = new ArrayList<>();
		final List<Way> ways = new ArrayList<>();
		final List<Relation> relations = new ArrayList<>();
		final TLongHashSet intersectionNodes = new TLongHashSet();
		final TLongHashSet usedNodes = new TLongHashSet();

		final Sink sinkImplementation = new Sink() {

			@Override
			public void process(final EntityContainer entityContainer) {
				final Entity entity = entityContainer.getEntity();
				final boolean toFilter = filteringOptions != null && !filteringOptions.isEmpty();
				if (entity instanceof Bound) {
					final Bound bound = (Bound) entity;
					final Envelope3D env =
							new Envelope3D(bound.getLeft(), bound.getRight(), bound.getBottom(), bound.getTop(), 0, 0);
					computeProjection(scope, env);
				} else if (returnIt) {
					if (entity instanceof Node) {
						final Node node = (Node) entity;
						nodes.add(node);
						final Geometry g = gis == null
								? GamaPoint.create(node.getLongitude(), node.getLatitude()).getInnerGeometry()
								: gis.transform(
										GamaPoint.create(node.getLongitude(), node.getLatitude()).getInnerGeometry());
						nodesPt.put(node.getId(), new GamaShape(g));
					} else if (entity instanceof Way) {
						if (toFilter) {
							boolean keepObject = false;
							for (final String keyN : filteringOptions.getKeys()) {
								final GamaList valsPoss = filteringOptions.get(keyN);
								for (final Tag tagN : ((Way) entity).getTags()) {
									if (keyN.equals(tagN.getKey())) {
										if (valsPoss == null || valsPoss.isEmpty()
												|| valsPoss.contains(tagN.getValue())) {
											keepObject = true;
											break;
										}
									}

								}
							}
							if (!keepObject) { return; }
						}
						registerHighway((Way) entity, usedNodes, intersectionNodes);
						ways.add((Way) entity);
					} else if (entity instanceof Relation) {
						relations.add((Relation) entity);
					}
				}

			}

			@Override
			public void complete() {}

			@Override
			public void initialize(final Map<String, Object> arg0) {}
		};
		readFile(scope, sinkImplementation, getFile(scope));
		if (returnIt) {
			setBuffer(buildGeometries(nodes, ways, relations, intersectionNodes, nodesPt));
		}
	}

	private void addAttribute(final Map<String, String> atts, final String nameAt, final Object val) {
		final String type = atts.get(nameAt);
		if (type != null && type.equals("string")) { return; }
		String newType = "int";
		try {
			Integer.parseInt(val.toString());
		} catch (final Exception e) {
			try {
				Double.parseDouble(val.toString());
			} catch (final Exception e2) {
				newType = "string";
			}
		}

		if (type == null || newType.equals("string")) {
			atts.put(nameAt, newType);
		}
	}

	/**
	 * @see msi.gama.util.GamaFile#fillBuffer()
	 */
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		if (getBuffer() != null) { return; }
		setBuffer(GamaListFactory.<IShape> create(Types.GEOMETRY));
		getFeatureIterator(scope, true);
	}

	public IList<IShape> buildGeometries(final List<Node> nodes, final List<Way> ways, final List<Relation> relations,
			final TLongHashSet intersectionNodes, final TLongObjectHashMap<GamaShape> nodesPt) {
		final IList<IShape> geometries = GamaListFactory.create(Types.GEOMETRY);

		final Map<Long, Entity> geomMap = new HashMap<>();
		for (final Node node : nodes) {
			geomMap.put(node.getId(), node);
			final GamaShape pt = nodesPt.get(node.getId());
			final boolean hasAttributes = !node.getTags().isEmpty();
			final Map<String, String> atts = new HashMap<>();
			if (pt != null) {
				for (final Tag tg : node.getTags()) {
					final String key = tg.getKey().split(":")[0];
					final Object val = tg.getValue();
					if (val != null) {
						addAttribute(atts, key, val);
					}
					pt.setAttribute(key, val);
					if (key.equals("highway")) {
						intersectionNodes.add(node.getId());
					}
				}
				if (hasAttributes) {
					geometries.add(pt);
					for (final Object att : pt.getAttributes().keySet()) {
						if (featureTypes.contains(att)) {
							final String idType = att + " (point)";
							List objs = layers.get(idType);
							if (objs == null) {
								objs = GamaListFactory.create(Types.GEOMETRY);
								layers.put(idType, objs);
							}
							objs.add(pt);
							for (final String v : atts.keySet()) {
								final String id = idType + ";" + v;
								attributes.put(id, atts.get(v));
							}
							break;
						}
					}
				}
			}
		}
		for (final Way way : ways) {
			geomMap.put(way.getId(), way);
			final Map<String, Object> values = new TOrderedHashMap<>();
			final Map<String, String> atts = new HashMap<>();

			for (final Tag tg : way.getTags()) {
				final String key = tg.getKey().split(":")[0];
				final Object val = tg.getValue();
				if (val != null) {
					addAttribute(atts, key, val);
				}
				values.put(key, tg.getValue());
			}
			values.put("osm_id", way.getId());

			final boolean isPolyline = values.containsKey("highway") || way.getWayNodes().get(0).getNodeId() != way
					.getWayNodes().get(way.getWayNodes().size() - 1).getNodeId();
			if (isPolyline) {
				final List<IShape> geoms = createSplitRoad(way.getWayNodes(), values, intersectionNodes, nodesPt);
				geometries.addAll(geoms);
				if (!geoms.isEmpty()) {
					for (final Object att : values.keySet()) {
						final String idType = att + " (line)";
						if (featureTypes.contains(att)) {
							List objs = layers.get(idType);
							if (objs == null) {
								objs = GamaListFactory.create(Types.GEOMETRY);
								layers.put(idType, objs);
							}
							objs.addAll(geoms);
							for (final String v : atts.keySet()) {
								final String id = idType + ";" + v;
								attributes.put(id, atts.get(v));
							}
							break;
						}
					}
				}
			} else {
				final List<IShape> points = GamaListFactory.create(Types.GEOMETRY);
				for (final WayNode node : way.getWayNodes()) {
					final GamaShape pp = nodesPt.get(node.getNodeId());
					if (pp == null) {
						continue;
					}
					points.add(pp);
				}
				if (points.size() < 3) {
					continue;
				}
				final IShape geom = GamaGeometryType.buildPolygon(points);
				if (geom != null && geom.getInnerGeometry() != null && !geom.getInnerGeometry().isEmpty()
						&& geom.getInnerGeometry().getArea() > 0) {
					for (final Entry<String, Object> entry : values.entrySet()) {
						geom.setAttribute(entry.getKey(), entry.getValue());
					}
					geometries.add(geom);
					if (geom.getAttributes() != null) {
						for (final Object att : geom.getAttributes().keySet()) {
							final String idType = att + " (polygon)";
							if (featureTypes.contains(att)) {
								List objs = layers.get(idType);
								if (objs == null) {
									objs = GamaListFactory.create(Types.GEOMETRY);
									layers.put(idType, objs);
								}
								objs.add(geom);
								for (final String v : atts.keySet()) {
									final String id = idType + ";" + v;
									attributes.put(id, atts.get(v));
								}
								break;
							}
						}
					}
				}
			}

		}
		for (final Relation relation : relations) {
			final Map<String, Object> values = new TOrderedHashMap<>();

			for (final Tag tg : relation.getTags()) {
				final String key = tg.getKey().split(":")[0];
				values.put(key, tg.getValue());
			}

			int order = 0;
			for (final RelationMember member : relation.getMembers()) {
				final Entity entity = geomMap.get(member.getMemberId());
				if (entity instanceof Way) {
					final List<WayNode> relationWays = ((Way) entity).getWayNodes();
					final Map<String, Object> wayValues = new TOrderedHashMap<>();
					wayValues.put("entity_order", order++);
					// TODO FIXME AD: What's that ??
					wayValues.put("gama_bus_line", values.get("name"));
					wayValues.put("osm_way_id", ((Way) entity).getId());
					if (relationWays.size() > 0) {
						final List<IShape> geoms = createSplitRoad(relationWays, wayValues, intersectionNodes, nodesPt);
						geometries.addAll(geoms);
					}
				} else if (entity instanceof Node) {
					final GamaShape pt = nodesPt.get(((Node) entity).getId());
					final GamaShape pt2 = pt.copy(null);

					final List objs = GamaListFactory.create(Types.GEOMETRY);
					objs.add(pt2);

					pt2.setAttribute("gama_bus_line", values.get("name"));

					geometries.add(pt2);

				}
			}

			// if(relationWays.size() > 0) {
			// final List<IShape> geoms = createSplitRoad(relationWays, values, intersectionNodes, nodesPt);
			// geometries.addAll(geoms);
			// }
		}
		nbObjects = geometries == null ? 0 : geometries.size();
		return geometries;
	}

	public List<IShape> createSplitRoad(final List<WayNode> wayNodes, final Map<String, Object> values,
			final TLongHashSet intersectionNodes, final TLongObjectHashMap<GamaShape> nodesPt) {
		final List<List<IShape>> pointsList = GamaListFactory.create(Types.LIST.of(Types.GEOMETRY));
		List<IShape> points = GamaListFactory.create(Types.GEOMETRY);
		final IList<IShape> geometries = GamaListFactory.create(Types.GEOMETRY);
		final WayNode endNode = wayNodes.get(wayNodes.size() - 1);
		for (final WayNode node : wayNodes) {
			final Long id = node.getNodeId();
			final GamaShape pt = nodesPt.get(id);
			if (pt == null) {
				continue;
			}
			points.add(pt);
			if (intersectionNodes.contains(id) || node == endNode) {
				if (points.size() > 1) {
					pointsList.add(points);
				}
				points = GamaListFactory.create(Types.GEOMETRY);
				points.add(pt);

			}
		}
		int index = 0;
		for (final List<IShape> pts : pointsList) {
			final Map<String, Object> tempValues = new HashMap<>(values);
			tempValues.put("way_order", index++);
			final IShape g = createRoad(pts, tempValues);
			if (g != null) {
				geometries.add(g);
			}
		}
		return geometries;

	}

	private IShape createRoad(final List<IShape> points, final Map<String, Object> values) {
		if (points.size() < 2) { return null; }
		final IShape geom = GamaGeometryType.buildPolyline(points);
		if (geom != null && geom.getInnerGeometry() != null && !geom.getInnerGeometry().isEmpty()
				&& geom.getInnerGeometry().isSimple() && geom.getPerimeter() > 0) {
			for (final String key : values.keySet()) {
				geom.setAttribute(key, values.get(key));
			}
			return geom;
		}
		return null;
	}

	void registerHighway(final Way way, final TLongHashSet usedNodes, final TLongHashSet intersectionNodes) {
		for (final Tag tg : way.getTags()) {
			final String key = tg.getKey();
			if (key.equals("highway")) {
				final List<WayNode> nodes = way.getWayNodes();
				for (final WayNode node : nodes) {
					final long id = node.getNodeId();
					if (usedNodes.contains(id)) {
						intersectionNodes.add(id);
					} else {
						usedNodes.add(id);
					}
				}
				if (nodes.size() > 2 && nodes.get(0) == nodes.get(nodes.size() - 1)) {
					intersectionNodes.add(nodes.get(nodes.size() / 2).getNodeId());
				}
			}
		}
	}

	private void readFile(final IScope scope, final Sink sink, final File osmFile) {
		final String ext = getExtension(scope);
		RunnableSource reader = null;
		switch (ext) {
			case "pbf":
				try (FileInputStream stream = new FileInputStream(osmFile)) {
					reader = new OsmosisReader(stream);
					reader.setSink(sink);
					reader.run();
				} catch (final IOException e) {
					throw GamaRuntimeException.create(e, scope);
				}
				break;
			default:
				readXML(scope, sink);
		}

	}

	private void readXML(final IScope scope, final Sink sink) throws GamaRuntimeException {
		try {
			InputStream inputStream = new FileInputStream(getFile(scope));
			final String ext = getExtension(scope);
			switch (ext) {
				case "gz":
					inputStream = new GZIPInputStream(inputStream);
					break;
				case "bz2":
					inputStream = new BZip2CompressorInputStream(inputStream);
					break;
			}
			try (InputStream stream = inputStream) {
				final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
				parser.parse(stream, new OsmHandler(sink, false));
			}
		} catch (final Exception e) {
			throw GamaRuntimeException.error("Unable to parse xml file " + getName(scope) + ": " + e.getMessage(),
					scope);
		}

	}

	@Override
	public Envelope3D computeEnvelope(final IScope scope) {
		if (gis == null) {
			getFeatureIterator(scope, false);
		}
		return gis.getProjectedEnvelope();

	}

	/**
	 * Method getExistingCRS()
	 *
	 * @see msi.gama.util.file.GamaGisFile#getExistingCRS()
	 */
	@Override
	protected CoordinateReferenceSystem getOwnCRS(final IScope scope) {
		// Is it always true ?
		return DefaultGeographicCRS.WGS84;
	}

	public Map<String, String> getOSMAttributes(final IScope scope) {
		if (attributes == null) {
			attributes = new HashMap<>();
			getFeatureIterator(scope, true);
		}
		return attributes;
	}

	public Map<String, List<IShape>> getLayers() {
		return layers;
	}

	public List<String> getFeatureTypes() {
		return featureTypes;
	}

}
