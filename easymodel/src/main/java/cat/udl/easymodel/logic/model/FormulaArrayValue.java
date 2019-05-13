package cat.udl.easymodel.logic.model;

public class FormulaArrayValue {
	private String value=null;
	
	public FormulaArrayValue() {
		
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
