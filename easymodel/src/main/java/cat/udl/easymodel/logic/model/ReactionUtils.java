package cat.udl.easymodel.logic.model;

import java.util.regex.Pattern;

import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.utils.p;

public class ReactionUtils {
	// ?! negative lookahead, ?: non-capturing group
	public static final String varNameRegex ="([a-zA-Z]\\w*)";
	public static final String varNameExceptions = "(?!(?:"+FormulaUtils.getInstance().getKeywordsRegEx()+")$)";
	public static final String oneSpeciesWithStoi = "(\\s*((\\d+)(\\s+|\\s*\\*\\s*))?(" + varNameExceptions
			+ varNameRegex + "))";
	public static final String oneSpeciesSide = oneSpeciesWithStoi + "(\\s*\\+" + oneSpeciesWithStoi + ")*";
	public static final String modifiers = "\\s*(;\\s*-?(" + varNameExceptions + varNameRegex + ")\\s*)*";
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
