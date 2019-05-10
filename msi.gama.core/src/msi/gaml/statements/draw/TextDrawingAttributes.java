/*******************************************************************************************************
 *
 * msi.gaml.statements.draw.TextDrawingAttributes.java, in plugin msi.gama.core, is part of the source code of the GAMA
 * modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gaml.statements.draw;

import msi.gama.common.geometry.Scaling3D;
import msi.gama.metamodel.agent.AgentIdentifier;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.IShape.Type;
import msi.gama.util.GamaColor;
import msi.gama.util.GamaFont;
import msi.gama.util.GamaMaterial;
import msi.gama.util.GamaPair;

@SuppressWarnings ({ "rawtypes", "unchecked" })
public class TextDrawingAttributes extends DrawingAttributes implements Cloneable {

	public final GamaFont font;
	public final boolean perspective;
	public final GamaPoint anchor;

	public TextDrawingAttributes(final Scaling3D size, final GamaPair<Double, GamaPoint> rotation,
			final GamaPoint location, final GamaPoint anchor, final GamaColor color, final GamaFont font,
			final Boolean perspective) {
		super(size, rotation, location, color, null, null);
		this.font = font;
		this.anchor = anchor;
		this.perspective = perspective == null ? true : perspective.booleanValue();
	}

	public TextDrawingAttributes copyTranslatedBy(GamaPoint p) {
		try {
			TextDrawingAttributes copy = (TextDrawingAttributes) super.clone();
			copy.geometryProperties = copy.geometryProperties.copy();
			// GamaPoint p1 = copy.geometryProperties.location;
			copy.geometryProperties.location = copy.geometryProperties.location.plus(p);
			// GamaPoint p2 = copy.geometryProperties.location;
			// DEBUG.OUT("" + p1 + " " + p2);
			return copy;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/**
	 * Method getMaterial()
	 *
	 * @see msi.gaml.statements.draw.DrawingAttributes#getMaterial()
	 */
	@Override
	public GamaMaterial getMaterial() {
		// TODO
		return null;
	}

	@Override
	public AgentIdentifier getAgentIdentifier() {
		return null;
	}

	@Override
	public Type getType() {
		return Type.POLYGON;
	}

	@Override
	public GamaPoint getAnchor() {
		return anchor;
	}

}