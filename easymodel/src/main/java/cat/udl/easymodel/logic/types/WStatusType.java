package cat.udl.easymodel.logic.types;

public enum WStatusType {
	OK(0), KO(1), BACK(2);
	private int value;

	private WStatusType(int value) {
		this.value = value;
	}

	public static WStatusType fromInt(int value) {
		switch (value) {
		case 0:
			return OK;
		case 1:
			return KO;
		case 2:
			return BACK;
		}
		return null;
	}
}
