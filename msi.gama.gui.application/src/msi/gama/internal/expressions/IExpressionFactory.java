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
package msi.gama.internal.expressions;

import msi.gama.interfaces.*;
import msi.gama.internal.compilation.*;
import msi.gama.internal.expressions.*;
import msi.gama.kernel.exceptions.*;

/**
 * Written by drogoul Modified on 27 d�c. 2010
 * 
 * @todo Description
 * 
 */
public interface IExpressionFactory {

	public abstract IExpression createConst(final Object val) throws GamaRuntimeException;

	public abstract IExpression createConst(final Object val, final IType type)
		throws GamaRuntimeException;

	public abstract IExpression createConst(final Object val, final IType type,
		final IType contentType) throws GamaRuntimeException;

	public abstract IExpression createExpr(final ExpressionDescription s) throws GamlException;

	public abstract IExpression createExpr(final ExpressionDescription s, final IDescription context)
		throws GamlException;

	public abstract IVarExpression createVar(final String name, final IType type,
		final IType contentType, final boolean isConst, final int scope);

	public abstract IOperator createOperator(final String name, final boolean binary,
		final boolean var, final IType returnType, final IOperatorExecuter helper,
		final boolean canBeConst, final short type, final short contentType, final boolean lazy);

	public abstract IOperator createPrimitiveOperator(final String name);

	public abstract IOperator copyPrimitiveOperatorForSpecies(IOperator op, IDescription species);

	public abstract IExpression createUnaryExpr(final String op, final IExpression c) throws GamlException;

}