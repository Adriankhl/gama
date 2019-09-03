package gama.extensions.bdi;

import java.util.Map;

import gama.processor.annotations.IConcept;
import gama.processor.annotations.GamlAnnotations.doc;
import gama.processor.annotations.GamlAnnotations.type;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.runtime.scope.IScope;
import msi.gaml.types.GamaType;
import msi.gaml.types.IType;

@SuppressWarnings("unchecked")
@type(name = "predicate", id = PredicateType.id, wraps = { Predicate.class }, concept = { IConcept.TYPE, IConcept.BDI })
@doc("represents a predicate")
public class PredicateType extends GamaType<Predicate> {

	public final static int id = IType.AVAILABLE_TYPES + 546654;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	@doc("cast an object as a predicate")
	public Predicate cast(final IScope scope, final Object obj, final Object val, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof Predicate) {
			return (Predicate) obj;
		}
		if (obj instanceof String) {
			return new Predicate((String) obj);
		}
		if (obj != null && obj instanceof Map) {
			final Map<String, Object> map = (Map<String, Object>) obj;
			final String nm = (String) (map.containsKey("name") ? map.get("name") : "predicate");
			final Map values = (Map) (map.containsKey("name") ? map.get("values") : null);
			return new Predicate(nm, values);
		}
		return null;
	}

	@Override
	public Predicate getDefault() {
		return null;
	}

}
