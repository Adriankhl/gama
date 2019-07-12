/*********************************************************************************************
 *
 * 'GamaListReducer.java, in plugin ummisco.gama.serialize, is part of the source code of the GAMA modeling and
 * simulation platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gama.serializer.gamaType.reduced;

import java.util.ArrayList;

import msi.gama.kernel.simulation.SimulationAgent;
import msi.gama.runtime.IScope;
import msi.gama.util.GamaListFactory;
import msi.gama.util.IList;
import msi.gama.util.IReference;
import msi.gaml.types.IType;
import ummisco.gama.serializer.gamaType.reference.ReferenceList;

@SuppressWarnings ({ "rawtypes" })
public class GamaListReducer {
	private ArrayList<Object> valuesListReducer = new ArrayList<>();
	private final IType contentTypeListReducer;

	public GamaListReducer(final IList l) {
		contentTypeListReducer = l.getGamlType().getContentType();

		for (final Object p : l) {
			valuesListReducer.add(p);
		}
	}

	public IList constructObject(final IScope scope) {

		boolean isReference = false;
		int i = 0;
		while (!isReference && i < valuesListReducer.size()) {
			isReference = IReference.isReference(valuesListReducer.get(i));
			i++;
		}

		return isReference ? new ReferenceList(this)
				: GamaListFactory.create(scope, contentTypeListReducer, valuesListReducer);
	}

	public ArrayList<Object> getValuesListReducer() {
		return valuesListReducer;
	}

	public IType getContentTypeListReducer() {
		return contentTypeListReducer;
	}

	public void unreferenceReducer(final SimulationAgent sim) {
		final ArrayList<Object> listWithoutRef = new ArrayList<>();

		for (final Object elt : valuesListReducer) {
			listWithoutRef.add(IReference.getObjectWithoutReference(elt, sim));
		}

		valuesListReducer = listWithoutRef;
	}
}
