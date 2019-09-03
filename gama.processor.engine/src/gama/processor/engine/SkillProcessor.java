package gama.processor.engine;

import javax.lang.model.element.Element;

import gama.processor.annotations.GamlAnnotations.skill;

public class SkillProcessor extends ElementProcessor<skill> {

	@Override
	protected Class<skill> getAnnotationClass() {
		return skill.class;
	}

	@Override
	public void createElement(final StringBuilder sb, final ProcessorContext context, final Element e,
			final skill skill) {
		verifyDoc(context, e, "skill " + skill.name(), skill);
		sb.append(in).append("_skill(").append(toJavaString(skill.name())).append(',')
				.append(toClassObject(rawNameOf(context, e.asType()))).append(',');
		toArrayOfStrings(skill.attach_to(), sb).append(");");
	}

}
