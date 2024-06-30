package cat.udl.easymodel.logic.types;

public enum SpeciesVarTypeType {
	 TIME_DEP(0), INDEPENDENT(1);
    private final int value;

    SpeciesVarTypeType(int value) {
            this.value = value;
    }

	public static SpeciesVarTypeType valueOf(int value) {
		for (SpeciesVarTypeType v : values()) {
			if (v.value == value)
				return v;
		}
		return null;
	}
    
    public String getString() {
    	String type = "";
    	switch (value) {
    	case 0:
    		type = "Time Dependent";
    		break;
    	case 1:
    		type = "Independent";
    		break;
    	}
    	return type;
    }
    
	public int getValue() {
		return value;
	}
}
