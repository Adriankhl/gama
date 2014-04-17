/*********************************************************************************************
 * 
 * 
 * 'ExperimentSpecies.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.kernel.experiment;

import java.util.*;
import msi.gama.common.interfaces.*;
import msi.gama.common.util.*;
import msi.gama.kernel.batch.*;
import msi.gama.kernel.experiment.ExperimentSpecies.BatchValidator;
import msi.gama.kernel.model.IModel;
import msi.gama.kernel.simulation.SimulationAgent;
import msi.gama.metamodel.agent.IMacroAgent;
import msi.gama.outputs.*;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.GamlAnnotations.validator;
import msi.gama.precompiler.*;
import msi.gama.runtime.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.TOrderedHashMap;
import msi.gaml.compilation.*;
import msi.gaml.descriptions.IDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.species.GamlSpecies;
import msi.gaml.types.IType;
import msi.gaml.variables.IVariable;

/**
 * Written by drogoul Modified on 28 mai 2011
 * Apr. 2013: Important modifications to enable running true experiment agents
 * 
 * Principe de base du batch :
 * - si batch, créer un agent "spécial" (BatchExperimentAgent ?)
 * - faire que ce soit lui qui gère tout ce qu'il y a dans BatchExperimentSpecies
 * 
 * 
 * @todo Description
 * 
 */
@symbol(name = { IKeyword.EXPERIMENT }, kind = ISymbolKind.EXPERIMENT, with_sequence = true)
@facets(value = {
	@facet(name = IKeyword.NAME, type = IType.LABEL, optional = false),
	@facet(name = IKeyword.TITLE, type = IType.LABEL, optional = false),
	@facet(name = IKeyword.PARENT, type = IType.ID, optional = true),
	@facet(name = IKeyword.SKILLS, type = IType.LIST, optional = true),
	@facet(name = IKeyword.CONTROL, type = IType.ID, optional = true),
	@facet(name = IKeyword.FREQUENCY, type = IType.INT, optional = true),
	@facet(name = IKeyword.SCHEDULES, type = IType.CONTAINER, optional = true),
	@facet(name = IKeyword.KEEP_SEED, type = IType.BOOL, optional = true),
	@facet(name = IKeyword.REPEAT, type = IType.INT, optional = true),
	@facet(name = IKeyword.UNTIL, type = IType.BOOL, optional = true),
	@facet(name = IKeyword.MULTICORE, type = IType.BOOL, optional = true),
	@facet(name = IKeyword.TYPE,
		type = IType.LABEL,
		values = { IKeyword.BATCH, IKeyword.REMOTE, IKeyword.GUI_ },
		optional = false) }, omissible = IKeyword.NAME)
@inside(kinds = { ISymbolKind.SPECIES, ISymbolKind.MODEL })
@validator(BatchValidator.class)
public class ExperimentSpecies extends GamlSpecies implements IExperimentSpecies {

	public static class BatchValidator implements IDescriptionValidator {

		/**
		 * Method validate()
		 * @see msi.gaml.compilation.IDescriptionValidator#validate(msi.gaml.descriptions.IDescription)
		 */
		@Override
		public void validate(final IDescription desc) {
			String type = desc.getFacets().getLabel(IKeyword.TYPE);
			if ( !type.equals(IKeyword.BATCH) ) { return; }
			if ( !desc.getFacets().containsKey(IKeyword.UNTIL) ) {
				desc.warning(
					"No stop condition have been defined (facet 'until:'). This may result in an endless run of the simulation",
					IGamlIssue.MISSING_FACET);
			}
		}
	}

	protected IOutputManager simulationOutputs;
	protected IOutputManager experimentOutputs;
	private ItemList parametersEditors;
	protected final Map<String, IParameter> parameters = new TOrderedHashMap();
	protected final Map<String, IParameter.Batch> explorableParameters = new TOrderedHashMap();
	protected ExperimentAgent agent;
	protected final Scope stack = new Scope(null);
	protected IModel model;
	protected IExploration exploration;
	// private BatchOutput fileOutputDescription;
	private FileOutput log;

	//hqnghi: manage experiment's controller
	private String controllerName="";

	public String getControllerName() {
		return controllerName;
	}

	public void setControllerName(String controllerName) {
		this.controllerName = controllerName;
	}
	//end-hqnghi
	
	@Override
	public ExperimentAgent getAgent() {
		return agent;
	}

	public ExperimentSpecies(final IDescription description) {
		super(description);
		setName(description.getName());
		String type = description.getFacets().getLabel(IKeyword.TYPE);
		if ( type.equals(IKeyword.BATCH) ) {
			exploration = new ExhaustiveSearch(null);
		}
	}

	@Override
	public void dispose() {
		parametersEditors = null;
		if ( agent != null ) {
			GAMA.releaseScope(agent.getScope());
			agent.dispose();
			agent = null;
		}
		if ( simulationOutputs != null ) {
			simulationOutputs.dispose();
			simulationOutputs = null;
		}
		if ( experimentOutputs != null ) {
			experimentOutputs.dispose();
			experimentOutputs = null;
		}
		parameters.clear();

		// Should be put somewhere around here, but probably not here exactly.
		// ProjectionFactory.reset();
		super.dispose();
	}

	public void createAgent() {
		final ExperimentPopulation pop = new ExperimentPopulation(this);
		final IScope scope = getExperimentScope();
		pop.initializeFor(scope);
		agent = (ExperimentAgent) pop.createAgents(scope, 1, Collections.EMPTY_LIST, false).get(0);
		addDefaultParameters();
	}

	@Override
	public IModel getModel() {
		return model;
	}

	@Override
	public void setModel(final IModel model) {
		this.model = model;
		if ( !isBatch() ) {
			for ( final IVariable v : model.getVars() ) {
				if ( v.isParameter() ) {
					// GuiUtils.debug("from ExperimentSpecies.setModel:");
					IParameter p = new ExperimentParameter(stack, v);
					final String name = p.getName();
					boolean already = parameters.containsKey(name);
					if ( !already ) {
						parameters.put(name, p);
					}
					// boolean registerParameter = !already;
				}

			}
		}
	}

	protected void addDefaultParameters() {
		for ( IParameter.Batch p : agent.getDefaultParameters() ) {
			addParameter(p);
		}
	}

	@Override
	public final IOutputManager getSimulationOutputs() {
		return simulationOutputs;
	}

	@Override
	public final IOutputManager getExperimentOutputs() {
		return experimentOutputs;
	}

	@Override
	public void setChildren(final List<? extends ISymbol> children) {
		super.setChildren(children);
		// We first verify if we are in a batch -- or normal -- situation
		for ( final ISymbol s : children ) {
			if ( s instanceof IExploration /* && (s.hasFacet(IKeyword.MAXIMIZE) || s.hasFacet(IKeyword.MINIMIZE)) */) {
				exploration = (IExploration) s;
				break;
			}
		}
		if ( exploration != null ) {
			children.remove(exploration);
		}

		BatchOutput fileOutputDescription = null;
		for ( final ISymbol s : children ) {
			if ( s instanceof BatchOutput ) {
				fileOutputDescription = (BatchOutput) s;
			} else if ( s instanceof SimulationOutputManager ) {
				if ( simulationOutputs != null ) {
					((SimulationOutputManager) simulationOutputs).setChildren(new ArrayList(((AbstractOutputManager) s)
						.getOutputs().values()));
				} else {
					simulationOutputs = (SimulationOutputManager) s;
				}
			} else if ( s instanceof IParameter.Batch ) {
				IParameter.Batch pb = (IParameter.Batch) s;
				if ( isBatch() ) {
					if ( pb.canBeExplored() ) {
						pb.setEditable(false);
						addExplorableParameter(pb);
						continue;
					}
				}
				IParameter p = (IParameter) s;
				final String name = p.getName();
				boolean already = parameters.containsKey(name);
				if ( !already ) {
					parameters.put(name, p);
				}
			} else if ( s instanceof ExperimentOutputManager ) {
				if ( experimentOutputs != null ) {
					((ExperimentOutputManager) experimentOutputs).setChildren(new ArrayList(((AbstractOutputManager) s)
						.getOutputs().values()));
				} else {
					experimentOutputs = (ExperimentOutputManager) s;
				}
			}
		}
		if ( simulationOutputs == null ) {
			simulationOutputs = new SimulationOutputManager(null);
		}
		if ( experimentOutputs == null ) {
			experimentOutputs = new ExperimentOutputManager(null);
		}
		if ( fileOutputDescription != null ) {
			createOutput(fileOutputDescription);
		}
	}

	private void createOutput(final BatchOutput output) throws GamaRuntimeException {
		// TODO revoir tout ceci. Devrait plut�t �tre une commande
		if ( output == null ) { return; }
		IExpression data = output.getFacet(IKeyword.DATA);
		if ( data == null ) {
			data = exploration.getFitnessExpression();
		}
		String dataString = data == null ? "time" : data.toGaml();
		log = new FileOutput(output.getLiteral(IKeyword.TO), dataString, new ArrayList(parameters.keySet()), this);
	}

	@Override
	public void open() {
		createAgent();
		// GuiUtils.prepareForExperiment(this);
		agent.schedule();
		if ( isBatch() ) {
			GuiUtils.informStatus(" Batch ready ");
		}
	}

	@Override
	public void reload() {
		if ( isBatch() ) {
			agent.dispose();
			parametersEditors = null;
			open();
		} else {
			agent.reset();
			agent.init(agent.getScope());
		}
	}

	@Override
	public ItemList getParametersEditors() {
		if ( parameters.isEmpty() && explorableParameters.isEmpty() ) { return null; }
		if ( parametersEditors == null ) {
			Collection<IParameter> params = new ArrayList(getParameters().values());
			params.addAll(explorableParameters.values());
			parametersEditors = new ExperimentsParametersList(params);
		}
		return parametersEditors;
	}

	// @Override
	@Override
	public boolean isBatch() {
		return exploration != null;
	}

	@Override
	public boolean isGui() {
		return true;
	}

	@Override
	public IScope getExperimentScope() {
		return stack;
	}

	// @Override
	// public boolean hasParameters() {
	// return targetedVars.size() != 0;
	// }

	// @Override
	public void setParameterValue(final IScope scope, final String name, final Object val) throws GamaRuntimeException {
		checkGetParameter(name).setValue(scope, val);
	}

	// @Override
	public Object getParameterValue(final String name) throws GamaRuntimeException {
		return checkGetParameter(name).value();
	}

	@Override
	public boolean hasParameter(final String name) {
		return getParameter(name) != null;
	}

	public IParameter.Batch getParameter(final String name) {
		final IParameter p = parameters.get(name);
		if ( p != null && p instanceof IParameter.Batch ) { return (IParameter.Batch) p; }
		return null;
	}

	public void addParameter(final IParameter p) {
		// GuiUtils.debug("ExperimentSpecies.addParameter " + p.getName());
		// TODO Verify this
		final String name = p.getName();
		IParameter already = parameters.get(name);
		if ( already != null ) {
			p.setValue(stack, already.getInitialValue(stack));
		}
		parameters.put(name, p);
	}

	protected IParameter.Batch checkGetParameter(final String name) throws GamaRuntimeException {
		final IParameter.Batch v = getParameter(name);
		if ( v == null ) { throw GamaRuntimeException.error("No parameter named " + name + " in experiment " +
			getName()); }
		return v;
	}

	@Override
	public Map<String, IParameter> getParameters() {
		return parameters;
		// result.addAll(systemParameters);
		// return result;
	}

	@Override
	public SimulationAgent getCurrentSimulation() {
		if ( agent == null ) { return null; }
		return (SimulationAgent) agent.getSimulation();
	}

	//
	// @Override
	// public boolean isLoading() {
	// if ( agent == null ) { return false; }
	// return agent.isLoading();
	// }

	/**
	 * A short-circuited scope that represents the scope of the experiment. If a simulation is
	 * available, it refers to it and gains access to its global scope. If not, it throws the
	 * appropriate runtime exceptions when a feature dependent on the existence of a simulation is
	 * accessed
	 * 
	 * @author Alexis Drogoul
	 * @since November 2011
	 */
	private class Scope extends AbstractScope {

		/**
		 * A flag indicating that the experiment is shutting down. As this scope is used before any experiment agent
		 * (and runtime scope) is defined, it is necessary to define it here.
		 */
		private volatile boolean interrupted;

		public Scope(final IMacroAgent root) {
			super(root);
		}

		@Override
		public void setGlobalVarValue(final String name, final Object v) throws GamaRuntimeException {
			if ( hasParameter(name) ) {
				setParameterValue(this, name, v);
				GuiUtils.updateParameterView(ExperimentSpecies.this);
				return;
			}
			SimulationAgent a = getCurrentSimulation();
			if ( a != null ) {
				a.setDirectVarValue(this, name, v);
			}
		}

		@Override
		public Object getGlobalVarValue(final String name) throws GamaRuntimeException {
			if ( hasParameter(name) ) { return getParameterValue(name); }
			SimulationAgent a = getCurrentSimulation();
			if ( a != null ) { return a.getDirectVarValue(this, name); }
			return null;
		}

		@Override
		public SimulationAgent getSimulationScope() {
			return getCurrentSimulation();
		}

		@Override
		public IExperimentAgent getExperiment() {
			return (ExperimentAgent) root;
		}

		@Override
		public IDescription getExperimentContext() {
			return ExperimentSpecies.this.getDescription();
		}

		@Override
		public IDescription getModelContext() {
			return ExperimentSpecies.this.getModel().getDescription();
		}

		@Override
		public IModel getModel() {
			return ExperimentSpecies.this.getModel();
		}

		@Override
		protected boolean _root_interrupted() {
			return interrupted;
		}

		@Override
		public void setInterrupted(final boolean interrupted) {
			this.interrupted = interrupted;
		}

		@Override
		public IScope copy() {
			return new Scope(root);
		}

		/**
		 * Method getRandom()
		 * @see msi.gama.runtime.IScope#getRandom()
		 */
		@Override
		public RandomUtils getRandom() {
			SimulationAgent a = getCurrentSimulation();
			if ( a != null ) { return a.getExperiment().getRandomGenerator(); }
			return null;
		}

	}

	@Override
	public IExploration getExplorationAlgorithm() {
		return exploration;
	}

	@Override
	public FileOutput getLog() {
		return log;
	}

	public void addExplorableParameter(final IParameter.Batch p) {
		p.setCategory(EXPLORABLE_CATEGORY_NAME);
		p.setUnitLabel(null);
		explorableParameters.put(p.getName(), p);
	}

	@Override
	public Map<String, IParameter.Batch> getExplorableParameters() {
		return explorableParameters;
	}
}
