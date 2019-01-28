package cat.udl.easymodel.logic.types;

public enum RepositoryType {
	PUBLIC(0), PRIVATE(1), TEMP(2);
    private int value;

    private RepositoryType(int value) {
            this.value = value;
    }
    
    public static RepositoryType fromInt(int value) {
        switch(value) {
        case 0:
            return PUBLIC;
        case 1:
            return PRIVATE;
        case 2:
            return TEMP;
        }
        return null;
    }
    
    public String getString() {
    	String type = "";
    	switch (value) {
    	case 0:
    		type = "Public";
    		break;
    	case 1:
    		type = "Private";
    		break;
    	case 2:
    		type = "TEMP";
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
