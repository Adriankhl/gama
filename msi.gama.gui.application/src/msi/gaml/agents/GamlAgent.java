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
package msi.gaml.agents;

import java.util.*;
import msi.gama.agents.AbstractAgent;
import msi.gama.environment.ITopology;
import msi.gama.interfaces.*;
import msi.gama.kernel.GAMA;
import msi.gama.kernel.exceptions.GamaRuntimeException;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.args;
import msi.gama.precompiler.GamlAnnotations.species;
import msi.gama.util.*;
import msi.gaml.kernel.IGamlPopulation;
import com.vividsolutions.jts.geom.*;

/**
 * The Class Agent. Represents agents that can be manipulated in GAML. They are provided with
 * everything their species defines .
 */
@species("default")
public class GamlAgent extends AbstractAgent implements IGamlAgent {

	/**
	 * Instantiates a new GAMA object using a Species. Constructor to use when the species is known
	 * in advance and able to act by itself to add the agent, etc. In this constructor, it is
	 * supposed that the species does the setting of skills, variables,behaviors, etc. by itself.
	 * 
	 * @param s the Species used to prototype the agent.
	 * @throws GamlException
	 */
	public GamlAgent(final ISimulation sim, final IPopulation s) throws GamaRuntimeException {
		super(sim, s);
		if ( s == null ) { // built-in species/agents like Conversion, Message, ClusterBuilder,
							// MulticriteriaAnalyzer, ...
			population = findPopulation(sim);
			getPopulation().createVariablesFor(sim.getGlobalScope(), this);
			population.add(this, null);
		}
	}

	protected IGamlPopulation findPopulation(final ISimulation sim) {
		IGamlPopulation spec;
		final species sa = this.getClass().getAnnotation(species.class);
		String speciesName = null;
		if ( sa != null ) {
			speciesName = sa.value();
		}
		spec = (IGamlPopulation) sim.getWorld().getPopulationFor(speciesName);
		return spec;
	}

	@Override
	public IGamlPopulation getPopulation() {
		return (IGamlPopulation) population;
	}

	@Override
	public Object getDirectVarValue(final String n) throws GamaRuntimeException {
		IVariable var = population.getVar(this, n);
		if ( var != null ) { return var.value(this); }
		IAgent host = this.getHost();
		if ( host != null ) {
			IVariable varOfHost = host.getPopulation().getVar(host, n);
			if ( varOfHost != null ) { return varOfHost.value(host); }
		}

		return null;
	}

	@Override
	public void setDirectVarValue(final IScope scope, final String s, final Object v)
		throws GamaRuntimeException {
		population.getVar(this, s).setVal(scope, this, v);
	}

	@Override
	public void updateAttributes(final IScope scope) throws GamaRuntimeException {
		getPopulation().updateVariablesFor(scope, this);
	}

	@Override
	public boolean isGridAgent() {
		return population.isGrid();
	}

	@Override
	public void init(final IScope scope) throws GamaRuntimeException {
		// callback from the scheduler once the agent has been scheduled.
		getPopulation().init(scope);
	}

	@Override
	// TODO move up to AbstractAgent
	public void step(final IScope scope) throws GamaRuntimeException {
		if ( dead() || !simulation.isAlive() ) { return; }

		scope.push(this);
		try {
			updateAttributes(scope);
			getPopulation().step(scope);
		} catch (GamaRuntimeException g) {
			g.addContext(this);
			GAMA.reportError(g);
		} finally {
			scope.pop(this);
		}
	}

	@Override
	public void computeAgentsToSchedule(final IScope scope, final GamaList list)
		throws GamaRuntimeException {
		List<IPopulation> pops = this.getMicroPopulations();

		try {
			scope.push(this);

			for ( IPopulation pop : pops ) {
				pop.computeAgentsToSchedule(scope, list);
			}
		} finally {
			scope.pop(this);
		}
	}

	@action("debug")
	@args({ "message" })
	public final Object primDebug(final IScope scope) throws GamaRuntimeException {
		final String m = Cast.asString(scope.getArg("message"));
		debug(m + "\nsender: " + toMap());
		return m;
	}

	@action("write")
	@args({ "message" })
	public final Object primWrite(final IScope scope) throws GamaRuntimeException {
		String s = Cast.asString(scope.getArg("message"));
		super.write(s);
		return s;
	}

	@action("error")
	@args({ "message" })
	public final Object primError(final IScope scope) throws GamaRuntimeException {
		String error = Cast.asString(scope.getArg("message"));
		super.error(error);
		return error;
	}

	@action("tell")
	@args({ "message" })
	public final Object primTell(final IScope scope) throws GamaRuntimeException {
		final String s = getName() + " says : " + Cast.asString(scope.getArg("message"));
		super.tell(s);
		return s;
	}

	@action("die")
	public Object primDie(final IScope scope) throws GamaRuntimeException {
		scope.setStatus(ExecutionStatus.interrupt);
		die();
		return null;
	}

	@Override
	public boolean contains(final IAgent component) {
		if ( component == null ) { return false; }
		return this.equals(((IGamlAgent) component).getHost());
	}

	@Override
	public IAgent copy() {
		return this;
		// agents are immutable
	}

	@Override
	public boolean isPoint() {
		return geometry != null && geometry.isPoint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see msi.gama.interfaces.IAgent#getTopology()
	 */
	@Override
	public ITopology getTopology() {
		return population.getTopology();
	}

	@Override
	public List<IAgent> captureMicroAgents(final ISpecies microSpecies,
		final List<IAgent> microAgents) throws GamaRuntimeException {
		if ( microAgents == null || microAgents.isEmpty() || microSpecies == null ||
			!this.getSpecies().getMicroSpecies().contains(microSpecies) ) { return GamaList.EMPTY_LIST; }

		List<IAgent> candidates = new GamaList<IAgent>();
		for ( IAgent a : microAgents ) {
			if ( this.canCapture(a, microSpecies) ) {
				candidates.add(a);
			}
		}

		List<IAgent> capturedAgents = new GamaList<IAgent>();
		IPopulation microSpeciesPopulation = this.getPopulationFor(microSpecies);
		for ( IAgent micro : candidates ) {
			SavedAgent savedMicro = new SavedAgent(micro);
			micro.die();
			capturedAgents.add(savedMicro.restoreTo(microSpeciesPopulation));
		}

		return capturedAgents;
	}

	@Override
	public List<IAgent> releaseMicroAgents(final List<IAgent> microAgents)
		throws GamaRuntimeException {
		IPopulation originalSpeciesPopulation;
		List<IAgent> releasedAgents = new GamaList<IAgent>();

		for ( IAgent micro : microAgents ) {
			SavedAgent savedMicro = new SavedAgent(micro);
			originalSpeciesPopulation =
				micro.getPopulationFor(micro.getSpecies().getParentSpecies());
			micro.die();
			releasedAgents.add(savedMicro.restoreTo(originalSpeciesPopulation));
		}

		return releasedAgents;
	}

	/**
	 * A helper class to save agent and restore/recreate agent as a member of a population.
	 */
	private class SavedAgent {

		Map<String, Object> attributes;
		Map<String, List<SavedAgent>> microPopulations;

		SavedAgent(final IAgent agent) throws GamaRuntimeException {
			saveAttributes(agent);
			saveMicroAgents(agent);
		}

		/**
		 * Saves agent's attributes to a map.
		 * 
		 * @param agent
		 * @throws GamaRuntimeException
		 */
		private void saveAttributes(final IAgent agent) throws GamaRuntimeException {
			attributes = new HashMap<String, Object>();

			ISpecies species = agent.getSpecies();
			for ( String specVar : species.getVarNames() ) {
				if ( specVar.equals(IGamlAgent.SHAPE) ) {
					attributes.put(specVar, new GamaGeometry(((GamaGeometry) species
						.getVar(specVar).value(agent)).getInnerGeometry()));
					continue;
				}

				attributes.put(specVar, species.getVar(specVar).value(agent));
			}
		}

		/**
		 * Recursively save micro-agents of an agent.
		 * 
		 * @param agent The agent having micro-agents to be saved.
		 * @throws GamaRuntimeException
		 */
		private void saveMicroAgents(final IAgent agent) throws GamaRuntimeException {
			microPopulations = new HashMap<String, List<SavedAgent>>();

			for ( IPopulation microPop : agent.getMicroPopulations() ) {
				List<SavedAgent> savedAgents = new GamaList<SavedAgent>();

				for ( IAgent micro : microPop.getAgentsList() ) {
					savedAgents.add(new SavedAgent(micro));
				}

				microPopulations.put(microPop.getSpecies().getName(), savedAgents);
			}
		}

		/**
		 * Restores the saved agent as a member of the target population.
		 * 
		 * @param targetPopulation The population that the saved agent will be restored to.
		 * @return
		 * @throws GamaRuntimeException
		 */
		IAgent restoreTo(final IPopulation targetPopulation) throws GamaRuntimeException {
			List<Map<String, Object>> agentAttrs = new GamaList<Map<String, Object>>();
			agentAttrs.add(attributes);
			List<? extends IAgent> restoredAgents =
				targetPopulation.createAgents(targetPopulation.getHost().getSimulation()
					.getScheduler().getExecutionScope(), 1, agentAttrs, true);
			restoreMicroAgents(restoredAgents.get(0));

			return restoredAgents.get(0);
		}

		/**
		 * 
		 * 
		 * @param host
		 * @throws GamaRuntimeException
		 */
		void restoreMicroAgents(final IAgent host) throws GamaRuntimeException {
			IScope scope = host.getSimulation().getScheduler().getExecutionScope();

			for ( String microPopName : microPopulations.keySet() ) {
				IPopulation microPop = host.getMicroPopulation(microPopName);

				if ( microPop != null ) {
					List<SavedAgent> savedMicros = microPopulations.get(microPopName);
					List<Map<String, Object>> microAttrs = new GamaList<Map<String, Object>>();
					for ( SavedAgent sa : savedMicros ) {
						microAttrs.add(sa.attributes);
					}

					List<? extends IAgent> microAgents =
						microPop.createAgents(scope, savedMicros.size(), microAttrs, true);

					for ( int i = 0; i < microAgents.size(); i++ ) {
						savedMicros.get(i).restoreMicroAgents(microAgents.get(i));
					}
				}
			}
		}
	}

	/**
	 * @see msi.gama.interfaces.IGeometry#getInnerGeometry()
	 */
	@Override
	public Geometry getInnerGeometry() {
		GamaGeometry g = getGeometry();
		return g == null ? null : g.getInnerGeometry();
	}

	/**
	 * @see msi.gama.interfaces.IGeometry#getEnvelope()
	 */
	@Override
	public Envelope getEnvelope() {
		GamaGeometry g = getGeometry();
		return g == null ? null : g.getEnvelope();
	}

	/**
	 * @see msi.gaml.agents.IGamlAgent#setTopology(msi.gama.environment.ITopology)
	 */
	@Override
	public void setTopology(final ITopology t) {
		// NOTHING TO DO
	}

	/**
	 * @see msi.gama.interfaces.IGeometry#covers(msi.gama.interfaces.IGeometry)
	 */
	@Override
	public boolean covers(final IGeometry g) {
		GamaGeometry gg = getGeometry();
		return gg == null ? false : gg.covers(g);
	}

	/**
	 * @see msi.gama.interfaces.IGeometry#euclidianDistanceTo(msi.gama.interfaces.IGeometry)
	 */
	@Override
	public double euclidianDistanceTo(final IGeometry g) {
		GamaGeometry gg = getGeometry();
		return gg == null ? 0d : gg.euclidianDistanceTo(g);
	}

	/**
	 * @see msi.gama.interfaces.IGeometry#intersects(msi.gama.interfaces.IGeometry)
	 */
	@Override
	public boolean intersects(final IGeometry g) {
		GamaGeometry gg = getGeometry();
		return gg == null ? false : gg.intersects(g);
	}

	/**
	 * @see msi.gama.interfaces.IGeometry#getAgent()
	 */
	@Override
	public IAgent getAgent() {
		return this;
	}

	/**
	 * @see msi.gama.interfaces.IGeometry#setAgent(msi.gama.interfaces.IAgent)
	 */
	@Override
	public void setAgent(final IAgent agent) {}
}
