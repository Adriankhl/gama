/*********************************************************************************************
 * 
 * 
 * 'StatementFactory.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gaml.factories;

import java.util.List;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.precompiler.GamlAnnotations.factory;
import msi.gama.precompiler.*;
import msi.gaml.descriptions.*;
import msi.gaml.descriptions.StatementDescription.StatementWithChildrenDescription;
import msi.gaml.statements.Facets;
import org.eclipse.emf.ecore.EObject;

/**
 * Written by drogoul Modified on 8 févr. 2010
 * 
 * @todo Description
 * 
 */
@factory(handles = { ISymbolKind.SEQUENCE_STATEMENT, ISymbolKind.SINGLE_STATEMENT, ISymbolKind.BEHAVIOR,
	ISymbolKind.ACTION, ISymbolKind.LAYER, ISymbolKind.BATCH_METHOD, ISymbolKind.OUTPUT })
public class StatementFactory extends SymbolFactory implements IKeyword {

	public StatementFactory(final List<Integer> handles) {
		super(handles);
	}

	@Override
	protected StatementDescription buildDescription(final String keyword, final Facets facets, final EObject element,
		final ChildrenProvider children, final IDescription enclosing, final SymbolProto proto) {
		if ( keyword.equals(PRIMITIVE) ) { return new PrimitiveDescription(keyword, enclosing, children,
			proto.hasScope(), proto.hasArgs(), element, facets); }
		if ( DescriptionFactory.getProto(keyword, enclosing).hasSequence() && !children.getChildren().isEmpty() ) { return new StatementWithChildrenDescription(
			keyword, enclosing, children, proto.hasScope(), proto.hasArgs(), element, facets); }
		return new StatementDescription(keyword, enclosing, children, proto.hasScope(), proto.hasArgs(), element,
			facets);
	}

}
