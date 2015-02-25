/**
 * Created by drogoul, 15 avr. 2014
 * 
 */
package msi.gaml.factories;

import static msi.gama.common.interfaces.IKeyword.*;
import java.util.*;
import java.util.Map.Entry;
import msi.gama.common.interfaces.*;
import msi.gama.util.*;
import msi.gaml.compilation.*;
import msi.gaml.descriptions.*;
import msi.gaml.expressions.ConstantExpression;
import msi.gaml.statements.Facets;
import msi.gaml.types.*;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;

/**
 * Class ModelAssembler.
 * 
 * @author drogoul
 * @since 15 avr. 2014
 * 
 */
public class ModelAssembler {

	/**
	 *
	 */
	public ModelAssembler() {}

	public ModelDescription assemble(final String projectPath, final String modelPath,
		final List<ISyntacticElement> models, final ErrorCollector collector, final boolean document,
		final Map<String, ModelDescription> mm, final Collection<URI> imports) {
		final Map<String, ISyntacticElement> speciesNodes = new TOrderedHashMap();
		final Map<String, Map<String, ISyntacticElement>> experimentNodes = new TOrderedHashMap();
		final ISyntacticElement globalNodes = SyntacticFactory.create(GLOBAL, (EObject) null, true);
		final ISyntacticElement source = models.get(0);
		final Facets globalFacets = new Facets();
		final List<ISyntacticElement> otherNodes = new ArrayList();

		ISyntacticElement lastGlobalNode = source;
		for ( int n = models.size(), i = n - 1; i >= 0; i-- ) {
			final ISyntacticElement currentModel = models.get(i);
			if ( currentModel != null ) {
				for ( final ISyntacticElement se : currentModel.getChildren() ) {
					if ( se.isGlobal() ) {
						// We build the facets resulting from the different arguments
						globalFacets.putAll(se.copyFacets(null));
						for ( final ISyntacticElement ge : se.getChildren() ) {
							if ( ge.isSpecies() ) {
								addSpeciesNode(ge, speciesNodes, collector);
							} else if ( ge.isExperiment() ) {
								addExperimentNode(ge, currentModel.getName(), experimentNodes, collector);
							} else {
								lastGlobalNode = ge;
								globalNodes.addChild(ge);
							}
						}

					} else if ( se.isSpecies() ) {
						addSpeciesNode(se, speciesNodes, collector);
					} else if ( se.isExperiment() ) {
						addExperimentNode(se, currentModel.getName(), experimentNodes, collector);
					} else {
						if ( !ENVIRONMENT.equals(se.getKeyword()) ) {
							collector.add(new GamlCompilationError("This " + se.getKeyword() +
								" should be declared either in a species or in the global section", null, se
								.getElement(), true, false));
						}
						otherNodes.add(se);
					}
				}
			}
		}

		final String modelName = buildModelName(source.getName());
		globalFacets.putAsLabel(NAME, modelName);

		// We first sort the species so that grids are always the last ones (see SignalVariable)
		for ( final ISyntacticElement speciesNode : new ArrayList<ISyntacticElement>(speciesNodes.values()) ) {
			if ( speciesNode.getKeyword().equals(GRID) ) {
				speciesNodes.remove(speciesNode.getName());
				speciesNodes.put(speciesNode.getName(), speciesNode);
			}
		}
		List<String> importStrings = Collections.EMPTY_LIST;
		if ( !imports.isEmpty() ) {
			importStrings = new ArrayList();
			for ( URI uri : imports ) {
				importStrings.add(uri.toFileString());
			}
		}
		final ModelDescription model = new ModelDescription(modelName, null, projectPath, modelPath, /* lastGlobalNode.getElement() */
		source.getElement(), null, ModelDescription.ROOT, globalFacets, collector, importStrings);

		// model.setGlobal(true);
		model.addSpeciesType(model);
		model.isDocumenting(document);

		// hqnghi add micro-models
		if ( mm != null ) {
			model.setMicroModels(mm);
			model.addChildren(new ArrayList(mm.values()));
		}
		// end-hqnghi
		// recursively add user-defined species to world and down on to the hierarchy
		for ( final ISyntacticElement speciesNode : speciesNodes.values() ) {
			addMicroSpecies(model, speciesNode);
		}
		for ( String s : experimentNodes.keySet() ) {
			for ( final ISyntacticElement experimentNode : experimentNodes.get(s).values() ) {
				addExperiment(s, model, experimentNode);
			}
		}

		// Parent the species and the experiments of the model (all are now known).
		for ( final ISyntacticElement speciesNode : speciesNodes.values() ) {
			parentSpecies(model, speciesNode, model);
		}
		for ( String s : experimentNodes.keySet() ) {
			for ( final ISyntacticElement experimentNode : experimentNodes.get(s).values() ) {
				parentExperiment(model, experimentNode, model);
			}
		}
		// Initialize the hierarchy of types
		model.buildTypes();
		// hqnghi build micro-models as types
		for ( Entry<String, ModelDescription> entry : mm.entrySet() ) {
			model.getTypesManager().alias(entry.getValue().getName(), entry.getKey());
		}
		// end-hqnghi

		// Make species and experiments recursively create their attributes, actions....
		complementSpecies(model, globalNodes);
		for ( final ISyntacticElement speciesNode : speciesNodes.values() ) {
			complementSpecies(model.getMicroSpecies(speciesNode.getName()), speciesNode);
		}
		for ( String s : experimentNodes.keySet() ) {
			for ( final ISyntacticElement experimentNode : experimentNodes.get(s).values() ) {
				complementSpecies(model.getExperiment(experimentNode.getName()), experimentNode);
			}
		}

		// Complement recursively the different species (incl. the world). The recursion is hierarchical
		final TypeTree<SpeciesDescription> hierarchy = model.getTypesManager().getSpeciesHierarchy();
		// GuiUtils.debug("Hierarchy: " + hierarchy.toStringWithDepth());
		final List<TypeNode<SpeciesDescription>> list = hierarchy.build(TypeTree.Order.PRE_ORDER);

		model.inheritFromParent();
		// GuiUtils.debug("ModelFactory.assemble building inheritance for " + list);
		for ( final TypeNode<SpeciesDescription> node : list ) {

			final SpeciesDescription sd = node.getData();
			if ( !sd.isBuiltIn() ) {
				// GuiUtils.debug("Copying Java additions and parent additions to " + sd.getName());
				sd.inheritFromParent();
				if ( sd.isExperiment() ) {
					sd.finalizeDescription();
				}
			}
		}

		model.finalizeDescription();

		// We now can safely put the model inside "experiment"
		model.setEnclosingDescription(ModelDescription.ROOT.getSpeciesDescription(EXPERIMENT));

		// Parse the other definitions (output, environment, ...)
		boolean environmentDefined = false;
		for ( final ISyntacticElement e : otherNodes ) {
			// COMPATIBILITY to remove the environment and put its definition in the world
			if ( ENVIRONMENT.equals(e.getKeyword()) ) {
				environmentDefined = translateEnvironment(model, e);
			} else {
				//
				final IDescription dd = DescriptionFactory.create(e, model, null);
				if ( dd != null ) {
					model.addChild(dd);
				}
			}
		}
		if ( !environmentDefined ) {
			VariableDescription vd = model.getVariable(SHAPE);
			if ( !vd.getFacets().containsKey(INIT) ) {
				final Facets f = new Facets(NAME, SHAPE);
				// TODO Catch the right EObject (instead of null)
				f.put(INIT,
					GAML.getExpressionFactory().createOperator("envelope", model, null, new ConstantExpression(100)));
				final ISyntacticElement shape = SyntacticFactory.create(IKeyword.GEOMETRY, f, false);
				vd = (VariableDescription) DescriptionFactory.create(shape, model, null);
				model.addChild(vd);
				model.resortVarName(vd);
			}
		}

		if ( document ) {
			DescriptionFactory.document(model);
		}
		return model;

	}

	void addExperiment(final String origin, final ModelDescription model, final ISyntacticElement experiment) {
		// Create the experiment description
		IDescription desc = DescriptionFactory.create(experiment, model, ChildrenProvider.NONE);
		// final ExperimentDescription eDesc = (ExperimentDescription) desc;
		((SymbolDescription) desc).resetOriginName();
		desc.setOriginName(buildModelName(origin));
		model.addChild(desc);
	}

	void addExperimentNode(final ISyntacticElement element, final String modelName,
		final Map<String, Map<String, ISyntacticElement>> experimentNodes, final ErrorCollector collector) {
		// First we verify that this experiment has not been declared previously
		String experimentName = element.getName();
		for ( String otherModel : experimentNodes.keySet() ) {
			if ( !otherModel.equals(modelName) ) {
				Map<String, ISyntacticElement> otherExperiments = experimentNodes.get(otherModel);
				if ( otherExperiments.containsKey(experimentName) ) {
					collector.add(new GamlCompilationError("Experiment " + experimentName +
						" supersedes the one declared in " + otherModel, IGamlIssue.DUPLICATE_DEFINITION, element
						.getElement(), false, true));
					// We remove the old one
					otherExperiments.remove(experimentName);
				}
			}
		}

		if ( !experimentNodes.containsKey(modelName) ) {
			experimentNodes.put(modelName, new TOrderedHashMap());
		}
		Map<String, ISyntacticElement> nodes = experimentNodes.get(modelName);
		if ( nodes.containsKey(experimentName) ) {
			collector.add(new GamlCompilationError("Experiment " + element.getName() + " is declared twice",
				IGamlIssue.DUPLICATE_DEFINITION, element.getElement(), false, false));
		}
		nodes.put(experimentName, element);
	}

	void addMicroSpecies(final SpeciesDescription macro, final ISyntacticElement micro) {
		// Create the species description without any children
		final SpeciesDescription mDesc =
			(SpeciesDescription) DescriptionFactory.create(micro, macro, ChildrenProvider.NONE);
		// Add it to its macro-species
		macro.addChild(mDesc);
		// Recursively create each micro-species of the newly added micro-species
		for ( final ISyntacticElement speciesNode : micro.getChildren() ) {
			if ( speciesNode.isSpecies() || speciesNode.isExperiment() ) {
				addMicroSpecies(mDesc, speciesNode);
			}
		}
	}

	void addSpeciesNode(final ISyntacticElement element, final Map<String, ISyntacticElement> speciesNodes,
		final ErrorCollector collector) {
		String name = element.getName();
		if ( speciesNodes.containsKey(name) ) {
			collector.add(new GamlCompilationError("Species " + name + " is declared twice",
				IGamlIssue.DUPLICATE_DEFINITION, element.getElement(), false, false));
			collector.add(new GamlCompilationError("Species " + name + " is declared twice",
				IGamlIssue.DUPLICATE_DEFINITION, speciesNodes.get(name).getElement(), false, false));
		}
		speciesNodes.put(element.getName(), element);
	}

	/**
	 * Recursively complements a species and its micro-species.
	 * Add variables, behaviors (actions, reflex, task, states, ...), aspects to species.
	 * 
	 * @param macro the macro-species
	 * @param micro the structure of micro-species
	 */
	void complementSpecies(final SpeciesDescription species, final ISyntacticElement node) {
		if ( species == null ) { return; }
		species.copyJavaAdditions();
		// GuiUtils.debug("++++++ Building variables & behaviors of " + species.getName());
		final List<ISyntacticElement> subspecies = new ArrayList();
		for ( final ISyntacticElement child : node.getChildren() ) {
			if ( !child.isExperiment() && !child.isSpecies() ) {
				final IDescription childDesc = DescriptionFactory.create(child, species, null);
				if ( childDesc != null ) {
					species.addChild(childDesc);
				}
			} else {
				subspecies.add(child);
			}
		}
		// recursively complement micro-species
		for ( final ISyntacticElement e : subspecies ) {
			final SpeciesDescription sd = species.getMicroSpecies(e.getName());
			if ( sd != null ) {
				complementSpecies(sd, e);
			}
		}

	}

	void parentExperiment(final ModelDescription macro, final ISyntacticElement micro, final ModelDescription model) {
		// Gather the previously created species
		final SpeciesDescription mDesc = macro.getExperiment(micro.getName());
		if ( mDesc == null ) { return; }
		final String p = mDesc.getFacets().getLabel(IKeyword.PARENT);
		// If no parent is defined, we assume it is "experiment"
		SpeciesDescription parent = model.getExperiment(p);
		if ( parent == null ) {
			parent = (SpeciesDescription) ModelDescription.ROOT.getTypesManager().getSpecies(IKeyword.EXPERIMENT);
		}
		mDesc.setParent(parent);
		// for ( SyntacticElement speciesNode : micro.getSpeciesChildren() ) {
		// parentSpecies(mDesc, speciesNode, model);
		// }
	}

	void parentSpecies(final SpeciesDescription macro, final ISyntacticElement micro, final ModelDescription model) {
		// Gather the previously created species
		final SpeciesDescription mDesc = macro.getMicroSpecies(micro.getName());
		if ( mDesc == null || mDesc.isExperiment() ) { return; }
		String p = mDesc.getFacets().getLabel(IKeyword.PARENT);
		// If no parent is defined, we assume it is "agent"
		if ( p == null ) {
			p = IKeyword.AGENT;
		}
		final SpeciesDescription parent = model.getSpeciesDescription(p);
		mDesc.setParent(parent);
		for ( final ISyntacticElement speciesNode : micro.getChildren() ) {
			if ( speciesNode.isSpecies() || speciesNode.isExperiment() ) {
				parentSpecies(mDesc, speciesNode, model);
			}
		}
	}

	boolean translateEnvironment(final SpeciesDescription world, final ISyntacticElement e) {
		final boolean environmentDefined = true;
		final ISyntacticElement shape = SyntacticFactory.create(GEOMETRY, new Facets(NAME, SHAPE), false);
		IExpressionDescription bounds = e.getExpressionAt(BOUNDS);
		if ( bounds == null ) {
			final IExpressionDescription width = e.getExpressionAt(WIDTH);
			final IExpressionDescription height = e.getExpressionAt(HEIGHT);
			if ( width != null && height != null ) {
				bounds = new OperatorExpressionDescription(POINT, width, height);
			} else {
				bounds = ConstantExpressionDescription.create(100);
			}
		}
		bounds = new OperatorExpressionDescription("envelope", bounds);
		shape.setFacet(INIT, bounds);
		final IExpressionDescription depends = e.getExpressionAt(DEPENDS_ON);
		if ( depends != null ) {
			shape.setFacet(DEPENDS_ON, depends);
		}
		final VariableDescription vd = (VariableDescription) DescriptionFactory.create(shape, world, null);
		world.addChild(vd);
		world.resortVarName(vd);
		final IExpressionDescription ed = e.getExpressionAt(TORUS);
		// TODO Is the call to compilation correct at that point ?
		if ( ed != null ) {
			world.getFacets().put(TORUS, ed.compile(world));
		}
		return environmentDefined;
	}

	protected String buildModelName(final String source) {
		final String modelName = source.replace(' ', '_') + ModelDescription.MODEL_SUFFIX;
		return modelName;
	}

}
