/*********************************************************************************************
 * 
 *
 * 'GamlDocProcessor.java', in plugin 'msi.gama.processor', is part of the source code of the 
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit http://gama-platform.googlecode.com for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.precompiler;

import java.io.*;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.constant;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.facets;
import msi.gama.precompiler.GamlAnnotations.file;
import msi.gama.precompiler.GamlAnnotations.inside;
import msi.gama.precompiler.GamlAnnotations.operator;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.species;
import msi.gama.precompiler.GamlAnnotations.symbol;
import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.precompiler.GamlAnnotations.var;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.constants.ColorCSS;
import msi.gama.precompiler.doc.DocProcessorAnnotations;
import msi.gama.precompiler.doc.Element.Category;
import msi.gama.precompiler.doc.Element.Operand;
import msi.gama.precompiler.doc.Element.Operator;
import msi.gama.precompiler.doc.utils.TypeConverter;
import msi.gama.precompiler.doc.utils.XMLElements;

import org.w3c.dom.*;

@SupportedAnnotationTypes({ "msi.gama.precompiler.GamlAnnotations.*" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class GamlDocProcessor {

	public static final String BASIC_SKILL = "msi.gaml.skills.Skill";
	
	public static final Character[] cuttingLettersOperatorDoc = {'l'};

	ProcessingEnvironment processingEnv;
	Messager mes;
	TypeConverter tc;
	
	boolean firstParsing;

	// Statistiques values
	int nbrOperators;
	int nbrOperatorsDoc;
	int nbrSkills;
	int nbrSymbols;

	public GamlDocProcessor(final ProcessingEnvironment procEnv) {
		processingEnv = procEnv;
		mes = processingEnv.getMessager();
		firstParsing = true;
		nbrOperators = 0;
		nbrOperatorsDoc = 0;
		nbrSkills = 0;
		nbrSymbols = 0;
		tc = new TypeConverter();
	}

	public void processDocXML(final RoundEnvironment env, final Writer out) {

		DocumentBuilder docBuilder = null;

		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Impossible to create a DocumentBuilder.");
			System.exit(1);
		}

		Document doc = docBuilder.newDocument();

		// Set<? extends Element> setRoot = env.getRootElements();
		org.w3c.dom.Element root = doc.createElement("doc");

		// ////////////////////////////////////////////////
		// /// Parsing of Constants	Categories
		Set<? extends Element> setConstants =
				(Set<? extends Element>) env.getElementsAnnotatedWith(constant.class);
		
		root.appendChild(this.processDocXMLCategories(setConstants, doc,XMLElements.CONSTANTS_CATEGORIES));
		
		// ////////////////////////////////////////////////
		// /// Parsing of Constants		
		root.appendChild(this.processDocXMLConstants(setConstants, doc));		
		
		// ////////////////////////////////////////////////
		// /// Parsing of Operators Categories
		Set<? extends ExecutableElement> setOperatorsCategories =
			(Set<? extends ExecutableElement>) env.getElementsAnnotatedWith(operator.class);
		root.appendChild(this.processDocXMLCategories(setOperatorsCategories, doc,XMLElements.OPERATORS_CATEGORIES));

		// ////////////////////////////////////////////////
		// /// Parsing of Operators
		Set<? extends ExecutableElement> setOperators =
			(Set<? extends ExecutableElement>) env.getElementsAnnotatedWith(operator.class);
		root.appendChild(this.processDocXMLOperators(setOperators, doc));

		// ////////////////////////////////////////////////
		// /// Parsing of Skills
		Set<? extends Element> setSkills = env.getElementsAnnotatedWith(skill.class);
		root.appendChild(this.processDocXMLSkills(setSkills, doc));

		// ////////////////////////////////////////////////
		// /// Parsing of Species
		Set<? extends Element> setSpecies = env.getElementsAnnotatedWith(species.class);
		root.appendChild(this.processDocXMLSpecies(setSpecies, doc));

		// ////////////////////////////////////////////////
		// /// Parsing of Inside statements	(kinds and symbols)
		Set<? extends Element> setStatementsInside = env.getElementsAnnotatedWith(symbol.class);
		root.appendChild(this.processDocXMLStatementsInsideKind(setStatementsInside, doc));
		root.appendChild(this.processDocXMLStatementsInsideSymbol(setStatementsInside, doc));
		
		// ////////////////////////////////////////////////
		// /// Parsing of Statements
		Set<? extends Element> setStatements = env.getElementsAnnotatedWith(symbol.class);
		root.appendChild(this.processDocXMLStatements(setStatements, doc));

		// ////////////////////////////////////////////////
		// /// Parsing of Types 
		Set<? extends Element> setTypes = env.getElementsAnnotatedWith(type.class);
		ArrayList<org.w3c.dom.Element> listEltOperatorsFromTypes = this.processDocXMLOperatorsFromTypes(setTypes, doc);
		
		org.w3c.dom.Element eltOperators = (org.w3c.dom.Element) root.getElementsByTagName(XMLElements.OPERATORS).item(0);
		for(org.w3c.dom.Element eltOp : listEltOperatorsFromTypes){
			eltOperators.appendChild(eltOp);
		}		
		
		// ////////////////////////////////////////////////
		// /// Parsing of Files 
		Set<? extends Element> setFiles = env.getElementsAnnotatedWith(file.class);
		ArrayList<org.w3c.dom.Element> listEltOperatorsFromFiles = this.processDocXMLOperatorsFromFiles(setFiles, doc);
		
		for(org.w3c.dom.Element eltOp : listEltOperatorsFromFiles){
			eltOperators.appendChild(eltOp);
		}	

		
		// //////////////////////
		// Final step:
		doc.appendChild(root);

		// ////////////////////////////////////////////////

		try {
			// Creation of the DOM source
			DOMSource source = new DOMSource(doc);

			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1"); //"UTF-8");//

			transformer.transform(source, result);
			String stringResult = writer.toString();

			final PrintWriter docWriterXML = new PrintWriter(out);
			docWriterXML.append(stringResult).println("");
			docWriterXML.close();

		} catch (Exception e) {
			throw new NullPointerException("Error in the Processor ");
		}
	}

	private org.w3c.dom.Element processDocXMLConstants(
					final Set<? extends Element> set, final Document doc) {
		org.w3c.dom.Element eltConstants = doc.createElement(XMLElements.CONSTANTS);
		for ( Element e : set ) {
			if(e.getAnnotation(constant.class).value().equals(e.getSimpleName().toString())) {
			org.w3c.dom.Element eltConstant = DocProcessorAnnotations.getConstantElt(e.getAnnotation(constant.class), doc, e, mes, tc);
			eltConstants.appendChild(eltConstant);
			} 
			
			if((e.getAnnotation(constant.class).category() != null) && (IConstantCategory.COLOR_CSS.equals(e.getAnnotation(constant.class).category()[0]))) {
				Object[] colorTab = ColorCSS.array;
				for(int i = 0; i<colorTab.length; i+=2){
					org.w3c.dom.Element constantElt = doc.createElement(XMLElements.CONSTANT);		
					constantElt.setAttribute(XMLElements.ATT_CST_NAME, DocProcessorAnnotations.PREFIX_CONSTANT+colorTab[i]);
					constantElt.setAttribute(XMLElements.ATT_CST_VALUE, "r="+((int[])colorTab[i+1])[0]+", g="+((int[])colorTab[i+1])[1]+", b="+((int[])colorTab[i+1])[2]+", alpha="+((int[])colorTab[i+1])[3]);
					constantElt.appendChild(DocProcessorAnnotations.getCategories(e, doc, doc.createElement(XMLElements.CATEGORIES),tc));
					
					eltConstants.appendChild(constantElt);
				}
			}
		}
		return eltConstants;
	}	
	
	private ArrayList<org.w3c.dom.Element> processDocXMLOperatorsFromTypes(
			final Set<? extends Element> set, final Document doc) {
		
		ArrayList<org.w3c.dom.Element> eltOpFromTypes = new ArrayList<org.w3c.dom.Element>();
		for ( Element e : set ) {
			// Operators to be created:
			// - name_type: converts the parameter into the type name_type
			Operator op = new Operator(doc, tc.getProperCategory("Types"), e.getAnnotation(type.class).name());
			op.setOperands(((TypeElement) e).getQualifiedName().toString(), "", e.getAnnotation(type.class).name(), "");
			op.addOperand(new Operand(doc,"val",0,"any"));
			op.setDocumentation("Casts the operand into the type "+ e.getAnnotation(type.class).name());
			
			eltOpFromTypes.add(op.getElementDOM());
		}
		
		return eltOpFromTypes;
	}

	private ArrayList<org.w3c.dom.Element> processDocXMLOperatorsFromFiles(
			final Set<? extends Element> set, final Document doc) {
		
		ArrayList<org.w3c.dom.Element> eltOpFromTypes = new ArrayList<org.w3c.dom.Element>();
		for ( Element e : set ) {
			// Operators to be created:
			// - "is_"+name : test whether the operand parameter is of the given kind of file			
			// - name+"_file": converts the parameter into the type name_type
			Operator op_is = new Operator(doc, tc.getProperCategory("Files"), "is_" + e.getAnnotation(file.class).name(),
					"Tests whether the operand is a "+ e.getAnnotation(file.class).name() + " file.");
			op_is.setOperands(((TypeElement) e).getQualifiedName().toString(), "", "bool", "");
			op_is.addOperand(new Operand(doc,"val",0,"any"));
			// op_is.setDocumentation("Tests whether the operand is a "+ e.getAnnotation(file.class).name() + " file.");

			Operator op_file = new Operator(doc, tc.getProperCategory("Files"), e.getAnnotation(file.class).name() + "_file");
			op_file.setOperands(((TypeElement) e).getQualifiedName().toString(), "", "file", "");
			op_file.addOperand(new Operand(doc, "val", 0, "string"));
			
			String[] tabExtension = e.getAnnotation(file.class).extensions();
			String listExtension = "";
			if(tabExtension.length > 0){
				listExtension = tabExtension[0];
				if(tabExtension.length > 1){
					for(int i = 1; i< tabExtension.length; i++){
						listExtension = listExtension + ", " + tabExtension[i];
					}
				}
			}
			op_file.setDocumentation("Constructs a file of type "+ e.getAnnotation(file.class).name() + ". Allowed extensions are limited to " + listExtension);
			
			eltOpFromTypes.add(op_is.getElementDOM());
			eltOpFromTypes.add(op_file.getElementDOM());
		}
			
		return eltOpFromTypes;
	}

	private org.w3c.dom.Element processDocXMLCategories(
											final Set<? extends Element> set, final Document doc, final String typeElement) {
		org.w3c.dom.Element categories = doc.createElement(typeElement);
		for ( Element e : set ) {
			String[] categoryNames = new String[1];
			// String categoryName;
			if(e.getAnnotation(operator.class) != null && e.getAnnotation(operator.class).category().length > 0) {
				categoryNames =e.getAnnotation(operator.class).category();
			} else if(e.getAnnotation(constant.class) != null && e.getAnnotation(constant.class).category().length > 0) {
				categoryNames =e.getAnnotation(constant.class).category();
			} else {
				categoryNames[0] = tc.getProperCategory(e.getEnclosingElement().getSimpleName().toString());
			}
			
			NodeList nL = categories.getElementsByTagName(XMLElements.CATEGORY);
			
			for(String categoryName : categoryNames) {
				if(!IOperatorCategory.DEPRECATED.equals(categoryName)){
					int i = 0;
					boolean found = false;
					while (!found && i < nL.getLength()) {
						org.w3c.dom.Element elt = (org.w3c.dom.Element) nL.item(i);
						if ( categoryName.equals(tc.getProperCategory(elt.getAttribute(XMLElements.ATT_CAT_ID))) ) {
							found = true;
						}
						i++;
					}
		
					if ( !found ) {
						org.w3c.dom.Element category;
						category = doc.createElement(XMLElements.CATEGORY);
						category.setAttribute(XMLElements.ATT_CAT_ID, categoryName);
						categories.appendChild(category);
					}
				}
			}
		}
		return categories;
	}

	private org.w3c.dom.Element processDocXMLOperators(final Set<? extends ExecutableElement> set,
		final Document doc) {
		org.w3c.dom.Element operators = doc.createElement(XMLElements.OPERATORS);

		for ( ExecutableElement e : set ) {
			nbrOperators++;
			List<? extends VariableElement> args = e.getParameters();
			Set<Modifier> m = e.getModifiers();
			boolean isStatic = m.contains(Modifier.STATIC);
			int arity = 0;

			if ( e.getAnnotation(doc.class) != null &&
				!"".equals(e.getAnnotation(doc.class).deprecated()) ) {
				// We just omit it
				// String strDeprecated = e.getAnnotation(doc.class).deprecated();
				// mes.printMessage(Kind.ERROR, "The deprecative message __" + strDeprecated );
			} else {
				// Look for an already parsed operator with the same name
				org.w3c.dom.Element operator =
						DocProcessorAnnotations.getOperatorElement(operators, e.getAnnotation(operator.class).value()[0]);
				if ( operator == null ) {
					operator = doc.createElement(XMLElements.OPERATOR);
					operator.setAttribute(XMLElements.ATT_OP_ID,
						tc.getProperOperatorName(e.getAnnotation(operator.class).value()[0]));
					operator.setAttribute(XMLElements.ATT_OP_NAME,
						tc.getProperOperatorName(e.getAnnotation(operator.class).value()[0]));
					
					operator.setAttribute(XMLElements.ATT_ALPHABET_ORDER, getAlphabetOrder(e.getAnnotation(operator.class).value()[0]));
				} 
				// Parse the alternative names of the operator
				// we will create one operator markup per alternative name
				for ( String name : e.getAnnotation(operator.class).value() ) {
					if ( !"".equals(name) &&
						!name.equals(e.getAnnotation(operator.class).value()[0]) ) {
						// Look for an already parsed operator with the same name
						org.w3c.dom.Element altElt = DocProcessorAnnotations.getOperatorElement(operators, name);
						if ( altElt == null ) {
							altElt = doc.createElement(XMLElements.OPERATOR);
							altElt.setAttribute(XMLElements.ATT_OP_ID, name);
							altElt.setAttribute(XMLElements.ATT_OP_NAME, name);
							altElt.setAttribute(XMLElements.ATT_OP_ALT_NAME, e.getAnnotation(operator.class).value()[0]);
							altElt.setAttribute(XMLElements.ATT_ALPHABET_ORDER, getAlphabetOrder(name));
							
							altElt.appendChild(DocProcessorAnnotations.getCategories(e,doc,tc));
							operators.appendChild(altElt);
						} else {
							// Show an error in the case where two alternative names do not refer to
							// the same operator
							if ( !e.getAnnotation(operator.class).value()[0].equals(altElt
								.getAttribute(XMLElements.ATT_OP_ALT_NAME)) ) {
								mes.printMessage(Kind.ERROR,
									"The alternative name __" + name +
										"__ is used for two different operators: " +
										e.getAnnotation(operator.class).value()[0] + " and " +
										altElt.getAttribute("alternativeNameOf"));
							}
						}
					}
				}

				// Parse of categories
				
				// Category
				org.w3c.dom.Element categoriesElt;
				if ( operator.getElementsByTagName(XMLElements.OPERATOR_CATEGORIES).getLength() == 0 ) {
					categoriesElt = DocProcessorAnnotations.getCategories(e,doc,doc.createElement(XMLElements.OPERATOR_CATEGORIES),tc);
				} else {
					categoriesElt = DocProcessorAnnotations.getCategories(e,doc,
						(org.w3c.dom.Element) operator.getElementsByTagName(XMLElements.OPERATOR_CATEGORIES).item(0),tc);
				}
				operator.appendChild(categoriesElt);
				
				// operator.appendChild(getOperatorElement(e,doc,operator));
				
				// Parse the combinaison operands / result
				org.w3c.dom.Element combinaisonOpResElt;
				if ( operator.getElementsByTagName(XMLElements.COMBINAISON_IO).getLength() == 0 ) {
					combinaisonOpResElt = doc.createElement(XMLElements.COMBINAISON_IO);
				} else {
					combinaisonOpResElt =
						(org.w3c.dom.Element) operator.getElementsByTagName(XMLElements.COMBINAISON_IO).item(0);
				}

				org.w3c.dom.Element operands = doc.createElement(XMLElements.OPERANDS);
				operands.setAttribute("returnType", tc.getProperType(e.getReturnType().toString()));
				operands.setAttribute("contentType", "" +
					e.getAnnotation(operator.class).content_type());
				operands.setAttribute("type", "" + e.getAnnotation(operator.class).type());

				// To specify where we can find the source code of the class defining the operator
				String pkgName = "" + ((TypeElement) e.getEnclosingElement()).getQualifiedName();
				// Now we have to deal with Spatial operators, that are defined in inner classes
				if ( pkgName.contains("Spatial") ) {
					// We do not take into account what is after 'Spatial'
					pkgName = pkgName.split("Spatial")[0] + "Spatial";
				}
				pkgName = pkgName.replace('.', '/');
				pkgName = pkgName + ".java";
				operands.setAttribute("class", pkgName);

				if ( !isStatic ) {
					org.w3c.dom.Element operand = doc.createElement((XMLElements.OPERAND));
					operand.setAttribute(XMLElements.ATT_OPERAND_TYPE, tc.getProperType(e.getEnclosingElement().asType()
						.toString()));
					operand.setAttribute(XMLElements.ATT_OPERAND_POSITION, "" + arity);
					arity++;
					operand.setAttribute(XMLElements.ATT_OPERAND_NAME, e.getEnclosingElement().asType().toString()
						.toLowerCase());
					operands.appendChild(operand);
				}
				if ( args.size() > 0 ) {
					int first_index = args.get(0).asType().toString().contains("IScope") ? 1 : 0;
					for ( int i = first_index; i <= args.size() - 1; i++ ) {
						org.w3c.dom.Element operand = doc.createElement((XMLElements.OPERAND));
						operand
							.setAttribute(XMLElements.ATT_OPERAND_TYPE, tc.getProperType(args.get(i).asType().toString()));
						operand.setAttribute(XMLElements.ATT_OPERAND_POSITION, "" + arity);
						arity++;
						operand.setAttribute(XMLElements.ATT_OPERAND_NAME, args.get(i).getSimpleName().toString());
						operands.appendChild(operand);
					}
				}
				// operator.setAttribute("arity", ""+arity);
				combinaisonOpResElt.appendChild(operands);
				operator.appendChild(combinaisonOpResElt);

				// /////////////////////////////////////////////////////
				// Parsing of the documentation
				org.w3c.dom.Element docElt;
				if(operator.getElementsByTagName(XMLElements.DOCUMENTATION).getLength() == 0){
					docElt = DocProcessorAnnotations.getDocElt(e.getAnnotation(doc.class), doc, mes, "Operator " + operator.getAttribute("name"), tc, e);
				} else {
					docElt = DocProcessorAnnotations.getDocElt(e.getAnnotation(doc.class), doc,
							(org.w3c.dom.Element) operator.getElementsByTagName(XMLElements.DOCUMENTATION).item(0),
							mes, "Operator " + operator.getAttribute("name"), tc, e);
				}
				
				if(docElt != null){
					operator.appendChild(docElt);
				}
				
				operators.appendChild(operator);
			}
		}
		return operators;
	}

	private org.w3c.dom.Element processDocXMLSkills(final Set<? extends Element> setSkills,
		final Document doc) {

		org.w3c.dom.Element skills = doc.createElement(XMLElements.SKILLS);

		for ( Element e : setSkills ) {
			nbrSkills++;
			org.w3c.dom.Element skillElt = doc.createElement(XMLElements.SKILL);

			skillElt.setAttribute(XMLElements.ATT_SKILL_ID, e.getAnnotation(skill.class).name());
			skillElt.setAttribute(XMLElements.ATT_SKILL_NAME, e.getAnnotation(skill.class).name());

			// get extends
			skillElt.setAttribute(XMLElements.ATT_SKILL_CLASS, ((TypeElement) e).getQualifiedName().toString());
			skillElt.setAttribute(XMLElements.ATT_SKILL_EXTENDS, ((TypeElement) e).getSuperclass().toString());
			
			org.w3c.dom.Element docEltSkill = 
					DocProcessorAnnotations.getDocElt(e.getAnnotation(doc.class), doc, mes, e.getSimpleName().toString(), tc, null);
			if(docEltSkill != null){
				skillElt.appendChild(docEltSkill);
			}

			// Parsing of vars
			if ( e.getAnnotation(vars.class) != null ) {
				org.w3c.dom.Element varsElt = doc.createElement(XMLElements.VARS);
				for ( var v : e.getAnnotation(vars.class).value() ) {
					org.w3c.dom.Element varElt = doc.createElement(XMLElements.VAR);
					varElt.setAttribute(XMLElements.ATT_VAR_NAME, v.name());
					varElt.setAttribute(XMLElements.ATT_VAR_TYPE, tc.getTypeString(Integer.valueOf(v.type())));
					varElt.setAttribute(XMLElements.ATT_VAR_CONSTANT, "" + v.constant());
					
					org.w3c.dom.Element docEltVar = 
							DocProcessorAnnotations.getDocElt(v.doc(), doc, mes, "Var " + v.name() + " from " + skillElt.getAttribute("name"), tc, null);
					if(docEltVar != null){
						varElt.appendChild(docEltVar);
					}
					
					String dependsOn = new String();
					for ( String dependElement : v.depends_on() ) {
						dependsOn = ("".equals(dependsOn) ? "" : dependsOn + ",") + dependElement;
					}
					varElt.setAttribute(XMLElements.ATT_VAR_DEPENDS_ON, dependsOn);
					varsElt.appendChild(varElt);
				}
				skillElt.appendChild(varsElt);
			}

			// Parsing of actions
			org.w3c.dom.Element actionsElt = doc.createElement(XMLElements.ACTIONS);

			for ( Element eltMethod : e.getEnclosedElements() ) {
				org.w3c.dom.Element actionElt = 
						DocProcessorAnnotations.getActionElt(eltMethod.getAnnotation(action.class), doc, mes, eltMethod, tc);		
				
				if(actionElt != null){
					actionsElt.appendChild(actionElt);
				}
			}
			skillElt.appendChild(actionsElt);

			skills.appendChild(skillElt);

			// Skills now have only one name

			// // Addition of other skills for alternative names of the species
			// for ( int i = 1; i < e.getAnnotation(skill.class).name().length; i++ ) {
			// org.w3c.dom.Element skillAlt = doc.createElement("skill");
			// skillAlt.setAttribute("id", e.getAnnotation(skill.class).name()[i]);
			// skillAlt.setAttribute("name", e.getAnnotation(skill.class).name()[i]);
			// skillAlt.setAttribute("alternativeNameOfSkill", id);
			// skills.appendChild(skillAlt);
			// }
		}
		// check the inheritance between Skills
		NodeList nlSkill = skills.getElementsByTagName(XMLElements.SKILL);
		for ( int i = 0; i < nlSkill.getLength(); i++ ) {
			org.w3c.dom.Element elt = (org.w3c.dom.Element) nlSkill.item(i);
			if ( elt.hasAttribute(XMLElements.ATT_SKILL_EXTENDS) ) {
				if ( BASIC_SKILL.equals(elt.getAttribute(XMLElements.ATT_SKILL_EXTENDS)) ) {
					elt.setAttribute(XMLElements.ATT_SKILL_EXTENDS, "");
				} else {
					for ( int j = 0; j < nlSkill.getLength(); j++ ) {
						org.w3c.dom.Element testedElt = (org.w3c.dom.Element) nlSkill.item(j);
						if ( testedElt.getAttribute(XMLElements.ATT_SKILL_CLASS).equals(elt.getAttribute(XMLElements.ATT_SKILL_EXTENDS)) ) {
							elt.setAttribute(XMLElements.ATT_SKILL_EXTENDS, testedElt.getAttribute(XMLElements.ATT_SKILL_NAME));
						}
					}
				}
			}
		}

		return skills;
	}

	private org.w3c.dom.Element processDocXMLSpecies(final Set<? extends Element> setSpecies,
		final Document doc) {
		org.w3c.dom.Element species = doc.createElement(XMLElements.SPECIESS);

		for ( Element e : setSpecies ) {
			org.w3c.dom.Element spec = doc.createElement(XMLElements.SPECIES);
			spec.setAttribute(XMLElements.ATT_SPECIES_ID, e.getAnnotation(species.class).name());
			spec.setAttribute(XMLElements.ATT_SPECIES_NAME, e.getAnnotation(species.class).name());
			
			org.w3c.dom.Element docEltSkill = 
					DocProcessorAnnotations.getDocElt(e.getAnnotation(doc.class), doc, mes, e.getSimpleName().toString(), tc, null);
			if(docEltSkill != null){
				spec.appendChild(docEltSkill);
			}

			// Parsing of actions
			org.w3c.dom.Element actionsElt = doc.createElement(XMLElements.ACTIONS);
			for ( Element eltMethod : e.getEnclosedElements() ) {
				org.w3c.dom.Element actionElt = 
						DocProcessorAnnotations.getActionElt(eltMethod.getAnnotation(action.class), doc, mes, eltMethod, tc);		
				
				if(actionElt != null){
					actionsElt.appendChild(actionElt);
				}
			}
			spec.appendChild(actionsElt);
			
			species.appendChild(spec);
		}
		return species;
	}

	private org.w3c.dom.Element processDocXMLStatementsInsideSymbol(final Set<? extends Element> setStatement,
			final Document doc) {
			org.w3c.dom.Element statementsInsideSymbolElt = doc.createElement(XMLElements.INSIDE_STAT_SYMBOLS);
			ArrayList<String> insideStatementSymbol = new ArrayList<String>();
			
			for ( Element e : setStatement ) {
				inside insideAnnot = e.getAnnotation(inside.class);
				
				if(insideAnnot != null){
					for(String sym : insideAnnot.symbols()){
						if( !insideStatementSymbol.contains(sym) ) { insideStatementSymbol.add(sym); }
					}		
				}					
			}
			
			for(String insName : insideStatementSymbol) {
				org.w3c.dom.Element insideStatElt = doc.createElement(XMLElements.INSIDE_STAT_SYMBOL);		
				insideStatElt.setAttribute(XMLElements.ATT_INSIDE_STAT_SYMBOL, insName);
				statementsInsideSymbolElt.appendChild(insideStatElt);
			}
			
		return statementsInsideSymbolElt;
	}

	private org.w3c.dom.Element processDocXMLStatementsInsideKind(final Set<? extends Element> setStatement,
			final Document doc) {
			org.w3c.dom.Element statementsInsideKindElt = doc.createElement(XMLElements.INSIDE_STAT_KINDS);
			ArrayList<String> insideStatementKind = new ArrayList<String>();
			
			for ( Element e : setStatement ) {
				inside insideAnnot = e.getAnnotation(inside.class);
				
				if(insideAnnot != null){
					for(int kind : insideAnnot.kinds()){
						String kindStr = tc.getSymbolKindStringFromISymbolKind(kind);
						if( !insideStatementKind.contains(kindStr) ) { insideStatementKind.add(kindStr); }						
					}			
				}					
			}
			
			for(String insName : insideStatementKind) {
				org.w3c.dom.Element insideStatElt = doc.createElement(XMLElements.INSIDE_STAT_KIND);		
				insideStatElt.setAttribute(XMLElements.ATT_INSIDE_STAT_SYMBOL, insName);
				statementsInsideKindElt.appendChild(insideStatElt);
			}
			
		return statementsInsideKindElt;
	}	
	
	private org.w3c.dom.Element processDocXMLStatements(final Set<? extends Element> setStatement,
		final Document doc) {
		org.w3c.dom.Element statementsElt = doc.createElement(XMLElements.STATEMENTS);

		for ( Element e : setStatement ) {
			nbrSymbols++;
			org.w3c.dom.Element statElt = doc.createElement(XMLElements.STATEMENT);
			if ( e.getAnnotation(symbol.class).name().length != 0 ) {
				statElt.setAttribute(XMLElements.ATT_STAT_ID, e.getAnnotation(symbol.class).name()[0]);
				statElt.setAttribute(XMLElements.ATT_STAT_NAME, e.getAnnotation(symbol.class).name()[0]);
			} else {
				// TODO : case of variables declarations ... Variable, ContainerVariable,
				// NumberVariable
			}
			statElt.setAttribute(XMLElements.ATT_STAT_KIND, tc.getSymbolKindStringFromISymbolKind(e.getAnnotation(symbol.class).kind()));

			// Parsing of facets
			org.w3c.dom.Element facetsElt = 
				DocProcessorAnnotations.getFacetsElt(e.getAnnotation(facets.class), doc, mes, statElt.getAttribute(XMLElements.ATT_STAT_NAME), tc);
			if(facetsElt != null){
				statElt.appendChild(facetsElt);
			}

			// Parsing of documentation
			org.w3c.dom.Element docstatElt = 
				DocProcessorAnnotations.getDocElt(e.getAnnotation(doc.class), doc, mes, "Statement " + statElt.getAttribute(XMLElements.ATT_STAT_NAME), tc, null);
			if(docstatElt != null){
				statElt.appendChild(docstatElt);
			}

			// Parsing of inside
			org.w3c.dom.Element insideElt = 
				DocProcessorAnnotations.getInsideElt(e.getAnnotation(inside.class), doc, tc);
			if(insideElt != null){
				statElt.appendChild(insideElt);
			}
			
			statementsElt.appendChild(statElt);
		}
		return statementsElt;
	}
	
	public static String getAlphabetOrder(String name) {
		String order = "";
		String lastChar = "z";
		
		for(int i = 0; i < cuttingLettersOperatorDoc.length ; i++){
			Character previousChar = (i==0) ? 'a' : cuttingLettersOperatorDoc[i-1];
			Character c = cuttingLettersOperatorDoc[i];
			
			if((i==0 && name.compareTo(c.toString()) < 0) 
					|| (name.compareTo(previousChar.toString()) >= 0 && name.compareTo(c.toString()) < 0)){   // name is < to cutting letter
				order = previousChar.toString() + ((Character)(Character.toChars(c-1))[0]).toString();
			} 
		}
		if ("".equals(order)) order = cuttingLettersOperatorDoc[cuttingLettersOperatorDoc.length-1].toString() + lastChar;
		
		return order;
	}
	
}