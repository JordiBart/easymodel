package cat.udl.easymodel.logic.model;

public class ModelUtils {
	public static final String modelNextNameSeparator="";
	private static ModelUtils thisSingleton = new ModelUtils();

	private ModelUtils() {
	}

	public static ModelUtils getInstance() {
		return thisSingleton;
	}

}