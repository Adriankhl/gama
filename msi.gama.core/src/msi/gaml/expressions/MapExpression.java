/*********************************************************************************************
 * 
 * 
 * 'MapExpression.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.expressions;

import java.util.*;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gaml.types.*;

/**
 * ListValueExpr.
 * 
 * @author drogoul 23 août 07
 */
public class MapExpression extends AbstractExpression {

	public static IExpression create(final List<? extends IExpression> elements) {
		MapExpression u = new MapExpression(elements);
		if ( u.isConst() ) {
			IExpression e = GAML.getExpressionFactory().createConst(u.value(null), u.getType(), u.serialize(false));
			// System.out.println("				==== Simplification of " + u.toGaml() + " into " + e.toGaml());
		}
		return u;
	}

	private final IExpression[] keys;
	private final IExpression[] vals;
	private final GamaMap values;
	private boolean isConst, computed;

	MapExpression(final List<? extends IExpression> pairs) {
		keys = new IExpression[pairs.size()];
		vals = new IExpression[pairs.size()];
		for ( int i = 0, n = pairs.size(); i < n; i++ ) {
			IExpression e = pairs.get(i);
			if ( e instanceof BinaryOperator ) {
				BinaryOperator pair = (BinaryOperator) e;
				keys[i] = pair.exprs[0];
				vals[i] = pair.exprs[1];
			}
		}
		IType keyType = GamaType.findCommonType(keys, GamaType.TYPE);
		IType contentsType = GamaType.findCommonType(vals, GamaType.TYPE);
		values = GamaMapFactory.create(keyType, contentsType, keys.length);
		setName(pairs.toString());
		type = Types.MAP.of(keyType, contentsType);
	}

	MapExpression(final GamaMap<IExpression, IExpression> pairs) {
		keys = new IExpression[pairs.size()];
		vals = new IExpression[pairs.size()];
		int i = 0;
		for ( Map.Entry<IExpression, IExpression> entry : pairs.entrySet() ) {
			keys[i] = entry.getKey();
			vals[i] = entry.getValue();
			i++;
		}
		IType keyType = GamaType.findCommonType(keys, GamaType.TYPE);
		IType contentsType = GamaType.findCommonType(vals, GamaType.TYPE);
		values = GamaMapFactory.create(keyType, contentsType, keys.length);
		setName(pairs.toString());
		type = Types.MAP.of(keyType, contentsType);
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		GamaMap result = GamaMapFactory.create(type.getKeyType(), type.getContentType(), keys.length);
		for ( int i = 0; i < keys.length; i++ ) {
			if ( keys[i] == null || vals[i] == null ) {
				continue;
			}
			result.put(keys[i].resolveAgainst(scope), vals[i].resolveAgainst(scope));
		}
		MapExpression copy = new MapExpression(getElements());
		return copy;
	}

	@Override
	public GamaMap value(final IScope scope) throws GamaRuntimeException {
		if ( isConst && computed ) { return (GamaMap) values.clone(); }
		for ( int i = 0; i < keys.length; i++ ) {
			if ( keys[i] == null || vals[i] == null ) {
				computed = false;
				return GamaMapFactory.EMPTY_MAP;
			}
			values.put(keys[i].value(scope), vals[i].value(scope));
		}
		computed = true;
		return (GamaMap) values.clone();
	}

	@Override
	public String toString() {
		return getElements().toString();
	}

	@Override
	public boolean isConst() {
		for ( int i = 0; i < keys.length; i++ ) {
			// indicates an error in the compilation process of a former expression
			if ( keys[i] == null || vals[i] == null ) {
				continue;
			}
			if ( vals[i] != null || !keys[i].isConst() || vals[i] != null && !vals[i].isConst() ) { return false; }
		}
		isConst = true;
		return true;
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder();
		sb.append(' ').append('[');
		for ( int i = 0; i < keys.length; i++ ) {
			if ( i > 0 ) {
				sb.append(',');
			}
			if ( keys[i] == null || vals[i] == null ) {
				sb.append("nill::nil");
			} else {
				sb.append(keys[i].serialize(includingBuiltIn));
				sb.append("::");
				sb.append(vals[i].serialize(includingBuiltIn));
			}
		}
		sb.append(']').append(' ');
		return sb.toString();
	}

	public IExpression[] keysArray() {
		return keys;
	}

	public IExpression[] valuesArray() {
		return vals;
	}

	public GamaMap<IExpression, IExpression> getElements() {
		GamaMap result = GamaMapFactory.create(type.getKeyType(), type.getContentType(), keys.length);
		for ( int i = 0; i < keys.length; i++ ) {
			if ( keys[i] == null ) {
				continue;
			}
			result.put(keys[i], vals[i]);
		}
		return result;
	}

	@Override
	public String getTitle() {
		return "literal map of type " + getType().getTitle();
	}

	/**
	 * @see msi.gaml.expressions.IExpression#getDocumentation()
	 */

	@Override
	public String getDocumentation() {
		return "Constant " + isConst() + "<br>Contains elements of type " + type.getContentType().getTitle();
	}

}
