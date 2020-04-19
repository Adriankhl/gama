/*******************************************************************************************************
 *
 * gaml.operators.System.java, in plugin gama.core, is part of the source code of the GAMA modeling and simulation
 * platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.operators;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;

import gama.common.interfaces.IAgent;
import gama.common.interfaces.IKeyword;
import gama.common.interfaces.IValue;
import gama.common.util.TextBuilder;
import gama.processor.annotations.GamlAnnotations.doc;
import gama.processor.annotations.GamlAnnotations.example;
import gama.processor.annotations.GamlAnnotations.no_test;
import gama.processor.annotations.GamlAnnotations.operator;
import gama.processor.annotations.GamlAnnotations.test;
import gama.processor.annotations.GamlAnnotations.usage;
import gama.processor.annotations.IConcept;
import gama.processor.annotations.IOperatorCategory;
import gama.processor.annotations.ITypeProvider;
import gama.runtime.exceptions.GamaRuntimeException;
import gama.runtime.scope.IScope;
import gama.util.map.GamaMapFactory;
import gama.util.map.IMap;
import gaml.GAML;
import gaml.descriptions.IDescription;
import gaml.expressions.IExpression;
import gaml.expressions.MapExpression;
import gaml.types.GamaType;
import gaml.types.IType;
import gaml.types.Types;

/**
 * Written by drogoul Modified on 10 d�c. 2010
 *
 * @todo Description
 *
 */
public class System {

	@operator (
			value = "dead",
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.SPECIES })
	@doc (
			value = "true if the agent is dead (or null), false otherwise.",
			examples = @example (
					value = "dead(agent_A)",
					equals = "true or false",
					isExecutable = false))
	@test ("dead(simulation) = false")
	public static Boolean opDead(final IScope scope, final IAgent a) {
		return a == null || a.dead();
	}

	@operator (
			value = "is_error",
			can_be_const = true,
			concept = IConcept.TEST)
	@doc ("Returns whether or not the argument raises an error when evaluated")
	@test ("is_error(1.0 = 1) = false")
	public static Boolean is_error(final IScope scope, final IExpression expr) {
		try {
			expr.value(scope);
		} catch (final GamaRuntimeException e) {
			return !e.isWarning();
		} catch (final Exception e1) {}
		return false;
	}

	@operator (
			value = "is_warning",
			can_be_const = true,
			concept = IConcept.TEST)
	@doc ("Returns whether or not the argument raises a warning when evaluated")
	@test ("is_warning(1.0 = 1) = false")
	public static Boolean is_warning(final IScope scope, final IExpression expr) {
		try {
			expr.value(scope);
		} catch (final GamaRuntimeException e) {
			return e.isWarning();
		} catch (final Exception e1) {}
		return false;
	}

	@operator (
			value = "command",
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.COMMUNICATION })
	@doc ("command allows GAMA to issue a system command using the system terminal or shell and to receive a string containing the outcome of the command or script executed. By default, commands are blocking the agent calling them, unless the sequence ' &' is used at the end. In this case, the result of the operator is an empty string. The basic form with only one string in argument uses the directory of the model and does not set any environment variables. Two other forms (with a directory and a map<string, string> of environment variables) are available.")
	@no_test
	public static String console(final IScope scope, final String s) {
		return console(scope, s, scope.getSimulation().getExperiment().getWorkingPath());
	}

	@operator (
			value = "command",
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.COMMUNICATION })
	@doc ("command allows GAMA to issue a system command using the system terminal or shell and to receive a string containing the outcome of the command or script executed. By default, commands are blocking the agent calling them, unless the sequence ' &' is used at the end. In this case, the result of the operator is an empty string. The basic form with only one string in argument uses the directory of the model and does not set any environment variables. Two other forms (with a directory and a map<string, string> of environment variables) are available.")
	@no_test
	public static String console(final IScope scope, final String s, final String directory) {
		return console(scope, s, directory, GamaMapFactory.create());
	}

	@operator (
			value = "command",
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.COMMUNICATION })
	@doc ("command allows GAMA to issue a system command using the system terminal or shell and to receive a string containing the outcome of the command or script executed. By default, commands are blocking the agent calling them, unless the sequence ' &' is used at the end. In this case, the result of the operator is an empty string")
	@no_test
	public static String console(final IScope scope, final String s, final String directory,
			final IMap<String, String> environment) {
		if (s == null || s.isEmpty())
			return "";
		try (TextBuilder sb = TextBuilder.create()) {
			final List<String> commands = new ArrayList<>();
			commands.add(Platform.getOS().equals(Platform.OS_WIN32) ? "cmd.exe" : "/bin/bash");
			commands.add(Platform.getOS().equals(Platform.OS_WIN32) ? "/C" : "-c");
			commands.add(s.trim());
			// commands.addAll(Arrays.asList(s.split(" ")));
			final boolean nonBlocking = commands.get(commands.size() - 1).endsWith("&");
			if (nonBlocking) {
				// commands.(commands.size() - 1);
			}
			final ProcessBuilder b = new ProcessBuilder(commands);
			b.redirectErrorStream(true);
			b.directory(new File(directory));
			b.environment().putAll(environment);
			try {
				final Process p = b.start();
				if (nonBlocking)
					return "";
				final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				final int returnValue = p.waitFor();
				String line = "";
				while ((line = reader.readLine()) != null) {
					sb.append(line + Strings.LN);
				}

				if (returnValue != 0)
					throw GamaRuntimeException.error("Error in console command." + sb.toString(), scope);
			} catch (final IOException | InterruptedException e) {
				throw GamaRuntimeException.error("Error in console command. " + e.getMessage(), scope);
			}
			return sb.toString();
		}

	}

	@operator (
			value = { IKeyword._DOT, IKeyword.OF },
			type = ITypeProvider.TYPE_AT_INDEX + 2,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 2,
			index_type = ITypeProvider.KEY_TYPE_AT_INDEX + 2,
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM, IConcept.ATTRIBUTE })
	@doc (
			value = "It has two different uses: it can be the dot product between 2 matrices or return an evaluation of the expression (right-hand operand) in the scope the given agent.",
			masterDoc = true,
			special_cases = "if the agent is nil or dead, throws an exception",
			usages = @usage (
					value = "if the left operand is an agent, it evaluates of the expression (right-hand operand) in the scope the given agent",
					examples = { @example (
							value = "agent1.location",
							equals = "the location of the agent agent1",
							isExecutable = false),
					// @example (value = "map(nil).keys", raises = "exception", isTestOnly = false)
					}))
	@no_test
	public static Object opGetValue(final IScope scope, final IAgent a, final IExpression s)
			throws GamaRuntimeException {
		if (a == null) {
			if (!scope.interrupted())
				throw GamaRuntimeException
						.warning("Cannot evaluate " + s.serialize(false) + " as the target agent is nil", scope);
			return null;
		}
		if (a.dead()) {
			// scope.getGui().debug("System.opGetValue");
			if (!scope.interrupted())
				// scope.getGui().debug("System.opGetValue error");
				throw GamaRuntimeException
						.warning("Cannot evaluate " + s.serialize(false) + " as the target agent is dead", scope);
			return null;
		}
		return scope.evaluate(s, a).getValue();
	}

	@SuppressWarnings ("unchecked")
	@operator (
			value = "copy",
			type = ITypeProvider.TYPE_AT_INDEX + 1,
			content_type = ITypeProvider.CONTENT_TYPE_AT_INDEX + 1,
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM })
	@doc (
			value = "returns a copy of the operand.")
	@no_test
	public static <T> T opCopy(final IScope scope, final T o) throws GamaRuntimeException {
		if (o instanceof IValue)
			return (T) ((IValue) o).copy(scope);
		return o;
	}

	@operator (
			value = IKeyword.USER_INPUT,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			concept = { IConcept.SYSTEM, IConcept.GUI })
	@doc (
			value = "asks the user for some values (not defined as parameters). Takes a string (optional) and a map as arguments. The string is used to specify the message of the dialog box. The map is to specify the parameters you want the user to change before the simulation starts, with the name of the parameter in string key, and the default value as value.",
			masterDoc = true,
			comment = "This operator takes a map [string::value] as argument, displays a dialog asking the user for these values, and returns the same map with the modified values (if any). "
					+ "The dialog is modal and will interrupt the execution of the simulation until the user has either dismissed or accepted it. It can be used, for instance, in an init section to force the user to input new values instead of relying on the initial values of parameters :",
			examples = {
					@example ("map<string,unknown> values <- user_input([\"Number\" :: 100, \"Location\" :: {10, 10}]);"),
					@example (
							value = "(values at \"Number\") as int",
							equals = "100",
							returnType = "int",
							isTestOnly = true),
					@example (
							value = " (values at \"Location\") as point",
							equals = "{10,10}",
							returnType = "point",
							isTestOnly = true),
					@example (
							value = "create bug number: int(values at \"Number\") with: [location:: (point(values at \"Location\"))];",
							isExecutable = false) })
	public static IMap<String, Object> userInput(final IScope scope, final IExpression map) {
		final IAgent agent = scope.getAgent();
		return userInput(scope, agent.getSpeciesName() + " #" + agent.getIndex() + " request", map);
	}

	@SuppressWarnings ("unchecked")
	@operator (
			value = IKeyword.USER_INPUT,
			category = { IOperatorCategory.SYSTEM, IOperatorCategory.USER_CONTROL },
			concept = {})
	@doc (
			value = "asks the user for some values (not defined as parameters). Takes a string (optional) and a map as arguments. The string is used to specify the message of the dialog box. The map is to specify the parameters you want the user to change before the simulation starts, with the name of the parameter in string key, and the default value as value.",
			examples = {
					@example ("map<string,unknown> values2 <- user_input(\"Enter numer of agents and locations\",[\"Number\" :: 100, \"Location\" :: {10, 10}]);"),
					@example (
							value = "create bug number: int(values2 at \"Number\") with: [location:: (point(values2 at \"Location\"))];",
							isExecutable = false) })
	public static IMap<String, Object> userInput(final IScope scope, final String title, final IExpression expr) {
		Map<String, Object> initialValues = GamaMapFactory.create();
		final Map<String, IType<?>> initialTypes = GamaMapFactory.create();
		if (expr instanceof MapExpression) {
			final MapExpression map = (MapExpression) expr;
			for (final Map.Entry<IExpression, IExpression> entry : map.getElements().entrySet()) {
				final String key = Cast.asString(scope, entry.getKey().value(scope));
				final IExpression val = entry.getValue();
				initialValues.put(key, val.value(scope));
				initialTypes.put(key, val.getGamlType());
			}
		} else {
			initialValues = Cast.asMap(scope, expr.value(scope), false);
			for (final Map.Entry<String, Object> entry : initialValues.entrySet()) {
				initialTypes.put(entry.getKey(), GamaType.of(entry.getValue()));
			}
		}
		if (initialValues.isEmpty())
			return GamaMapFactory.create(Types.STRING, Types.NO_TYPE);
		return GamaMapFactory.createWithoutCasting(Types.STRING, Types.NO_TYPE,
				scope.getGui().openUserInputDialog(scope, title, initialValues, initialTypes));
	}

	@operator (
			value = "eval_gaml",
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM })
	@doc (
			value = "evaluates the given GAML string.",
			examples = { @example (
					value = "eval_gaml(\"2+3\")",
					equals = "5") })
	public static Object opEvalGaml(final IScope scope, final String gaml) {
		final IAgent agent = scope.getAgent();
		final IDescription d = agent.getSpecies().getDescription();
		try {
			final IExpression e = GAML.getExpressionFactory().createExpr(gaml, d);
			// scope.disableErrorReporting();
			return scope.evaluate(e, agent).getValue();
		} catch (final GamaRuntimeException e) {
			scope.getGui().getConsole().informConsole(
					"Error in evaluating Gaml code : '" + gaml + "' in " + scope.getAgent()
							+ java.lang.System.getProperty("line.separator") + "Reason: " + e.getMessage(),
					scope.getRoot());

			return null;
		} finally {
			// scope.enableErrorReporting();
		}

	}

	@operator (
			value = "copy_to_clipboard",
			can_be_const = false,
			category = { IOperatorCategory.SYSTEM },
			concept = { IConcept.SYSTEM })
	@doc (
			examples = @example ("bool copied  <- copy_to_clipboard('text to copy');"),
			value = "Tries to copy the text in parameter to the clipboard and returns whether it has been correctly copied or not (for instance it might be impossible in a headless environment)")
	@no_test ()
	public static Boolean copyToClipboard(final IScope scope, final String text) {
		return scope.getGui().copyToClipboard(text);
	}
	// @operator(value = "eval_java", can_be_const = false)
	// @doc(value = "evaluates the given java code string.", deprecated = "Does
	// not work", see = { "eval_gaml",
	// "evaluate_with" })
	// public static Object opEvalJava(final IScope scope, final String code) {
	// try {
	// final ScriptEvaluator se = new ScriptEvaluator();
	// se.setReturnType(Object.class);
	// se.cook(code);
	// // Evaluate script with actual parameter values.
	// return se.evaluate(new Object[0]);
	//
	// // Version sans arguments pour l'instant.
	// } catch (final Exception e) {
	// scope.getGui().informConsole("Error in evaluating Java code : '" + code +
	// "' in " + scope.getAgentScope() +
	// java.lang.System.getProperty("line.separator") + "Reason: " +
	// e.getMessage());
	// return null;
	// }
	// }

	// private static final String[] gamaDefaultImports = new String[] {};

}