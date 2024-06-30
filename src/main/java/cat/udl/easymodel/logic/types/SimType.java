package cat.udl.easymodel.logic.types;

public enum SimType {
	DYNAMIC_DETERMINISTIC(0), STEADY_STATE(1), DYNAMIC_STOCHASTIC(2);
	private final int value;

	SimType(int value) {
		this.value=value;
	}

	public int getValue() {
		return value;
	}

	public static SimType valueOf(int value) {
		for (SimType v : values()) {
			if (v.value == value)
				return v;
		}
		return null;
	}

	@Override
	public String toString() {
		switch (value) {
			case 0:
				return "Dynamic (Deterministic)";
			case 1:
				return "Steady State";
			case 2:
				return "Dynamic (Stochastic)";
			default:
				return "Error sim type";
		}
	}
}
