package cat.udl.easymodel.logic.model;

import java.util.Map;
import java.util.SortedMap;

import cat.udl.easymodel.logic.formula.Formula;

public interface Reaction extends Comparable<Reaction> {

	public abstract int getIdJava();

	public abstract void setIdJava(int id);

	public abstract String getReactionStr();

	public abstract void setReactionStr(String reactionStr);

	public abstract boolean isValid();

	public abstract boolean isBlank();

	public abstract int compareTo(Reaction react2);

	public abstract boolean parse();

	public abstract SortedMap<String, FormulaValue> getFormulaValues();

	public abstract SortedMap<String, SortedMap<String, String>> getFormulaModifiersArrayParameters();

	public abstract SortedMap<String, SortedMap<String, String>> getFormulaSubstratesArrayParameters();

	public abstract void setFormula(Formula formula);

	public abstract Formula getFormula();

	public abstract SortedMap<String, Integer> getSpeciesValuesForStMatrix();

	public abstract boolean areFormulaValuesValid();

	public abstract SortedMap<String, Integer> getRightPartSpecies();

	public abstract SortedMap<String, Integer> getLeftPartSpecies();

	public abstract SortedMap<String, Integer> getModifiers();

	public abstract boolean isReactionConcentrationsSet(SortedMap<String, String> concentrations);

	// XXX only keyset is used, not values!!! Values comes from concentration
	public abstract SortedMap<String, Integer> getBothSides();

	public abstract SortedMap<String, FormulaValue> getFormulaValuesForFormula(Formula f);

	public abstract SortedMap<String, SortedMap<String, String>> getFormulaSubstratesArrayParametersForFormula(Formula f);

	public abstract SortedMap<String, SortedMap<String, String>> getFormulaModifiersArrayParametersForFormula(Formula f);

	String getIdJavaStr();

	int getId();

	void setId(int id);

	SortedMap<String, FormulaValue> getFormulaValuesNative();

	SortedMap<String, SortedMap<String, String>> getFormulaSubstratesArrayParametersNative();

	SortedMap<String, SortedMap<String, String>> getFormulaModifiersArrayParametersNative();

}