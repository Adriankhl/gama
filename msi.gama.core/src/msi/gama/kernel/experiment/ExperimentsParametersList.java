/*********************************************************************************************
 * 
 * 
 * 'ExperimentsParametersList.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.kernel.experiment;

import gnu.trove.map.hash.THashMap;
import java.util.*;
import msi.gama.common.interfaces.IParameterEditor;
import msi.gama.common.util.GuiUtils;
import msi.gama.metamodel.agent.IAgent;

public class ExperimentsParametersList extends EditorsList<String> {

	public ExperimentsParametersList(final Collection<? extends IParameter> params) {
		super();
		add(params, null);
	}

	@Override
	public String getItemDisplayName(final String obj, final String previousName) {
		return obj;
	}

	@Override
	public void add(final Collection<? extends IParameter> params, final IAgent agent) {
		for ( final IParameter var : params ) {
			IParameterEditor gp = GuiUtils.getEditorFactory().create((IAgent) null, var, null);
			String cat = var.getCategory();
			cat = cat == null ? "General" : cat;
			addItem(cat);
			categories.get(cat).put(gp.getParam().getName(), gp);
		}
	}

	@Override
	public boolean addItem(final String cat) {
		if ( !categories.containsKey(cat) ) {
			categories.put(cat, new THashMap<String, IParameterEditor>());
			return true;
		}
		return false;
	}

	@Override
	public void updateItemValues() {
		for ( Map.Entry<String, THashMap<String, IParameterEditor>> entry : categories.entrySet() ) {
			for ( IParameterEditor gp : entry.getValue().values() ) {
				gp.updateValue();
			};
		}
	}

}
