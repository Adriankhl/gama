/*********************************************************************************************
 * 
 * 
 * 'AgentLayer.java', in plugin 'msi.gama.application', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.outputs.layers;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import java.awt.geom.Rectangle2D;
import java.util.*;
import msi.gama.common.interfaces.*;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.statements.*;

/**
 * Written by drogoul Modified on 23 août 2008
 * 
 * @todo Description
 * 
 */
public class AgentLayer extends AbstractLayer {

	public AgentLayer(final ILayerStatement layer) {
		super(layer);
	}

	@Override
	public Rectangle2D focusOn(final IShape geometry, final IDisplaySurface s) {
		return shapes.get(geometry);
	}

	protected final Map<IAgent, Rectangle2D> shapes = new THashMap();

	@Override
	public void privateDrawDisplay(final IScope scope, final IGraphics g) throws GamaRuntimeException {
		shapes.clear();
		// performance issue
		String aspectName = IKeyword.DEFAULT;
		if ( definition instanceof AgentLayerStatement ) {
			aspectName = ((AgentLayerStatement) definition).getAspectName();
		}
		for ( final IAgent a : getAgentsToDisplay() ) {
			if ( a != null/* && !scope.interrupted() */) {
				IExecutable aspect = a.getSpecies().getAspect(aspectName);
				if ( aspect == null ) {
					aspect = AspectStatement.DEFAULT_ASPECT;
				}
				Object[] result = new Object[1];
				scope.execute(aspect, a, null, result);
				final Rectangle2D r = (Rectangle2D) result[0];
				// final Rectangle2D r = aspect.draw(scope, a);
				if ( r != null ) {
					shapes.put(a, r);
				}
			}
		}
	}

	public Collection<IAgent> getAgentsForMenu(final IScope scope) {
		if ( shapes.isEmpty() ) { return getAgentsToDisplay(); }
		// Avoid recalculating the agents
		return shapes.keySet();
	}

	public Collection<IAgent> getAgentsToDisplay() {
		// return agents;
		if ( definition instanceof AgentLayerStatement ) { return ((AgentLayerStatement) definition)
			.getAgentsToDisplay(); }
		return ((GridLayerStatement) definition).getAgentsToDisplay();
	}

	@Override
	public Set<IAgent> collectAgentsAt(final int x, final int y, final IDisplaySurface g) {
		final Set<IAgent> selectedAgents = new THashSet();
		final Rectangle2D selection = new Rectangle2D.Double();
		selection.setFrameFromCenter(x, y, x + IDisplaySurface.SELECTION_SIZE / 2, y + IDisplaySurface.SELECTION_SIZE /
			2);
		for ( final Map.Entry<IAgent, Rectangle2D> entry : shapes.entrySet() ) {
			if ( entry.getValue().intersects(selection) ) {
				selectedAgents.add(entry.getKey());
			}
		}

		return selectedAgents;
	}

	@Override
	public String getType() {
		return "Agents layer";
	}

}
