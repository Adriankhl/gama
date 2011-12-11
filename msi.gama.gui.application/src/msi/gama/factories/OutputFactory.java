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
package msi.gama.factories;

import java.awt.Color;
import msi.gama.interfaces.*;
import msi.gama.kernel.exceptions.*;
import msi.gama.outputs.LayerDisplayOutput;
import msi.gama.outputs.layers.*;
import msi.gama.precompiler.GamlAnnotations.handles;
import msi.gama.precompiler.GamlAnnotations.uses;
import msi.gama.precompiler.*;
import msi.gama.util.GamaList;

/**
 * The Class OutputFactory.
 * 
 * @author drogoul
 */
@handles({ ISymbolKind.OUTPUT })
@uses({ ISymbolKind.LAYER })
public class OutputFactory extends SymbolFactory {

	static public LayerBox largeBox = new LayerBox(1d, 0d, 0d, 1d, 1d);

	public static LayerDisplayOutput createDisplay(final String name, final int refresh,
		final Color background, final AbstractDisplayLayer ... layers) throws GamaRuntimeException {
		IDescription desc;
		try {
			desc = DescriptionFactory.createDescription(ISymbol.DISPLAY, ISymbol.NAME, name);
		} catch (GamlException e) {
			throw new GamaRuntimeException(e);
		}
		LayerDisplayOutput l = new LayerDisplayOutput(desc);
		l.setRefreshRate(refresh);
		l.setBackgroundColor(background);
		l.setLayers(new GamaList(layers));
		return l;
	}

	public static LayerDisplayOutput createDisplay(final String name, final int refresh,
		final Color background) throws GamaRuntimeException {
		return createDisplay(name, refresh, background, new AbstractDisplayLayer[0]);
	}

	public static LayerDisplayOutput createDisplay(final String name, final int refresh)
		throws GamaRuntimeException {
		return createDisplay(name, refresh, Color.white);
	}

	public static LayerDisplayOutput createDisplay(final String name) throws GamaRuntimeException {
		return createDisplay(name, 1);
	}

}
