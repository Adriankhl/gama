/*******************************************************************************************************
 *
 * msi.gaml.types.GamaKmlExport.java, in plugin msi.gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package msi.gaml.types;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Link;
import de.micromata.opengis.kml.v_2_2_0.Location;
import de.micromata.opengis.kml.v_2_2_0.Model;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Orientation;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Scale;
import de.micromata.opengis.kml.v_2_2_0.Style;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaColor;
import msi.gama.util.GamaDate;
import msi.gaml.operators.Spatial;


public class GamaKmlExport{
	private Kml kml;
	private Document doc;
	private KmlFolder defolder; // Default folder in case we need one.
	private HashMap<String, KmlFolder> folders;
	

	public GamaKmlExport() {
		kml = new Kml();
		doc = kml.createAndSetDocument();
		folders = new HashMap<String, KmlFolder>();
	}
	
	public KmlFolder addFolder(String label, GamaDate beginDate, GamaDate endDate) {
		KmlFolder kf = new KmlFolder(doc, label, dateToKml(beginDate),
				dateToKml(endDate));
		folders.put(label, kf);
		return kf;
	}


	public void add3DModel(IScope scope, GamaPoint loc, double orientation,
			double scale, GamaDate beginDate, GamaDate endDate, String daefile) {
		getDefaultFolder().add3DModel(scope,loc, orientation, scale,
				dateToKml(beginDate), dateToKml(endDate), daefile);
	}

	
	public void addGeometry(IScope scope,String label, GamaDate beginDate, GamaDate endDate,
			IShape geom, String styleName, double height) {
		getDefaultFolder().addGeometry(scope, label, dateToKml(beginDate),
				dateToKml(endDate), geom, styleName, height);
	}



	/**
	 * Defines a new style to be used with addStyledRecord
	 * 
	 * @param name
	 *            Style name. Must be unique.
	 * @param lineWidth
	 *            Width of the line. A thin line should have a value 1.0.
	 * @param lineColor
	 *            Color of the line.
	 * @param fillColor
	 *            Polygon fill color.
	 */
	public void defStyle(String name, double lineWidth, GamaColor lineColor,
			GamaColor fillColor) {
		Style style = doc.createAndAddStyle().withId(name);
		style.createAndSetLineStyle().withColor(kmlColor(lineColor))
				.withWidth(lineWidth);
		style.createAndSetPolyStyle().withColor(kmlColor(fillColor))
				.withColorMode(ColorMode.NORMAL);
	}


	private static String toHex2Digit(int a) {
		String prefix = "";
		if (a % 256 < 16)
			prefix = "0";
		return prefix + Integer.toHexString(a % 256);
	}

	/**
	 * Produces a Kml String representation of the Color given in argument
	 * 
	 * @param c
	 *            A Color
	 * @return A String in ABGR Hex text format
	 */
	public String kmlColor(GamaColor c) {
		return toHex2Digit(c.alpha())+toHex2Digit(c.blue())+toHex2Digit(c.green())+toHex2Digit(c.red());
	}

	/**
	 * Defines a new IconStyle to be used with addStyledRecord
	 * 
	 * @param name
	 *            Style name. Must be unique.
	 * @param iconFile
	 *            Name of an image (png) file. The path is relative to the
	 *            location where the KML file will be saved.
	 * @param scale
	 *            Scale factor applied to the image. Choose 1.0 if you don't
	 *            know.
	 * @param heading
	 *            Orientation heading of the icon. Should be a value between 0.0
	 *            and 360.0. 0.0 is the normal orientation of the icon. Higher
	 *            numbers apply a clockwise rotation of the icon.
	 */

	public void defIconStyle(String name, String iconFile, double scale,
			double heading) {
		Style style = doc.createAndAddStyle().withId(name);
		IconStyle iconstyle = style.createAndSetIconStyle().withScale(scale)
				.withHeading(heading);
		iconstyle.createAndSetIcon().withHref(iconFile);
	}


	public void saveAsKml(IScope scope, String filename) {
		try {
			if (!filename.isEmpty())
				kml.marshal(new File(filename));
			else
				System.out
						.println("Failed to save the kml file : no valid file name was provided.");
		} catch (FileNotFoundException e) {
			GamaRuntimeException.error("Failed to open " + filename + " for saving to KML.", scope); 
		}
	}

	public void saveAsKmz(IScope scope, String filename) {
		try {
			if (!filename.isEmpty())
				kml.marshalAsKmz(filename);
			else
				System.out
						.println("Failed to save the kmz file : no valid file name was provided.");
		} catch (IOException e) {
			GamaRuntimeException.error("Failed to open " + filename + " for saving to KMZ.", scope); 
		}
	}


   public void hideFolder(String folname) {
	   KmlFolder kf = getFolder(folname);
	   kf.setVisibility(false);
   }	
	
   public void showFolder(String folname) {
	   KmlFolder kf = getFolder(folname);
	   kf.setVisibility(true);
   }	
	
	
	protected KmlFolder getDefaultFolder() {
		if (defolder == null) {
			defolder = new KmlFolder(doc, "kml");
			folders.put("kml", defolder);
		}
		return defolder;
	}

	protected KmlFolder getFolder(String folname) {
		KmlFolder kf = folders.get(folname);
		if (kf == null) {
			kf = new KmlFolder(doc, folname);
			folders.put(folname, kf);
		}
		return kf;

	}

	protected String dateToKml(GamaDate d) {
		return d.toISOString();
		//("yyyy-MM-dd") + "T" + d.toString("HH:mm:ss");
	}

	
	public void addLabel(IScope scope, GamaPoint loc, 
			GamaDate beginDate, GamaDate endDate, String name, String description,
			String styleName) {
		getDefaultFolder().addLabel(scope,loc, dateToKml(beginDate),
				dateToKml(endDate), name, description, styleName);
	}

	
	public void addLabel(IScope scope, String foldname, GamaPoint loc, GamaDate beginDate, GamaDate endDate, String name,
			String description, String styleName) {
		getFolder(foldname).addLabel(scope,loc, dateToKml(beginDate),
				dateToKml(endDate), name, description, styleName);
	}
	public class KmlFolder {
		public Folder fold;
		static final String ERR_HEADER = "Kml Export: ";

		public KmlFolder(Document doc, String label, String beginDate,
				String endDate) {
			this.fold = doc.createAndAddFolder();
			fold.withName(label).createAndSetTimeSpan().withBegin(beginDate)
					.withEnd(endDate);
		}

		public KmlFolder(Document doc, String label) {
			this.fold = doc.createAndAddFolder();
			fold.withName(label);
		}
		
		/**
		 * Adds a KML Extruded Label (see
		 * https://kml-samples.googlecode.com/svn/trunk
		 * /interactive/index.html#./Point_Placemarks/Point_Placemarks.Extruded.kml)
		 * 
		 * @param xpos
		 *            Latitude or X position, units depending on the CRS used in the model
		 * @param ypos
		 *            Longitude or Y position, units depending on the CRS used in the model
		 * @param height
		 *            Height at which the label should be displayed
		 * @param beginDate
		 *            Begining date of the timespan
		 * @param endDate
		 *            End date of the timespan
		 * @param name
		 *            Title (always displayed)
		 * @param description
		 *            Description (only displayed on mouse click on the label)
		 */
		public void addLabel(IScope scope, GamaPoint loc, 
				String beginDate, String endDate, String name, String description,
				String styleName) {
			Placemark placemark = fold.createAndAddPlacemark().withStyleUrl(
					"#" + styleName);
			placemark.createAndSetTimeSpan().withBegin(beginDate).withEnd(endDate);
			de.micromata.opengis.kml.v_2_2_0.Point ls = placemark
					.createAndSetPoint();
			ls.setExtrude(true);
			ls.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
			GamaPoint locTM = Spatial.Projections.transform_CRS(scope, loc, "EPSG:4326").getCentroid();
		
			ls.addToCoordinates(locTM.x,locTM.y,locTM.z);
			placemark.setName(name);
			placemark.setDescription(description);
		}

		
		public void add3DModel(IScope scope, GamaPoint loc, double orientation,
				double scale, String beginDate, String endDate, String daefile) {

			Placemark placemark = fold.createAndAddPlacemark();
			placemark.createAndSetTimeSpan().withBegin(beginDate).withEnd(endDate);
			Model model = placemark.createAndSetModel();
			model.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
			GamaPoint locTM = Spatial.Projections.transform_CRS(scope, loc, "EPSG:4326").getCentroid();
			Location locKML = new Location();
			locKML.setLongitude(locTM.x); locKML.setLatitude(locTM.y); locKML.setAltitude(locTM.z);
			model.setLocation(locKML);
			model.setScale(new Scale().withX(scale).withY(scale).withZ(scale));
			model.setLink(new Link().withHref(daefile));
			model.setOrientation(new Orientation().withHeading(orientation));
		}

		/**
		 * Add a placemark with a geometry object.The geometry can be a Point, a
		 * Line, a Polygon or any Multi-geometry. Points will be represented by an
		 * icon and linear or surface objects will be drawn.
		 * 
		 * @param label
		 *            The title of the folder that will be created for this
		 *            ShpRecord
		 * @param beginDate
		 *            Begining date of the timespan
		 * @param endDate
		 *            End date of the timespan
		 * @param geom
		 *            Geometry object to be drawn
		 * @param height
		 *            Height of the feature to draw. If > 0 the feature will be
		 *            shown extruded to the given height (relative to the ground
		 *            level). If <= 0 the feature will be drawn flat on the ground.
		 */
		public void addGeometry(IScope scope, String label, String beginDate, String endDate,
				IShape shape, String styleName, double height) {
			Placemark placemark = fold.createAndAddPlacemark().withStyleUrl(
					"#" + styleName);
			placemark.setName(label);
			placemark.createAndSetTimeSpan().withBegin(beginDate).withEnd(endDate);
			
			IShape shapeTM = Spatial.Projections.transform_CRS(scope, shape, "EPSG:4326");
			Geometry geom = shapeTM.getInnerGeometry();
			
			if (geom instanceof Point)
				addPoint(placemark, ((Point) geom), height);
			else if (geom instanceof LineString)
				addLine(placemark, ((LineString) geom), height);
			else if (geom instanceof Polygon)
				addPolygon(placemark, ((Polygon) geom), height);
			else if (geom instanceof MultiPoint)
				addMultiPoint(placemark, ((MultiPoint) geom), height);
			else if (geom instanceof MultiLineString)
					addMultiLine(placemark, ((MultiLineString) geom), height);
			else if (geom instanceof MultiPolygon)
				addMultiPolygon(placemark, ((MultiPolygon) geom),height);
		}
		
		
		public void setVisibility(boolean value) {
			this.fold.setVisibility(value);
			for (Feature f:this.fold.getFeature()) {
			  f.setVisibility(value);
			}
		}
		

		public void addPoint(Placemark pm, Point point, double height) {
			de.micromata.opengis.kml.v_2_2_0.Point kmlpoint = pm
					.createAndSetPoint();
			fillPoint(kmlpoint, point, height);
		}

		public void fillPoint(de.micromata.opengis.kml.v_2_2_0.Point kmlpoint,
				Point point, double height) {
			Coordinate pos = point.getCoordinate();
			if (height > 0.0) {
				kmlpoint.setExtrude(true);
				kmlpoint.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
				kmlpoint.addToCoordinates(pos.x, pos.y, height);
			} else
				kmlpoint.addToCoordinates(pos.x, pos.y);
		}

		public void addLine(Placemark pm, LineString line, double height) {
			de.micromata.opengis.kml.v_2_2_0.LineString kmlline = pm
					.createAndSetLineString();
			fillLine(kmlline, line, height);
		}

		public void fillLine(de.micromata.opengis.kml.v_2_2_0.LineString kmlline,
				LineString line, double height) {
			if (height > 0.0) {
				kmlline.setExtrude(true);
				kmlline.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
				for (Coordinate pos : line.getCoordinates()) {
					kmlline.addToCoordinates(pos.x, pos.y, height);
				}
			} else {
				for (Coordinate pos : line.getCoordinates()) {
					kmlline.addToCoordinates(pos.x, pos.y);
				}
			}
		}

		public void addPolygon(Placemark pm, Polygon poly, double height) {
			de.micromata.opengis.kml.v_2_2_0.Polygon kmlpoly = pm
					.createAndSetPolygon();
			fillPolygon(kmlpoly, poly, height);
		}

		public void fillPolygon(de.micromata.opengis.kml.v_2_2_0.Polygon kmlpoly,
				Polygon poly, double height) {

			// Shell ring
			de.micromata.opengis.kml.v_2_2_0.LinearRing kmlring = kmlpoly
					.createAndSetOuterBoundaryIs().createAndSetLinearRing();
			if (height > 0.0) {
				kmlpoly.setExtrude(true);
				kmlpoly.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
				for (Coordinate pos : poly.getExteriorRing().getCoordinates()) {
					kmlring.addToCoordinates(pos.x, pos.y, height);
				}
			} else {
				kmlpoly.setTessellate(true);
				for (Coordinate pos : poly.getExteriorRing().getCoordinates()) {
					kmlring.addToCoordinates(pos.x, pos.y);
				}
			}

			// Holes
			for (int hi = 0; hi < poly.getNumInteriorRing(); hi++) {
				de.micromata.opengis.kml.v_2_2_0.LinearRing kmlhole = kmlpoly
						.createAndAddInnerBoundaryIs().createAndSetLinearRing();
				if (height > 0.0) {
					kmlpoly.setExtrude(true);
					kmlpoly.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
					for (Coordinate pos : poly.getInteriorRingN(hi)
							.getCoordinates()) {
						kmlhole.addToCoordinates(pos.x, pos.y, height);
					}
				} else {
					kmlpoly.setTessellate(true);
					for (Coordinate pos : poly.getInteriorRingN(hi)
							.getCoordinates()) {
						kmlhole.addToCoordinates(pos.x, pos.y);
					}
					;
				}
			}
		}

		public void addMultiPoint(Placemark pm, MultiPoint mpoint, double height) {
			int ng = mpoint.getNumGeometries();
			MultiGeometry mg = pm.createAndSetMultiGeometry();
			for (int gx = 0; gx < ng; gx++) {
				de.micromata.opengis.kml.v_2_2_0.Point kmlpoint = mg
						.createAndAddPoint();
				fillPoint(kmlpoint, (Point) mpoint.getGeometryN(gx), height);
			}
		}

		public void addMultiLine(Placemark pm, MultiLineString mline, double height) {
			int ng = mline.getNumGeometries();
			MultiGeometry mg = pm.createAndSetMultiGeometry();
			for (int gx = 0; gx < ng; gx++) {
				de.micromata.opengis.kml.v_2_2_0.LineString kmlline = mg
						.createAndAddLineString();
				fillLine(kmlline, (LineString) mline.getGeometryN(gx), height);
			}
		}

		public void addMultiPolygon(Placemark pm, MultiPolygon mpoly, double height) {
			int ng = mpoly.getNumGeometries();
			MultiGeometry mg = pm.createAndSetMultiGeometry();
			for (int gx = 0; gx < ng; gx++) {
				de.micromata.opengis.kml.v_2_2_0.Polygon kmlpoly = mg
						.createAndAddPolygon();
				fillPolygon(kmlpoly, (Polygon) mpoly.getGeometryN(gx), height);
			}
		}
	}
}