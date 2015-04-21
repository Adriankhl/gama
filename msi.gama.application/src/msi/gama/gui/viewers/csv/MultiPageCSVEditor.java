/*
 * Copyright 2011 csvedit
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package msi.gama.gui.viewers.csv;

import msi.gama.gui.navigator.commands.RefreshHandler;
import msi.gama.gui.swt.controls.GamaToolbar2;
import msi.gama.gui.viewers.csv.model.*;
import msi.gama.gui.viewers.csv.text.*;
import msi.gama.gui.views.IToolbarDecoratedView;
import msi.gama.gui.views.actions.GamaToolbarFactory;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.*;

/**
 * 
 * @author fhenri
 * 
 */
public class MultiPageCSVEditor extends MultiPageEditorPart implements IResourceChangeListener, IToolbarDecoratedView, IToolbarDecoratedView.Sizable {

	private boolean isPageModified;
	GamaToolbar2 toolbar;
	private final static int ADD_ROW = -10, REMOVE_ROW = -11, DUPLICATE_ROW = -12, ADD_COLUMN = -13,
		REMOVE_COLUMN = -14, SHOW_COLUMN = -15, SAVE_AS = -16, WITH_HEADER = -17;
	/** index of the source page */
	public static final int indexSRC = 1;
	/** index of the table page */
	public static final int indexTBL = 0;

	/** The text editor used in page 0. */
	protected TextEditor editor;

	/** The table viewer used in page 1. */
	protected TableViewer tableViewer;

	private CSVTableSorter tableSorter;

	// private Menu tableHeaderMenu;

	private AbstractCSVFile model;

	private final ICsvFileModelListener csvFileListener = new ICsvFileModelListener() {

		@Override
		public void entryChanged(final CSVRow row, final int rowIndex) {
			tableModified();
		}
	};

	// private class RowNumberLabelProvider extends CellLabelProvider {
	//
	// // private TableViewer viewer;
	//
	// // @Override
	// // protected void initialize(final ColumnViewer viewer, final ViewerColumn column) {
	// // super.initialize(viewer, column);
	// // this.viewer = null;
	// // if ( viewer instanceof TableViewer ) {
	// // this.viewer = (TableViewer) viewer;
	// // }
	// // }
	//
	// @Override
	// public void update(final ViewerCell cell) {
	// // if ( viewer != null ) {
	// int index = Arrays.asList(tableViewer.getTable().getItems()).indexOf(cell.getItem());
	// cell.setText("" + (index + 1));
	// // }
	// }
	// }

	/**
	 * Creates a multi-page editor example.
	 */
	public MultiPageCSVEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		// model = createCSVFile();
	}

	/**
	 * Method getToolbarActionsId()
	 * @see msi.gama.gui.views.IToolbarDecoratedView#getToolbarActionsId()
	 */
	@Override
	public Integer[] getToolbarActionsId() {
		return new Integer[] { WITH_HEADER, SEP, ADD_ROW, REMOVE_ROW, SEP, ADD_COLUMN, REMOVE_COLUMN, SHOW_COLUMN, SEP,
			SAVE_AS };
	}

	@Override
	public Control getSizableFontControl() {
		if ( tableViewer == null ) { return null; }
		return tableViewer.getTable();
	}

	/**
	 * Creates the pages of the multi-page editor.
	 * 
	 * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
	 */
	@Override
	protected void createPages() {
		try {
			model = new DefaultCSVFile(getFileFor(getEditorInput()));
			createTablePage();
			createSourcePage();
			updateTitle();
			populateTablePage();
			setActivePage(0);
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}

	private IFile getFileFor(final IEditorInput input) {
		if ( input instanceof IFileEditorInput ) {
			return ((IFileEditorInput) input).getFile();
		} else if ( input instanceof IStorageEditorInput ) {
			try {
				IStorage storage = ((IStorageEditorInput) input).getStorage();
				if ( storage instanceof IFile ) { return (IFile) storage; }
			} catch (CoreException ignore) {
				// intentionally blank
			}
		}
		return null;
	}

	/**
	 * Creates page 0 of the multi-page editor, which contains a text editor.
	 */
	private void createSourcePage() {
		try {
			editor = new CSVTextEditor(model.getCustomDelimiter());
			addPage(editor, getEditorInput());
			setPageText(indexSRC, "Text");
		} catch (PartInitException e) {
			// ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
		}
	}

	/**
     *
     */
	private void createTablePage() {
		Composite parent = getContainer();
		Composite intermediate = new Composite(parent, SWT.NONE);
		Composite composite = GamaToolbarFactory.createToolbars(this, intermediate);
		tableViewer =
			new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		// tableViewer.getControl().setLayoutData(GamaToolbarFactory.getLayoutDataForChild());
		tableViewer.setUseHashlookup(true);
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		// set the sorter for the table
		tableSorter = new CSVTableSorter();
		tableViewer.setSorter(tableSorter);
		// set a table filter
		final CSVTableFilter tableFilter = new CSVTableFilter();
		tableViewer.addFilter(tableFilter);

		// add the filtering and coloring when searching specific elements.
		final Text searchText = new Text(toolbar.getToolbar(SWT.LEFT), SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);

		toolbar.control(searchText, 150, SWT.LEFT);
		searchText.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(final KeyEvent ke) {
				tableFilter.setSearchText(searchText.getText(), model.getSensitiveSearch());
				String filterText = searchText.getText();
				for ( int i = 0; i < tableViewer.getColumnProperties().length; i++ ) {
					CellLabelProvider labelProvider = tableViewer.getLabelProvider(i);
					if ( labelProvider != null ) {
						((CSVLabelProvider) labelProvider).setSearchText(filterText);
					}
				}
				tableViewer.refresh();
			}
		});

		addPage(intermediate);
		setPageText(indexTBL, "Table");
	}

	/**
	 * Set Name of the file to the tab
	 */
	private void updateTitle() {
		IEditorInput input = getEditorInput();
		setPartName(input.getName());
		setTitleToolTip(input.getToolTipText());
	}

	/**
	 * @throws Exception
	 */
	private void populateTablePage() throws Exception {

		tableViewer.setContentProvider(new CSVContentProvider());

		// make the selection available
		getSite().setSelectionProvider(tableViewer);

		tableViewer.getTable().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				updateTableFromTextEditor();
			}
		});
	}

	/**
     *
     */
	public void tableModified() {
		tableViewer.refresh();
		boolean wasPageModified = isPageModified;
		isPageModified = true;
		if ( !wasPageModified ) {
			firePropertyChange(IEditorPart.PROP_DIRTY);
			editor.validateEditorInputState(); // will invoke: FileModificationValidator.validateEdit() (expected by some repository providers)
		}
	}

	/**
     *
     */
	private void updateTableFromTextEditor() {
		// PropertyFile propertyFile = (PropertyFile) treeViewer.getInput();
		model.removeModelListener(csvFileListener);

		model.setInput(editor.getDocumentProvider().getDocument(editor.getEditorInput()).get());

		// tableHeaderMenu = new Menu(tableViewer.getTable());
		TableColumn[] columns = tableViewer.getTable().getColumns();
		if ( columns.length > 0 ) { // if table header columns already created
			// update column header text
			for ( int i = 0; i < model.getHeader().size(); i++ ) {
				if ( i < columns.length ) {
					columns[i].setText(model.getHeader().get(i));
					final int index = i;
					addMenuItemToColumn(columns[i], index);
				}
			}
		} else {
			// create columns
			for ( int i = 0; i < model.getHeader().size(); i++ ) {
				final TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.LEFT);
				final int index = i;
				column.getColumn().setText(model.getHeader().get(i));
				column.getColumn().setWidth(100);
				column.getColumn().setResizable(true);
				column.getColumn().setMoveable(true);
				column.setLabelProvider(new CSVLabelProvider());
				addMenuItemToColumn(column.getColumn(), index);
			}
		}

		// if ( model.isFirstLineHeader() ) {
		// new MenuItem(tableHeaderMenu, SWT.SEPARATOR);
		//
		// }

		tableViewer.setInput(model);
		model.addModelListener(csvFileListener);

		// tableViewer.getTable().addListener(SWT.MenuDetect, new Listener() {
		//
		// @Override
		// public void handleEvent(final Event event) {
		// tableViewer.getTable().setMenu(tableHeaderMenu);
		// }
		// });

		defineCellEditing();
	}

	/**
     *
     */
	private void defineCellEditing() {
		String[] columnProperties = new String[model.getColumnCount()];
		CellEditor[] cellEditors = new CellEditor[model.getColumnCount()];

		for ( int i = 0; i < model.getColumnCount(); i++ ) {
			columnProperties[i] = Integer.toString(i);
			cellEditors[i] = new TextCellEditor(tableViewer.getTable());
		}

		tableViewer.setColumnProperties(columnProperties);

		// XXX can be replaced by tableViewer.setEditingSupport()
		tableViewer.setCellEditors(cellEditors);
		tableViewer.setCellModifier(new CSVEditorCellModifier());

	}

	/**
	 * Find a column in the Table by its name
	 * 
	 * @param columnName
	 * @return the index of the Column indicated by its name
	 */
	private int findColumnForName(final String columnName) {
		int index = -1;
		TableColumn[] tableColumns = tableViewer.getTable().getColumns();
		for ( int i = 0; i < tableColumns.length; i++ ) {
			TableColumn column = tableColumns[i];
			if ( columnName.equalsIgnoreCase(column.getText()) ) { return i; }
		}
		return index;
	}

	/**
	 * @param column
	 * @param index
	 */
	private void addMenuItemToColumn(final TableColumn column, final int index) {
		// create menu item
		// final MenuItem itemName = new MenuItem(tableHeaderMenu, SWT.CHECK, index);
		// itemName.setText(column.getText());
		// itemName.setSelection(column.getResizable());
		// itemName.addListener(SWT.Selection, new Listener() {
		//
		// @Override
		// public void handleEvent(final Event event) {
		// if ( itemName.getSelection() ) {
		// column.setWidth(100);
		// column.setResizable(true);
		// } else {
		// column.setWidth(0);
		// column.setResizable(false);
		// }
		// }
		// });

		// Setting the right sorter
		column.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				int dir = tableViewer.getTable().getSortDirection();
				switch (dir) {
					case SWT.UP:
						dir = SWT.DOWN;
						break;
					case SWT.DOWN:
						dir = SWT.NONE;
						break;
					case SWT.NONE:
						dir = SWT.UP;
						break;
				}
				tableSorter.setColumn(index, dir);
				tableViewer.getTable().setSortDirection(dir);
				if ( dir == SWT.NONE ) {
					tableViewer.getTable().setSortColumn(null);
				} else {
					tableViewer.getTable().setSortColumn(column);
				}
				tableViewer.refresh();
			}
		});

	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this <code>IWorkbenchPart</code> method disposes all nested editors.
	 * This method is automatically called when the editor is closed
	 * and marks the end of the editor's life cycle.
	 * It cleans up any platform resources, such as images, clipboard,
	 * and so on, which were created by this class.
	 * 
	 * @see org.eclipse.ui.part.MultiPageEditorPart#dispose()
	 */
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 * If the save is successful, the part should fire a property
	 * changed event (PROP_DIRTY property), reflecting the new dirty state.
	 * If the save is canceled via user action, or for any other reason,
	 * the part should invoke setCanceled on the IProgressMonitor to
	 * inform the caller
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(final IProgressMonitor monitor) {
		if ( getActivePage() == indexTBL && isPageModified ) {
			updateTextEditorFromTable();
		} else {
			updateTableFromTextEditor();
		}

		isPageModified = false;
		editor.doSave(monitor);
	}

	/**
	 * Returns whether the "Save As" operation is supported by this part.
	 * 
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page 0's tab, and updates this multi-page editor's input to
	 * correspond to the nested editor's.
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		if ( getActivePage() == indexTBL && isPageModified ) {
			updateTextEditorFromTable();
		} else {
			updateTableFromTextEditor();
		}

		isPageModified = false;

		editor.doSaveAs();
		setInput(editor.getEditorInput());
		updateTitle();
	}

	/**
	 * @see org.eclipse.ui.part.MultiPageEditorPart#handlePropertyChange(int)
	 */
	@Override
	protected void handlePropertyChange(final int propertyId) {
		if ( propertyId == IEditorPart.PROP_DIRTY ) {
			isPageModified = isDirty();
		}
		super.handlePropertyChange(propertyId);
	}

	/**
	 * @see org.eclipse.ui.part.MultiPageEditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return isPageModified || super.isDirty();
	}

	/**
	 * Calculates the contents of page 2 when the it is activated.
	 * 
	 * @see org.eclipse.ui.part.MultiPageEditorPart#pageChange(int)
	 */
	@Override
	protected void pageChange(final int newPageIndex) {
		switch (newPageIndex) {
			case indexSRC:
				if ( isDirty() ) {
					updateTextEditorFromTable();
				}
				break;
			case indexTBL:
				if ( isDirty() ) {
					updateTableFromTextEditor();
				}
				break;
		}
		isPageModified = false;
		super.pageChange(newPageIndex);
	}

	/**
     *
     */
	private void updateTextEditorFromTable() {
		editor.getDocumentProvider().getDocument(editor.getEditorInput())
			.set(((AbstractCSVFile) tableViewer.getInput()).getTextRepresentation());
	}

	/**
	 * When the focus shifts to the editor, this method is called;
	 * it must then redirect focus to the appropriate editor based
	 * on which page is currently selected.
	 * 
	 * @see org.eclipse.ui.part.MultiPageEditorPart#setFocus()
	 */
	@Override
	public void setFocus() {
		switch (getActivePage()) {
			case indexSRC:
				editor.setFocus();
				break;
			case indexTBL:
				tableViewer.getTable().setFocus();
				break;
		}
	}

	/**
	 * Closes all project files on project close.
	 * 
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		if ( event.getType() == IResourceChangeEvent.PRE_CLOSE ) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for ( int i = 0; i < pages.length; i++ ) {
						if ( ((FileEditorInput) editor.getEditorInput()).getFile().getProject()
							.equals(event.getResource()) ) {
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
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
			case DUPLICATE_ROW:
				tb.button("action.duplicate.row2", "Duplicate", "Duplicate Row", new SelectionAdapter() {

					@Override
					public void widgetSelected(final SelectionEvent e) {
						CSVRow row = (CSVRow) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
						if ( row != null ) {
							model.duplicateRow(row);
							tableModified();
						}
					}
				}, SWT.RIGHT);
				break;
			case ADD_ROW:
				tb.button("action.add.row2", "Add row",
					"Insert a new row before the currently selected one or at the end of the file if none is selected",
					new SelectionAdapter() {

						@Override
						public void widgetSelected(final SelectionEvent e) {
							CSVRow row = (CSVRow) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
							if ( row != null ) {
								model.addRowAfterElement(row);
							} else {
								model.addRow();
							}
							tableModified();
						}
					}, SWT.RIGHT);
				break;
			case REMOVE_ROW:
				tb.button("action.delete.row2", "Delete row", "Delete currently selected rows", new SelectionAdapter() {

					@Override
					public void widgetSelected(final SelectionEvent e) {

						CSVRow row = (CSVRow) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();

						while (row != null) {
							row = (CSVRow) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
							if ( row != null ) {
								model.removeRow(row);
								tableModified();
							}
						}
					}
				}, SWT.RIGHT);
				break;
			case ADD_COLUMN:
				if ( model.isFirstLineHeader() ) {
					tb.button("action.add.column2", "Add column", "Add new column", new SelectionAdapter() {

						@Override
						public void widgetSelected(final SelectionEvent arg0) {
							// call insert/add column page
							InsertColumnPage acPage =
								new InsertColumnPage(getSite().getShell(), model.getArrayHeader());
							if ( acPage.open() == Window.OK ) {
								String colToInsert = acPage.getColumnNewName();
								model.addColumn(colToInsert);
								tableViewer.setInput(model);
								final TableColumn column = new TableColumn(tableViewer.getTable(), SWT.LEFT);
								column.setText(colToInsert);
								column.setWidth(100);
								column.setResizable(true);
								column.setMoveable(true);
								addMenuItemToColumn(column, model.getColumnCount() - 1);
								defineCellEditing();
								tableModified();
							}
						}
					}, SWT.RIGHT);
				}
				break;
			case REMOVE_COLUMN:
				if ( model.isFirstLineHeader() ) {
					tb.button("action.delete.column2", "Delete column", "Delete one or several column(s)",
						new SelectionAdapter() {

							@Override
							public void widgetSelected(final SelectionEvent e) {

								// call delete column page
								DeleteColumnPage dcPage =
									new DeleteColumnPage(getSite().getShell(), model.getArrayHeader());
								if ( dcPage.open() == Window.OK ) {
									String[] colToDelete = dcPage.getColumnSelected();
									for ( String column : colToDelete ) {
										int colIndex = findColumnForName(column);
										tableViewer.getTable().getColumn(colIndex).dispose();
										// tableHeaderMenu.getItem(colIndex).dispose();
										model.removeColumn(column);
									}
									tableModified();
								}

							}
						}, SWT.RIGHT);
				}
				break;
			case SHOW_COLUMN:
				break;
			case WITH_HEADER:
				ToolItem t =
					tb.check("action.set.header2", "First line is header", "First line is header",
						new SelectionAdapter() {

							@Override
							public void widgetSelected(final SelectionEvent e) {
								ToolItem t = (ToolItem) e.widget;
								model.setFirstLineHeader(t.getSelection());
								try {
									populateTablePage();
								} catch (Exception e1) {
									e1.printStackTrace();
								}
								// tableModified();
								RefreshHandler.run();
							}

						}, SWT.RIGHT);
				t.setSelection(model.isFirstLineHeader());
				break;
			case SAVE_AS:
				tb.button("menu.saveas2", "Save as...", "Save as...", new SelectionAdapter() {

					@Override
					public void widgetSelected(final SelectionEvent e) {
						doSaveAs();
					}
				}, SWT.RIGHT);
				break;

		}

	}
}
