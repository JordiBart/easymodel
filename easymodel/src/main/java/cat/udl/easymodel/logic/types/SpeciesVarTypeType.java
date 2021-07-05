package cat.udl.easymodel.logic.types;

public enum SpeciesVarTypeType {
	 TIME_DEP(0), INDEPENDENT(1);
    private int value;

    private SpeciesVarTypeType(int value) {
            this.value = value;
    }
    
    public static SpeciesVarTypeType fromInt(int value) {
        switch(value) {
        case 0:
            return TIME_DEP;
        case 1:
            return INDEPENDENT;
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

	public void setValue(int value) {
		this.value = value;
	}
}
