/*********************************************************************************************
 *
 * 'ConnectorMessage.java, in plugin ummisco.gama.extensions.network, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package ummisco.gama.extensions.network.common;

import msi.gama.extensions.messaging.GamaMessage;
import msi.gama.runtime.scope.IScope;

public interface ConnectorMessage {
	public String getSender();
	public String getReceiver();
	public String getPlainContents();
	public boolean isPlainMessage();
	public boolean isCommandMessage();
	public GamaMessage getContents(IScope scope);
}
