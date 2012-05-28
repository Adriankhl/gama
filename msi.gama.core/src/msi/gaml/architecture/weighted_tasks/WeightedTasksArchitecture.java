/**
 * Created by drogoul, 21 d�c. 2011
 * 
 */
package msi.gaml.architecture.weighted_tasks;

import java.util.*;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.architecture.reflex.ReflexArchitecture;
import msi.gaml.statements.IStatement;
import msi.gama.precompiler.GamlAnnotations.skill;

/**
 * The class WeightedTasksArchitecture. A simple architecture of competing tasks, where one can be
 * active at a time. Weights of the tasks are computed every step and the chosen task is simply the
 * one with the maximal weight.
 * 
 * task t1 weight: a_float { ... }
 * task t2 weight: another_float {...}
 * 
 * @author drogoul
 * @since 21 d�c. 2011
 * 
 */
@skill(WeightedTasksArchitecture.WT)
public class WeightedTasksArchitecture extends ReflexArchitecture {

	public static final String WT = "weighted_tasks";
	List<WeightedTaskStatement> tasks = new ArrayList();

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		// We let inits, reflexes run
		super.executeOn(scope);
		WeightedTaskStatement active = chooseTask(scope);
		if ( active != null ) { return active.executeOn(scope); }
		return null;
	}

	protected WeightedTaskStatement chooseTask(final IScope scope) throws GamaRuntimeException {
		Double max = Double.MIN_VALUE;
		WeightedTaskStatement active = null;
		for ( WeightedTaskStatement c : tasks ) {
			Double weight = c.computeWeight(scope);
			if ( weight > max ) {
				active = c;
				max = weight;
			}
		}
		return active;
	}

	@Override
	public void addBehavior(final IStatement c) {
		if ( c instanceof WeightedTaskStatement ) {
			tasks.add((WeightedTaskStatement) c);
		} else {
			super.addBehavior(c);
		}
	}

}
