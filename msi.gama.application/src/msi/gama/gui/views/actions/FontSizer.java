/**
 * Created by drogoul, 9 févr. 2015
 * 
 */
package msi.gama.gui.views.actions;

import msi.gama.gui.swt.SwtGui;
import msi.gama.gui.swt.controls.GamaToolbar;
import msi.gama.gui.views.IToolbarDecoratedView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Control;

/**
 * Class FontSizer.
 * 
 * @author drogoul
 * @since 9 févr. 2015
 * 
 */
public class FontSizer {

	IToolbarDecoratedView.Sizable view;
	Font currentFont;

	private final GestureListener gl = new GestureListener() {

		@Override
		public void gesture(final GestureEvent ge) {
			if ( ge.detail == SWT.GESTURE_MAGNIFY ) {
				changeFontSize((int) (2 * Math.signum(ge.magnification - 1.0)));
			}
		}
	};

	public FontSizer(final IToolbarDecoratedView.Sizable view) {
		// We add a control listener to the toolbar in order to install the gesture once the control to resize have been created.
		this.view = view;
	}

	public void install(final GamaToolbar tb) {
		// We add a control listener to the toolbar in order to install the gesture once the control to resize have been created.
		tb.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(final ControlEvent e) {
				Control c = view.getSizableFontControl();
				if ( c != null ) {
					c.addGestureListener(gl);
					// once installed the listener removes itself from the toolbar
					tb.removeControlListener(this);
				}
			}

		});
		tb.button("console.increase2", "Increase font size", "Increase font size", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent arg0) {
				changeFontSize(2);
			}

		});
		tb.button("console.decrease2", "Decrease font size", "Decrease font size", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent arg0) {
				changeFontSize(-2);
			}
		});

		tb.sep(16);

	}

	private void changeFontSize(final int delta) {
		Control c = view.getSizableFontControl();
		if ( c != null ) {
			FontData data = c.getFont().getFontData()[0];
			data.height += delta;
			if ( data.height < 6 || data.height > 256 ) { return; }
			Font oldFont = currentFont;
			currentFont = new Font(SwtGui.getDisplay(), data);
			c.setFont(currentFont);
			if ( oldFont != null && !oldFont.isDisposed() ) {
				oldFont.dispose();
			}
		}
	}

}
