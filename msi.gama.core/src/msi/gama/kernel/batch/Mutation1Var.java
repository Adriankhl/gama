/*******************************************************************************************************
 *
 * msi.gama.kernel.batch.Mutation1Var.java, in plugin msi.gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gama.kernel.batch;

import java.util.List;

import msi.gama.common.interfaces.batch.Mutation;
import msi.gama.common.interfaces.experiment.IParameter;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.runtime.scope.IScope;

public class Mutation1Var implements Mutation {

	public Mutation1Var() {
	}

	@Override
	public Chromosome mutate(final IScope scope, final Chromosome chromosome, final List<IParameter.Batch> variables)
			throws GamaRuntimeException {
		final Chromosome chromoMut = new Chromosome(chromosome);

		final int indexMut = scope.getRandom().between(0, chromoMut.getGenes().length - 1);
		final String varStr = chromoMut.getPhenotype()[indexMut];
		IParameter.Batch var = null;
		for (final IParameter.Batch p : variables) {
			if (p.getName().equals(varStr)) {
				var = p;
				break;
			}
		}
		// TODO Lourd et pas du tout optimis�.
		if (var != null) {
			chromoMut.setGene(scope, var, indexMut);
		}
		return chromoMut;
	}

}
