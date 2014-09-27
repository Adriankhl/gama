/*********************************************************************************************
 * 
 * 
 * 'GamlAgent.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.metamodel.agent;

import gnu.trove.map.hash.THashMap;
import java.util.*;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.util.RandomUtils;
import msi.gama.kernel.experiment.IExperimentAgent;
import msi.gama.metamodel.population.*;
import msi.gama.metamodel.shape.*;
import msi.gama.metamodel.topology.ITopology;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.species;
import msi.gama.runtime.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gama.util.graph.GamaGraph;
import msi.gaml.descriptions.ModelDescription;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.IStatement;
import msi.gaml.types.*;
import msi.gaml.variables.IVariable;
import com.google.common.collect.Iterables;

/**
 * The Class GamlAgent. Represents agents that can be manipulated in GAML. They are provided with
 * everything their species defines .
 */
@species(name = IKeyword.AGENT)
public class GamlAgent extends MinimalAgent implements IMacroAgent {

	/** The population that this agent belongs to. */
	protected final IPopulation population;
	// hqnghi manipulate micro-models
	protected GamaMap<String, IPopulation> externMicroPopulations = new GamaMap<String, IPopulation>();

	@Override
	public void addExternMicroPopulation(final String expName, final IPopulation pop) {
		externMicroPopulations.put(expName, pop);
	}

	@Override
	public IPopulation getExternMicroPopulationFor(final String expName) {
		if ( externMicroPopulations.size() > 0 ) { return externMicroPopulations.get(expName); }
		return null;
	}

	@Override
	public GamaMap<String, IPopulation> getExternMicroPopulations() {
		return externMicroPopulations;
	}

	// end-hqnghi

	protected IShape geometry;
	protected String name;

	/**
	 * @param s the population used to prototype the agent.
	 */
	public GamlAgent(final IPopulation s) {
		population = s;
	}

	@Override
	protected IPopulation checkedPopulation() {
		// The population is never null
		return population;
	}

	@Override
	protected IShape checkedGeometry() {
		// The geometry is never null (?)
		return getGeometry();
	}

	@Override
	public IPopulation getPopulation() {
		return population;
	}

	@Override
	public Object getDirectVarValue(final IScope scope, final String n) throws GamaRuntimeException {
		final IVariable var = population.getVar(this, n);
		if ( var != null ) { return var.value(scope, this); }
		final IAgent host = this.getHost();
		if ( host != null ) {
			final IVariable varOfHost = host.getPopulation().getVar(host, n);
			if ( varOfHost != null ) { return varOfHost.value(scope, host); }
		}
		// TODO: else ? launch an error ?
		return null;
	}

	@Override
	public void setDirectVarValue(final IScope scope, final String s, final Object v) throws GamaRuntimeException {
		final IVariable var = population.getVar(this, s);
		if ( var != null ) {
			var.setVal(scope, this, v);
		} else {
			final IAgent host = this.getHost();
			if ( host != null ) {
				final IVariable varOfHost = host.getPopulation().getVar(host, s);
				if ( varOfHost != null ) {
					varOfHost.setVal(scope, host, v);
				}
			}
		}
		// TODO: else ? launch an error ?
		// population.getVar(this, s).setVal(scope, this, v);
	}

	/**
	 * During the call to init, the agent will search for the action named _init_ and execute it. Its default
	 * implementation is provided in this class as well.
	 * @see GamlAgent#_init_()
	 * @see msi.gama.common.interfaces.IStepable#step(msi.gama.runtime.IScope)
	 * @warning This method should NOT be overriden (except for some rare occasions like in SimulationAgent). Always
	 *          override _init_(IScope) instead.
	 */
	@Override
	public boolean init(final IScope scope) {
		if ( !getSpecies().isInitOverriden() ) {
			_init_(scope);
		} else {
			executeCallbackAction(scope, getSpecies().getAction(ISpecies.initActionName));
		}
		return !scope.interrupted();
	}

	/**
	 * During the call to step, the agent will search for the action named _step_ and execute it. Its default
	 * implementation is provided in this class as well.
	 * @see GamlAgent#_step_()
	 * @see msi.gama.common.interfaces.IStepable#step(msi.gama.runtime.IScope)
	 * @warning This method should NOT be overriden (except for some rare occasions like in SimulationAgent). Always
	 *          override _step_(IScope) instead.
	 */
	@Override
	public boolean step(final IScope scope) {
		if ( !getSpecies().isStepOverriden() ) {
			_step_(scope);
		} else {
			executeCallbackAction(scope, getSpecies().getAction(ISpecies.stepActionName));
		}
		return !scope.interrupted();
	}

	/**
	 * Callback Actions
	 * 
	 */
	static Object[] callbackResult = new Object[1];

	protected Object executeCallbackAction(final IScope scope, final IStatement action) {
		scope.execute(action, this, null, callbackResult);
		return callbackResult[0];
	}

	@action(name = ISpecies.initActionName)
	public Object _init_(final IScope scope) {
		getSpecies().getArchitecture().init(scope);
		return this;
	}

	@action(name = ISpecies.stepActionName)
	public Object _step_(final IScope scope) {
		scope.update(this);
		// getPopulation().updateVariables(scope, this);
		// we ask the architecture to execute on this
		Object[] result = new Object[1];
		if ( scope.execute(getSpecies().getArchitecture(), this, null, result) ) {
			// we ask the sub-populations to step their agents
			return stepSubPopulations(scope);
		}
		return result[0];
	}

	protected Object stepSubPopulations(final IScope scope) {
		for ( IPopulation pop : getMicroPopulations() ) {
			// try {
			// for ( final IPopulation pop : ImmutableList.copyOf(getMicroPopulations()) ) {
			scope.step(pop);
		}
		// if ( !scope.step(pop) ) { return null; }
		// }
		// } catch (final GamaRuntimeException g) {
		// GAMA.reportError(g);
		// }
		return this;
	}

	@Override
	public IList<IAgent> captureMicroAgents(final IScope scope, final ISpecies microSpecies,
		final IList<IAgent> microAgents) throws GamaRuntimeException {
		if ( microAgents == null || microAgents.isEmpty() || microSpecies == null ||
			!this.getSpecies().getMicroSpecies().contains(microSpecies) ) { return GamaList.EMPTY_LIST; }

		final List<IAgent> candidates = new GamaList<IAgent>();
		for ( final IAgent a : microAgents.iterable(scope) ) {
			if ( this.canCapture(a, microSpecies) ) {
				candidates.add(a);
			}
		}
		final IList<IAgent> capturedAgents = new GamaList<IAgent>();
		final IPopulation microSpeciesPopulation = this.getPopulationFor(microSpecies);
		for ( final IAgent micro : candidates ) {
			final SavedAgent savedMicro = new SavedAgent(scope, micro);
			micro.dispose();
			capturedAgents.add(savedMicro.restoreTo(scope, microSpeciesPopulation));
		}
		return capturedAgents;
	}

	@Override
	public IAgent captureMicroAgent(final IScope scope, final ISpecies microSpecies, final IAgent microAgent)
		throws GamaRuntimeException {
		if ( this.canCapture(microAgent, microSpecies) ) {
			final IPopulation microSpeciesPopulation = this.getMicroPopulation(microSpecies);
			final SavedAgent savedMicro = new SavedAgent(scope, microAgent);
			microAgent.dispose();
			return savedMicro.restoreTo(scope, microSpeciesPopulation);
		}

		return null;
	}

	@Override
	public IList<IAgent> releaseMicroAgents(final IScope scope, final IList<IAgent> microAgents)
		throws GamaRuntimeException {
		IPopulation originalSpeciesPopulation;
		final IList<IAgent> releasedAgents = new GamaList<IAgent>();

		for ( final IAgent micro : microAgents.iterable(scope) ) {
			final SavedAgent savedMicro = new SavedAgent(scope, micro);
			originalSpeciesPopulation = micro.getPopulationFor(micro.getSpecies().getParentSpecies());
			micro.dispose();
			releasedAgents.add(savedMicro.restoreTo(scope, originalSpeciesPopulation));
		}
		return releasedAgents;
	}

	/**
	 * Migrates some micro-agents from one micro-species to another micro-species of this agent's
	 * species.
	 * 
	 * @param microAgent
	 * @param newMicroSpecies
	 * @return
	 */
	@Override
	public IList<IAgent> migrateMicroAgents(final IScope scope, final IList<IAgent> microAgents,
		final ISpecies newMicroSpecies) {
		final List<IAgent> immigrantCandidates = new GamaList<IAgent>();

		for ( final IAgent m : microAgents.iterable(scope) ) {
			if ( m.getSpecies().isPeer(newMicroSpecies) ) {
				immigrantCandidates.add(m);
			}
		}

		final IList<IAgent> immigrants = new GamaList<IAgent>();
		if ( !immigrantCandidates.isEmpty() ) {
			final IPopulation microSpeciesPopulation = this.getPopulationFor(newMicroSpecies);
			for ( final IAgent micro : immigrantCandidates ) {
				final SavedAgent savedMicro = new SavedAgent(scope, micro);
				micro.dispose();
				immigrants.add(savedMicro.restoreTo(scope, microSpeciesPopulation));
			}
		}

		return immigrants;
	}

	/**
	 * Migrates some micro-agents from one micro-species to another micro-species of this agent's
	 * species.
	 * 
	 * @param microAgent
	 * @param newMicroSpecies
	 * @return
	 */
	@Override
	public IList<IAgent> migrateMicroAgents(final IScope scope, final ISpecies oldMicroSpecies,
		final ISpecies newMicroSpecies) {
		final IPopulation oldMicroPop = this.getPopulationFor(oldMicroSpecies);

		final IPopulation newMicroPop = this.getPopulationFor(newMicroSpecies);
		final IList<IAgent> immigrants = new GamaList<IAgent>();
		final Iterator<IAgent> it = oldMicroPop.iterator();
		while (it.hasNext()) {
			final IAgent m = it.next();
			final SavedAgent savedMicro = new SavedAgent(scope, m);
			m.dispose();
			immigrants.add(savedMicro.restoreTo(scope, newMicroPop));
		}

		return immigrants;
	}

	/** Variables which are not saved during the capture and release process. */
	private static final List<String> UNSAVABLE_VARIABLES = Arrays.asList(IKeyword.PEERS, IKeyword.AGENTS,
		IKeyword.HOST, IKeyword.TOPOLOGY, IKeyword.MEMBERS, "populations");

	/**
	 * A helper class to save agent and restore/recreate agent as a member of a population.
	 */
	private class SavedAgent {

		Map<String, Object> variables;
		Map<String, List<SavedAgent>> innerPopulations;

		SavedAgent(final IScope scope, final IAgent agent) throws GamaRuntimeException {
			saveAttributes(scope, agent);
			if ( agent instanceof IMacroAgent ) {
				saveMicroAgents(scope, (IMacroAgent) agent);
			}
		}

		/**
		 * Saves agent's attributes to a map.
		 * 
		 * @param agent
		 * @throws GamaRuntimeException
		 */
		private void saveAttributes(final IScope scope, final IAgent agent) throws GamaRuntimeException {
			variables = new THashMap<String, Object>(11, 0.9f);
			final ISpecies species = agent.getSpecies();
			for ( final String specVar : species.getVarNames() ) {
				if ( UNSAVABLE_VARIABLES.contains(specVar) ) {
					continue;
				}

				if ( species.getVar(specVar).value(scope, agent) instanceof IPopulation ) {
					continue;
				}

				if ( specVar.equals(IKeyword.SHAPE) ) {
					// variables.put(specVar, geometry.copy());
					// Changed 3/2/12: is it necessary to make the things below ?
					variables.put(specVar,
						new GamaShape(((GamaShape) species.getVar(specVar).value(scope, agent)).getInnerGeometry()));
					continue;
				}
				variables.put(specVar, species.getVar(specVar).value(scope, agent));
			}
		}

		/**
		 * Recursively save micro-agents of an agent.
		 * 
		 * @param agent The agent having micro-agents to be saved.
		 * @throws GamaRuntimeException
		 */
		private void saveMicroAgents(final IScope scope, final IMacroAgent agent) throws GamaRuntimeException {
			innerPopulations = new THashMap<String, List<SavedAgent>>();

			for ( final IPopulation microPop : agent.getMicroPopulations() ) {
				final List<SavedAgent> savedAgents = new GamaList<SavedAgent>();
				final Iterator<IAgent> it = microPop.iterator();
				while (it.hasNext()) {
					savedAgents.add(new SavedAgent(scope, it.next()));
				}

				innerPopulations.put(microPop.getSpecies().getName(), savedAgents);
			}
		}

		/**
		 * @param scope
		 *            Restores the saved agent as a member of the target population.
		 * 
		 * @param targetPopulation The population that the saved agent will be restored to.
		 * @return
		 * @throws GamaRuntimeException
		 */
		IAgent restoreTo(final IScope scope, final IPopulation targetPopulation) throws GamaRuntimeException {
			final List<Map> agentAttrs = new GamaList<Map>();
			agentAttrs.add(variables);
			final List<? extends IAgent> restoredAgents = targetPopulation.createAgents(scope, 1, agentAttrs, true);
			restoreMicroAgents(scope, restoredAgents.get(0));

			return restoredAgents.get(0);
		}

		/**
		 * 
		 * 
		 * @param host
		 * @throws GamaRuntimeException
		 */
		void restoreMicroAgents(final IScope scope, final IAgent host) throws GamaRuntimeException {

			for ( final String microPopName : innerPopulations.keySet() ) {
				final IPopulation microPop = ((IMacroAgent) host).getMicroPopulation(microPopName);

				if ( microPop != null ) {
					final List<SavedAgent> savedMicros = innerPopulations.get(microPopName);
					final List<Map> microAttrs = new GamaList<Map>();
					for ( final SavedAgent sa : savedMicros ) {
						microAttrs.add(sa.variables);
					}

					final List<? extends IAgent> microAgents =
						microPop.createAgents(scope, savedMicros.size(), microAttrs, true);

					for ( int i = 0; i < microAgents.size(); i++ ) {
						savedMicros.get(i).restoreMicroAgents(scope, microAgents.get(i));
					}
				}
			}
		}
	}

	@Override
	public void initializeMicroPopulation(final IScope scope, final String name) {
		final ISpecies microSpec = getModel().getSpecies(name);
		final IPopulation microPop = GamaPopulation.createPopulation(scope, this, microSpec);
		attributes.put(microSpec.getName(), microPop);
		microPop.initializeFor(scope);
	}

	@Override
	public void dispose() {
		if ( dead() ) { return; }
		// if ( getSpecies().getName().equals("flock") ) {
		// GuiUtils.debug("GamlAgent.dispose " + this);
		// }
		try {
			acquireLock();
			for ( final Map.Entry<Object, Object> entry : attributes.entrySet() ) {
				if ( entry.getValue() instanceof IPopulation ) {
					final IPopulation microPop = (IPopulation) entry.getValue();
					// microPop.killMembers();
					microPop.dispose();
				}
			}
			final GamaGraph graph = (GamaGraph) getAttribute("attached_graph");
			if ( graph != null ) {

				final Set edgesToModify = graph.edgesOf(this);
				graph.removeVertex(this);

				for ( final Object obj : edgesToModify ) {
					if ( obj instanceof IAgent ) {
						((IAgent) obj).dispose();
					}
				}
			}
		} finally {
			releaseLock();
		}
		super.dispose();
	}

	@Override
	public String getName() {
		if ( name == null ) { return super.getName(); }
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public/* synchronized */IShape getGeometry() {
		return geometry;
	}

	@Override
	public Envelope3D getEnvelope() {
		// Explicitely redefined here in order to address Issue 709. Having a lock on getGeometry() would prevent the
		// QuadTree from working in a multi-thread environment.
		if ( geometry == null ) { return null; }
		return geometry.getEnvelope();
	}

	@Override
	public/* synchronized */void setGeometry(final IShape newGeometry) {
		// Addition to address Issue 817: if the new geometry is exactly the one possessed by the agent, no need to
		// change anything.
		if ( newGeometry == geometry || newGeometry == null || newGeometry.getInnerGeometry() == null || dead() ) { return; }

		final ITopology topology = population.getTopology();
		final ILocation newGeomLocation = newGeometry.getLocation().copy(getScope());

		// if the old geometry is "shared" with another agent, we create a new one.
		// otherwise, we copy it directly.
		final IAgent other = newGeometry.getAgent();
		final GamaShape newLocalGeom = (GamaShape) (other == null ? newGeometry : newGeometry.copy(getScope()));
		topology.normalizeLocation(newGeomLocation, false);

		if ( !newGeomLocation.equals(newLocalGeom.getLocation()) ) {
			newLocalGeom.setLocation(newGeomLocation);
		}

		newLocalGeom.setAgent(this);
		final IShape previous = geometry;
		geometry = newLocalGeom;

		topology.updateAgent(previous, this);

		// update micro-agents' locations accordingly

		// TODO DOES NOT WORK FOR THE MOMENT
		// for ( final IPopulation pop : getMicroPopulations() ) {
		// pop.hostChangesShape();
		// }
	}

	@Override
	public/* synchronized */void setLocation(final ILocation point) {
		if ( point == null || dead() ) { return; }
		final ILocation newLocation = point.copy(getScope());
		final ITopology topology = population.getTopology();
		if ( topology == null ) { return; }
		topology.normalizeLocation(newLocation, false);

		if ( geometry == null || geometry.getInnerGeometry() == null ) {
			setGeometry(GamaGeometryType.createPoint(newLocation));
		} else {
			final ILocation previousPoint = geometry.getLocation();
			if ( newLocation.equals(previousPoint) ) { return; }
			final IShape previous =
				geometry.isPoint() ? previousPoint : new GamaShape(geometry.getInnerGeometry().getEnvelope());
			// Envelope previousEnvelope = geometry.getEnvelope();
			geometry.setLocation(newLocation);
			// final Integer newHeading = topology.directionInDegreesTo(getScope(), previousPoint, newLocation);
			// if ( newHeading != null && !getTopology().isTorus() ) {
			// setHeading(newHeading);
			// }
			topology.updateAgent(previous, this);

			// update micro-agents' locations accordingly
			// for ( final IPopulation pop : getMicroPopulations() ) {
			// // FIXME DOES NOT WORK FOR THE MOMENT
			// pop.hostChangesShape();
			// }
		}
		final GamaGraph graph = (GamaGraph) getAttribute("attached_graph");
		if ( graph != null ) {
			final Set edgesToModify = graph.edgesOf(this);
			for ( final Object obj : edgesToModify ) {
				if ( obj instanceof IAgent ) {
					final IShape ext1 = (IShape) graph.getEdgeSource(obj);
					final IShape ext2 = (IShape) graph.getEdgeTarget(obj);
					((IAgent) obj).setGeometry(GamaGeometryType.buildLine(ext1.getLocation(), ext2.getLocation()));
				}
			}

		}
	}

	@Override
	public/* synchronized */ILocation getLocation() {
		if ( geometry == null || geometry.getInnerGeometry() == null ) {
			IScope scope = this.getScope();
			final ILocation randomLocation = population.getTopology().getRandomLocation(scope);
			if ( randomLocation == null ) { return null; }
			setGeometry(GamaGeometryType.createPoint(randomLocation));
			return randomLocation;
		}
		return geometry.getLocation();
	}

	@Override
	public void hostChangesShape() {
		setLocation(new GamaPoint(getLocation()));
	}

	@Override
	public boolean isInstanceOf(final ISpecies s, final boolean direct) {
		if ( s.getName().equals(IKeyword.AGENT) ) { return true; }
		return super.isInstanceOf(s, direct);
	}

	@Override
	public IPopulation[] getMicroPopulations() {
		Iterable<IPopulation> it = Iterables.filter(attributes.values(), IPopulation.class);
		IPopulation[] pops = Iterables.toArray(it, IPopulation.class);

		return pops;
	}

	@Override
	public synchronized IPopulation getMicroPopulation(final String microSpeciesName) {
		return (IPopulation) attributes.get(microSpeciesName);
	}

	@Override
	public IPopulation getMicroPopulation(final ISpecies microSpecies) {
		return (IPopulation) attributes.get(microSpecies.getName());
	}

	@Override
	public boolean hasMembers() {
		if ( dead() ) { return false; }
		for ( final IPopulation pop : getMicroPopulations() ) {
			if ( pop.size() > 0 ) { return true; }
		}
		return false;
	}

	@Override
	public IContainer<?, IAgent> getMembers(final IScope scope) {
		if ( dead() ) { return GamaList.EMPTY_LIST; }
		return new MetaPopulation(getMicroPopulations());
	}

	@Override
	public void setMembers(final IList<IAgent> newMembers) {
		// Directly changing "members" not supported
	}

	@Override
	public void setAgents(final IList<IAgent> agents) {
		// "agents" is read-only attribute
	}

	@Override
	public IList<IAgent> getAgents(final IScope scope) {
		if ( !hasMembers() ) { return GamaList.EMPTY_LIST; }

		final IContainer<?, IAgent> members = getMembers(scope);
		final IList<IAgent> agents = new GamaList<IAgent>();
		agents.addAll(members.listValue(scope, Types.NO_TYPE));
		for ( final IAgent m : members.iterable(scope) ) {
			if ( m != null && m instanceof IMacroAgent ) {
				agents.addAll(((IMacroAgent) m).getAgents(scope));
			}
		}

		return agents;
	}

	@Override
	public IPopulation getPopulationFor(final ISpecies species) {
		// hqnghi adjust to get population for species which come from main as well micro models
		ModelDescription micro = species.getDescription().getModelDescription();
		ModelDescription main = (ModelDescription) this.getModel().getDescription();
		IPopulation microPopulation = null;
		if ( main.getMicroModel(micro.getAlias()) == null ) {
			microPopulation = this.getMicroPopulation(species);
			if ( microPopulation == null && getHost() != null ) {
				microPopulation = getHost().getPopulationFor(species);
			}
		} else {
			microPopulation = this.getExternMicroPopulationFor(species.getName());
		}
		// end-hqnghi
		return microPopulation;
	}

	@Override
	public IPopulation getPopulationFor(final String speciesName) {
		final IPopulation microPopulation = this.getMicroPopulation(speciesName);
		if ( microPopulation == null && getHost() != null ) { return getHost().getPopulationFor(speciesName); }
		return microPopulation;
	}

	/**
	 * Verifies if this agent can capture other agent as newSpecies.
	 * 
	 * @return true if the following conditions are correct:
	 *         1. newSpecies is one micro-species of this agent's species;
	 *         2. newSpecies is a sub-species of this agent's species or other species is a
	 *         sub-species of this agent's species;
	 *         3. the "other" agent is not macro-agent of this agent;
	 *         4. the "other" agent is not a micro-agent of this agent.
	 */
	@Override
	public boolean canCapture(final IAgent other, final ISpecies newSpecies) {
		if ( other == null || other.dead() || newSpecies == null || !this.getSpecies().containMicroSpecies(newSpecies) ) { return false; }
		if ( this.getMacroAgents().contains(other) ) { return false; }
		if ( other.getHost().equals(this) ) { return false; }
		return true;
	}

	@Override
	public IScope obtainNewScope() {
		if ( dead ) { return null; }
		return new Scope();
	}

	@Override
	public void releaseScope(final IScope scope) {
		if ( scope != null ) {
			scope.clear();
		}
	}

	protected class Scope extends AbstractScope {

		volatile boolean interrupted = false;

		public Scope() {
			super(GamlAgent.this);
		}

		@Override
		protected boolean _root_interrupted() {
			return interrupted || dead;
		}

		@Override
		public void setInterrupted(final boolean interrupted) {
			this.interrupted = true;
			// GuiUtils.debug("GamlAgent.Scope.setInterrupted : " + this);
			// if ( !GamlAgent.this.dead ) {
			// GamlAgent.this.dispose();
			// }
		}

		@Override
		public IScope copy() {
			return new Scope();
		}

		/**
		 * Method getRandom()
		 * @see msi.gama.runtime.IScope#getRandom()
		 */
		@Override
		public RandomUtils getRandom() {
			IExperimentAgent a = this.getExperiment();
			return a == null ? null : a.getRandomGenerator();
		}

	}

	/**
	 * Method getPoints()
	 * @see msi.gama.metamodel.shape.IShape#getPoints()
	 */
	@Override
	public IList<? extends ILocation> getPoints() {
		if ( geometry == null ) { return GamaList.EMPTY_LIST; }
		return geometry.getPoints();
	}

	@Override
	public void setDepth(final double depth) {
		if ( geometry == null ) { return; }
		geometry.setDepth(depth);

	}

}
