package cat.udl.easymodel.logic.model;

import cat.udl.easymodel.logic.types.SpeciesVarTypeType;

public class Species {

	private String concentration = "1";
	private SpeciesVarTypeType varType = SpeciesVarTypeType.TIMEDEP;
	private boolean isStochastic = false;
	private String amount = null;

	public Species() {
	}

	
	public boolean isSet() {
		if (concentration == null || varType == null)
			return false;
		return true;
	}

	
	public String getConcentration() {
		return concentration;
	}

	
	public void setConcentration(String concentration) {
		this.concentration = concentration;
	}

	
	public SpeciesVarTypeType getVarType() {
		return varType;
	}
	
	public void setVarType(SpeciesVarTypeType varType) {
		this.varType = varType;
	}
	
	public String getAmount() {
		return amount;
	}
	
	public void setAmount(String amount) {
		this.amount = amount;
	}


	
	public boolean isStochastic() {
		return isStochastic;
	}
	
	public void setStochastic(boolean isStochastic) {
		this.isStochastic = isStochastic;
	}

	
	public Species getCopy() {
		Species copy = new Species();
		copy.setConcentration(this.getConcentration());
		copy.setVarType(this.getVarType());
		return copy;
	}

}
