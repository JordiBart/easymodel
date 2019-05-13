package cat.udl.easymodel.logic.model;

import cat.udl.easymodel.logic.types.FormulaValueType;

public interface FormulaValue {

	public abstract FormulaValueType getType();

	public abstract void setType(FormulaValueType type);

	public abstract String getConstantValue();

	public abstract void setConstantValue(String constantValue);

	public abstract String getSubstrateValue();

	public abstract void setSubstrateValue(String speciesValue);

	public abstract String getModifierValue();

	public abstract void setModifierValue(String modifierValue);

	public abstract boolean isFilled();

	public abstract String getStringValue();

}