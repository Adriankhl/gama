/*********************************************************************************************
 * 
 *
 * 'LoopStatement.java', in plugin 'msi.gama.core', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.statements;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.precompiler.GamlAnnotations.combination;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.GamlAnnotations.usage;
import msi.gama.precompiler.*;
import msi.gama.runtime.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.IContainer;
import msi.gaml.descriptions.IDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.types.IType;

// A group of commands that can be executed repeatedly.

@symbol(name = IKeyword.LOOP, kind = ISymbolKind.SEQUENCE_STATEMENT, with_sequence = true)
@facets(value = { @facet(name = IKeyword.FROM, type = IType.INT, optional = true, doc = @doc("an int expression")),
	@facet(name = IKeyword.TO, type = IType.INT, optional = true, doc = @doc("an int expression")),
	@facet(name = IKeyword.STEP, type = IType.INT, optional = true, doc = @doc("an int expression")),
	@facet(name = IKeyword.NAME, type = IType.NEW_TEMP_ID, optional = true, doc = @doc("a temporary variable name")),
	@facet(name = IKeyword.OVER, type = { IType.CONTAINER, IType.POINT }, optional = true, doc = @doc("a list, point, matrix or map expression")),
	@facet(name = IKeyword.WHILE, type = IType.BOOL, optional = true, doc = @doc("a boolean expression")),
	@facet(name = IKeyword.TIMES, type = IType.INT, optional = true, doc = @doc("an int expression")) },
combinations = {

@combination({ IKeyword.FROM, IKeyword.TO, IKeyword.NAME, IKeyword.STEP }),
	@combination({ IKeyword.FROM, IKeyword.TO, IKeyword.NAME }), @combination({ IKeyword.OVER, IKeyword.NAME }),
	@combination({ IKeyword.TIMES }), @combination({ IKeyword.WHILE }) }, omissible = IKeyword.NAME)
@inside(kinds = { ISymbolKind.BEHAVIOR, ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.LAYER })
@doc(value="Allows the agent to perform the same set of statements either a fixed number of times, or while a condition is true, or by progressing in a collection of elements or along an interval of integers. Be aware that there are no prevention of infinite loops. As a consequence, open loops should be used with caution, as one agent may block the execution of the whole model.", usages = {
	@usage(value="The basic syntax for repeating a fixed number of times a set of statements is:", examples = {@example(value="loop times: an_int_expression {", isExecutable=false),@example(value="     // [statements]", isExecutable=false),@example(value="}", isExecutable=false),@example(value="int sumTimes <- 1;", isTestOnly=true),@example(value="loop times: 3 {sumTimes <- sumTimes + sumTimes;}", isTestOnly=true),@example(var="sumTimes",equals="8",isTestOnly=true)}),
	@usage(value="The basic syntax for repeating a set of statements while a condition holds is:", examples = {@example(value="loop while: a_bool_expression {", isExecutable=false),@example(value="     // [statements]", isExecutable=false),@example(value="}", isExecutable=false),@example(value="int sumWhile <- 1;", isTestOnly=true),@example(value="loop while: (sumWhile < 5) {sumWhile <- sumWhile + sumWhile;}", isTestOnly=true),@example(var="sumWhile",equals="8",isTestOnly=true)}),
	@usage(value="The basic syntax for repeating a set of statements by progressing over a container of a point is:", examples = {@example(value="loop a_temp_var over: a_collection_expression {", isExecutable=false),@example(value="     // [statements]", isExecutable=false),@example(value="}", isExecutable=false)}),
	@usage(value="The basic syntax for repeating a set of statements while an index iterates over a range of values with a fixed step of 1 is:", examples = {@example(value="loop a_temp_var from: int_expression_1 to: int_expression_2 {", isExecutable=false),@example(value="     // [statements]", isExecutable=false),@example(value="}", isExecutable=false)}),
	@usage(value="The incrementation step of the index can also be chosen:", examples = {@example(value="loop a_temp_var from: int_expression_1 to: int_expression_2 step: int_expression3 {", isExecutable=false),@example(value="     // [statements]", isExecutable=false),@example(value="}", isExecutable=false),@example(value="int sumFor <- 0;", isTestOnly=true),@example(value="loop i from: 10 to: 30 step: 10 {sumFor <- sumFor + i;}", isTestOnly=true),@example(var="sumFor",equals="60",isTestOnly=true)}),
	@usage(value="In these latter three cases, the name facet designates the name of a temporary variable, whose scope is the loop, and that takes, in turn, the value of each of the element of the list (or each value in the interval). For example, in the first instance of the \"loop over\" syntax :", examples = {@example(value="int a <- 0;"),@example(value="loop i over: [10, 20, 30] {"),@example(value="     a <- a + i;"),@example(value="} // a now equals 60"), @example(var="a",equals="60",isTestOnly=true)}),
	@usage(value="The second (quite common) case of the loop syntax allows one to use an interval of integers. The from and to facets take an integer expression as arguments, with the first (resp. the last) specifying the beginning (resp. end) of the inclusive interval (i.e. [to, from]). If the step is not defined, it is assumed to be equal to 1.", examples = {@example(value="list the_list <-list (species_of (self));"),@example(value="loop i from: 0 to: length (the_list) - 1 {"),@example(value="     ask the_list at i {"),@example(value="        // ..."), @example(value="     }"),@example(value="} // every  agent of the list is asked to do something")})
})
public class LoopStatement extends AbstractStatementSequence {

	private final LoopExecuter executer;
	private final String varName;
	private final Object[] result = new Object[1];

	public LoopStatement(final IDescription desc) throws GamaRuntimeException {
		super(desc);
		final boolean isWhile = getFacet(IKeyword.WHILE) != null;
		final boolean isList = getFacet(IKeyword.OVER) != null;
		final boolean isBounded = getFacet(IKeyword.FROM) != null && getFacet(IKeyword.TO) != null;
		varName = getLiteral(IKeyword.NAME);
		executer =
				isWhile ? new While() : isList ? new Over() : isBounded ? new Bounded()
				: new Times();
	}

	@Override
	public void enterScope(final IScope scope) {
		// 25/02/14: Suppressed because already done in loopBody() : super.enterScope(scope);

		if ( varName != null ) {
			scope.addVarWithValue(varName, null);
		}
	}

	@Override
	public void leaveScope(final IScope scope) {
		// Should clear any _loop_halted status present
		scope.popLoop();
		// 25/02/14: Suppressed because already done in loopBody() : super.leaveScope(scope);
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		return executer.runIn(scope);
	}

	protected boolean loopBody(final IScope scope, final Object var) {
		scope.push(this);
		if ( varName != null ) {
			scope.setVarValue(varName, var);
		}
		result[0] = super.privateExecuteIn(scope);
		scope.pop(this);
		return !scope.interrupted();
	}

	interface LoopExecuter {

		abstract Object runIn(final IScope scope);
	}

	class Bounded implements LoopExecuter {

		private final IExpression from = getFacet(IKeyword.FROM);
		private final IExpression to = getFacet(IKeyword.TO);
		private final IExpression step = getFacet(IKeyword.STEP);
		private Integer constantFrom, constantTo, constantStep;

		Bounded() throws GamaRuntimeException {
			final IScope scope = GAMA.obtainNewScope();
			if ( from.isConst() ) {
				constantFrom = Cast.asInt(scope, from.value(scope));
			}
			if ( to.isConst() ) {
				constantTo = Cast.asInt(scope, to.value(scope));
			}
			if ( step == null ) {
				constantStep = 1;
			} else if ( step.isConst() ) {
				constantStep = Cast.asInt(scope, step.value(scope));
			}
			GAMA.releaseScope(scope);
		}

		@Override
		public Object runIn(final IScope scope) throws GamaRuntimeException {
			final int f = constantFrom == null ? Cast.asInt(scope, from.value(scope)) : constantFrom;
			final int t = constantTo == null ? Cast.asInt(scope, to.value(scope)) : constantTo;
			final int s = constantStep == null ? Cast.asInt(scope, step.value(scope)) : constantStep;
			Object[] result = new Object[1];
			for ( int i = f, n = t + 1; i < n && loopBody(scope, i); i += s ) {}
			return result[0];
		}
	}

	class Over implements LoopExecuter {

		private final IExpression over = getFacet(IKeyword.OVER);

		@Override
		public Object runIn(final IScope scope) throws GamaRuntimeException {
			final Object obj = over.value(scope);
			final Iterable list_ =
				!(obj instanceof IContainer) ? Cast.asList(scope, obj) : ((IContainer) obj).iterable(scope);
			for ( final Object each : list_ ) {
				if ( !loopBody(scope, each) ) {
					break;
				}
			}
			return result[0];
		}
	}

	class Times implements LoopExecuter {

		private final IExpression times = getFacet(IKeyword.TIMES);
		private Integer constantTimes;

		Times() throws GamaRuntimeException {
			if ( times.isConst() ) {
				constantTimes = Cast.as(times, Integer.class);
			}
		}

		@Override
		public Object runIn(final IScope scope) throws GamaRuntimeException {
			final int max = constantTimes == null ? Cast.asInt(scope, times.value(scope)) : constantTimes;
			for ( int i = 0; i < max && loopBody(scope, null); i++ ) {}
			return result[0];
		}

	}

	class While implements LoopExecuter {

		private final IExpression cond = getFacet(IKeyword.WHILE);

		@Override
		public Object runIn(final IScope scope) throws GamaRuntimeException {
			while (Cast.asBool(scope, cond.value(scope)) && loopBody(scope, null)) {}
			return result[0];
		}
	}

}