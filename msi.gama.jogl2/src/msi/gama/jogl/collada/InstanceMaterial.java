/*********************************************************************************************
 * 
 *
 * 'InstanceMaterial.java', in plugin 'msi.gama.jogl2', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.jogl.collada;

import java.util.ArrayList;

public class InstanceMaterial {
	
	private ArrayList<BindVertexInput> m_bindVertexInputs = new ArrayList<BindVertexInput> ();
	private String m_symbol = null;
	private String m_target = null;
	
	public ArrayList<BindVertexInput> getBindVertexInput() {
		return m_bindVertexInputs;
	}
	public void setBindVertexInput(ArrayList<BindVertexInput> m_bindVertexInput) {
		this.m_bindVertexInputs = m_bindVertexInput;
	}
	public String getSymbol() {
		return m_symbol;
	}
	public void setSymbol(String m_symbol) {
		this.m_symbol = m_symbol;
	}
	public String getTarget() {
		return m_target;
	}
	public void setTarget(String m_target) {
		this.m_target = m_target;
	}
	
}
