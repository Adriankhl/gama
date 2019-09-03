/*******************************************************************************************************
 *
 * msi.gama.outputs.layers.ILayerStatement.java, in plugin msi.gama.core, is part of the source code of the GAMA
 * modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.common.interfaces.outputs;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.runtime.IStepable;
import msi.gaml.compilation.interfaces.ISymbol;
import msi.gaml.expressions.IExpression;

/**
 * The class ILayerStatement. Supports the GAML definition of layers in a display
 *
 * @author drogoul
 * @since 14 d�c. 2011
 *
 */
public interface ILayerStatement extends IStepable, ISymbol, Comparable<ILayerStatement> {

	public enum LayerType {

		GRID(IKeyword.GRID),
		AGENTS(IKeyword.AGENTS),
		GRID_AGENTS("grid_agents"),
		SPECIES(IKeyword.SPECIES),
		IMAGE(IKeyword.IMAGE),
		GIS(IKeyword.GIS),
		CHART(IKeyword.CHART),
		EVENT(IKeyword.EVENT),
		GRAPHICS(IKeyword.GRAPHICS),
		OVERLAY(IKeyword.OVERLAY),
		CAMERA(IKeyword.CAMERA),
		LIGHT("light");

		private final String name;

		LayerType(final String s) {
			name = s;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	LayerType getType(boolean isOpenGL);

	void setDisplayOutput(IDisplayOutput output);

	IExpression getRefreshFacet();

	boolean isToCreate();

}