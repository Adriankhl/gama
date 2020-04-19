/*******************************************************************************************************
 *
 * gama.util.graph.GraphObject.java, in plugin gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.util.graph;

import org.jgrapht.Graph;

/**
 * Class GraphObject.
 *
 * @author drogoul
 * @since 12 janv. 2014
 *
 */
public abstract class GraphObject<T extends IGraph<V, E>, V, E> {

	protected final T graph;
	protected double weight = Graph.DEFAULT_EDGE_WEIGHT;

	GraphObject(final T g, final double w) {
		graph = g;
		weight = w;
	}

	public void setWeight(final double w) {
		weight = w;
	}

	public abstract double getWeight();

	public boolean isNode() {
		return false;
	}

	public boolean isEdge() {
		return false;
	}
}
