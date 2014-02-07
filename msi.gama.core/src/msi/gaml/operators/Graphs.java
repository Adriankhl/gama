/*
 * GAMA - V1.4 http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen (Batch, GeoTools & JTS), 2009-2012
 * - Benoit Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gaml.operators;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.metamodel.topology.graph.GamaSpatialGraph;
import msi.gama.metamodel.topology.graph.GamaSpatialGraph.VertexRelationship;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.ITypeProvider;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;
import msi.gama.util.GamaMap;
import msi.gama.util.GamaPair;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gama.util.file.GamaFile;
import msi.gama.util.graph.GamaGraph;
import msi.gama.util.graph.GraphAlgorithmsHandmade;
import msi.gama.util.graph.IGraph;
import msi.gama.util.graph.layout.AvailableGraphLayouts;
import msi.gama.util.graph.loader.GraphLoader;
import msi.gama.util.matrix.GamaIntMatrix;
import msi.gama.util.matrix.GamaMatrix;
import msi.gama.util.path.GamaSpatialPath;
import msi.gama.util.path.IPath;
import msi.gaml.species.ISpecies;
import msi.gaml.types.GamaGraphType;
import msi.gaml.types.GamaPathType;
import msi.gaml.types.IType;

import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;

/**
 * Written by drogoul Modified on 13 avr. 2011
 * 
 * @todo Description
 * 
 */
public class Graphs {

	private static class IntersectionRelation implements VertexRelationship<IShape> {

		double tolerance;

		IntersectionRelation(final double t) {
			tolerance = t;
		}

		@Override
		public boolean related(final IScope scope, final IShape p1, final IShape p2) {
			return Spatial.Properties.intersects(
				Spatial.Transformations.enlarged_by(scope, p1.getGeometry(), tolerance),
				Spatial.Transformations.enlarged_by(scope, p2.getGeometry(), tolerance));
		}

		@Override
		public boolean equivalent(final IScope scope, final IShape p1, final IShape p2) {
			return p1 == null ? p2 == null : p1.getGeometry().equals(p2.getGeometry());
		}
	};

	private static class IntersectionRelationLine implements VertexRelationship<IShape> {

		IntersectionRelationLine() {}

		@Override
		public boolean related(final IScope scope, final IShape p1, final IShape p2) {
			return p1.getInnerGeometry().relate(p2.getInnerGeometry(), "****1****");
		}

		@Override
		public boolean equivalent(final IScope scope, final IShape p1, final IShape p2) {
			return p1 == null ? p2 == null : p1.getGeometry().equals(p2.getGeometry());
		}

	};

	private static class DistanceRelation implements VertexRelationship<IShape> {

		double distance;

		DistanceRelation(final double d) {
			distance = d;
		}

		/**
		 * @throws GamaRuntimeException
		 * @see msi.gama.util.graph.GamaSpatialGraph.VertexRelationship#related(msi.gama.interfaces.IScope,
		 *      msi.gama.interfaces.IGeometry, msi.gama.interfaces.IGeometry)
		 */
		@Override
		public boolean related(final IScope scope, final IShape g1, final IShape g2) {
			return Spatial.Relations.distance_to(scope, g1.getGeometry(), g2.getGeometry()) <= distance;
		}

		/**
		 * @throws GamaRuntimeException
		 * @see msi.gama.util.graph.GamaSpatialGraph.VertexRelationship#equivalent(msi.gama.interfaces.IGeometry,
		 *      msi.gama.interfaces.IGeometry)
		 */
		@Override
		public boolean equivalent(final IScope scope, final IShape p1, final IShape p2) {
			return p1 == null ? p2 == null : p1.getGeometry().equals(p2.getGeometry());
		}

	}

	@operator(value = "agent_from_geometry")
	@doc(value = "returns the agent corresponding to given geometry (right-hand operand) in the given path (left-hand operand).", special_cases = "if the left-hand operand is nil, returns nil", examples = {
		"let line type: geometry <- one_of(path_followed.segments);",
		"let ag type: road <- road(path_followed agent_from_geometry line);" })
	public static IAgent getAgentFromGeom(final IPath path, final IShape geom) {
		if ( path == null ) { return null; }
		return (IAgent) path.getRealObject(geom);
	}

	/*
	 * TO DO : CHECK THE VALIDITY OF THESE OPERATORS FOR ALL KINDS OF PATH
	 * 
	 * @operator(value = "vertices")
	 * public static GamaList nodesOfPath(final GamaPath path) {
	 * if ( path == null ) { return new GamaList(); }
	 * return path.getVertexList();
	 * }
	 * 
	 * @operator(value = "edges")
	 * public static GamaList edgesOfPath(final GamaPath path) {
	 * if ( path == null ) { return new GamaList(); }
	 * return path.getEdgeList();
	 * }
	 */

	@operator(value = "contains_vertex")
	@doc(value = "returns true if the graph(left-hand operand) contains the given vertex (righ-hand operand), false otherwise", special_cases = "if the left-hand operand is nil, returns false", examples = {
		"let graphFromMap type: graph <-  as_edge_graph([{1,5}::{12,45},{12,45}::{34,56}]);",
		"graphFromMap contains_vertex {1,5}  --: true" }, see = { "contains_edge" })
	public static Boolean containsVertex(final GamaGraph graph, final Object vertex) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the contains_vertex operator, the graph should not be null!"); }
		return graph.containsVertex(vertex);
	}

	@operator(value = "contains_edge")
	@doc(value = "returns true if the graph(left-hand operand) contains the given edge (righ-hand operand), false otherwise", special_cases = "if the left-hand operand is nil, returns false", examples = {
		"let graphFromMap type: graph <-  as_edge_graph([{1,5}::{12,45},{12,45}::{34,56}]);",
		"graphFromMap contains_edge link({1,5}::{12,45})  --: true" }, see = { "contains_vertex" })
	public static Boolean containsEdge(final IGraph graph, final Object edge) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the contains_edge operator, the graph should not be null!"); }
		return graph.containsEdge(edge);
	}

	@operator(value = "contains_edge")
	@doc(special_cases = "if the right-hand operand is a pair, returns true if it exists an edge between the two elements of the pair in the graph", examples = {
		"let graphEpidemio type: graph <- generate_barabasi_albert( [\"edges_specy\"::edge,\"vertices_specy\"::node,\"size\"::3,\"m\"::5] );",
		"graphEpidemio contains_edge (node(0)::node(3));   --:   true" })
	public static Boolean containsEdge(final IGraph graph, final GamaPair edge) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the contains_edge operator, the graph should not be null!"); }
		return graph.containsEdge(edge.first(), edge.last());
	}

	@operator(value = "source_of", type = ITypeProvider.FIRST_CONTENT_TYPE)
	@doc(value = "returns the source of the edge (right-hand operand) contained in the graph given in left-hand operand.", special_cases = "if the lef-hand operand (the graph) is nil, throws an Exception", examples = {
		"let graphEpidemio type: graph <- generate_barabasi_albert( [\"edges_specy\"::edge,\"vertices_specy\"::node,\"size\"::3,\"m\"::5] );",
		"graphEpidemio source_of(edge(3)) 				--:  node1",
		"let graphFromMap type: graph <-  as_edge_graph([{1,5}::{12,45},{12,45}::{34,56}]);",
		"graphFromMap source_of(link({1,5}::{12,45}))  	--: {1.0;5.0}" }, see = { "target_of" })
	public static Object sourceOf(final IGraph graph, final Object edge) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the source_of operator, the graph should not be null!"); }
		if ( graph.containsEdge(edge) ) { return graph.getEdgeSource(edge); }
		return null;
	}

	@operator(value = "target_of", type = ITypeProvider.FIRST_CONTENT_TYPE)
	@doc(value = "returns the target of the edge (right-hand operand) contained in the graph given in left-hand operand.", special_cases = "if the lef-hand operand (the graph) is nil, returns nil", examples = {
		"let graphEpidemio type: graph <- generate_barabasi_albert( [\"edges_specy\"::edge,\"vertices_specy\"::node,\"size\"::3,\"m\"::5] );",
		"graphEpidemio source_of(edge(3)) 				--:  node1",
		"let graphFromMap type: graph <-  as_edge_graph([{1,5}::{12,45},{12,45}::{34,56}]);",
		"graphFromMap source_of(link({1,5}::{12,45}))  	--: {1.0;5.0}" }, see = "source_of")
	public static Object targetOf(final IGraph graph, final Object edge) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the target_of operator, the graph should not be null!"); }
		if ( graph.containsEdge(edge) ) { return graph.getEdgeTarget(edge); }
		return null;
	}

	@operator(value = "weight_of")
	@doc(value = "returns the weight of the given edge (right-hand operand) contained in the graph given in right-hand operand.", comment = "In a localized graph, an edge has a weight by default (the distance between both vertices).", special_cases = {
		"if the left-operand (the graph) is nil, returns nil",
		"if the right-hand operand is not an edge of the given graph, weight_of checks whether it is a node of the graph and tries to return its weight",
		"if the right-hand operand is neither a node, nor an edge, returns 1." }, examples = {
		"let graphFromMap type: graph <-  as_edge_graph([{1,5}::{12,45},{12,45}::{34,56}]);",
		"graphFromMap source_of(link({1,5}::{12,45}))  --: 41.48493702538308" })
	public static Double weightOf(final IGraph graph, final Object edge) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the weight_of operator, the graph should not be null!"); }
		if ( graph.containsEdge(edge) ) {
			return graph.getEdgeWeight(edge);
		} else if ( graph.containsVertex(edge) ) { return graph.getVertexWeight(edge); }
		return 1d;
	}

	@operator(value = "in_edges_of", content_type = ITypeProvider.FIRST_CONTENT_TYPE)
	@doc(value = "returns the list of the in-edges of a vertex (right-hand operand) in the graph given as left-hand operand.", examples = { "graphFromMap in_edges_of node({12,45})  --:  [LineString]" }, see = "out_edges_of")
	public static IList inEdgesOf(final IGraph graph, final Object vertex) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the in_edges_of operator, the graph should not be null!"); }
		if ( graph.containsVertex(vertex) ) { return new GamaList(graph.incomingEdgesOf(vertex)); }
		return new GamaList();
	}

	@operator(value = "edge_between", content_type = ITypeProvider.FIRST_CONTENT_TYPE)
	@doc(value = "returns the edge linking two nodes", examples = { "graphFromMap edge_between node1::node2  --:  edge1" }, see = {
		"out_edges_of", "in_edges_of" })
	public static Object EdgeBetween(final IGraph graph, final GamaPair verticePair) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the edge_between operator, the graph should not be null!"); }
		if ( graph.containsVertex(verticePair.key) && graph.containsVertex(verticePair.value) ) { return graph.getEdge(
			verticePair.key, verticePair.value); }
		return null;
	}

	@operator(value = "in_degree_of")
	@doc(value = "returns the in degree of a vertex (right-hand operand) in the graph given as left-hand operand.", examples = { "graphEpidemio in_degree_of (node(3))   --:  2" }, see = {
		"out_degree_of", "degree_of" })
	public static int inDregreeOf(final IGraph graph, final Object vertex) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the in_degree_of operator, the graph should not be null!"); }
		if ( graph.containsVertex(vertex) ) { return graph.inDegreeOf(vertex); }
		return 0;
	}

	@operator(value = "out_edges_of", content_type = ITypeProvider.FIRST_CONTENT_TYPE)
	@doc(value = "returns the list of the out-edges of a vertex (right-hand operand) in the graph given as left-hand operand.", examples = { "graphEpidemio out_edges_of (node(3))" }, see = "in_edges_of")
	public static IList outEdgesOf(final IGraph graph, final Object vertex) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the out_edges_of operator, the graph should not be null!"); }
		if ( graph.containsVertex(vertex) ) { return new GamaList(graph.outgoingEdgesOf(vertex)); }
		return new GamaList();
	}

	@operator(value = "out_degree_of")
	@doc(value = "returns the out degree of a vertex (right-hand operand) in the graph given as left-hand operand.", examples = { "graphEpidemio out_degree_of (node(3))" }, see = {
		"in_degree_of", "degree_of" })
	public static int outDregreeOf(final IGraph graph, final Object vertex) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the in_degree_of operator, the graph should not be null!"); }
		if ( graph.containsVertex(vertex) ) { return graph.outDegreeOf(vertex); }
		return 0;
	}

	@operator(value = "degree_of")
	@doc(value = "returns the degree (in+out) of a vertex (right-hand operand) in the graph given as left-hand operand.", examples = { "graphEpidemio degree_of (node(3))" }, see = {
		"in_degree_of", "out_degree_of" })
	public static int degreeOf(final IGraph graph, final Object vertex) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the in_degree_of operator, the graph should not be null!"); }
		if ( graph.containsVertex(vertex) ) { return graph.degreeOf(vertex); }
		return 0;
	}
	
	@operator(value = "connected_components_of")
	@doc(value = "returns the connected components of of a graph, i.e. the list of all vertices that are in the maximally connected component together with the specified vertex. ", examples = { "list<list> <- connected_components_of (my_graph)" }, see = {
			"alpha_index","connectivity_index", "nb_cycles" })
	public static List<List> connectedComponentOf(final IGraph graph) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the nb_connected_components_of operator, the graph should not be null!"); }

		ConnectivityInspector ci;
		if (graph.isDirected())
			ci= new ConnectivityInspector((DirectedGraph) graph);
		else 
			ci= new ConnectivityInspector((UndirectedGraph) graph);
		List<List> results = new GamaList<List>();
		for (Object obj : ci.connectedSets()) {
			results.add(new GamaList((Set) obj));
			
		}
		return results;
	}
	
	@operator(value = "beta_index")
	@doc(value = "returns the beta index of the graph (Measures the level of connectivity in a graph and is expressed by the relationship between the number of links (e) over the number of nodes (v) : beta = e/v.", examples = { "beta_index(graphEpidemio)" }, see = {
		"alpha_index","gamma_index","nb_cycles","connectivity_index" })
	public static double betaIndex(final IGraph graph) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the beta_index operator, the graph should not be null!"); }
		return graph.getEdges().size() / graph.getVertices().size();
	}
	
	@operator(value = "gamma_index")
	@doc(value = "returns the gamma index of the graph (A measure of connectivity that considers the relationship between the number of observed links and the number of possible links: gamma = e/(3 * (v - 2)) - for planar graph.", examples = { "gamma_index(graphEpidemio)" }, see = {
		"alpha_index","beta_index","nb_cycles","connectivity_index" })
	public static double gammaIndex(final IGraph graph) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the gamma_index operator, the graph should not be null!"); }
		return graph.getEdges().size() / (2 * graph.getVertices().size() - 5);
	}
	
	@operator(value = "connectivity_index")
	@doc(value = "retruns a simple connetivity index. This number is estimated through the number of nodes (v) and of sub-graphs (p) : IC = (v - p) /(v - 1).", examples = { "connectivity_index(graphEpidemio)" }, see = {
		"alpha_index","beta_index","gamma_index","nb_cycles" })
	public static double connectivityIndex(final IGraph graph) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the connectivity_index operator, the graph should not be null!"); }
		int S = graph.getVertices().size();
		int C = connectedComponentOf(graph).size();
		return (S-C)/(S-1);
	}

	

	@operator(value = "nb_cycles")
	@doc(value = "returns the maximum number of independent cycles in a graph. This number (u) is estimated through the number of nodes (v), links (e) and of sub-graphs (p): u = e - v + p.", examples = { "nb_cycles(graphEpidemio)" }, see = {
		"alpha_index","beta_index","gamma_index","connectivity_index" })
	public static int nbCycles(final IGraph graph) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the nb_cycles operator, the graph should not be null!"); }
		int S = graph.getVertices().size();
		int C = connectedComponentOf(graph).size();
		int L = graph.getEdges().size();
		return (L-S+C);
	}

	
	@operator(value = "betweenness_centrality")
	@doc(value = "returns a map containing for each vertex (key), its betweenness centrality (value): number of shortest paths passing through each vertex ", examples = { "betweenness_centrality(graphEpidemio)" }, see = {
		 })
	public static GamaMap betweennessCentrality(final IGraph graph) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the betweenness_centrality operator, the graph should not be null!"); }
		//java.lang.System.out.println("result.getRaw() : " + result.getRaw());
		
		GamaMap mapResult = new GamaMap();
		
		for (Object v1 : graph.getVertices()) {
			for (Object v2 : graph.getVertices()) {
				if (v1 == v2) continue;
				List edges = graph.computeBestRouteBetween(v1, v2);
				if (edges == null) continue;
				for (Object edge :edges) {
					Object node = graph.getEdgeTarget(edge);
					if (node != v2) {
						Double val = (Double) mapResult.get(node);
						if (val == null) {val = 1.0;}
						else {val += 1;}
						mapResult.put(node, val);
					}
				}
			}
		}
		return mapResult;
	}
	
	@operator(value = "alpha_index")
	@doc(value = "returns the alpha index of the graph (measure of connectivity which evaluates the number of cycles in a graph in comparison with the maximum number of cycles. The higher the alpha index, the more a network is connected. alpha = nb_cycles / (2*S-5) - planar graph)", examples = { "alpha_index(graphEpidemio)" }, see = {
		"beta_index","gamma_index","nb_cycles","connectivity_index" })
	public static double alphaIndex(final IGraph graph) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the alpha_index operator, the graph should not be null!"); }
		int S = graph.getVertices().size();
		return nbCycles(graph) / (2 * S-5);
	}

	
	@operator(value = "neighbours_of", content_type = ITypeProvider.FIRST_CONTENT_TYPE)
	@doc(value = "returns the list of neighbours of the given vertex (right-hand operand) in the given graph (left-hand operand)", examples = {
		"graphEpidemio neighbours_of (node(3)) 		--:	[node0,node2]",
		"graphFromMap neighbours_of node({12,45}) 	--: [{1.0;5.0},{34.0;56.0}]" }, see = { "predecessors_of",
		"successors_of" })
	public static IList neighboursOf(final IGraph graph, final Object vertex) {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the neighbours_of operator, the graph should not be null!"); }
		if ( graph.containsVertex(vertex) ) { return new GamaList(org.jgrapht.Graphs.neighborListOf(graph, vertex)); }
		return new GamaList();
	}

	@operator(value = "predecessors_of", content_type = ITypeProvider.FIRST_CONTENT_TYPE)
	@doc(value = "returns the list of predecessors (i.e. sources of in edges) of the given vertex (right-hand operand) in the given graph (left-hand operand)", examples = {
		"graphEpidemio predecessors_of (node(3)) 		--: [node0,node2]",
		"graphFromMap predecessors_of node({12,45}) 	--:	[{1.0;5.0}]" }, see = { "neighbours_of", "successors_of" })
	public static IList predecessorsOf(final IGraph graph, final Object vertex) {
		if ( graph.containsVertex(vertex) ) { return new GamaList(org.jgrapht.Graphs.predecessorListOf(graph, vertex)); }
		return new GamaList();
	}

	@operator(value = "successors_of", content_type = ITypeProvider.FIRST_CONTENT_TYPE)
	@doc(value = "returns the list of successors (i.e. targets of out edges) of the given vertex (right-hand operand) in the given graph (left-hand operand)", examples = {
		"graphEpidemio successors_of (node(3)) 		--: []", "graphFromMap successors_of node({12,45}) 	--: [{34.0;56.0}]" }, see = {
		"predecessors_of", "neighbours_of" })
	public static IList successorsOf(final IGraph graph, final Object vertex) {
		if ( graph.containsVertex(vertex) ) { return new GamaList(org.jgrapht.Graphs.successorListOf(graph, vertex)); }
		return new GamaList();
	}

	// @operator(value = "graph_from_edges")
	// public static IGraph fromEdges(final IScope scope, final GamaList edges) {
	// return new GamaGraph(edges, true, false);
	// }

	@operator(value = "as_edge_graph", content_type = IType.GEOMETRY, index_type = IType.GEOMETRY)
	@doc(value = "creates a graph from the list/map of edges given as operand", special_cases = "if the operand is a list, the graph will be built with elements of the list as vertices", examples = { "as_edge_graph([{1,5},{12,45},{34,56}])  --:  build a graph with these three vertices and reflexive links on each vertices" }, see = {
		"as_intersection_graph", "as_distance_graph" })
	public static IGraph spatialFromEdges(final IScope scope, final IContainer edges) {
		return new GamaSpatialGraph(edges, true, false, null, null, scope);
	}

	// @operator(value = "graph_from_edges")
	// public static IGraph fromEdges(final IScope scope, final GamaMap edges) {
	// Edges are represented by pairs of vertex::vertex
	// return GamaGraphType.from(edges, false);
	// }

	@operator(value = "as_edge_graph")
	@doc(special_cases = "if the operand is a map, the graph will be built by creating edges from pairs of the map", examples = "as_edge_graph([{1,5}::{12,45},{12,45}::{34,56}])  --:  build a graph with these three vertices and two edges")
	public static IGraph spatialFromEdges(final IScope scope, final GamaMap edges) {
		// Edges are represented by pairs of vertex::vertex

		return GamaGraphType.from(scope, edges, true);
	}

	// @operator(value = "graph_from_vertices")
	// public static IGraph fromVertices(final IScope scope, final GamaList vertices) {
	// return new GamaGraph(vertices, false, false);
	// }

	@operator(value = "as_intersection_graph", content_type = IType.GEOMETRY, index_type = IType.GEOMETRY)
	@doc(value = "creates a graph from a list of vertices (left-hand operand). An edge is created between each pair of vertices with an intersection (with a given tolerance).", comment = "as_intersection_graph is more efficient for a list of geometries (but less accurate) than as_distance_graph.", examples = "list(ant) as_intersection_graph 0.5;", see = {
		"as_distance_graph", "as_edge_graph" })
	public static IGraph spatialFromVertices(final IScope scope, final IContainer vertices, final Double tolerance) {
		return new GamaSpatialGraph(vertices, false, false, new IntersectionRelation(tolerance), null, scope);
	}

	public static IGraph spatialLineIntersection(final IScope scope, final IContainer vertices) {
		return new GamaSpatialGraph(vertices, false, false, new IntersectionRelationLine(), null, scope);
	}

	@operator(value = "as_distance_graph", content_type = IType.GEOMETRY, index_type = IType.GEOMETRY)
	@doc(value = "creates a graph from a list of vertices (left-hand operand). An edge is created between each pair of vertices close enough (less than a distance, right-hand operand).", comment = "as_distance_graph is more efficient for a list of points than as_intersection_graph.", examples = "list(ant) as_distance_graph 3.0;", see = {
		"as_intersection_graph", "as_edge_graph" })
	public static IGraph spatialDistanceGraph(final IScope scope, final IContainer vertices, final Double distance) {
		return new GamaSpatialGraph(vertices, false, false, new DistanceRelation(distance), null, scope);
	}

	@operator(value = "as_distance_graph", content_type = IType.GEOMETRY, index_type = IType.GEOMETRY)
	@doc(value = "creates a graph from a list of vertices (left-hand operand). An edge is created between each pair of vertices close enough (less than a distance, right-hand operand).", comment = "as_distance_graph is more efficient for a list of points than as_intersection_graph.", examples = "list(ant) as_distance_graph 3.0;", see = {
		"as_intersection_graph", "as_edge_graph" })
	public static IGraph spatialDistanceGraph(final IScope scope, final IContainer vertices, final Double distance,
		final ISpecies edgeSpecies) {
		return new GamaSpatialGraph(vertices, false, false, new DistanceRelation(distance), edgeSpecies, scope);
	}

	@operator(value = "as_distance_graph")
	@doc(value = "creates a graph from a list of vertices (left-hand operand). An edge is created between each pair of vertices close enough (less than a distance, right-hand operand).", comment = "as_distance_graph is more efficient for a list of points than as_intersection_graph.", examples = "list(ant) as_distance_graph 3.0;", see = {
		"as_intersection_graph", "as_edge_graph" })
	public static IGraph spatialDistanceGraph(final IScope scope, final IContainer vertices, final GamaMap params) {
		Double distance = (Double) params.get("distance");
		ISpecies edgeSpecies = (ISpecies) params.get("species");
		return new GamaSpatialGraph(vertices, false, false, new DistanceRelation(distance), edgeSpecies, scope);
	}

	@operator(value = "spatial_graph")
	@doc(value = "allows to create a spatial graph from a container of vertices, without trying to wire them. The container can be empty. Emits an error if the contents of the container are not geometries, points or agents", see = { "graph" })
	public static IGraph spatial_graph(final IScope scope, final IContainer vertices) {
		return new GamaSpatialGraph(vertices, false, false, null, null, scope);
	}

	// @operator(value = "spatialize")
	// public static IGraph asSpatialGraph(final GamaGraph g) {
	// return GamaGraphType.asSpatialGraph(g);
	// }

	// @operator(value = "unspatialize")
	// public static IGraph asRegularGraph(final GamaGraph g) {
	// return GamaGraphType.asRegularGraph(g);
	// }

	@operator(value = "use_cache")
	@doc(value = "if the second operand is true, the operand graph will store in a cache all the previously computed shortest path (the cache be cleared if the graph is modified).", comment = "the operator alters the operand graph, it does not create a new one.", see = { "path_between" })
	public static IGraph useCacheForShortestPaths(final IGraph g, final boolean useCache) {
		return GamaGraphType.useChacheForShortestPath(g, useCache);
	}
	
	@operator(value = "directed")
	@doc(value = "the operand graph becomes a directed graph.", comment = "the operator alters the operand graph, it does not create a new one.", see = { "undirected" })
	public static IGraph asDirectedGraph(final IGraph g) {
		g.incVersion();
		return GamaGraphType.asDirectedGraph(g);
	}

	@operator(value = "undirected")
	@doc(value = "the operand graph becomes an undirected graph.", comment = "the operator alters the operand graph, it does not create a new one.", see = { "directed" })
	public static IGraph asUndirectedGraph(final IGraph g) {
		g.incVersion();
		return GamaGraphType.asUndirectedGraph(g);
	}

	@operator(value = "with_weights")
	@doc(value = "returns the graph (left-hand operand) with weight given in the map (right-hand operand).", comment = "this operand re-initializes the path finder", special_cases = "if the left-hand operand is a map, the map should contains pairs such as: vertex/edge::double", examples = "graph_from_edges (list(ant) as_map each::one_of (list(ant))) with_weights (list(ant) as_map each::each.food)")
	public static IGraph withWeights(final IScope scope, final IGraph graph, final GamaMap weights) {
		// a map of vertex/edge::double to provide weights
		// Example : graph_from_edges (list ant as_map each::one_of (list ant)) with_weights (list
		// ant as_map each::each.food)
		graph.setWeights(weights);
		graph.incVersion();
		if ( graph instanceof GamaSpatialGraph ) {
			((GamaSpatialGraph) graph).reInitPathFinder();
		}
		return graph;
	}

	@operator(value = "with_weights")
	@doc(special_cases = "if the right-hand operand is a list, affects the n elements of the list to the n first edges. "
		+ "Note that the ordering of edges may change overtime, which can create some problems...")
	public static IGraph withWeights(final IScope scope, final IGraph graph, final IList weights) {
		// Simply a list of double... and, by default, for edges.However, the ordering of edges may
		// change overtime, which can create a problem somewhere...
		IList edges = graph.getEdges();
		int n = edges.size();
		if ( n != weights.size() ) { return graph; }
		for ( int i = 0; i < n; i++ ) {
			graph.setEdgeWeight(edges.get(i), Cast.asFloat(scope, weights.get(i)));
		}
		graph.incVersion();
		if ( graph instanceof GamaSpatialGraph ) {
			((GamaSpatialGraph) graph).reInitPathFinder();
		}
		return graph;
	}

	@operator(value = "with_optimizer_type")
	@doc(value = "changes the shortest path computation method of the griven graph", comment = "the right-hand operand can be \"Djikstra\", \"Bellmann\", \"Astar\" to use the associated algorithm. "
		+ "Note that these methods are dynamic: the path is computed when needed. In contrarily, if the operand is another string, "
		+ "a static method will be used, i.e. all the shortest are previously computed.", examples = "set graphEpidemio <- graphEpidemio with_optimizer_type \"static\";", see = "set_verbose")
	public static IGraph setOptimizeType(final IScope scope, final IGraph graph, final String optimizerType) {
		graph.setOptimizerType(optimizerType);
		return graph;
	}

	@operator(value = "add_node")
	@doc(value = "adds a node in a graph.", comment = ".", examples = "node(0) add_node graph;    --: 	returns the graph with node(0)")
	public static IGraph addNode(final IShape node, final IGraph g) {
		g.addVertex(node);
		g.incVersion();
		return g;
	}

	@operator(value = "remove_node_from")
	@doc(value = "removes a node from a graph.", comment = "all the edges containing this node are also removed.", examples = "node(0) remove_node_from graphEpidemio;    --: 	returns the graph without node(0)")
	public static IGraph removeNodeFrom(final IShape node, final IGraph g) {
		g.removeVertex(node);
		g.incVersion();

		return g;
	}

	@operator(value = "rewire_p")
	@doc(value = "Rewires a graph (in the Watts-Strogatz meaning)", deprecated = "Does not work now", examples = "set graphEpidemio <- graphEpidemio rewire_p 0.2;", see = "rewire_p")
	public static IGraph rewireGraph(final IGraph g, final Double probability) {
		GraphAlgorithmsHandmade.rewireGraphProbability(g, probability);
		g.incVersion();
		return g;
	}

	@operator(value = "rewire_n")
	@doc(value = "rewires the given count of edges.", comment = "If there are too many edges, all the edges will be rewired.", examples = "set graphEpidemio <- graphEpidemio rewire_n 10;", see = "rewire_p")
	public static IGraph rewireGraph(final IGraph g, final Integer count) {
		GraphAlgorithmsHandmade.rewireGraphCount(g, count);
		g.incVersion();
		return g;
	}

	@operator(value = "add_edge")
	@doc(value = "add an edge between source vertex and the target vertex", comment = "If the edge already exists the graph is unchanged", examples = "set graph <- graph add_edge (source::target);", see = "")
	public static IGraph addEdge(final IGraph g, final GamaPair nodes) {
		g.addEdge(nodes.first(), nodes.last());
		g.incVersion();
		return g;
	}

	@operator(value = "path_between", content_type = ITypeProvider.FIRST_CONTENT_TYPE)
	@doc(value = "The shortest path between a list of two objects in a graph", examples = { "my_graph path_between (ag1:: ag2) --: A path between ag1 and ag2" })
	public static IPath path_between(final IScope scope, final GamaGraph graph, final GamaPair sourTarg)
		throws GamaRuntimeException {
		// java.lang.System.out.println("Cast.asTopology(scope, graph) : " + Cast.asTopology(scope, graph));
		return Cast.asTopology(scope, graph).pathBetween(scope, (IShape) sourTarg.key, (IShape) sourTarg.value);

		// return graph.computeShortestPathBetween(sourTarg.key, sourTarg.value);

	}
	
	@operator(value = "K_path_between", content_type = ITypeProvider.FIRST_CONTENT_TYPE)
	@doc(value = "The K shortest paths between a list of two objects in a graph", examples = { "K_path_between(my_graph, ag1:: ag2, 2) --: the 2 shortest paths (ordered by length) between ag1 and ag2" })
	public static List<GamaSpatialPath> Kpaths_between(final IScope scope, final GamaGraph graph, final GamaPair sourTarg, final int k)
		throws GamaRuntimeException {
		// java.lang.System.out.println("Cast.asTopology(scope, graph) : " + Cast.asTopology(scope, graph));
		return Cast.asTopology(scope, graph).KpathsBetween(scope, (IShape) sourTarg.key, (IShape) sourTarg.value,k);

		// return graph.computeShortestPathBetween(sourTarg.key, sourTarg.value);

	}

	@operator(value = "as_path", content_type = ITypeProvider.FIRST_CONTENT_TYPE)
	@doc(value = "create a graph path from the list of shape", examples = { "[road1,road2,road3] as_path my_graph --: A path road1->road2->road3 of my_graph" })
	public static IPath as_path(final IScope scope, final GamaList<IShape> edgesNodes, final GamaGraph graph)
		throws GamaRuntimeException {
		// java.lang.System.out.println("Cast.asTopology(scope, graph) : " + Cast.asTopology(scope, graph));
		IPath path = GamaPathType.staticCast(scope, edgesNodes, null);
		path.setGraph(graph);
		return path;

		// return graph.computeShortestPathBetween(sourTarg.key, sourTarg.value);

	}

	/**
	 * the comment for all the operators
	 */
	static private final String comment =
		"Available formats: "
			+ "\"pajek\": Pajek (Slovene word for Spider) is a program, for Windows, for analysis and visualization of large networks. See: http://pajek.imfm.si/doku.php?id=pajek for more details."
			+
			// "\"dgs_old\", \"dgs\": DGS is a file format allowing to store graphs and dynamic graphs in a textual human readable way, yet with a small size allowing to store large graphs. Graph dynamics is defined using events like adding, deleting or changing a node or edge. With DGS, graphs will therefore be seen as stream of such events. [From GraphStream related page: http://graphstream-project.org/]"+
			"\"lgl\": LGL is a compendium of applications for making the visualization of large networks and trees tractable. See: http://lgl.sourceforge.net/ for more details."
			+ "\"dot\": DOT is a plain text graph description language. It is a simple way of describing graphs that both humans and computer programs can use. See: http://en.wikipedia.org/wiki/DOT_language for more details."
			+ "\"edge\": This format is a simple text file with numeric vertex ids defining the edges."
			+ "\"gexf\": GEXF (Graph Exchange XML Format) is a language for describing complex networks structures, their associated data and dynamics. Started in 2007 at Gephi project by different actors, deeply involved in graph exchange issues, the gexf specifications are mature enough to claim being both extensible and open, and suitable for real specific applications. See: http://gexf.net/format/ for more details."
			+ "\"graphml\": GraphML is a comprehensive and easy-to-use file format for graphs based on XML. See: http://graphml.graphdrawing.org/ for more details."
			+ "\"tlp\" or \"tulip\": TLP is the Tulip software graph format. See: http://tulip.labri.fr/TulipDrupal/?q=tlp-file-format for more details. "
			+ "\"ncol\": This format is used by the Large Graph Layout progra. It is simply a symbolic weighted edge list. It is a simple text file with one edge per line. An edge is defined by two symbolic vertex names separated by whitespace. (The symbolic vertex names themselves cannot contain whitespace.) They might followed by an optional number, this will be the weight of the edge. See: http://bioinformatics.icmb.utexas.edu/lgl for more details."
			+ "The map operand should includes following elements:";

	// version depuis un filename avec edge et specy et indication si spatial ou pas

	@operator(value = "load_graph_from_file")
	@doc(value = "returns a graph loaded from a given file encoded into a given format. The last boolean parameter indicates whether the resulting graph will be considered as spatial or not by GAMA", comment = comment, special_cases = {
		"\"format\": the format of the file", "\"filename\": the filename of the file containing the network",
		"\"edges_specy\": the species of edges", "\"vertices_specy\": the species of vertices" }, examples = {
		"graph<myVertexSpecy,myEdgeSpecy> myGraph <- load_graph_from_file(", "			\"pajek\",",
		"			\"./example_of_Pajek_file\",", "			myVertexSpecy,", "			myEdgeSpecy , true);" }, see = "TODO")
	public static IGraph primLoadGraphFromFile(final IScope scope, final String format, final String filename,
		final ISpecies vertex_specy, final ISpecies edge_specy, final Boolean spatial) throws GamaRuntimeException {

		return GraphLoader.loadGraph(scope, filename, vertex_specy, edge_specy, null, null, format, spatial);

	}

	@operator(value = "load_graph_from_file")
	@doc(value = "returns a graph loaded from a given file encoded into a given format. This graph will not be spatial.", comment = comment, special_cases = {
		"\"format\": the format of the file", "\"filename\": the filename of the file containing the network",
		"\"edges_specy\": the species of edges", "\"vertices_specy\": the species of vertices" }, examples = {
		"graph<myVertexSpecy,myEdgeSpecy> myGraph <- load_graph_from_file(", "			\"pajek\",",
		"			\"./example_of_Pajek_file\",", "			myVertexSpecy,", "			myEdgeSpecy );" }, see = "TODO")
	public static IGraph primLoadGraphFromFile(final IScope scope, final String format, final String filename,
		final ISpecies vertex_specy, final ISpecies edge_specy) throws GamaRuntimeException {

		return primLoadGraphFromFile(scope, format, filename, vertex_specy, edge_specy, false);

	}

	@operator(value = "load_graph_from_file")
	@doc(special_cases = { "\"filename\": the filename of the file containing the network",
		"\"edges_specy\": the species of edges", "\"vertices_specy\": the species of vertices" }, examples = {
		"graph<myVertexSpecy,myEdgeSpecy> myGraph <- load_graph_from_file(", "			\"pajek\",",
		"			\"./example_of_Pajek_file\",", "			myVertexSpecy,", "			myEdgeSpecy );" }, see = "TODO")
	public static IGraph primLoadGraphFromFile(final IScope scope, final String filename, final ISpecies vertex_specy,
		final ISpecies edge_specy) throws GamaRuntimeException {

		return primLoadGraphFromFile(scope, null, filename, vertex_specy, edge_specy);

	}

	// version depuis un file avec edge et specy

	@operator(value = "load_graph_from_file")
	@doc(special_cases = { "\"format\": the format of the file", "\"file\": the file containing the network",
		"\"edges_specy\": the species of edges", "\"vertices_specy\": the species of vertices" }, examples = {
		"graph<myVertexSpecy,myEdgeSpecy> myGraph <- load_graph_from_file(", "			\"pajek\",",
		"			\"example_of_Pajek_file\",", "			myVertexSpecy,", "			myEdgeSpecy );" }, see = "TODO")
	public static IGraph primLoadGraphFromFile(final IScope scope, final String format, final GamaFile<?, ?> gamaFile,
		final ISpecies vertex_specy, final ISpecies edge_specy) throws GamaRuntimeException {
		return primLoadGraphFromFile(scope, gamaFile.getPath(), vertex_specy, edge_specy);

	}

	// version depuis un filename sans edge et sans specy

	@operator(value = "load_graph_from_file")
	@doc(special_cases = { "\"format\": the format of the file",
		"\"filename\": the filename of the file containing the network" }, examples = {
		"graph<myVertexSpecy,myEdgeSpecy> myGraph <- load_graph_from_file(", "			\"pajek\",",
		"			\"example_of_Pajek_file\");" }, see = "TODO")
	public static IGraph primLoadGraphFromFile(final IScope scope, final String format, final String filename)
		throws GamaRuntimeException {
		// AD 29/09/13: Changed the previous code that was triggering an overflow.
		return primLoadGraphFromFile(scope, format, filename, null, null);
	}

	// version depuis un file avec edge et specy

	@operator(value = "load_graph_from_file")
	@doc(special_cases = { "\"format\": the format of the file", "\"file\": the file containing the network", }, examples = {
		"graph<myVertexSpecy,myEdgeSpecy> myGraph <- load_graph_from_file(", "			\"pajek\",",
		"			\"example_of_Pajek_file\");" }, see = "TODO")
	public static IGraph primLoadGraphFromFile(final IScope scope, final String format, final GamaFile<?, ?> gamaFile)
		throws GamaRuntimeException {
		// AD 29/09/13 : Simply called the previous method with the path of the file. Not efficient, but should work.
		return primLoadGraphFromFile(scope, format, gamaFile.getPath());
		// throw GamaRuntimeException.error("not implemented: loading from gama file");

	}

	@operator(value = "load_graph_from_file")
	@doc(special_cases = { "\"file\": the file containing the network" }, examples = {
		"graph<myVertexSpecy,myEdgeSpecy> myGraph <- load_graph_from_file(", "			\"pajek\",",
		"			\"example_of_Pajek_file\");" }, see = "TODO")
	public static IGraph primLoadGraphFromFile(final IScope scope, final String filename) throws GamaRuntimeException {
		return primLoadGraphFromFile(scope, null, filename);
	}

	/*
	 * public static IGraph addRandomEdges(final IGraph g, final Double probability) {
	 * GraphAlgorithmsHandmade.rewireGraph(g, probability);
	 * return g;
	 * }
	 */
	
	@operator(value = "load_shortest_paths")
	@doc(value = "put in the graph cache the computed shortest paths contained in the matrix (rows: source, columns: target)", examples = { "my_graph load_shortest_paths(shortest_paths_matrix) --: return my_graph with all the shortest paths computed" })
	public static IGraph primLoadGraphFromFile(final IScope scope, final GamaGraph graph, final GamaMatrix matrix)
		throws GamaRuntimeException {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the load_shortest_paths operator, the graph should not be null!"); }
		if ( graph.getVertices().size() != matrix.numCols || graph.getVertices().size() != matrix.numRows ) { throw GamaRuntimeException
			.error("In the load_shortest_paths operator, the number of vertices of the graph should be equal to the number of rows and columns of the matrix!"); }
		graph.loadShortestPaths(matrix);
		return graph;
		// throw GamaRuntimeException.error("not implemented: loading from gama file");

	}
	

	@operator(value = "save_shortest_paths")
	@doc(value = "return a matrix containing all the shortest paths (rows: source, columns: target)", examples = { "matrix shortest_paths_matrix <- save_shortest_paths(my_graph); --: shortest_paths_matrix will contain all the shortest paths" })
	public static GamaIntMatrix primSaveGraphFromFile(final IScope scope, final GamaGraph graph)
		throws GamaRuntimeException {
		if ( graph == null ) { throw GamaRuntimeException
			.error("In the save_shortest_paths operator, the graph should not be null!"); }
		return graph.saveShortestPaths();
		// throw GamaRuntimeException.error("not implemented: loading from gama file");

	}
	
	
	@operator(value = "layout")
	@doc(value = "layouts a GAMA graph.", comment = "TODO", special_cases = { "TODO." }, examples = { "TODO;" }, see = { "TODO" })
	// TODO desc
	public static IGraph layoutOneshot(final IScope scope, final GamaGraph graph, final String layoutEngine,
		final int timeout, final GamaMap<String, Object> options) {

		// translate Gama options to
		Map<String, Object> jOptions = null;
		if ( options.isEmpty() ) {
			jOptions = Collections.EMPTY_MAP;
		} else {
			jOptions = new HashMap<String, Object>(options.size());
			for ( String key : options.keySet() ) {
				jOptions.put(key, options.get(scope, key));
			}
		}
		AvailableGraphLayouts
		// retrieve layout for he layout that was selected by the user (may raise an exception)
			.getStaticLayout(layoutEngine.trim().toLowerCase())
			// apply this layout with the options
			.doLayoutOneShot(scope, graph, timeout, jOptions);

		return graph;
	}

	@operator(value = "layout")
	@doc(value = "layouts a GAMA graph.", comment = "TODO", special_cases = { "TODO." }, examples = { "TODO;" }, see = { "TODO" })
	public static IGraph layoutOneshot(final IScope scope, final GamaGraph graph, final String layoutEngine,
		final int timeout) {
		return layoutOneshot(scope, graph, layoutEngine, timeout, new GamaMap<String, Object>());
	}

	@operator(value = "layout")
	@doc(value = "layouts a GAMA graph.", comment = "TODO", special_cases = { "TODO." }, examples = { "TODO;" }, see = { "TODO" })
	public static IGraph layoutOneshot(final IScope scope, final GamaGraph graph, final String layoutEngine) {
		return layoutOneshot(scope, graph, layoutEngine, -1);
	}

	// TODO "complete" (pour cr�er un graphe complet)

	// vertices_to_graph [vertices] with_weights (vertices collect: each.val) -> renvoie un graphe
	// construit � partir des vertex (edges g�n�r�s soit sous la forme d'une paire vertex::vertex,
	// soit sous la forme d'un lien g�om�trique)
	// vertices_to_graph [a1, a2, a3] with_weights ([1, 4, 8]) -> m�me chose
	// edges_to_graph [edges] with_weights (edges collect: each.length) -> renvoie un graphe
	// construit � partir des edges (vertex g�n�r�s soit sous la forme d'une paire edge::edge, soit
	// sous la forme d'un point pour les g�om�tries)
	// edges_to_graph [a1::a2, a2::a3] with_weights ([3.0, 1.3]) -> m�me chose
	// add item: v1 to:g weight: 1 -> ajout d'un vertex
	// add item: v1::v2 to:g weight:1 -> ajout d'un edge g�n�r� (et des vertex correspondants si
	// n�cessaire)
	// add item: (v1::v2)::e to: g weight: 1 -> edge (ajout d'un edge explicite et des vertex
	// correspondants si n�cessaire)
	// remove item: v1::v2 from: g -> remove edge
	// remove item: o from: g -> remove edge / vertex
	// put item: e2 at: v1::v2 in: g -> replace/add an edge (on peut aussi faire la m�me chose pour
	// remplacer un vertex)

	// TODO Transformer peu � peu toutes les primitives (GeometricFunctions, GeometricSkill, etc.)
	// en op�rateurs (as_graph, as_network, as_triangle_graph, as_complete_graph -- En cr�ant les
	// liens dynamiques correspondants --, as_weighted_graph ...).

	// TODO Ajouter les op�rateurs d'union, d'intersection, d'�galit�, de diff�rence

	// TODO Ajouter des g�n�rateurs sp�cifiques a partir de GraphGenerator (pb: quelles classes pour
	// les vertices/edges ??

}
