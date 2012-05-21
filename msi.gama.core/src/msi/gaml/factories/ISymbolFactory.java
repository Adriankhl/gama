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
package msi.gaml.factories;

import java.util.*;
import msi.gama.common.interfaces.ISyntacticElement;
import msi.gama.kernel.model.IModel;
import msi.gaml.compilation.ISymbol;
import msi.gaml.descriptions.*;

/**
 * Written by drogoul Modified on 28 d�c. 2010
 * 
 * @todo Description
 * 
 */
public interface ISymbolFactory {

	public abstract IDescription createDescription(ISyntacticElement cur,
		final IDescription superDescription, final List<IDescription> children);

	public abstract IDescription createDescriptionRecursively(ISyntacticElement cur,
		IDescription superDesc);

	public abstract ISymbol compileDescription(final IDescription desc);

	public abstract void validateDescription(final IDescription desc);

	public abstract ISymbolFactory chooseFactoryFor(String keyword);

	public abstract Set<String> getKeywords();

	public abstract String getOmissibleFacetForSymbol(String symbol);

	SymbolMetaDescription getMetaDescriptionFor(IDescription context, String keyword);

	public abstract String getName();

	public interface Model extends ISymbolFactory {

		public abstract IModel compile(ModelStructure ms);

		public abstract ModelDescription validate(ModelStructure parse);
	}

	public abstract void addSpeciesNameAsType(String name);

}