/*
 * GAMA - V1.4  http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2011
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2011
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen  (Batch, GeoTools & JTS), 2009-2011
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2011
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gama.util.graph;

import msi.gama.interfaces.IGeometry;
import msi.gama.kernel.exceptions.GamaRuntimeException;
import msi.gama.util.*;

public class _SpatialEdge extends _Edge<IGeometry> {

	public _SpatialEdge(final GamaSpatialGraph graph, final Object edge, final Object source,
		final Object target) throws GamaRuntimeException {
		super(graph, edge, source, target);
	}

	@Override
	protected void init(final Object edge, final Object source, final Object target)
		throws GamaRuntimeException {
		if ( !(edge instanceof IGeometry) ) {
			// storedObject = (IGeometry) edge;
			// } else {
			throw new GamaRuntimeException(Cast.toGaml(edge) + " is not a geometry");
		}
		super.init(edge, source, target);
	}

	@Override
	protected void buildSource(final Object edge, final Object source) {
		Object s = source;
		IGeometry g = (IGeometry) edge;
		if ( s == null ) {
			GamaPoint c1 = g.getGeometry().getPoints().get(0);
			s = findVertexWithCoordinates(c1);
		}
		super.buildSource(edge, s);
	}

	@Override
	protected void buildTarget(final Object edge, final Object target) {
		Object s = target;
		IGeometry g = (IGeometry) edge;
		if ( s == null ) {
			GamaPoint c1 = g.getGeometry().getPoints().last();
			s = findVertexWithCoordinates(c1);
		}
		super.buildTarget(edge, s);
	}

	private Object findVertexWithCoordinates(final GamaPoint c) {
		for ( Object vertex : graph.vertexSet() ) {
			// _SpatialVertex internal = getVertex(vertex);
			if ( vertex instanceof IGeometry && ((IGeometry) vertex).getLocation().equals(c) ) { return vertex; }
		}
		IGeometry vertex = new GamaPoint(c);
		graph.addVertex(vertex);
		return vertex;
	}

	@Override
	public double getWeight(final Object storedObject) {
		double w = super.getWeight(storedObject);
		if ( storedObject instanceof IGeometry ) {
			w *= ((IGeometry) storedObject).getInnerGeometry().getLength(); // A voir...
		}
		return w;
	}
}