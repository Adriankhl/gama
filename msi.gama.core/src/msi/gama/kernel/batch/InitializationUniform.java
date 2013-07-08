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
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gama.kernel.batch;

import java.util.*;
import msi.gama.kernel.experiment.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;

public class InitializationUniform implements Initialization {

	public InitializationUniform() {}

	@Override
	public List<Chromosome> initializePop(final List<IParameter.Batch> variables, final BatchAgent exp,
		final int populationDim, final int nbPrelimGenerations, final boolean isMaximize) throws GamaRuntimeException {
		final Set<Chromosome> populationInit = new HashSet<Chromosome>();
		for ( int i = 0; i < nbPrelimGenerations; i++ ) {
			for ( int j = 0; j < populationDim; j++ ) {
				populationInit.add(new Chromosome(variables, true));
			}
		}
		for ( final Chromosome chromosome : populationInit ) {
			final ParametersSet sol = chromosome.convertToSolution(variables);
			chromosome.setFitness(exp.launchSimulationsWithSolution(sol));
		}
		for ( final Chromosome chromosome1 : populationInit ) {
			final ParametersSet sol = chromosome1.convertToSolution(variables);
			chromosome1.setFitness(exp.launchSimulationsWithSolution(sol));
		}
		final List<Chromosome> populationInitOrd = new ArrayList<Chromosome>(populationInit);
		Collections.sort(populationInitOrd);
		final List<Chromosome> populationInitFinal = new ArrayList<Chromosome>();
		if ( !isMaximize ) {
			for ( int i = 0; i < populationDim; i++ ) {
				populationInitFinal.add(populationInitOrd.get(i));
			}
		} else {
			for ( int i = populationInitOrd.size() - 1; i > populationInitOrd.size() - populationDim - 1; i-- ) {
				populationInitFinal.add(populationInitOrd.get(i));
			}
		}
		return populationInitFinal;
	}

}
