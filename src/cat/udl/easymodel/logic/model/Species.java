package cat.udl.easymodel.logic.model;

import cat.udl.easymodel.logic.types.SpeciesVarTypeType;

public interface Species {

	String getConcentration();

	public abstract void setConcentration(String concentration);

	public abstract SpeciesVarTypeType getVarType();

	public abstract void setVarType(SpeciesVarTypeType varType);

	public abstract boolean isSet();

	public abstract Species getCopy();

}