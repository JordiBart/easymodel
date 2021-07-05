package cat.udl.easymodel.logic.formula;

public class FormulaArrayValue {
	private String value=null;
	
	public FormulaArrayValue() {
	}

	public FormulaArrayValue(FormulaArrayValue from) {
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
