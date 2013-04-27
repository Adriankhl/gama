package msi.gama.jogl.utils;

import java.awt.Color;
import javax.media.opengl.GL;
import msi.gama.jogl.utils.GraphicDataType.*;
import msi.gama.jogl.utils.JTSGeometryOpenGLDrawer.JTSDrawer;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.*;

public class BasicOpenGlDrawer {

	// OpenGL member
	private final GL myGl;

	// need to have the GLRenderer to enable texture mapping.
	public JOGLAWTGLRenderer myGLRender;

	public JTSDrawer myJTSDrawer;

	public BasicOpenGlDrawer(final JOGLAWTGLRenderer gLRender) {

		myGl = gLRender.gl;
		myGLRender = gLRender;

		myJTSDrawer = new JTSDrawer(myGLRender);

	}

	/**
	 * Draw a geometry
	 * 
	 * @param geometry
	 */
	public void DrawJTSGeometry(MyJTSGeometry geometry) {
		if ( geometry.offSet.x != 0 || geometry.offSet.y != 0 ) {
			myGl.glTranslated(geometry.offSet.x, -geometry.offSet.y, 0.0f);
		}

		for ( int i = 0; i < geometry.geometry.getNumGeometries(); i++ ) {
			if ( geometry.geometry.getGeometryType() == "MultiPolygon" ) {
				myJTSDrawer.DrawMultiPolygon((MultiPolygon) geometry.geometry, geometry.z_layer, geometry.color,
					geometry.alpha, geometry.fill, geometry.border, geometry.angle, geometry.height, geometry.rounded);
			}

			else if ( geometry.geometry.getGeometryType() == "Polygon" ) {
				// The JTS geometry of a sphere is a circle (a polygon)
				if ( geometry.type.equals("sphere") ) {
					myJTSDrawer.DrawSphere(geometry.agent.getLocation(), geometry.z_layer, geometry.height,
						geometry.color, geometry.alpha);
				} else {
					if ( geometry.height > 0 ) {
						myJTSDrawer.DrawPolyhedre((Polygon) geometry.geometry, geometry.z_layer, geometry.color,
							geometry.alpha, geometry.fill, geometry.height, geometry.angle, true, geometry.border,
							geometry.rounded);
					} else {
						myJTSDrawer.DrawPolygon((Polygon) geometry.geometry, geometry.z_layer, geometry.color,
							geometry.alpha, geometry.fill, geometry.border, geometry.isTextured, geometry.angle, true,
							geometry.rounded);
					}
				}
			} else if ( geometry.geometry.getGeometryType() == "MultiLineString" ) {

				myJTSDrawer.DrawMultiLineString((MultiLineString) geometry.geometry, geometry.z_layer, geometry.color,
					geometry.alpha, geometry.height);
			}

			else if ( geometry.geometry.getGeometryType() == "LineString" ) {

				if ( geometry.height > 0 ) {
					myJTSDrawer.DrawPlan((LineString) geometry.geometry, geometry.z_layer, geometry.color,
						geometry.alpha, geometry.height, 0, true);
				} else {
					myJTSDrawer.DrawLineString((LineString) geometry.geometry, geometry.z_layer, 1.2f, geometry.color,
						geometry.alpha);
				}
			}

			else if ( geometry.geometry.getGeometryType() == "Point" ) {
				if ( geometry.height > 0 ) {
					myJTSDrawer.DrawSphere(geometry.agent.getLocation(), geometry.z_layer, geometry.height,
						geometry.color, geometry.alpha);
				} else {
					myJTSDrawer.DrawPoint((Point) geometry.geometry, geometry.z_layer, 10, myGLRender.displaySurface
						.getIGraphics().getMaxEnvDim() / 1000, geometry.color, geometry.alpha);
				}
			}
		}
		if ( geometry.offSet.x != 0 || geometry.offSet.y != 0 ) {
			myGl.glTranslated(-geometry.offSet.x, geometry.offSet.y, 0.0f);
		}
	}

	/**
	 * Draw a geometry with a specific color
	 * 
	 * @param geometry
	 */
	public void DrawJTSGeometry(MyJTSGeometry geometry, Color c) {

		myGl.glTranslated(geometry.offSet.x, -geometry.offSet.y, 0.0f);

		for ( int i = 0; i < geometry.geometry.getNumGeometries(); i++ ) {

			if ( geometry.geometry.getGeometryType() == "MultiPolygon" ) {
				myJTSDrawer.DrawMultiPolygon((MultiPolygon) geometry.geometry, geometry.z_layer, c, geometry.alpha,
					geometry.fill, geometry.border, geometry.angle, geometry.height, geometry.rounded);
			}

			else if ( geometry.geometry.getGeometryType() == "Polygon" ) {
				// The JTS geometry of a sphere is a circle (a polygon)
				if ( geometry.type.equals("sphere") ) {
					myJTSDrawer.DrawSphere(geometry.agent.getLocation(), geometry.z_layer, geometry.height, c,
						geometry.alpha);
				} else {
					if ( geometry.height > 0 ) {
						myJTSDrawer.DrawPolyhedre((Polygon) geometry.geometry, geometry.z_layer, c, geometry.alpha,
							geometry.fill, geometry.height, geometry.angle, true, geometry.border, geometry.rounded);
					} else {
						myJTSDrawer
							.DrawPolygon((Polygon) geometry.geometry, geometry.z_layer, c, geometry.alpha,
								geometry.fill, geometry.border, geometry.isTextured, geometry.angle, true,
								geometry.rounded);
					}
				}
			} else if ( geometry.geometry.getGeometryType() == "MultiLineString" ) {
				myJTSDrawer.DrawMultiLineString((MultiLineString) geometry.geometry, geometry.z_layer, c,
					geometry.alpha, geometry.height);
			}

			else if ( geometry.geometry.getGeometryType() == "LineString" ) {
				if ( geometry.height > 0 ) {
					myJTSDrawer.DrawPlan((LineString) geometry.geometry, geometry.z_layer, c, geometry.alpha,
						geometry.height, 0, true);
				} else {
					myJTSDrawer.DrawLineString((LineString) geometry.geometry, geometry.z_layer, 1.2f, c,
						geometry.alpha);
				}
			}

			else if ( geometry.geometry.getGeometryType() == "Point" ) {
				if ( geometry.height > 0 ) {
					myJTSDrawer.DrawSphere(geometry.agent.getLocation(), geometry.z_layer, geometry.height, c,
						geometry.alpha);
				} else {
					myJTSDrawer.DrawPoint((Point) geometry.geometry, geometry.z_layer, 10, geometry.height, c,
						geometry.alpha);
				}

			}
		}

		myGl.glTranslated(-geometry.offSet.x, geometry.offSet.y, 0.0f);
	}

	public void drawSimpleFeatureCollection(MyCollection collection) {

		// Draw Shape file so need to inverse the y composante.
		myJTSDrawer.yFlag = 1;

		myGl.glTranslated(-collection.collection.getBounds().centre().x, -collection.collection.getBounds().centre().y,
			0.0f);

		// Iterate throught all the collection
		SimpleFeatureIterator iterator = collection.collection.features();

		// Color color= Color.red;

		while (iterator.hasNext()) {

			SimpleFeature feature = iterator.next();

			Geometry sourceGeometry = (Geometry) feature.getDefaultGeometry();

			if ( sourceGeometry.getGeometryType() == "MultiPolygon" ) {
				myJTSDrawer.DrawMultiPolygon((MultiPolygon) sourceGeometry, 0.0f, collection.color, 1.0f, true, null,
					0, 0.0f, false);
			}

			else if ( sourceGeometry.getGeometryType() == "Polygon" ) {
				myJTSDrawer.DrawPolygon((Polygon) sourceGeometry, 0.0f, collection.color, 1.0f, true, null, false, 0,
					true, false);
			} else if ( sourceGeometry.getGeometryType() == "MultiLineString" ) {
				myJTSDrawer.DrawMultiLineString((MultiLineString) sourceGeometry, 0.0f, collection.color, 1.0f, 0.0f);
			}

			else if ( sourceGeometry.getGeometryType() == "LineString" ) {
				myJTSDrawer.DrawLineString((LineString) sourceGeometry, 0.0f, 1.0f, collection.color, 1.0f);
			}

			else if ( sourceGeometry.getGeometryType() == "Point" ) {
				myJTSDrawer.DrawPoint((Point) sourceGeometry, 0.0f, 10, 10, collection.color, 1.0f);
			}
		}

		myGl.glTranslated(collection.collection.getBounds().centre().x, +collection.collection.getBounds().centre().y,
			0.0f);

		myJTSDrawer.yFlag = -1;

	}

}
