package msi.gama.metamodel.population;

import java.util.*;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.util.GuiUtils;
import msi.gama.kernel.experiment.ExperimentAgent;
import msi.gama.kernel.simulation.SimulationAgent;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.*;
import msi.gama.metamodel.topology.AbstractTopology.RootTopology;
import msi.gama.metamodel.topology.continuous.AmorphousTopology;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.IList;
import msi.gaml.operators.Cast;
import msi.gaml.species.ISpecies;

public class SimulationPopulation extends GamaPopulation {

	public SimulationPopulation(final ISpecies species) {
		super(null, species);
	}

	@Override
	public void killMembers() throws GamaRuntimeException {
		// Simulations should be killed in a more precautious way...
		// for ( final IAgent a : toArray() ) {
		// if ( a.getScope().interrupted() ) {
		// a.dispose();
		// }
		// }
	}

	@Override
	public IList<? extends IAgent> createAgents(final IScope scope, final int number, final List<Map> initialValues,
		final boolean isRestored) throws GamaRuntimeException {
		GuiUtils.waitStatus("Initializing simulation");
		final SimulationAgent world = new SimulationAgent(this);
		world.setIndex(currentAgentIndex++);
		add(world);
		getHost().setSimulation(world);
		if ( scope.interrupted() ) { return null; }
		GuiUtils.waitStatus("Instantiating agents");
		createVariablesFor(world.getScope(), this, initialValues);
		world.schedule();
		GuiUtils.informStatus("Simulation Ready");
		return this;
	}

	@Override
	public ExperimentAgent getHost() {
		return (ExperimentAgent) super.getHost();
	}

	@Override
	public IAgent getAgent(final ILocation value) {
		return get(null, 0);
	}

	public void setHost(final ExperimentAgent agent) {
		host = agent;
	}

	public void setTopology(final IScope scope, final IShape gisShape, final IShape shape) {
		final boolean torus = Cast.asBool(scope, species.getFacet(IKeyword.TORUS));
		topology = new RootTopology(scope, gisShape, shape, torus);
	}

	@Override
	public void computeTopology(final IScope scope) throws GamaRuntimeException {
		// Temporary topology set before the world gets a shape
		topology = new AmorphousTopology();
	}

	// @Override
	// public IList<IAgent> computeAgentsToSchedule(final IScope scope) {
	// final int frequency = scheduleFrequency == null ? 1 : Cast.asInt(scope, scheduleFrequency.value(scope));
	// final int step = scope.getClock().getCycle();
	// if ( frequency == 0 || step % frequency != 0 ) { return GamaList.EMPTY_LIST; }
	//
	// }

}