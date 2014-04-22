/*********************************************************************************************
 * 
 *
 * 'MonitorOutput.java', in plugin 'msi.gama.core', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.outputs;

import msi.gama.common.interfaces.*;
import msi.gama.common.util.GuiUtils;
import msi.gama.precompiler.GamlAnnotations.facet;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.usage;
import msi.gama.precompiler.GamlAnnotations.example;
import msi.gama.precompiler.*;
import msi.gama.runtime.*;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GAML;
import msi.gaml.descriptions.IDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.factories.DescriptionFactory;
import msi.gaml.types.IType;

/**
 * The Class MonitorOutput.
 * 
 * @author drogoul
 */
@symbol(name = IKeyword.MONITOR, kind = ISymbolKind.OUTPUT, with_sequence = false)
@facets(value = { @facet(name = IKeyword.NAME, type = IType.LABEL, optional = false, doc = @doc("identifier of the monitor")),
	@facet(name = IKeyword.REFRESH_EVERY, type = IType.INT, optional = true, doc = @doc("number of simulation steps between two computations of the expression (default is 1)")),
	@facet(name = IKeyword.VALUE, type = IType.NONE, optional = false, doc = @doc("expression that will be evaluated to be displayed in the monitor")) }, omissible = IKeyword.NAME)
@inside(symbols = { IKeyword.OUTPUT, IKeyword.PERMANENT })
@doc(value="A monitor allows to follow the value of an arbitrary expression in GAML.", usages = {
	@usage(value = "An example of use is:", examples = @example(value="monitor \"nb preys\" value: length(prey as list) refresh_every: 5;  ", isExecutable=false))})
public class MonitorOutput extends AbstractDisplayOutput {

	//

	public MonitorOutput(final IDescription desc) {
		super(desc);
		setValue(getFacet(IKeyword.VALUE));
		expressionText = getValue() == null ? "" : getValue().toGaml();
	}

	public MonitorOutput(final String name, final String expr) {
		super(DescriptionFactory.create(IKeyword.MONITOR, IKeyword.VALUE, expr, IKeyword.NAME, name == null ? expr
			: name));
		setScope(GAMA.obtainNewScope());
		setUserCreated(true);
		setNewExpressionText(expr);
		if ( getScope().init(this) ) {
			GAMA.getExperiment().getSimulationOutputs().addOutput(this);
			resume();
			open();
		}
	}

	protected String expressionText = "";
	protected IExpression value;
	protected Object lastValue = "";

	public Object getLastValue() {
		return lastValue;
	}

	@Override
	public String getViewId() {
		return GuiUtils.MONITOR_VIEW_ID;
	}

	@Override
	public String getId() {
		return getViewId() + ":" + getName();
	}

	@Override
	public boolean step(final IScope scope) {
		if ( scope.interrupted() ) { return false; }
		if ( getValue() != null ) {
			try {
				lastValue = getValue().value(scope);
			} catch (final GamaRuntimeException e) {
				lastValue = ItemList.ERROR_CODE + e.getMessage();
			}
		} else {
			lastValue = null;
		}
		return true;
	}

	public String getExpressionText() {
		return expressionText == null ? "" : expressionText;
	}

	@Override
	public boolean isUnique() {
		return true;
	}

	public boolean setNewExpressionText(final String string) {
		expressionText = string;
		setValue(GAML.compileExpression(string, getScope().getSimulationScope()));
		return getScope().step(this);
	}

	public void setNewExpression(final IExpression expr) throws GamaRuntimeException {
		expressionText = expr == null ? "" : expr.toGaml();
		setValue(expr);
		getScope().step(this);
	}

	@Override
	public String getViewName() {
		String result = super.getViewName();
		if ( result == null ) {
			result = getExpressionText();
		}
		return result;
	}

	public IExpression getValue() {
		return value;
	}

	protected void setValue(final IExpression value) {
		this.value = value;
	}

}
