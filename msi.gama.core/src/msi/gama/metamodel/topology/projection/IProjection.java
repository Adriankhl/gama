/*********************************************************************************************
 * 
 *
 * 'IProjection.java', in plugin 'msi.gama.core', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.metamodel.topology.projection;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import com.vividsolutions.jts.geom.*;

/**
 * Class IProjection.
 * 
 * @author drogoul
 * @since 17 déc. 2013
 * 
 */
public interface IProjection {

	public abstract void createTransformation(final MathTransform t);

	public abstract Geometry transform(final Geometry g);

	public abstract Geometry inverseTransform(final Geometry g);

	public abstract CoordinateReferenceSystem getInitialCRS();

	public abstract CoordinateReferenceSystem getTargetCRS();

	public abstract Envelope getProjectedEnvelope();

	/**
	 * @param geom
	 */
	public abstract void translate(Geometry geom);

	public abstract void inverseTranslate(Geometry geom);

}