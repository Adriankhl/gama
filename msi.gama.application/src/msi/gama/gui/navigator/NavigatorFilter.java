package msi.gama.gui.navigator;

import msi.gama.gui.navigator.shapefiles.LightweightShapefileSupportDecorator;
import org.eclipse.core.resources.*;
import org.eclipse.jface.viewers.*;

public class NavigatorFilter extends ViewerFilter {

	public NavigatorFilter() {}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if ( parentElement instanceof TreePath && element instanceof IFile ) {
			TreePath p = (TreePath) parentElement;
			if ( p.getLastSegment() instanceof IFolder ) {
				IResource r = LightweightShapefileSupportDecorator.shapeFileSupportedBy((IFile) element);
				if ( r != null ) {
					System.out.println("Filtering out " + element);
					return false;
				}
			}
		}
		return true;
	}

}
