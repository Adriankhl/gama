/*********************************************************************************************
 * 
 * 
 * 'StaticLayerObject.java', in plugin 'msi.gama.jogl2', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package ummisco.gama.opengl.scene;

import java.awt.*;
import ummisco.gama.opengl.JOGLRenderer;
import msi.gama.metamodel.shape.*;
import msi.gaml.types.GamaGeometryType;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.gl2.GLUT;
import com.vividsolutions.jts.geom.Geometry;

public class StaticLayerObject extends LayerObject {

	static Geometry NULL_GEOM = GamaGeometryType.buildRectangle(0, 0, new GamaPoint(0, 0)).getInnerGeometry();
	static final GamaPoint WORLD_OFFSET = new GamaPoint();
	static final GamaPoint WORLD_SCALE = new GamaPoint(1, 1, 1);
	static final Double WORLD_ALPHA = 1d;

	public StaticLayerObject(final JOGLRenderer renderer, final Integer id) {
		super(renderer, id);
	}

	@Override
	protected SceneObjects buildSceneObjects(final ObjectDrawer drawer, final boolean asList, final boolean asVBO) {
		return new SceneObjects.Static(drawer, asList, asVBO);
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public void clear(final GL gl, final int traceSize) {}

	public static class WaitingLayerObject extends StaticLayerObject {

		public WaitingLayerObject(final JOGLRenderer renderer) {
			super(renderer, 0);
			setTrace(0);
			setFading(false);
			setAlpha(WORLD_ALPHA);
			setOffset(WORLD_OFFSET);
			setScale(WORLD_SCALE);
		}

		@Override
		public void draw(final GL2 gl, final JOGLRenderer renderer, final boolean picking) {
			super.draw(gl, renderer, picking);

			gl.glDisable(GL.GL_BLEND);
			gl.glColor4d(0.0, 0.0, 0.0, 1.0d);
			gl.glRasterPos3d(-renderer.getWidth() / 10, renderer.getHeight() / 10, 0);
			gl.glScaled(8.0d, 8.0d, 8.0d);
			GLUT glut = new GLUT();
			glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_10, "Loading...");
			gl.glScaled(0.5d, 0.5d, 0.5d);
			gl.glEnable(GL.GL_BLEND);
			gl.glColor4d(1, 1, 1, 1);
		}

	}

	public static class WordLayerObject extends StaticLayerObject {

		private final double startTime;
		private int frameCount = 0;
		private double currentTime = 0;
		private double previousTime = 0;
		public float fps = 00.00f;
		public boolean axesDrawn = false;

		public WordLayerObject(final JOGLRenderer renderer) {
			super(renderer, 0);
			startTime = System.currentTimeMillis();
			setTrace(0);
			setFading(false);
			setAlpha(WORLD_ALPHA);
			setOffset(WORLD_OFFSET);
			setScale(WORLD_SCALE);
		}

		public void computeFrameRate() {
			frameCount++;
			currentTime = System.currentTimeMillis() - startTime;
			int timeInterval = (int) (currentTime - previousTime);
			if ( timeInterval > 1000 ) {
				fps = frameCount / (timeInterval / 1000.0f);
				previousTime = currentTime;
				frameCount = 0;
			}
		}

		@Override
		public void draw(final GL2 gl, final JOGLRenderer renderer, final boolean picking) {
			super.draw(gl, renderer, picking);
			if ( renderer.data.isDrawEnv() && !axesDrawn ) {
				drawAxes(renderer.data.getEnvWidth(), renderer.data.getEnvHeight());
				axesDrawn = true;
			}
			// GL2 gl = GLContext.getCurrentGL().getGL2();

			if ( renderer.data.isShowfps() ) {
				computeFrameRate();
				gl.glDisable(GL.GL_BLEND);
				// renderer.getContext().makeCurrent();
				gl.glColor4d(0.0, 0.0, 0.0, 1.0d);
				gl.glRasterPos3d(-renderer.getWidth() / 10, renderer.getHeight() / 10, 0);
				gl.glScaled(8.0d, 8.0d, 8.0d);
				GLUT glut = new GLUT();
				glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_10, "fps : " + fps);
				gl.glScaled(0.125d, 0.125d, 0.125d);
				gl.glEnable(GL.GL_BLEND);
			}
			gl.glColor4d(1, 1, 1, 1);
		}

		public void drawAxes(final double w, final double h) {
			double size = (w > h ? w : h) / 10;
			// add the world
			// Geometry g = GamaGeometryType.buildRectangle(w, h, new GamaPoint(w / 2, h / 2)).getInnerGeometry();
			Color c = new Color(150, 150, 150);
			GamaPoint origin = new GamaPoint();
			Geometry g = GamaGeometryType.buildLine(origin, new GamaPoint(w, 0)).getInnerGeometry();
			addGeometry(g, null, c, false, c, false, null, 0, 0, false, IShape.Type.LINESTRING, null, null);
			g = GamaGeometryType.buildLine(new GamaPoint(w, 0), new GamaPoint(w, h)).getInnerGeometry();
			addGeometry(g, null, c, false, c, false, null, 0, 0, false, IShape.Type.LINESTRING, null, null);
			g = GamaGeometryType.buildLine(new GamaPoint(w, h), new GamaPoint(0, h)).getInnerGeometry();
			addGeometry(g, null, c, false, c, false, null, 0, 0, false, IShape.Type.LINESTRING, null, null);
			g = GamaGeometryType.buildLine(new GamaPoint(0, h), origin).getInnerGeometry();
			addGeometry(g, null, c, false, c, false, null, 0, 0, false, IShape.Type.LINESTRING, null, null);
			// addGeometry(g, GAMA.getSimulation().getAgent(), c, false, c, false, null, 0, size / 20, false,
			// IShape.Type.ENVIRONMENT, 0);
			// build the lines

			g = GamaGeometryType.buildLine(origin, new GamaPoint(size, 0, 0)).getInnerGeometry();
			addGeometry(g, null, Color.red, true, Color.red, false, null, 0, 0, false, IShape.Type.LINESTRING, null,
				null);
			g = GamaGeometryType.buildLine(origin, new GamaPoint(0, size, 0)).getInnerGeometry();
			addGeometry(g, null, Color.green, true, Color.green, false, null, 0, 0, false, IShape.Type.LINESTRING,
				null, null);
			g = GamaGeometryType.buildLine(origin, new GamaPoint(0, 0, size)).getInnerGeometry();
			addGeometry(g, null, Color.blue, true, Color.blue, false, null, 0, 0, false, IShape.Type.LINESTRING, null,
				null);
			// add the legends
			addString("X", new GamaPoint(1.2f * size, 0.0d, 0.0d), 12, 12d, Color.black, "Arial", Font.BOLD, 0d, false);
			addString("Y", new GamaPoint(0.0d, -1.2f * size, 0.0d), 12, 12d, Color.black, "Arial", Font.BOLD, 0d, false);
			addString("Z", new GamaPoint(0.0d, 0.0d, 1.2f * size), 12, 12d, Color.black, "Arial", Font.BOLD, 0d, false);
			// add the triangles
			g =
				GamaGeometryType.buildArrow(origin, new GamaPoint(size + size / 10, 0, 0), size / 4, size / 4, true)
					.getInnerGeometry();
			addGeometry(g, null, Color.red, true, Color.red, false, null, 0, 0, false, IShape.Type.POLYGON, null, null);
			g =
				GamaGeometryType.buildArrow(origin, new GamaPoint(0, size + size / 10, 0), size / 4, size / 4, true)
					.getInnerGeometry();
			addGeometry(g, null, Color.green, true, Color.green, false, null, 0, 0, false, IShape.Type.POLYGON, null,
				null);
			g =
				GamaGeometryType.buildArrow(origin, new GamaPoint(0, 0, size + size / 10), size / 4, size / 4, true)
					.getInnerGeometry();
			// FIXME See Issue 832: depth cannot be applied here.
			addGeometry(g, null, Color.blue, true, Color.blue, false, null, 0, 0, false, IShape.Type.POLYGON, null,
				null);

		}
	}

}