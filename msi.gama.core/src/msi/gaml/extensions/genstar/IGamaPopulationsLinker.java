package msi.gaml.extensions.genstar;

import java.util.List;

import msi.gama.metamodel.agent.IMacroAgent;
import msi.gama.runtime.IScope;

public interface IGamaPopulationsLinker {

	public abstract void setTotalRound(final int totalRound); 
	
	public abstract int getTotalRound();
	
	public abstract int getCurrentRound();
	
	public abstract void establishRelationship(final IScope scope, final List<List<IMacroAgent>> populations);
}
