package cat.udl.easymodel.utils;

public class Utils {
	private Utils() {

	}

	public static Integer boolToInt(Boolean bool) {
		if (bool == null)
			return null;
		return (bool) ? 1 : 0;
	}
	
	public static Boolean intToBool(Integer in) {
		if (in == null)
			return null;
		return (in == 0) ? false : true;
	}
}
