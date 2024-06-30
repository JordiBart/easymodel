package cat.udl.easymodel.logic.types;

public enum UserType {
	USER(0), ADMIN(1), GUEST(2);
	private final int value;

	UserType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static UserType valueOf(int value) {
		for (UserType v : values()) {
			if (v.value == value)
				return v;
		}
		return null;
	}

	public String getString() {
		String type = "";
		switch (value) {
		case 0:
			type = "User";
			break;
		case 1:
			type = "Admin";
			break;
		case 2:
			type = "Guest";
			break;
		}
		return type;
	}
}
