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

import java.awt.Graphics2D;
import msi.gama.interfaces.*;
import msi.gama.util.GamaList;
import com.vividsolutions.jts.geom.*;

/**
 * Written by drogoul Modified on 23 f�vr. 2011
 * 
 * @todo Description
 * 
 */
public interface ISpatialIndex {

	public abstract void insert(final Envelope bounds, final IAgent o);

	public abstract void insert(final Coordinate location, final IAgent agent);

	public abstract void remove(final Envelope bounds, final IAgent o);

	public abstract void remove(final Coordinate previousLoc, final IAgent agent);

	public abstract GamaList<IAgent> allAtDistance(final IGeometry source, final double dist,
		final IAgentFilter f);

	public abstract IAgent firstAtDistance(final IGeometry source, final double dist,
		final IAgentFilter f);

	public abstract GamaList<IAgent> allInEnvelope(final IGeometry source, final Envelope envelope,
		final IAgentFilter f, boolean contained);

	public abstract void drawOn(Graphics2D g2, int width, int height);

	public abstract void update();

}