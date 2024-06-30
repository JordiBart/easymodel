package cat.udl.easymodel.logic.types;

public enum FormulaElemType {
	 OPERATOR(0), NUMBER(1), GENPARAM(2), PARAMBYSUBSANDMODS(3);
     private final int value;

     FormulaElemType(int value) {
             this.value=value;
     }

     public int getValue() {
		return value;
	}

	public static FormulaElemType valueOf(int value) {
        for (FormulaElemType v : values()) {
            if (v.value == value)
                return v;
        }
        return null;
    }
}
