package cat.udl.easymodel.logic.model;

import java.util.SortedMap;

import cat.udl.easymodel.logic.formula.Formula;

public interface Reaction extends Comparable<Reaction> {

	int getIdJava();

	void setIdJava(int id);

	String getReactionStr();

	void setReactionStr(String reactionStr);

	boolean isValid();

	boolean isBlank();

	int compareTo(Reaction react2);

	boolean parse();

	SortedMap<String, FormulaValue> getFormulaValues();

	SortedMap<String, SortedMap<String, FormulaArrayValue>> getFormulaSubstratesArrayParameters();

	SortedMap<String, SortedMap<String, FormulaArrayValue>> getFormulaModifiersArrayParameters();

	SortedMap<String, SortedMap<String, FormulaArrayValue>> getFormulaSubstratesArrayParametersRAW();
	
	SortedMap<String, SortedMap<String, FormulaArrayValue>> getFormulaModifiersArrayParametersRAW();
	
	SortedMap<String, SortedMap<String, FormulaArrayValue>> getFormulaSubstratesArrayParametersForFormula(Formula f);
	
	SortedMap<String, SortedMap<String, FormulaArrayValue>> getFormulaModifiersArrayParametersForFormula(Formula f);
	
	void setFormula(Formula formula);

	Formula getFormula();

	SortedMap<String, Integer> getSpeciesValuesForStMatrix();

	boolean areFormulaValuesValid();

	SortedMap<String, Integer> getRightPartSpecies();

	SortedMap<String, Integer> getLeftPartSpecies();

	SortedMap<String, Integer> getModifiers();

	boolean isReactionConcentrationsSet(SortedMap<String, String> concentrations);

	// only keyset is used, not values!!! Values/Concentrations are stored in getRightPartSpecies() getLeftPartSpecies() getModifiers()
	SortedMap<String, Integer> getBothSides();

	SortedMap<String, FormulaValue> getFormulaValuesForFormula(Formula f);


	String getIdJavaStr();
	
	String getMathematicaContext();

	int getId();

	void setId(int id);

	SortedMap<String, FormulaValue> getFormulaValuesRAW();


}