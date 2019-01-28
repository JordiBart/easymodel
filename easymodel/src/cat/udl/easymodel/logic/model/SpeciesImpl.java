package cat.udl.easymodel.logic.model;

import cat.udl.easymodel.logic.types.SpeciesVarTypeType;

public class SpeciesImpl implements Species {

	private String concentration = null;
	private SpeciesVarTypeType varType = SpeciesVarTypeType.TIMEDEP;
	private boolean isStochastic = false;
	private String amount = null;

	public SpeciesImpl() {
	}

	@Override
	public boolean isSet() {
		if (concentration == null || varType == null)
			return false;
		return true;
	}

	@Override
	public String getConcentration() {
		return concentration;
	}

	@Override
	public void setConcentration(String concentration) {
		this.concentration = concentration;
	}

	@Override
	public SpeciesVarTypeType getVarType() {
		return varType;
	}
	@Override
	public void setVarType(SpeciesVarTypeType varType) {
		this.varType = varType;
	}
	@Override
	public String getAmount() {
		return amount;
	}
	@Override
	public void setAmount(String amount) {
		this.amount = amount;
	}


	@Override
	public boolean isStochastic() {
		return isStochastic;
	}
	@Override
	public void setStochastic(boolean isStochastic) {
		this.isStochastic = isStochastic;
	}

	@Override
	public Species getCopy() {
		Species copy = new SpeciesImpl();
		copy.setConcentration(this.getConcentration());
		copy.setVarType(this.getVarType());
		return copy;
	}

}
