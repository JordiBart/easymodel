package cat.udl.easymodel.logic.formula;

import cat.udl.easymodel.logic.types.FormulaElemType;

public class FormulaElem {
	private FormulaElemType fet = null;
	private String value = null;

	public FormulaElem(FormulaElemType fet, String value) {
		this.fet = fet;
		this.value = value;
	}

	public FormulaElem(FormulaElem from) {
		fet = from.fet;
		value = from.value;
	}

	public FormulaElemType getFormulaElemType() {
		return fet;
	}

	public void setFet(FormulaElemType fet) {
		this.fet = fet;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
