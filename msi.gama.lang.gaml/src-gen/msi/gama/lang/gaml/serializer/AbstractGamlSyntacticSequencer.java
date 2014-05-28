package msi.gama.lang.gaml.serializer;

import com.google.inject.Inject;
import java.util.List;
import msi.gama.lang.gaml.services.GamlGrammarAccess;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.AbstractElementAlias;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.AlternativeAlias;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.GroupAlias;
import org.eclipse.xtext.serializer.analysis.GrammarAlias.TokenAlias;
import org.eclipse.xtext.serializer.analysis.ISyntacticSequencerPDAProvider.ISynNavigable;
import org.eclipse.xtext.serializer.analysis.ISyntacticSequencerPDAProvider.ISynTransition;
import org.eclipse.xtext.serializer.sequencer.AbstractSyntacticSequencer;

@SuppressWarnings("all")
public abstract class AbstractGamlSyntacticSequencer extends AbstractSyntacticSequencer {

	protected GamlGrammarAccess grammarAccess;
	protected AbstractElementAlias match_S_Equations_SemicolonKeyword_3_1_or___LeftCurlyBracketKeyword_3_0_0_RightCurlyBracketKeyword_3_0_2__;
	protected AbstractElementAlias match_S_Set_LessThanSignHyphenMinusKeyword_2_1_or_ValueKeyword_2_0;
	
	@Inject
	protected void init(IGrammarAccess access) {
		grammarAccess = (GamlGrammarAccess) access;
		match_S_Equations_SemicolonKeyword_3_1_or___LeftCurlyBracketKeyword_3_0_0_RightCurlyBracketKeyword_3_0_2__ = new AlternativeAlias(false, false, new GroupAlias(false, false, new TokenAlias(false, false, grammarAccess.getS_EquationsAccess().getLeftCurlyBracketKeyword_3_0_0()), new TokenAlias(false, false, grammarAccess.getS_EquationsAccess().getRightCurlyBracketKeyword_3_0_2())), new TokenAlias(false, false, grammarAccess.getS_EquationsAccess().getSemicolonKeyword_3_1()));
		match_S_Set_LessThanSignHyphenMinusKeyword_2_1_or_ValueKeyword_2_0 = new AlternativeAlias(false, false, new TokenAlias(false, false, grammarAccess.getS_SetAccess().getLessThanSignHyphenMinusKeyword_2_1()), new TokenAlias(false, false, grammarAccess.getS_SetAccess().getValueKeyword_2_0()));
	}
	
	@Override
	protected String getUnassignedRuleCallToken(EObject semanticObject, RuleCall ruleCall, INode node) {
		return "";
	}
	
	
	@Override
	protected void emitUnassignedTokens(EObject semanticObject, ISynTransition transition, INode fromNode, INode toNode) {
		if (transition.getAmbiguousSyntaxes().isEmpty()) return;
		List<INode> transitionNodes = collectNodes(fromNode, toNode);
		for (AbstractElementAlias syntax : transition.getAmbiguousSyntaxes()) {
			List<INode> syntaxNodes = getNodesFor(transitionNodes, syntax);
			if(match_S_Equations_SemicolonKeyword_3_1_or___LeftCurlyBracketKeyword_3_0_0_RightCurlyBracketKeyword_3_0_2__.equals(syntax))
				emit_S_Equations_SemicolonKeyword_3_1_or___LeftCurlyBracketKeyword_3_0_0_RightCurlyBracketKeyword_3_0_2__(semanticObject, getLastNavigableState(), syntaxNodes);
			else if(match_S_Set_LessThanSignHyphenMinusKeyword_2_1_or_ValueKeyword_2_0.equals(syntax))
				emit_S_Set_LessThanSignHyphenMinusKeyword_2_1_or_ValueKeyword_2_0(semanticObject, getLastNavigableState(), syntaxNodes);
			else acceptNodes(getLastNavigableState(), syntaxNodes);
		}
	}

	/**
	 * Syntax:
	 *     ('{' '}') | ';'
	 */
	protected void emit_S_Equations_SemicolonKeyword_3_1_or___LeftCurlyBracketKeyword_3_0_0_RightCurlyBracketKeyword_3_0_2__(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
	/**
	 * Syntax:
	 *     '<-' | 'value:'
	 */
	protected void emit_S_Set_LessThanSignHyphenMinusKeyword_2_1_or_ValueKeyword_2_0(EObject semanticObject, ISynNavigable transition, List<INode> nodes) {
		acceptNodes(transition, nodes);
	}
	
}
