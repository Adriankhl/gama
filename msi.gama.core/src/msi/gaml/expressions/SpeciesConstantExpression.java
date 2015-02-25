/*********************************************************************************************
 * 
 * 
 * 'SpeciesConstantExpression.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.expressions;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.runtime.IScope;
import msi.gaml.descriptions.ModelDescription;
import msi.gaml.types.IType;

public class SpeciesConstantExpression extends ConstantExpression {

	boolean computed = false;

	public SpeciesConstantExpression(final String val, final IType t) {
		super(val, t);
	}

	@Override
	public Object value(final IScope scope) {
		IAgent a = scope.getAgentScope();
		if ( a != null ) {
			//hqnghi if main description contains micro-description then species comes from micro-model 
			ModelDescription micro = this.getType().getContentType().getSpecies().getModelDescription();
			ModelDescription main  = (ModelDescription) scope.getModel().getDescription(); 
			Boolean fromMicroModel = main.getMicroModel(micro.getAlias()) != null ;
			if( !fromMicroModel ) {
				IPopulation pop = scope.getAgentScope().getPopulationFor((String) value);
				if ( pop != null ) { return pop.getSpecies(); }
				return scope.getSimulationScope().getModel().getSpecies((String) value);
			}else {
				IPopulation pop = scope.getAgentScope().getExternMicroPopulationFor((String) value);
				if ( pop != null ) { return pop.getSpecies(); }		
				return scope.getSimulationScope().getModel().getSpecies((String) value, this.getType().getContentType().getSpecies());
			}
			//end-hqnghi
		}
		return null;
	}

	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public String serialize(boolean includingBuiltIn) {
		if ( computed ) { return super.serialize(includingBuiltIn); }
		return (String) value;
	}

}
