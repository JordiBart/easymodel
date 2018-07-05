package cat.udl.easymodel.logic.model;

import cat.udl.easymodel.logic.types.SpeciesVarTypeType;
import cat.udl.easymodel.main.SharedData;

public class SpeciesImpl implements Species {
	
	private String concentration = null;
	private SpeciesVarTypeType varType = SpeciesVarTypeType.TIMEDEP;
	
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
	public Species getCopy() {
		Species copy = new SpeciesImpl();
		copy.setConcentration(this.getConcentration());
		copy.setVarType(this.getVarType());
		return copy;
	}
}
