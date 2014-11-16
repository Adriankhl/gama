/*********************************************************************************************
 * 
 * 
 * 'AbstractSpecies.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.species;

import gnu.trove.map.hash.THashMap;
import java.util.*;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gama.util.graph.AbstractGraphNodeAgent;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.architecture.IArchitecture;
import msi.gaml.compilation.*;
import msi.gaml.descriptions.*;
import msi.gaml.statements.*;
import msi.gaml.statements.IStatement.WithArgs;
import msi.gaml.types.IType;
import msi.gaml.variables.IVariable;

/**
 * Written by drogoul Modified on 29 d�c. 2010
 * 
 * @todo Description
 * 
 */
public abstract class AbstractSpecies extends Symbol implements ISpecies {

	protected final boolean isGrid, isGraph;
	protected final Map<String, ISpecies> microSpecies = new THashMap<String, ISpecies>();
	private final Map<String, IVariable> variables = new TOrderedHashMap<String, IVariable>();
	private final Map<String, AspectStatement> aspects = new TOrderedHashMap<String, AspectStatement>();
	private final Map<String, ActionStatement> actions = new TOrderedHashMap<String, ActionStatement>();
	private final Map<String, UserCommandStatement> userCommands = new TOrderedHashMap();
	private final IList<IStatement> behaviors = new GamaList<IStatement>();
	protected ISpecies macroSpecies, parentSpecies;
	private boolean isInitOverriden, isStepOverriden;

	public AbstractSpecies(final IDescription description) {
		super(description);
		setName(description.getName());
		isGrid = description.getFacets().equals(IKeyword.KEYWORD, IKeyword.GRID);
		isGraph = AbstractGraphNodeAgent.class.isAssignableFrom(((TypeDescription) description).getJavaBase());
	}

	@Override
	public java.lang.Iterable<? extends IAgent> iterable(final IScope scope) {
		return getPopulation(scope).iterable(scope);
	}

	@Override
	public IPopulation getPopulation(final IScope scope) {
		final IAgent a = scope.getAgentScope();
		IPopulation result = null;
		if ( a != null ) {
			// AD 19/09/13 Patch to allow experiments to gain access to the simulation populations
			result = a.getPopulationFor(this);
		}
		return result;
	}

	@Override
	public IList listValue(final IScope scope, final IType contentsType) throws GamaRuntimeException {
		// return getPopulation(scope).listValue(scope, contentsType);
		// hqnghi 16/04/14
		IPopulation pop = getPopulation(scope);
		if ( pop == null ) {
			pop = scope.getSimulationScope().getPopulationFor(contentsType.getName());
		}
		return pop.listValue(scope, contentsType);
		// end-hqnghi
	}

	@Override
	public String stringValue(final IScope scope) {
		return name;
	}

	@Override
	public GamaMap mapValue(final IScope scope, final IType keyType, final IType contentsType)
		throws GamaRuntimeException {
		final IList<IAgent> agents = listValue(scope, contentsType);
		// Default behavior : Returns a map containing the names of agents as keys and the agents themselves as values
		final GamaMap result = new GamaMap();
		for ( final IAgent agent : agents.iterable(scope) ) {
			result.put(agent.getName(), agent);
		}
		return result;
	}

	@Override
	public boolean isGrid() {
		return isGrid;
	}

	@Override
	public boolean isGraph() {
		return isGraph;
	}

	// @Override
	// public String toGaml() {
	// return name;
	// }

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public ISpecies copy(final IScope scope) {
		return this;
		// Species are immutable
	}

	@Override
	public IList<ISpecies> getMicroSpecies() {
		final IList<ISpecies> retVal = new GamaList<ISpecies>();
		retVal.addAll(microSpecies.values());
		final ISpecies parentSpecies = this.getParentSpecies();
		if ( parentSpecies != null ) {
			retVal.addAll(parentSpecies.getMicroSpecies());
		}
		return retVal;
	}

	@Override
	public Collection<String> getMicroSpeciesNames() {
		return microSpecies.keySet();
	}

	/**
	 * Returns a micro-species with the specified name or null otherwise.
	 * 
	 * @param microSpeciesName
	 * @return a species or null
	 */
	@Override
	public ISpecies getMicroSpecies(final String microSpeciesName) {
		final ISpecies retVal = microSpecies.get(microSpeciesName);
		if ( retVal != null ) { return retVal; }
		final ISpecies parentSpecies = this.getParentSpecies();
		if ( parentSpecies != null ) { return parentSpecies.getMicroSpecies(microSpeciesName); }
		return null;
	}

	@Override
	public boolean containMicroSpecies(final ISpecies species) {
		final ISpecies parentSpecies = this.getParentSpecies();
		return microSpecies.values().contains(species) ||
			(parentSpecies != null ? parentSpecies.containMicroSpecies(species) : false);
	}

	@Override
	public boolean hasMicroSpecies() {
		final ISpecies parentSpecies = this.getParentSpecies();
		return !microSpecies.isEmpty() || (parentSpecies != null ? parentSpecies.hasMicroSpecies() : false);
	}

	@Override
	public SpeciesDescription getDescription() {
		return (SpeciesDescription) description;
	}

	@Override
	public boolean isPeer(final ISpecies other) {
		return other != null && other.getMacroSpecies().equals(this.getMacroSpecies());
	}

	@Override
	public List<ISpecies> getSelfWithParents() {
		final List<ISpecies> retVal = new GamaList<ISpecies>();
		retVal.add(this);
		ISpecies currentParent = this.getParentSpecies();
		while (currentParent != null) {
			retVal.add(currentParent);
			currentParent = currentParent.getParentSpecies();
		}

		return retVal;
	}

	@Override
	public ISpecies getParentSpecies() {
		if ( parentSpecies == null ) {
			final TypeDescription parentSpecDesc = getDescription().getParent();
			// Takes care of invalid species (see Issue 711)
			if ( parentSpecDesc == null || parentSpecDesc == getDescription() ) { return null; }
			ISpecies currentMacroSpec = this.getMacroSpecies();
			while (currentMacroSpec != null && parentSpecies == null) {
				parentSpecies = currentMacroSpec.getMicroSpecies(parentSpecDesc.getName());
				currentMacroSpec = currentMacroSpec.getMacroSpecies();
			}
		}
		return parentSpecies;
	}

	@Override
	public boolean extendsSpecies(final ISpecies s) {
		ISpecies parent = getParentSpecies();
		if ( parent == null ) { return false; }
		if ( parent == s ) { return true; }
		return parent.extendsSpecies(s);
	}

	@Override
	public String getParentName() {
		return getDescription().getParentName();
	}

	@Override
	public boolean equals(final Object other) {
		return this == other;
	}

	@Override
	public IArchitecture getArchitecture() {
		return getDescription().getControl();
	}

	@Override
	public IVariable getVar(final String n) {
		return variables.get(n);
	}

	@Override
	public boolean hasVar(final String name) {
		return variables.containsKey(name);
	}

	@Override
	public IList<String> getVarNames() {
		return getDescription().getVarNames();
	}

	@Override
	public Collection<IVariable> getVars() {
		return variables.values();
	}

	@Override
	public Collection<UserCommandStatement> getUserCommands() {
		return userCommands.values();
	}

	@Override
	public WithArgs getAction(final String name) {
		return actions.get(name);
	}

	@Override
	public IList<ActionStatement> getActions() {
		return new GamaList<ActionStatement>(actions.values());
	}

	@Override
	public boolean hasAspect(final String n) {
		return aspects.containsKey(n);
	}

	@Override
	public IExecutable getAspect(final String n) {
		return aspects.get(n);
	}

	@Override
	public IList<IExecutable> getAspects() {
		return new GamaList<IExecutable>(aspects.values());
	}

	@Override
	public IList<String> getAspectNames() {
		return new GamaList<String>(aspects.keySet());
	}

	@Override
	public IList<IStatement> getBehaviors() {
		return behaviors;
	}

	@Override
	public void setChildren(final List<? extends ISymbol> children) {
		// First we verify the control architecture
		final IArchitecture control = getArchitecture();
		if ( control == null ) { throw GamaRuntimeException.error("The control of this species cannot be computed"); }
		// Then we classify the children in their categories
		for ( final ISymbol s : children ) {
			if ( s instanceof ISpecies ) {
				final ISpecies oneMicroSpecies = (ISpecies) s;
				oneMicroSpecies.setMacroSpecies(this);
				microSpecies.put(oneMicroSpecies.getName(), oneMicroSpecies);
			} else if ( s instanceof IVariable ) {
				variables.put(s.getName(), (IVariable) s);
			} else if ( s instanceof AspectStatement ) {
				aspects.put(s.getName(), (AspectStatement) s);
			} else if ( s instanceof ActionStatement ) {
				if ( !s.getDescription().isBuiltIn() ) {
					String name = s.getName();
					if ( name.equals(initActionName) ) {
						isInitOverriden = true;
					} else if ( name.equals(stepActionName) ) {
						isStepOverriden = true;
					}
				}
				actions.put(s.getName(), (ActionStatement) s);
			} else if ( s instanceof UserCommandStatement ) {
				userCommands.put(s.getName(), (UserCommandStatement) s);
			} else if ( s instanceof IStatement ) {
				behaviors.add((IStatement) s); // reflexes, states or tasks
			}
		}
		control.setChildren(behaviors);
		control.verifyBehaviors(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		for ( final IVariable v : variables.values() ) {
			v.dispose();
		}
		variables.clear();
		for ( final AspectStatement ac : aspects.values() ) {
			ac.dispose();
		}
		aspects.clear();
		for ( final ActionStatement ac : actions.values() ) {
			ac.dispose();
		}
		actions.clear();
		for ( final IStatement c : behaviors ) {
			c.dispose();
		}
		behaviors.clear();
		macroSpecies = null;
		parentSpecies = null;
		// TODO dispose micro_species first???
		microSpecies.clear();
	}

	// TODO review this
	// this is the "original" macro-species???
	@Override
	public ISpecies getMacroSpecies() {
		return macroSpecies;
	}

	@Override
	public void setMacroSpecies(final ISpecies macroSpecies) {
		this.macroSpecies = macroSpecies;
	}

	/*
	 * Equation (Huynh Quang Nghi)
	 */

	@Override
	public <T extends IStatement> T getStatement(final Class<T> clazz, final String valueOfFacetName) {
		for ( final IStatement s : behaviors ) {
			final boolean instance = clazz.isAssignableFrom(s.getClass());
			if ( instance ) {
				if ( valueOfFacetName == null ) { return (T) s; }
				final String t = s.getFacet(IKeyword.NAME).literalValue();
				if ( t != null ) {
					final boolean named = t.equals(valueOfFacetName);
					if ( named ) { return (T) s; }
				}
			}
		}
		return null;
	}

	/*
	 * end-of Equation
	 */

	@Override
	public Boolean implementsSkill(final String skill) {
		return getDescription().implementsSkill(skill);
	}

	/**
	 * Method isInitOverriden()
	 * @see msi.gaml.species.ISpecies#isInitOverriden()
	 */
	@Override
	public boolean isInitOverriden() {
		return isInitOverriden;
	}

	/**
	 * Method isStepOverriden()
	 * @see msi.gaml.species.ISpecies#isStepOverriden()
	 */
	@Override
	public boolean isStepOverriden() {
		return isStepOverriden;
	}

	@Override
	public IAgent get(final IScope scope, final Integer index) throws GamaRuntimeException {
		return getPopulation(scope).get(scope, index);
	}

	@Override
	public boolean contains(final IScope scope, final Object o) throws GamaRuntimeException {
		return getPopulation(scope).contains(scope, o);
	}

	@Override
	public IAgent firstValue(final IScope scope) throws GamaRuntimeException {
		return getPopulation(scope).firstValue(scope);
	}

	@Override
	public IAgent lastValue(final IScope scope) throws GamaRuntimeException {
		return getPopulation(scope).lastValue(scope);
	}

	@Override
	public int length(final IScope scope) {
		return getPopulation(scope).length(scope);
	}

	@Override
	public boolean isEmpty(final IScope scope) {
		return getPopulation(scope).isEmpty(scope);
	}

	@Override
	public IContainer<Integer, IAgent> reverse(final IScope scope) throws GamaRuntimeException {
		return getPopulation(scope).reverse(scope);
	}

	@Override
	public IAgent anyValue(final IScope scope) {
		return getPopulation(scope).anyValue(scope);
	}

	//
	// @Override
	// public boolean checkBounds(final Integer index, final boolean forAdding) {
	// return false;
	// }

	// @Override
	// public void add(final IScope scope, final Integer index, final Object value, final Object param, final boolean
	// all,
	// final boolean add) throws GamaRuntimeException {
	// // NOT ALLOWED
	// }
	//
	// @Override
	// public void remove(final IScope scope, final Object index, final Object value, final boolean all)
	// throws GamaRuntimeException {
	// // NOT ALLOWED
	// }

	@Override
	public IMatrix matrixValue(final IScope scope, final IType contentsType) throws GamaRuntimeException {
		return getPopulation(scope).matrixValue(scope, contentsType);
	}

	@Override
	public IMatrix matrixValue(final IScope scope, final IType contentsType, final ILocation preferredSize)
		throws GamaRuntimeException {
		return getPopulation(scope).matrixValue(scope, contentsType, preferredSize);
	}

	@Override
	public IAgent getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		return getPopulation(scope).getFromIndicesList(scope, indices);
	}

	@Override
	public boolean isMirror() {
		return getDescription().isMirror();
	}

	@Override
	public Collection<? extends IPopulation> getPopulations(final IScope scope) {
		return Collections.singleton(getPopulation(scope));
	}

	/**
	 * Method add()
	 * @see msi.gama.util.IContainer#add(msi.gama.runtime.IScope, java.lang.Object)
	 */
	// @Override
	// public void add(final IScope scope, final IAgent value) {}

	/**
	 * Method add()
	 * @see msi.gama.util.IContainer#add(msi.gama.runtime.IScope, java.lang.Object, java.lang.Object)
	 */
	// @Override
	// public void add(final IScope scope, final Integer index, final IAgent value) {}

	/**
	 * Method put()
	 * @see msi.gama.util.IContainer#put(msi.gama.runtime.IScope, java.lang.Object, java.lang.Object)
	 */
	// @Override
	// public void put(final IScope scope, final Integer index, final IAgent value) {}

	/**
	 * Method addAll()
	 * @see msi.gama.util.IContainer#addAll(msi.gama.runtime.IScope, msi.gama.util.IContainer)
	 */
	// @Override
	// public void addAll(final IScope scope, final IContainer values) {}

	/**
	 * Method setAll()
	 * @see msi.gama.util.IContainer#setAll(msi.gama.runtime.IScope, java.lang.Object)
	 */
	// @Override
	// public void setAll(final IScope scope, final IAgent value) {}

	/**
	 * Method remove()
	 * @see msi.gama.util.IContainer#remove(msi.gama.runtime.IScope, java.lang.Object)
	 */
	// @Override
	// public void remove(final IScope scope, final Object value) {}

	/**
	 * Method removeAt()
	 * @see msi.gama.util.IContainer#removeAt(msi.gama.runtime.IScope, java.lang.Object)
	 */
	// @Override
	// public void removeAt(final IScope scope, final Integer index) {}

	/**
	 * Method removeAll()
	 * @see msi.gama.util.IContainer#removeAll(msi.gama.runtime.IScope, msi.gama.util.IContainer)
	 */
	// @Override
	// public void removeAll(final IScope scope, final IContainer values) {}

	/**
	 * Method removeAll()
	 * @see msi.gama.util.IContainer#removeAll(msi.gama.runtime.IScope, java.lang.Object)
	 */
	// @Override
	// public void removeAll(final IScope scope, final Object value) {}

}