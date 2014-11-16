/*********************************************************************************************
 * 
 * 
 * 'GamlResource.java', in plugin 'msi.gama.lang.gaml', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.lang.gaml.resource;

import static msi.gaml.factories.DescriptionFactory.getModelFactory;
import java.net.URLDecoder;
import java.util.*;
import msi.gama.common.interfaces.IGamlIssue;
import msi.gama.kernel.model.IModel;
import msi.gama.lang.gaml.gaml.*;
import msi.gama.lang.gaml.gaml.impl.ImportImpl;
import msi.gama.lang.gaml.parsing.*;
import msi.gama.lang.gaml.parsing.GamlSyntacticParser.GamlParseResult;
import msi.gama.lang.gaml.validation.*;
import msi.gama.lang.gaml.validation.IGamlBuilderListener.IGamlBuilderListener2;
import msi.gama.util.*;
import msi.gaml.compilation.*;
import msi.gaml.descriptions.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
import com.google.common.collect.ImmutableList;

/*
 * 
 * The class GamlResource.
 * 
 * @author drogoul
 * 
 * @since 24 avr. 2012
 */
public class GamlResource extends LazyLinkingResource {

	private IGamlBuilderListener listener;
	private volatile ErrorCollector collector;
	private volatile boolean isValidating;
	private volatile boolean isEdited;

	public ErrorCollector getErrorCollector() {
		if ( collector == null ) {
			collector = new ErrorCollector(this);
		}
		return collector;
	}

	@Override
	public String getEncoding() {
		return "UTF-8";
	}

	public void resetErrorCollector() {
		if ( collector == null ) {
			getErrorCollector();
		} else {
			collector.clear();
		}
	}

	@Override
	public String toString() {
		return "GAML resource" + "[" + getURI() + "]";
	}

	@Override
	protected void addSyntaxErrors() {
		super.addSyntaxErrors();
		GamlParseResult r = getParseResult();
		if ( r != null ) {
			getWarnings().addAll(r.getWarnings());
		}
	}

	@Override
	public GamlParseResult getParseResult() {
		GamlParseResult r = (GamlParseResult) super.getParseResult();
		r.fixURIsWith(this);
		return r;
	}

	public void updateWith(final ModelDescription model) {
		if ( listener != null ) {
			if ( listener instanceof IGamlBuilderListener2 ) {
				((IGamlBuilderListener2) listener).validationEnded(
					model == null ? Collections.EMPTY_SET : model.getExperiments(), collector);
			} else {
				listener.validationEnded(model == null ? Collections.EMPTY_SET : model.getExperimentNames(), collector);
			}
		}
	}

	public void setListener(final IGamlBuilderListener listener) {
		this.listener = listener;
	}

	public void removeListener() {
		listener = null;
	}

	public IGamlBuilderListener getListener() {
		return listener;
	}

	public ISyntacticElement getSyntacticContents() {
		GamlParseResult parseResult = getParseResult();
		if ( parseResult == null ) { // Should not happen, but in case...
			Set<org.eclipse.xtext.diagnostics.Diagnostic> errors = new LinkedHashSet();
			ISyntacticElement result = GamlCompatibilityConverter.buildSyntacticContents(getContents().get(0), errors);
			getWarnings().addAll(errors);
			return result;
		}
		return parseResult.getSyntacticContents();
	}

	// AD 8/4/14 The resource itself is now responsible for returning the whole syntactic contents of the set of files
	// that constitute the model

	// private Iterable<ISyntacticElement> getAllSyntacticContents(final Set<GamlResource> totalResources) {
	// return Iterables.transform(totalResources, new Function<GamlResource, ISyntacticElement>() {
	//
	// @Override
	// public ISyntacticElement apply(final GamlResource r) {
	// return r.getSyntacticContents();
	// }
	//
	// });
	// }

	private ModelDescription buildModelDescription(final Map<GamlResource, String> resources) {

		// AD -> Nghi: microModels to use
		final Map<ISyntacticElement, String> microModels = new GamaMap();
		final List<ISyntacticElement> models = new ArrayList();
		for ( Map.Entry<GamlResource, String> entry : resources.entrySet() ) {
			if ( entry.getValue() == null ) {
				models.add(entry.getKey().getSyntacticContents());
			} else {
				microModels.put(entry.getKey().getSyntacticContents(), entry.getValue());
			}
		}
		// final Iterable<ISyntacticElement> models = getAllSyntacticContents(resources);
		GAML.getExpressionFactory().resetParser();
		IPath path = getPath();
		String modelPath, projectPath;
		if ( getURI().isFile() ) {
			modelPath = path.toOSString();
			projectPath = modelPath;
		} else {
			final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			IPath fullPath = file.getLocation();
			modelPath = fullPath == null ? "" : fullPath.toOSString();
			fullPath = file.getProject().getLocation();
			projectPath = fullPath == null ? "" : fullPath.toOSString();
		}
		// GamlResourceDocManager.clearCache();
		// We document only when the resource is marked as 'edited'
		// hqnghi build micro-model
		GamaMap<String, ModelDescription> mm = new GamaMap<String, ModelDescription>();
		for ( ISyntacticElement r : microModels.keySet() ) {
			List<ISyntacticElement> res = new ArrayList<ISyntacticElement>();
			res.add(r);
			ModelDescription mic =
				getModelFactory().createModelDescription(projectPath, modelPath, res, getErrorCollector(), isEdited,
					new GamaMap());
			mic.setAlias(microModels.get(r));
			mm.addValue(null, new GamaPair<String, ModelDescription>(microModels.get(r), mic));
		}
		// end-hqnghi
		return getModelFactory().createModelDescription(projectPath, modelPath, models, getErrorCollector(), isEdited,
			mm);
	}

	public LinkedHashMap<URI, String> computeAllImportedURIs(final ResourceSet set) {
		// TODO A Revoir pour éviter trop de créations de listes/map, etc.

		final LinkedHashMap<URI, String> totalResources = new LinkedHashMap();
		// Map<Resource, Boolean> = if true, resources are used to compose the model; if false, they are used as sub-models
		final LinkedHashMap<GamlResource, String> newResources = new LinkedHashMap();
		// The current resource is considered as imported "normally"
		newResources.put(this, null);
		while (!newResources.isEmpty()) {
			final Map<GamlResource, String> resourcesToConsider = new LinkedHashMap(newResources);
			newResources.clear();
			for ( final GamlResource gr : resourcesToConsider.keySet() ) {
				if ( !totalResources.containsKey(gr.getURI()) ) {
					totalResources.put(gr.getURI(), resourcesToConsider.get(gr));
					final TOrderedHashMap<GamlResource, Import> imports = gr.loadImports(set);
					for ( Map.Entry<GamlResource, Import> entry : imports.entrySet() ) {
						ImportImpl impl = (ImportImpl) entry.getValue();
						if ( impl.eIsSet(GamlPackage.IMPORT__NAME) ) {
							newResources.put(entry.getKey(), impl.getName());
						} else {
							// "normal" Import (no "as")
							newResources.put(entry.getKey(), null);
						}
					}
					// newResources.addAll(imports.keySet());
				}
			}
		}
		// GamlModelBuilder.imports(getURI(), totalResources);
		return totalResources;

	}

	public TOrderedHashMap<GamlResource, Import> loadImports(final ResourceSet resourceSet) {
		final TOrderedHashMap<GamlResource, Import> imports = new TOrderedHashMap();
		final Model model = (Model) getContents().get(0);
		for ( final Import imp : model.getImports() ) {
			final String importUri = imp.getImportURI();
			if ( importUri != null ) {
				final URI iu = URI.createURI(importUri, false).resolve(getURI());
				if ( EcoreUtil2.isValidUri(this, iu) ) {
					GamlResource ir = (GamlResource) resourceSet.getResource(iu, true);
					if ( ir != null ) {

						imports.put(ir, imp);
					}
				}
			}
		}
		return imports;
	}

	public LinkedHashMap<GamlResource, String> loadAllResources(final ResourceSet resourceSet) {
		final LinkedHashMap<GamlResource, String> totalResources = new LinkedHashMap();
		Map<URI, String> uris = computeAllImportedURIs(resourceSet);
		for ( URI uri : uris.keySet() ) {
			// if ( uris.get(uri) ) {
			final GamlResource ir = (GamlResource) resourceSet.getResource(uri, true);
			if ( ir != null ) {
				totalResources.put(ir, uris.get(uri));
			}
			// }
		}
		return totalResources;
	}

	public EObject findImport(final URI uri) {
		Model m = (Model) getContents().get(0);
		for ( final Import imp : m.getImports() ) {
			final URI iu = URI.createURI(imp.getImportURI(), false).resolve(getURI());
			if ( uri.equals(iu) ) { return imp; }
		}
		return null;
	}

	private void invalidateBecauseOfImportedProblem(final String msg, final GamlResource resource) {
		getErrorCollector().add(
			new GamlCompilationError(msg, IGamlIssue.GENERAL, resource.getContents().get(0), false, false));
		updateWith(null);
	}

	private ModelDescription buildCompleteDescription(final ResourceSet set) {
		resetErrorCollector();
		// We make sure the resource is loaded in the ResourceSet passed
		// TODO Does it validate it ?
		set.getResource(getURI(), true);
		ModelDescription model = null;

		// If one of the resources has already errors, no need to validate
		// We first build the list of resources (including this);
		Map<GamlResource, String> imports = loadAllResources(set);
		for ( GamlResource r : imports.keySet() ) {
			if ( !r.getErrors().isEmpty() ) {
				invalidateBecauseOfImportedProblem("Syntax errors detected ", r);
				return null;
			}
		}

		model = buildModelDescription(imports);

		// If, for whatever reason, the description is null, we stop the semantic validation
		if ( model == null ) {
			invalidateBecauseOfImportedProblem("Impossible to validate model " + getURI().lastSegment() +
				" (check the logs)", this);
		}
		return model;
	}

	/**
	 * Validates the resource by compiling its contents into a ModelDescription.
	 * @return errors an ErrorCollector which contains semantic errors (as opposed to the ones obtained via
	 *         resource.getErrors(), which are syntactic errors), This collector can be probed for compilation
	 *         errors via its hasErrors(), hasInternalErrors(), hasImportedErrors() methods
	 * 
	 */
	public boolean validate(final ResourceSet set) {
		try {
			setValidating(true);
			// We first build the model description
			final long begin = System.nanoTime();
			final long mem = Runtime.getRuntime().freeMemory();
			final long mb = 1024;

			ModelDescription model = buildCompleteDescription(set);

			if ( model == null ) { return false; }

			// We then validate it and get rid of the description. The documentation is produced only if the resource is
			// marked as 'edited'
			model.validate(isEdited);
			updateWith(model);
			model.dispose();

			//
			System.out.println("****************************************************");
			System.out.println("Thread [" + Thread.currentThread().getName() + "] | Resource set [" +
				getResourceSet().getResources().size() + " resources]");
			System.out.println("'" + getURI().lastSegment() + "' validated in " + (System.nanoTime() - begin) /
				1000000d + " ms [ ~" + (mem - Runtime.getRuntime().freeMemory()) / mb + " kb used ]");
			System.out.println("****************************************************");

			return !getErrorCollector().hasInternalErrors();
		} catch (final Exception e) {
			e.printStackTrace();
			invalidateBecauseOfImportedProblem("An exception has occured during the validation of " +
				getURI().lastSegment() + ": " + e.getMessage(), this);
			return false;
		} finally {
			setValidating(false);
		}
	}

	IModel build(final ResourceSet set) {
		return build(set, new ArrayList());
	}

	ModelDescription buildDescription(final ResourceSet set, final List<GamlCompilationError> errors) {

		// We make sure the resource is loaded in the ResourceSet passed
		set.getResource(getURI(), true);

		System.out.println("Thread [" + Thread.currentThread().getName() + "]");
		System.out.println("Resource " + getURI().lastSegment() + " building");
		System.out.println("****************************************************");

		// Syntactic errors detected, we cannot build the resource
		if ( !getErrors().isEmpty() ) {
			getErrorCollector().add(
				new GamlCompilationError("Syntactic errors detected in " + getURI().lastSegment(), IGamlIssue.GENERAL,
					getContents().get(0), false, false));
			return null;
		}

		// We build the description
		ModelDescription model = buildCompleteDescription(set);
		// If the description has errors, we cannot build the resource
		if ( collector.hasErrors() ) {
			errors.addAll(collector.getInternalErrors());
			errors.addAll(collector.getImportedErrors());
			model.dispose();
			return null;
		}

		errors.addAll(ImmutableList.copyOf(collector));
		return model;
	}

	public IModel build(final ResourceSet set, final List<GamlCompilationError> errors) {
		// We build the description
		ModelDescription model = buildDescription(set, errors);
		// And compile it before returning it.
		return model == null ? null : (IModel) model.compile();
	}

	public IPath getPath() {
		IPath path;
		URI uri = getURI();
		if ( uri.isPlatform() ) {
			path = new Path(getURI().toPlatformString(false));
		} else if ( uri.isFile() ) {
			path = new Path(uri.toFileString());
		} else {
			path = new Path(uri.path());
		}
		path = new Path(URLDecoder.decode(path.toOSString()));
		return path;
	}

	public boolean isValidating() {
		return isValidating;
	}

	private void setValidating(final boolean v) {
		isValidating = v;
	}

	public void setEdited(final boolean b) {
		isEdited = b;
		GamlResourceDocManager.getInstance().document(this, b);
	}

	public boolean isEdited() {
		return isEdited;
	}

}
