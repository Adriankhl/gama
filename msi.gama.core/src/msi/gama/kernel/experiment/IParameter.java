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
package msi.gama.kernel.experiment;

import java.util.*;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.IType;

/**
 * Written by drogoul Modified on 4 juin 2010
 * 
 * @todo Description
 * 
 */
public interface IParameter {

	public abstract String getName();

	public abstract String getTitle();

	public abstract String getCategory();

	public abstract String getUnitLabel();

	public abstract Integer getDefinitionOrder();

	public abstract void setValue(Object value);

	public abstract Object value(IScope scope) throws GamaRuntimeException;

	public abstract IType type();

	public String toGaml();

	public abstract Object getInitialValue();

	public abstract Number getMinValue();

	public abstract Number getMaxValue();

	public abstract List getAmongValue();

	public abstract boolean isEditable();

	public abstract boolean isLabel();

	public abstract boolean allowsTooltip();

	public abstract Number getStepValue();

	public interface Batch extends IParameter {

		public Object value();

		public void setCategory(String cat);

		public void reinitRandomly();

		public abstract Set<Object> neighbourValues() throws GamaRuntimeException;

		public void setEditable(boolean b);

		public boolean canBeExplored();

	}

	public abstract void reinitIfNull();
}
