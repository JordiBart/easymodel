package cat.udl.easymodel.logic.types;

public enum FormulaType {
	 PREDEFINED(0), CUSTOM(1);
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
         }
         return null;
     }
     
     public String getString() {
     	String type = "";
     	switch (value) {
     	case 0:
     		type = "Predefined";
     		break;
     	case 1:
     		type = "Custom";
     		break;
     	}
     	return type;
     }

 	public int getValue() {
 		return value;
 	}

 	public void setValue(int value) {
 		this.value = value;
 	}
}
