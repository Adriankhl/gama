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
package msi.gaml.batch;

import java.util.*;
import msi.gama.interfaces.IParameter;
import msi.gama.kernel.exceptions.GamaRuntimeException;

public class Neighborhood1Var extends Neighborhood {

	public Neighborhood1Var(final List<IParameter.Batch> variables) {
		super(variables);
	}

	@Override
	public List<Solution> neighbor(final Solution solution) throws GamaRuntimeException {
		final List<Solution> neighbors = new ArrayList<Solution>();
		for ( final IParameter.Batch var : variables ) {
			final Set<Object> neighbourValues = var.neighbourValues();
			for ( final Object val : neighbourValues ) {
				final Solution newSol = new Solution(solution);
				newSol.put(var.getName(), val);
				neighbors.add(newSol);
			}
		}
		neighbors.remove(solution);
		return neighbors;
	}
}
