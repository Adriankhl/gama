package msi.gama.headless.runtime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import msi.gama.headless.common.Globals;
import msi.gama.headless.core.GamaHeadlessException;
import msi.gama.headless.core.HeadlessSimulationLoader;
import msi.gama.headless.job.ExperimentJob;
import msi.gama.kernel.experiment.ExperimentPlan;
import msi.gama.kernel.experiment.IExperimentPlan;
import msi.gama.kernel.model.IModel;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.HeadlessListener;
import msi.gaml.compilation.GamlCompilationError;
import msi.gaml.descriptions.IDescription;
import ummisco.gama.dev.utils.DEBUG;

public class LocalSimulationRuntime extends Observable implements SimulationRuntime, RuntimeContext {

	static {
		DEBUG.ON();
	}
	private final Map<String, ExperimentJob> simulations;
	private final ArrayList<FakeApplication> queue;
	private final ArrayList<FakeApplication> started;
	private final HashMap<String, ArrayList<IModel>> loadedModels;
	private final HashMap<String, ArrayList<IModel>> availableLoadedModels;
	private final int allocatedProcessor;
	private boolean isTraceKept;

	public LocalSimulationRuntime() {
		this(UNDEFINED_QUEUE_SIZE);
	}

	public LocalSimulationRuntime(final int numberOfCoresAsked) {
		simulations = new HashMap<>();
		queue = new ArrayList<>();
		started = new ArrayList<>();
		loadedModels = new HashMap<>();
		availableLoadedModels = new HashMap<>();
		this.allocatedProcessor = getAvailableCores(numberOfCoresAsked);
	}

	private static int getAvailableCores(final int asked) {
		final int max = Runtime.getRuntime().availableProcessors();
		final int cpus = Math.max(1, Math.min(max, asked));
		System.out.println("Number of cpus used:" + cpus + " (available: " + max + ")");
		DEBUG.LOG("Number of cpus used:" + cpus + " (available: " + max + ")");
		return cpus;
	}

	public void listenMe(final Observer v) {
		this.addObserver(v);
	}

	@Override
	public boolean isTraceKept() {
		return this.isTraceKept;
	}

	@Override
	public void keepTrace(final boolean t) {
		this.isTraceKept = t;
	}

	@Override
	public void pushSimulation(final ExperimentJob s) {
		simulations.put(s.getExperimentID(), s);
		final FakeApplication f = new FakeApplication(s, this);
		if (started.size() < allocatedProcessor) {
			this.startSimulation(f);
		} else {
			queue.add(f);
		}
	}

	private void startSimulation(final FakeApplication s) {
		started.add(s);
		s.start();
		this.notifyListener();
	}

	public void closeSimulation(final FakeApplication s) {
		started.remove(s);
		if (queue.size() > 0) {
			final FakeApplication p = queue.get(0);
			queue.remove(p);
			this.startSimulation(p);
		}
		if (!this.isTraceKept) {
			simulations.remove(s.getExperimentJob().getExperimentID());
		}
		this.notifyListener();
	}

	private void notifyListener() {
		this.setChanged();
		this.notifyObservers();
	}

	@Override
	public SimulationState getSimulationState(final String id) {
		final ExperimentJob tmp = simulations.get(id);
		if (tmp == null) { return SimulationState.UNDEFINED; }
		/**
		 * TODO AD BUG: ATTENTION !! queue contient des FakeApplication, pas des ExperimentJob
		 */
		if (started.contains(tmp)) { return SimulationState.STARTED; }
		/**
		 * TODO AD BUG: ATTENTION !! queue contient des FakeApplication, pas des ExperimentJob
		 */
		if (queue.contains(tmp)) { return SimulationState.ENQUEUED; }
		return SimulationState.ACHIEVED;
	}

	@Override
	public boolean isPerformingSimulation() {
		return started.size() > 0 || queue.size() > 0;
	}

	public synchronized void releaseModel(final String key, final IModel mdl) {
		// DEBUG.LOG("release simulation");
		// String key = mdl.getFilePath();
		// availableLoadedModels.get(key).add(mdl);
		// DEBUG.LOG("remove " + mdl.getFilePath());
		// lockUnLock(null,key, mdl);
		// DEBUG.LOG("model released ") ;
	}

	// private synchronized IModel lockUnLock(final File fl, final String key, final IModel mdl)
	// throws IOException, GamaHeadlessException {
	// IModel mm = null;
	// if (mdl != null) {
	// availableLoadedModels.get(key).add(mdl);
	// mm = mdl;
	// }
	// if (fl != null) {
	// mm = lockModel(fl);
	// }
	// return mm;
	// }

	public synchronized IModel lockModel(final File fl) throws IOException, GamaHeadlessException {
		IModel mdl;
		final String key = fl.getAbsolutePath();
		ArrayList<IModel> arr = availableLoadedModels.get(fl.getAbsolutePath());
		// DEBUG.OUT(fl.getAbsolutePath());
		if (arr == null) {
			arr = new ArrayList<>();
			availableLoadedModels.put(key, arr);
			loadedModels.put(key, new ArrayList<IModel>());
		}
		if (arr.size() == 0) {
			mdl = HeadlessSimulationLoader.loadModel(fl);
			loadedModels.get(key).add(mdl);
		} else {
			mdl = arr.get(0);
			arr.remove(0);
		}
		return mdl;
	}

	@Override
	public synchronized IModel loadModel(final File fl) throws IOException, GamaHeadlessException {
		// return lockUnLock( fl,null, null) ; //lockModel(fl); //
		final List<GamlCompilationError> errors = new ArrayList<>();
		return HeadlessSimulationLoader.loadModel(fl, errors); // lockModel(fl); //mdl.c;
	}

	@Override
	public IExperimentPlan buildExperimentPlan(final String expName, final IModel mdl) {
		final IDescription des = mdl.getExperiment(expName).getDescription();
		final IExperimentPlan expp = new ExperimentPlan(des);
		expp.setModel(mdl);
		return expp;
	}

	class FakeApplication extends Thread {// implements Runnable {

		private ExperimentJob si = null;
		private LocalSimulationRuntime runtime = null;

		ExperimentJob getExperimentJob() {
			return si;
		}

		public FakeApplication(final ExperimentJob sim, final LocalSimulationRuntime rn) {
			si = sim;
			this.runtime = rn;
		}

		@Override
		public void run() {
			boolean noErrorFound = true;
			try {
				final BufferedWriter file = new BufferedWriter(new FileWriter(Globals.OUTPUT_PATH + "/"
						+ Globals.CONSOLE_OUTPUT_FILENAME + "-" + si.getExperimentID() + ".txt"));
				((HeadlessListener) GAMA.getHeadlessGui()).registerJob(file);
			} catch (final IOException e1) {
				e1.printStackTrace();
				noErrorFound = false;
			}
			try {
				if (noErrorFound) {
					si.loadAndBuild(this.runtime);
				}

			} catch (final InstantiationException e) {
				noErrorFound = false;
				System.out.println(e.toString());
				DEBUG.ERR(e);
			} catch (final IllegalAccessException e) {
				noErrorFound = false;
				System.out.println(e.toString());
				DEBUG.ERR(e);
			} catch (final ClassNotFoundException e) {
				noErrorFound = false;
				System.out.println(e.toString());
				DEBUG.ERR(e);
			} catch (final IOException e) {
				noErrorFound = false;
				System.out.println(e.toString());
				DEBUG.ERR(e);
			} catch (final GamaHeadlessException e) {
				noErrorFound = false;
				// e.printStackTrace();
				System.out.println(e.toString());
				DEBUG.ERR(e);
			}
			if (noErrorFound) {
				si.playAndDispose();
			}
			((HeadlessListener) GAMA.getHeadlessGui()).leaveJob();
			runtime.closeSimulation(this);
			runtime.releaseModel(si.getSourcePath(), si.getSimulation().getModel());

		}

	}

	@Override
	public HashMap<String, Double> getSimulationState() {

		final HashMap<String, Double> res = new HashMap<>();
		for (final ExperimentJob exp : simulations.values()) {
			res.put(exp.getExperimentID(), new Double(exp.getStep() / exp.getFinalStep()));
		}
		return res;
	}

}
