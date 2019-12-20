/*********************************************************************************************
 *
 * 'GamlResourceDocumenter.java, in plugin gama.core.lang, is part of the source code of the GAMA modeling and
 * simulation platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package gama.core.lang.gaml.documentation;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import gama.core.lang.gaml.resource.GamlResourceServices;
import gama.dev.utils.DEBUG;
import gama.common.interfaces.IDocManager;
import gama.common.interfaces.IGamlDescription;
import gama.util.map.IMap;
import gaml.descriptions.IDescription;
import gaml.descriptions.IDescription.DescriptionVisitor;
import gaml.descriptions.ModelDescription;

/**
 * Class GamlResourceDocManager.
 *
 * @author drogoul
 * @since 13 avr. 2014
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamlResourceDocumenter implements IDocManager {

	final ConcurrentLinkedQueue<ModelDescription> cleanupTasks = new ConcurrentLinkedQueue();
	final ConcurrentLinkedQueue<DocumentationTask> documentationQueue = new ConcurrentLinkedQueue();
	final Job documentationJob = new Job("Documentation") {
		{
			setUser(false);
			setPriority(Job.SHORT);
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			DocumentationTask task = documentationQueue.poll();
			while (task != null) {
				task.process();
				task = documentationQueue.poll();
			}
			ModelDescription r = cleanupTasks.poll();
			while (r != null) {
				r.dispose();
				r = cleanupTasks.poll();
			}
			return Status.OK_STATUS;
		}
	};

	final DescriptionVisitor<IDescription> documentingVisitor = desc -> {
		document(desc);
		return true;

	};

	@Override
	public void addCleanupTask(final ModelDescription model) {
		cleanupTasks.add(model);
	}

	@Override
	public void setGamlDocumentation(final EObject object, final IGamlDescription description, final boolean replace,
			final boolean force) {
		if (!force && !shouldDocument(object)) { return; }
		documentationQueue.add(new DocumentationTask(object, description, this));
		documentationJob.schedule(50);
	}

	IMap<EObject, IGamlDescription> getDocumentationCache(final Resource resource) {
		if (resource == null) { return null; }
		return GamlResourceServices.getDocumentationCache(resource);
	}

	// To be called once the validation has been done
	@Override
	public void document(final IDescription desc) {
		if (desc == null) { return; }
		final EObject e = desc.getUnderlyingElement();
		if (e == null) { return; }
		setGamlDocumentation(e, desc, true);
		desc.visitOwnChildren(documentingVisitor);

	}

	@Override
	public IGamlDescription getGamlDocumentation(final IGamlDescription o) {
		if (o == null) { return null; }
		try {
			return new DocumentationNode(o);
		} catch (final IOException e) {
			DEBUG.ERR("GamlResourceDocumenter.getGamlDocumentation(): " + e.getMessage() + " for " + o.getTitle());
			return null;
		}
	}

	@Override
	public IGamlDescription getGamlDocumentation(final EObject object) {
		if (object == null) { return null; }
		final IMap<EObject, IGamlDescription> map = getDocumentationCache(object.eResource());
		if (map == null) { return null; }
		return map.get(object);
	}

	private static boolean shouldDocument(final EObject object) {
		if (object == null) { return false; }
		final Resource r = object.eResource();
		if (r == null) { return false; }
		if (!GamlResourceServices.isEdited(r)) { return false; }
		return true;
	}

}