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
package cenres.gaml.extensions.hydro.utils;

import java.util.*;

import com.vividsolutions.jts.geom.Coordinate;

import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;

/**
 * The class GamaGeometryUtils.
 * 
 * @author drogoul
 * @since 14 d�c. 2011
 * 
 */
public class WaterLevelUtils {

	/**
	 * This class allows to sort coordinates in increasing order according to the x value.
	 * @author Philippe Caillou
	 *
	 */
	public static class XCoordinatesComparator implements Comparator<Coordinate> {

		@Override
		public int compare(Coordinate arg0, Coordinate arg1) {
			return (arg0.x > arg1.x)?1:(arg0.x < arg1.x)?-1:0;
		}
		
	}
	/** 
	 * This class allows to sort coordinates in decreasing order according to the y value.
	 * @author Philippe
	 *
	 */
	public static class YCoordinatesComparator implements Comparator<Coordinate> {



		@Override
		public int compare(Coordinate arg0, Coordinate arg1) {
			return (arg0.y > arg1.y)?-1:(arg0.y < arg1.y)?1:0;
		}
		
	}
	
	public static double heigth(final List<Coordinate> points, double targetsurface)  {
//		double totalsurface;
		double currentheight;
		double previousheight;
//		double previoussurface;
		double currentsurface;
		
//		double tempsurf;
		boolean trouve=false;
		double res=-1;
		
		int nbtrap=points.size()-1;
		
		double[] prevtrapsurf=new double[nbtrap];
		double[] nexttrapsurf=new double[nbtrap];
		double[] trapwidth=new double[nbtrap];
		double[] prevtrapwidth=new double[nbtrap];
		double[] leftheight=new double[nbtrap+1];
		double[] leftheightprec=new double[nbtrap+1];

		
		List<Coordinate> sortedpointsy = new ArrayList<Coordinate>();
		sortedpointsy.addAll(points);
		Collections.sort(sortedpointsy, new YCoordinatesComparator()); //max to min
		
		
		List<Coordinate> sortedpointsx = new ArrayList<Coordinate>();
		sortedpointsx.addAll(points);
		Collections.sort(sortedpointsx, new XCoordinatesComparator()); //min to max
				
		currentheight=sortedpointsy.get(0).y;
		
		boolean finished=false;
		int nextpoint=-1;
		
		for (int i=0; i<nbtrap+1; i++)
		{
			leftheight[i]=currentheight-sortedpointsx.get(i).y;			
		}
		currentsurface=0;
		for (int i=0; i<nbtrap; i++)
		{
			trapwidth[i]=sortedpointsx.get(i+1).x-sortedpointsx.get(i).x;			
			nexttrapsurf[i]=trapwidth[i]*(leftheight[i]+leftheight[i+1])/2;			
			currentsurface=currentsurface+nexttrapsurf[i];
		}
		
		while (!finished)
		{
			nextpoint++;
			// previoussurface=currentsurface;
			currentsurface=0;

			prevtrapsurf=nexttrapsurf;
			nexttrapsurf=new double[nbtrap];
			
			leftheightprec=leftheight;
			leftheight=new double[nbtrap+1];
			
			prevtrapwidth=trapwidth;
			trapwidth=new double[nbtrap];
			
			previousheight=currentheight;
			currentheight=sortedpointsy.get(nextpoint).y;
			for (int i=0; i<nbtrap+1; i++)
			{
				leftheight[i]=currentheight-sortedpointsx.get(i).y;	
			}

			for (int i=0; i<nbtrap; i++)
			if (prevtrapwidth[i]<=0)
				trapwidth[i]=0;
			for (int i=0; i<nbtrap; i++)
				if (prevtrapwidth[i]>0)
			{
					trapwidth[i]=prevtrapwidth[i];
					if ((leftheight[i]<=0)&(leftheight[i+1]>0))
					{					
						trapwidth[i]=(sortedpointsx.get(i+1).x-sortedpointsx.get(i).x)*(currentheight-sortedpointsx.get(i+1).y)/(sortedpointsx.get(i).y-sortedpointsx.get(i+1).y);
						leftheight[i]=0;
					}
					if ((leftheight[i]>0)&(leftheight[i+1]<=0))
					{					
						trapwidth[i]=(sortedpointsx.get(i+1).x-sortedpointsx.get(i).x)*(currentheight-sortedpointsx.get(i).y)/(sortedpointsx.get(i+1).y-sortedpointsx.get(i).y);
						leftheight[i+1]=0;
					}
				if ((leftheight[i]<=0)&(leftheight[i+1]<=0))
				{					
					trapwidth[i]=0;
				}
				nexttrapsurf[i]=trapwidth[i]*(leftheight[i]+leftheight[i+1])/2;			
				currentsurface=currentsurface+trapwidth[i]*(leftheight[i]+leftheight[i+1])/2;
			}
			if (currentsurface<targetsurface)
			{
				finished=true;
				
//  			STOT=somme(triangles,h2*dx/dy)+somme(rect,h*dx)
//				STOT=h2*somm(dxtri/dytri/2)+h*somme(dxrect)				
//				STOT=A*h2+Bh
//				A=somme(dxtri/dytri/2) B=somme(dxrect)
//				hsol=(-b +- sqrt(b2-4ac) )/2a with c=-stot
				double A=0;
				double B=0;
				double stot=targetsurface-currentsurface;
				double C=-stot;
				for (int i=0; i<nbtrap; i++)
					if (prevtrapsurf[i]!=0)
					{
						if ((leftheight[i]>=0)&(leftheight[i+1]>=0))
						{
							B=B+(sortedpointsx.get(i+1).x-sortedpointsx.get(i).x);
						}
						else
						{
							if (sortedpointsx.get(i+1).y<sortedpointsx.get(i).y)
							{
								A=A+0.5*(prevtrapwidth[i])/(leftheightprec[i+1]);
								B=B+0.5*leftheight[i+1]*(prevtrapwidth[i])/(leftheightprec[i+1]);
								C=C-leftheight[i+1]*trapwidth[i];												
							}
							else
							{
								A=A+0.5*(prevtrapwidth[i])/(leftheightprec[i]);
								B=B+0.5*leftheight[i]*(prevtrapwidth[i])/(leftheightprec[i]);
								C=C-leftheight[i]*trapwidth[i];				
							}
						}
					}
				double sol1=(-B+Math.sqrt(B*B-4.0*A*C))/(2.0*A);
				double sol2=(-B-Math.sqrt(B*B-4.0*A*C))/(2.0*A);
				if ((A==0)&(B>0)) sol1=-C/B;
				if ((A==0)&(B>0)) sol2=-1;
				
				if ((sol1>=0)&(sol1<=(previousheight-currentheight)))
				{
					trouve=true;
					res=currentheight+sol1;
					if ((sol2>0)&(sol2<(previousheight-currentheight)))
					{
						throw new GamaRuntimeException("2 possible water level, pb...");
						
					}
					
				}
				if ((sol2>=0)&(sol2<=(previousheight-currentheight)))
				{
					trouve=true;
					res=currentheight+sol2;
					if ((sol1>0)&(sol1<(previousheight-currentheight)))
					{
						throw new GamaRuntimeException("2 possible water level, pb...");
						
					}
					
				}
				
			}
			if (nextpoint==nbtrap)
				finished=true;
		}
		if (!trouve)
			throw new GamaRuntimeException("no possible water level, pb...");
		return res;
	}
	
	public static double area(final List<Coordinate> points, double targetheight)  {
		int nbtrap=points.size()-1;
		
		double[] nexttrapsurf=new double[nbtrap];
		double[] trapwidth=new double[nbtrap];
		double[] leftheight=new double[nbtrap+1];
		
		double currentsurface=0;

		
		
		List<Coordinate> sortedpointsx = new ArrayList<Coordinate>();
		sortedpointsx.addAll(points);
		Collections.sort(sortedpointsx, new XCoordinatesComparator()); //min to max
				
		for (int i=0; i<nbtrap+1; i++)
		{
			leftheight[i]=targetheight-sortedpointsx.get(i).y;			
		}
		for (int i=0; i<nbtrap; i++)
		{
				trapwidth[i]=sortedpointsx.get(i+1).x-sortedpointsx.get(i).x;			
				if ((leftheight[i]<=0)&(leftheight[i+1]>0))
				{					
					trapwidth[i]=(sortedpointsx.get(i+1).x-sortedpointsx.get(i).x)*(targetheight-sortedpointsx.get(i+1).y)/(sortedpointsx.get(i).y-sortedpointsx.get(i+1).y);
					leftheight[i]=0;
				}
				if ((leftheight[i]>0)&(leftheight[i+1]<=0))
				{					
					trapwidth[i]=(sortedpointsx.get(i+1).x-sortedpointsx.get(i).x)*(targetheight-sortedpointsx.get(i).y)/(sortedpointsx.get(i+1).y-sortedpointsx.get(i).y);
					leftheight[i+1]=0;
				}
			if ((leftheight[i]<=0)&(leftheight[i+1]<=0))
			{					
				trapwidth[i]=0;
			}
			nexttrapsurf[i]=trapwidth[i]*(leftheight[i]+leftheight[i+1])/2;			
			currentsurface=currentsurface+trapwidth[i]*(leftheight[i]+leftheight[i+1])/2;
		}
		return currentsurface;

		
	}

	public static GamaList<GamaList<GamaPoint>> areaPolylines(final List<Coordinate> points, double targetheight)  {
		int nbtrap=points.size()-1;
		
		double[] nexttrapsurf=new double[nbtrap];
		double[] trapwidth=new double[nbtrap];
		double[] leftheight=new double[nbtrap+1];
		
		double currentsurface=0;
		boolean inthewater=false;

		GamaPoint currentstartpoint=new GamaPoint(0,0);
		GamaList<GamaList<GamaPoint>> listoflist=new GamaList<GamaList<GamaPoint>>();
		GamaList<GamaPoint> currentlist=new GamaList<GamaPoint>();
		
		List<Coordinate> sortedpointsx = new ArrayList<Coordinate>();
		sortedpointsx.addAll(points);
		Collections.sort(sortedpointsx, new XCoordinatesComparator()); //min to max
				
		for (int i=0; i<nbtrap+1; i++)
		{
			leftheight[i]=targetheight-sortedpointsx.get(i).y;			
		}
		for (int i=0; i<nbtrap; i++)
		{
			if ((leftheight[i]>0)&(leftheight[i+1]>0))
			{
				if (!inthewater)
				{
					currentlist=new GamaList<GamaPoint>();
					currentstartpoint=new GamaPoint(sortedpointsx.get(i).x,targetheight);
					currentlist.add(new GamaPoint(sortedpointsx.get(i).x,targetheight));
					currentlist.add(new GamaPoint(sortedpointsx.get(i).x,sortedpointsx.get(i).y));
				}
				inthewater=true;

				currentlist.add(new GamaPoint(sortedpointsx.get(i+1).x,sortedpointsx.get(i+1).y));
				trapwidth[i]=sortedpointsx.get(i+1).x-sortedpointsx.get(i).x;							
			}
				if ((leftheight[i]<=0)&(leftheight[i+1]>0))
				{					
					trapwidth[i]=(sortedpointsx.get(i+1).x-sortedpointsx.get(i).x)*(targetheight-sortedpointsx.get(i+1).y)/(sortedpointsx.get(i).y-sortedpointsx.get(i+1).y);
					leftheight[i]=0;
					if (!inthewater)
					{
						currentlist=new GamaList<GamaPoint>();
						currentstartpoint=new GamaPoint(sortedpointsx.get(i).x+trapwidth[i],targetheight);
						currentlist.add(new GamaPoint(sortedpointsx.get(i).x+trapwidth[i],targetheight));
						inthewater=true;
					}
					currentlist.add(new GamaPoint(sortedpointsx.get(i+1).x+trapwidth[i],sortedpointsx.get(i+1).y));
				}
				if ((leftheight[i]>0)&(leftheight[i+1]<=0))
				{					
					trapwidth[i]=(sortedpointsx.get(i+1).x-sortedpointsx.get(i).x)*(targetheight-sortedpointsx.get(i).y)/(sortedpointsx.get(i+1).y-sortedpointsx.get(i).y);
					leftheight[i+1]=0;
					if (inthewater)
					{
						currentlist.add(new GamaPoint(sortedpointsx.get(i).x+trapwidth[i],targetheight));
						currentlist.add(currentstartpoint);
						listoflist.add(currentlist);
						inthewater=false;
					}
				}
			if ((leftheight[i]<=0)&(leftheight[i+1]<=0))
			{					
				trapwidth[i]=0;
				if (inthewater)
				{
					currentlist.add(new GamaPoint(sortedpointsx.get(i).x,targetheight));
					currentlist.add(currentstartpoint);
					listoflist.add(currentlist);
					inthewater=false;
				}
			}
			nexttrapsurf[i]=trapwidth[i]*(leftheight[i]+leftheight[i+1])/2;			
			currentsurface=currentsurface+trapwidth[i]*(leftheight[i]+leftheight[i+1])/2;
		}
		if (inthewater)
		{
			currentlist.add(currentstartpoint);
			listoflist.add(currentlist);
			inthewater=false;
		}
		return listoflist;

		
	}


}
