/*******************************************************************************************************
 *
 * gama.util.path.GamaSpatialPath.java, in plugin gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.util.path;

import static gama.common.geometry.GeometryUtils.GEOMETRY_FACTORY;
import static gama.common.geometry.GeometryUtils.getContourCoordinates;
import static gama.common.geometry.GeometryUtils.getLastPointOf;
import static gama.common.geometry.GeometryUtils.getPointsOf;
import static gama.common.geometry.GeometryUtils.split_at;
import static gaml.operators.Spatial.Punctal._closest_point_to;
import static java.lang.Math.min;

import java.awt.geom.Line2D;

import org.apache.commons.lang.ArrayUtils;
import org.jgrapht.Graph;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import gama.common.geometry.GeometryUtils;
import gama.common.geometry.ICoordinates;
import gama.common.interfaces.IAgent;
import gama.common.util.Collector;
import gama.metamodel.shape.GamaPoint;
import gama.metamodel.shape.GamaShape;
import gama.metamodel.shape.IShape;
import gama.metamodel.topology.ITopology;
import gama.metamodel.topology.graph.GamaSpatialGraph;
import gama.runtime.scope.IScope;
import gama.util.graph.IGraph;
import gama.util.list.GamaListFactory;
import gama.util.list.IList;
import gama.util.map.GamaMapFactory;
import gama.util.map.IMap;
import gaml.operators.Cast;
import gaml.operators.Spatial.Punctal;
import gaml.types.GamaGeometryType;
import gaml.types.Types;

@SuppressWarnings ({ "rawtypes", "unchecked" })
public class GamaSpatialPath extends GamaPath<IShape, IShape, IGraph<IShape, IShape>> {

	IList<IShape> segments;
	IShape shape;
	boolean threeD;
	IMap<IShape, IShape> realObjects; // key = part of the geometry

	GamaSpatialPath(final GamaSpatialGraph g, final IShape start, final IShape target, final IList<IShape> _edges,
			final boolean modify_edges) {
		super(g, start, target, _edges, modify_edges);
	}

	// Used by the automated or manual casting to path
	GamaSpatialPath(final IList<IShape> nodes) {
		super(nodes);
	}

	GamaSpatialPath(final GamaSpatialGraph g, final IList<? extends IShape> nodes) {
		if (nodes.isEmpty()) {
			source = new GamaPoint(0, 0);
			target = source;
		} else {
			source = nodes.get(0);
			target = nodes.get(nodes.size() - 1);
		}
		segments = GamaListFactory.<IShape> create(Types.GEOMETRY);
		realObjects = GamaMapFactory.createUnordered(nodes.size());
		graph = g;

		for (int i = 0, n = nodes.size(); i < n - 1; i++) {
			segments.add(createEdge(nodes.get(i), nodes.get(i + 1)));
			final IAgent ag = nodes.get(i).getAgent();
			if (ag != null) {
				realObjects.put(nodes.get(i).getGeometry(), ag);
			}
		}
		final IAgent ag = nodes.isEmpty() ? null : nodes.get(nodes.size() - 1).getAgent();
		if (ag != null) {
			realObjects.put(nodes.get(nodes.size() - 1).getGeometry(), ag);
		}
	}

	@Override
	protected IShape createEdge(final IShape v, final IShape v2) {
		return GamaGeometryType.buildLine(v.getLocation(), v2.getLocation());
	}

	@Override
	public void init(final IShape start, final IShape target, final IList<? extends IShape> _edges,
			final boolean modify_edges) {
		super.init(start, target, _edges, modify_edges);
		source = start;
		this.target = target;
		this.segments = GamaListFactory.create(Types.GEOMETRY);
		realObjects = GamaMapFactory.createUnordered();
		graphVersion = 0;
		final Geometry firstLine = _edges == null || _edges.isEmpty() ? null : _edges.get(0).getInnerGeometry();
		GamaPoint pt = null, pt0 = null, pt1 = null;
		if (firstLine != null) {
			final GamaPoint[] firstLinePoints = GeometryUtils.getPointsOf(firstLine);
			pt0 = firstLinePoints[0];
			pt1 = firstLinePoints[firstLinePoints.length - 1];
		}
		if (firstLine != null && _edges != null && pt0 != null && pt1 != null) {
			if (_edges.size() > 1) {
				final double Z = pt0.z;
				for (final IShape e : _edges) {
					for (final GamaPoint p : GeometryUtils.getPointsOf(e)) {
						if (p.z != Z) {
							threeD = true;
							break;
						}
						if (threeD) {
							break;
						}
					}
				}
				final IShape secondLine = _edges.get(1).getGeometry();
				if (threeD) {
					pt = source.euclidianDistanceTo(pt0) < source.euclidianDistanceTo(pt1) ? pt0 : pt1;
				} else {
					pt = pt0.euclidianDistanceTo(secondLine) > pt1.euclidianDistanceTo(secondLine) ? pt0 : pt1;
				}

			} else {
				final IShape lineEnd = edges.get(edges.size() - 1);
				final GamaPoint falseTarget = _closest_point_to(getEndVertex().getLocation(), lineEnd);
				pt = start.euclidianDistanceTo(pt0) < falseTarget.euclidianDistanceTo(pt0) ? pt0 : pt1;
			}
			if (graph != null) {
				graphVersion = graph.getVersion();
			}
			int cpt = 0;
			for (final IShape edge : _edges) {
				if (modify_edges) {
					final IAgent ag = edge instanceof IAgent ? (IAgent) edge : null;
					final GamaPoint[] points = getPointsOf(edge);
					final Geometry geom = edge.getInnerGeometry();
					Geometry geom2;
					final GamaPoint c0 = points[0];
					final GamaPoint c1 = points[points.length - 1];
					IShape edge2 = null;
					final GamaPoint[] coords = getContourCoordinates(geom).toCoordinateArray().clone();
					if ((graph == null || !graph.isDirected())
							&& pt.euclidianDistanceTo(c0) > pt.euclidianDistanceTo(c1)) {
						ArrayUtils.reverse(coords);
						pt = c0;
					} else {
						pt = c1;
					}
					final ICoordinates cc = GEOMETRY_FACTORY.getCoordinateSequenceFactory().create(coords, false);
					geom2 = GEOMETRY_FACTORY.createLineString(cc);
					// geom2 = geom.reverse();
					edge2 = new GamaShape(geom2);
					if (!threeD) {

						if (cpt == 0 && !source.equals(pt)) {
							GamaPoint falseSource = source.getLocation();
							if (source.euclidianDistanceTo(edge2) > min(0.01, edge2.getPerimeter() / 1000)) {
								falseSource = _closest_point_to(source, edge2);
								falseSource.z = zVal(falseSource, edge2);
							}
							edge2 = split_at(edge2, falseSource).get(1);
						}
						if (cpt == _edges.size() - 1 && !target.equals(getLastPointOf(edge2))) {
							GamaPoint falseTarget = target.getLocation();
							if (target.euclidianDistanceTo(edge2) > min(0.01, edge2.getPerimeter() / 1000)) {
								falseTarget = _closest_point_to(target, edge2);
								falseTarget.z = zVal(falseTarget, edge2);
							}
							edge2 = split_at(edge2, falseTarget).get(0);
						}
					}
					if (ag != null) {
						realObjects.put(edge2.getGeometry(), ag);
					} else {
						realObjects.put(edge2.getGeometry(), edge);
					}
					segments.add(edge2.getGeometry());

				} else {
					segments.add(edge.getGeometry());
				}
				cpt++;
				// segmentsInGraph.put(agents, agents);
			}

		}
	}

	protected double zValOld(final GamaPoint point, final IShape edge) {
		double z = 0.0;
		final int nbSp = getPointsOf(edge).length;
		final Coordinate[] temp = new Coordinate[2];
		final Point pointGeom = (Point) point.getInnerGeometry();
		double distanceS = Double.MAX_VALUE;
		final GamaPoint[] edgePoints = GeometryUtils.getPointsOf(edge);
		for (int i = 0; i < nbSp - 1; i++) {
			temp[0] = edgePoints[i];
			temp[1] = edgePoints[i + 1];
			final LineString segment = GeometryUtils.GEOMETRY_FACTORY.createLineString(temp);
			final double distS = segment.distance(pointGeom);
			if (distS < distanceS) {
				distanceS = distS;
				final GamaPoint pt0 = new GamaPoint(temp[0]);
				final GamaPoint pt1 = new GamaPoint(temp[1]);
				z = pt0.z + (pt1.z - pt0.z) * point.distance(pt0) / segment.getLength();
			}
		}
		return z;
	}

	protected double zVal(final GamaPoint point, final IShape edge) {
		final GamaPoint[] points = getPointsOf(edge);
		final double distanceS = Double.MAX_VALUE;
		GamaPoint ps1 = new GamaPoint(), ps2 = new GamaPoint();
		for (int i = 0; i < points.length - 1; i++) {
			final GamaPoint p1 = points[i];
			final GamaPoint p2 = points[i + 1];
			final double distS = Line2D.ptSegDistSq(p1.x, p1.y, p2.x, p2.y, point.x, point.y);
			if (distS < distanceS) {
				ps1 = p1;
				ps2 = p2;
			}
		}
		return ps1.z + (ps2.z - ps1.z) * point.distance(ps1) / ps1.distance(ps2);

	}

	// /////////////////////////////////////////////////
	// Implements methods from IValue

	@Override
	public GamaSpatialPath copy(final IScope scope) {
		return new GamaSpatialPath(getGraph(), source, target, edges, true);
	}

	@Override
	public GamaSpatialGraph getGraph() {
		return (GamaSpatialGraph) graph;
	}

	@Override
	public IList getEdgeGeometry() {
		return segments;
	}

	@Override
	public void acceptVisitor(final IAgent agent) {
		agent.setAttribute("current_path", this); // ???
	}

	@Override
	public void forgetVisitor(final IAgent agent) {
		agent.setAttribute("current_path", null); // ???
	}

	@Override
	public int indexOf(final IAgent a) {
		return Cast.asInt(null, a.getAttribute("index_on_path")); // ???
	}

	@Override
	public int indexSegmentOf(final IAgent a) {
		return Cast.asInt(null, a.getAttribute("index_on_path_segment")); // ???
	}

	@Override
	public boolean isVisitor(final IAgent a) {
		return a.getAttribute("current_path") == this;
	}

	@Override
	public void setIndexOf(final IAgent a, final int index) {
		a.setAttribute("index_on_path", index);
	}

	@Override
	public void setIndexSegementOf(final IAgent a, final int indexSegement) {
		a.setAttribute("index_on_path_segment", indexSegement);
	}

	@Override
	public double getDistance(final IScope scope) {
		if (segments == null || segments.isEmpty()) { return Double.MAX_VALUE; }
		final Coordinate[] coordsSource = segments.get(0).getInnerGeometry().getCoordinates();
		final Coordinate[] coordsTarget = segments.get(getEdgeList().size() - 1).getInnerGeometry().getCoordinates();
		if (coordsSource.length == 0 || coordsTarget.length == 0) { return Double.MAX_VALUE; }
		final GamaPoint sourceEdges = new GamaPoint(coordsSource[0]);
		final GamaPoint targetEdges = new GamaPoint(coordsTarget[coordsTarget.length - 1]);
		final boolean keepSource = source.getLocation().equals(sourceEdges);
		final boolean keepTarget = target.getLocation().equals(targetEdges);
		if (keepSource && keepTarget) {
			double d = 0d;
			for (final IShape g : segments) {
				d += g.getInnerGeometry().getLength();
			}
			return d;
		}
		return getDistanceComplex(scope, keepSource, keepTarget);
	}

	private double getDistanceComplex(final IScope scope, final boolean keepSource, final boolean keepTarget) {
		double distance = 0;
		int index = 0;
		int indexSegment = 0;
		GamaPoint currentLocation = source.getLocation().copy(scope);
		final int nb = segments.size();
		if (!keepSource) {
			double distanceS = Double.MAX_VALUE;
			IShape line = null;
			for (int i = 0; i < nb; i++) {
				line = segments.get(i);
				final double distS = line.euclidianDistanceTo(currentLocation);
				if (distS < distanceS) {
					distanceS = distS;
					index = i;
				}
			}
			line = segments.get(index);
			currentLocation = Punctal._closest_point_to(currentLocation, line);
			final Point pointGeom = (Point) currentLocation.getInnerGeometry();
			if (line.getInnerGeometry().getNumPoints() >= 3) {
				distanceS = Double.MAX_VALUE;
				final Coordinate coords[] = line.getInnerGeometry().getCoordinates();
				final int nbSp = coords.length;
				final Coordinate[] temp = new Coordinate[2];
				for (int i = 0; i < nbSp - 1; i++) {
					temp[0] = coords[i];
					temp[1] = coords[i + 1];
					final LineString segment = GeometryUtils.GEOMETRY_FACTORY.createLineString(temp);
					final double distS = segment.distance(pointGeom);
					if (distS < distanceS) {
						distanceS = distS;
						indexSegment = i + 1;
					}
				}
			}
		}
		final IShape lineEnd = segments.get(nb - 1);
		int endIndexSegment = lineEnd.getInnerGeometry().getNumPoints();
		GamaPoint falseTarget = target.getLocation();
		if (!keepTarget) {
			falseTarget = Punctal._closest_point_to(getEndVertex(), lineEnd);
			endIndexSegment = 1;
			final Point pointGeom = (Point) falseTarget.getInnerGeometry();
			if (lineEnd.getInnerGeometry().getNumPoints() >= 3) {
				double distanceT = Double.MAX_VALUE;
				final Coordinate coords[] = lineEnd.getInnerGeometry().getCoordinates();
				final int nbSp = coords.length;
				final Coordinate[] temp = new Coordinate[2];
				for (int i = 0; i < nbSp - 1; i++) {
					temp[0] = coords[i];
					temp[1] = coords[i + 1];
					final LineString segment = GeometryUtils.GEOMETRY_FACTORY.createLineString(temp);
					final double distT = segment.distance(pointGeom);
					if (distT < distanceT) {
						distanceT = distT;
						endIndexSegment = i + 1;
					}
				}
			}
		}
		for (int i = index; i < nb; i++) {
			final IShape line = segments.get(i);
			final Coordinate coords[] = line.getInnerGeometry().getCoordinates();

			for (int j = indexSegment; j < coords.length; j++) {
				GamaPoint pt = null;
				if (i == nb - 1 && j == endIndexSegment) {
					pt = falseTarget;
				} else {
					pt = new GamaPoint(coords[j]);
				}
				final double dist = currentLocation.euclidianDistanceTo(pt);
				currentLocation = pt;
				distance = distance + dist;
				if (i == nb - 1 && j == endIndexSegment) {
					break;
				}
				indexSegment++;
			}
			indexSegment = 1;
			index++;
		}
		return distance;
	}

	@Override
	public ITopology getTopology(final IScope scope) {
		if (graph == null) { return null; }
		return ((GamaSpatialGraph) graph).getTopology(scope);
	}

	@Override
	public void setRealObjects(final IMap<IShape, IShape> realObjects) {
		this.realObjects = realObjects;
	}

	@Override
	public IShape getRealObject(final Object obj) {
		return realObjects.get(obj);
	}

	@Override
	public IShape getGeometry() {

		if (shape == null && segments.size() > 0) {
			if (segments.size() == 1) {
				shape = new GamaShape(segments.get(0));
			} else {
				try (Collector.AsList<IShape> pts = Collector.newList()) {
					for (final IShape ent : segments) {
						for (final GamaPoint p : GeometryUtils.getPointsOf(ent)) {
							if (!pts.contains(p)) {
								pts.add(p);
							}
						}
					}
					if (pts.size() > 0) {
						shape = GamaGeometryType.buildPolyline(pts.items());
					}
				}
			}

		}
		return shape;
	}

	@Override
	public void setGraph(final IGraph<IShape, IShape> graph) {
		this.graph = graph;
		graphVersion = graph.getVersion();

		for (final IShape edge : edges) {
			final IAgent ag = edge.getAgent();
			if (ag != null) {
				realObjects.put(edge.getGeometry(), ag);
			} else {
				realObjects.put(edge.getGeometry(), edge);
			}
		}

	}

	@Override
	public IList<IShape> getEdgeList() {
		if (edges == null) { return segments; }
		return edges;
	}

	@Override
	public IList<IShape> getVertexList() {
		if (graph == null) {
			try (final Collector.AsList<IShape> vertices = Collector.newList()) {
				IShape g = null;
				for (final Object ed : getEdgeList()) {
					g = (IShape) ed;
					vertices.add(GeometryUtils.getFirstPointOf(g));
				}
				if (g != null) {
					vertices.add(GeometryUtils.getLastPointOf(g));
				}
				return vertices.items();
			}
		}
		return getPathVertexList();
	}

	public IList<IShape> getPathVertexList() {
		final Graph<IShape, IShape> g = getGraph();
		try (final Collector.AsList<IShape> list = Collector.newList()) {
			IShape v = getStartVertex();
			list.add(v);
			IShape vPrev = null;
			for (final IShape e : getEdgeList()) {
				vPrev = v;
				v = getOppositeVertex(g, e, v);
				if (!v.equals(vPrev)) {
					list.add(v);
				}
			}
			return list.items();
		}
	}

	public static IShape getOppositeVertex(final Graph<IShape, IShape> g, final IShape e, final IShape v) {
		final IShape source = g.getEdgeSource(e);
		final IShape target = g.getEdgeTarget(e);
		if (v.equals(source)) {
			return target;
		} else if (v.equals(target)) {
			return source;
		} else {
			return v.euclidianDistanceTo(source) > v.euclidianDistanceTo(target) ? target : source;
		}
	}

	public boolean isThreeD() {
		return threeD;
	}

}