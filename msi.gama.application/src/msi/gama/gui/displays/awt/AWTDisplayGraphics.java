/*
 * GAMA - V1.4 http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */

package msi.gama.gui.displays.awt;

import static java.awt.RenderingHints.*;
import java.awt.*;
import java.awt.Point;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import msi.gama.common.interfaces.IGraphics;
import msi.gama.common.util.GuiUtils;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaShape;
import msi.gama.metamodel.topology.ITopology;
import msi.gama.runtime.IScope;
import msi.gaml.operators.Maths;
import org.jfree.chart.JFreeChart;
import com.vividsolutions.jts.awt.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.index.quadtree.IntervalSize;

/**
 * 
 * Simplifies the drawing of circles, rectangles, and so forth. Rectangles are generally faster to
 * draw than circles. The Displays should take care of layouts while objects that wish to be drawn
 * as a shape need only call the appropriate method.
 * <p>
 * 
 * @author Nick Collier, Alexis Drogoul, Patrick Taillandier
 * @version $Revision: 1.13 $ $Date: 2010-03-19 07:12:24 $
 */

public class AWTDisplayGraphics implements IGraphics {

	int[] highlightColor = GuiUtils.defaultHighlight;
	boolean ready = false;
	private Graphics2D g2;
	private Rectangle clipping;
	private final Rectangle2D rect = new Rectangle2D.Double(0, 0, 1, 1);
	private final Ellipse2D oval = new Ellipse2D.Double(0, 0, 1, 1);
	private final Line2D line = new Line2D.Double();
	private double currentAlpha = 1;
	private int displayWidth, displayHeight, curX = 0, curY = 0, curWidth = 5, curHeight = 5, offsetX = 0, offsetY = 0;
	private double currentXScale = 1, currentYScale = 1;
	// private static RenderingHints rendering;
	private static final Font defaultFont = new Font("Helvetica", Font.PLAIN, 12);

	static {

		// System.setProperty("sun.java2d.ddscale", "true");
		// System.setProperty("sun.java2d.accthreshold", "0");
		// System.setProperty("sun.java2d.allowrastersteal", "true");
		// System.setProperty("sun.java2d.opengl", "true");
		// System.setProperty("apple.awt.graphics.UseQuartz", "true");
		QUALITY_RENDERING.put(KEY_RENDERING, VALUE_RENDER_QUALITY);
		QUALITY_RENDERING.put(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY);
		QUALITY_RENDERING.put(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
		QUALITY_RENDERING.put(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
		QUALITY_RENDERING.put(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);

		MEDIUM_RENDERING.put(KEY_RENDERING, VALUE_RENDER_QUALITY);
		MEDIUM_RENDERING.put(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_SPEED);
		MEDIUM_RENDERING.put(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
		MEDIUM_RENDERING.put(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
		MEDIUM_RENDERING.put(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);

		SPEED_RENDERING.put(KEY_RENDERING, VALUE_RENDER_SPEED);
		SPEED_RENDERING.put(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_SPEED);
		SPEED_RENDERING.put(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_SPEED);
		SPEED_RENDERING.put(KEY_INTERPOLATION, VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		SPEED_RENDERING.put(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);

	}

	private final PointTransformation pt = new PointTransformation() {

		@Override
		public void transform(final Coordinate c, final Point2D p) {
			int xp = offsetX + (int) (currentXScale * c.x + 0.5);
			int yp = offsetY + (int) (currentYScale * c.y + 0.5);
			p.setLocation(xp, yp);
		}
	};
	private final ShapeWriter sw = new ShapeWriter(pt);

	public AWTDisplayGraphics(final BufferedImage image) {
		this(image.getWidth(), image.getHeight());
		setGraphics((Graphics2D) image.getGraphics());
	}

	/**
	 * Constructor for DisplayGraphics.
	 * @param width int
	 * @param height int
	 */
	public AWTDisplayGraphics(final int width, final int height) {
		setDisplayDimensions(width, height);
	}

	/**
	 * Method setGraphics.
	 * @param g Graphics2D
	 */
	@Override
	public void setGraphics(final Graphics2D g) {
		ready = true;
		g2 = g;
		setQualityRendering(false);
		g2.setFont(defaultFont);
	}

	@Override
	public void setQualityRendering(final boolean quality) {
		if ( g2 != null ) {
			g2.setRenderingHints(quality ? QUALITY_RENDERING : SPEED_RENDERING);
		}
	}

	@Override
	public boolean isReady() {
		return ready;
	}

	/**
	 * Method setComposite.
	 * @param alpha AlphaComposite
	 */
	@Override
	public void setOpacity(final double alpha) {
		// 1 means opaque ; 0 means transparent
		if ( IntervalSize.isZeroWidth(alpha, currentAlpha) ) { return; }
		currentAlpha = alpha;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
	}

	/**
	 * Method getDisplayWidth.
	 * @return int
	 */
	@Override
	public int getDisplayWidth() {
		return displayWidth;
	}

	/**
	 * Method getDisplayHeight.
	 * @return int
	 */
	@Override
	public int getDisplayHeight() {
		return displayHeight;
	}

	/**
	 * Method setDisplayDimensions.
	 * @param width int
	 * @param height int
	 */
	@Override
	public void setDisplayDimensions(final int width, final int height) {
		displayWidth = width;
		displayHeight = height;
	}

	/**
	 * Method setFont.
	 * @param font Font
	 */
	@Override
	public void setFont(final Font font) {
		g2.setFont(font);
	}

	/**
	 * Method getXScale.
	 * @return double
	 */
	@Override
	public double getXScale() {
		return currentXScale;
	}

	/**
	 * Method setXScale.
	 * @param scale double
	 */
	@Override
	public void setXScale(final double scale) {
		this.currentXScale = scale;
	}

	/**
	 * Method getYScale.
	 * @return double
	 */
	@Override
	public double getYScale() {
		return currentYScale;
	}

	/**
	 * Method setYScale.
	 * @param scale double
	 */
	@Override
	public void setYScale(final double scale) {
		this.currentYScale = scale;
	}

	/**
	 * Method setDrawingCoordinates.
	 * @param x double
	 * @param y double
	 */
	@Override
	public void setDrawingCoordinates(final double x, final double y) {
		curX = (int) x + offsetX;
		curY = (int) y + offsetY;
	}

	/**
	 * Method setDrawingOffset.
	 * @param x int
	 * @param y int
	 */
	@Override
	public void setDrawingOffset(final int x, final int y) {
		offsetX = x;
		offsetY = y;
	}

	/**
	 * Method setDrawingDimensions.
	 * @param width int
	 * @param height int
	 */
	@Override
	public void setDrawingDimensions(final int width, final int height) {
		curWidth = width;
		curHeight = height;
	}

	/**
	 * Method setDrawingColor.
	 * @param c Color
	 */
	private void setDrawingColor(final Color c) {
		if ( g2 != null && g2.getColor() != c ) {
			g2.setColor(c);
		}
	}

	// private final AffineTransform at = new AffineTransform();

	/**
	 * Method drawImage.
	 * @param img Image
	 * @param angle Integer
	 * @param smooth boolean
	 * @param name String
	 * @param z float (has no effet in java 2D)
	 */
	@Override
	public Rectangle2D drawImage(final IScope scope, final BufferedImage img, final Integer angle,
		final boolean smooth, final String name, final float z) {
		AffineTransform saved = g2.getTransform();
		// RenderingHints hints = g2.getRenderingHints();
		if ( angle != null ) {
			g2.rotate(Maths.toRad * angle, curX + curWidth / 2, curY + curHeight / 2);
		}
		// if ( !smooth ) {
		// g2.setRenderingHints(SPEED_RENDERING);
		// }
		g2.drawImage(img, curX, curY, curWidth, curHeight, null);
		// g2.setRenderingHints(hints);
		g2.setTransform(saved);
		rect.setRect(curX, curY, curWidth, curHeight);
		return rect.getBounds2D();
	}

	@Override
	public Rectangle2D drawImage(final IScope scope, final BufferedImage img, final Integer angle, final String name,
		final float z) {
		return drawImage(scope, img, angle, true, name, z);
	}

	/**
	 * Method drawChart.
	 * @param chart JFreeChart
	 */
	@Override
	public Rectangle2D drawChart(final JFreeChart chart) {
		rect.setRect(curX, curY, curWidth, curHeight);
		// drawImage(chart.createBufferedImage(curWidth, curHeight), null);
		Graphics2D g3 = (Graphics2D) g2.create();
		chart.draw(g3, rect);
		g3.dispose();
		return rect.getBounds2D();
	}

	/**
	 * Method drawCircle.
	 * @param c Color
	 * @param fill boolean
	 * @param angle Integer
	 * @param height height of the rectangle but only used in opengl display
	 */
	@Override
	public Rectangle2D drawCircle(final IScope scope, final Color c, final boolean fill, final Color border,
		final Integer angle, final float height) {
		oval.setFrame(curX, curY, curWidth, curWidth);
		return drawShape(c, oval, fill, border, angle);
	}

	/**
	 * Method drawTriangle.
	 * @param c Color
	 * @param fill boolean
	 * @param angle Integer
	 * @param height height of the rectangle but only used in opengl display
	 */
	@Override
	public Rectangle2D drawTriangle(final IScope scope, final Color c, final boolean fill, final Color border,
		final Integer angle, final float height) {
		// curWidth is equal to half the width of the triangle
		final GeneralPath p0 = new GeneralPath();
		// double dist = curWidth / (2 * Math.sqrt(2.0));
		p0.moveTo(curX, curY + curWidth);
		p0.lineTo(curX + curWidth / 2.0, curY);
		p0.lineTo(curX + curWidth, curY + curWidth);
		p0.closePath();
		return drawShape(c, p0, fill, border, angle);
	}

	/**
	 * Method drawLine.
	 * @param c Color
	 * @param toX double
	 * @param toY double
	 */
	@Override
	public Rectangle2D drawLine(final Color c, final double toX, final double toY) {
		line.setLine(curX, curY, toX + offsetX, toY + offsetY);
		return drawShape(c, line, false, null, null);
	}

	/**
	 * Method drawRectangle.
	 * @param color Color
	 * @param fill boolean
	 * @param angle Integer
	 * @param height height of the rectangle but only used in opengl display
	 */
	@Override
	public Rectangle2D drawRectangle(final IScope scope, final Color color, final boolean fill, final Color border,
		final Integer angle, final float height) {
		rect.setFrame(curX, curY, curWidth, curHeight);
		return drawShape(color, rect, fill, border, angle);
	}

	/**
	 * Method drawString.
	 * @param string String
	 * @param stringColor Color
	 * @param angle Integer
	 * @param z float (has no effect in 2D)
	 */
	@Override
	public Rectangle2D drawString(final IAgent agent, final String string, final Color stringColor,
		final Integer angle, final float z) {
		setDrawingColor(stringColor);
		AffineTransform saved = g2.getTransform();
		if ( angle != null ) {
			Rectangle2D r = g2.getFontMetrics().getStringBounds(string, g2);
			g2.rotate(Maths.toRad * angle, curX + r.getWidth() / 2, curY + r.getHeight() / 2);
		}
		g2.drawString(string, curX, curY);
		g2.setTransform(saved);
		return g2.getFontMetrics().getStringBounds(string, g2);
	}

	/**
	 * Method drawGeometry.
	 * @param geometry Geometry
	 * @param color Color
	 * @param fill boolean
	 * @param angle Integer
	 * @param rounded boolean (not yet implemented in JAVA 2D)
	 */
	@Override
	public Rectangle2D drawGeometry(final IScope scope, final Geometry geometry, final Color color, final boolean fill,
		final Color border, final Integer angle, final boolean rounded) {
		Geometry geom = null;
		ITopology topo = scope.getTopology();
		if ( topo != null && topo.isTorus() ) {
			geom = topo.returnToroidalGeom(geometry);
		} else {
			geom = geometry;
		}
		boolean f = geom instanceof LineString || geom instanceof MultiLineString ? false : fill;
		return drawShape(color, sw.toShape(geom), f, border, angle);
	}

	/**
	 * Method drawGeometry.
	 * @param geometry GamaShape
	 * @param color Color
	 * @param fill boolean
	 * @param angle Integer
	 * @param rounded boolean (not yet implemented in JAVA 2D)
	 */
	@Override
	public Rectangle2D drawGamaShape(final IScope scope, final GamaShape geometry, final Color color,
		final boolean fill, final Color border, final Integer angle, final boolean rounded) {
		Geometry geom = null;
		if ( geometry == null ) { return null; }
		ITopology topo = scope.getTopology();
		// Necessary to check in case the scope has been erased (in cases of reload)
		if ( topo != null && topo.isTorus() ) {
			geom = topo.returnToroidalGeom(geometry.getInnerGeometry());
		} else {
			geom = geometry.getInnerGeometry();
		}
		boolean f = geom instanceof LineString || geom instanceof MultiLineString ? false : fill;
		return drawShape(color, sw.toShape(geom), f, border, angle);
	}

	/**
	 * Method drawShape.
	 * @param c Color
	 * @param s Shape
	 * @param fill boolean
	 * @param angle Integer
	 */
	public Rectangle2D drawShape(final Color c, final Shape s, final boolean fill, final Color border,
		final Integer angle) {
		try {
			Rectangle2D r = s.getBounds2D();
			AffineTransform saved = g2.getTransform();
			if ( angle != null ) {
				g2.rotate(Maths.toRad * angle, r.getX() + r.getWidth() / 2, r.getY() + r.getHeight() / 2);
			}
			setDrawingColor(c);
			if ( fill ) {
				g2.fill(s);
				setDrawingColor(border);
			}
			g2.draw(s);
			g2.setTransform(saved);
			return r;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void fill(final Color bgColor, final double opacity) {
		setOpacity(opacity);
		g2.setColor(bgColor);
		g2.fillRect(0, 0, displayWidth, displayHeight);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see msi.gama.gui.graphics.IGraphics#setClipping(java.awt.Rectangle)
	 */
	@Override
	public void setClipping(final Rectangle imageClipBounds) {
		clipping = imageClipBounds;
		g2.setClip(imageClipBounds);
	}

	@Override
	public Rectangle getClipping() {
		return clipping;
	}

	@Override
	public void drawGrid(final BufferedImage image, final Color lineColor, final Point displaySize) {
		// The image contains the dimensions of the grid.
		double stepx = (double) displaySize.x / (double) image.getWidth();
		for ( double step = 0.0, end = displaySize.x; step < end + 1; step += stepx ) {
			this.setDrawingCoordinates(step, 0);
			this.drawLine(lineColor, step, displaySize.y);
		}
		setDrawingCoordinates(displaySize.x - 1, 0);
		drawLine(lineColor, displaySize.x - 1, displaySize.y - 1);
		double stepy = (double) displaySize.y / (double) image.getHeight();
		for ( double step = 0.0, end = displaySize.y; step < end + 1; step += stepy ) {
			setDrawingCoordinates(0, step);
			drawLine(lineColor, displaySize.x, step);
		}
		setDrawingCoordinates(0, displaySize.y - 1);
		drawLine(lineColor, displaySize.x - 1, displaySize.y - 1);

	}

	@Override
	public int[] getHighlightColor() {
		return highlightColor;
	}

	@Override
	public void setHighlightColor(final int[] rgb) {
		highlightColor = rgb;
	}

	@Override
	public void highlight(final Rectangle2D r) {
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(5));
		Color old = g2.getColor();
		g2.setColor(new Color(highlightColor[0], highlightColor[1], highlightColor[2]));
		g2.draw(r);
		g2.setStroke(oldStroke);
		g2.setColor(old);
	}

	@Override
	/**
	 * Not use in Java2D
	 */
	public void initLayers() {}

	@Override
	/**
	 * Not use in Java2D
	 */
	public void newLayer(final double zLayerValue, final Boolean refresh) {}

	//
	// @Override
	// public boolean isOpenGL() {
	// return false;
	// }

}
