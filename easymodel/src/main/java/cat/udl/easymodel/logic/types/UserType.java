package cat.udl.easymodel.logic.types;

public enum UserType {
	USER(0), ADMIN(1), GUEST(2);
	private int value;

	private UserType(int value) {
		this.value = value;
	}

	public static UserType fromInt(int value) {
		switch (value) {
		case 0:
			return USER;
		case 1:
			return ADMIN;
		case 2:
			return GUEST;
		}
		return null;
	}
	public int getValue() {
		return value;
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
