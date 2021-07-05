package cat.udl.easymodel.logic.types;

public enum SimType {
	DETERMINISTIC(0), STOCHASTIC(1);
	private int value;

	private SimType(int value) {
		this.setValue(value);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public static SimType fromInt(int value) {
		switch (value) {
		case 0:
			return DETERMINISTIC;
		case 1:
			return STOCHASTIC;
		}
		return null;
	}
	
    public String getString() {
    	String type = "";
    	switch (value) {
    	case 0:
    		type = "Deterministic";
    		break;
    	case 1:
    		type = "Stochastic";
    		break;
    	}
    	return type;
    }
}
