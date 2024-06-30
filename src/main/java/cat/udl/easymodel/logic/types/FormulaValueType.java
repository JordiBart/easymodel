package cat.udl.easymodel.logic.types;

public enum FormulaValueType {
	 CONSTANT(0), SUBSTRATE(1), MODIFIER(2);
	 private final int value;

     FormulaValueType(int value) {
         this.value = value;
     }

	public int getValue() {
		return this.value;
	}

    public static FormulaValueType valueOf(int value) {
         for (FormulaValueType v : values()) {
             if (v.value == value)
                 return v;
         }
         return null;
    }
}
