package cat.udl.easymodel.logic.formula;

import cat.udl.easymodel.logic.types.FormulaValueType;

public class FormulaValue {
	private String parameterName = null;
	private FormulaValueType type = null;
	private String value = null;
	private boolean isForceConstant=false;

	public FormulaValue(String parameterName) {
		this.parameterName = parameterName;
	}

	public FormulaValue(FormulaValue from) {
		copyFrom(from);
	}

	public void copyFrom(FormulaValue from) {
		parameterName = from.parameterName;
		type = from.type;
		value = from.value;
		isForceConstant = from.isForceConstant;
	}

	public FormulaValue(String parameterName, FormulaValueType fvt, String newVal) {
		this.parameterName = parameterName;
		this.type = fvt;
		this.value=newVal;
	}

	public boolean isFilled() {
		return (type != null && value != null);
	}

	public String getStringValue() {
		if (type == null || value == null)
			return "0";
		return value;
	}

	public String getParameterName() {
		return parameterName;
	}

	public FormulaValueType getType() {
		return type;
	}

	public void setType(FormulaValueType type) {
		if (!isForceConstant)
			this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String val) {
		this.value = val;
	}

	public boolean isForceConstant() {
		return isForceConstant;
	}

	public void setForceConstantToTrue() {
		isForceConstant = true;
		this.type=FormulaValueType.CONSTANT;
	}
}
