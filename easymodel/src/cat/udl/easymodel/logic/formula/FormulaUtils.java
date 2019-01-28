package cat.udl.easymodel.logic.formula;

import java.util.ArrayList;

import cat.udl.easymodel.sbml.SBMLMan;
import cat.udl.easymodel.utils.p;

public class FormulaUtils {
	public static final String modelShortNameSeparator="_K";
	public static final String mathematicaSymbolPrefix="m:"; //System`
	public static final String mathematicaBuiltInPrefix="b:";
	public static final String mathematicaIndexPrefix="i:";
	public static final String mathematicaPrefixRegex="^("+mathematicaSymbolPrefix+"\\w+|"+mathematicaIndexPrefix+"\\w+)$";
	public static final String mathFormulaOperators = "\\+\\-\\*\\/^";
	public static final String variableRegex = "[a-zA-Z][a-zA-Z0-9]*";
	public static final String mathFormulaExpressionRegex = "(?!.*[a-zA-Z0-9]+\\(.*)^[a-zA-Z0-9\\[\\],{}\\(\\)\\s\\.:"+mathFormulaOperators+"]+$";
	//in mathFormulaExpressionRegex: (?!.*[a-zA-Z0-9]+\\(.*) <- negative look ahead to avoid "function(..."
	private ArrayList<String> builtInVars = new ArrayList<>();
	private ArrayList<String> generalVars = new ArrayList<>();
	private ArrayList<String> keywords = new ArrayList<>();
	private static FormulaUtils thisSingleton = new FormulaUtils();

	private FormulaUtils() {
		builtInVars.add(mathematicaBuiltInPrefix+"X");
		builtInVars.add(mathematicaBuiltInPrefix+"M");
		builtInVars.add(mathematicaBuiltInPrefix+"A");
		builtInVars.add(mathematicaBuiltInPrefix+"XF");
		builtInVars.add(mathematicaBuiltInPrefix+"MF");
		
		generalVars.add(mathematicaBuiltInPrefix+"t");

		for (int i = 0; i < builtInVars.size(); i++)
			keywords.add(builtInVars.get(i));
		for (int i = 0; i < generalVars.size(); i++)
			keywords.add(generalVars.get(i));
	}

	public static FormulaUtils getInstance() {
		return thisSingleton;
	}

	public String getToNotParseRegEx() {
		return getKeywordsRegEx()+"|(\\d+(.\\d+)?)|("+mathematicaSymbolPrefix+"[a-zA-Z]\\w*)|("+mathematicaIndexPrefix+"[a-zA-Z]\\w*)";
	}
	
	public String getMathematicaSymbolRegex() {
		return "("+mathematicaSymbolPrefix+"[a-zA-Z]\\w*)";
	}
	
	public String getMathematicaIndexRegex() {
		return "("+mathematicaIndexPrefix+"[a-zA-Z]\\w*)";
	}
	
	public String getBuiltInVarsRegEx() {
		String res = "";
		for (int i = 0; i < builtInVars.size(); i++) {
			if (i > 0)
				res += "|";
			res += builtInVars.get(i);
		}
		return res;
	}

	public String getGeneralVarsRegEx() {
		String res = "";
		for (int i = 0; i < generalVars.size(); i++) {
			if (i > 0)
				res += "|";
			res += generalVars.get(i);
		}
		return res;
	}
	
	// public String getBuiltInVarsRegExExceptions() {
	// String res="";
	// for (int i=0;i<builtInVars.size();i++) {
	// res+="(?!"+builtInVars.get(i)+"$)";
	// }
	// return res;
	// }

	public String getKeywordsRegEx() {
		String res = "";
		for (int i = 0; i < keywords.size(); i++) {
			if (i > 0)
				res += "|";
			res += keywords.get(i);
		}
		return res;
	}

	public static boolean isValid(String formulaStr) {
		return formulaStr.matches(mathFormulaExpressionRegex);
	}

	public static boolean isBlank(String formulaStr) {
		return formulaStr.matches("\\s*");
	}

//	public String getSBMLFormulaKeywordFix(String formula) {
////		formula = formula.substring(compartment.length() + 1);
//		p.p(formula);
//		String newStr = "";
//		String part = "";
//		String currentChar = null;
//		int varCount=10;
//		for (int i = 0; i <= formula.length(); i++) {
//			if (i < formula.length())
//				currentChar = String.valueOf(formula.charAt(i));
//			if (!currentChar.matches("\\w") || i == formula.length()) {
//				if (!part.isEmpty()) {
//					if (!part.matches(getToNotParseRegEx())) {
//						if (varCount > 0)
//							newStr += part;
//						varCount++;
//						// restrictions
//					} else {
////						p.p(part);
//						if (varCount > 0)
//							newStr += SBMLTools.getFixedSpeciesId(part);
////						p.p(SBMLTools.getFixedSpeciesId(part));
//						varCount++;
////						if (part.matches(FormulaUtils.getInstance().getBuiltInVarsRegEx())) {
////						} else {
////							if (varCount > 0)
////								newStr += part;
////							varCount++;
////						}
//					}
//				}
//				part = "";
//				if (i < formula.length()) {
//					if (varCount > 1)
//						newStr += currentChar; // +-*/
//				}
//			} else
//				part += currentChar;
//		}
//		p.p(newStr);
//		return newStr;
//	}
}
