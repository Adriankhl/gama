/*********************************************************************************************
 * 
 * 
 * 'GamaNavigator.java', in plugin 'msi.gama.application', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.gui.navigator;

import msi.gama.common.*;
import msi.gama.gui.swt.IGamaColors;
import msi.gama.gui.swt.controls.GamaToolbar2;
import msi.gama.gui.views.IToolbarDecoratedView;
import msi.gama.gui.views.actions.GamaToolbarFactory;
import org.eclipse.core.resources.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.internal.navigator.CommonNavigatorActionGroup;
import org.eclipse.ui.internal.navigator.actions.LinkEditorAction;
import org.eclipse.ui.navigator.*;

public class GamaNavigator extends CommonNavigator implements IToolbarDecoratedView, ISelectionChangedListener {

	final public static int LINK = 50;
	final public static int COLLAPSE = 51;
	final public static int NEW = 52;
	final public static int IMPORT = 54;
	final public static int PROJECT = 53;

	String OPEN_BROWSER_COMMAND_ID = "msi.gama.application.commands.OpenBrowser";

	IResourceChangeListener listener;
	IAction link, collapse;
	ToolItem linkItem, collapseItem, newFileItem, newProjectItem, importItem;
	protected Composite parent;
	// protected GamaToolbar leftToolbar, rightToolbar;
	protected GamaToolbar2 toolbar;
	private IDescriptionProvider commonDescriptionProvider;

	@Override
	protected CommonNavigatorManager createCommonManager() {
		CommonNavigatorManager manager = new CommonNavigatorManager(this, memento);
		commonDescriptionProvider = /* getNavigatorContentService().createCommonDescriptionProvider(); */
		new IDescriptionProvider() {

			@Override
			public String getDescription(final Object anElement) {
				if ( anElement instanceof IStructuredSelection ) {
					IStructuredSelection selection = (IStructuredSelection) anElement;
					if ( selection == null || selection.isEmpty() ) { return ""; }
					String message = null;
					if ( selection.size() > 1 ) {
						message = "Multiple elements";
					} else if ( selection.getFirstElement() instanceof VirtualContent ) {
						message = ((VirtualContent) selection.getFirstElement()).getName();
					} else if ( selection.getFirstElement() instanceof IResource ) {
						message = ((IResource) selection.getFirstElement()).getName();
					}
					return message;
				}
				return "";
			}
		};
		getCommonViewer().addPostSelectionChangedListener(this);
		return manager;
	}

	@Override
	public void createPartControl(final Composite compo) {
		this.parent = GamaToolbarFactory.createToolbars(this, compo);

		super.createPartControl(parent);
		// getCommonViewer().getControl().setLayoutData(GamaToolbarFactory.getLayoutDataForChild());
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		for ( IContributionItem item : toolbar.getItems() ) {
			if ( item instanceof ActionContributionItem ) {
				ActionContributionItem aci = (ActionContributionItem) item;
				IAction action = aci.getAction();
				if ( action instanceof LinkEditorAction ) {
					link = action;
					toolbar.remove(aci);
				} else if ( action instanceof org.eclipse.ui.internal.navigator.actions.CollapseAllAction ) {
					collapse = action;
					toolbar.remove(aci);
				}

			}
		}
		// toolbar.removeAll();
		linkItem.setSelection(link.isChecked());
	}

	@Override
	protected void initListeners(final TreeViewer viewer) {
		super.initListeners(viewer);
		listener = new IResourceChangeListener() {

			@Override
			public void resourceChanged(final IResourceChangeEvent event) {

				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						TreePath[] treePaths = viewer.getExpandedTreePaths();
						viewer.refresh();
						viewer.setExpandedTreePaths(treePaths);
					}
				});
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);

		// viewer.getControl().addMouseTrackListener(new MouseTrackListener() {
		//
		// TreeItem previous;
		//
		// @Override
		// public void mouseHover(final MouseEvent e) {
		// Point p = new Point(e.x, e.y);
		// Tree c = (Tree) viewer.getControl();
		// TreeItem i = c.getItem(p);
		// if ( i == null ) {
		// if ( previous != null ) {
		// previous.setBackground(IGamaColors.WHITE.color());
		// }
		// } else {
		// i.setBackground(IGamaColors.OK.color());
		// if ( previous != null && i != previous ) {
		// previous.setBackground(IGamaColors.WHITE.color());
		// }
		// previous = i;
		// }
		//
		// }
		//
		// @Override
		// public void mouseExit(final MouseEvent e) {}
		//
		// @Override
		// public void mouseEnter(final MouseEvent e) {}
		// });
	}

	@Override
	public void dispose() {
		super.dispose();
		if ( listener != null ) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
			listener = null;
		}
	}

	@Override
	protected Object getInitialInput() {
		return new NavigatorRoot();
	}

	@Override
	protected void handleDoubleClick(final DoubleClickEvent anEvent) {
		IStructuredSelection selection = (IStructuredSelection) anEvent.getSelection();
		Object element = selection.getFirstElement();
		if ( element instanceof IFile ) {
			TreeViewer viewer = getCommonViewer();
			if ( viewer.isExpandable(element) ) {
				viewer.setExpandedState(element, !viewer.getExpandedState(element));
			}
		}
		if ( element instanceof VirtualContent && ((VirtualContent) element).handleDoubleClick() ) {
			return;
		} else {
			super.handleDoubleClick(anEvent);
		}
	}

	@Override
	protected ActionGroup createCommonActionGroup() {
		return new CommonNavigatorActionGroup(this, getCommonViewer(), getLinkHelperService()) {

			@Override
			protected void fillViewMenu(final IMenuManager menu) {
				menu.removeAll();
			}

		};
	}

	/**
	 * Method setToolbar()
	 * @see msi.gama.gui.views.IToolbarDecoratedView#setToolbar(msi.gama.gui.swt.controls.GamaToolbar2)
	 */
	@Override
	public void setToolbar(final GamaToolbar2 toolbar) {
		this.toolbar = toolbar;
	}

	/**
	 * Method createToolItem()
	 * @see msi.gama.gui.views.IToolbarDecoratedView#createToolItem(int, msi.gama.gui.swt.controls.GamaToolbar2)
	 */
	@Override
	public void createToolItem(final int code, final GamaToolbar2 tb) {

		switch (code) {
			case LINK:
				linkItem =
					tb.check("navigator/navigator.link2", "", "Stay in sync with the editor", new SelectionAdapter() {

						@Override
						public void widgetSelected(final SelectionEvent e) {
							link.run();
						}

					}, SWT.RIGHT);
				break;
			case COLLAPSE:
				collapseItem =
					tb.button("navigator/navigator.collapse2", "", "Collapse all items", new SelectionAdapter() {

						@Override
						public void widgetSelected(final SelectionEvent e) {
							collapse.run();
						}

					}, SWT.RIGHT);
				break;
			case NEW:
				newFileItem = tb.menu("navigator/navigator.new2", "", "New...", new SelectionAdapter() {

					@Override
					public void widgetSelected(final SelectionEvent trigger) {
						GamaNavigatorNewMenu menu =
							new GamaNavigatorNewMenu((IStructuredSelection) getCommonViewer().getSelection());
						final ToolItem target = (ToolItem) trigger.widget;
						final ToolBar toolBar = target.getParent();
						menu.open(toolBar, trigger);

					}

				}, SWT.RIGHT);
				break;
			case IMPORT:
				importItem = tb.menu("navigator/navigator.import2", "", "Import...", new SelectionAdapter() {

					@Override
					public void widgetSelected(final SelectionEvent trigger) {
						GamaNavigatorImportMenu menu =
							new GamaNavigatorImportMenu((IStructuredSelection) getCommonViewer().getSelection());
						final ToolItem target = (ToolItem) trigger.widget;
						final ToolBar toolBar = target.getParent();
						menu.open(toolBar, trigger);

					}

				}, SWT.RIGHT);
		}

	}

	/**
	 * Method getToolbarActionsId()
	 * @see msi.gama.gui.views.IToolbarDecoratedView#getToolbarActionsId()
	 */
	@Override
	public Integer[] getToolbarActionsId() {
		return new Integer[] { IMPORT, NEW, SEP, COLLAPSE, LINK };
	}

	/**
	 * Method selectionChanged()
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		if ( selection == null || selection.isEmpty() ) {
			toolbar.wipe(SWT.LEFT);
			return;
		}
		String message = commonDescriptionProvider.getDescription(selection);
		Image img = ((ILabelProvider) getCommonViewer().getLabelProvider()).getImage(selection.getFirstElement());
		toolbar.status(img, message, IGamaColors.BLUE, SWT.LEFT);
	}

	public Menu getSubMenu(final String text) {
		Menu m = getCommonViewer().getTree().getMenu();
		for ( MenuItem mi : m.getItems() ) {
			if ( text.equals(mi.getText()) ) { return mi.getMenu(); }
		}
		return m;
	}

}
