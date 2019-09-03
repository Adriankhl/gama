/*******************************************************************************************************
 *
 * msi.gaml.expressions.NowUnitExpression.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling
 * and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gaml.expressions;

import java.time.LocalDateTime;

import msi.gama.runtime.scope.IScope;
import msi.gama.util.GamaDate;
import msi.gaml.types.Types;

public class NowUnitExpression extends UnitConstantExpression<GamaDate> {

	public NowUnitExpression(final String name, final String doc) {
		super(null, Types.DATE, name, doc, null);
	}

	@Override
	public GamaDate _value(final IScope scope) {
		return GamaDate.of(LocalDateTime.now());
	}

	@Override
	public boolean isConst() {
		return false;
	}

}
