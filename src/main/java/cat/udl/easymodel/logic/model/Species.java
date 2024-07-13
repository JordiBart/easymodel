package cat.udl.easymodel.logic.model;

import cat.udl.easymodel.logic.types.SpeciesVarTypeType;
import cat.udl.easymodel.main.SharedData;

public class Species {
	private String name = null;
	private String concentration = SharedData.defaultInitialConcentration;
	private SpeciesVarTypeType varType = SpeciesVarTypeType.TIME_DEP;
	private String amount = null;

	public Species(String name) {
		this.name=name;
		if (name == null)
			System.err.print("WARNING: Species name cannot be null!");
	}

	public Species(Species from) {
		copyFrom(from);
	}

	public void copyFrom(Species from) {
		name = from.name;
		concentration = from.concentration;
		varType = from.varType;
		amount = from.amount;
	}
	
	public String getName() {
		return name;
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

	public Species getCopy() {
		Species copy = new Species(this.name);
		copy.setConcentration(this.getConcentration());
		copy.setVarType(this.getVarType());
		return copy;
	}
}
