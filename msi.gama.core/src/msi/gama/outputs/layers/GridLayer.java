/*********************************************************************************************
 * 
 * 
 * 'GridLayer.java', in plugin 'msi.gama.application', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.outputs.layers;

import gnu.trove.set.hash.THashSet;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.util.Set;
import msi.gama.common.interfaces.*;
import msi.gama.common.util.ImageUtils;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.*;
import msi.gama.metamodel.topology.grid.IGrid;
import msi.gama.outputs.layers.*;
import msi.gama.runtime.IScope;
import msi.gama.util.file.GamaImageFile;
import com.vividsolutions.jts.geom.Envelope;

public class GridLayer extends ImageLayer {

	@Override
	public Rectangle2D focusOn(final IShape geometry, final IDisplaySurface s) {
		final GridLayerStatement g = (GridLayerStatement) definition;
		IAgent a = geometry.getAgent();
		if ( a == null ) { return null; }
		if ( a.getSpecies() != g.getEnvironment().getCellSpecies() ) { return null; }
		Envelope env = a.getEnvelope();
		Point min = this.getScreenCoordinatesFrom(env.getMinX(), env.getMinY(), s);
		Point max = this.getScreenCoordinatesFrom(env.getMaxX(), env.getMaxY(), s);
		return new Rectangle2D.Double(min.x, min.y, max.x - min.x, max.y - min.y);
	}

	public boolean turnGridOn;
	private double cellSize;

	public GridLayer(final IScope scope, final ILayerStatement layer) {
		super(scope, layer);
		turnGridOn = ((GridLayerStatement) layer).drawLines();
	}

	@Override
	public void reloadOn(final IDisplaySurface surface) {
		super.reloadOn(surface);
		if ( image != null ) {
			image.flush();
			image = null;
		}
	}

	@Override
	protected void buildImage(final IScope scope) {
		if ( scope == null ) { return; }
		final GridLayerStatement g = (GridLayerStatement) definition;
		final IGrid m = g.getEnvironment();
		final ILocation p = m.getDimensions();
		// in case the agents have been killed
		if ( m.getAgents().size() > 0 ) {
			cellSize = m.getAgents().get(0).getGeometry().getEnvelope().getWidth();
		}

		if ( image == null ) {
			image = ImageUtils.createCompatibleImage(p.getX(), p.getY());
		}
		int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(m.getDisplayData(), 0, data, 0, data.length);
		image.setRGB(0, 0, (int) p.getX(), (int) p.getY(), m.getDisplayData(), 0, (int) p.getX());
	}

	@Override
	public void privateDrawDisplay(final IScope scope, final IGraphics dg) {
		buildImage(scope);
		final GridLayerStatement g = (GridLayerStatement) definition;
		if ( image == null ) { return; }
		Color lineColor = null;
		if ( turnGridOn ) {
			lineColor = g.getLineColor();
			if ( lineColor == null ) {
				lineColor = Color.black;
			}
		}
		double[] gridValueMatrix = g.getElevationMatrix(scope);
		if ( gridValueMatrix != null ) {
			GamaImageFile textureFile = g.textureFile();
			if ( textureFile != null ) { // display grid dem:texturefile
				BufferedImage texture = textureFile.getImage(scope);
				dg.drawGrid(scope, texture, gridValueMatrix, true, g.isTriangulated(), g.isGrayScaled(),
					g.isShowText(), lineColor, cellSize, this.getName());
			} else {
				dg.drawGrid(scope, image, gridValueMatrix, g.isTextured(), g.isTriangulated(), g.isGrayScaled(),
					g.isShowText(), lineColor, cellSize, this.getName());
			}

		} else {
			dg.drawImage(scope, image, null, null, lineColor, null, true, this.getName());
		}
	}

	private IAgent getPlaceAt(final GamaPoint loc) {
		return ((GridLayerStatement) definition).getEnvironment().getAgentAt(loc);
	}

	@Override
	public Set<IAgent> collectAgentsAt(final int x, final int y, final IDisplaySurface g) {
		final Set<IAgent> result = new THashSet();
		result.add(getPlaceAt(this.getModelCoordinatesFrom(x, y, g)));
		return result;
	}

	@Override
	public String getType() {
		return "Grid layer";
	}

	/**
	 * @param newValue
	 */
	public void setDrawLines(final Boolean newValue) {
		turnGridOn = newValue;
	}

}
