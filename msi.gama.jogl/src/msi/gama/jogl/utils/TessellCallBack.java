package msi.gama.jogl.utils;

import javax.media.opengl.GL;
import javax.media.opengl.glu.*;

public class TessellCallBack implements GLUtessellatorCallback {

	private final GL gl;
	private final GLU glu;

	public TessellCallBack(final GL gl, final GLU glu) {
		this.gl = gl;
		this.glu = glu;
	}

	@Override
	public void begin(final int type) {
		gl.glBegin(type);
	}

	@Override
	public void end() {
		gl.glEnd();
	}

	@Override
	public void vertex(final Object vertexData) {
		double[] pointer;
		if ( vertexData instanceof double[] ) {
			pointer = (double[]) vertexData;
			if ( pointer.length == 6 ) {
				gl.glColor3dv(pointer, 3);
			}
			gl.glVertex3dv(pointer, 0);
		}

	}

	@Override
	public void vertexData(final Object vertexData, final Object polygonData) {}

	/*
	 * combineCallback is used to create a new vertex when edges intersect.
	 * coordinate location is trivial to calculate, but weight[4] may be
	 * used to average color, normal, or texture coordinate data. In this
	 * program, color is weighted.
	 */
	@Override
	public void combine(final double[] coords, final Object[] data, //
		final float[] weight, final Object[] outData) {
		double[] vertex = new double[6];
		int i;

		vertex[0] = coords[0];
		vertex[1] = coords[1];
		vertex[2] = coords[2];
		for ( i = 3; i < 6/* 7OutOfBounds from C! */; i++ ) {
			vertex[i] =
				weight[0] //
					*
					((double[]) data[0])[i] + weight[1] * ((double[]) data[1])[i] +
					weight[2] *
					((double[]) data[2])[i] + weight[3] * ((double[]) data[3])[i];
		}
		outData[0] = vertex;
	}

	@Override
	public void combineData(final double[] coords, final Object[] data, //
		final float[] weight, final Object[] outData, final Object polygonData) {}

	@Override
	public void error(final int errnum) {
		String estring;

		estring = glu.gluErrorString(errnum);
		System.err.println("Tessellation Error: " + estring);
		System.exit(0);
	}

	@Override
	public void beginData(final int type, final Object polygonData) {}

	@Override
	public void endData(final Object polygonData) {}

	@Override
	public void edgeFlag(final boolean boundaryEdge) {}

	@Override
	public void edgeFlagData(final boolean boundaryEdge, final Object polygonData) {}

	@Override
	public void errorData(final int errnum, final Object polygonData) {}
}// tessellCallBack