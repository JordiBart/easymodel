package cat.udl.easymodel.logic.types;

public enum FormulaElemType {
	 OPERATOR(0), NUMBER(1), GENPARAM(2), PARAMBYSUBSANDMODS(3);
     private int value;

     private FormulaElemType(int value) {
             this.setValue(value);
     }

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
    public static FormulaElemType fromInt(int value) {
        switch(value) {
        case 0:
            return OPERATOR;
        case 1:
            return NUMBER;
        case 2:
            return GENPARAM;
        case 3:
        	return PARAMBYSUBSANDMODS;
        }
        return null;
    }
}
