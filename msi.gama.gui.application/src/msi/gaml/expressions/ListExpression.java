/*
 * GAMA - V1.4  http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2011
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2011
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen  (Batch, GeoTools & JTS), 2009-2011
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2011
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gaml.expressions;

import java.util.List;
import msi.gama.interfaces.*;
import msi.gama.internal.types.Types;
import msi.gama.kernel.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;

/**
 * ListValueExpr.
 * 
 * @author drogoul 23 août 07
 */
public class ListExpression extends AbstractExpression {

	private final IExpression[] elements;
	private final Object[] values;
	private boolean isConst, computed;

	ListExpression(final List<? extends IExpression> elements) {
		this.elements = elements.toArray(new IExpression[0]);
		values = new Object[this.elements.length];
		setName(elements.toString());
		type = Types.get(IType.LIST);
		isConst();
	}

	@Override
	public GamaList value(final IScope scope) throws GamaRuntimeException {
		if ( isConst && computed ) { return new GamaList(values); }
		for ( int i = 0; i < elements.length; i++ ) {
			values[i] = elements[i].value(scope);
		}
		computed = true;
		// Important NOT to return the reference to values (but a copy of it).
		return new GamaList(values);
	}

	@Override
	public String toString() {
		return String.valueOf(elements);
	}

	@Override
	public boolean isConst() {
		for ( final IExpression e : elements ) {
			if ( !e.isConst() ) { return false; }
		}
		isConst = true;
		return true;
	}

	public void setContentType(final IType ct) {
		contentType = ct;
	}

	@Override
	public String toGaml() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for ( int i = 0; i < elements.length; i++ ) {
			if ( i > 0 ) {
				sb.append(',');
			}
			sb.append(elements[i].toGaml());
		}
		sb.append(']');
		return sb.toString();
	}

}
