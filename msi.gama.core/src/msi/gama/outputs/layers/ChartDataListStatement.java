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
package msi.gama.outputs.layers;

import java.util.ArrayList;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.interfaces.IValue;
import msi.gama.common.util.GuiUtils;
import msi.gama.outputs.layers.ChartDataStatement.ChartData;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.*;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gaml.descriptions.IDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;
import msi.gaml.operators.Random;
import msi.gaml.statements.AbstractStatement;
import msi.gaml.types.IType;
import msi.gaml.variables.IVariable;

import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.xy.*;

@symbol(name = "datalist", kind = ISymbolKind.SINGLE_STATEMENT, with_sequence = false, doc = @doc("add a list of series to a chart. The number of series can be dynamic (the size of the list changes each step). See Ant Foraging (Charts) model in ChartTest for examples."))
@inside(symbols = IKeyword.CHART, kinds = ISymbolKind.SEQUENCE_STATEMENT)
@facets(value = {
	@facet(name = IKeyword.VALUE, type = IType.LIST, optional = false, doc = @doc("the values to display. Has to be a List of List. Each element can be a number (series/histogram) or a list with two values (XY chart)")),
//	@facet(name = IKeyword.NAME, type =  IType.LIST, optional = true, doc = @doc("the name of the series: a list of strings (can be a variable with dynamic names)")),
	@facet(name = IKeyword.LEGEND, type =  IType.LIST, optional = true, doc = @doc("the name of the series: a list of strings (can be a variable with dynamic names)")),
	@facet(name = ChartDataListStatement.CATEGNAMES, type =  IType.LIST, optional = true, doc = @doc("the name of categories (can be a variable with dynamic names)")),
	@facet(name = ChartDataListStatement.REVERSECATEG, type =  IType.BOOL, optional = true, doc = @doc("reverse the order of series/categories ([[1,2],[3,4],[5,6]] --> [[1,3,5],[2,4,6]]. May be useful when it is easier to construct one list over the other.")),
	@facet(name = IKeyword.COLOR, type =  IType.LIST, optional = true, doc = @doc("list of colors")),
	@facet(name = IKeyword.STYLE, type = IType.ID, values = { IKeyword.LINE, IKeyword.WHISKER, IKeyword.AREA,
		IKeyword.BAR, IKeyword.DOT, IKeyword.STEP, IKeyword.SPLINE, IKeyword.STACK, IKeyword.THREE_D, IKeyword.RING,
		IKeyword.EXPLODED }, optional = true, doc = @doc("series style")) }, omissible = IKeyword.LEGEND)
public class ChartDataListStatement extends AbstractStatement {	

	public static final String DATALISTS = "datalist";
//	public static final String UPDATEDATA = "updatedata";
//	public static final String REVERSEDATA = "reversedata";
	public static final String CATEGNAMES = "categoriesnames";
//	public static final String SERIESNAMES = "seriesnames";
	public static final String REVERSECATEG= "inverse_series_categories";
//	protected int dataNumber = 0;
	
	public static class ChartDataList {

		IExpression colorlistexp;
		IExpression valuelistexp;
		IExpression legendlistexp;
		IExpression categlistexp;
		boolean doreverse;
		AbstractRenderer renderer;
		Object lastvalue;
		String name;
		int previoussize=0;


	}


	public ChartDataListStatement(final IDescription desc) {
		super(desc);
	}

	/**
	 * @throws GamaRuntimeException
	 * @param scope
	 */
	
	public static ChartData newChartData(final IScope scope, AbstractRenderer style, String name, GamaColor color, Object value)
	{
		ChartData data = new ChartData();

		data.renderer = style;

		data.name =
			Cast.asString(scope,name);
		data.color = Cast.asColor(scope, color);
		// in order to "detach" the expression from the current definition scope
		data.lastvalue=value;
		data.value=null;
//		data.lastvalue =(IVariable) value;
		return data;
		
	}
	
	public  ChartDataList createData(final IScope scope) throws GamaRuntimeException {
		ChartDataList datalist=new ChartDataList();
		

//		scope.addVarWithValue(ChartDataListStatement.UPDATEDATA, new Boolean(true));
//		scope.addVarWithValue(ChartDataListStatement.REVERSEDATA, new Boolean(reverse));
//		for (int i=0; i<values.size(); i++)
//		{
//			((ArrayList) scope.getVarValue(ChartDataStatement.DATAS)).add(values.get(i));			
//		}
		IExpression valexp=getFacet(IKeyword.VALUE);
		datalist.valuelistexp=valexp;
		Boolean reverse= Cast.asBool(scope, getFacetValue(scope, "inverse_series_categories",false));
		datalist.doreverse=reverse;
		
		IExpression categexp=getFacet(ChartDataListStatement.CATEGNAMES);
		datalist.categlistexp=categexp;

		IExpression colorexp=getFacet(IKeyword.COLOR);
		datalist.colorlistexp=colorexp;

		IExpression serexp=getFacet(IKeyword.LEGEND);
		datalist.legendlistexp=serexp;
		
		
		if (categexp!=null)
		{
//			scope.addVarWithValue(ChartDataListStatement.CATEGNAMES, categexp);			
		}
		if (serexp!=null)
		{
//			scope.addVarWithValue(ChartDataListStatement.SERIESNAMES, serexp);
		}

/*		Object val=valexp.resolveAgainst(scope).value(scope);
		if (!(val instanceof GamaList))
		{
			GuiUtils.debug("chart list with no list...");
			return datalist;
		}
		
		IList values = Cast.asList(scope,val);
		GamaList defaultnames=new GamaList<String>();
		GamaList defaultcolors=new GamaList<GamaColor>();
		for (int i=0; i<values.size();i++)
		{
			defaultnames.add("data"+i);
			if (i<10)
			{
				if (i==0) defaultcolors.add((GamaColor)Cast.asColor(scope,GamaColor.CYAN));
				if (i==1) defaultcolors.add((GamaColor)Cast.asColor(scope,GamaColor.RED));
				if (i==2) defaultcolors.add((GamaColor)Cast.asColor(scope,GamaColor.YELLOW));
				if (i==3) defaultcolors.add((GamaColor)Cast.asColor(scope,GamaColor.GREEN));
				if (i==4) defaultcolors.add((GamaColor)Cast.asColor(scope,GamaColor.BLUE));
				if (i==5) defaultcolors.add((GamaColor)Cast.asColor(scope,GamaColor.PINK));
				if (i==6) defaultcolors.add((GamaColor)Cast.asColor(scope,GamaColor.MAGENTA));
				if (i==7) defaultcolors.add((GamaColor)Cast.asColor(scope,GamaColor.ORANGE));
				if (i==8) defaultcolors.add((GamaColor)Cast.asColor(scope,GamaColor.LIGHT_GRAY));
				if (i==9) defaultcolors.add((GamaColor)Cast.asColor(scope,GamaColor.DARK_GRAY));
			}
			if (i>=10)
			if (i<GamaColor.colors.size())
				defaultcolors.add(GamaColor.int_colors.values().toArray()[i]);
			else
				defaultcolors.add(GamaColor.getInt(Random.opRnd(scope, 10000)));				
			
		}
		IList colors=defaultcolors;

		boolean dynamicseriesnames=false;
		GamaList seriesnames=defaultnames;
		
		IExpression serievalue=(IExpression) scope.getVarValue(ChartDataListStatement.SERIESNAMES);
		
		if (serievalue!=null)
		{
		Object valc=serievalue.resolveAgainst(scope).value(scope);
		if ((valc instanceof GamaList))
		{
			dynamicseriesnames=true;
			seriesnames=(GamaList)valc;
			for (int i=0; i<Math.min(values.size(),seriesnames.size());i++)
			{
				defaultnames.set(i,seriesnames.get(i)+"("+i+")");
			}
		}
		}
//		GuiUtils.debug("dyncateg:"+defaultnames);
		
//		names = Cast.asList(scope, getFacetValue(scope, IKeyword.LEGEND,getFacetValue(scope, IKeyword.NAME,defaultnames)));
		colors = Cast.asList(scope, getFacetValue(scope, IKeyword.COLOR,defaultcolors));
*/
		String style = getLiteral(IKeyword.STYLE);
		if ( style == null ) {
			style = IKeyword.LINE;
		}
			AbstractRenderer r = null;
			if ( style.equals(IKeyword.LINE) ) {
				r = new XYLineAndShapeRenderer();
			} else if ( style.equals(IKeyword.AREA) ) {
				r = new XYAreaRenderer();
			} else if ( style.equals(IKeyword.WHISKER) ) {
				r = new BoxAndWhiskerRenderer();
			} else if ( style.equals(IKeyword.BAR) ) {
				r = new BarRenderer();
			} else if ( style.equals(IKeyword.DOT) ) {
				r = new XYDotRenderer();
			} else if ( style.equals(IKeyword.SPLINE) ) {
				r = new XYSplineRenderer();
			} else if ( style.equals(IKeyword.STEP) ) {
				r = new XYStepRenderer();
			} else if ( style.equals(IKeyword.STACK) ) {
				r = new StackedBarRenderer();
			}
		datalist.renderer=r;
			
		return datalist;
	}

	/**
	 * DataList statement requires a variable in the scope created by the Scope:
	 * DataListVars to transfer the data
	 */

	@Override
	protected Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {		
		ChartDataList data = createData(scope);
		((ArrayList) scope.getVarValue(DATALISTS)).add(data);
		return data;

//		ChartDataList values = createData(scope);
//		IExpression valexp=getFacet(IKeyword.VALUE);
//		Boolean reverse= Cast.asBool(scope, getFacetValue(scope, "inverse_series_categories",false));
//		scope.addVarWithValue(ChartDataListStatement.DATALIST, valexp);
//		scope.addVarWithValue(ChartDataListStatement.UPDATEDATA, new Boolean(true));
//		scope.addVarWithValue(ChartDataListStatement.REVERSEDATA, new Boolean(reverse));
//		for (int i=0; i<values.size(); i++)
//		{
//			((ArrayList) scope.getVarValue(ChartDataStatement.DATAS)).add(values.get(i));			
//		}
//		return valexp;
	}

}