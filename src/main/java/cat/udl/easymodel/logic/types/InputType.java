package cat.udl.easymodel.logic.types;

public enum InputType {
	STRING(0), MATHEXPRESSION(1), DECIMAL(2), CHECKBOX(3), NATURAL(6), SELECT(7);
	private final int value;

//	public static final String mathAtomRegex = "(\\s*\\-?\\(*\\-?(\\d+(\\.\\d+)?)\\)*\\s*)";
//	public static final String simpleMathExpressionRegex = "^" + mathAtomRegex + "((\\+|\\-|\\*|\\^|\\s+|/)"
//			+ mathAtomRegex + ")*$";

	InputType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static InputType valueOf(int value) {
		for (InputType v : values()) {
			if (v.value == value)
				return v;
		}
		return null;
	}
}