package cat.udl.easymodel.logic.types;

public enum InputType {
	STRING(0), MATHEXPRESSION(1), DECIMAL(2), CHECKBOX(3), SLIDER(4), ARRAYCHECKBOX(5), NATURAL(6), SELECT(7);
	private int value;

//	public static final String mathAtomRegex = "(\\s*\\-?\\(*\\-?(\\d+(\\.\\d+)?)\\)*\\s*)";
//	public static final String simpleMathExpressionRegex = "^" + mathAtomRegex + "((\\+|\\-|\\*|\\^|\\s+|/)"
//			+ mathAtomRegex + ")*$";

	private InputType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static InputType fromInt(int value) {
		for (InputType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		return null;
	}
	// toString()
}
