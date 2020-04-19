/*******************************************************************************************************
 *
 * gaml.types.GamaPathType.java, in plugin gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gaml.types;

import java.util.List;

import gama.common.interfaces.IKeyword;
import gama.metamodel.shape.IShape;
import gama.processor.annotations.IConcept;
import gama.processor.annotations.ISymbolKind;
import gama.processor.annotations.GamlAnnotations.doc;
import gama.processor.annotations.GamlAnnotations.example;
import gama.processor.annotations.GamlAnnotations.type;
import gama.processor.annotations.GamlAnnotations.usage;
import gama.runtime.exceptions.GamaRuntimeException;
import gama.runtime.scope.IScope;
import gama.util.list.GamaListFactory;
import gama.util.list.IList;
import gama.util.path.GamaPath;
import gama.util.path.IPath;
import gama.util.path.PathFactory;
import gaml.operators.Cast;

@type (
		name = IKeyword.PATH,
		id = IType.PATH,
		wraps = { IPath.class, GamaPath.class },
		kind = ISymbolKind.Variable.REGULAR,
		concept = { IConcept.TYPE },
		doc = @doc ("Ordered lists of objects that represent a path in a graph"))
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class GamaPathType extends GamaType<IPath> {

	@doc(value="Cast any object as a path",   
			usages = {
				@usage(value = "if the operand is a path, returns this path"), 
				@usage(value = "if the operand is a geometry of an agent, returns a path from the list of points of the geometry"),
				@usage(value = "if the operand is a list, cast each element of the list as a point and create a path from these points",
					examples = {
						@example("path p <- path([{12,12},{30,30},{50,50}]);")
					}) 
			})
	@Override
	public IPath cast(final IScope scope, final Object obj, final Object param, final boolean copy)
			throws GamaRuntimeException {
		return staticCast(scope, obj, param, copy);
	}

	@Override
	public IPath getDefault() {
		return null;
	}

	public static IPath staticCast(final IScope scope, final Object obj, final Object param, final boolean copy) {
		if (obj instanceof IPath) { return (IPath) obj; }
		if (obj instanceof IShape) { 
			IShape shape = ((IShape) obj);
			return PathFactory.create(scope, (IList<IShape>) shape.getPoints(), false);
		}
		 
		if (obj instanceof List) {
			// List<GamaPoint> list = new GamaList();
			final List<IShape> list = GamaListFactory.create(Types.GEOMETRY);
			boolean isEdges = true;

			for (final Object p : (List) obj) {
				list.add(Cast.asPoint(scope, p));
				if (isEdges && !(p instanceof IShape && ((IShape) p).isLine())) {
					isEdges = false;
				}
			}
			// return new GamaPath(scope.getTopology(), list);
			return PathFactory.create(scope, isEdges ? (IList<IShape>) obj : (IList<IShape>) list, isEdges);
		}
		return null;
	}

	@Override
	public boolean isDrawable() {
		return true;
	}

	@Override
	public boolean canCastToConst() {
		return false;
	}

}