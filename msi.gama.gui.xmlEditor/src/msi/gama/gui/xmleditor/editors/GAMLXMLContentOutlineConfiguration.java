/*
 * GAMA - V1.4  http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC 
 * 
 * Developers :
 * 
 * - Alexis Drogoul, IRD (Kernel, Metamodel, XML-based GAML), 2007-2011
 * - Vo Duc An, IRD & AUF (SWT integration, multi-level architecture), 2008-2011
 * - Patrick Taillandier, AUF & CNRS (batch framework, GeoTools & JTS integration), 2009-2011
 * - Pierrick Koch, IRD (XText-based GAML environment), 2010-2011
 * - Romain Lavaud, IRD (project-based environment), 2010
 * - Francois Sempe, IRD & AUF (EMF behavioral model, batch framework), 2007-2009
 * - Edouard Amouroux, IRD (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, IRD (OpenMap integration), 2007-2008
 */

package msi.gama.gui.xmleditor.editors;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.xml.ui.views.contentoutline.XMLContentOutlineConfiguration;

/**
 * Written by drogoul
 * Modified on 12 déc. 2009
 *
 * @todo Description
 *
 */
public class GAMLXMLContentOutlineConfiguration extends XMLContentOutlineConfiguration {

	private ILabelProvider labelProvider;

	@Override
	public ILabelProvider getLabelProvider(final TreeViewer viewer) {
		viewer.getControl().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		if (labelProvider == null) {
			labelProvider = new GAMLXMLOutlineLabelProvider();
		}
		return labelProvider;
	}


}
