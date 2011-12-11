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

import static msi.gama.precompiler.ITypeProvider.*;
import msi.gama.interfaces.*;
import msi.gama.internal.compilation.*;
import msi.gama.internal.types.Types;
import msi.gama.kernel.exceptions.GamaRuntimeException;

/**
 * The Class UnaryOpCustomExpr.
 */
public class UnaryOperator extends AbstractExpression implements IOperator {

	protected IExpression child;
	private final IOperatorExecuter helper;
	private final boolean canBeConst;
	private final short typeProvider;
	private final short contentTypeProvider;

	@Override
	public boolean isConst() {
		return canBeConst && child.isConst();
	}

	public UnaryOperator(final IType rt, final IOperatorExecuter exec, final boolean canBeConst,
		final short tProv, final short ctProv) {
		type = rt;
		helper = exec;
		this.canBeConst = canBeConst;
		typeProvider = tProv;
		contentTypeProvider = ctProv;
	}

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {

		Object childValue = child.value(scope);
		try {
			return helper.execute(scope, childValue, null);
		} catch (GamaRuntimeException e1) {
			e1.addContext("when applying the " + literalValue() + " operator on " + childValue);
			throw e1;

		} catch (Exception e) {
			GamaRuntimeException ee = new GamaRuntimeException(e);
			ee.addContext("when applying the " + literalValue() + " operator on " + childValue);
			throw ee;
		}
	}

	@Override
	public UnaryOperator copy() {
		UnaryOperator copy =
			new UnaryOperator(type, helper, canBeConst, typeProvider, contentTypeProvider);
		copy.name = name;
		copy.contentType = contentType;
		return copy;
	}

	@Override
	public String toGaml() {
		return literalValue() + " (" + child.toGaml() + ")";
	}

	@Override
	public String toString() {
		return literalValue() + "(" + child + ")";
	}

	public void computeType() {
		short t = typeProvider;
		type =
			t == CHILD_TYPE ? child.type() : t == CHILD_CONTENT_TYPE ? child.getContentType()
				: t >= 0 ? Types.get(t) : type;
	}

	public void computeContentType() {
		short t = contentTypeProvider;
		contentType =
			t == CHILD_TYPE ? child.type() : t == CHILD_CONTENT_TYPE ? child.getContentType()
				: t >= 0 ? Types.get(t) : type.id() == IType.LIST || type.id() == IType.MATRIX ||
					type.id() == IType.CONTAINER ? child.getContentType() : type.isSpeciesType()
					? type : type.defaultContentType();
	}

	@Override
	public UnaryOperator init(final String name, final IExpression child, final IExpression none) {
		setName(name);
		setChild(child);
		computeType();
		computeContentType();
		return this;
	}

	private void setChild(final IExpression c) {
		child = c;
	}

	public boolean hasChildren() {
		return true;
	}

	@Override
	public IExpression left() {
		return child;
	}

	@Override
	public IExpression right() {
		return null;
	}

}
