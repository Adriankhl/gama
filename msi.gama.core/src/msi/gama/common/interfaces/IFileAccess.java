/**
 * Created by drogoul, 20 d�c. 2011
 * 
 */
package msi.gama.common.interfaces;

import msi.gama.precompiler.GamlProperties;
import msi.gama.runtime.exceptions.GamaStartupException;
import org.osgi.framework.Bundle;

/**
 * The class IFileAccess.
 * 
 * @author drogoul
 * @since 20 d�c. 2011
 * 
 */
public interface IFileAccess {

	GamlProperties getGamaProperties(final Bundle plugin, final String pathToAdditions,
		final String fileName) throws GamaStartupException;

}
