/*********************************************************************************************
 * 
 * 
 * 'FacetProto.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.descriptions;

import gnu.trove.set.hash.THashSet;
import java.util.*;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.precompiler.JavaWriter;
import msi.gaml.types.*;

public class FacetProto implements IGamlDescription, Comparable<FacetProto> {

	public final String name;
	public String deprecated = null;
	public final int[] types;
	public final boolean optional;
	public final boolean internal;
	private final boolean isLabel;
	private final boolean isId;
	public final boolean isType;
	public final Set<String> values;
	public String doc = "No documentation yet";
	// private SymbolProto owner;
	static FacetProto KEYWORD = KEYWORD();
	static FacetProto DEPENDS_ON = DEPENDS_ON();
	static FacetProto NAME = NAME();

	public FacetProto(final String name, final int[] types, final String[] values, final boolean optional,
		final boolean internal, final String doc) {
		this.name = name;
		this.types = types;
		this.optional = optional;
		this.internal = internal;
		isLabel = SymbolProto.ids.contains(types[0]);
		isId = isLabel && types[0] != IType.LABEL;
		isType = types[0] == IType.TYPE_ID;
		this.values = new THashSet(Arrays.asList(values));
		if ( doc != null ) {
			String[] strings = doc.split(JavaWriter.DOC_SEP, -1);
			this.doc = strings[0];
			if ( strings.length > 1 ) {
				this.deprecated = strings[1];
				if ( deprecated.length() == 0 ) {
					deprecated = null;
				}
			}
		}
	}

	boolean isLabel() {
		return isLabel;
	}

	public boolean isId() {
		return isId;
	}

	// public void setOwner(final SymbolProto symbol) {
	// owner = symbol;
	// }

	static FacetProto DEPENDS_ON() {
		return new FacetProto(IKeyword.DEPENDS_ON, new int[] { IType.LIST }, new String[0], true, true,
			"the dependencies of expressions (internal)");
	}

	static FacetProto KEYWORD() {
		return new FacetProto(IKeyword.KEYWORD, new int[] { IType.ID }, new String[0], true, true,
			"the declared keyword (internal)");
	}

	static FacetProto NAME() {
		return new FacetProto(IKeyword.NAME, new int[] { IType.LABEL }, new String[0], true, true,
			"the declared name (internal)");
	}

	/**
	 * Method getTitle()
	 * @see msi.gaml.descriptions.IGamlDescription#getTitle()
	 */
	@Override
	public String getTitle() {
		// String p = owner == null ? "" : " of statement " + owner.getName();
		return "Facet " + name /* + p */;
	}

	public String typesToString() {
		StringBuilder s = new StringBuilder(30);
		s.append(types.length < 2 ? " " : " any type in [");
		for ( int i = 0; i < types.length; i++ ) {
			switch (types[i]) {
				case IType.ID:
					s.append("an identifier");
					break;
				case IType.LABEL:
					s.append("a label");
					break;
				case IType.NEW_TEMP_ID:
					s.append("a new identifier");
					break;
				case IType.NEW_VAR_ID:
					s.append("a new identifier");
					break;
				case IType.TYPE_ID:
					s.append("a datatype identifier");
					break;
				case IType.NONE:
					s.append("any type");
					break;
				default:
					s.append("a ").append(Types.get(types[i]).toString());
			}
			if ( i != types.length - 1 ) {
				s.append(", ");
			}
		}
		if ( types.length >= 2 ) {
			s.append("]");
		}
		return s.toString();
	}

	/**
	 * Method getDocumentation()
	 * @see msi.gaml.descriptions.IGamlDescription#getDocumentation()
	 */
	@Override
	public String getDocumentation() {
		StringBuilder sb = new StringBuilder(100);
		sb.append("<b>").append(name).append("</b>, ")
			.append(deprecated != null ? "deprecated" : optional ? "optional" : "required").append("")
			.append(", expects ").append(typesToString());
		if ( values.size() > 0 ) {
			sb.append(", takes values in ").append(values).append(". ");
		}
		if ( doc != null && doc.length() > 0 ) {
			sb.append(" - ").append(doc);
		}
		if ( deprecated != null ) {
			sb.append(" <b>[");
			sb.append(deprecated);
			sb.append("]</b>");
		}
		return sb.toString();
	}

	/**
	 * Method getName()
	 * @see msi.gaml.descriptions.IGamlDescription#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Method compareTo()
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final FacetProto o) {
		return getName().compareTo(o.getName());
	}

	/**
	 * @return
	 */

}