/*******************************************************************************************************
 *
 * msi.gama.kernel.batch.InitializationUniform.java, in plugin msi.gama.core, is part of the source code of the GAMA
 * modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.kernel.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import msi.gama.common.interfaces.batch.Initialization;
import msi.gama.common.interfaces.experiment.IParameter;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.runtime.scope.IScope;

public class InitializationUniform implements Initialization {

	public InitializationUniform() {}

	@Override
	public List<Chromosome> initializePop(final IScope scope, final List<IParameter.Batch> variables,
			final GeneticAlgorithm algo) throws GamaRuntimeException {
		final Set<Chromosome> populationInit = new HashSet<Chromosome>();
		final int nbPrelimGenerations = algo.getNbPrelimGenerations();
		final int populationDim = algo.getPopulationDim();
		for (int i = 0; i < nbPrelimGenerations; i++) {
			for (int j = 0; j < populationDim; j++) {
				populationInit.add(new Chromosome(scope, variables, true));
			}
		}
		for (final Chromosome chromosome : populationInit) {
			algo.computeChroFitness(scope, chromosome);
		}
		final List<Chromosome> populationInitOrd = new ArrayList<>(populationInit);
		Collections.sort(populationInitOrd);
		if (algo.isMaximize) {
			Collections.reverse(populationInitOrd);
		}
		return populationInitOrd.subList(0, populationDim);
	}

}
