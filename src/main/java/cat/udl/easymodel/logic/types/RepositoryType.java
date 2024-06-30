package cat.udl.easymodel.logic.types;

public enum RepositoryType {
	PUBLIC(0), PRIVATE(1), TEMP(2);
    private final int value;

    RepositoryType(int value) {
            this.value = value;
    }

	public static RepositoryType valueOf(int value) {
		for (RepositoryType v : values()) {
			if (v.value == value)
				return v;
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
    		type = "Temp";
    		break;
    	}
    	return type;
    }

	public int getValue() {
		return value;
	}
}
