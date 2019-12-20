/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 and Eclipse Distribution License v. 1.0 which accompanies this distribution. The Eclipse Public
 * License is available at http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution License is available
 * at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jts.triangulate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.index.kdtree.KdNode;
import org.locationtech.jts.index.kdtree.KdTree;
import org.locationtech.jts.triangulate.quadedge.LastFoundQuadEdgeLocator;
import org.locationtech.jts.triangulate.quadedge.QuadEdgeSubdivision;
import org.locationtech.jts.triangulate.quadedge.Vertex;
import org.locationtech.jts.util.Debug;

/**
 * Computes a Conforming Delaunay Triangulation over a set of sites and a set of linear constraints.
 * <p>
 * A conforming Delaunay triangulation is a true Delaunay triangulation. In it each constraint segment is present as a
 * union of one or more triangulation edges. Constraint segments may be subdivided into two or more triangulation edges
 * by the insertion of additional sites. The additional sites are called Steiner points, and are necessary to allow the
 * segments to be faithfully reflected in the triangulation while maintaining the Delaunay property. Another way of
 * stating this is that in a conforming Delaunay triangulation every constraint segment will be the union of a subset of
 * the triangulation edges (up to tolerance).
 * <p>
 * A Conforming Delaunay triangulation is distinct from a Constrained Delaunay triangulation. A Constrained Delaunay
 * triangulation is not necessarily fully Delaunay, and it contains the constraint segments exactly as edges of the
 * triangulation.
 * <p>
 * A typical usage pattern for the triangulator is:
 * 
 * <pre>
 * ConformingDelaunayTriangulator cdt = new ConformingDelaunayTriangulator(sites, tolerance);
 *
 * // optional
 * cdt.setSplitPointFinder(splitPointFinder);
 * cdt.setVertexFactory(vertexFactory);
 * 
 * cdt.setConstraints(segments, new ArrayList(vertexMap.values()));
 * cdt.formInitialDelaunay();
 * cdt.enforceConstraints();
 * subdiv = cdt.getSubdivision();
 * </pre>
 *
 * @author David Skea
 * @author Martin Davis
 */
public class ConformingDelaunayTriangulator {
	private static Envelope computeVertexEnvelope(final Collection<?> vertices) {
		final Envelope env = new Envelope();
		for (final Object name : vertices) {
			final Vertex v = (Vertex) name;
			env.expandToInclude(v.getCoordinate());
		}
		return env;
	}

	private final List<?> initialVertices; // List<Vertex>
	private List<?> segVertices; // List<Vertex>

	// MD - using a Set doesn't seem to be much faster
	// private Set segments = new HashSet();
	private List<Segment> segments = new ArrayList<>(); // List<Segment>
	private QuadEdgeSubdivision subdiv = null;
	private IncrementalDelaunayTriangulator incDel;
	private Geometry convexHull;
	private ConstraintSplitPointFinder splitFinder = new NonEncroachingSplitPointFinder();
	private KdTree kdt = null;
	private ConstraintVertexFactory vertexFactory = null;

	// allPointsEnv expanded by a small buffer
	private Envelope computeAreaEnv;
	// records the last split point computed, for error reporting
	private Coordinate splitPt = null;

	private final double tolerance; // defines if two sites are the same.

	/**
	 * Creates a Conforming Delaunay Triangulation based on the given unconstrained initial vertices. The initial vertex
	 * set should not contain any vertices which appear in the constraint set.
	 *
	 * @param initialVertices
	 *            a collection of {@link ConstraintVertex}
	 * @param tolerance
	 *            the distance tolerance below which points are considered identical
	 */
	public ConformingDelaunayTriangulator(final Collection<?> initialVertices, final double tolerance) {
		this.initialVertices = new ArrayList<Object>(initialVertices);
		this.tolerance = tolerance;
		kdt = new KdTree(tolerance);
	}

	/**
	 * Sets the constraints to be conformed to by the computed triangulation. The constraints must not contain duplicate
	 * segments (up to orientation). The unique set of vertices (as {@link ConstraintVertex}es) forming the constraints
	 * must also be supplied. Supplying it explicitly allows the ConstraintVertexes to be initialized appropriately
	 * (e.g. with external data), and avoids re-computing the unique set if it is already available.
	 *
	 * @param segments
	 *            a list of the constraint {@link Segment}s
	 * @param segVertices
	 *            the set of unique {@link ConstraintVertex}es referenced by the segments
	 */
	public void setConstraints(final List<Segment> segments, final List<?> segVertices) {
		this.segments = segments;
		this.segVertices = segVertices;
	}

	/**
	 * Sets the {@link ConstraintSplitPointFinder} to be used during constraint enforcement. Different splitting
	 * strategies may be appropriate for special situations.
	 *
	 * @param splitFinder
	 *            the ConstraintSplitPointFinder to be used
	 */
	public void setSplitPointFinder(final ConstraintSplitPointFinder splitFinder) {
		this.splitFinder = splitFinder;
	}

	/**
	 * Gets the tolerance value used to construct the triangulation.
	 *
	 * @return a tolerance value
	 */
	public double getTolerance() {
		return tolerance;
	}

	/**
	 * Gets the <tt>ConstraintVertexFactory</tt> used to create new constraint vertices at split points.
	 *
	 * @return a new constraint vertex
	 */
	public ConstraintVertexFactory getVertexFactory() {
		return vertexFactory;
	}

	/**
	 * Sets a custom {@link ConstraintVertexFactory} to be used to allow vertices carrying extra information to be
	 * created.
	 *
	 * @param vertexFactory
	 *            the ConstraintVertexFactory to be used
	 */
	public void setVertexFactory(final ConstraintVertexFactory vertexFactory) {
		this.vertexFactory = vertexFactory;
	}

	/**
	 * Gets the {@link QuadEdgeSubdivision} which represents the triangulation.
	 *
	 * @return a subdivision
	 */
	public QuadEdgeSubdivision getSubdivision() {
		return subdiv;
	}

	/**
	 * Gets the {@link KdTree} which contains the vertices of the triangulation.
	 *
	 * @return a KdTree
	 */
	public KdTree getKDT() {
		return kdt;
	}

	/**
	 * Gets the sites (vertices) used to initialize the triangulation.
	 * 
	 * @return a List of Vertex
	 */
	public List<?> getInitialVertices() {
		return initialVertices;
	}

	/**
	 * Gets the {@link Segment}s which represent the constraints.
	 *
	 * @return a collection of Segments
	 */
	public Collection<Segment> getConstraintSegments() {
		return segments;
	}

	/**
	 * Gets the convex hull of all the sites in the triangulation, including constraint vertices. Only valid after the
	 * constraints have been enforced.
	 *
	 * @return the convex hull of the sites
	 */
	public Geometry getConvexHull() {
		return convexHull;
	}

	// ==================================================================

	private void computeBoundingBox() {
		final Envelope vertexEnv = computeVertexEnvelope(initialVertices);
		final Envelope segEnv = computeVertexEnvelope(segVertices);

		final Envelope allPointsEnv = new Envelope(vertexEnv);
		allPointsEnv.expandToInclude(segEnv);

		final double deltaX = allPointsEnv.getWidth() * 0.2;
		final double deltaY = allPointsEnv.getHeight() * 0.2;

		final double delta = Math.max(deltaX, deltaY);

		computeAreaEnv = new Envelope(allPointsEnv);
		computeAreaEnv.expandBy(delta);
	}

	private void computeConvexHull() {
		final GeometryFactory fact = new GeometryFactory();
		final Coordinate[] coords = getPointArray();
		final ConvexHull hull = new ConvexHull(coords, fact);
		convexHull = hull.getConvexHull();
	}

	// /**
	// * Adds the segments in the Convex Hull of all sites in the input data as
	// linear constraints.
	// * This is required if TIN Refinement is performed. The hull segments are
	// flagged with a
	// unique
	// * data object to allow distinguishing them.
	// *
	// * @param convexHullSegmentData the data object to attach to each convex
	// hull segment
	// */
	// private void addConvexHullToConstraints(Object convexHullSegmentData) {
	// Coordinate[] coords = convexHull.getCoordinates();
	// for (int i = 1; i < coords.length; i++) {
	// Segment s = new Segment(coords[i - 1], coords[i], convexHullSegmentData);
	// addConstraintIfUnique(s);
	// }
	// }

	// private void addConstraintIfUnique(Segment r) {
	// boolean exists = false;
	// Iterator it = segments.iterator();
	// Segment s = null;
	// while (it.hasNext()) {
	// s = (Segment) it.next();
	// if (r.equalsTopo(s)) {
	// exists = true;
	// }
	// }
	// if (!exists) {
	// segments.add((Object) r);
	// }
	// }

	private Coordinate[] getPointArray() {
		final Coordinate[] pts = new Coordinate[initialVertices.size() + segVertices.size()];
		int index = 0;
		for (final Object name : initialVertices) {
			final Vertex v = (Vertex) name;
			pts[index++] = v.getCoordinate();
		}
		for (final Object name : segVertices) {
			final Vertex v = (Vertex) name;
			pts[index++] = v.getCoordinate();
		}
		return pts;
	}

	private ConstraintVertex createVertex(final Coordinate p) {
		ConstraintVertex v = null;
		if (vertexFactory != null) {
			v = vertexFactory.createVertex(p, null);
		} else {
			v = new ConstraintVertex(p);
		}
		return v;
	}

	/**
	 * Creates a vertex on a constraint segment
	 *
	 * @param p
	 *            the location of the vertex to create
	 * @param seg
	 *            the constraint segment it lies on
	 * @return the new constraint vertex
	 */
	private ConstraintVertex createVertex(final Coordinate p, final Segment seg) {
		ConstraintVertex v = null;
		if (vertexFactory != null) {
			v = vertexFactory.createVertex(p, seg);
		} else {
			v = new ConstraintVertex(p);
		}
		v.setOnConstraint(true);
		return v;
	}

	/**
	 * Inserts all sites in a collection
	 *
	 * @param vertices
	 *            a collection of ConstraintVertex
	 */
	private void insertSites(final Collection<?> vertices) {
		Debug.println("Adding sites: " + vertices.size());
		for (final Object name : vertices) {
			final ConstraintVertex v = (ConstraintVertex) name;
			insertSite(v);
		}
	}

	private ConstraintVertex insertSite(final ConstraintVertex v) {
		final KdNode kdnode = kdt.insert(v.getCoordinate(), v);
		if (!kdnode.isRepeated()) {
			incDel.insertSite(v);
		} else {
			final ConstraintVertex snappedV = (ConstraintVertex) kdnode.getData();
			snappedV.merge(v);
			return snappedV;
			// testing
			// if ( v.isOnConstraint() && ! currV.isOnConstraint()) {
			// System.out.println(v);
			// }
		}
		return v;
	}

	/**
	 * Inserts a site into the triangulation, maintaining the conformal Delaunay property. This can be used to further
	 * refine the triangulation if required (e.g. to approximate the medial axis of the constraints, or to improve the
	 * grading of the triangulation).
	 *
	 * @param p
	 *            the location of the site to insert
	 */
	public void insertSite(final Coordinate p) {
		insertSite(createVertex(p));
	}

	// ==================================================================

	/**
	 * Computes the Delaunay triangulation of the initial sites.
	 */
	public void formInitialDelaunay() {
		computeBoundingBox();
		subdiv = new QuadEdgeSubdivision(computeAreaEnv, tolerance);
		subdiv.setLocator(new LastFoundQuadEdgeLocator(subdiv));
		incDel = new IncrementalDelaunayTriangulator(subdiv);
		insertSites(initialVertices);
	}

	// ==================================================================

	private final static int MAX_SPLIT_ITER = 99;

	/**
	 * Enforces the supplied constraints into the triangulation.
	 *
	 * @throws ConstraintEnforcementException
	 *             if the constraints cannot be enforced
	 */
	public void enforceConstraints() {
		addConstraintVertices();
		// if (true) return;

		int count = 0;
		int splits = 0;
		do {
			splits = enforceGabriel(segments);

			count++;
			Debug.println("Iter: " + count + "   Splits: " + splits + "   Current # segments = " + segments.size());
		} while (splits > 0 && count < MAX_SPLIT_ITER);
		if (count == MAX_SPLIT_ITER) {
			Debug.println("ABORTED! Too many iterations while enforcing constraints");
			if (!Debug.isDebugging()) {
				throw new ConstraintEnforcementException(
						"Too many splitting iterations while enforcing constraints.  Last split point was at: ",
						splitPt);
			}
		}
	}

	private void addConstraintVertices() {
		computeConvexHull();
		// insert constraint vertices as sites
		insertSites(segVertices);
	}

	/*
	 * private List findMissingConstraints() { List missingSegs = new ArrayList(); for (int i = 0; i < segments.size();
	 * i++) { Segment s = (Segment) segments.get(i); QuadEdge q = subdiv.locate(s.getStart(), s.getEnd()); if (q ==
	 * null) missingSegs.add(s); } return missingSegs; }
	 */

	private int enforceGabriel(final Collection<Segment> segsToInsert) {
		final List<Segment> newSegments = new ArrayList<>();
		int splits = 0;
		final List<Segment> segsToRemove = new ArrayList<>();

		/**
		 * On each iteration must always scan all constraint (sub)segments, since some constraints may be rebroken by
		 * Delaunay triangle flipping caused by insertion of another constraint. However, this process must converge
		 * eventually, with no splits remaining to find.
		 */
		for (final Segment segment : segsToInsert) {
			final Segment seg = segment;
			// System.out.println(seg);

			final Coordinate encroachPt = findNonGabrielPoint(seg);
			// no encroachment found - segment must already be in subdivision
			if (encroachPt == null) {
				continue;
			}

			// compute split point
			splitPt = splitFinder.findSplitPoint(seg, encroachPt);
			final ConstraintVertex splitVertex = createVertex(splitPt, seg);

			// DebugFeature.addLineSegment(DEBUG_SEG_SPLIT, encroachPt, splitPt, "");
			// Debug.println(WKTWriter.toLineString(encroachPt, splitPt));

			/**
			 * Check whether the inserted point still equals the split pt. This will not be the case if the split pt was
			 * too close to an existing site. If the point was snapped, the triangulation will not respect the inserted
			 * constraint - this is a failure. This can be caused by:
			 * <ul>
			 * <li>An initial site that lies very close to a constraint segment The cure for this is to remove any
			 * initial sites which are close to constraint segments in a preprocessing phase.
			 * <li>A narrow constraint angle which causing repeated splitting until the split segments are too small.
			 * The cure for this is to either choose better split points or "guard" narrow angles by cracking the
			 * segments equidistant from the corner.
			 * </ul>
			 */
			final ConstraintVertex insertedVertex = insertSite(splitVertex);
			if (!insertedVertex.getCoordinate().equals2D(splitPt)) {
				Debug.println("Split pt snapped to: " + insertedVertex);
				// throw new ConstraintEnforcementException("Split point snapped to
				// existing point
				// (tolerance too large or constraint interior narrow angle?)",
				// splitPt);
			}

			// split segment and record the new halves
			final Segment s1 = new Segment(seg.getStartX(), seg.getStartY(), seg.getStartZ(), splitVertex.getX(),
					splitVertex.getY(), splitVertex.getZ(), seg.getData());
			final Segment s2 = new Segment(splitVertex.getX(), splitVertex.getY(), splitVertex.getZ(), seg.getEndX(),
					seg.getEndY(), seg.getEndZ(), seg.getData());
			newSegments.add(s1);
			newSegments.add(s2);
			segsToRemove.add(seg);

			splits = splits + 1;
		}
		segsToInsert.removeAll(segsToRemove);
		segsToInsert.addAll(newSegments);

		return splits;
	}

	// public static final String DEBUG_SEG_SPLIT = "C:\\proj\\CWB\\test\\segSplit.jml";

	/**
	 * Given a set of points stored in the kd-tree and a line segment defined by two points in this set, finds a
	 * {@link Coordinate} in the circumcircle of the line segment, if one exists. This is called the Gabriel point - if
	 * none exists then the segment is said to have the Gabriel condition. Uses the heuristic of finding the non-Gabriel
	 * point closest to the midpoint of the segment.
	 *
	 * @param p
	 *            start of the line segment
	 * @param q
	 *            end of the line segment
	 * @return a point which is non-Gabriel or null if no point is non-Gabriel
	 */
	private Coordinate findNonGabrielPoint(final Segment seg) {
		final Coordinate p = seg.getStart();
		final Coordinate q = seg.getEnd();
		// Find the mid point on the line and compute the radius of enclosing circle
		final Coordinate midPt = new Coordinate((p.x + q.x) / 2.0, (p.y + q.y) / 2.0);
		final double segRadius = p.distance(midPt);

		// compute envelope of circumcircle
		final Envelope env = new Envelope(midPt);
		env.expandBy(segRadius);
		// Find all points in envelope
		final List<?> result = kdt.query(env);

		// For each point found, test if it falls strictly in the circle
		// find closest point
		Coordinate closestNonGabriel = null;
		double minDist = Double.MAX_VALUE;
		for (final Object name : result) {
			final KdNode nextNode = (KdNode) name;
			final Coordinate testPt = nextNode.getCoordinate();
			// ignore segment endpoints
			if (testPt.equals2D(p) || testPt.equals2D(q)) {
				continue;
			}

			final double testRadius = midPt.distance(testPt);
			if (testRadius < segRadius) {
				// double testDist = seg.distance(testPt);
				final double testDist = testRadius;
				if (closestNonGabriel == null || testDist < minDist) {
					closestNonGabriel = testPt;
					minDist = testDist;
				}
			}
		}
		return closestNonGabriel;
	}

}