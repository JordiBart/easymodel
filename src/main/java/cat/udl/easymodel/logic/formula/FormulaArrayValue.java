package cat.udl.easymodel.logic.formula;

public class FormulaArrayValue {
	private String parameterName=null;
	private String species=null;
	private String value=null;
	
	public FormulaArrayValue(String parameterName, String species, String value) {
		this.parameterName=parameterName;
		this.species=species;
		this.value=value;
	}

	public FormulaArrayValue(FormulaArrayValue from) {
		copyFrom(from);
	}

	public void copyFrom(FormulaArrayValue from) {
		parameterName=from.parameterName;
		species=from.species;
		value=from.value;
	}
	
	public FormulaArrayValue(String val) {
		this.value=val;
	}

	public Boolean isFilled() {
		return value != null;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
