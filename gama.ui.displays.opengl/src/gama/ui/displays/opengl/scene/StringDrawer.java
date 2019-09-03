/*******************************************************************************************************
 *
 * gama.ui.displays.opengl.scene.StringDrawer.java, in plugin gama.ui.displays.opengl, is part of the source code of the GAMA
 * modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.displays.opengl.scene;

import java.awt.Font;

import com.jogamp.opengl.util.gl2.GLUT;

import gama.ui.displays.opengl.OpenGL;
import gama.common.geometry.AxisAngle;
import gama.metamodel.shape.GamaPoint;

/**
 *
 * The class StringDrawer.
 *
 * @author drogoul
 * @since 4 mai 2013
 *
 */

public class StringDrawer extends ObjectDrawer<StringObject> {

	public StringDrawer(final OpenGL gl) {
		super(gl);
	}

	@Override
	protected void _draw(final StringObject s) {
		try {
			gl.pushMatrix();
			final AxisAngle rotation = s.getAttributes().getRotation();
			final GamaPoint p = s.getAttributes().getLocation();
			if (rotation != null) {
				gl.translateBy(p.x, p.y, p.z);
				final GamaPoint axis = rotation.getAxis();
				// AD Change to a negative rotation to fix Issue #1514
				gl.rotateBy(-rotation.getAngle(), axis.x, axis.y, axis.z);
				// Voids the location so as to make only one translation
				p.setLocation(0, 0, 0);
			}
			if (s.getAttributes().font != null && s.getAttributes().perspective) {
				final Font f = s.getAttributes().font;
				gl.perspectiveText(s.getObject(), f, p.x, p.y, p.z, s.getAttributes().getAnchor());
			} else {
				int fontToUse = GLUT.BITMAP_HELVETICA_18;
				final Font f = s.getAttributes().font;
				if (f != null) {
					if (f.getSize() < 10) {
						fontToUse = GLUT.BITMAP_HELVETICA_10;
					} else if (f.getSize() < 16) {
						fontToUse = GLUT.BITMAP_HELVETICA_12;
					}
				}
				gl.rasterText(s.getObject(), fontToUse, p.x, p.y, p.z);
			}
		} finally {
			gl.popMatrix();
		}
	}

}