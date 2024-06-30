package cat.udl.easymodel.logic.types;

public enum FormulaType {
	PREDEFINED(0), MODEL(1), GENERIC(2);
	private final int value;

	FormulaType(int value) {
		this.value = value;
	}

	public static FormulaType valueOf(int value) {
		for (FormulaType v : values()) {
			if (v.value == value)
				return v;
		}
		return null;
	}
	public String getString() {
		switch (value) {
		case 0:
			return "Predefined";
		case 1:
			return "Model";
		case 2:
			return "Generic";
		}
		return "err";
	}

	public int getValue() {
		return value;
	}
}
