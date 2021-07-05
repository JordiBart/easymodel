package cat.udl.easymodel.logic.types;

public enum FormulaValueType {
	 CONSTANT(0), SUBSTRATE(1), MODIFIER(2);
     private int value;

     private FormulaValueType(int value) {
             this.setValue(value);
     }

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
    public static FormulaValueType fromInt(int value) {
        switch(value) {
        case 0:
            return CONSTANT;
        case 1:
            return SUBSTRATE;
        case 2:
            return MODIFIER;
        }
        return null;
    }
}
