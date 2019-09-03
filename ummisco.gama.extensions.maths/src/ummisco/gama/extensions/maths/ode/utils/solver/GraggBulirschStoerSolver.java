/*********************************************************************************************
 *
 * 'GraggBulirschStoerSolver.java, in plugin ummisco.gama.extensions.maths, is part of the source code of the GAMA
 * modeling and simulation platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gama.extensions.maths.ode.utils.solver;

import org.apache.commons.math3.ode.nonstiff.GraggBulirschStoerIntegrator;

import msi.gama.util.list.IList;
import msi.gama.util.map.IMap;

public class GraggBulirschStoerSolver extends Solver {

	public GraggBulirschStoerSolver(final double minStep, final double maxStep, final double scalAbsoluteTolerance,
			final double scalRelativeTolerance, final IMap<String, IList<Double>> integrated_val) {
		super((minStep + maxStep) / 2,
				new GraggBulirschStoerIntegrator(minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance),
				integrated_val);
	}

}