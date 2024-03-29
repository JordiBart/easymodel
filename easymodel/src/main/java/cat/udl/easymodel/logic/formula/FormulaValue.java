package cat.udl.easymodel.logic.formula;

import cat.udl.easymodel.logic.types.FormulaValueType;

public class FormulaValue {
	private String parameterName = null;
	private FormulaValueType type = null;
	private String constantValue = null;
	private String substrateValue = null;
	private String modifierValue = null;

	public FormulaValue(String parameterName) {
		this.parameterName = parameterName;
	}

	public FormulaValue(FormulaValue from) {
		parameterName = from.parameterName;
		type = from.type;
		constantValue = from.constantValue;
		substrateValue = from.substrateValue;
		modifierValue = from.modifierValue;
	}

	public FormulaValue(String parameterName, FormulaValueType fvt, Object value) {
		this.parameterName = parameterName;
		if (fvt != null && value != null) {
			switch (fvt) {
			case CONSTANT:
				if (value instanceof String) {
					setConstantValue((String) value);
					setType(fvt);
				}
				break;
			case SUBSTRATE:
				if (value instanceof String) {
					setSubstrateValue((String) value);
					setType(fvt);
				}
				break;
			case MODIFIER:
				if (value instanceof String) {
					setModifierValue((String) value);
					setType(fvt);
				}
				break;
			}
		}
	}

	public boolean isFilled() {
		if (getType() == null)
			return false;

		switch (getType()) {
		case CONSTANT:
			if (getConstantValue() == null)
				return false;
			break;
		case SUBSTRATE:
			if (getSubstrateValue() == null)
				return false;
			break;
		case MODIFIER:
			if (getModifierValue() == null)
				return false;
			break;
		}
		return true;
	}

	public String getStringValue() {
		String nullVal = "0";
		if (getType() == null)
			return nullVal;

		switch (getType()) {
		case CONSTANT:
			if (getConstantValue() != null) {
				return constantValue;
			}
			break;
		case SUBSTRATE:
			if (getSubstrateValue() != null)
				return getSubstrateValue();
			break;
		case MODIFIER:
			if (getModifierValue() != null)
				return getModifierValue();
			break;
		}
		return nullVal;
	}

	public String getParameterName() {
		return parameterName;
	}

	public FormulaValueType getType() {
		return type;
	}

	public void setType(FormulaValueType type) {
		this.type = type;
	}

	public String getConstantValue() {
		return constantValue;
	}

	public void setConstantValue(String constantValue) {
		this.constantValue = constantValue;
	}

	public String getSubstrateValue() {
		return substrateValue;
	}

	public void setSubstrateValue(String substrateValue) {
		this.substrateValue = substrateValue;
	}

	public String getModifierValue() {
		return modifierValue;
	}

	public void setModifierValue(String modifierValue) {
		this.modifierValue = modifierValue;
	}
}
