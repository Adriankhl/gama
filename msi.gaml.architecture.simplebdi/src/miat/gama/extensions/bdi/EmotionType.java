package miat.gama.extensions.bdi;

import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.GamaType;
import msi.gaml.types.IType;

@SuppressWarnings("unchecked")
@type(name = "emotion", id = EmotionType.id, wraps = { Emotion.class }, concept = { IConcept.TYPE, IConcept.BDI })
@doc("represents the type emotion")
public class EmotionType extends GamaType<Emotion> {

	public final static int id = IType.AVAILABLE_TYPES + 546656;

	@Override
	public boolean canCastToConst() {
		return true;
	}

	@Override
	@doc("cast an object instance of emotion as an emotion")
	public Emotion cast(final IScope scope, final Object obj, final Object val, final boolean copy)
			throws GamaRuntimeException {
		if (obj instanceof Emotion) {
			return (Emotion) obj;
		}
		return null;
	}

	@Override
	public Emotion getDefault() {
		// TODO Auto-generated method stub
		return null;
	}

}
