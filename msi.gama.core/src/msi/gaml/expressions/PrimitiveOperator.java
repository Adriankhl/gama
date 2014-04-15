/*********************************************************************************************
 * 
 * 
 * 'PrimitiveOperator.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.expressions;

import java.util.List;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.descriptions.*;
import msi.gaml.operators.Cast;
import msi.gaml.statements.*;
import msi.gaml.types.IType;
import com.google.common.base.Function;
import com.google.common.collect.*;

/**
 * PrimitiveOperator. An operator that wraps a primitive or an action.
 * 
 * @author drogoul 4 sept. 07
 */

public class PrimitiveOperator extends AbstractNAryOperator {

	// IStatement.WithArgs statement;
	final Arguments parameters;
	final StatementDescription action;

	public PrimitiveOperator(final OperatorProto proto, final IDescription callerContext,
		final StatementDescription action, final IExpression call, final Arguments args) {
		super(proto, call);
		name = action.getName();
		type = action.getType();
		this.action = action;
		parameters = args;

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		if ( scope == null ) { return null; }
		final IAgent target = Cast.asAgent(scope, arg(0).value(scope));
		if ( target == null ) { return null; }
		// AD 13/05/13 The target should not be pushed so early to the scope, as the arguments will be (incorrectly)
		// evaluated in its context, but how to prevent it ? See Issue 401.
		// One way is (1) to gather the executer
		final IStatement.WithArgs executer = target.getSpecies().getAction(getName());
		// Then, (2) to set the caller to the actual agent on the scope (in the context of which the arguments need to
		// be evaluated
		if ( executer != null ) {
			// Now done by the scope itself: parameters.setCaller(scope.getAgentScope());
			// And finally, (3) to execute the executer on the target (it will be pushed in the scope)
			Object[] result = new Object[1];
			scope.execute(executer, target, parameters, result);
			return result[0];
		}
		return null;
	}

	@Override
	public PrimitiveOperator copy() {
		// See what impact it has got.
		return this;
	}

	@Override
	protected IType computeType(final int t, final IType def, final int kind) {
		return def;
	}

	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public String getTitle() {
		final StringBuilder sb = new StringBuilder(50);
		sb.append("action ").append(getName()).append(" defined in species ").append(arg(0).getType().getSpeciesName())
			.append(" returns ").append(getType().getTitle());
		return sb.toString();

	}

	@Override
	public String getDocumentation() {
		final StringBuilder sb = new StringBuilder(200);

		if ( action.getArgNames().size() > 0 ) {
			List<String> args =
				ImmutableList.copyOf(Iterables.transform(action.getArgs(), new Function<IDescription, String>() {

					@Override
					public String apply(final IDescription desc) {
						StringBuilder sb = new StringBuilder(100);
						sb.append("<li><b>").append(tab).append(desc.getName()).append("</b> of type ")
							.append(desc.getType());
						if ( desc.getFacets().containsKey(IKeyword.DEFAULT) ) {
							sb.append(" <i>(default: ").append(desc.getFacets().getExpr(IKeyword.DEFAULT).toGaml())
								.append(")</i>");
						}
						sb.append("</li>").append(ln);

						return sb.toString();
					}
				}));
			sb.append("Arguments accepted : ").append("<br/><ul>").append(ln);
			for ( String a : args ) {
				sb.append(a);
			}
			sb.append("</ul><br/>");
		}

		return sb.toString();
	}
}
