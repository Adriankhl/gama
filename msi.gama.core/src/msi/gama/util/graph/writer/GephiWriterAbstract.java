package msi.gama.util.graph.writer;

import java.io.*;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.file.GamaFile;
import msi.gama.util.graph.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

public abstract class GephiWriterAbstract implements IGraphWriter {

	protected void initializeExporter(final GraphExporter exporter) {}

	private final void writeGraph(final String filename, final Workspace gephiWorkspace) {

		// retrieve exporter
		ExportController controller = null;
		GraphExporter exporter = null;
		synchronized (GraphUtilsGephi.gephiStaticLocker) {
			controller = Lookup.getDefault().lookup(ExportController.class);
			exporter = (GraphExporter) controller.getExporter(getFormat()); // Get GEXF exporter

		}
		if ( exporter == null ) { throw GamaRuntimeException.error("unable to find an exporter for format " +
			getFormat()); }

		// configure the exporter
		exporter.setExportVisible(false); // Only exports the visible (filtered) graph
		exporter.setWorkspace(gephiWorkspace);

		try {
			controller.exportFile(new File(filename), exporter);
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("Unable to export the graph in file " + filename + " (" + e.getMessage() +
				")");
		} catch (IOException e) {
			e.printStackTrace();
			throw GamaRuntimeException.error("Unable to export the graph in file " + filename + " (" + e.getMessage() +
				")");
		}
	}

	protected abstract String getFormat();

	@Override
	public void writeGraph(final IScope scope, final IGraph<?, ?> gamaGraph, final GamaFile<?, ?> gamaFile,
		final String filename) {

		// translate the gama graph as a gephi graph (that is, stored into a gephi workspace)
		Workspace gephiWorkspace = GraphUtilsGephi.loadIntoAGephiWorkspace(gamaGraph);

		writeGraph(filename, gephiWorkspace);
	}

}
