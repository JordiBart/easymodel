package cat.udl.easymodel.logic.types;

public enum FormulaType {
	 PREDEFINED(0), CUSTOM(1),TEMP(2);
     private int value;

     private FormulaType(int value) {
             this.value = value;
     }
     
     public static FormulaType fromInt(int value) {
         switch(value) {
         case 0:
             return FormulaType.PREDEFINED;
         case 1:
             return CUSTOM;
         case 2:
             return TEMP;
         }
         return null;
     }
     
     public String getString() {
     	switch (value) {
     	case 0:
     		return "Predefined";
     	case 1:
     		return "Custom";
     	case 2:
     		return "Temp";
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
