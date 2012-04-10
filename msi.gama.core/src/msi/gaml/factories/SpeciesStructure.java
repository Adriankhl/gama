/**
 * Created by drogoul, 20 d�c. 2011
 * 
 */
package msi.gaml.factories;

import java.util.*;
import msi.gama.common.interfaces.*;
import msi.gama.common.util.IErrorCollector;
import msi.gaml.compilation.GamlCompilationError;

public class SpeciesStructure {

	private final ISyntacticElement node;

	private final List<SpeciesStructure> microSpecies;

	private boolean isGrid = false;

	public SpeciesStructure(final ISyntacticElement node, final IErrorCollector collect) {
		microSpecies = new ArrayList<SpeciesStructure>();
		if ( node == null ) {
			collect.add(new GamlCompilationError("Species element is null!", node));
			this.node = null;
			return;
		}
		this.node = node;
		isGrid = node.getKeyword().equals(IKeyword.GRID);
	}

	public boolean isGrid() {
		return isGrid;
	}

	public void addMicroSpecies(final SpeciesStructure species) {
		microSpecies.add(species);
	}

	public List<SpeciesStructure> getMicroSpecies() {
		return microSpecies;
	}

	public ISyntacticElement getNode() {
		return node;
	}

	public String getName() {
		return node.getLabel(IKeyword.NAME);
	}

	@Override
	public String toString() {
		return "Species " + getName();
	}
}