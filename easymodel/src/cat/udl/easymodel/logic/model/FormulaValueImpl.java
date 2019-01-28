package cat.udl.easymodel.logic.model;

import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.main.SharedData;

public class FormulaValueImpl implements FormulaValue {

	private FormulaValueType type = null;
	private String constantValue = null;
	private String substrateValue = null;
	private String modifierValue = null;
	
	public FormulaValueImpl() {
	}

	public FormulaValueImpl(FormulaValueType fvt, Object value) {
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

	@Override
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

	@Override
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

	@Override
	public FormulaValueType getType() {
		return type;
	}

	@Override
	public void setType(FormulaValueType type) {
		this.type = type;
	}

	@Override
	public String getConstantValue() {
		return constantValue;
	}

	@Override
	public void setConstantValue(String constantValue) {
		this.constantValue = constantValue;
	}

	@Override
	public String getSubstrateValue() {
		return substrateValue;
	}

	@Override
	public void setSubstrateValue(String substrateValue) {
		this.substrateValue = substrateValue;
	}

	@Override
	public String getModifierValue() {
		return modifierValue;
	}

	@Override
	public void setModifierValue(String modifierValue) {
		this.modifierValue = modifierValue;
	}
}
