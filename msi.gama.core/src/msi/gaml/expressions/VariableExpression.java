/*******************************************************************************************************
 *
 * msi.gaml.expressions.VariableExpression.java, in plugin msi.gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gaml.expressions;

import msi.gaml.descriptions.IDescription;
import msi.gaml.types.IType;
import ummisco.gama.dev.utils.DEBUG;

public abstract class VariableExpression extends AbstractExpression implements IVarExpression {

	protected final String name;
	protected final boolean isNotModifiable;
	private final IDescription definitionDescription;

	protected VariableExpression(final String n, final IType<?> type, final boolean notModifiable,
			final IDescription definitionDescription) {
		name = n;
		if(DEBUG.flag2&&name.equals("each")) {
			System.out.println("   each of type "+type);
		}
		setType(type);
		isNotModifiable = notModifiable;
		this.definitionDescription = definitionDescription;
	}

	@Override
	public IExpression getOwner() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public VariableExpression getVar() {
		return this;
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		return getName();
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean isNotModifiable() {
		return isNotModifiable;
	}

	@Override
	public boolean isConst() {
		return false;
	}

	public IDescription getDefinitionDescription() {
		return definitionDescription;
	}

	protected void setType(final IType<?> type) {
		this.type = type;
	}

	@Override
	public String getTitle() {

		return isNotModifiable ? "constant" : "variable " + getName() + " of type " + getGamlType()
				+ (definitionDescription != null ? " defined in " + getDefinitionDescription().getTitle() : "");
	}

	@Override
	public boolean shouldBeParenthesized() {
		return false;
	}

	/**
	 * Method collectPlugins()
	 * 
	 * @see msi.gama.common.interfaces.IGamlDescription#collectPlugins(java.util.Set)
	 */
	// @Override
	// public void collectMetaInformation(final GamlProperties meta) {
	// if (definitionDescription != null) {
	// final IDescription var = definitionDescription.getSpeciesContext().getAttribute(getName());
	// if (var != null) {
	// meta.put(GamlProperties.PLUGINS, var.getDefiningPlugin());
	// }
	// }
	// }

	@Override
	public boolean isContextIndependant() {
		return false;
	}

}
