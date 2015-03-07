/*********************************************************************************************
 * 
 * 
 * 'UserControlView.java', in plugin 'msi.gama.application', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.gui.views;

import java.util.List;
import msi.gama.common.interfaces.EditorListener;
import msi.gama.common.util.GuiUtils;
import msi.gama.gui.parameters.EditorFactory;
import msi.gama.gui.swt.*;
import msi.gama.gui.swt.controls.*;
import msi.gama.outputs.IDisplayOutput;
import msi.gama.runtime.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.architecture.user.UserInputStatement;
import msi.gaml.statements.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class UserControlView extends GamaViewPart {

	public static final int CONTINUE = 100;
	public static final int INSPECT = 101;

	public static String ID = "msi.gama.views.userControlView";

	private IScope scope;
	private List<IStatement> userCommands;
	private String title;
	private Composite body;
	ToolItem inspectItem, continueItem;

	public void initFor(final IScope scope, final List<IStatement> userCommands, final String title) {
		this.scope = scope;
		this.userCommands = userCommands;
		this.title = title;
		if ( body != null && !body.isDisposed() ) {
			body.dispose();
			body = null;
		}

		ownCreatePartControl(parent);
		parent.layout();
	}

	@Override
	public Integer[] getToolbarActionsId() {
		return new Integer[] { INSPECT, CONTINUE };
	}

	private void deactivate(final Composite parent) {
		for ( Control c : parent.getChildren() ) {
			if ( c instanceof Composite ) {
				deactivate((Composite) c);
			} else {
				c.setEnabled(false);
			}
		}
	}

	@Override
	public void ownCreatePartControl(final Composite parent) {
		parent.setBackground(IGamaColors.WHITE.color());
		if ( scope == null ) { return; }
		inspectItem.setEnabled(true);
		continueItem.setEnabled(true);
		setPartName(title);
		parent.setLayout(new FillLayout());
		parent.setBackground(IGamaColors.WHITE.color());
		toolbar.status((Image) null, "User control, agent " + scope.getAgentScope().getName() + ", cycle " +
			scope.getClock().getCycle(), IGamaColors.NEUTRAL, SWT.LEFT);
		body = new Composite(parent, SWT.None);
		GridLayout layout = new GridLayout(3, false);
		body.setLayout(layout);
		body.setBackground(IGamaColors.WHITE.color());
		for ( final IStatement c : userCommands ) {
			if ( c instanceof UserCommandStatement ) {
				Composite commandComposite = new Composite(body, SWT.BORDER);
				GridData data = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
				commandComposite.setLayoutData(data);
				layout = new GridLayout(3, false);
				commandComposite.setLayout(layout);
				commandComposite.setBackground(IGamaColors.WHITE.color());
				List<UserInputStatement> inputs = ((UserCommandStatement) c).getInputs();
				int nbLines = inputs.size() > 1 ? inputs.size() : 1;
				int nbCol = inputs.size() > 0 ? 1 : 3;
				FlatButton b =
					FlatButton.button(commandComposite, IGamaColors.BLUE, c.getName(), GamaIcons.create("small.run")
						.image());
				b.setEnabled(((UserCommandStatement) c).isEnabled(scope));
				GridData gd = new GridData(SWT.LEFT, SWT.CENTER, true, true, nbCol, nbLines);
				b.setLayoutData(gd);
				b.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(final SelectionEvent e) {
						c.executeOn(scope);
						GAMA.getExperiment().getSimulationOutputs().forceUpdateOutputs();
					}

				});
				for ( final UserInputStatement i : inputs ) {
					scope.addVarWithValue(i.getTempVarName(), i.value(scope));
					EditorFactory.create(commandComposite, i, new EditorListener() {

						@Override
						public void valueModified(final Object newValue) throws GamaRuntimeException {
							i.setValue(scope, newValue);
							i.executeOn(scope);
						}
					}, false);
				}

			}
		}

	}

	@Override
	public void update(final IDisplayOutput output) {
		initFor(scope, userCommands, title);
	}

	@Override
	public void createToolItem(final int code, final GamaToolbarSimple tb) {
		switch (code) {
			case CONTINUE:
				continueItem =
					tb.button(IGamaIcons.PANEL_CONTINUE.getCode(), "Continue", "Continue", new SelectionListener() {

						@Override
						public void widgetSelected(final SelectionEvent e) {
							GAMA.controller.getScheduler().setUserHold(false);
							deactivate(parent);
							GuiUtils.hideView(ID);
						}

						@Override
						public void widgetDefaultSelected(final SelectionEvent e) {
							widgetSelected(e);
						}

					});
				continueItem.setEnabled(false);
				break;
			case INSPECT:
				inspectItem =
					tb.button(IGamaIcons.PANEL_INSPECT.getCode(), "Inspect", "Inspect", new SelectionAdapter() {

						@Override
						public void widgetSelected(final SelectionEvent e) {
							GuiUtils.setSelectedAgent(scope.getAgentScope());
						}

					});
				inspectItem.setEnabled(false);
		}
	}

	/**
	 * Method setToolbar()
	 * @see msi.gama.gui.views.IToolbarDecoratedView#setToolbar(msi.gama.gui.swt.controls.GamaToolbar2)
	 */
	@Override
	public void setToolbar(final GamaToolbar2 toolbar) {}

	/**
	 * Method createToolItem()
	 * @see msi.gama.gui.views.IToolbarDecoratedView#createToolItem(int, msi.gama.gui.swt.controls.GamaToolbar2)
	 */
	@Override
	public void createToolItem(final int code, final GamaToolbar2 tb) {

		switch (code) {
			case CONTINUE:
				continueItem =
					tb.button(IGamaIcons.PANEL_CONTINUE.getCode(), "Continue", "Continue", new SelectionListener() {

						@Override
						public void widgetSelected(final SelectionEvent e) {
							GAMA.controller.getScheduler().setUserHold(false);
							deactivate(parent);
							GuiUtils.hideView(ID);
						}

						@Override
						public void widgetDefaultSelected(final SelectionEvent e) {
							widgetSelected(e);
						}

					}, SWT.RIGHT);
				continueItem.setEnabled(false);
				break;
			case INSPECT:
				inspectItem =
					tb.button(IGamaIcons.PANEL_INSPECT.getCode(), "Inspect", "Inspect", new SelectionAdapter() {

						@Override
						public void widgetSelected(final SelectionEvent e) {
							GuiUtils.setSelectedAgent(scope.getAgentScope());
						}

					}, SWT.RIGHT);
				inspectItem.setEnabled(false);
		}

	}

}
