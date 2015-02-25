/*********************************************************************************************
 * 
 * 
 * 'TypeFieldExpression.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.expressions;

import msi.gama.runtime.IScope;
import msi.gaml.descriptions.*;

public class TypeFieldExpression extends UnaryOperator {

	public TypeFieldExpression(final OperatorProto proto, final IDescription context, final IExpression ... exprs) {
		super(proto, context, exprs);
	}

	@Override
	public TypeFieldExpression resolveAgainst(final IScope scope) {
		return new TypeFieldExpression(prototype, null, child.resolveAgainst(scope));
	}

	@Override
	public String serialize(boolean includingBuiltIn) {
		StringBuilder sb = new StringBuilder();
		parenthesize(sb, child);
		sb.append(".").append(name);
		return sb.toString();
	}

	@Override
	public String toString() {
		if ( child == null ) { return prototype.signature.toString() + "." + name; }
		return child.serialize(false) + "." + name;
	}

	@Override
	public String getDocumentation() {
		if ( child != null ) { return "Defined on objects of type " + child.getType().getTitle(); }
		return "";
	}

	@Override
	public String getTitle() {
		return "field <b>" + getName() + "</b> of type " + getType().getTitle();
	}

}
