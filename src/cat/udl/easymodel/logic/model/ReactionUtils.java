package cat.udl.easymodel.logic.model;

import java.util.regex.Pattern;

import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.utils.p;

public class ReactionUtils {
	public static final String varNameExceptions = "(?!(?:"+FormulaUtils.getInstance().getKeywordsRegEx()+")$)";
	public static final String oneSpeciesPair = "(\\s*((\\d+)(\\s+|\\s*\\*\\s*))?(" + varNameExceptions
			+ "(\\w*[a-zA-Z]\\w*)))";
	public static final String oneSpeciesSide = oneSpeciesPair + "(\\s*\\+" + oneSpeciesPair + ")*";
	public static final String modifiers = "\\s*(;\\s*-?(" + varNameExceptions + "(\\w*[a-zA-Z]\\w*))\\s*)*";
	public static final String isValidRegEx = "^(" + oneSpeciesSide + "\\s*->(" + oneSpeciesSide + ")?" + modifiers
			+ ")|(" + "(" + oneSpeciesSide + ")?\\s*->" + oneSpeciesSide + modifiers + ")$";
	// reactions may have one side empty, but not both!
	public static final String isBlankRegEx = "^\\s*$";

	public static boolean isValid(String reactionsString) {
//		p.p(isValidRegEx);
		return Pattern.matches(isValidRegEx, reactionsString);
	}

	public static boolean isBlank(String reactionsString) {
		return Pattern.matches(isBlankRegEx, reactionsString);
	}
}
