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
package msi.gama.gui.parameters;

import java.util.ArrayList;
import msi.gama.kernel.GAMA;
import msi.gama.kernel.exceptions.GamlException;
import msi.gama.util.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * The ListParameterDialog supply a window to help user to modify the list in the visual way.
 */
public class ListEditorDialog extends Dialog {

	private final ArrayList<String> data = new ArrayList<String>();

	private Button newElementButton = null;
	private Button upButton = null;
	private Button downButton = null;
	private Button removeButton = null;
	private List list = null;
	private String listname = null;

	protected ListEditorDialog(final Shell parentShell, final GamaList list, final String listname) {
		super(parentShell);
		this.listname = listname;
		for ( Object o : list ) {
			data.add(Cast.toGaml(o));
		}
		// final String tmpGamlList = list.substring(1, list.length() - 1);
		// final StringTokenizer elementTokenizer = new StringTokenizer(tmpGamlList, ",");
		// while (elementTokenizer.hasMoreTokens()) {
		// final String tmp = elementTokenizer.nextToken().trim();
		// data.add(tmp);
		// }
	}

	/**
	 * Creates and returns the contents of the upper part of this dialog (above the button bar).
	 * 
	 * @param parent the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout(3, false);
		container.setLayout(gridLayout);

		final Label dialogLabel = new Label(container, SWT.NONE);
		dialogLabel
			.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, false, false, 3, 1));
		dialogLabel.setText("Modify the list \'" + listname + "\'");

		/** The Text widget containing the new element to be added. */
		final Text newElementText = new Text(container, SWT.BORDER);
		newElementText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent me) {
				if ( newElementText.getText() == null ||
					newElementText.getText().trim().length() == 0 ) {
					newElementButton.setEnabled(false);
				}
				if ( newElementText.getText().trim().length() > 0 ) {
					newElementButton.setEnabled(true);
				}
			}
		});

		final GridData newElementTextGridData =
			new GridData(GridData.FILL, GridData.CENTER, true, false);
		newElementTextGridData.widthHint = 40;
		newElementText.setLayoutData(newElementTextGridData);

		/** The button used to add one new element. */
		newElementButton = new Button(container, SWT.PUSH);
		newElementButton.setText("Add");
		newElementButton.setToolTipText("Add new element");
		newElementButton.setEnabled(false);
		newElementButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(final MouseEvent me) {
				list.add(newElementText.getText());
				data.add(newElementText.getText());
				newElementText.setText("");
				newElementButton.setEnabled(false);
			}
		});

		/**
		 * The list widget containing all the elements of the corresponding GAML list.
		 */
		list = new List(container, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		for ( final String gamlElement : data ) {
			list.add(gamlElement);
		}

		final GridData listGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 5);
		listGridData.widthHint = 60;
		listGridData.heightHint = 100;
		list.setLayoutData(listGridData);

		list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(final MouseEvent me) {
				if ( list.getSelectionIndex() != -1 ) {
					if ( list.getSelectionIndex() > 0 ) {
						upButton.setEnabled(true);
					} else {
						upButton.setEnabled(false);
					}

					if ( list.getSelectionIndex() < data.size() - 1 ) {
						downButton.setEnabled(true);
					} else {
						downButton.setEnabled(false);
					}

					removeButton.setEnabled(true);
				} else {
					upButton.setEnabled(false);
					downButton.setEnabled(false);
					removeButton.setEnabled(false);
				}
			}
		});

		final Composite buttonBox = new Composite(container, SWT.NONE);

		final GridLayout buttonBoxgridLayout = new GridLayout();
		buttonBoxgridLayout.numColumns = 1;
		buttonBox.setLayout(buttonBoxgridLayout);

		final GridData buttonBoxGridData = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 5);
		buttonBoxGridData.heightHint = 100;
		buttonBox.setLayoutData(buttonBoxGridData);

		/** The Up button used to move an element up one position. */
		upButton = new Button(buttonBox, SWT.PUSH);
		upButton.setText("Up");
		upButton.setEnabled(false);

		final GridData upButtonGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		upButtonGridData.horizontalSpan = 1;
		upButton.setLayoutData(upButtonGridData);

		upButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(final MouseEvent me) {
				int selectionIndex = list.getSelectionIndex();

				final String currentSelectedElement = list.getItem(selectionIndex);
				list.remove(selectionIndex);
				data.remove(selectionIndex);

				if ( selectionIndex > 0 ) {
					selectionIndex--;
				}
				list.add(currentSelectedElement, selectionIndex);
				data.add(selectionIndex, currentSelectedElement);

				list.setSelection(selectionIndex);

				if ( selectionIndex == 0 ) {
					upButton.setEnabled(false);
				}

				if ( selectionIndex < data.size() - 1 ) {
					downButton.setEnabled(true);
				}
			}
		});

		/** The Down button used to move an element down on position. */
		downButton = new Button(buttonBox, SWT.PUSH);
		downButton.setText("Down");
		downButton.setEnabled(false);

		final GridData downButtonGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		downButtonGridData.horizontalSpan = 1;
		downButton.setLayoutData(downButtonGridData);

		downButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(final MouseEvent me) {
				int selectionIndex = list.getSelectionIndex();

				final String currentSelectedElement = list.getItem(selectionIndex);
				list.remove(selectionIndex);
				data.remove(selectionIndex);

				if ( selectionIndex < data.size() ) {
					selectionIndex++;
				}

				list.add(currentSelectedElement, selectionIndex);
				data.add(selectionIndex, currentSelectedElement);

				list.setSelection(selectionIndex);

				if ( selectionIndex >= data.size() - 1 ) {
					downButton.setEnabled(false);
				}

				if ( selectionIndex > 0 ) {
					upButton.setEnabled(true);
				}
			}
		});

		/** The Remove button used to remove an element. */
		removeButton = new Button(buttonBox, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.setEnabled(false);

		final GridData removeButtonGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		removeButtonGridData.horizontalSpan = 1;
		removeButton.setLayoutData(removeButtonGridData);

		removeButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(final MouseEvent me) {
				int selectionIndex = list.getSelectionIndex();

				list.remove(selectionIndex);
				data.remove(selectionIndex);

				if ( data.size() > 0 ) {
					if ( selectionIndex >= data.size() ) {
						selectionIndex--;
					}

					if ( selectionIndex >= 0 && selectionIndex < data.size() ) {
						list.setSelection(selectionIndex);
						removeButton.setEnabled(true);
					}

					if ( selectionIndex >= data.size() - 1 ) {
						downButton.setEnabled(false);
					} else {
						downButton.setEnabled(true);
					}

					if ( selectionIndex > 0 ) {
						upButton.setEnabled(true);
					} else {
						upButton.setEnabled(false);
					}
				} else {
					upButton.setEnabled(false);
					downButton.setEnabled(false);
					removeButton.setEnabled(false);
				}
			}
		});
		return container;
	}

	public GamaList getList(final ListEditor editor) {
		// GamaList result = new GamaList();

		boolean isFirstElement = true;
		final StringBuffer tmp = new StringBuffer("[");

		for ( final String element : data ) {
			if ( isFirstElement ) {
				isFirstElement = false;
				tmp.append(element);
			} else {
				tmp.append("," + element);
			}
		}
		tmp.append("]");
		try {
			return (GamaList) GAMA.evaluateExpression(tmp.toString(), editor.getAgent());
		} catch (GamlException e) {
			return new GamaList();
		}
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
