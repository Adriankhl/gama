/*******************************************************************************************************
 *
 * gaml.architecture.weighted_tasks.WeightedTasksArchitecture.java, in plugin gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.architecture.weighted_tasks;

import java.util.ArrayList;
import java.util.List;

import gama.common.interfaces.IStatement;
import gama.processor.annotations.IConcept;
import gama.processor.annotations.GamlAnnotations.doc;
import gama.processor.annotations.GamlAnnotations.skill;
import gama.runtime.exceptions.GamaRuntimeException;
import gama.runtime.scope.IScope;
import gaml.architecture.reflex.ReflexArchitecture;

/**
 * The class WeightedTasksArchitecture. A simple architecture of competing tasks, where one can be active at a time.
 * Weights of the tasks are computed every step and the chosen task is simply the one with the maximal weight.
 * 
 * task t1 weight: a_float { ... } task t2 weight: another_float {...}
 * 
 * @author drogoul
 * @since 21 dec. 2011
 * 
 */
@skill (
		name = WeightedTasksArchitecture.WT,
		concept = { IConcept.ARCHITECTURE, IConcept.BEHAVIOR, IConcept.TASK_BASED },
		doc = @doc ("A control architecture, based on the concept of tasks, where the most 'heavy' one is executed"))
public class WeightedTasksArchitecture extends ReflexArchitecture {

	public static final String WT = "weighted_tasks";
	List<WeightedTaskStatement> tasks = new ArrayList<>();

	@Override
	protected void clearBehaviors() {
		super.clearBehaviors();
		tasks.clear();
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		// We let inits, reflexes run
		super.executeOn(scope);
		final WeightedTaskStatement active = chooseTask(scope);
		if (active != null) { return scope.execute(active).getValue(); }
		return null;
	}

	protected WeightedTaskStatement chooseTask(final IScope scope) throws GamaRuntimeException {
		Double max = Double.MIN_VALUE;
		WeightedTaskStatement active = null;
		for (final WeightedTaskStatement c : tasks) {
			final Double weight = c.computeWeight(scope);
			if (weight > max) {
				active = c;
				max = weight;
			}
		}
		return active;
	}

	@Override
	public void addBehavior(final IStatement c) {
		if (c instanceof WeightedTaskStatement) {
			tasks.add((WeightedTaskStatement) c);
		} else {
			super.addBehavior(c);
		}
	}

}
