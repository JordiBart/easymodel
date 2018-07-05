package cat.udl.easymodel.logic.formula;

import java.util.ArrayList;

import cat.udl.easymodel.sbml.SBMLTools;

public class FormulaUtils {
	public static final String modelShortNameSeparator="_K";
	private ArrayList<String> mathematicaConstants = new ArrayList<>();
	private ArrayList<String> builtInVars = new ArrayList<>();
	private ArrayList<String> keywords = new ArrayList<>();
	private static FormulaUtils thisSingleton = new FormulaUtils();

	private FormulaUtils() {
		mathematicaConstants.add("Pi");
		mathematicaConstants.add("E");
		mathematicaConstants.add("D");

		builtInVars.add("X");
		builtInVars.add("M");
		builtInVars.add("A");
		builtInVars.add("XF");
		builtInVars.add("MF");
		builtInVars.add("t");

		for (int i = 0; i < mathematicaConstants.size(); i++)
			keywords.add(mathematicaConstants.get(i));
		for (int i = 0; i < builtInVars.size(); i++)
			keywords.add(builtInVars.get(i));
		keywords.add("Sum");
		keywords.add("Product");
		keywords.add("Length");
		keywords.add("i");
		keywords.add("j");
		keywords.add("l");
		keywords.add("Sin");
		keywords.add("Cos");
		keywords.add("Tan");
		keywords.add("ArcSin");
		keywords.add("ArcCos");
		keywords.add("ArcTan");
		keywords.add("Exp");
		keywords.add("Log");
		keywords.add("UnitStep");
	}

	public static FormulaUtils getInstance() {
		return thisSingleton;
	}

	public String getMathematicaConstantsRegEx() {
		String res = "";
		for (int i = 0; i < mathematicaConstants.size(); i++) {
			if (i > 0)
				res += "|";
			res += mathematicaConstants.get(i);
		}
		return res;
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
		return formulaStr.matches("^[a-zA-Z0-9\\[\\]^,{}\\(\\)+\\-*/\\s\\.]+$") && !isBlank(formulaStr);
	}

	public static boolean isBlank(String formulaStr) {
		return formulaStr.matches("\\s*");
	}

	public static String getSBMLFormulaKeywordFix(String formula) {
//		formula = formula.substring(compartment.length() + 1);
		String newStr = "";
		String part = "";
		String currentChar = null;
		int varCount=10;
		for (int i = 0; i <= formula.length(); i++) {
			if (i < formula.length())
				currentChar = String.valueOf(formula.charAt(i));
			if (!currentChar.matches("\\w") || i == formula.length()) {
				if (!part.isEmpty()) {
					if (!part.matches(FormulaImpl.toNotParseRegEx)) {
						if (varCount > 0)
							newStr += part;
						varCount++;
						// restrictions
					} else {
						if (varCount > 0)
							newStr += SBMLTools.getFixedSpeciesName(part);
						varCount++;
//						if (part.matches(FormulaUtils.getInstance().getBuiltInVarsRegEx())) {
//						} else {
//							if (varCount > 0)
//								newStr += part;
//							varCount++;
//						}
					}
				}
				part = "";
				if (i < formula.length()) {
					if (varCount > 1)
						newStr += currentChar; // +-*/
				}
			} else
				part += currentChar;
		}
		return newStr;
	}
}
