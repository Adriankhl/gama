/*********************************************************************************************
 * 
 * 
 * 'GeometryUtils.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.common.util;

import static msi.gama.metamodel.shape.IShape.Type.*;
import java.util.*;
import msi.gama.database.sql.*;
import msi.gama.metamodel.shape.*;
import msi.gama.metamodel.shape.IShape.Type;
import msi.gama.metamodel.topology.projection.IProjection;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gama.util.file.IGamaFile;
import msi.gama.util.graph.IGraph;
import msi.gaml.operators.*;
import msi.gaml.operators.Spatial.Operators;
import msi.gaml.operators.Spatial.ThreeD;
import msi.gaml.species.ISpecies;
import msi.gaml.types.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.prep.*;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import com.vividsolutions.jts.triangulate.*;
import com.vividsolutions.jts.triangulate.quadedge.LocateFailureException;

/**
 * The class GamaGeometryUtils.
 * 
 * @author drogoul
 * @since 14 d�c. 2011
 * 
 */
public class GeometryUtils {

	public static class GamaCoordinateSequenceFactory implements CoordinateSequenceFactory {

		/**
		 * Method create()
		 * @see com.vividsolutions.jts.geom.CoordinateSequenceFactory#create(com.vividsolutions.jts.geom.Coordinate[])
		 */
		@Override
		public GamaCoordinateSequence create(final Coordinate[] coordinates) {
			return new GamaCoordinateSequence(coordinates);
		}

		/**
		 * Method create()
		 * @see com.vividsolutions.jts.geom.CoordinateSequenceFactory#create(com.vividsolutions.jts.geom.CoordinateSequence)
		 */
		@Override
		public GamaCoordinateSequence create(final CoordinateSequence coordSeq) {
			return new GamaCoordinateSequence(coordSeq);
		}

		/**
		 * Method create()
		 * @see com.vividsolutions.jts.geom.CoordinateSequenceFactory#create(int, int)
		 */
		@Override
		public GamaCoordinateSequence create(final int size, final int dimension) {
			return new GamaCoordinateSequence(size);
		}

	}

	public static class GamaCoordinateSequence implements CoordinateSequence {

		GamaPoint[] points;

		/**
		 * @param points2
		 */
		public GamaCoordinateSequence(final Coordinate[] points2) {
			if ( points2 == null ) {
				points = new GamaPoint[0];
			} else {
				points = new GamaPoint[points2.length];
				for ( int i = 0; i < points2.length; i++ ) {
					points[i] = new GamaPoint(points2[i]);
				}
			}
		}

		/**
		 * @param size
		 */
		public GamaCoordinateSequence(final int size) {
			points = new GamaPoint[size];
			for ( int i = 0; i < size; i++ ) {
				points[i] = new GamaPoint(0d, 0d, 0d);
			}
		}

		/**
		 * @param coordSeq
		 */
		public GamaCoordinateSequence(final CoordinateSequence coordSeq) {
			this(coordSeq.toCoordinateArray());
		}

		/**
		 * Method getDimension()
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#getDimension()
		 */
		@Override
		public int getDimension() {
			return 3;
		}

		@Override
		public GamaCoordinateSequence clone() {
			return new GamaCoordinateSequence(points);
		}

		/**
		 * Method getCoordinate()
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#getCoordinate(int)
		 */
		@Override
		public Coordinate getCoordinate(final int i) {
			return points[i];
		}

		/**
		 * Method getCoordinateCopy()
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#getCoordinateCopy(int)
		 */
		@Override
		public Coordinate getCoordinateCopy(final int i) {
			return new GamaPoint((Coordinate) points[i]);
		}

		/**
		 * Method getCoordinate()
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#getCoordinate(int, com.vividsolutions.jts.geom.Coordinate)
		 */
		@Override
		public void getCoordinate(final int index, final Coordinate coord) {
			coord.setCoordinate(points[index]);
		}

		/**
		 * Method getX()
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#getX(int)
		 */
		@Override
		public double getX(final int index) {
			return points[index].x;
		}

		/**
		 * Method getY()
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#getY(int)
		 */
		@Override
		public double getY(final int index) {
			return points[index].y;
		}

		/**
		 * Method getOrdinate()
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#getOrdinate(int, int)
		 */
		@Override
		public double getOrdinate(final int index, final int ordinateIndex) {
			return points[index].getOrdinate(ordinateIndex);
		}

		/**
		 * Method size()
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#size()
		 */
		@Override
		public int size() {
			return points.length;
		}

		/**
		 * Method setOrdinate()
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#setOrdinate(int, int, double)
		 */
		@Override
		public void setOrdinate(final int index, final int ordinateIndex, final double value) {
			points[index].setOrdinate(ordinateIndex, value);
		}

		/**
		 * Method toCoordinateArray()
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#toCoordinateArray()
		 */
		@Override
		public Coordinate[] toCoordinateArray() {
			return points;
		}

		/**
		 * Method expandEnvelope()
		 * @see com.vividsolutions.jts.geom.CoordinateSequence#expandEnvelope(com.vividsolutions.jts.geom.Envelope)
		 */
		@Override
		public Envelope expandEnvelope(final Envelope env) {
			// TODO Create an Envelope3D ??
			for ( GamaPoint p : points ) {
				env.expandToInclude(p);
			}
			return env;
		}

	}

	public static GeometryFactory FACTORY = new GeometryFactory(new GamaCoordinateSequenceFactory());
	public static PreparedGeometryFactory pgfactory = new PreparedGeometryFactory();
	public static CoordinateSequenceFactory coordFactory = FACTORY.getCoordinateSequenceFactory();

	public static GamaPoint pointInGeom(final Geometry geom, final RandomUtils rand) {
		// WARNING Only in 2D !
		if ( geom instanceof Point ) { return new GamaPoint(geom.getCoordinate()); }
		if ( geom instanceof LineString ) {
			final int i = rand.between(0, geom.getCoordinates().length - 2);
			final Coordinate source = geom.getCoordinates()[i];
			final Coordinate target = geom.getCoordinates()[i + 1];
			if ( source.x != target.x ) {
				final double a = (source.y - target.y) / (source.x - target.x);
				final double b = source.y - a * source.x;
				final double x = rand.between(source.x, target.x);
				final double y = a * x + b;
				return new GamaPoint(x, y, 0);
			}
			final double x = source.x;
			final double y = rand.between(source.y, target.y);
			return new GamaPoint(x, y, 0);
		}
		if ( geom instanceof Polygon ) {
			final Envelope env = geom.getEnvelopeInternal();
			final double xMin = env.getMinX();
			final double xMax = env.getMaxX();
			final double yMin = env.getMinY();
			final double yMax = env.getMaxY();
			final double x = rand.between(xMin, xMax);
			final Coordinate coord1 = new Coordinate(x, yMin);
			final Coordinate coord2 = new Coordinate(x, yMax);
			final Coordinate[] coords = { coord1, coord2 };
			Geometry line = FACTORY.createLineString(coords);
			try {
				line = line.intersection(geom);
			} catch (final Exception e) {
				final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
				line =
					GeometryPrecisionReducer.reducePointwise(line, pm).intersection(
						GeometryPrecisionReducer.reducePointwise(geom, pm));

			}
			return pointInGeom(line, rand);
		}
		if ( geom instanceof GeometryCollection ) { return pointInGeom(
			geom.getGeometryN(rand.between(0, geom.getNumGeometries() - 1)), rand); }

		return null;

	}

	private static Coordinate[] minimiseLength(final Coordinate[] coords) {
		final double dist1 = FACTORY.createLineString(coords).getLength();
		final Coordinate[] coordstest1 = new Coordinate[3];
		coordstest1[0] = coords[0];
		coordstest1[1] = coords[2];
		coordstest1[2] = coords[1];
		final double dist2 = FACTORY.createLineString(coordstest1).getLength();

		final Coordinate[] coordstest2 = new Coordinate[3];
		coordstest2[0] = coords[1];
		coordstest2[1] = coords[0];
		coordstest2[2] = coords[2];
		final double dist3 = FACTORY.createLineString(coordstest2).getLength();

		if ( dist1 <= dist2 && dist1 <= dist3 ) { return coords; }
		if ( dist2 <= dist1 && dist2 <= dist3 ) { return coordstest1; }
		if ( dist3 <= dist1 && dist3 <= dist2 ) { return coordstest2; }
		return coords;
	}

	public static Coordinate[] extractPoints(final IShape triangle, final Geometry geom, final int degree) {
		final Coordinate[] coords = triangle.getInnerGeometry().getCoordinates();
		final Coordinate[] c1 = { coords[0], coords[1] };
		final Coordinate[] c2 = { coords[1], coords[2] };
		final Coordinate[] c3 = { coords[2], coords[3] };
		final LineString l1 = FACTORY.createLineString(c1);
		final LineString l2 = FACTORY.createLineString(c2);
		final LineString l3 = FACTORY.createLineString(c3);
		final Coordinate[] pts = new Coordinate[degree];
		if ( degree == 3 ) {
			pts[0] = l1.getCentroid().getCoordinate();
			pts[1] = l2.getCentroid().getCoordinate();
			pts[2] = l3.getCentroid().getCoordinate();
			return minimiseLength(pts);
		} else if ( degree == 2 ) {
			final Geometry bounds = geom.getBoundary().buffer(1);
			final double val1 = bounds.intersection(l1).getLength() / l1.getLength();
			final double val2 = bounds.intersection(l2).getLength() / l2.getLength();
			final double val3 = bounds.intersection(l3).getLength() / l3.getLength();
			if ( val1 > val2 ) {
				if ( val1 > val3 ) {
					pts[0] = l2.getCentroid().getCoordinate();
					pts[1] = l3.getCentroid().getCoordinate();
				} else {
					pts[0] = l1.getCentroid().getCoordinate();
					pts[1] = l2.getCentroid().getCoordinate();
				}
			} else {
				if ( val2 > val3 ) {
					pts[0] = l1.getCentroid().getCoordinate();
					pts[1] = l3.getCentroid().getCoordinate();
				} else {
					pts[0] = l1.getCentroid().getCoordinate();
					pts[1] = l2.getCentroid().getCoordinate();
				}
			}
		} else {
			return null;
		}
		return pts;
	}

	public static IList<IShape> hexagonalGridFromGeom(final IShape geom, final int nbRows, final int nbColumns) {
		final double widthEnv = geom.getEnvelope().getWidth();
		final double heightEnv = geom.getEnvelope().getHeight();
		double xmin = geom.getEnvelope().getMinX();
		double ymin = geom.getEnvelope().getMinY();
		final double widthHex = widthEnv / (nbColumns * 0.75 + 0.25);
		final double heightHex = heightEnv / nbRows;
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		xmin += widthHex / 2.0;
		ymin += heightHex / 2.0;
		for ( int l = 0; l < nbRows; l++ ) {
			for ( int c = 0; c < nbColumns; c = c + 2 ) {
				final GamaShape poly =
					(GamaShape) GamaGeometryType.buildHexagon(widthHex, heightHex, new GamaPoint(xmin + c * widthHex *
						0.75, ymin + l * heightHex, 0));
				// GamaShape poly = (GamaShape) GamaGeometryType.buildHexagon(size, xmin + (c * 1.5)
				// * size, ymin + 2* size*val * l);
				if ( geom.covers(poly) ) {
					geoms.add(poly);
				}
			}
		}
		for ( int l = 0; l < nbRows; l++ ) {
			for ( int c = 1; c < nbColumns; c = c + 2 ) {
				final GamaShape poly =
					(GamaShape) GamaGeometryType.buildHexagon(widthHex, heightHex, new GamaPoint(xmin + c * widthHex *
						0.75, ymin + (l + 0.5) * heightHex, 0));
				// GamaShape poly = (GamaShape) GamaGeometryType.buildHexagon(size, xmin + (c * 1.5)
				// * size, ymin + 2* size*val * l);
				if ( geom.covers(poly) ) {
					geoms.add(poly);
				}
			}
		}
		/*
		 * for(int l=0;l<nbColumns;l++){
		 * for(int c=0;c<nbRows;c = c+2){
		 * GamaShape poly = (GamaShape) GamaGeometryType.buildHexagon(size, xmin + ((c +1) * 1.5) *
		 * size, ymin + 2* size*val * (l+0.5));
		 * if (geom.covers(poly))
		 * geoms.add(poly);
		 * }
		 * }
		 */
		return geoms;
	}

	public static IList<IShape> discretisation(final Geometry geom, final double size_x, final double size_y,
		final boolean overlaps) {
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		if ( geom instanceof GeometryCollection ) {
			final GeometryCollection gc = (GeometryCollection) geom;
			for ( int i = 0; i < gc.getNumGeometries(); i++ ) {
				geoms.addAll(discretisation(gc.getGeometryN(i), size_x, size_y, overlaps));
			}
		} else {
			final Envelope env = geom.getEnvelopeInternal();
			final double xMax = env.getMaxX();
			final double yMax = env.getMaxY();
			double x = env.getMinX();
			double y = env.getMinY();
			while (x < xMax) {
				y = env.getMinY();
				while (y < yMax) {
					final Coordinate c1 = new Coordinate(x, y);
					final Coordinate c2 = new Coordinate(x + size_x, y);
					final Coordinate c3 = new Coordinate(x + size_x, y + size_y);
					final Coordinate c4 = new Coordinate(x, y + size_y);
					final Coordinate[] cc = { c1, c2, c3, c4, c1 };
					final Geometry square = FACTORY.createPolygon(FACTORY.createLinearRing(cc), null);
					y += size_y;
					if ( !overlaps ) {
						if ( square.coveredBy(geom) ) {
							geoms.add(new GamaShape(square));
						}
					} else {
						if ( square.intersects(geom) ) {
							geoms.add(new GamaShape(square));
						}
					}
				}
				x += size_x;
			}
		}
		return geoms;
	}

	public static IList<IShape> geometryDecomposition(final IShape geom, final double x_size, final double y_size) {
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		double zVal = geom.getLocation().getZ();
		IList<IShape> rects = discretisation(geom.getInnerGeometry(), x_size, y_size, true);
		for ( IShape shape : rects ) {
			IShape gg = Operators.inter(null, shape, geom);
			if ( gg != null && !gg.getInnerGeometry().isEmpty() ) {
				GamaShape sp = new GamaShape(gg);
				IList<ILocation> pts = (IList<ILocation>) sp.getPoints();
				for ( int i = 0; i < pts.size(); i++ ) {
					ILocation gp = pts.get(i);
					if ( zVal != gp.getZ() ) {
						ThreeD.set_z(null, sp, i, zVal);
					}
				}
				geoms.add(sp);
			}
		}
		return geoms;
	}

	public static IList<IShape> geometryDecomposition(final IShape geom, final int nbCols, final int nbRows) {
		double x_size = geom.getEnvelope().getWidth() / nbCols;
		double y_size = geom.getEnvelope().getHeight() / nbRows;
		return geometryDecomposition(geom, x_size, y_size);
	}

	public static IList<IShape> triangulation(final IScope scope, final IList<IShape> lines) {
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		final ConformingDelaunayTriangulationBuilder dtb = new ConformingDelaunayTriangulationBuilder();

		final Geometry points = GamaGeometryType.geometriesToGeometry(scope, lines).getInnerGeometry();
		final double sizeTol = Math.sqrt(points.getEnvelope().getArea()) / 100.0;

		dtb.setSites(points);
		dtb.setConstraints(points);
		dtb.setTolerance(sizeTol);
		final GeometryCollection tri = (GeometryCollection) dtb.getTriangles(FACTORY);
		final int nb = tri.getNumGeometries();
		for ( int i = 0; i < nb; i++ ) {
			final Geometry gg = tri.getGeometryN(i);
			geoms.add(new GamaShape(gg));
		}
		return geoms;
	}

	public static IList<IShape> voronoi(final IScope scope, final IList<GamaPoint> points) {
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		final VoronoiDiagramBuilder dtb = new VoronoiDiagramBuilder();
		dtb.setSites(points);
		final GeometryCollection g = (GeometryCollection) dtb.getDiagram(FACTORY);
		final int nb = g.getNumGeometries();
		for ( int i = 0; i < nb; i++ ) {
			final Geometry gg = g.getGeometryN(i);
			geoms.add(new GamaShape(gg));
		}
		return geoms;
	}

	public static IList<IShape> triangulation(final IScope scope, final Geometry geom) {
		final IList<IShape> geoms = GamaListFactory.create(Types.GEOMETRY);
		if ( geom instanceof GeometryCollection ) {
			final GeometryCollection gc = (GeometryCollection) geom;
			for ( int i = 0; i < gc.getNumGeometries(); i++ ) {
				geoms.addAll(triangulation(scope, gc.getGeometryN(i)));
			}
		} else if ( geom instanceof Polygon ) {
			final Polygon polygon = (Polygon) geom;
			final double sizeTol = Math.sqrt(polygon.getArea()) / 100.0;
			final ConformingDelaunayTriangulationBuilder dtb = new ConformingDelaunayTriangulationBuilder();
			GeometryCollection tri = null;
			try {
				dtb.setSites(polygon);
				dtb.setConstraints(polygon);
				dtb.setTolerance(sizeTol);
				tri = (GeometryCollection) dtb.getTriangles(FACTORY);
			} catch (final LocateFailureException e) {
				throw GamaRuntimeException.error("Impossible to draw Geometry", scope);
			}
			final PreparedGeometry pg = pgfactory.create(polygon.buffer(sizeTol, 5, 0));
			final PreparedGeometry env = pgfactory.create(pg.getGeometry().getEnvelope());
			final int nb = tri.getNumGeometries();
			for ( int i = 0; i < nb; i++ ) {

				final Geometry gg = tri.getGeometryN(i);

				if ( env.covers(gg) && pg.covers(gg) ) {
					geoms.add(new GamaShape(gg));
				}
			}
		}
		return geoms;
	}

	public static List<LineString> squeletisation(final IScope scope, final Geometry geom) {
		final List<LineString> network = new ArrayList<LineString>();
		final IList polys = GeometryUtils.triangulation(scope, geom);
		final IGraph graph = Graphs.spatialLineIntersection(scope, polys);

		final Collection<GamaShape> nodes = graph.vertexSet();
		for ( final GamaShape node : nodes ) {
			final Coordinate[] coordsArr = GeometryUtils.extractPoints(node, geom, graph.degreeOf(node) / 2);
			if ( coordsArr != null ) {
				network.add(FACTORY.createLineString(coordsArr));
			}
		}

		return network;
	}

	public static Geometry buildGeometryJTS(final List<List<List<ILocation>>> listPoints) {
		final IShape.Type geometryType = geometryType(listPoints);
		switch (geometryType) {
			case NULL:
				return null;
			case POINT:
				return buildPoint(listPoints.get(0));
			case LINESTRING:
				return buildLine(listPoints.get(0));
			case POLYGON:
				return buildPolygon(listPoints.get(0));
			case MULTIPOINT:
				final int nb = listPoints.size();
				final Point[] geoms = new Point[nb];
				for ( int i = 0; i < nb; i++ ) {
					geoms[i] = (Point) buildPoint(listPoints.get(i));
				}
				return FACTORY.createMultiPoint(geoms);
			case MULTILINESTRING:
				final int n = listPoints.size();
				final LineString[] lines = new LineString[n];
				for ( int i = 0; i < n; i++ ) {
					lines[i] = (LineString) buildLine(listPoints.get(i));
				}
				return FACTORY.createMultiLineString(lines);
			case MULTIPOLYGON:
				final int n3 = listPoints.size();
				final Polygon[] polys = new Polygon[n3];
				for ( int i = 0; i < n3; i++ ) {
					polys[i] = (Polygon) buildPolygon(listPoints.get(i));
				}
				return FACTORY.createMultiPolygon(polys);
			default:
				return null;
		}
	}

	private static Geometry buildPoint(final List<List<ILocation>> listPoints) {
		return FACTORY.createPoint((Coordinate) listPoints.get(0).get(0));
	}

	public static Geometry buildGeometryCollection(final List<IShape> geoms) {
		int nb = geoms.size();
		Geometry[] geometries = new Geometry[nb];
		for ( int i = 0; i < nb; i++ ) {
			geometries[i] = geoms.get(i).getInnerGeometry();
		}
		Geometry geom = FACTORY.createGeometryCollection(geometries);

		return geom;
	}

	private static Geometry buildLine(final List<List<ILocation>> listPoints) {
		final List<ILocation> coords = listPoints.get(0);
		final int nb = coords.size();
		final Coordinate[] coordinates = new Coordinate[nb];
		for ( int i = 0; i < nb; i++ ) {
			coordinates[i] = (Coordinate) coords.get(i);
		}
		return FACTORY.createLineString(coordinates);
	}

	private static Geometry buildPolygon(final List<List<ILocation>> listPoints) {
		final List<ILocation> coords = listPoints.get(0);
		final int nb = coords.size();
		final Coordinate[] coordinates = new Coordinate[nb];
		for ( int i = 0; i < nb; i++ ) {
			coordinates[i] = (Coordinate) coords.get(i);
		}
		final int nbHoles = listPoints.size() - 1;
		LinearRing[] holes = null;
		if ( nbHoles > 0 ) {
			holes = new LinearRing[nbHoles];
			for ( int i = 0; i < nbHoles; i++ ) {
				final List<ILocation> coordsH = listPoints.get(i + 1);
				final int nbp = coordsH.size();
				final Coordinate[] coordinatesH = new Coordinate[nbp];
				for ( int j = 0; j < nbp; j++ ) {
					coordinatesH[j] = (Coordinate) coordsH.get(j);
				}
				holes[i] = FACTORY.createLinearRing(coordinatesH);
			}
		}
		final Polygon poly = FACTORY.createPolygon(FACTORY.createLinearRing(coordinates), holes);
		return poly;
	}

	private static IShape.Type geometryType(final List<List<List<ILocation>>> listPoints) {
		final int size = listPoints.size();
		if ( size == 0 ) { return NULL; }
		if ( size == 1 ) { return geometryTypeSimp(listPoints.get(0)); }
		final IShape.Type type = geometryTypeSimp(listPoints.get(0));
		switch (type) {
			case POINT:
				return MULTIPOINT;
			case LINESTRING:
				return MULTILINESTRING;
			case POLYGON:
				return POLYGON;
			default:
				return NULL;
		}
	}

	private static IShape.Type geometryTypeSimp(final List<List<ILocation>> listPoints) {
		if ( listPoints.isEmpty() || listPoints.get(0).isEmpty() ) { return NULL; }
		final List<ILocation> list0 = listPoints.get(0);
		final int size0 = list0.size();
		if ( size0 == 1 || size0 == 2 && list0.get(0).equals(list0.get(listPoints.size() - 1)) ) { return POINT; }
		if ( !list0.get(0).equals(list0.get(listPoints.size() - 1)) || size0 < 3 ) { return LINESTRING; }
		return POLYGON;
	}

	public static IList<GamaPoint> locsOnGeometry(final Geometry geom, final Double distance) {
		final IList<GamaPoint> locs = GamaListFactory.create(Types.POINT);
		if ( geom instanceof Point ) {
			locs.add(new GamaPoint(geom.getCoordinate()));
		} else if ( geom instanceof LineString ) {
			double distCur = 0;
			final Coordinate[] coordsSimp = geom.getCoordinates();
			if ( coordsSimp.length > 0 ) {
				locs.add(new GamaPoint(coordsSimp[0]));
			}
			final int nbSp = coordsSimp.length;
			for ( int i = 0; i < nbSp - 1; i++ ) {

				Coordinate s = coordsSimp[i];
				final Coordinate t = coordsSimp[i + 1];
				while (true) {
					final double dist = s.distance(t);
					if ( distance - distCur < dist ) {
						double distTravel = distance - distCur;
						final double ratio = distTravel / dist;
						double x_s = s.x + ratio * (t.x - s.x);
						double y_s = s.y + ratio * (t.y - s.y);
						s = new Coordinate(x_s, y_s);
						locs.add(new GamaPoint(s));
						distCur = 0;

					} else if ( distance - distCur > dist ) {
						distCur += dist;
						break;
					} else {
						distCur = 0;
						locs.add(new GamaPoint(t));
						break;
					}
				}

			}
			if ( locs.size() > 1 ) {
				if ( locs.get(0).distance(locs.get(locs.size() - 1)) < 0.1 * distance ) {
					locs.remove(locs.size() - 1);
				}
			}
		} else if ( geom instanceof Polygon ) {
			final Polygon poly = (Polygon) geom;
			locs.addAll(locsOnGeometry(poly.getExteriorRing(), distance));
			for ( int i = 0; i < poly.getNumInteriorRing(); i++ ) {
				locs.addAll(locsOnGeometry(poly.getInteriorRingN(i), distance));
			}
		}
		return locs;
	}

	// ---------------------------------------------------------------------------------------------
	// Thai.truongminh@gmail.com
	// Created date:24-Feb-2013: Process for SQL - MAP type
	// Modified: 03-Jan-2014

	public static Envelope computeEnvelopeFromSQLData(final IScope scope, final Map<String, Object> params) {
		final String crs = (String) params.get("crs");
		final String srid = (String) params.get("srid");
		final Boolean longitudeFirst =
			params.containsKey("longitudeFirst") ? (Boolean) params.get("longitudeFirst") : true;
		SqlConnection sqlConn;
		Envelope env = null;
		// create connection
		sqlConn = SqlUtils.createConnectionObject(scope, params);
		// get data
		final IList gamaList = sqlConn.selectDB(scope, (String) params.get("select"));
		env = SqlConnection.getBounds(gamaList);

		GuiUtils.debug("GeometryUtils.computeEnvelopeFromSQLData.Before Projection:" + env);

		IProjection gis;
		gis = scope.getSimulationScope().getProjectionFactory().fromParams(params, env);
		env = gis.getProjectedEnvelope();

		GuiUtils.debug("GeometryUtils.computeEnvelopeFromSQLData.After Projection:" + env);
		return env;
		// ----------------------------------------------------------------------------------------------------
	}

	public static Envelope computeEnvelopeFrom(final IScope scope, final Object obj) {
		Envelope result = new Envelope3D();
		if ( obj instanceof ISpecies ) {
			return computeEnvelopeFrom(scope, ((ISpecies) obj).getPopulation(scope));
		} else if ( obj instanceof Number ) {
			final double size = ((Number) obj).doubleValue();
			result = new Envelope3D(0, size, 0, size, 0, size);
		} else if ( obj instanceof ILocation ) {
			final ILocation size = (ILocation) obj;
			result = new Envelope3D(0, size.getX(), 0, size.getY(), 0, size.getZ());
		} else if ( obj instanceof IShape ) {
			result = ((IShape) obj).getEnvelope();
		} else if ( obj instanceof Envelope ) {
			result = new Envelope3D((Envelope) obj);
		} else if ( obj instanceof String ) {
			result = computeEnvelopeFrom(scope, Files.from(scope, (String) obj));
		} else if ( obj instanceof Map ) {
			result = computeEnvelopeFromSQLData(scope, (Map) obj);
		} else if ( obj instanceof IGamaFile ) {
			result = ((IGamaFile) obj).computeEnvelope(scope);
		} else if ( obj instanceof IList ) {
			Envelope boundsEnv = null;
			for ( final Object bounds : (IList) obj ) {
				final Envelope env = computeEnvelopeFrom(scope, bounds);
				if ( boundsEnv == null ) {
					boundsEnv = env;
				} else {
					boundsEnv.expandToInclude(env);
				}
			}
			result = boundsEnv;
		}
		return result;
	}

	public static IList<IShape> split_at(final IShape geom, final ILocation pt) {
		final IList<IShape> lines = GamaListFactory.create(Types.GEOMETRY);
		List<Geometry> geoms = null;
		if ( geom.getInnerGeometry() instanceof LineString ) {
			final Coordinate[] coords = ((LineString) geom.getInnerGeometry()).getCoordinates();
			final Point pt1 = FACTORY.createPoint(new GamaPoint(pt.getLocation()));
			final int nb = coords.length;
			int indexTarget = -1;
			double distanceT = Double.MAX_VALUE;
			for ( int i = 0; i < nb - 1; i++ ) {
				final Coordinate s = coords[i];
				final Coordinate t = coords[i + 1];
				final Coordinate[] seg = { s, t };
				final Geometry segment = FACTORY.createLineString(seg);
				final double distT = segment.distance(pt1);
				if ( distT < distanceT ) {
					distanceT = distT;
					indexTarget = i;
				}
			}
			int nbSp = indexTarget + 2;
			final Coordinate[] coords1 = new Coordinate[nbSp];
			for ( int i = 0; i <= indexTarget; i++ ) {
				coords1[i] = coords[i];
			}
			coords1[indexTarget + 1] = new GamaPoint(pt.getLocation());

			nbSp = coords.length - indexTarget;
			final Coordinate[] coords2 = new Coordinate[nbSp];
			coords2[0] = new GamaPoint(pt.getLocation());
			int k = 1;
			for ( int i = indexTarget + 1; i < coords.length; i++ ) {
				coords2[k] = coords[i];
				k++;
			}
			final List<Geometry> geoms1 = new ArrayList<Geometry>();
			geoms1.add(FACTORY.createLineString(coords1));
			geoms1.add(FACTORY.createLineString(coords2));
			geoms = geoms1;
		} else if ( geom.getInnerGeometry() instanceof MultiLineString ) {
			final Point point = FACTORY.createPoint((Coordinate) pt);
			Geometry geom2 = null;
			double distMin = Double.MAX_VALUE;
			final MultiLineString ml = (MultiLineString) geom.getInnerGeometry();
			for ( int i = 0; i < ml.getNumGeometries(); i++ ) {
				final double dist = ml.getGeometryN(i).distance(point);
				if ( dist <= distMin ) {
					geom2 = ml.getGeometryN(i);
					distMin = dist;
				}
			}
			final Coordinate[] coords = ((LineString) geom2).getCoordinates();
			final Point pt1 = FACTORY.createPoint(new GamaPoint(pt.getLocation()));
			final int nb = coords.length;
			int indexTarget = -1;
			double distanceT = Double.MAX_VALUE;
			for ( int i = 0; i < nb - 1; i++ ) {
				final Coordinate s = coords[i];
				final Coordinate t = coords[i + 1];
				final Coordinate[] seg = { s, t };
				final Geometry segment = FACTORY.createLineString(seg);
				final double distT = segment.distance(pt1);
				if ( distT < distanceT ) {
					distanceT = distT;
					indexTarget = i;
				}
			}
			int nbSp = indexTarget + 2;
			final Coordinate[] coords1 = new Coordinate[nbSp];
			for ( int i = 0; i <= indexTarget; i++ ) {
				coords1[i] = coords[i];
			}
			coords1[indexTarget + 1] = new GamaPoint(pt.getLocation());

			nbSp = coords.length - indexTarget;
			final Coordinate[] coords2 = new Coordinate[nbSp];
			coords2[0] = new GamaPoint(pt.getLocation());
			int k = 1;
			for ( int i = indexTarget + 1; i < coords.length; i++ ) {
				coords2[k] = coords[i];
				k++;
			}
			final List<Geometry> geoms1 = new ArrayList<Geometry>();
			geoms1.add(FACTORY.createLineString(coords1));
			geoms1.add(FACTORY.createLineString(coords2));
			geoms = geoms1;
		}
		if ( geoms != null ) {
			for ( final Geometry g : geoms ) {
				lines.add(new GamaShape(g));
			}
		}
		return lines;
	}

	/**
	 * @param intersect
	 * @return
	 */
	public static Type getTypeOf(final Geometry g) {
		if ( g == null ) { return Type.NULL; }
		return IShape.JTS_TYPES.get(g.getGeometryType());
	}
}
