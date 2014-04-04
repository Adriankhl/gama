/**
 * Created by drogoul, 12 déc. 2013
 * 
 */
package msi.gama.util.file;

import msi.gama.metamodel.shape.IShape;
import msi.gama.metamodel.topology.projection.IProjection;
import msi.gama.metamodel.topology.projection.ProjectionFactory;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.GamaGeometryType;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Class GamaGisFile.
 * 
 * @author drogoul
 * @since 12 déc. 2013
 * 
 */
public abstract class GamaGisFile extends GamaGeometryFile {

	// The code to force reading the GIS data as already projected
	public static final int ALREADY_PROJECTED_CODE = 0;
	protected IProjection gis;
	protected Integer initialCRSCode = null;
	protected String initialCRSCodeStr = null;

	// Faire les tests sur ALREADY_PROJECTED ET LE PASSER AUSSI A GIS UTILS ???

	/**
	 * Returns the CRS defined with this file (in a ".prj" file or elsewhere)
	 * @return
	 */
	protected CoordinateReferenceSystem getExistingCRS(final IScope scope) {
		if ( initialCRSCode != null ) {
			try {
				return scope.getSimulationScope().getProjectionFactory().getCRS(initialCRSCode);
			} catch (GamaRuntimeException e) {
				throw GamaRuntimeException.error("The code " + initialCRSCode +
					" does not correspond to a known EPSG code. GAMA is unable to load " + getPath(), scope);
			}
		} 
		if ( initialCRSCodeStr != null ) {
			try {
				return scope.getSimulationScope().getProjectionFactory().getCRS(initialCRSCodeStr);
			} catch (GamaRuntimeException e) {
				throw GamaRuntimeException.error("The code " + initialCRSCodeStr +
					" does not correspond to a known CRS code. GAMA is unable to load " + getPath(), scope);
			}
		} 
		CoordinateReferenceSystem crs = getOwnCRS();
		if ( crs == null ) {
			crs = scope.getSimulationScope().getProjectionFactory().getDefaultInitialCRS();
		}
		return crs;
	}

	/**
	 * @return
	 */
	protected abstract CoordinateReferenceSystem getOwnCRS();

	protected void computeProjection(final IScope scope, final Envelope env) {
		CoordinateReferenceSystem crs = getExistingCRS(scope);
		ProjectionFactory pf = scope.getSimulationScope().getProjectionFactory();
		gis = pf.fromCRS(crs, env);
	}

	public GamaGisFile(final IScope scope, final String pathName, final Integer code) {
		super(scope, pathName);
		initialCRSCode = code;
	}
	
	public GamaGisFile(final IScope scope, final String pathName, final String code) {
		super(scope, pathName);
		initialCRSCodeStr = code;
	}

	/**
	 * Method flushBuffer()
	 * @see msi.gama.util.file.GamaFile#flushBuffer()
	 */
	@Override
	protected void flushBuffer() throws GamaRuntimeException {
		// Not yet done for GIS files
	}

	public IProjection getGis(final IScope scope) {
		if ( gis == null ) {
			fillBuffer(scope);
		}
		return gis;
	}

	@Override
	protected IShape buildGeometry(final IScope scope) {
		return GamaGeometryType.geometriesToGeometry(scope, getBuffer());
	}

	@Override
	public void invalidateContents() {
		super.invalidateContents();
		gis = null;
		initialCRSCode = null;
		initialCRSCodeStr = null;
	}

}
