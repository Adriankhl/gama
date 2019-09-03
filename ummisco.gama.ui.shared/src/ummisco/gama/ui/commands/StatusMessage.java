/*******************************************************************************************************
 *
 * msi.gama.common.StatusMessage.java, in plugin msi.gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package ummisco.gama.ui.commands;

import msi.gama.common.interfaces.gui.IGui;
import msi.gama.runtime.IStatusMessage;
import msi.gama.util.GamaColor;

public class StatusMessage implements IStatusMessage {

	String message = "";
	protected int code = IGui.INFORM;
	protected String icon;

	public StatusMessage(final String msg, final int s) {
		message = msg;
		code = s;
	}

	public StatusMessage(final String msg, final int s, final String icon) {
		message = msg;
		this.icon = icon;
		code = s;
	}

	@Override
	public String getText() {
		return message;
	}

	@Override
	public int getCode() {
		return code;
	}

	/**
	 * Method getColor()
	 * 
	 * @see msi.gama.runtime.IStatusMessage#getColor()
	 */
	@Override
	public GamaColor getColor() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see msi.gama.common.IStatusMessage#getIcon()
	 */
	@Override
	public String getIcon() {
		return icon;
	}

}