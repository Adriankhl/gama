/*********************************************************************************************
 *
 * 'GamlModelBuilder.java, in plugin msi.gama.lang.gaml, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package msi.gama.lang.gaml.validation;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.SynchronizedXtextResourceSet;

import com.google.common.collect.Iterables;

import msi.gama.common.interfaces.IGamlIssue;
import msi.gama.kernel.model.IModel;
import msi.gama.lang.gaml.resource.GamlResource;
import msi.gaml.compilation.GamlCompilationError;
import msi.gaml.compilation.IGamlModelBuilder;
import msi.gaml.descriptions.ModelDescription;

/**
 * Class GamlResourceBuilder.
 *
 * @author drogoul
 * @since 8 avr. 2014
 *
 */
public class GamlModelBuilder implements IGamlModelBuilder {

	private final ResourceSet buildResourceSet;

	// /**
	// * A constructor that builds the resource set based on an existing injecto
	// *
	// * @param injector
	// */
	// public GamlModelBuilder(final Injector injector) {
	// buildResourceSet = injector.getInstance(ResourceSet.class);
	// }

	public GamlModelBuilder() {
		buildResourceSet = new SynchronizedXtextResourceSet();
	}

	@Override
	public IModel compile(final URL url, final List<GamlCompilationError> errors) {
		try {
			final java.net.URI uri = new java.net.URI(url.getProtocol(), url.getPath(), null).normalize();
			final URI resolvedURI = URI.createURI(uri.toString());
			return compile(resolvedURI, errors);
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public IModel compile(final URI uri, final List<GamlCompilationError> errors) {
		// We build the description and fill the errors list
		final ModelDescription model = buildModelDescription(uri, errors);
		// And compile it before returning it, unless it is null.
		return model == null ? null : (IModel) model.compile();
	}

	private ModelDescription buildModelDescription(final URI uri, final List<GamlCompilationError> errors) {
		try {
			final GamlResource r = (GamlResource) buildResourceSet.getResource(uri, true);
			// Syntactic errors detected, we cannot build the resource
			if (r.hasErrors()) {
				if (errors != null) {
					final String err_ =
							r.getErrors() != null && r.getErrors().size() > 0 ? r.getErrors().get(0).toString() : "";
					errors.add(new GamlCompilationError("Syntax errors: " + err_, IGamlIssue.GENERAL,
							r.getContents().get(0), false, false));
				}
				return null;
			} else {
				// We build the description
				final ModelDescription model = r.buildCompleteDescription();
				if (model != null) {
					model.validate();
				}
				if (errors != null) {
					Iterables.addAll(errors, r.getValidationContext());
				}
				if (r.getValidationContext().hasErrors()) { return null; }
				return model;
			}
		} finally {
			final boolean wasDeliver = buildResourceSet.eDeliver();
			try {
				buildResourceSet.eSetDeliver(false);
				buildResourceSet.getResources().clear();
			} finally {
				buildResourceSet.eSetDeliver(wasDeliver);
			}
		}
	}

	@Override
	public void loadURLs(final List<URL> URLs) {
		for (final URL url : URLs) {
			java.net.URI uri;
			try {
				uri = new java.net.URI(url.getProtocol(), url.getPath(), null).normalize();
				final URI resolvedURI = URI.createURI(uri.toString());
				buildResourceSet.getResource(resolvedURI, true);
			} catch (final URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
}
