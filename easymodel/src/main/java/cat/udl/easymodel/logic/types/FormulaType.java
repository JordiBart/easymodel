package cat.udl.easymodel.logic.types;

public enum FormulaType {
	PREDEFINED(0), MODEL(1), GENERIC(2);
	private int value;

	private FormulaType(int value) {
		this.value = value;
	}

	public static FormulaType fromInt(int value) {
		switch (value) {
		case 0:
			return FormulaType.PREDEFINED;
		case 1:
			return MODEL;
		case 2:
			return GENERIC;
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

	public void setValue(int value) {
		this.value = value;
	}
}
