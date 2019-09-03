/*******************************************************************************************************
 *
 * msi.gama.util.file.GamaGeometryFile.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling
 * and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.extensions.files;

import msi.gama.common.geometry.AxisAngle;
import msi.gama.common.geometry.Envelope3D;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.runtime.scope.IScope;
import msi.gama.util.file.GamaFile;
import msi.gama.util.file.IGamaFile;
import msi.gama.util.list.IList;
import msi.gaml.types.IContainerType;
import msi.gaml.types.Types;

/**
 * Class GamaGeometryFile. An abstract class that supports loading and saving geometries in specific subclasses. The
 * buffer is a GamaList of points (GamaPoint) from which the GamaGeometry can be constructed (using
 * geometry(file("..."));)
 *
 * @author drogoul
 * @since 30 déc. 2013
 *
 */
public abstract class GamaGeometryFile extends GamaFile<IList<IShape>, IShape> implements IGamaFile.Geom {

	protected IShape geometry;

	public GamaGeometryFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	@Override
	public IContainerType<?> getGamlType() {
		return Types.FILE.of(Types.INT, Types.GEOMETRY);
	}

	/**
	 * Method computeEnvelope()
	 *
	 * @see msi.gama.util.file.IGamaFile#computeEnvelope(msi.gama.runtime.scope.IScope)
	 */
	@Override
	public Envelope3D computeEnvelope(final IScope scope) {
		return getGeometry(scope).getEnvelope();
	}

	@Override
	public IShape getGeometry(final IScope scope) {
		fillBuffer(scope);
		if (geometry == null) {
			geometry = buildGeometry(scope);
		}
		return geometry;
	}

	protected abstract IShape buildGeometry(IScope scope);

	@Override
	public void invalidateContents() {
		super.invalidateContents();
		geometry = null;
	}

	@Override
	public AxisAngle getInitRotation() {
		return null;
	}

	@Override
	public boolean is2D() {
		return true;
	}

}
