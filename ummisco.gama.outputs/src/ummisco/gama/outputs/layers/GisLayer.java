/*******************************************************************************************************
 *
 * ummisco.gama.outputs.layers.GisLayer.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package ummisco.gama.outputs.layers;

import java.awt.Color;
import java.util.List;

import msi.gama.common.interfaces.IAgent;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.interfaces.outputs.IGraphics;
import msi.gama.common.interfaces.outputs.ILayerStatement;
import msi.gama.common.preferences.GamaPreferences;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.runtime.scope.IScope;
import msi.gama.util.GamaColor;
import msi.gama.util.file.IGamaFile;
import msi.gama.util.file.IGamaFile.Gis;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.statements.draw.DrawingAttributes;
import msi.gaml.statements.draw.ShapeDrawingAttributes;
import msi.gaml.types.GamaFileType;
import msi.gaml.types.IType;

public class GisLayer extends AbstractLayer {

	IExpression gisExpression, colorExpression;

	public GisLayer(final ILayerStatement layer) {
		super(layer);
		gisExpression = layer.getFacet(IKeyword.GIS);
		colorExpression = layer.getFacet(IKeyword.COLOR);
	}

	@Override
	public void privateDraw(final IScope scope, final IGraphics g) {
		final GamaColor color =
				colorExpression == null ? GamaColor.getInt(GamaPreferences.Displays.CORE_COLOR.getValue().getRGB())
						: Cast.asColor(scope, colorExpression.value(scope));
		final List<IShape> shapes = buildGisLayer(scope);
		if (shapes != null) {
			for (final IShape geom : shapes) {
				if (geom != null) {
					final DrawingAttributes attributes =
							new ShapeDrawingAttributes(geom, (IAgent) null, color, new GamaColor(Color.black));
					g.drawShape(geom.getInnerGeometry(), attributes);
				}
			}
		}
	}

	public List<IShape> buildGisLayer(final IScope scope) throws GamaRuntimeException {
		final IGamaFile.Gis file = getShapeFile(scope);
		if (file == null)
			return null;
		return file.getContents(scope);
	}

	private IGamaFile.Gis getShapeFile(final IScope scope) {
		if (gisExpression == null)
			return null;
		if (gisExpression.getGamlType().id() == IType.STRING) {
			final String fileName = Cast.asString(scope, gisExpression.value(scope));
			return (Gis) GamaFileType.createFile(scope, fileName, null);
		}
		final Object o = gisExpression.value(scope);
		if (o instanceof IGamaFile.Gis)
			return (IGamaFile.Gis) o;
		return null;
	}

	@Override
	public String getType() {
		return "Gis layer";
	}

}
