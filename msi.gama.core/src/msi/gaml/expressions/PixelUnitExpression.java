/*********************************************************************************************
 * 
 * 
 * 'PixelUnitExpression.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.expressions;

import msi.gama.common.interfaces.IGraphics;
import msi.gama.runtime.IScope;
import msi.gaml.types.Types;

public class PixelUnitExpression extends UnitConstantExpression {

	public PixelUnitExpression(final String name, final String doc) {
		super(1.0, Types.FLOAT, name, doc, null);
	}

	@Override
	public Double value(final IScope scope) {
		IGraphics g = scope.getGraphics();
		if ( g == null ) { return 0d; }
		double ratio = g.getyRatioBetweenPixelsAndModelUnits();
		if ( ratio == 0d ) { return 0d; }
		Double v = 1d / ratio;
		return v;
	}

	@Override
	public boolean isConst() {
		return false;
	}

}
