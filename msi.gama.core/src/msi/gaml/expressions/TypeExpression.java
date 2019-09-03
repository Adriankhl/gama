/*******************************************************************************************************
 *
 * msi.gaml.expressions.TypeExpression.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling
 * and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gaml.expressions;

import java.util.Collection;

import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.runtime.scope.IScope;
import msi.gaml.descriptions.SpeciesDescription;
import msi.gaml.descriptions.VariableDescription;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

/**
 * Class TypeExpression.
 *
 * @author drogoul
 * @since 7 sept. 2013
 *
 */
public class TypeExpression extends AbstractExpression {

	@SuppressWarnings ("rawtypes")
	public TypeExpression(final IType type) {
		this.type = type;
	}

	@Override
	public IType<?> _value(final IScope scope) throws GamaRuntimeException {
		// Normally never evaluated
		return getDenotedType();
	}

	@Override
	public String getDefiningPlugin() {
		return type.getDefiningPlugin();
	}

	@Override
	public boolean shouldBeParenthesized() {
		return false;
	}

	@Override
	public boolean isConst() {
		return type.canCastToConst();
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		return type.serialize(includingBuiltIn);
	}

	@Override
	public String getTitle() {
		return type.getTitle();
	}

	/**
	 * Method getDocumentation()
	 *
	 * @see msi.gama.common.interfaces.IGamlDescription#getDocumentation()
	 */
	@Override
	public String getDocumentation() {
		return "Represents the data type " + type.getTitle();
	}

	@Override
	public IType<?> getGamlType() {
		return Types.TYPE;
	}

	@Override
	public IType<?> getDenotedType() {
		return type;
	}

	@Override
	public String literalValue() {
		return type.serialize(false);
	}

	/**
	 * Method collectPlugins()
	 *
	 * @see msi.gama.common.interfaces.IGamlDescription#collectPlugins(java.util.Set)
	 */
	// @Override
	// public void collectMetaInformation(final GamlProperties meta) {
	// type.collectMetaInformation(meta);
	// }

	@Override
	public boolean isContextIndependant() {
		return false;
	}

	@Override
	public void collectUsedVarsOf(final SpeciesDescription species, final Collection<VariableDescription> result) {}

}
