/*********************************************************************************************
 * 
 * 
 * 'AWTDisplayView.java', in plugin 'msi.gama.application', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.gui.views;

import javax.swing.JComponent;
import msi.gama.common.GamaPreferences;
import msi.gama.common.util.GuiUtils;
import msi.gama.gui.displays.awt.DisplaySurfaceMenu;
import msi.gama.gui.swt.SwtGui;
import msi.gama.gui.swt.perspectives.ModelingPerspective;
import msi.gama.gui.swt.swing.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;

public class AWTDisplayView extends LayeredDisplayView implements ISizeProvider {

	public static final String ID = GuiUtils.LAYER_VIEW_ID;

	@Override
	protected Composite createSurfaceComposite() {

		final Runnable displayOverlay = new Runnable() {

			@Override
			public void run() {
				if ( overlay.isHidden() ) { return; }
				overlay.update();
			}
		};

		final java.awt.event.MouseMotionListener mlAwt2 = new java.awt.event.MouseMotionAdapter() {

			@Override
			public void mouseMoved(final java.awt.event.MouseEvent e) {
				GuiUtils.asyncRun(displayOverlay);
			}

			@Override
			public void mouseDragged(final java.awt.event.MouseEvent e) {
				GuiUtils.asyncRun(displayOverlay);
			}
		};

		final boolean isOpenGL = getOutput().isOpenGL();
		final String outputName = getOutput().getName();

		OutputSynchronizer.incInitializingViews(outputName, getOutput().isPermanent()); // incremented in the SWT thread
		surfaceComposite = new SwingControl(parent, SWT.NONE) {

			@Override
			protected JComponent createSwingComponent() {
				final JComponent frameAwt = (JComponent) getOutput().getSurface();
				frameAwt.addMouseMotionListener(mlAwt2);
				return frameAwt;
			}

			@Override
			public Composite getLayoutAncestor() {
				// Seems necessary to return null for OpenGL displays to show up and call init on the
				// renderer
				return null;
			}

			@Override
			public boolean isSwtTabOrderExtended() {
				return false;
			}

			@Override
			public boolean isAWTPermanentFocusLossForced() {
				return false;
			}

			@Override
			public void afterComponentCreatedSWTThread() {
				if ( GamaPreferences.CORE_OVERLAY.getValue() ) {
					overlay.setHidden(false);
				}
			}

			@Override
			public void afterComponentCreatedAWTThread() {
				if ( !isOpenGL ) {
					// Deferred to the OpenGL renderer to signify its initialization
					// see JOGLAWTGLRendered.init()
					OutputSynchronizer.decInitializingViews(outputName);
				}
				new DisplaySurfaceMenu(getOutput().getSurface(), surfaceComposite, AWTDisplayView.this);
			}
		};

		perspectiveListener = new IPerspectiveListener() {

			boolean previousState = false;

			@Override
			public void perspectiveChanged(final IWorkbenchPage page, final IPerspectiveDescriptor perspective,
				final String changeId) {}

			@Override
			public void perspectiveActivated(final IWorkbenchPage page, final IPerspectiveDescriptor perspective) {
				if ( perspective.getId().equals(ModelingPerspective.ID) ) {
					if ( getOutput() != null && getOutput().getSurface() != null ) {
						previousState = getOutput().isPaused();
						getOutput().setPaused(true);
					}
					if ( overlay != null ) {
						overlay.hide();
					}
				} else {
					if ( getOutput() != null && getOutput().getSurface() != null ) {
						getOutput().setPaused(previousState);
					}
					if ( overlay != null ) {
						overlay.update();
					}
				}
			}
		};

		SwtGui.getWindow().addPerspectiveListener(perspectiveListener);
		return surfaceComposite;
	}

	@Override
	public void fixSize() {

		// AD: Reworked to address Issue 535. It seems necessary to read the size of the composite inside an SWT
		// thread and run the sizing inside an AWT thread
		OutputSynchronizer.cleanResize(new Runnable() {

			@Override
			public void run() {
				final Rectangle r = parent.getBounds();

				java.awt.EventQueue.invokeLater(new Runnable() {

					@Override
					public void run() {
						((SwingControl) surfaceComposite).getFrame().setBounds(r.x, r.y, r.width, r.height);
						getOutput().getSurface().resizeImage(r.width, r.height, false);
						getOutput().getSurface().updateDisplay(true);

						GuiUtils.run(new Runnable() {

							@Override
							public void run() {
								parent.layout(true, true);
							}
						});
					}
				});

			}

		});
	}

	@Override
	public int getSizeFlags(final boolean width) {
		return SWT.MIN;
	}

	@Override
	public int computePreferredSize(final boolean width, final int availableParallel, final int availablePerpendicular,
		final int preferredResult) {
		return 400;
	}
}