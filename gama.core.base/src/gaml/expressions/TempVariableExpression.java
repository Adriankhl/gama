/*******************************************************************************************************
 *
 * gaml.expressions.TempVariableExpression.java, in plugin gama.core, is part of the source code of the GAMA modeling
 * and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.expressions;

import gama.common.interfaces.IKeyword;
import gama.runtime.exceptions.GamaRuntimeException;
import gama.runtime.scope.IScope;
import gaml.GAML;
import gaml.descriptions.IDescription;
import gaml.types.IType;

public class TempVariableExpression extends VariableExpression {

	protected TempVariableExpression(final String n, final IType<?> type, final IDescription definitionDescription) {
		super(n, type, false, definitionDescription);
	}

	@Override
	public Object _value(final IScope scope) {
		return scope.getVarValue(getName());
	}

	@Override
	public void setVal(final IScope scope, final Object v, final boolean create) throws GamaRuntimeException {
		final Object val = type.cast(scope, v, null, false);
		if (create) {
			scope.addVarWithValue(getName(), val);
		} else {
			scope.setVarValue(getName(), val);
		}
	}

	@Override
	public String getTitle() {
		return "temporary variable " + getName() + " of type " + getGamlType().getTitle();
	}

	/**
	 * @see gaml.expressions.IExpression#getDocumentation()
	 */
	@Override
	public String getDocumentation() {
		final IDescription desc = getDefinitionDescription();
		return "temporary variable " + getName() + " of type " + getGamlType().getTitle()
				+ (desc == null ? "<br>Built In" : "<br>Defined in " + desc.getTitle());
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return GAML.getExpressionFactory().createConst(value(scope), type, name);
	}

	public static class MyselfExpression extends TempVariableExpression {

		protected MyselfExpression(final IType<?> type, final IDescription definitionDescription) {
			super(IKeyword.MYSELF, type, definitionDescription);
		}

		@Override
		public IExpression resolveAgainst(final IScope scope) {
			return this;
		}

		@Override
		public void setVal(final IScope scope, final Object v, final boolean create) throws GamaRuntimeException {}

		@Override
		public String getTitle() {
			return "pseudo variable " + getName() + " of type " + getGamlType().getTitle();
		}

		@Override
		public String getDocumentation() {
			final IDescription desc = getDefinitionDescription();
			return "pseudo variable " + getName() + " of type " + getGamlType().getTitle()
					+ (desc == null ? "<br>Built In" : "<br>Defined in " + desc.getTitle());
		}

	}
}
