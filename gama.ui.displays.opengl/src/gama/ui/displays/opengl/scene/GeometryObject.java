/*******************************************************************************************************
 *
 * gama.ui.displays.opengl.scene.GeometryObject.java, in plugin gama.ui.displays.opengl, is part of the source code of the GAMA
 * modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.displays.opengl.scene;

import org.locationtech.jts.geom.Geometry;

import msi.gama.common.geometry.GeometryUtils;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gaml.statements.draw.DrawingAttributes;

public class GeometryObject extends AbstractObject<Geometry, DrawingAttributes> {

	public GeometryObject(final Geometry geometry, final DrawingAttributes attributes) {
		super(geometry, attributes, DrawerType.GEOMETRY);
	}

	@Override
	public void getTranslationInto(final GamaPoint p) {
		final GamaPoint explicitLocation = getAttributes().getLocation();
		if (explicitLocation == null) {
			p.setLocation(0, 0, 0);
		} else {
			GeometryUtils.getContourCoordinates(getObject()).getCenter(p);
			p.negate();
			p.add(explicitLocation);
		}
	}

	@Override
	public void getTranslationForRotationInto(final GamaPoint p) {
		final GamaPoint explicitLocation = getAttributes().getLocation();
		if (explicitLocation == null) {
			GeometryUtils.getContourCoordinates(getObject()).getCenter(p);
		} else {
			p.setLocation(explicitLocation);
		}
	}

	@Override
	public void getTranslationForScalingInto(final GamaPoint p) {
		GeometryUtils.getContourCoordinates(getObject()).getCenter(p);
	}

}
