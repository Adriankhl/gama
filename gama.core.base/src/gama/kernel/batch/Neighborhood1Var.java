/*******************************************************************************************************
 *
 * gama.kernel.batch.Neighborhood1Var.java, in plugin gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.kernel.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gama.common.interfaces.experiment.IParameter;
import gama.kernel.experiment.ParametersSet;
import gama.runtime.exceptions.GamaRuntimeException;
import gama.runtime.scope.IScope;

public class Neighborhood1Var extends Neighborhood {

	public Neighborhood1Var(final List<IParameter.Batch> variables) {
		super(variables);
	}

	@Override
	public List<ParametersSet> neighbor(final IScope scope, final ParametersSet solution) throws GamaRuntimeException {
		final List<ParametersSet> neighbors = new ArrayList<ParametersSet>();
		for (final IParameter.Batch var : variables) {
			var.setValue(scope, solution.get(var.getName()));
			final Set<Object> neighborValues = var.neighborValues(scope);
			for (final Object val : neighborValues) {
				final ParametersSet newSol = new ParametersSet(solution);
				newSol.put(var.getName(), val);
				neighbors.add(newSol);
			}
		}
		neighbors.remove(solution);
		return neighbors;
	}
}
