/*******************************************************************************************************
 *
 * msi.gama.common.interfaces.IOverlayProvider.java, in plugin msi.gama.core, is part of the source code of the GAMA
 * modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.common.interfaces.outputs;

import msi.gama.runtime.IUpdaterTarget;

/**
 * Class IOverlay.
 *
 * @author drogoul
 * @since 9 mars 2014
 *
 */
public interface IOverlayProvider<C extends IOverlayInfo> {

	void setTarget(IUpdaterTarget<C> overlay, IDisplaySurface surface);
}
