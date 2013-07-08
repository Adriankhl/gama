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
package msi.gama.gui.swt.controls;

import msi.gama.common.interfaces.IGui;
import msi.gama.common.util.GuiUtils;
import msi.gama.gui.swt.SwtGui;
import msi.gama.kernel.simulation.*;
import msi.gama.runtime.GAMA;
import msi.gaml.operators.Strings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.jfree.util.StringUtils;

public class StatusControlContribution extends WorkbenchWindowControlContribution implements IPopupProvider {

	private Composite compo;
	Label label;
	private Popup popup;
	int status;

	public StatusControlContribution() {}

	public StatusControlContribution(final String id) {
		super(id);
	}

	//
	@Override
	protected int computeWidth(final Control control) {
		return control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
	}

	@Override
	protected Control createControl(final Composite parent) {
		compo = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 2;
		compo.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, true);
		data.widthHint = 300;
		label = new Label(compo, SWT.CENTER);
		label.setLayoutData(data);
		label.setBackground(SwtGui.COLOR_OK);
		label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		label.setText("No simulation running");
		label.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(final MouseEvent e) {
				GAMA.getClock().toggleDisplay();
			}
		});
		popup = new Popup(this, label);
		SwtGui.setStatusControl(this);
		return compo;
	}

	public boolean isDisposed() {
		return label.isDisposed();
	}

	/**
	 * @param message
	 */
	public void setText(final String message, final int code) {
		status = code;
		label.setBackground(getPopupBackground());
		label.setText(message);
		if ( popup.isVisible() ) {
			popup.display();
		}
	}

	/**
	 * @see msi.gama.gui.swt.controls.IPopupProvider#getPopupText()
	 */
	@Override
	public String getPopupText() {
		if ( !GuiUtils.isSimulationPerspective() ) { return null; }
		if ( status == IGui.ERROR || status == IGui.WAIT ) { return label.getText(); }
		StringBuilder sb = new StringBuilder(300);
		String nl = StringUtils.getLineSeparator();
		SimulationAgent simulation = GAMA.getSimulation();
		if ( simulation == null ) { return "No simulation running"; }
		SimulationClock clock = simulation.getClock();
		sb.append("Cycles elapsed: ").append("\t").append(clock.getCycle()).append(nl);
		sb.append("Simulated time: ").append("\t").append(Strings.asDate(clock.getTime(), null)).append(nl);
		sb.append("Cycle duration: ").append("\t").append("\t").append(clock.getDuration()).append("ms").append(nl);
		sb.append("Average duration: ").append("\t").append((int) clock.getAverageDuration()).append("ms").append(nl);
		sb.append("Total duration: ").append("\t").append("\t").append(clock.getTotalDuration()).append("ms");
		return sb.toString();
	}

	/**
	 * @see msi.gama.gui.swt.controls.IPopupProvider#getPopupBackground()
	 */
	@Override
	public Color getPopupBackground() {
		return status == IGui.ERROR ? SwtGui.COLOR_ERROR : status == IGui.WAIT ? SwtGui.COLOR_WARNING : SwtGui.COLOR_OK;
	}

	@Override
	public Shell getControllingShell() {
		return label.getShell();
	}

	@Override
	public Point getAbsoluteOrigin() {
		return label.toDisplay(new Point(label.getLocation().x, label.getSize().y));
	}

}
