/*********************************************************************************************
 * 
 * 
 * 'ErrorView.java', in plugin 'msi.gama.application', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.gui.views;

import java.util.*;
import java.util.List;
import msi.gama.common.GamaPreferences;
import msi.gama.common.interfaces.*;
import msi.gama.common.util.GuiUtils;
import msi.gama.gui.parameters.EditorFactory;
import msi.gama.gui.swt.*;
import msi.gama.gui.swt.controls.GamaToolbar2;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class ErrorView extends ExpandableItemsView<GamaRuntimeException> implements IToolbarDecoratedView.Pausable {

	public static String ID = GuiUtils.ERROR_VIEW_ID;
	int numberOfDisplayedErrors = GamaPreferences.CORE_ERRORS_NUMBER.getValue();
	boolean mostRecentFirst = GamaPreferences.CORE_RECENT.getValue();
	private final ArrayList<GamaRuntimeException> exceptions = new ArrayList();

	// static public int numberOfDisplayedErrors = 10;
	// static public boolean mostRecentFirst = true;
	// static public boolean showErrors = true;

	// ParameterExpandItem parametersItem;

	@Override
	protected boolean areItemsClosable() {
		return true;
	}

	@Override
	public boolean addItem(final GamaRuntimeException e) {
		createItem(parent, e, false);
		return true;
	}

	public synchronized void addNewError(final GamaRuntimeException ex) {
		// if ( ex.isReported() ) { return; }

		for ( final GamaRuntimeException e : exceptions ) {
			// if ( e == ex ) { return; }
			if ( e.equivalentTo(ex) && e != ex ) {
				e.addAgents(ex.getAgentsNames());
				if ( GamaPreferences.CORE_SHOW_ERRORS.getValue() ) {
					reset();
					displayItems();
				}
				return;
			}
		}
		if ( !exceptions.contains(ex) ) {
			WorkbenchPlugin.log("GamaRuntimeException " + ex.getMessage(), ex);
			exceptions.add(ex);
		}
		if ( GamaPreferences.CORE_REVEAL_AND_STOP.getValue() && !ex.isReported() ) {
			ex.setReported();
			gotoEditor(ex);
		}

		if ( GamaPreferences.CORE_SHOW_ERRORS.getValue() ) {
			reset();
			displayItems();
		}
	}

	/**
	 * @see msi.gama.gui.views.GamaViewPart#getToolbarActionsId()
	 */
	@Override
	public Integer[] getToolbarActionsId() {
		// TODO Need to be defined and usable (not the case now)
		return new Integer[] {};
	}

	@Override
	public void ownCreatePartControl(final Composite view) {
		// super.ownCreatePartControl(view);
		final Composite intermediate = new Composite(view, SWT.VERTICAL);
		final GridLayout parentLayout = new GridLayout(1, false);
		parentLayout.marginWidth = 0;
		parentLayout.marginHeight = 0;
		parentLayout.verticalSpacing = 0;
		intermediate.setLayout(parentLayout);
		final Composite parameters = new Group(intermediate, SWT.None);
		final GridLayout layout = new GridLayout(2, false);

		parameters.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		layout.verticalSpacing = 0;
		parameters.setLayout(layout);

		// final IntEditor ed =
		EditorFactory.create(parameters, "Display last ", null, numberOfDisplayedErrors, 0, 100, 1, false,
			new EditorListener<Integer>() {

				@Override
				public void valueModified(final Integer newValue) {
					if ( newValue == numberOfDisplayedErrors ) { return; }
					numberOfDisplayedErrors = newValue;
					reset();
					displayItems();
				}

			});

		EditorFactory.create(parameters, "Most recent first", mostRecentFirst, new EditorListener<Boolean>() {

			@Override
			public void valueModified(final Boolean newValue) {

				mostRecentFirst = newValue;
				reset();
				displayItems();
			}

		});

		parameters.pack();
		parent = intermediate;
	}

	private void gotoEditor(final GamaRuntimeException exception) {

		final EObject o = exception.getEditorContext();
		if ( o != null && GamaPreferences.CORE_REVEAL_AND_STOP.getValue() ) {
			GuiUtils.asyncRun(new Runnable() {

				@Override
				public void run() {
					GuiUtils.editModel(o);
				}
			});
		}

	}

	@Override
	protected Composite createItemContentsFor(final GamaRuntimeException exception) {
		final ScrolledComposite compo = new ScrolledComposite(getViewer(), SWT.NONE);
		final GridLayout layout = new GridLayout(1, false);
		final GridData firstColData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layout.verticalSpacing = 5;
		compo.setLayout(layout);
		final Table t = new Table(compo, SWT.H_SCROLL | SWT.V_SCROLL);

		t.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				gotoEditor(exception);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {}
		});
		t.setLayoutData(firstColData);
		final java.util.List<String> strings = exception.getContextAsList();
		t.setLinesVisible(true);
		t.setForeground(exception.isWarning() ? GamaColors.get(SwtGui.WARNING_TEXT_COLOR.getValue()).color()
			: GamaColors.get(SwtGui.ERROR_TEXT_COLOR.getValue()).color());
		final TableColumn c = new TableColumn(t, SWT.NONE);
		c.setResizable(true);
		final TableColumn column2 = new TableColumn(t, SWT.NONE);
		for ( int i = 0; i < strings.size(); i++ ) {
			final TableItem item = new TableItem(t, SWT.NONE);
			item.setText(new String[] { String.valueOf(i), strings.get(i) });
		}
		c.pack();
		column2.pack();
		t.setSize(t.computeSize(1000, 200));
		compo.setContent(t);
		compo.setSize(compo.computeSize(SWT.DEFAULT, 200));
		return compo;
	}

	@Override
	public void removeItem(final GamaRuntimeException obj) {
		exceptions.remove(obj);
	}

	@Override
	public void pauseItem(final GamaRuntimeException obj) {}

	@Override
	public void resumeItem(final GamaRuntimeException obj) {}

	@Override
	public String getItemDisplayName(final GamaRuntimeException obj, final String previousName) {
		final StringBuilder sb = new StringBuilder(300);
		final String a = obj.getAgentSummary();
		if ( a != null ) {
			sb.append(a).append(" at ");
		}
		sb.append("cycle ").append(obj.getCycle()).append(ItemList.SEPARATION_CODE)
			.append(obj.isWarning() ? ItemList.WARNING_CODE : ItemList.ERROR_CODE).append(obj.getMessage());
		return sb.toString();
	}

	@Override
	public void focusItem(final GamaRuntimeException data) {
		// gotoEditor(data);
	}

	@Override
	public List<GamaRuntimeException> getItems() {
		final List<GamaRuntimeException> errors = new ArrayList();
		final int size = exceptions.size();
		if ( size == 0 ) { return errors; }
		final int end = size;
		int begin = end - numberOfDisplayedErrors;
		begin = begin < 0 ? 0 : begin;
		errors.addAll(exceptions.subList(begin, end));
		if ( mostRecentFirst ) {
			Collections.reverse(errors);
		}
		return errors;
	}

	@Override
	public void updateItemValues() {
		this.getViewer().updateItemNames();
	}

	public void clearErrors() {
		this.reset();
		exceptions.clear();
		displayItems();
	}

	/**
	 * Method createToolItem()
	 * @see msi.gama.gui.views.IToolbarDecoratedView#createToolItem(int, msi.gama.gui.swt.controls.GamaToolbar2)
	 */
	@Override
	public void createToolItem(final int code, final GamaToolbar2 tb) {}

}
