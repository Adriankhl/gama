/*********************************************************************************************
 * 
 *
 * 'SimulationAgent.java', in plugin 'msi.gama.core', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.kernel.simulation;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.util.GuiUtils;
import msi.gama.kernel.experiment.AgentScheduler;
import msi.gama.kernel.experiment.ExperimentPlan;
import msi.gama.metamodel.agent.*;
import msi.gama.metamodel.population.*;
import msi.gama.metamodel.shape.*;
import msi.gama.metamodel.topology.projection.ProjectionFactory;
import msi.gama.outputs.IOutputManager;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.args;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.species;
import msi.gama.precompiler.GamlAnnotations.var;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.operators.Spatial.Transformations;
import msi.gaml.species.ISpecies;
import msi.gaml.types.IType;

/**
 * Defines an instance of a model (a simulation). Serves as the support for model species (whose metaclass is
 * GamlModelSpecies)
 * Written by drogoul Modified on 1 d�c. 2010, May 2013
 * 
 * @todo Description
 * 
 */
@species(name = IKeyword.MODEL)
@vars({
	@var(name = IKeyword.STEP, type = IType.FLOAT, doc = @doc(value = "Represents the value of the interval, in model time, between two simulation cycles", comment = "If not set, its value is equal to 1.0 and, since the default time unit is the second, to 1 second")),
	@var(name = SimulationAgent.TIME, type = IType.FLOAT, doc = @doc(value = "Represents the total time passed, in model time, since the beginning of the simulation", comment = "Equal to cycle * step if the user does not arbitrarily initialize it.")),
	@var(name = SimulationAgent.CYCLE, type = IType.INT, doc = @doc("Returns the current cycle of the simulation")),
	@var(name = SimulationAgent.DURATION, type = IType.STRING, doc = @doc("Returns a string containing the duration, in milliseconds, of the previous simulation cycle")),
	@var(name = SimulationAgent.TOTAL_DURATION, type = IType.STRING, doc = @doc("Returns a string containing the total duration, in milliseconds, of the simulation since it has been launched ")),
	@var(name = SimulationAgent.AVERAGE_DURATION, type = IType.STRING, doc = @doc("Returns a string containing the average duration, in milliseconds, of a simulation cycle.")),
	@var(name = SimulationAgent.MACHINE_TIME, type = IType.FLOAT, doc = @doc(value = "Returns the current system time in milliseconds", comment = "The return value is a float number")), })
public class SimulationAgent extends GamlAgent {

	public static final String DURATION = "duration";
	public static final String MACHINE_TIME = "machine_time";
	public static final String TOTAL_DURATION = "total_duration";
	public static final String AVERAGE_DURATION = "average_duration";
	public static final String CYCLE = "cycle";
	public static final String TIME = "time";

	final SimulationClock clock;
	AgentScheduler scheduler;
	IScope scope;
	IOutputManager outputs;
	ProjectionFactory projectionFactory;

	public SimulationAgent(final IPopulation pop) throws GamaRuntimeException {
		super(pop);
		clock = new SimulationClock();
		scope = obtainNewScope();
		scheduler = new AgentScheduler(scope, pop);
		projectionFactory = new ProjectionFactory();
	}

	@Override
	public void schedule() {
		super.schedule();
		// Necessary to put it here as the output manager is initialized *after* the agent, meaning it will remove
		// everything in the errors/console view that is being written by the init of the simulation
		GuiUtils.prepareForSimulation(this);
//		GAMA.controller.getScheduler().schedule(scheduler, scope);
//		if ( outputs != null ) {
//			final IScope simulationScope = obtainNewScope();
//			if ( simulationScope != null ) {
//				GAMA.controller.getScheduler().schedule(outputs, simulationScope);
//			} else {
//				// TODO What does it do here ? Should be elsewhere (but where ?)
//				GuiUtils.cleanAfterSimulation();
//				// GuiUtils.hideView(GuiUtils.PARAMETER_VIEW_ID);
//				// GuiUtils.hideMonitorView();
//			}
//		}
		
		//hqnghi: 2 case: multi controllers and mono controller
		if(!((ExperimentPlan)getExperiment().getSpecies()).getControllerName().equals("")){
			GAMA.getController(((ExperimentPlan)getExperiment().getSpecies()).getControllerName())
			.getScheduler()
			.schedule(scheduler,
			scope);
			if ( outputs != null ) {
				final IScope simulationScope = obtainNewScope();
				if ( simulationScope != null ) {
					GAMA.getController(((ExperimentPlan)getExperiment().getSpecies()).getControllerName()).getScheduler()
							.schedule(
							outputs, simulationScope);
				} else {
					// TODO What does it do here ? Should be elsewhere (but where ?)
					GuiUtils.cleanAfterSimulation();
					// GuiUtils.hideView(GuiUtils.PARAMETER_VIEW_ID);
					// GuiUtils.hideMonitorView();
				}
			}
		}else{			
			GAMA.controller.getScheduler().schedule(scheduler, scope);
			if ( outputs != null ) {
				final IScope simulationScope = obtainNewScope();
				if ( simulationScope != null ) {
					GAMA.controller.getScheduler().schedule(outputs, simulationScope);
				} else {
					// TODO What does it do here ? Should be elsewhere (but where ?)
					GuiUtils.cleanAfterSimulation();
					// GuiUtils.hideView(GuiUtils.PARAMETER_VIEW_ID);
					// GuiUtils.hideMonitorView();
				}
			}
		}
		//end-hqnghi
	}

	@Override
	// TODO A redefinition of this method in GAML will lose all information regarding the clock and the advance of time,
	// which will have to be done manually (i.e. cycle <- cycle + 1; time <- time + step;)
	public Object _step_(final IScope scope) {
		clock.beginCycle();
		// A simulation always runs in its own scope
		try {
			super._step_(this.scope);
		} finally {
			clock.step(this.scope);
		}
		return this;
	}

	@Override
	public Object _init_(final IScope scope) {
		// A simulation always runs in its own scope
		return super._init_(this.scope);
	}

	/**
	 * Scope related utilities
	 * 
	 */

	@Override
	public IScope getScope() {
		return scope;
	}

	public ProjectionFactory getProjectionFactory() {
		return projectionFactory;
	}

	@Override
	public AgentScheduler getScheduler() {
		return scheduler;
	}

	@Override
	public SimulationClock getClock() {
		return clock;
	}

	@Override
	public void dispose() {
		if ( dead ) { return; }
		super.dispose();
		// We dispose of any scheduler still running
		if ( scheduler != null ) {
			scheduler.dispose();
			scheduler = null;
		}
		projectionFactory = new ProjectionFactory();

	}

	@Override
	public synchronized void setLocation(final ILocation newGlobalLoc) {}

	@Override
	public synchronized ILocation getLocation() {
		if ( geometry == null ) { return new GamaPoint(0, 0); }
		return super.getLocation();
	}

	@Override
	public synchronized void setGeometry(final IShape geom) {
		if ( geometry != null ) {
			GAMA.reportError(
				GamaRuntimeException
					.warning(
						"Changing the shape of the world after its creation is a bad idea that can have unexpected consequences",
						scope), false);
		}

		// We systematically translate the geometry to {0,0}
		final Envelope3D env = geom.getEnvelope();
		final GamaPoint p = new GamaPoint(-env.getMinX(), -env.getMinY(), -env.getMinZ());
		geometry = Transformations.translated_by(getScope(), geom, p);
		getPopulation().setTopology(getScope(), geometry);
	}

	@Override
	public SimulationPopulation getPopulation() {
		return (SimulationPopulation) population;
	}

	@Override
	public IPopulation getPopulationFor(final String speciesName) throws GamaRuntimeException {
		IPopulation pop = super.getPopulationFor(speciesName);
		if ( pop != null ) { return pop; }
		final ISpecies microSpec = getSpecies().getMicroSpecies(speciesName);
		if ( microSpec == null ) { return null; }
		pop = GamaPopulation.createPopulation(getScope(), this, microSpec);
		attributes.put(microSpec, pop);
		pop.initializeFor(getScope());
		return pop;
	}

	@getter(CYCLE)
	public Integer getCycle(final IScope scope, final IAgent agent) {
		final SimulationClock clock = getClock();
		if ( clock != null ) { return clock.getCycle(); }
		return 0;
	}

	@getter(IKeyword.STEP)
	public double getTimeStep(final IScope scope, final IAgent agent) {
		final SimulationClock clock = getClock();
		if ( clock != null ) { return clock.getStep(); }
		return 1d;
	}

	@setter(IKeyword.STEP)
	public void setTimeStep(final IScope scope, final IAgent agent, final double t) throws GamaRuntimeException {
		final SimulationClock clock = getClock();
		if ( clock != null ) {
			clock.setStep(t);
		}
	}

	@getter(TIME)
	public double getTime(final IScope scope, final IAgent agent) {
		final SimulationClock clock = getClock();
		if ( clock != null ) { return clock.getTime(); }
		return 0d;
	}

	@setter(TIME)
	public void setTime(final IScope scope, final IAgent agent, final double t) throws GamaRuntimeException {
		final SimulationClock clock = getClock();
		if ( clock != null ) {
			clock.setTime(t);
		}
	}

	@getter(DURATION)
	public String getDuration() {
		return Long.toString(getClock().getDuration());
	}

	@getter(TOTAL_DURATION)
	public String getTotalDuration() {
		return Long.toString(getClock().getTotalDuration());
	}

	@getter(AVERAGE_DURATION)
	public String getAverageDuration() {
		return Double.toString(getClock().getAverageDuration());
	}

	@getter(MACHINE_TIME)
	public Double getMachineTime() {
		return (double) System.currentTimeMillis();
	}

	@setter(MACHINE_TIME)
	public void setMachineTime(final Double t) throws GamaRuntimeException {
		// NOTHING
	}

	@action(name = "pause", doc = @doc("Allows to pause the current simulation **ACTUALLY EXPERIMENT FOR THE MOMENT**. It can be set to continue with the manual intervention of the user."))
	@args(names = {})
	public Object pause(final IScope scope) {
		String ctrlName=((ExperimentPlan)scope.getExperiment().getSpecies()).getControllerName();
		if(!ctrlName.equals("")){
			GAMA.getController(ctrlName).offer(FrontEndController._PAUSE);
		}else{
			GAMA.controller.offer(FrontEndController._PAUSE);
		}
		return null;
	}

	@action(name = "halt", doc = @doc(deprecated = "It is preferable to use 'die' instead to kill a simulation, or 'pause' to stop it temporarily", value = "Allows to stop the current simulation so that cannot be continued after. All the behaviors and updates are stopped. "))
	@args(names = {})
	public Object halt(final IScope scope) {
		getExperiment().closeSimulation();
		return null;
	}

	public void setOutputs(final IOutputManager iOutputManager) {
		outputs = iOutputManager;
	}

}