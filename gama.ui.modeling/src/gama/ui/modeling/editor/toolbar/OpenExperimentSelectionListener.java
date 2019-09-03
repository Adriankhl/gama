/*********************************************************************************************
 *
 * 'OpenExperimentSelectionListener.java, in plugin gama.ui.base.modeling, is part of the source code of the GAMA
 * modeling and simulation platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package gama.ui.modeling.editor.toolbar;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;

import gama.ui.base.controls.FlatButton;
import gama.ui.base.utils.WorkbenchHelper;
import gama.ui.base.views.toolbar.Selector;
import gama.ui.modeling.editor.GamlEditor;
import gama.ui.modeling.editor.GamlEditorState;
import gama.common.interfaces.IModel;
import gama.common.preferences.GamaPreferences;
import gama.runtime.GAMA;
import gama.runtime.exceptions.GamaRuntimeException;
import gaml.compilation.GAML;

/**
 * The class CreateExperimentSelectionListener.
 *
 * @author drogoul
 * @since 27 août 2016
 *
 */
public class OpenExperimentSelectionListener implements Selector {

	GamlEditor editor;
	GamlEditorState state;

	/**
	 *
	 */
	public OpenExperimentSelectionListener(final GamlEditor editor, final GamlEditorState state) {
		this.editor = editor;
		this.state = state;
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(final SelectionEvent e) {

		// final IGui gui = GAMA.getRegularGui();
		// We refuse to run if there is no XtextGui available.
		editor.doSave(null);
		if (GamaPreferences.Modeling.EDITOR_SAVE.getValue()) {
			WorkbenchHelper.getPage().saveAllEditors(GamaPreferences.Modeling.EDITOR_SAVE_ASK.getValue());
		}
		String name = (String) ((FlatButton) e.widget).getData("exp");
		final int i = state.abbreviations.indexOf(name);
		if (i == -1) { return; }
		name = state.experiments.get(i);
		final IXtextDocument doc = editor.getDocument();
		IModel model = null;
		try {
			model = doc.readOnly(state -> GAML.compile(state.getURI(), null));
		} catch (final GamaRuntimeException ex) {
			GAMA.getGui().error("Experiment cannot be instantiated because of the following error: " + ex.getMessage());
		}
		GAMA.runModel(model, name, false);

	}

}
