/*
 * GAMA - V1.4  http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2011
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2011
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen  (Batch, GeoTools & JTS), 2009-2011
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2011
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gama.java;

import java.util.Map;

import msi.gama.environment.ITopology;
import msi.gama.interfaces.*;
import msi.gama.internal.compilation.*;
import msi.gama.kernel.*;
import msi.gama.kernel.exceptions.*;
import msi.gama.kernel.experiment.*;
import msi.gaml.batch.Solution;

public class JavaSimulation extends AbstractSimulation {

	/**
	 * @throws InterruptedException
	 * @throws GamlException
	 * @throws GamaRuntimeException
	 * @param facets
	 * @param description
	 */

	public JavaSimulation(final IExperiment exp, final Solution parameters, final Long seed)
		throws GamaRuntimeException, GamlException, InterruptedException {
		super(exp, parameters);
	}

	Map<String, ExperimentParameter> parameters;

	//
	// @Override
	// public void reload(final Map<String, IParameter> parameters) {
	// // TODO Auto-generated method stub
	//
	// }

	// @Override
	// public void initialize(final Map<String, IParameter> parameters, final Long seed)
	// throws GamlException {
	// Ajouter toutes les esp�ces ? Normalement d�j� ajout�es par le programmeur
	// Cr�er remoteControl
	// Initialiser le Random avec la seed
	// Cr�er (ou initialiser) le "monde".
	// Changer les param�tres ( parameters.setFacets(paramValues); ?)
	// Initialiser l'environnement
	// Initialiser les agent managers
	// Lancer (et fermer) l'init sequence du scheduler
	// Construire les outputs

	// }
	//
	// @Override
	// public List<IParameter> getParameters() {
	// return new GamaList(parameters.values());
	// }

	// @Override
	// public Object getParameterValue(final String name) throws GamaRuntimeException {
	// IParameter p = getParameter(name);
	// if ( p == null ) { return null; }
	// return p.value(getGlobalScope());
	// }

	//
	// @Override
	// public boolean isGui() {
	// return true; // ??
	// }

	// @Override
	// public void setParameterValue(final String name, final Object value)
	// throws GamaRuntimeException {
	// IParameter p = parameters.get(name);
	// if ( p != null ) {
	// p.setInitial(experiment.getExpressionFactory().createConst(value));
	// }
	// }

	//
	// @Override
	// public void addOutput(final IOutput output) {
	// this.output.addOutput(output);
	// }
	//
	// @Override
	// public void setEnvironmentSize(final int width, final int height) throws GamaRuntimeException
	// {
	// environment.setDimensions(getGlobalScope(), width, height);
	// }

	// @Override
	// public IAgentManager addGrid(final ISpecies spec, final int width, final int height,
	// final int neighbours, final boolean torus) {
	// return addAgentManager(spec, new JavaRegularAgentManager(spec, new GamaSpatialMatrix(
	// getWorldScope().getGeometry(), width, height, torus, neighbours == 4)));
	// }
	//
	// @Override
	// public IParameter getParameter(final String name) {
	// return parameters.get(name);
	// }

	//
	// @Override
	// protected void initializeEnvironment() {
	//
	// }

	@Override
	protected void initializeWorld(final Map<String, Object> parameters) {

	}

	@Override
	protected void initializeWorldPopulation() {
		// TODO Auto-generated method stub
		
	}

}
