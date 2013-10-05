/*
* generated by Xtext
*/
package msi.gama.lang.gaml.ui.contentassist.antlr;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.RecognitionException;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.ui.editor.contentassist.antlr.AbstractContentAssistParser;
import org.eclipse.xtext.ui.editor.contentassist.antlr.FollowElement;
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.AbstractInternalContentAssistParser;

import com.google.inject.Inject;

import msi.gama.lang.gaml.services.GamlGrammarAccess;

public class GamlParser extends AbstractContentAssistParser {
	
	@Inject
	private GamlGrammarAccess grammarAccess;
	
	private Map<AbstractElement, String> nameMappings;
	
	@Override
	protected msi.gama.lang.gaml.ui.contentassist.antlr.internal.InternalGamlParser createParser() {
		msi.gama.lang.gaml.ui.contentassist.antlr.internal.InternalGamlParser result = new msi.gama.lang.gaml.ui.contentassist.antlr.internal.InternalGamlParser(null);
		result.setGrammarAccess(grammarAccess);
		return result;
	}
	
	@Override
	protected String getRuleName(AbstractElement element) {
		if (nameMappings == null) {
			nameMappings = new HashMap<AbstractElement, String>() {
				private static final long serialVersionUID = 1L;
				{
					put(grammarAccess.getModelAccess().getAlternatives(), "rule__Model__Alternatives");
					put(grammarAccess.getStatementAccess().getAlternatives(), "rule__Statement__Alternatives");
					put(grammarAccess.getStatementAccess().getAlternatives_1(), "rule__Statement__Alternatives_1");
					put(grammarAccess.getStatementAccess().getAlternatives_1_1(), "rule__Statement__Alternatives_1_1");
					put(grammarAccess.getS_1Expr_Facets_BlockOrEndAccess().getAlternatives_4(), "rule__S_1Expr_Facets_BlockOrEnd__Alternatives_4");
					put(grammarAccess.getS_DoAccess().getAlternatives_4(), "rule__S_Do__Alternatives_4");
					put(grammarAccess.getS_IfAccess().getElseAlternatives_4_1_0(), "rule__S_If__ElseAlternatives_4_1_0");
					put(grammarAccess.getS_OtherAccess().getAlternatives_2(), "rule__S_Other__Alternatives_2");
					put(grammarAccess.getS_DeclarationAccess().getAlternatives(), "rule__S_Declaration__Alternatives");
					put(grammarAccess.getS_SpeciesAccess().getAlternatives_4(), "rule__S_Species__Alternatives_4");
					put(grammarAccess.getS_ExperimentAccess().getNameAlternatives_2_0(), "rule__S_Experiment__NameAlternatives_2_0");
					put(grammarAccess.getS_DefinitionAccess().getNameAlternatives_2_0(), "rule__S_Definition__NameAlternatives_2_0");
					put(grammarAccess.getS_DefinitionAccess().getAlternatives_5(), "rule__S_Definition__Alternatives_5");
					put(grammarAccess.getS_ActionAccess().getAlternatives_6(), "rule__S_Action__Alternatives_6");
					put(grammarAccess.getS_AssignmentAccess().getAlternatives(), "rule__S_Assignment__Alternatives");
					put(grammarAccess.getS_SetAccess().getAlternatives_2(), "rule__S_Set__Alternatives_2");
					put(grammarAccess.getS_EquationsAccess().getAlternatives_3(), "rule__S_Equations__Alternatives_3");
					put(grammarAccess.getS_EquationAccess().getExprAlternatives_0_0(), "rule__S_Equation__ExprAlternatives_0_0");
					put(grammarAccess.getS_SolveAccess().getAlternatives_4(), "rule__S_Solve__Alternatives_4");
					put(grammarAccess.getS_DisplayAccess().getNameAlternatives_2_0(), "rule__S_Display__NameAlternatives_2_0");
					put(grammarAccess.getDisplayStatementAccess().getAlternatives(), "rule__DisplayStatement__Alternatives");
					put(grammarAccess.getSpeciesOrGridDisplayStatementAccess().getAlternatives_3(), "rule__SpeciesOrGridDisplayStatement__Alternatives_3");
					put(grammarAccess.get_SpeciesKeyAccess().getAlternatives(), "rule___SpeciesKey__Alternatives");
					put(grammarAccess.get_1Expr_Facets_BlockOrEnd_KeyAccess().getAlternatives(), "rule___1Expr_Facets_BlockOrEnd_Key__Alternatives");
					put(grammarAccess.get_LayerKeyAccess().getAlternatives(), "rule___LayerKey__Alternatives");
					put(grammarAccess.get_VarOrConstKeyAccess().getAlternatives(), "rule___VarOrConstKey__Alternatives");
					put(grammarAccess.get_ReflexKeyAccess().getAlternatives(), "rule___ReflexKey__Alternatives");
					put(grammarAccess.get_AssignmentKeyAccess().getAlternatives(), "rule___AssignmentKey__Alternatives");
					put(grammarAccess.getFacetAccess().getAlternatives(), "rule__Facet__Alternatives");
					put(grammarAccess.getFirstFacetKeyAccess().getAlternatives(), "rule__FirstFacetKey__Alternatives");
					put(grammarAccess.getDefinitionFacetKeyAccess().getAlternatives(), "rule__DefinitionFacetKey__Alternatives");
					put(grammarAccess.getTypeFacetKeyAccess().getAlternatives(), "rule__TypeFacetKey__Alternatives");
					put(grammarAccess.getSpecialFacetKeyAccess().getAlternatives(), "rule__SpecialFacetKey__Alternatives");
					put(grammarAccess.getClassicFacetAccess().getAlternatives_0(), "rule__ClassicFacet__Alternatives_0");
					put(grammarAccess.getDefinitionFacetAccess().getNameAlternatives_1_0(), "rule__DefinitionFacet__NameAlternatives_1_0");
					put(grammarAccess.getFunctionFacetAccess().getAlternatives_0(), "rule__FunctionFacet__Alternatives_0");
					put(grammarAccess.getTypeFacetAccess().getAlternatives_1(), "rule__TypeFacet__Alternatives_1");
					put(grammarAccess.getBlockAccess().getAlternatives_2(), "rule__Block__Alternatives_2");
					put(grammarAccess.getExpressionAccess().getAlternatives(), "rule__Expression__Alternatives");
					put(grammarAccess.getArgumentPairAccess().getAlternatives_0_0(), "rule__ArgumentPair__Alternatives_0_0");
					put(grammarAccess.getArgumentPairAccess().getOpAlternatives_0_0_1_0_0(), "rule__ArgumentPair__OpAlternatives_0_0_1_0_0");
					put(grammarAccess.getComparisonAccess().getOpAlternatives_1_0_1_0(), "rule__Comparison__OpAlternatives_1_0_1_0");
					put(grammarAccess.getAdditionAccess().getOpAlternatives_1_0_1_0(), "rule__Addition__OpAlternatives_1_0_1_0");
					put(grammarAccess.getMultiplicationAccess().getOpAlternatives_1_0_1_0(), "rule__Multiplication__OpAlternatives_1_0_1_0");
					put(grammarAccess.getUnaryAccess().getAlternatives(), "rule__Unary__Alternatives");
					put(grammarAccess.getUnaryAccess().getAlternatives_1_1(), "rule__Unary__Alternatives_1_1");
					put(grammarAccess.getUnaryAccess().getOpAlternatives_1_1_1_0_0(), "rule__Unary__OpAlternatives_1_1_1_0_0");
					put(grammarAccess.getPrimaryAccess().getAlternatives(), "rule__Primary__Alternatives");
					put(grammarAccess.getAbstractRefAccess().getAlternatives(), "rule__AbstractRef__Alternatives");
					put(grammarAccess.getFunctionAccess().getAlternatives_3(), "rule__Function__Alternatives_3");
					put(grammarAccess.getParameterAccess().getAlternatives_1(), "rule__Parameter__Alternatives_1");
					put(grammarAccess.getParameterAccess().getBuiltInFacetKeyAlternatives_1_0_0(), "rule__Parameter__BuiltInFacetKeyAlternatives_1_0_0");
					put(grammarAccess.getGamlDefinitionAccess().getAlternatives(), "rule__GamlDefinition__Alternatives");
					put(grammarAccess.getEquationDefinitionAccess().getAlternatives(), "rule__EquationDefinition__Alternatives");
					put(grammarAccess.getTypeDefinitionAccess().getAlternatives(), "rule__TypeDefinition__Alternatives");
					put(grammarAccess.getVarDefinitionAccess().getAlternatives(), "rule__VarDefinition__Alternatives");
					put(grammarAccess.getVarDefinitionAccess().getAlternatives_1(), "rule__VarDefinition__Alternatives_1");
					put(grammarAccess.getActionDefinitionAccess().getAlternatives(), "rule__ActionDefinition__Alternatives");
					put(grammarAccess.getValid_IDAccess().getAlternatives(), "rule__Valid_ID__Alternatives");
					put(grammarAccess.getTerminalExpressionAccess().getAlternatives(), "rule__TerminalExpression__Alternatives");
					put(grammarAccess.getModelAccess().getGroup_0(), "rule__Model__Group_0__0");
					put(grammarAccess.getModelAccess().getGroup_1(), "rule__Model__Group_1__0");
					put(grammarAccess.getImportAccess().getGroup(), "rule__Import__Group__0");
					put(grammarAccess.getS_1Expr_Facets_BlockOrEndAccess().getGroup(), "rule__S_1Expr_Facets_BlockOrEnd__Group__0");
					put(grammarAccess.getS_DoAccess().getGroup(), "rule__S_Do__Group__0");
					put(grammarAccess.getS_LoopAccess().getGroup(), "rule__S_Loop__Group__0");
					put(grammarAccess.getS_IfAccess().getGroup(), "rule__S_If__Group__0");
					put(grammarAccess.getS_IfAccess().getGroup_4(), "rule__S_If__Group_4__0");
					put(grammarAccess.getS_OtherAccess().getGroup(), "rule__S_Other__Group__0");
					put(grammarAccess.getS_ReturnAccess().getGroup(), "rule__S_Return__Group__0");
					put(grammarAccess.getS_SpeciesAccess().getGroup(), "rule__S_Species__Group__0");
					put(grammarAccess.getS_ExperimentAccess().getGroup(), "rule__S_Experiment__Group__0");
					put(grammarAccess.getS_ReflexAccess().getGroup(), "rule__S_Reflex__Group__0");
					put(grammarAccess.getS_ReflexAccess().getGroup_3(), "rule__S_Reflex__Group_3__0");
					put(grammarAccess.getS_DefinitionAccess().getGroup(), "rule__S_Definition__Group__0");
					put(grammarAccess.getS_DefinitionAccess().getGroup_3(), "rule__S_Definition__Group_3__0");
					put(grammarAccess.getS_ActionAccess().getGroup(), "rule__S_Action__Group__0");
					put(grammarAccess.getS_ActionAccess().getGroup_4(), "rule__S_Action__Group_4__0");
					put(grammarAccess.getS_VarAccess().getGroup(), "rule__S_Var__Group__0");
					put(grammarAccess.getS_DirectAssignmentAccess().getGroup(), "rule__S_DirectAssignment__Group__0");
					put(grammarAccess.getS_DirectAssignmentAccess().getGroup_0(), "rule__S_DirectAssignment__Group_0__0");
					put(grammarAccess.getS_SetAccess().getGroup(), "rule__S_Set__Group__0");
					put(grammarAccess.getS_EquationsAccess().getGroup(), "rule__S_Equations__Group__0");
					put(grammarAccess.getS_EquationsAccess().getGroup_3_0(), "rule__S_Equations__Group_3_0__0");
					put(grammarAccess.getS_EquationsAccess().getGroup_3_0_1(), "rule__S_Equations__Group_3_0_1__0");
					put(grammarAccess.getS_EquationAccess().getGroup(), "rule__S_Equation__Group__0");
					put(grammarAccess.getS_SolveAccess().getGroup(), "rule__S_Solve__Group__0");
					put(grammarAccess.getS_DisplayAccess().getGroup(), "rule__S_Display__Group__0");
					put(grammarAccess.getDisplayBlockAccess().getGroup(), "rule__DisplayBlock__Group__0");
					put(grammarAccess.getSpeciesOrGridDisplayStatementAccess().getGroup(), "rule__SpeciesOrGridDisplayStatement__Group__0");
					put(grammarAccess.get_AssignmentKeyAccess().getGroup_2(), "rule___AssignmentKey__Group_2__0");
					put(grammarAccess.getParametersAccess().getGroup(), "rule__Parameters__Group__0");
					put(grammarAccess.getActionArgumentsAccess().getGroup(), "rule__ActionArguments__Group__0");
					put(grammarAccess.getActionArgumentsAccess().getGroup_1(), "rule__ActionArguments__Group_1__0");
					put(grammarAccess.getArgumentDefinitionAccess().getGroup(), "rule__ArgumentDefinition__Group__0");
					put(grammarAccess.getArgumentDefinitionAccess().getGroup_2(), "rule__ArgumentDefinition__Group_2__0");
					put(grammarAccess.getClassicFacetKeyAccess().getGroup(), "rule__ClassicFacetKey__Group__0");
					put(grammarAccess.getClassicFacetAccess().getGroup(), "rule__ClassicFacet__Group__0");
					put(grammarAccess.getDefinitionFacetAccess().getGroup(), "rule__DefinitionFacet__Group__0");
					put(grammarAccess.getFunctionFacetAccess().getGroup(), "rule__FunctionFacet__Group__0");
					put(grammarAccess.getTypeFacetAccess().getGroup(), "rule__TypeFacet__Group__0");
					put(grammarAccess.getTypeFacetAccess().getGroup_1_0(), "rule__TypeFacet__Group_1_0__0");
					put(grammarAccess.getActionFacetAccess().getGroup(), "rule__ActionFacet__Group__0");
					put(grammarAccess.getVarFacetAccess().getGroup(), "rule__VarFacet__Group__0");
					put(grammarAccess.getBlockAccess().getGroup(), "rule__Block__Group__0");
					put(grammarAccess.getBlockAccess().getGroup_2_0(), "rule__Block__Group_2_0__0");
					put(grammarAccess.getBlockAccess().getGroup_2_0_0(), "rule__Block__Group_2_0_0__0");
					put(grammarAccess.getBlockAccess().getGroup_2_1(), "rule__Block__Group_2_1__0");
					put(grammarAccess.getArgumentPairAccess().getGroup(), "rule__ArgumentPair__Group__0");
					put(grammarAccess.getArgumentPairAccess().getGroup_0(), "rule__ArgumentPair__Group_0__0");
					put(grammarAccess.getArgumentPairAccess().getGroup_0_0_0(), "rule__ArgumentPair__Group_0_0_0__0");
					put(grammarAccess.getArgumentPairAccess().getGroup_0_0_1(), "rule__ArgumentPair__Group_0_0_1__0");
					put(grammarAccess.getPairAccess().getGroup(), "rule__Pair__Group__0");
					put(grammarAccess.getPairAccess().getGroup_1(), "rule__Pair__Group_1__0");
					put(grammarAccess.getPairAccess().getGroup_1_0(), "rule__Pair__Group_1_0__0");
					put(grammarAccess.getIfAccess().getGroup(), "rule__If__Group__0");
					put(grammarAccess.getIfAccess().getGroup_1(), "rule__If__Group_1__0");
					put(grammarAccess.getIfAccess().getGroup_1_3(), "rule__If__Group_1_3__0");
					put(grammarAccess.getOrAccess().getGroup(), "rule__Or__Group__0");
					put(grammarAccess.getOrAccess().getGroup_1(), "rule__Or__Group_1__0");
					put(grammarAccess.getAndAccess().getGroup(), "rule__And__Group__0");
					put(grammarAccess.getAndAccess().getGroup_1(), "rule__And__Group_1__0");
					put(grammarAccess.getCastAccess().getGroup(), "rule__Cast__Group__0");
					put(grammarAccess.getCastAccess().getGroup_1(), "rule__Cast__Group_1__0");
					put(grammarAccess.getCastAccess().getGroup_1_0(), "rule__Cast__Group_1_0__0");
					put(grammarAccess.getComparisonAccess().getGroup(), "rule__Comparison__Group__0");
					put(grammarAccess.getComparisonAccess().getGroup_1(), "rule__Comparison__Group_1__0");
					put(grammarAccess.getComparisonAccess().getGroup_1_0(), "rule__Comparison__Group_1_0__0");
					put(grammarAccess.getAdditionAccess().getGroup(), "rule__Addition__Group__0");
					put(grammarAccess.getAdditionAccess().getGroup_1(), "rule__Addition__Group_1__0");
					put(grammarAccess.getAdditionAccess().getGroup_1_0(), "rule__Addition__Group_1_0__0");
					put(grammarAccess.getMultiplicationAccess().getGroup(), "rule__Multiplication__Group__0");
					put(grammarAccess.getMultiplicationAccess().getGroup_1(), "rule__Multiplication__Group_1__0");
					put(grammarAccess.getMultiplicationAccess().getGroup_1_0(), "rule__Multiplication__Group_1_0__0");
					put(grammarAccess.getBinaryAccess().getGroup(), "rule__Binary__Group__0");
					put(grammarAccess.getBinaryAccess().getGroup_1(), "rule__Binary__Group_1__0");
					put(grammarAccess.getBinaryAccess().getGroup_1_0(), "rule__Binary__Group_1_0__0");
					put(grammarAccess.getUnitAccess().getGroup(), "rule__Unit__Group__0");
					put(grammarAccess.getUnitAccess().getGroup_1(), "rule__Unit__Group_1__0");
					put(grammarAccess.getUnitAccess().getGroup_1_0(), "rule__Unit__Group_1_0__0");
					put(grammarAccess.getUnaryAccess().getGroup_1(), "rule__Unary__Group_1__0");
					put(grammarAccess.getUnaryAccess().getGroup_1_1_0(), "rule__Unary__Group_1_1_0__0");
					put(grammarAccess.getUnaryAccess().getGroup_1_1_1(), "rule__Unary__Group_1_1_1__0");
					put(grammarAccess.getAccessAccess().getGroup(), "rule__Access__Group__0");
					put(grammarAccess.getAccessAccess().getGroup_1(), "rule__Access__Group_1__0");
					put(grammarAccess.getAccessAccess().getGroup_1_0(), "rule__Access__Group_1_0__0");
					put(grammarAccess.getDotAccess().getGroup(), "rule__Dot__Group__0");
					put(grammarAccess.getDotAccess().getGroup_1(), "rule__Dot__Group_1__0");
					put(grammarAccess.getDotAccess().getGroup_1_1(), "rule__Dot__Group_1_1__0");
					put(grammarAccess.getPrimaryAccess().getGroup_2(), "rule__Primary__Group_2__0");
					put(grammarAccess.getPrimaryAccess().getGroup_3(), "rule__Primary__Group_3__0");
					put(grammarAccess.getPrimaryAccess().getGroup_4(), "rule__Primary__Group_4__0");
					put(grammarAccess.getPrimaryAccess().getGroup_5(), "rule__Primary__Group_5__0");
					put(grammarAccess.getPrimaryAccess().getGroup_5_5(), "rule__Primary__Group_5_5__0");
					put(grammarAccess.getFunctionAccess().getGroup(), "rule__Function__Group__0");
					put(grammarAccess.getParameterAccess().getGroup(), "rule__Parameter__Group__0");
					put(grammarAccess.getParameterAccess().getGroup_1_1(), "rule__Parameter__Group_1_1__0");
					put(grammarAccess.getExpressionListAccess().getGroup(), "rule__ExpressionList__Group__0");
					put(grammarAccess.getExpressionListAccess().getGroup_1(), "rule__ExpressionList__Group_1__0");
					put(grammarAccess.getParameterListAccess().getGroup(), "rule__ParameterList__Group__0");
					put(grammarAccess.getParameterListAccess().getGroup_1(), "rule__ParameterList__Group_1__0");
					put(grammarAccess.getUnitRefAccess().getGroup(), "rule__UnitRef__Group__0");
					put(grammarAccess.getVariableRefAccess().getGroup(), "rule__VariableRef__Group__0");
					put(grammarAccess.getTypeRefAccess().getGroup(), "rule__TypeRef__Group__0");
					put(grammarAccess.getTypeRefAccess().getGroup_2(), "rule__TypeRef__Group_2__0");
					put(grammarAccess.getTypeRefAccess().getGroup_2_2(), "rule__TypeRef__Group_2_2__0");
					put(grammarAccess.getSkillRefAccess().getGroup(), "rule__SkillRef__Group__0");
					put(grammarAccess.getActionRefAccess().getGroup(), "rule__ActionRef__Group__0");
					put(grammarAccess.getEquationRefAccess().getGroup(), "rule__EquationRef__Group__0");
					put(grammarAccess.getUnitFakeDefinitionAccess().getGroup(), "rule__UnitFakeDefinition__Group__0");
					put(grammarAccess.getTypeFakeDefinitionAccess().getGroup(), "rule__TypeFakeDefinition__Group__0");
					put(grammarAccess.getActionFakeDefinitionAccess().getGroup(), "rule__ActionFakeDefinition__Group__0");
					put(grammarAccess.getSkillFakeDefinitionAccess().getGroup(), "rule__SkillFakeDefinition__Group__0");
					put(grammarAccess.getVarFakeDefinitionAccess().getGroup(), "rule__VarFakeDefinition__Group__0");
					put(grammarAccess.getEquationFakeDefinitionAccess().getGroup(), "rule__EquationFakeDefinition__Group__0");
					put(grammarAccess.getTerminalExpressionAccess().getGroup_0(), "rule__TerminalExpression__Group_0__0");
					put(grammarAccess.getTerminalExpressionAccess().getGroup_1(), "rule__TerminalExpression__Group_1__0");
					put(grammarAccess.getTerminalExpressionAccess().getGroup_2(), "rule__TerminalExpression__Group_2__0");
					put(grammarAccess.getTerminalExpressionAccess().getGroup_3(), "rule__TerminalExpression__Group_3__0");
					put(grammarAccess.getTerminalExpressionAccess().getGroup_4(), "rule__TerminalExpression__Group_4__0");
					put(grammarAccess.getTerminalExpressionAccess().getGroup_5(), "rule__TerminalExpression__Group_5__0");
					put(grammarAccess.getModelAccess().getNameAssignment_0_1(), "rule__Model__NameAssignment_0_1");
					put(grammarAccess.getModelAccess().getImportsAssignment_0_2(), "rule__Model__ImportsAssignment_0_2");
					put(grammarAccess.getModelAccess().getStatementsAssignment_0_3(), "rule__Model__StatementsAssignment_0_3");
					put(grammarAccess.getModelAccess().getTotoAssignment_1_1(), "rule__Model__TotoAssignment_1_1");
					put(grammarAccess.getModelAccess().getExprAssignment_1_3(), "rule__Model__ExprAssignment_1_3");
					put(grammarAccess.getImportAccess().getImportURIAssignment_1(), "rule__Import__ImportURIAssignment_1");
					put(grammarAccess.getS_1Expr_Facets_BlockOrEndAccess().getKeyAssignment_0(), "rule__S_1Expr_Facets_BlockOrEnd__KeyAssignment_0");
					put(grammarAccess.getS_1Expr_Facets_BlockOrEndAccess().getFirstFacetAssignment_1(), "rule__S_1Expr_Facets_BlockOrEnd__FirstFacetAssignment_1");
					put(grammarAccess.getS_1Expr_Facets_BlockOrEndAccess().getExprAssignment_2(), "rule__S_1Expr_Facets_BlockOrEnd__ExprAssignment_2");
					put(grammarAccess.getS_1Expr_Facets_BlockOrEndAccess().getFacetsAssignment_3(), "rule__S_1Expr_Facets_BlockOrEnd__FacetsAssignment_3");
					put(grammarAccess.getS_1Expr_Facets_BlockOrEndAccess().getBlockAssignment_4_0(), "rule__S_1Expr_Facets_BlockOrEnd__BlockAssignment_4_0");
					put(grammarAccess.getS_DoAccess().getKeyAssignment_0(), "rule__S_Do__KeyAssignment_0");
					put(grammarAccess.getS_DoAccess().getFirstFacetAssignment_1(), "rule__S_Do__FirstFacetAssignment_1");
					put(grammarAccess.getS_DoAccess().getExprAssignment_2(), "rule__S_Do__ExprAssignment_2");
					put(grammarAccess.getS_DoAccess().getFacetsAssignment_3(), "rule__S_Do__FacetsAssignment_3");
					put(grammarAccess.getS_DoAccess().getBlockAssignment_4_0(), "rule__S_Do__BlockAssignment_4_0");
					put(grammarAccess.getS_LoopAccess().getKeyAssignment_0(), "rule__S_Loop__KeyAssignment_0");
					put(grammarAccess.getS_LoopAccess().getNameAssignment_1(), "rule__S_Loop__NameAssignment_1");
					put(grammarAccess.getS_LoopAccess().getFacetsAssignment_2(), "rule__S_Loop__FacetsAssignment_2");
					put(grammarAccess.getS_LoopAccess().getBlockAssignment_3(), "rule__S_Loop__BlockAssignment_3");
					put(grammarAccess.getS_IfAccess().getKeyAssignment_0(), "rule__S_If__KeyAssignment_0");
					put(grammarAccess.getS_IfAccess().getFirstFacetAssignment_1(), "rule__S_If__FirstFacetAssignment_1");
					put(grammarAccess.getS_IfAccess().getExprAssignment_2(), "rule__S_If__ExprAssignment_2");
					put(grammarAccess.getS_IfAccess().getBlockAssignment_3(), "rule__S_If__BlockAssignment_3");
					put(grammarAccess.getS_IfAccess().getElseAssignment_4_1(), "rule__S_If__ElseAssignment_4_1");
					put(grammarAccess.getS_OtherAccess().getKeyAssignment_0(), "rule__S_Other__KeyAssignment_0");
					put(grammarAccess.getS_OtherAccess().getFacetsAssignment_1(), "rule__S_Other__FacetsAssignment_1");
					put(grammarAccess.getS_OtherAccess().getBlockAssignment_2_0(), "rule__S_Other__BlockAssignment_2_0");
					put(grammarAccess.getS_ReturnAccess().getKeyAssignment_0(), "rule__S_Return__KeyAssignment_0");
					put(grammarAccess.getS_ReturnAccess().getFirstFacetAssignment_1(), "rule__S_Return__FirstFacetAssignment_1");
					put(grammarAccess.getS_ReturnAccess().getExprAssignment_2(), "rule__S_Return__ExprAssignment_2");
					put(grammarAccess.getS_SpeciesAccess().getKeyAssignment_0(), "rule__S_Species__KeyAssignment_0");
					put(grammarAccess.getS_SpeciesAccess().getFirstFacetAssignment_1(), "rule__S_Species__FirstFacetAssignment_1");
					put(grammarAccess.getS_SpeciesAccess().getNameAssignment_2(), "rule__S_Species__NameAssignment_2");
					put(grammarAccess.getS_SpeciesAccess().getFacetsAssignment_3(), "rule__S_Species__FacetsAssignment_3");
					put(grammarAccess.getS_SpeciesAccess().getBlockAssignment_4_0(), "rule__S_Species__BlockAssignment_4_0");
					put(grammarAccess.getS_ExperimentAccess().getKeyAssignment_0(), "rule__S_Experiment__KeyAssignment_0");
					put(grammarAccess.getS_ExperimentAccess().getFirstFacetAssignment_1(), "rule__S_Experiment__FirstFacetAssignment_1");
					put(grammarAccess.getS_ExperimentAccess().getNameAssignment_2(), "rule__S_Experiment__NameAssignment_2");
					put(grammarAccess.getS_ExperimentAccess().getFacetsAssignment_3(), "rule__S_Experiment__FacetsAssignment_3");
					put(grammarAccess.getS_ExperimentAccess().getBlockAssignment_4(), "rule__S_Experiment__BlockAssignment_4");
					put(grammarAccess.getS_ReflexAccess().getKeyAssignment_0(), "rule__S_Reflex__KeyAssignment_0");
					put(grammarAccess.getS_ReflexAccess().getFirstFacetAssignment_1(), "rule__S_Reflex__FirstFacetAssignment_1");
					put(grammarAccess.getS_ReflexAccess().getNameAssignment_2(), "rule__S_Reflex__NameAssignment_2");
					put(grammarAccess.getS_ReflexAccess().getExprAssignment_3_1(), "rule__S_Reflex__ExprAssignment_3_1");
					put(grammarAccess.getS_ReflexAccess().getBlockAssignment_4(), "rule__S_Reflex__BlockAssignment_4");
					put(grammarAccess.getS_DefinitionAccess().getTkeyAssignment_0(), "rule__S_Definition__TkeyAssignment_0");
					put(grammarAccess.getS_DefinitionAccess().getFirstFacetAssignment_1(), "rule__S_Definition__FirstFacetAssignment_1");
					put(grammarAccess.getS_DefinitionAccess().getNameAssignment_2(), "rule__S_Definition__NameAssignment_2");
					put(grammarAccess.getS_DefinitionAccess().getArgsAssignment_3_1(), "rule__S_Definition__ArgsAssignment_3_1");
					put(grammarAccess.getS_DefinitionAccess().getFacetsAssignment_4(), "rule__S_Definition__FacetsAssignment_4");
					put(grammarAccess.getS_DefinitionAccess().getBlockAssignment_5_0(), "rule__S_Definition__BlockAssignment_5_0");
					put(grammarAccess.getS_ActionAccess().getKeyAssignment_1(), "rule__S_Action__KeyAssignment_1");
					put(grammarAccess.getS_ActionAccess().getFirstFacetAssignment_2(), "rule__S_Action__FirstFacetAssignment_2");
					put(grammarAccess.getS_ActionAccess().getNameAssignment_3(), "rule__S_Action__NameAssignment_3");
					put(grammarAccess.getS_ActionAccess().getArgsAssignment_4_1(), "rule__S_Action__ArgsAssignment_4_1");
					put(grammarAccess.getS_ActionAccess().getFacetsAssignment_5(), "rule__S_Action__FacetsAssignment_5");
					put(grammarAccess.getS_ActionAccess().getBlockAssignment_6_0(), "rule__S_Action__BlockAssignment_6_0");
					put(grammarAccess.getS_VarAccess().getKeyAssignment_1(), "rule__S_Var__KeyAssignment_1");
					put(grammarAccess.getS_VarAccess().getFirstFacetAssignment_2(), "rule__S_Var__FirstFacetAssignment_2");
					put(grammarAccess.getS_VarAccess().getNameAssignment_3(), "rule__S_Var__NameAssignment_3");
					put(grammarAccess.getS_VarAccess().getFacetsAssignment_4(), "rule__S_Var__FacetsAssignment_4");
					put(grammarAccess.getS_DirectAssignmentAccess().getExprAssignment_0_0(), "rule__S_DirectAssignment__ExprAssignment_0_0");
					put(grammarAccess.getS_DirectAssignmentAccess().getKeyAssignment_0_1(), "rule__S_DirectAssignment__KeyAssignment_0_1");
					put(grammarAccess.getS_DirectAssignmentAccess().getValueAssignment_0_2(), "rule__S_DirectAssignment__ValueAssignment_0_2");
					put(grammarAccess.getS_DirectAssignmentAccess().getFacetsAssignment_0_3(), "rule__S_DirectAssignment__FacetsAssignment_0_3");
					put(grammarAccess.getS_SetAccess().getKeyAssignment_0(), "rule__S_Set__KeyAssignment_0");
					put(grammarAccess.getS_SetAccess().getExprAssignment_1(), "rule__S_Set__ExprAssignment_1");
					put(grammarAccess.getS_SetAccess().getValueAssignment_3(), "rule__S_Set__ValueAssignment_3");
					put(grammarAccess.getS_EquationsAccess().getKeyAssignment_0(), "rule__S_Equations__KeyAssignment_0");
					put(grammarAccess.getS_EquationsAccess().getNameAssignment_1(), "rule__S_Equations__NameAssignment_1");
					put(grammarAccess.getS_EquationsAccess().getFacetsAssignment_2(), "rule__S_Equations__FacetsAssignment_2");
					put(grammarAccess.getS_EquationsAccess().getEquationsAssignment_3_0_1_0(), "rule__S_Equations__EquationsAssignment_3_0_1_0");
					put(grammarAccess.getS_EquationAccess().getExprAssignment_0(), "rule__S_Equation__ExprAssignment_0");
					put(grammarAccess.getS_EquationAccess().getKeyAssignment_1(), "rule__S_Equation__KeyAssignment_1");
					put(grammarAccess.getS_EquationAccess().getValueAssignment_2(), "rule__S_Equation__ValueAssignment_2");
					put(grammarAccess.getS_SolveAccess().getKeyAssignment_0(), "rule__S_Solve__KeyAssignment_0");
					put(grammarAccess.getS_SolveAccess().getFirstFacetAssignment_1(), "rule__S_Solve__FirstFacetAssignment_1");
					put(grammarAccess.getS_SolveAccess().getExprAssignment_2(), "rule__S_Solve__ExprAssignment_2");
					put(grammarAccess.getS_SolveAccess().getFacetsAssignment_3(), "rule__S_Solve__FacetsAssignment_3");
					put(grammarAccess.getS_SolveAccess().getBlockAssignment_4_0(), "rule__S_Solve__BlockAssignment_4_0");
					put(grammarAccess.getS_DisplayAccess().getKeyAssignment_0(), "rule__S_Display__KeyAssignment_0");
					put(grammarAccess.getS_DisplayAccess().getFirstFacetAssignment_1(), "rule__S_Display__FirstFacetAssignment_1");
					put(grammarAccess.getS_DisplayAccess().getNameAssignment_2(), "rule__S_Display__NameAssignment_2");
					put(grammarAccess.getS_DisplayAccess().getFacetsAssignment_3(), "rule__S_Display__FacetsAssignment_3");
					put(grammarAccess.getS_DisplayAccess().getBlockAssignment_4(), "rule__S_Display__BlockAssignment_4");
					put(grammarAccess.getDisplayBlockAccess().getStatementsAssignment_2(), "rule__DisplayBlock__StatementsAssignment_2");
					put(grammarAccess.getSpeciesOrGridDisplayStatementAccess().getKeyAssignment_0(), "rule__SpeciesOrGridDisplayStatement__KeyAssignment_0");
					put(grammarAccess.getSpeciesOrGridDisplayStatementAccess().getExprAssignment_1(), "rule__SpeciesOrGridDisplayStatement__ExprAssignment_1");
					put(grammarAccess.getSpeciesOrGridDisplayStatementAccess().getFacetsAssignment_2(), "rule__SpeciesOrGridDisplayStatement__FacetsAssignment_2");
					put(grammarAccess.getSpeciesOrGridDisplayStatementAccess().getBlockAssignment_3_0(), "rule__SpeciesOrGridDisplayStatement__BlockAssignment_3_0");
					put(grammarAccess.getParametersAccess().getParamsAssignment_1(), "rule__Parameters__ParamsAssignment_1");
					put(grammarAccess.getActionArgumentsAccess().getArgsAssignment_0(), "rule__ActionArguments__ArgsAssignment_0");
					put(grammarAccess.getActionArgumentsAccess().getArgsAssignment_1_1(), "rule__ActionArguments__ArgsAssignment_1_1");
					put(grammarAccess.getArgumentDefinitionAccess().getTypeAssignment_0(), "rule__ArgumentDefinition__TypeAssignment_0");
					put(grammarAccess.getArgumentDefinitionAccess().getNameAssignment_1(), "rule__ArgumentDefinition__NameAssignment_1");
					put(grammarAccess.getArgumentDefinitionAccess().getDefaultAssignment_2_1(), "rule__ArgumentDefinition__DefaultAssignment_2_1");
					put(grammarAccess.getClassicFacetAccess().getKeyAssignment_0_0(), "rule__ClassicFacet__KeyAssignment_0_0");
					put(grammarAccess.getClassicFacetAccess().getKeyAssignment_0_1(), "rule__ClassicFacet__KeyAssignment_0_1");
					put(grammarAccess.getClassicFacetAccess().getKeyAssignment_0_2(), "rule__ClassicFacet__KeyAssignment_0_2");
					put(grammarAccess.getClassicFacetAccess().getExprAssignment_1(), "rule__ClassicFacet__ExprAssignment_1");
					put(grammarAccess.getDefinitionFacetAccess().getKeyAssignment_0(), "rule__DefinitionFacet__KeyAssignment_0");
					put(grammarAccess.getDefinitionFacetAccess().getNameAssignment_1(), "rule__DefinitionFacet__NameAssignment_1");
					put(grammarAccess.getFunctionFacetAccess().getKeyAssignment_0_0(), "rule__FunctionFacet__KeyAssignment_0_0");
					put(grammarAccess.getFunctionFacetAccess().getKeyAssignment_0_1(), "rule__FunctionFacet__KeyAssignment_0_1");
					put(grammarAccess.getFunctionFacetAccess().getExprAssignment_2(), "rule__FunctionFacet__ExprAssignment_2");
					put(grammarAccess.getTypeFacetAccess().getKeyAssignment_0(), "rule__TypeFacet__KeyAssignment_0");
					put(grammarAccess.getTypeFacetAccess().getExprAssignment_1_0_0(), "rule__TypeFacet__ExprAssignment_1_0_0");
					put(grammarAccess.getTypeFacetAccess().getExprAssignment_1_1(), "rule__TypeFacet__ExprAssignment_1_1");
					put(grammarAccess.getActionFacetAccess().getKeyAssignment_0(), "rule__ActionFacet__KeyAssignment_0");
					put(grammarAccess.getActionFacetAccess().getExprAssignment_1(), "rule__ActionFacet__ExprAssignment_1");
					put(grammarAccess.getVarFacetAccess().getKeyAssignment_0(), "rule__VarFacet__KeyAssignment_0");
					put(grammarAccess.getVarFacetAccess().getExprAssignment_1(), "rule__VarFacet__ExprAssignment_1");
					put(grammarAccess.getBlockAccess().getFunctionAssignment_2_0_0_0(), "rule__Block__FunctionAssignment_2_0_0_0");
					put(grammarAccess.getBlockAccess().getStatementsAssignment_2_1_0(), "rule__Block__StatementsAssignment_2_1_0");
					put(grammarAccess.getArgumentPairAccess().getOpAssignment_0_0_0_0(), "rule__ArgumentPair__OpAssignment_0_0_0_0");
					put(grammarAccess.getArgumentPairAccess().getOpAssignment_0_0_1_0(), "rule__ArgumentPair__OpAssignment_0_0_1_0");
					put(grammarAccess.getArgumentPairAccess().getRightAssignment_1(), "rule__ArgumentPair__RightAssignment_1");
					put(grammarAccess.getPairAccess().getOpAssignment_1_0_1(), "rule__Pair__OpAssignment_1_0_1");
					put(grammarAccess.getPairAccess().getRightAssignment_1_1(), "rule__Pair__RightAssignment_1_1");
					put(grammarAccess.getIfAccess().getOpAssignment_1_1(), "rule__If__OpAssignment_1_1");
					put(grammarAccess.getIfAccess().getRightAssignment_1_2(), "rule__If__RightAssignment_1_2");
					put(grammarAccess.getIfAccess().getIfFalseAssignment_1_3_1(), "rule__If__IfFalseAssignment_1_3_1");
					put(grammarAccess.getOrAccess().getOpAssignment_1_1(), "rule__Or__OpAssignment_1_1");
					put(grammarAccess.getOrAccess().getRightAssignment_1_2(), "rule__Or__RightAssignment_1_2");
					put(grammarAccess.getAndAccess().getOpAssignment_1_1(), "rule__And__OpAssignment_1_1");
					put(grammarAccess.getAndAccess().getRightAssignment_1_2(), "rule__And__RightAssignment_1_2");
					put(grammarAccess.getCastAccess().getOpAssignment_1_0_1(), "rule__Cast__OpAssignment_1_0_1");
					put(grammarAccess.getCastAccess().getRightAssignment_1_1(), "rule__Cast__RightAssignment_1_1");
					put(grammarAccess.getComparisonAccess().getOpAssignment_1_0_1(), "rule__Comparison__OpAssignment_1_0_1");
					put(grammarAccess.getComparisonAccess().getRightAssignment_1_1(), "rule__Comparison__RightAssignment_1_1");
					put(grammarAccess.getAdditionAccess().getOpAssignment_1_0_1(), "rule__Addition__OpAssignment_1_0_1");
					put(grammarAccess.getAdditionAccess().getRightAssignment_1_1(), "rule__Addition__RightAssignment_1_1");
					put(grammarAccess.getMultiplicationAccess().getOpAssignment_1_0_1(), "rule__Multiplication__OpAssignment_1_0_1");
					put(grammarAccess.getMultiplicationAccess().getRightAssignment_1_1(), "rule__Multiplication__RightAssignment_1_1");
					put(grammarAccess.getBinaryAccess().getOpAssignment_1_0_1(), "rule__Binary__OpAssignment_1_0_1");
					put(grammarAccess.getBinaryAccess().getRightAssignment_1_1(), "rule__Binary__RightAssignment_1_1");
					put(grammarAccess.getUnitAccess().getOpAssignment_1_0_1(), "rule__Unit__OpAssignment_1_0_1");
					put(grammarAccess.getUnitAccess().getRightAssignment_1_1(), "rule__Unit__RightAssignment_1_1");
					put(grammarAccess.getUnaryAccess().getOpAssignment_1_1_0_0(), "rule__Unary__OpAssignment_1_1_0_0");
					put(grammarAccess.getUnaryAccess().getRightAssignment_1_1_0_1(), "rule__Unary__RightAssignment_1_1_0_1");
					put(grammarAccess.getUnaryAccess().getOpAssignment_1_1_1_0(), "rule__Unary__OpAssignment_1_1_1_0");
					put(grammarAccess.getUnaryAccess().getRightAssignment_1_1_1_1(), "rule__Unary__RightAssignment_1_1_1_1");
					put(grammarAccess.getAccessAccess().getArgsAssignment_1_1(), "rule__Access__ArgsAssignment_1_1");
					put(grammarAccess.getDotAccess().getOpAssignment_1_1_0(), "rule__Dot__OpAssignment_1_1_0");
					put(grammarAccess.getDotAccess().getRightAssignment_1_1_1(), "rule__Dot__RightAssignment_1_1_1");
					put(grammarAccess.getPrimaryAccess().getParamsAssignment_3_2(), "rule__Primary__ParamsAssignment_3_2");
					put(grammarAccess.getPrimaryAccess().getExprsAssignment_4_2(), "rule__Primary__ExprsAssignment_4_2");
					put(grammarAccess.getPrimaryAccess().getLeftAssignment_5_2(), "rule__Primary__LeftAssignment_5_2");
					put(grammarAccess.getPrimaryAccess().getOpAssignment_5_3(), "rule__Primary__OpAssignment_5_3");
					put(grammarAccess.getPrimaryAccess().getRightAssignment_5_4(), "rule__Primary__RightAssignment_5_4");
					put(grammarAccess.getPrimaryAccess().getZAssignment_5_5_1(), "rule__Primary__ZAssignment_5_5_1");
					put(grammarAccess.getFunctionAccess().getActionAssignment_1(), "rule__Function__ActionAssignment_1");
					put(grammarAccess.getFunctionAccess().getParametersAssignment_3_0(), "rule__Function__ParametersAssignment_3_0");
					put(grammarAccess.getFunctionAccess().getArgsAssignment_3_1(), "rule__Function__ArgsAssignment_3_1");
					put(grammarAccess.getParameterAccess().getBuiltInFacetKeyAssignment_1_0(), "rule__Parameter__BuiltInFacetKeyAssignment_1_0");
					put(grammarAccess.getParameterAccess().getLeftAssignment_1_1_0(), "rule__Parameter__LeftAssignment_1_1_0");
					put(grammarAccess.getParameterAccess().getRightAssignment_2(), "rule__Parameter__RightAssignment_2");
					put(grammarAccess.getExpressionListAccess().getExprsAssignment_0(), "rule__ExpressionList__ExprsAssignment_0");
					put(grammarAccess.getExpressionListAccess().getExprsAssignment_1_1(), "rule__ExpressionList__ExprsAssignment_1_1");
					put(grammarAccess.getParameterListAccess().getExprsAssignment_0(), "rule__ParameterList__ExprsAssignment_0");
					put(grammarAccess.getParameterListAccess().getExprsAssignment_1_1(), "rule__ParameterList__ExprsAssignment_1_1");
					put(grammarAccess.getUnitRefAccess().getRefAssignment_1(), "rule__UnitRef__RefAssignment_1");
					put(grammarAccess.getVariableRefAccess().getRefAssignment_1(), "rule__VariableRef__RefAssignment_1");
					put(grammarAccess.getTypeRefAccess().getRefAssignment_1(), "rule__TypeRef__RefAssignment_1");
					put(grammarAccess.getTypeRefAccess().getFirstAssignment_2_1(), "rule__TypeRef__FirstAssignment_2_1");
					put(grammarAccess.getTypeRefAccess().getSecondAssignment_2_2_1(), "rule__TypeRef__SecondAssignment_2_2_1");
					put(grammarAccess.getSkillRefAccess().getRefAssignment_1(), "rule__SkillRef__RefAssignment_1");
					put(grammarAccess.getActionRefAccess().getRefAssignment_1(), "rule__ActionRef__RefAssignment_1");
					put(grammarAccess.getEquationRefAccess().getRefAssignment_1(), "rule__EquationRef__RefAssignment_1");
					put(grammarAccess.getUnitFakeDefinitionAccess().getNameAssignment_1(), "rule__UnitFakeDefinition__NameAssignment_1");
					put(grammarAccess.getTypeFakeDefinitionAccess().getNameAssignment_1(), "rule__TypeFakeDefinition__NameAssignment_1");
					put(grammarAccess.getActionFakeDefinitionAccess().getNameAssignment_1(), "rule__ActionFakeDefinition__NameAssignment_1");
					put(grammarAccess.getSkillFakeDefinitionAccess().getNameAssignment_1(), "rule__SkillFakeDefinition__NameAssignment_1");
					put(grammarAccess.getVarFakeDefinitionAccess().getNameAssignment_1(), "rule__VarFakeDefinition__NameAssignment_1");
					put(grammarAccess.getEquationFakeDefinitionAccess().getNameAssignment_1(), "rule__EquationFakeDefinition__NameAssignment_1");
					put(grammarAccess.getTerminalExpressionAccess().getOpAssignment_0_1(), "rule__TerminalExpression__OpAssignment_0_1");
					put(grammarAccess.getTerminalExpressionAccess().getOpAssignment_1_1(), "rule__TerminalExpression__OpAssignment_1_1");
					put(grammarAccess.getTerminalExpressionAccess().getOpAssignment_2_1(), "rule__TerminalExpression__OpAssignment_2_1");
					put(grammarAccess.getTerminalExpressionAccess().getOpAssignment_3_1(), "rule__TerminalExpression__OpAssignment_3_1");
					put(grammarAccess.getTerminalExpressionAccess().getOpAssignment_4_1(), "rule__TerminalExpression__OpAssignment_4_1");
					put(grammarAccess.getTerminalExpressionAccess().getOpAssignment_5_1(), "rule__TerminalExpression__OpAssignment_5_1");
				}
			};
		}
		return nameMappings.get(element);
	}
	
	@Override
	protected Collection<FollowElement> getFollowElements(AbstractInternalContentAssistParser parser) {
		try {
			msi.gama.lang.gaml.ui.contentassist.antlr.internal.InternalGamlParser typedParser = (msi.gama.lang.gaml.ui.contentassist.antlr.internal.InternalGamlParser) parser;
			typedParser.entryRuleModel();
			return typedParser.getFollowElements();
		} catch(RecognitionException ex) {
			throw new RuntimeException(ex);
		}		
	}
	
	@Override
	protected String[] getInitialHiddenTokens() {
		return new String[] { "RULE_WS", "RULE_ML_COMMENT", "RULE_SL_COMMENT" };
	}
	
	public GamlGrammarAccess getGrammarAccess() {
		return this.grammarAccess;
	}
	
	public void setGrammarAccess(GamlGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}
}
