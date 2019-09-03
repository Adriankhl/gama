/*********************************************************************************************
 *
 * 'ReverseOperators.java, in plugin ummisco.gama.serialize, is part of the source code of the GAMA modeling and
 * simulation platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package gama.extensions.serialize.gaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.thoughtworks.xstream.XStream;

import gama.dev.utils.DEBUG;
import gama.extensions.serialize.factory.StreamConverter;
import gama.extensions.serialize.gamaType.converters.ConverterScope;
import gama.processor.annotations.GamlAnnotations.doc;
import gama.processor.annotations.GamlAnnotations.operator;
import gama.common.interfaces.IAgent;
import gama.common.interfaces.IKeyword;
import gama.common.util.FileUtils;
import gama.kernel.experiment.ExperimentAgent;
import gama.kernel.simulation.SimulationAgent;
import gama.metamodel.agent.SavedAgent;
import gama.runtime.scope.IScope;

public class ReverseOperators {

	static {
		DEBUG.OFF();
	}

	@operator (
			value = "serialize")
	@doc (
			value = "It serializes any object, i.e. transform it into a string.")
	public static String serialize(final IScope scope, final Object o) {
		DEBUG.OUT("**** Serialize Object ****");
		return StreamConverter.convertObjectToStream(scope, o);
	}

	@operator (
			value = "unserialize")
	@doc (
			value = "",
			deprecated = "Still in alpha version, do not use it.")
	public static Object unserialize(final IScope scope, final String s) {
		DEBUG.OUT("**** unSerialize Object ****");
		return StreamConverter.convertStreamToObject(scope, s);
	}

	@operator (
			value = "serialize_agent")
	@doc (
			value = "")
	public static String serializeAgent(final IScope scope, final IAgent agent) {
		return StreamConverter.convertObjectToStream(scope, new SavedAgent(scope, agent));
	}

	@operator (
			value = "restore_simulation_from_file")
	@doc (
			value = "restoreSimulationFromFile")
	public static int unSerializeSimulationFromFile(final IScope scope, final GamaSavedSimulationFile file) {
		return unSerializeSimulationFromXML(scope, file.getBuffer().get(0));
	}

	@operator (
			value = "restore_simulation")
	@doc (
			value = "restore_simulation")
	public static int unSerializeSimulationFromXML(final IScope scope, final String simul) {
		final ConverterScope cScope = new ConverterScope(scope);
		final XStream xstream = StreamConverter.loadAndBuild(cScope);

		final SavedAgent saveAgt = (SavedAgent) xstream.fromXML(simul);
		final ExperimentAgent expAgt = (ExperimentAgent) scope.getExperiment();
		final SimulationAgent simAgent = expAgt.getSimulation();

		simAgent.updateWith(scope, saveAgt);

		return 1;
	}

	// TODO : This should become a part of the save statement 
	@operator (
			value = "save_agent")
	@doc (
			value = "")
	public static int saveAgent(final IScope scope, final IAgent agent, final String pathname) {
		final String path = FileUtils.constructAbsoluteFilePath(scope, pathname, false);

		final String serializedAgent = serializeAgent(scope, agent);

		final ExperimentAgent expAgt = (ExperimentAgent) scope.getExperiment();
		final SimulationAgent simAgt = expAgt.getSimulation();
		final int savedCycle = simAgt.getClock().getCycle();
		final String savedModel = expAgt.getModel().getFilePath();
		final String savedExperiment = (String) expAgt.getSpecies().getFacet(IKeyword.NAME).value(scope);

		FileWriter fw = null;
		try {
			if (path.equals("")) { return -1; }
			
			final File f = new File(path);
			
			final File parent = f.getParentFile();
			if (!parent.exists()) {
				parent.mkdirs();
			}
									
			if (!f.exists()) {
				f.createNewFile();
			}
			fw = new FileWriter(f);

			// Write the Metadata
			fw.write(savedModel + System.lineSeparator());
			fw.write(savedExperiment + System.lineSeparator());
			fw.write(savedCycle + System.lineSeparator());

			// Write the serializedAgent
			fw.write(serializedAgent);
			fw.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return 0;
	}

	@operator (
			value = "save_simulation")
	@doc (
			value = "")
	public static int saveSimulation(final IScope scope, final String pathname) {
		final ExperimentAgent expAgt = (ExperimentAgent) scope.getExperiment();
		final SimulationAgent simAgt = expAgt.getSimulation();

		return saveAgent(scope, simAgt, pathname);
	}

	// TODO to remove when possible
	@operator (
			value = "serialize_network")
	@doc (
			value = "[For network purpose] It serializes any object, i.e. transform it into a string.",
			deprecated = "Still in alpha version, do not use it.")
	public static String serializeNetwork(final IScope scope, final Object o) {
		DEBUG.OUT("**** Serialize Object ****");
		return StreamConverter.convertNetworkObjectToStream(scope, o);
	}

	@operator (
			value = "unserialize_network")
	@doc (
			value = "[For network purpose]",
			deprecated = "Still in alpha version, do not use it.")
	public static Object unserializeNetwork(final IScope scope, final String s) {
		DEBUG.OUT("**** unSerialize Object ****");
		return StreamConverter.convertNetworkStreamToObject(scope, s);
	}
	// END TODO

}