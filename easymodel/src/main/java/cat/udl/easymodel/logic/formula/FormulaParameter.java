package cat.udl.easymodel.logic.formula;

import cat.udl.easymodel.logic.types.FormulaValueType;

public class FormulaParameter {
	private String name = null;
	private FormulaValueType forcedFormulaValueType = null;
	private String power = "1"; // can be numeric or another parameter (that must contain a number)

	public FormulaParameter(String name) {
		this.name = name;
	}
	
	public FormulaParameter(FormulaParameter from) {
		name = from.name;
		forcedFormulaValueType = from.forcedFormulaValueType;
		power = from.power;
	}

	public String getName() {
		return name;
	}

	public FormulaValueType getForcedFormulaValueType() {
		return forcedFormulaValueType;
	}

	public void setForcedFormulaValueType(FormulaValueType formulaValueType) {
		this.forcedFormulaValueType = formulaValueType;
	}

	public String getPower() {
		return power;
	}

	public void setPower(String power) {
		this.power = power;
	}
}
