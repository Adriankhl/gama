/*******************************************************************************************************
 *
 * ummisco.gama.outputs.layers.GridLayer.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package ummisco.gama.outputs.layers;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import msi.gama.common.interfaces.IAgent;
import msi.gama.common.interfaces.outputs.IDisplaySurface;
import msi.gama.common.interfaces.outputs.IGraphics;
import msi.gama.common.interfaces.outputs.ILayerData;
import msi.gama.common.interfaces.outputs.ILayerStatement;
import msi.gama.common.util.Collector;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.scope.IScope;
import msi.gama.util.GamaColor;
import msi.gama.util.file.IGamaFile;
import msi.gaml.statements.draw.FieldDrawingAttributes;

public class GridLayer extends AbstractLayer {

	public GridLayer(final ILayerStatement layer) {
		super(layer);
	}

	@Override
	protected ILayerData createData() {
		return new GridLayerData(definition);
	}

	@Override
	public GridLayerData getData() {
		return (GridLayerData) super.getData();
	}

	@Override
	public Rectangle2D focusOn(final IShape geometry, final IDisplaySurface s) {
		final IAgent a = geometry.getAgent();
		if (a == null || a.getSpecies() != getData().getGrid().getCellSpecies())
			return null;
		return super.focusOn(a, s);
	}

	@Override
	public void reloadOn(final IDisplaySurface surface) {
		super.reloadOn(surface);
		getData().setImage(null);
	}

	@Override
	public void privateDraw(final IScope scope, final IGraphics dg) {
		GamaColor lineColor = null;
		final GridLayerData data = getData();
		if (data.drawLines()) {
			lineColor = data.getLineColor();
		}

		final double[] gridValueMatrix = data.getElevationMatrix(scope);
		final IGamaFile.Image textureFile = data.textureFile();
		final FieldDrawingAttributes attributes =
				new FieldDrawingAttributes(getName(), lineColor, gridValueMatrix == null);
		attributes.grayScaled = data.isGrayScaled();
		final BufferedImage image = data.getImage();
		if (textureFile != null) {
			attributes.setTextures(Arrays.asList(textureFile));
		} else if (image != null) {
			final int[] imageData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			System.arraycopy(data.getGrid().getDisplayData(), 0, imageData, 0, imageData.length);
			attributes.setTextures(Arrays.asList(image));
		}
		attributes.triangulated = data.isTriangulated();
		attributes.withText = data.isShowText();
		attributes.setCellSize(data.getCellSize());
		attributes.setBorder(lineColor);

		if (gridValueMatrix == null) {
			dg.drawImage(image, attributes);
		} else {
			dg.drawField(gridValueMatrix, attributes);
		}
	}

	@Override
	public Set<IAgent> collectAgentsAt(final int x, final int y, final IDisplaySurface g) {
		try (Collector.AsSet<IAgent> result = Collector.newSet()) {
			result.add(getData().getGrid().getAgentAt(getModelCoordinatesFrom(x, y, g)));
			return result.items();
		}
	}

	@Override
	public String getType() {
		return "Grid layer";
	}

	@Override
	public Collection<IAgent> getAgentsForMenu(final IScope scope) {
		return getData().getGrid().getAgents();
	}

}
