/*******************************************************************************************************
 *
 * gaml.compilation.IGamaHelper.java, in plugin gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.compilation.interfaces;

import gama.common.interfaces.IAgent;
import gama.common.interfaces.IVarAndActionSupport;
import gama.runtime.scope.IScope;

/**
 * Written by drogoul Modified on 14 ao�t 2010. Modified on 23 Apr. 2013. A general purpose helper that can be
 * subclassed like a Runnable.
 *
 * @todo Description
 *
 */
@SuppressWarnings ({ "rawtypes" })
@FunctionalInterface
public interface IGamaHelper<T> {

	Object[] EMPTY_VALUES = new Object[0];

	default Class getSkillClass() {
		return null;
	}

	default T run(final IScope scope, final IAgent agent, final IVarAndActionSupport skill) {
		return run(scope, agent, skill, EMPTY_VALUES);
	}

	T run(final IScope scope, final IAgent agent, final IVarAndActionSupport skill, final Object values);

}