/*******************************************************************************************************
 *
 * msi.gaml.statements.AbstractPlaceHolderStatement.java, in plugin msi.gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gaml.statements;

import msi.gama.runtime.scope.IScope;
import msi.gaml.descriptions.IDescription;

public abstract class AbstractPlaceHolderStatement extends AbstractStatement {

	public AbstractPlaceHolderStatement(final IDescription desc) {
		super(desc);
	}

	@Override
	protected Object privateExecuteIn(final IScope stack) {
		return null;
	}

}
