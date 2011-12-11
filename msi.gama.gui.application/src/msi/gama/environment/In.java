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
package msi.gama.environment;

import java.util.*;
import msi.gama.interfaces.*;
import msi.gama.kernel.exceptions.GamaRuntimeException;

public abstract class In implements IAgentFilter {

	public static In list(final IScope scope, final IGamaContainer<?, IGeometry> targets)
		throws GamaRuntimeException {
		return new InList(new HashSet(targets.listValue(scope)));
	}

	public static In species(final ISpecies species) {
		return new InSpecies(species);
	}

	@Override
	public abstract boolean accept(IGeometry source, IGeometry a);

	private static class InList extends In {

		final Set<IGeometry> agents;

		InList(final Set list) {
			agents = list;
		}

		@Override
		public boolean accept(final IGeometry source, final IGeometry a) {
			return a.getGeometry() != source.getGeometry() && agents.contains(a);
		}

	}

	private static class InSpecies extends In {

		final ISpecies species;

		InSpecies(final ISpecies s) {
			species = s;
		}

		@Override
		public boolean accept(final IGeometry source, final IGeometry a) {
			return a.getGeometry() != source.getGeometry() &&
				((IAgent) a).isInstanceOf(species, true);
		}

	}

}
