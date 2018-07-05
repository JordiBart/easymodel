package cat.udl.easymodel.logic.types;

public enum SpeciesVarTypeType {
	 TIMEDEP(0), INDEP(1);
    private int value;

    private SpeciesVarTypeType(int value) {
            this.value = value;
    }
    
    public static SpeciesVarTypeType fromInt(int value) {
        switch(value) {
        case 0:
            return TIMEDEP;
        case 1:
            return INDEP;
        }
        return null;
    }
    
    public String getString() {
    	String type = "";
    	switch (value) {
    	case 0:
    		type = "Time dependent";
    		break;
    	case 1:
    		type = "Constant";
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
