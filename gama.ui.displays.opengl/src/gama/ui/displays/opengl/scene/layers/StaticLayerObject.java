/*******************************************************************************************************
 *
 * gama.ui.displays.opengl.scene.layers.StaticLayerObject.java, in plugin gama.ui.displays.opengl, is part of the source code of
 * the GAMA modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.ui.displays.opengl.scene.layers;

import java.util.Collection;

import gama.ui.displays.opengl.OpenGL;
import gama.ui.displays.opengl.renderer.IOpenGLRenderer;
import gama.ui.displays.opengl.scene.AbstractObject;

public class StaticLayerObject extends LayerObject {

	public static abstract class World extends StaticLayerObject {

		public World(final IOpenGLRenderer renderer) {
			super(renderer);
		}

		@Override
		public boolean canSplit() {
			return false;
		}

		@Override
		public void computeScale() {}

		@Override
		public void computeOffset() {}

		@Override
		public boolean isLightInteraction() {
			return false;
		}

		@Override
		public void draw(final OpenGL gl) {
			if (renderer.getPickingHelper().isPicking()) { return; }

			if (currentList.isEmpty()) {
				fillWithObjects(currentList);
			}

			gl.suspendZTranslation();
			final boolean previous = gl.setLighting(false);
			super.draw(gl);
			gl.setLighting(previous);
			gl.resumeZTranslation();

		}

		@Override
		protected boolean isPickable() {
			return false;
		}

		public abstract void fillWithObjects(Collection<AbstractObject<?, ?>> list);
	}

	public StaticLayerObject(final IOpenGLRenderer renderer) {
		super(renderer, null);
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public void clear(final OpenGL gl) {}

}