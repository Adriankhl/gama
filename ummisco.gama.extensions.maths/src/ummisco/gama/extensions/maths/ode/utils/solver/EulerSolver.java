/*********************************************************************************************
 *
 * 'EulerSolver.java, in plugin ummisco.gama.extensions.maths, is part of the source code of the GAMA modeling and
 * simulation platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gama.extensions.maths.ode.utils.solver;

import org.apache.commons.math3.ode.nonstiff.EulerIntegrator;

import msi.gama.util.IList;
import msi.gama.util.IMap;

public class EulerSolver extends Solver {

	public EulerSolver(final double step, final IMap<String, IList<Double>> integrated_val) {
		super(step, new EulerIntegrator(step), integrated_val);
	}

}
