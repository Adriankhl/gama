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
import msi.gama.interfaces.*;
import msi.gama.kernel.GAMA;
import msi.gama.kernel.exceptions.GamaRuntimeException;
import msi.gama.kernel.experiment.*;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.*;
import msi.gama.util.Cast;

@symbol(name = IBatch.GENETIC, kind = ISymbolKind.BATCH_METHOD)
@inside(kinds = { ISymbolKind.EXPERIMENT })
@facets({
	@facet(name = ISymbol.NAME, type = IType.ID, optional = false),
	@facet(name = GeneticAlgorithm.POP_DIM, type = IType.INT_STR, optional = true),
	@facet(name = GeneticAlgorithm.CROSSOVER_PROB, type = IType.FLOAT_STR, optional = true),
	@facet(name = GeneticAlgorithm.MUTATION_PROB, type = IType.FLOAT_STR, optional = true),
	@facet(name = GeneticAlgorithm.NB_GEN, type = IType.INT_STR, optional = true),
	@facet(name = GeneticAlgorithm.MAX_GEN, type = IType.INT_STR, optional = true),
	@facet(name = ISymbol.MAXIMIZE, type = IType.FLOAT_STR, optional = true),
	@facet(name = ISymbol.MINIMIZE, type = IType.FLOAT_STR, optional = true),
	@facet(name = ISymbol.AGGREGATION, type = IType.LABEL, optional = true, values = { ISymbol.MIN,
		ISymbol.MAX }) })
public class GeneticAlgorithm extends ParamSpaceExploAlgorithm {

	private int						populationDim		= 3;
	private double					crossoverProb		= 0.7;
	private double					mutationProb		= 0.1;
	private int						nbPrelimGenerations	= 1;
	private int						maxGenerations		= 20;

	private Initialization			initPop;
	private CrossOver				crossOverOp;
	private Mutation				mutationOp;
	private Selection				selectionOp;

	protected static final String	POP_DIM				= "pop_dim";
	protected static final String	CROSSOVER_PROB		= "crossover_prob";
	protected static final String	MUTATION_PROB		= "mutation_prob";
	protected static final String	NB_GEN				= "nb_prelim_gen";
	protected static final String	MAX_GEN				= "max_gen";

	public GeneticAlgorithm(final IDescription species) {
		super(species);
	}

	@Override
	public void initializeFor(final BatchExperiment f) throws GamaRuntimeException {
		super.initializeFor(f);
		initPop = new InitializationUniform();
		crossOverOp = new CrossOver1Pt();
		mutationOp = new Mutation1Var();
		selectionOp = new SelectionRoulette();
		final IExpression popDim = getFacet(POP_DIM);
		if ( popDim != null ) {
			populationDim = Cast.asInt(popDim.value(GAMA.getDefaultScope()));
		}
		final IExpression crossOverPb = getFacet(CROSSOVER_PROB);
		if ( crossOverPb != null ) {
			crossoverProb = Cast.asFloat(crossOverPb.value(GAMA.getDefaultScope()));
		}
		final IExpression mutationPb = getFacet(MUTATION_PROB);
		if ( mutationPb != null ) {
			mutationProb = Cast.asFloat(mutationPb.value(GAMA.getDefaultScope()));
		}
		final IExpression nbprelimgen = getFacet(NB_GEN);
		if ( nbprelimgen != null ) {
			nbPrelimGenerations = Cast.asInt(nbprelimgen.value(GAMA.getDefaultScope()));
		}
		final IExpression maxgen = getFacet(MAX_GEN);
		if ( maxgen != null ) {
			maxGenerations = Cast.asInt(maxgen.value(GAMA.getDefaultScope()));
		}

	}

	@Override
	public Solution findBestSolution() throws GamaRuntimeException {
		List<IParameter.Batch> variables = currentExperiment.getParametersToExplore();
		testedSolutions = new Hashtable<Solution, Double>();
		List<Chromosome> population =
			initPop.initializePop(variables, currentExperiment, populationDim, nbPrelimGenerations,
				isMaximize());
		bestFitness = isMaximize() ? Double.MIN_VALUE : Double.MAX_VALUE;
		int nbGen = 1;
		while (nbGen <= maxGenerations) {
			Set<Chromosome> children = new HashSet<Chromosome>();
			for ( final Chromosome chromosome : population ) {
				if ( getRandUniform().nextValue() < crossoverProb ) {
					children.addAll(crossOverOp.crossOver(chromosome,
						population.get(GAMA.getRandom().between(0, population.size() - 1))));
				}
			}
			population.addAll(children);
			children = null;

			Set<Chromosome> mutatePop = new HashSet<Chromosome>();
			for ( final Chromosome chromosome : population ) {
				if ( getRandUniform().nextValue() < mutationProb ) {
					mutatePop.add(mutationOp.mutate(chromosome, variables));
				}
			}
			population.addAll(mutatePop);
			mutatePop = null;
			computePopFitness(population);
			population = selectionOp.select(population, populationDim, isMaximize());
			nbGen++;
		}
		// System.out.println("Best solution : " + bestSolution + "  fitness : "
		// + bestFitness);
		return bestSolution;
	}

	private void computePopFitness(final List<Chromosome> population) throws GamaRuntimeException {
		for ( final Chromosome chromosome : population ) {
			final Solution sol =
				chromosome.convertToSolution(currentExperiment.getParametersToExplore());
			Double fitness = testedSolutions.get(sol);
			if ( fitness == null ) {
				fitness = Double.valueOf(currentExperiment.launchSimulationsWithSolution(sol));
			}
			testedSolutions.put(sol, fitness);

			chromosome.setFitness(fitness.doubleValue());
			if ( (isMaximize() && fitness.doubleValue() > bestFitness) || (!isMaximize() &&
				fitness.doubleValue() < bestFitness) ) {
				bestFitness = fitness.doubleValue();
				bestSolution = sol;
			}
		}
	}

	@Override
	public void addParametersTo(final BatchExperiment exp) {
		super.addParametersTo(exp);
		exp.addMethodParameter(new ParameterAdapter("Mutation probability",
			IExperiment.BATCH_CATEGORY_NAME, IType.FLOAT) {

			@Override
			public Object value() {
				return mutationProb;
			}

		});
		exp.addMethodParameter(new ParameterAdapter("Crossover probability",
			IExperiment.BATCH_CATEGORY_NAME, IType.FLOAT) {

			@Override
			public Object value() {
				return crossoverProb;
			}

		});
		exp.addMethodParameter(new ParameterAdapter("Population dimension",
			IExperiment.BATCH_CATEGORY_NAME, IType.INT) {

			@Override
			public Object value() {
				return populationDim;
			}

		});
		exp.addMethodParameter(new ParameterAdapter("Preliminary number of generations",
			IExperiment.BATCH_CATEGORY_NAME, IType.FLOAT) {

			@Override
			public Object value() {
				return nbPrelimGenerations;
			}

		});
		exp.addMethodParameter(new ParameterAdapter("Max. number of generations",
			IExperiment.BATCH_CATEGORY_NAME, IType.FLOAT) {

			@Override
			public Object value() {
				return maxGenerations;
			}

		});
	}

}
