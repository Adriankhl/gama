/*********************************************************************************************
 * 
 * 
 * 'LayerManager.java', in plugin 'msi.gama.application', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.outputs.display;

import java.awt.geom.Rectangle2D;
import java.util.*;
import msi.gama.common.interfaces.*;
import msi.gama.common.util.GuiUtils;
import msi.gama.metamodel.shape.IShape;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.outputs.layers.*;
import msi.gama.runtime.IScope;

/**
 * Written by drogoul Modified on 23 janv. 2011
 * 
 * @todo Description
 * 
 */
public class LayerManager implements ILayerManager {

	private final List<ILayer> enabledLayers = new ArrayList();
	private final List<ILayer> disabledLayers = new ArrayList();
	private final IDisplaySurface surface;
	private int count = 0;

	public LayerManager(final IDisplaySurface surface, final LayeredDisplayOutput output) {
		this.surface = surface;
		final List<AbstractLayerStatement> layers = output.getLayers();
		for ( final AbstractLayerStatement layer : layers ) {
			addLayer(AbstractLayer.createLayer(output.getScope(), layer));
		}
	}

	@Override
	public void dispose() {
		for ( final ILayer d : enabledLayers ) {
			d.dispose();
		}
		for ( final ILayer d : disabledLayers ) {
			d.dispose();
		}
		enabledLayers.clear();
		disabledLayers.clear();
	}

	@Override
	public ILayer addLayer(final ILayer d) {
		if ( addItem(d) ) { return d; }
		return null;
	}

	public void removeLayer(final ILayer found) {
		if ( found != null ) {
			enabledLayers.remove(found);
		}
		Collections.sort(enabledLayers);
	}

	@Override
	public List<ILayer> getLayersIntersecting(final int x, final int y) {
		final List<ILayer> result = new ArrayList();
		for ( final ILayer display : enabledLayers ) {
			if ( display.containsScreenPoint(x, y) ) {
				result.add(display);
			}
		}
		return result;
	}

	/**
	 * Method focusOn()
	 * @see msi.gama.common.interfaces.ILayerManager#focusOn(msi.gama.metamodel.shape.IShape)
	 */
	@Override
	public Rectangle2D focusOn(final IShape geometry, final IDisplaySurface s) {
		for ( final ILayer display : enabledLayers ) {
			Rectangle2D r = display.focusOn(geometry, s);
			if ( r != null ) { return r; }
		}
		return null;
	}

	private void enable(final ILayer found) {
		found.enableOn(surface);
		enabledLayers.add(found);
		disabledLayers.remove(found);
		Collections.sort(enabledLayers);
	}

	@Override
	public boolean isEnabled(final ILayer item) {
		return enabledLayers.contains(item);
	}

	private void disable(final ILayer found) {
		if ( found != null ) {
			found.disableOn(surface);
			removeLayer(found);
			disabledLayers.add(found);
		}
	}

	@Override
	public void enableLayer(final ILayer display, final Boolean enable) {

		surface.runAndUpdate(new Runnable() {

			@Override
			public void run() {
				// surface.canBeUpdated(false);
				if ( enable ) {
					enable(display);
				} else {
					disable(display);
				}
				surface.layersChanged();

				// surface.canBeUpdated(true);
			}
		});
	}

	@Override
	public void drawLayersOn(final IGraphics g) {
		IScope scope = surface.getDisplayScope();
		// If the experiment is already closed
		if ( scope == null || scope.interrupted() ) { return; }
		scope.setGraphics(g);
		try {
			g.beginDrawingLayers();
			for ( int i = 0, n = enabledLayers.size(); i < n; i++ ) {
				final ILayer dis = enabledLayers.get(i);
				dis.drawDisplay(scope, g);
			}
		} catch (final Exception e) {
			GuiUtils.debug(e);
		} finally {
			g.endDrawingLayers();
		}
	}

	@Override
	public List<ILayer> getItems() {
		final List<ILayer> items = new ArrayList();
		items.addAll(enabledLayers);
		items.addAll(disabledLayers);
		Collections.sort(items);
		return items;
	}

	@Override
	public void removeItem(final ILayer found) {
		if ( found != null ) {
			enabledLayers.remove(found);
		}
		Collections.sort(enabledLayers);
	}

	@Override
	public void pauseItem(final ILayer obj) {}

	@Override
	public void resumeItem(final ILayer obj) {}

	@Override
	public String getItemDisplayName(final ILayer obj, final String previousName) {
		return obj.getMenuName();
	}

	@Override
	public void focusItem(final ILayer obj) {}

	@Override
	public boolean addItem(final ILayer obj) {
		obj.setOrder(count++);
		enabledLayers.add(obj);
		Collections.sort(enabledLayers);
		obj.firstLaunchOn(surface);
		return true;
	}

	@Override
	public void updateItemValues() {}

	/**
	 * Allows the layers to do some cleansing when the output of the display changes
	 * @see msi.gama.common.interfaces.ILayerManager#outputChanged()
	 */
	@Override
	public void outputChanged() {
		for ( final ILayer i : enabledLayers ) {
			i.reloadOn(surface);
		}
		for ( final ILayer i : disabledLayers ) {
			i.reloadOn(surface);
		}
	}

	@Override
	public boolean stayProportional() {
		for ( final ILayer i : enabledLayers ) {
			if ( i.stayProportional() ) { return true; }
		}
		return false;
	}

	/**
	 * Method makeItemSelectable()
	 * @see msi.gama.common.interfaces.ItemList#makeItemSelectable(java.lang.Object, boolean)
	 */
	@Override
	public void makeItemSelectable(final ILayer data, final boolean b) {
		data.getDefinition().setSelectable(b);
	}

	/**
	 * Method makeItemVisible()
	 * @see msi.gama.common.interfaces.ItemList#makeItemVisible(java.lang.Object, boolean)
	 */
	@Override
	public void makeItemVisible(final ILayer obj, final boolean b) {
		enableLayer(obj, b);
	}

}
