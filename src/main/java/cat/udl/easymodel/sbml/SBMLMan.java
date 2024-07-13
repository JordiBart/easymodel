package cat.udl.easymodel.sbml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.ExplicitRule;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.xml.XMLNode;


import cat.udl.easymodel.controller.ContextUtils;
import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaArrayValue;
import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.formula.FormulaValue;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.types.SpeciesVarTypeType;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathLinkOp;
import cat.udl.easymodel.utils.ToolboxVaadin;

public class SBMLMan {

	public static final String sbmlVarNamePrefix = "EZMD";
	public static final String keywordFixSufix = "EZMD";
	public static final int substituteFormulaRecursionIterations = 1;
	public static final double stochasticFactor = (1 / (6.23E-8));
	private SharedData sharedData = SharedData.getInstance();

	public SBMLMan() {
	}

	public String exportSBML(cat.udl.easymodel.logic.model.Model m, MathLinkOp mathLinkOp) throws Exception {
		SBMLDocument doc = new SBMLDocument(SharedData.sbmlLevel, SharedData.sbmlVersion);
		// doc.addTreeNodeChangeListener(new SBMLTreeNodeChangeListener());

		// Create a new SBML model, and add a compartment to it.
		Model model = doc.createModel("model");
		model.setName(m.getName());
		model.setNotes(fixStringForHtml(m.getDescription()));
		Unit uM2 = new Unit();
		uM2.setKind(Kind.METRE);
		uM2.setScale(0);
		uM2.setExponent(2d);
		uM2.setMultiplier(1d);
		UnitDefinition udM2 = model.createUnitDefinition("m2");
		udM2.setName("m2");
		udM2.addUnit(uM2);

		model.setExtentUnits("mole");
		model.setLengthUnits("metre");
		model.setAreaUnits(udM2);
		model.setVolumeUnits("litre");
		model.setTimeUnits("second");
		model.setSubstanceUnits("mole");

		Compartment compartment = model.createCompartment("EasyModelComp");
		compartment.setName("EasyModelComp");
		compartment.setSize(100d);
		compartment.setConstant(true); // constant size
		compartment.setSpatialDimensions(3);

		// Create a model history object and add author information to it.
		// History hist = model.getHistory(); // Will create the History, if it does not
		// exist
		// Creator creator = new Creator("", "", "", "");
		// hist.addCreator(creator);

		SortedMap<String, Species> speciesMap = new TreeMap<>();
		for (String spKey : m.getAllSpecies().keySet()) {
			cat.udl.easymodel.logic.model.Species sp = m.getAllSpecies().get(spKey);
			speciesMap.put(spKey, model.createSpecies(spKey, compartment));
			speciesMap.get(spKey).setName(speciesMap.get(spKey).getId());
			speciesMap.get(spKey).setHasOnlySubstanceUnits(false); // if true, can only contain amount, not
																	// concentration
			if (sp.getVarType() == SpeciesVarTypeType.INDEPENDENT) {
				speciesMap.get(spKey).setBoundaryCondition(true);
				speciesMap.get(spKey).setConstant(true);
			} else {
				speciesMap.get(spKey).setBoundaryCondition(false);
				speciesMap.get(spKey).setConstant(false);
			}
			speciesMap.get(spKey).setInitialConcentration(Double.valueOf(sp.getConcentration()));
		}

		for (cat.udl.easymodel.logic.model.Reaction r : m) {
			Reaction rsb = model.createReaction("reaction" + r.getIdJavaStr());
			rsb.setName(rsb.getId());
			rsb.setReversible(false); // substrates <-> products
			for (String sp : r.getLeftPartSpecies().keySet()) {
				SpeciesReference sr = rsb.createReactant(speciesMap.get(sp));
				sr.setStoichiometry(r.getLeftPartSpecies().get(sp));
				sr.setConstant(speciesMap.get(sp).isConstant());
			}
			for (String sp : r.getRightPartSpecies().keySet()) {
				SpeciesReference sr = rsb.createProduct(speciesMap.get(sp));
				sr.setStoichiometry(r.getRightPartSpecies().get(sp));
				sr.setConstant(speciesMap.get(sp).isConstant());
			}
			for (String sp : r.getModifiers().keySet()) {
				ModifierSpeciesReference msr = rsb.createModifier(speciesMap.get(sp));
			}

			KineticLaw kl = new KineticLaw(rsb);
			kl.setId("KL" + r.getIdJavaStr());
			kl.setName(r.getFormula().getNameToShow());
			for (String par : r.getFormulaGeneralParameters().keySet()) {
				FormulaValue fv = r.getFormulaGeneralParameters().get(par);
				if (fv.getType() == FormulaValueType.CONSTANT) {
					LocalParameter lp = new LocalParameter(par);
					lp.setName(lp.getId());
					lp.setValue(Double.valueOf(fv.getValue()));
					kl.addLocalParameter(lp);
//					System.out.println(lp.toString());
				}
			}
			for (String par : r.getFormulaSubstratesArrayParameters().keySet()) {
				SortedMap<String, FormulaArrayValue> parMap = r.getFormulaSubstratesArrayParameters().get(par);
				for (String parBySpecies : parMap.keySet()) {
					Double val = Double.valueOf(parMap.get(parBySpecies).getValue());
					LocalParameter lp = new LocalParameter(ContextUtils.arrayContext.replaceAll("`", "") + par
							+ ContextUtils.substrateContext.replaceAll("`", "") + parBySpecies);
					lp.setName(lp.getId());
					lp.setValue(val);
					kl.addLocalParameter(lp);
				}
			}
			for (String par : r.getFormulaModifiersArrayParameters().keySet()) {
				SortedMap<String, FormulaArrayValue> parMap = r.getFormulaModifiersArrayParameters().get(par);
				for (String parBySpecies : parMap.keySet()) {
					Double val = Double.valueOf(parMap.get(parBySpecies).getValue());
					LocalParameter lp = new LocalParameter(ContextUtils.arrayContext.replaceAll("`", "") + par
							+ ContextUtils.modifierContext.replaceAll("`", "") + parBySpecies);
					lp.setName(lp.getId());
					lp.setValue(val);
					kl.addLocalParameter(lp);
				}
			}
			String formula = mathLinkOp.evaluateToString("InputForm[ReleaseHold[Hold["
					+ r.getFormula().getMathematicaReadyFormula(r.getMathematicaContext(), m)
					+ "]/.SubsFormVars/.ParArrayInVars/.ParVarVals]]");
//			p.p("mathematica formula: " + formula + " raw " + r.getFormula().getFormulaDef());
			formula = convertToSBMLFormula(formula);
//			p.p("converted SBML formula: " + formula);
			kl.setFormula(formula);
		}

		// CHECK SBML
		doc.checkConsistency();
		SBMLErrorLog errorLog = doc.getListOfErrors();
		if (errorLog.getErrorCount() > 0) {
			String excMsg = "SBML check errors: " + errorLog.getErrorCount() + "\n";
			for (int i = 0; i < errorLog.getErrorCount(); i++) {
				SBMLError err = errorLog.getError(i);
				excMsg += err.getMessage() + "\n";
			}
			throw new Exception(excMsg);
		}

		// GENERATE SBML
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SBMLWriter.write(doc, baos, SharedData.appName, SharedData.appVersion);
		return baos.toString("UTF-8");
	}

	private String fixStringForHtml(String str) {
		return str.replaceAll("<", "&lt;");
	}

	private String convertToSBMLFormula(String formula) {
		String currentChar = null;
		String part = "";
		String sbmlFormula = "";
		int pos;
		for (int i = 0; i <= formula.length(); i++) {
			if (i < formula.length())
				currentChar = String.valueOf(formula.charAt(i));
			if (!currentChar.matches("\\w|`") || i == formula.length()) {
				// currentChar=math operator
				if (!part.isEmpty()) {
					pos = part.indexOf("`"); // first reaction context
					if (pos != -1)
						part = part.substring(pos + 1);
					part = part.replaceAll("`", ""); // array parameter context (eg g)
					sbmlFormula += part;
				}
				if (i != formula.length())
					sbmlFormula += currentChar;
				part = "";
			} else
				part += currentChar;
		}
		return fixMathematicaNumbers(sbmlFormula);
	}

	private String fixMathematicaNumbers(String sbmlFormula) {
		String toParseRegEx = "(?=(?:[^a-zA-Z0-9_]|^)([0-9]+\\.?[0-9]*\\*\\^[\\+|\\-]?[0-9]+)(?:[^a-zA-Z0-9_]|$))";
		Pattern pattern = Pattern.compile(toParseRegEx);
		String result = "";
		String matcherStr = sbmlFormula;
		Matcher matcher = pattern.matcher(matcherStr);
		String match;
//		p.p("in "+sbmlFormula);
		while (matcher.find()) {
			match = matcherStr.substring(matcher.start(1), matcher.end(1));
			String[] parts = match.split("\\*\\^");
			Double d = Double.valueOf(parts[0]) * Math.pow(10d, Double.valueOf(parts[1]));
			result += matcherStr.substring(0, matcher.start(1)) + sharedData.doubleToString(d);
//	            	System.out.println("new "+formula);
			matcherStr = matcherStr.substring(matcher.end(1));
			matcher = pattern.matcher(matcherStr);
		}
		result += matcherStr;
//		p.p("ou "+result);
		return removeUnnecessaryDecimalPoint(result);
	}

	private String removeUnnecessaryDecimalPoint(String sbmlFormula) {
		String toParseRegEx = "(?=(?:[^a-zA-Z0-9_]|^)([0-9]+\\.)(?:[^a-zA-Z0-9_]|$))";
		Pattern pattern = Pattern.compile(toParseRegEx);
		String result = "";
		String matcherStr = sbmlFormula;
		Matcher matcher = pattern.matcher(matcherStr);
		String match;
//		p.p("in "+sbmlFormula);
		while (matcher.find()) {
			match = matcherStr.substring(matcher.start(1), matcher.end(1));
			result += matcherStr.substring(0, matcher.start(1)) + match.substring(0, match.length() - 1);
			matcherStr = matcherStr.substring(matcher.end(1));
			matcher = pattern.matcher(matcherStr);
		}
		result += matcherStr;
//		p.p("ou "+result);
		return result;
	}

	public cat.udl.easymodel.logic.model.Model importSBML(ByteArrayInputStream bais, StringBuilder report,
			String namePrefix, boolean isBatch) throws Exception {
		SBMLDocument doc = SBMLReader.read(bais);
		cat.udl.easymodel.logic.model.Model m = new cat.udl.easymodel.logic.model.Model();
		m.setRepositoryType(RepositoryType.TEMP);

		Model model = doc.getModel();
		m.setName((namePrefix != null ? namePrefix : "") + model.getName());
		if (model.getNotesString() != null)
			m.setDescription(ToolboxVaadin.sanitizeHTML(model.getNotesString()));//

		// fix sbml IDs
//		for (Species sp : model.getListOfSpecies())
//			sp.setId(getFixedSBMLId(sp.getId()));
//		for (Compartment c : model.getListOfCompartments())
//			c.setId(getFixedSBMLId(c.getId()));
//		for (Parameter p : model.getListOfParameters())
//			p.setId(getFixedSBMLId(p.getId()));
		// add reactions
		for (Reaction rs : model.getListOfReactions()) {
			cat.udl.easymodel.logic.model.Reaction r = new cat.udl.easymodel.logic.model.Reaction(
					getReactionStringFromReactionSBML(rs));
//			p.p(r.getReactionStr());
			m.addReaction(r);
			// p.p(FormulaUtils.getSBMLFormulaKeywordFix(rs.getKineticLaw().getFormula(),
			// keywordFixSufix));
			// add formula
		}
		// set initial concentration/type of variable / check stochastic model
		cat.udl.easymodel.logic.model.Species emSp;
		for (Species sp : model.getListOfSpecies()) {
			emSp = m.getAllSpecies().get(getFixedSBMLId(sp.getId()));
			if (emSp == null)
				continue; // filter out species not present in EasyModel model reactions
			if (!Double.isNaN(sp.getInitialConcentration())) {
				emSp.setConcentration(getDoubleWithStandardNotation(sp.getInitialConcentration()));
			}
			if (!Double.isNaN(sp.getInitialAmount())) {
				emSp.setAmount(getDoubleWithStandardNotation(sp.getInitialAmount()));
				if (emSp.getConcentration() == null) { // stochastic: no concentration but amount
					emSp.setConcentration(getDoubleWithStandardNotation(sp.getInitialAmount() * stochasticFactor));
				}
			}
			if (emSp.getConcentration() == null && emSp.getAmount() == null) {
				emSp.setConcentration("1");
				report.append("Initial concentration/amount for " + sp.getId()
						+ " was lacking and concentration was set to 1\n");// (rule?
				// "+(model.getRuleByVariable(sp.getId())!=null?"y":"n")+")\n");
			}
			emSp.setVarType(sp.isConstant() ? SpeciesVarTypeType.INDEPENDENT : SpeciesVarTypeType.TIME_DEP);
		}
		// set formulas + pars
		int i = 0;
		for (Reaction rs : model.getListOfReactions()) {
			cat.udl.easymodel.logic.model.Reaction r = m.get(i);
			String adaptedFormula = getSBMLFormulaKeywordFix(
					substituteRecursiveFormulaVars(rs.getKineticLaw().getFormula(), rs, model, true, null, 0));
			if (isBatch) {
				// Mathematica is used TODO AVOID
//				String testFormulaWithMathematica = sessionData.getMathLinkOp().evaluateToString(adaptedFormula);
//			System.out.println(adaptedFormula+" -> "+testFormulaWithMathematica);
//				if ("0".equals(testFormulaWithMathematica)) {
//					report.append("Formula 0 found " + adaptedFormula + "\n");
//					if (adaptedFormula.equals("0"))
//						adaptedFormula = "k";
//					else
//						adaptedFormula = getSBMLFormulaKeywordFix(substituteRecursiveFormulaVars(
//								rs.getKineticLaw().getFormula(), rs, model, false, null, 0));
//				}
			}
			Formula f = new Formula(m.getFormulas().getNextFormulaNameByModelShortName(), adaptedFormula,
					FormulaType.MODEL, m);
			if (!f.isValid())
				throw new Exception("Invalid EM formula " + f.getFormulaDef() + " in reaction " + rs.getId());
			m.getFormulas().addFormula(f);
			r.setFormula(f);
//			p.m(f.getGenericParameters());
//			p.p(f.getFormulaDef());
//			p.m2(r.getFormulaValues());
			// set formula parameter values
			formulaParsLoop: for (String par : r.getFormulaGeneralParameters().keySet()) {
//				p.p(f.getFormulaDef() + " " +rs.getKineticLaw().getFormula()+ " "+par);
				LocalParameter lp = findLocalPar(rs, par);
				if (lp != null) {
					r.getFormulaGeneralParameters().put(par, new FormulaValue(par, FormulaValueType.CONSTANT,
							getDoubleWithStandardNotation(lp.getValue())));
				} else {
					if (r.getLeftPartSpecies().containsKey(par)) {
						r.getFormulaGeneralParameters().put(par, new FormulaValue(par, FormulaValueType.SUBSTRATE, par));
					} else if (r.getModifiers().containsKey(par)) {
						r.getFormulaGeneralParameters().put(par, new FormulaValue(par, FormulaValueType.MODIFIER, par));
					} else if (m.getAllSpecies().containsKey(par)) {
//						throw new Exception("Product detected as par " + par + " in formula " + f.getFormulaDef()
//								+ " in XMLreaction " + rs.getId());
						r.setReactionStr(r.getReactionStr() + ";" + par); // add product also as modifier
						r.getFormulaGeneralParameters().put(par, new FormulaValue(par, FormulaValueType.MODIFIER, par));
					} else {
						// check global SBML compartments
						for (Compartment c : model.getListOfCompartments()) {
							if (par.equals(getFixedSBMLId(c.getId()))) {
								r.getFormulaGeneralParameters().put(par, new FormulaValue(par, FormulaValueType.CONSTANT, "1"));
								continue formulaParsLoop;
							}
						}
						// global SBML parameters
						for (Parameter p : model.getListOfParameters()) {
							if (par.equals(getFixedSBMLId(p.getId()))) {
								if (!Double.isNaN(p.getValue())) {
									r.getFormulaGeneralParameters().put(par, new FormulaValue(par, FormulaValueType.CONSTANT,
											getDoubleWithStandardNotation(p.getValue())));
								} else {
									r.getFormulaGeneralParameters().put(par,
											new FormulaValue(par, FormulaValueType.CONSTANT, "1"));
									report.append(
											"Global parameter " + par + " value was lacking and it was reset to 1\n");
//									ExplicitRule er = model.getRuleByVariable(p.getId());
//									FunctionDefinition fd = model.getFunctionDefinition(p.getId());
//									if (er != null)
//										throw new Exception(er.getFormula() + " Rule detected (recursion?) as par "
//												+ par + " in formula " + f.getFormulaDef() + " (original= "
//												+ rs.getKineticLaw().getFormula() + ") in XMLreaction " + rs.getId());
//									else if (fd != null)
//										throw new Exception(fd.getFormula() + " custom function detected as par " + par
//												+ " in formula " + f.getFormulaDef() + " in XMLreaction " + rs.getId());
//									else
//										throw new Exception("Variable " + par
//												+ " found in SBML parameter list but it's non-constant and can't find its reference. Formula "
//												+ f.getFormulaDef() + " in XMLreaction " + rs.getId());
								}
								continue formulaParsLoop;
							}
						}
						// check in ALL species (they may not be on reactions!)
						for (Species sp : model.getListOfSpecies()) {
							if (par.equals(getFixedSBMLId(sp.getId()))) {
								if (sp.isConstant()
										&& !"NaN".equals(getDoubleWithStandardNotation(sp.getInitialConcentration()))) {
									r.getFormulaGeneralParameters().put(par, new FormulaValue(par, FormulaValueType.CONSTANT,
											getDoubleWithStandardNotation(sp.getInitialConcentration())));
									continue formulaParsLoop;
								} else if (sp.isConstant()
										&& !"NaN".equals(getDoubleWithStandardNotation(sp.getInitialAmount()))
										&& sp.getInitialAmount() != 0) {
									r.getFormulaGeneralParameters().put(par,
											new FormulaValue(par, FormulaValueType.CONSTANT, "0"));
									continue formulaParsLoop;
								}
//								else
//									throw new Exception("Species " + sp.getId() + " detected as par of formula,"
//											+ " but species is not constant/invalid concentration value. Formula " + f.getFormulaDef()
//											+ " in XMLreaction " + rs.getId());
							}
						}
						String resetVal = "1";
						if ("pi".equals(par))
							resetVal = "3.14";
						r.getFormulaGeneralParameters().put(par, new FormulaValue(par, FormulaValueType.CONSTANT, resetVal));
						report.append("Parameter value for parameter " + par + " was lacking and it was reset to "
								+ resetVal + "\n");
//						throw new Exception("Can't find par val for " + par + " in formula " + f.getFormulaDef()
//								+ " in XMLreaction " + rs.getId());
					}
				}
			}
			i++;
		}
		return m;
	}

	private String findEventAssignmentFormula(Model model, String id) {
		for (Event e : model.getListOfEvents()) {
			for (EventAssignment ea : e.getListOfEventAssignments()) {
				if (id.equals(ea.getVariable())) {
					return ea.getFormula();
				}
			}
		}
		return null;
	}

	private String substituteRecursiveFormulaVars(String rawSBMLformula, Reaction rs, Model model,
			boolean replaceNonConstantPar, ArrayList<String> processedVars, int level) throws Exception { // start
																											// level=0
		String toParseRegEx = "(?=(?:[^a-zA-Z0-9_]|^)([a-zA-Z_][a-zA-Z0-9_]*)(?:[^a-zA-Z0-9_]|$))";
		Pattern pattern = Pattern.compile(toParseRegEx);
		String result = "";
		String matcherStr = rawSBMLformula;
		Matcher matcher = pattern.matcher(matcherStr);
		String replace;
		String var;
//		p.p("in "+matcherStr+ " r"+rs.getId()+" level "+level);
		while (matcher.find()) {
			replace = null;
			var = matcherStr.substring(matcher.start(1), matcher.end(1));
//			System.out.println(var);
			Parameter par = model.getParameter(var);
			if (par != null && !par.isConstant() && replaceNonConstantPar) {
				ExplicitRule er = model.getRuleByVariable(par.getId());
				if (er != null) {
					replace = er.getFormula();
				} else {
					String eventAssignmentFormula = findEventAssignmentFormula(model, par.getId());
					if (eventAssignmentFormula != null) {
						replace = eventAssignmentFormula;
					}
				}
				if (replace != null) {// && var.equals(search)) {
					if (level == 0)
						processedVars = new ArrayList<>();
					if (!processedVars.contains(var)) { // from level 1 on, children receive a list of vars
						processedVars.add(var);
						replace = "(" + substituteRecursiveFormulaVars(replace, rs, model, replaceNonConstantPar,
								processedVars, level + 1) + ")";
						processedVars.remove(processedVars.size() - 1); // remove added var in this same level
					} else {
						if (!"NaN".equals(String.valueOf(par.getValue()))) {
							replace = String.valueOf(par.getValue());
						} else {
							replace = "1";
//							throw new Exception(
//									"Formula infinite recursion detected! Formula " + rs.getKineticLaw().getFormula()
//											+ " variable recursion: " + processedVars.toString()
//											+ " last recursion formula " + rawSBMLformula + " Reaction " + rs.getId());
						}
					}
//            		p.p("replace "+replace+" in place of var "+var+"in raw formula "+rawSBMLformula);
//					System.out.println("replace " + var + " for " + replace + " in formula " + rawSBMLformula);
					result += matcherStr.substring(0, matcher.start(1)) + replace;
//	            	System.out.println("new "+formula);
					matcherStr = matcherStr.substring(matcher.end(1));
					matcher = pattern.matcher(matcherStr);
				}
			}
		}
		result += matcherStr;
//		p.p("ou "+matcherStr+ " r"+rs.getId()+" level "+level);
		return result;
	}

	private String getFormulaFromModelEvents(Model model, String var) {
		String formula = null;
		for (Event e : model.getListOfEvents()) {
//			e.getEventAssignment(eventAssignmentId)
		}
		return formula;
	}

	private LocalParameter findLocalPar(Reaction rs, String searchPar) {
		LocalParameter res = null;
		for (LocalParameter lp : rs.getKineticLaw().getListOfLocalParameters()) {
			if (getFixedSBMLId(lp.getId()).equals(searchPar)) {
				res = lp;
				break;
			}
		}
		return res;
	}

	private String getBodyContent(XMLNode node, String parentTag) {
		if (node != null) {
			if ("".equals(node.getName()) && "p".equals(parentTag)) { // node.getName() = html tag
				return node.getCharacters();
			} else {
				for (int i = 0; i < node.getChildCount(); i++) {
					XMLNode child = node.getChild(i);
					String chars = getBodyContent(child, node.getName());
					if (!chars.equals(""))
						return chars;
				}
			}
		}
		return "";
	}

	public String getFixedSBMLId(String name) {
		if (name == null || name.matches("\\d+(\\.\\d+)?"))
			return name;
		String res = name.replaceAll("_", ""); // amb Mathematica _ = \[LetterSpace]
		if (res.matches(FormulaUtils.getInstance().getKeywordsRegEx()))
			res = res + keywordFixSufix;
		else if (res.matches("[0-9]+[a-zA-Z][0-9a-zA-Z]*"))
			res = sbmlVarNamePrefix + res;
//		p.p(res);
		return res;
	}

	private String getReactionStringFromReactionSBML(Reaction r) {
		String res = "";
		for (int i = 0; i < r.getReactantCount(); i++) {
			SpeciesReference sr = r.getReactant(i);
			if (i > 0)
				res += " + ";
			res += getFixedStoichiometryWithMultiplier(sr.getStoichiometry()) + getFixedSBMLId(sr.getSpeciesInstance().getId());
		}
		res += " -> ";
		for (int i = 0; i < r.getProductCount(); i++) {
			SpeciesReference sr = r.getProduct(i);
			if (i > 0)
				res += " + ";
			res += getFixedStoichiometryWithMultiplier(sr.getStoichiometry()) + getFixedSBMLId(sr.getSpeciesInstance().getId());
		}
		for (int i = 0; i < r.getModifierCount(); i++) {
			ModifierSpeciesReference sr = r.getModifier(i);
			res += ";" + getFixedSBMLId(sr.getSpeciesInstance().getId());
		}
		return res;
	}

	private String getFixedStoichiometryWithMultiplier(double sbmlStoi) {
		String stoiNumberStr = String.format("%.0f", sbmlStoi); // remove decimal part
		stoiNumberStr = scientificNotationToStandard(stoiNumberStr);
		if (stoiNumberStr.equals("0") || stoiNumberStr.equals("1"))
			return "";
		else
			return stoiNumberStr+"*";
	}

	private String replaceFormulaVariable(String formula, String search, String replace) {
		replace = "(" + replace + ")";
		String toParseRegEx = "(?=(?:[^a-zA-Z0-9]|^)(" + search + ")(?:[^a-zA-Z0-9]|$))";
		Pattern pattern = Pattern.compile(toParseRegEx);
		Matcher matcher = pattern.matcher(formula);

		String var;
		int lastEndIndex;
		for (int i = 0; i < SBMLMan.substituteFormulaRecursionIterations; i++) {
			lastEndIndex = -1;
			while (matcher.find()) {
				var = formula.substring(matcher.start(1), matcher.end(1));
//	            p.p("found "+(matcher.group(1))+"  var "+var);
				if (lastEndIndex < matcher.end(1)) {// && var.equals(search)) {
					lastEndIndex = matcher.end(1) + replace.length();
//	            	System.out.println("replace "+search+" for "+replace+" in formula "+formula);
					formula = formula.substring(0, matcher.start(1)) + replace + formula.substring(matcher.end(1));
//	            	System.out.println("new "+formula);
					matcher = pattern.matcher(formula);
				}
			}
		}
		return formula;
	}

	private String getSBMLFormulaKeywordFix(String formula) {
		if (formula.length() == 0)
			return formula;
//		p.p(formula);
		formula = removeScientificNotationFromNumbers(formula);
		String newStr = "";
		String part = "";
		String currentChar = null;
		for (int i = 0; i <= formula.length(); i++) {
			if (i < formula.length())
				currentChar = String.valueOf(formula.charAt(i));
			if (!currentChar.matches("\\w") || i == formula.length()) {
				if (!part.isEmpty()) {
					newStr += getFixedSBMLId(part);
				}
				part = "";
				if (i < formula.length()) {
					newStr += currentChar; // +-*/
				}
			} else
				part += currentChar;
		}
//		p.p(newStr);
		return newStr;
	}

	private String removeScientificNotationFromNumbers(String str) {
		String toParseRegEx = "(?=(?:[^a-zA-Z0-9\\.]|^)(([0-9]+\\.)?[0-9]+E[\\+\\-]?[0-9]+)(?:[^a-zA-Z0-9\\.]|$))";
		Pattern pattern = Pattern.compile(toParseRegEx);
		Matcher matcher = pattern.matcher(str);

		String numWithE;
		while (matcher.find()) {
			numWithE = str.substring(matcher.start(1), matcher.end(1));
//	        System.out.println("FOUND "+numWithE+" in "+str);
			str = str.substring(0, matcher.start(1)) + scientificNotationToStandard(numWithE)
					+ str.substring(matcher.end(1));
//	        System.out.println("after subs "+str);
			matcher = pattern.matcher(str);
		}
		return str;
	}

	public String scientificNotationToStandard(String numWithE) {
		if (numWithE == null || !numWithE.matches("^([0-9]+\\.)?[0-9]+E[\\+\\-]?[0-9]+$"))
			return numWithE;
//		p.p("in "+numWithE);
		numWithE.replaceAll("\\+", "");
		String[] parts = numWithE.split("E");
//		p.p("pow "+parts[0]+" to "+parts[1]);
		Double d = Double.valueOf(parts[0]) * Math.pow(10d, Double.valueOf(parts[1]));
//		p.p("out "+sharedData.doubleToString(d));
		return sharedData.doubleToString(d);
	}

	public String getDoubleWithStandardNotation(double d) {
		// p.p(scientificNotationToStandard(String.valueOf(d)).length());
		return scientificNotationToStandard(String.valueOf(d));
	}

	private void updateSpeciesReferencesId(Reaction r, String oldId, String newId) {
		for (int i = 0; i < r.getReactantCount(); i++) {
			SpeciesReference sr = r.getReactant(i);
			if (sr.getId().equals(oldId))
				sr.setId(newId);
		}
		for (int i = 0; i < r.getProductCount(); i++) {
			SpeciesReference sr = r.getProduct(i);
			if (sr.getId().equals(oldId))
				sr.setId(newId);
		}
		for (int i = 0; i < r.getModifierCount(); i++) {
			ModifierSpeciesReference sr = r.getModifier(i);
			if (sr.getId().equals(oldId))
				sr.setId(newId);
		}
	}
}
