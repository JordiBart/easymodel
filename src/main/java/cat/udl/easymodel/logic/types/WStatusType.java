package cat.udl.easymodel.logic.types;

public enum WStatusType {
	OK(0), KO(1), BACK(2);
	private final int value;

	WStatusType(int value) {
		this.value = value;
	}

	public static WStatusType valueOf(int value) {
		for (WStatusType v : values()) {
			if (v.value == value)
				return v;
		}
		return null;
	}
}
