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
package msi.gama.outputs;

import java.util.*;
import msi.gama.interfaces.*;
import msi.gama.kernel.*;
import msi.gama.kernel.exceptions.GamaRuntimeException;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.util.Cast;

/**
 * The Class AbstractOutput.
 * 
 * @author drogoul
 */
@inside(symbols = ISymbol.OUTPUT)
public abstract class AbstractOutput extends Symbol implements IOutput {

	protected OutputManager outputManager;
	private IScope ownStack;
	boolean paused = false;
	private boolean isPermanent = false;
	boolean open = false;
	private Long nextRefreshTime;
	private int refreshRate;

	public AbstractOutput(final IDescription desc) {
		super(desc);
		name = getLiteral(ISymbol.NAME);
		if ( name != null ) {
			name.replace(':', '_');
			name.replace('/', '_');
			name.replace('\\', '_');
			if ( name.length() == 0 ) {
				name = "output";
			}
		}
		setRefreshRate(1);
	}

	@Override
	public IScope getStack() {
		return getOwnScope();
	}

	@Override
	public void prepare(final ISimulation sim) throws GamaRuntimeException {
		setOwnScope(GAMA.obtainNewScope());
		outputManager = GAMA.getExperiment().getOutputManager();
		IExpression refresh = getFacet(ISymbol.REFRESH_EVERY);
		if ( refresh != null ) {
			setRefreshRate(Cast.asInt(getOwnScope(), refresh.value(getOwnScope())));
		}
		setNextTime(sim.getScheduler().getCycle());
	}

	@Override
	public void close() {
		pause();
		setOpen(false);
	}

	@Override
	public boolean isClosed() {
		return !isOpen();
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public boolean isPaused() {
		return paused;
	}

	@Override
	public void open() {
		setOpen(true);
	}

	@Override
	public void pause() {
		setPaused(true);
	}

	@Override
	public void resume() {
		setPaused(false);
		reschedule();
	}

	@Override
	public int getRefreshRate() {
		return refreshRate;
	}

	@Override
	public void setRefreshRate(final int refresh) {
		refreshRate = refresh;
	}

	@Override
	public abstract void compute(IScope scope, Long cycle) throws GamaRuntimeException;

	@Override
	public abstract void update() throws GamaRuntimeException;

	void setOpen(final boolean open) {
		this.open = open;
	}

	public void setPaused(final boolean suspended) {
		paused = suspended;
	}

	@Override
	public void schedule() throws GamaRuntimeException {
		setNextTime(getOwnScope().getSimulationScope().getScheduler().getCycle());
		outputManager.scheduleOutput(this);
	}

	private void reschedule() {
		setNextTime(getOwnScope().getSimulationScope().getScheduler().getCycle() + getRefreshRate());
	}

	@Override
	public void setNextTime(final Long l) {
		nextRefreshTime = l;
	}

	@Override
	public long getNextTime() {
		return nextRefreshTime;
	}

	@Override
	public boolean isPermanent() {
		return isPermanent;
	}

	public void setPermanent(final boolean batchOutput) {
		isPermanent = batchOutput;
	}

	@Override
	public String toGaml() {
		// To redefine for outputs that need to be saved
		return null;
	}

	@Override
	public boolean isUserCreated() {
		// To redefine for outputs that need to be maintained
		return false;
	}

	@Override
	public void setChildren(final List<? extends ISymbol> commands) {

	}

	public List<? extends ISymbol> getChildren() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getId() {
		return getName(); // by default
	}

	protected void setOwnScope(final IScope ownStack) {
		this.ownStack = ownStack;
	}

	public IScope getOwnScope() {
		return ownStack;
	}

}
