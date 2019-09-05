/*******************************************************************************************************
 *
 * gaml.compilation.ast.SyntacticAttributeElement.java, in plugin gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.lang.gaml.ast;

import org.eclipse.emf.ecore.EObject;

/**
 * The Class SyntacticAttributeElement.
 */
public class SyntacticAttributeElement extends SyntacticSingleElement {

	/**
	 * The name.
	 */
	final String name;

	/**
	 * Instantiates a new syntactic attribute element.
	 *
	 * @param keyword
	 *            the keyword
	 * @param name
	 *            the name
	 * @param statement
	 *            the statement
	 */
	public SyntacticAttributeElement(final String keyword, final String name, final EObject statement) {
		super(keyword, null, statement);
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gaml.compilation.ast.AbstractSyntacticElement#toString()
	 */
	@Override
	public String toString() {
		return "Attribute " + getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gaml.compilation.ast.AbstractSyntacticElement#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

}
