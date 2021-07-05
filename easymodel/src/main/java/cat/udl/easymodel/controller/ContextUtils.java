package cat.udl.easymodel.controller;

public class ContextUtils {
	public static final String generalContext = "";
	public static final String modelContext = "m`";
	public static final String indexContext ="i`";
	public static final String systemContext = "System`";
	public static final String gainContext = "gain`";
	public static final String sensitivityContext = "sens`";
	public static final String builtInContext = "b`";
	public static final String arrayContext = ""; //"Ar`";
	public static final String substrateContext = "Sub`";
	public static final String modifierContext = "Mod`";
	
	public static String removeContext(String orig) {
		int lastIndex = orig.lastIndexOf("`");
		if (lastIndex != -1 && lastIndex != orig.length())
			return orig.substring(lastIndex+1, orig.length());
		else
			return orig;
	}
}
