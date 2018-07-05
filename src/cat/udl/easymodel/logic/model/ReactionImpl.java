package cat.udl.easymodel.logic.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaImpl;
import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.types.FormulaValueType;

public class ReactionImpl implements Comparable<Reaction>, Reaction {
	enum SpeciesType {
		REACTIVE, PRODUCTIVE, MODIFIER
	};

	private int id;
	private int idJava;
	private String reactionStr = "";
	private String loadedReactionStr = "";
	// species stored with coefficients/stoichiometry:
	private SortedMap<String, Integer> leftPartSpecies = new TreeMap<>();
	private SortedMap<String, Integer> rightPartSpecies = new TreeMap<>();
	private SortedMap<String, Integer> modifiers = new TreeMap<>();
	// map with only keys
	private SortedMap<String, Integer> bothSidesSpecies = new TreeMap<>();

	// FORMULA
	private Formula formula = null;
	private Formula loadedFormula = null;
	// <constant, constantValue>>
	private SortedMap<String, FormulaValue> formulaValues = new TreeMap<>();
	// <constant, <species, constantValue>>
	private SortedMap<String, SortedMap<String, String>> formulaSubstratesArrayParameters = new TreeMap<>();
	// <constant, <modifier, constantValue>>
	private SortedMap<String, SortedMap<String, String>> formulaModifiersArrayParameters = new TreeMap<>();

	public ReactionImpl() {
	}

	public ReactionImpl(String reactionStr) {
		setReactionStr(reactionStr);
	}

	@Override
	public SortedMap<String, FormulaValue> getFormulaValuesNative() {
		return formulaValues;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int getIdJava() {
		return idJava;
	}

	@Override
	public String getIdJavaStr() {
		return "R" + idJava;
	}

	@Override
	public void setIdJava(int idJava) {
		this.idJava = idJava;
	}

	@Override
	public String getReactionStr() {
		return reactionStr;
	}

	@Override
	public void setReactionStr(String reactionStr) {
		this.reactionStr = reactionStr;
		parse();
	}

	@Override
	public SortedMap<String, Integer> getSpeciesValuesForStMatrix() {
		SortedMap<String, Integer> sm = new TreeMap<String, Integer>();
		if (parse()) {
			sm.putAll(rightPartSpecies);
			for (String leftKey : leftPartSpecies.keySet()) {
				if (sm.containsKey(leftKey))
					sm.put(leftKey, sm.get(leftKey) - leftPartSpecies.get(leftKey));
				else
					sm.put(leftKey, 0 - leftPartSpecies.get(leftKey));
			}
		}
		return sm;
	}

	@Override
	public boolean isValid() {
		return ReactionUtils.isValid(reactionStr);
	}

	@Override
	public boolean isBlank() {
		return ReactionUtils.isBlank(reactionStr);
	}

	@Override
	public boolean parse() {
		// reaction data
		if (!reactionStr.equals(loadedReactionStr) && !reactionStr.equals("")) {
			bothSidesSpecies.clear();
			modifiers.clear();
			leftPartSpecies.clear();
			rightPartSpecies.clear();

			if (!isValid()) {
				loadedReactionStr = "";
				return false;
			}

			String coef = "";
			String species = "";
			String readValue = "";
			SpeciesType step = SpeciesType.REACTIVE;
			String currentChar = null;
			for (int i = 0; i < reactionStr.length() + 1; i++) {
				// to read the last value
				currentChar = i < reactionStr.length() ? String.valueOf(reactionStr.charAt(i)) : ";";
				if (!Pattern.matches("(\\w|\\.|\\+|-|;|\\s|\\*)", currentChar))
					continue;
				if (step == SpeciesType.REACTIVE || step == SpeciesType.PRODUCTIVE) {
					if (Pattern.matches("\\w", currentChar)) {
						readValue += currentChar;
					} else { // currentChar= '+' '-' (of ->) ';' '*' ' '
						if (Pattern.matches("\\d+", readValue))
							coef = readValue;
						else if (Pattern.matches("\\w+", readValue))
							species = readValue;
						readValue = "";

						if (!species.equals("")) {
							if (species.matches(FormulaUtils.getInstance().getKeywordsRegEx())) {
								loadedReactionStr = "";
								return false;
							}
							if ("".equals(coef))
								coef = "1";
							Integer coefV = Integer.valueOf(coef);
							bothSidesSpecies.put(species, null);
							if (step == SpeciesType.REACTIVE)
								leftPartSpecies.put(species,
										(leftPartSpecies.get(species) == null ? 0 : leftPartSpecies.get(species))
												+ coefV);
							else if (step == SpeciesType.PRODUCTIVE)
								rightPartSpecies.put(species,
										(rightPartSpecies.get(species) == null ? 0 : rightPartSpecies.get(species))
												+ coefV);
							coef = "";
							species = "";
						}
						// if currentChar = '+' '*' ' ', do nothing
						if (Pattern.matches("-", currentChar)) {
							step = SpeciesType.PRODUCTIVE;
						} else if (Pattern.matches(";", currentChar))
							step = SpeciesType.MODIFIER;
					}
				} else {
					// modifier step
					if (Pattern.matches("-", currentChar))
						coef = "-1";
					else if (Pattern.matches("\\w", currentChar))
						species += currentChar;
					else if (Pattern.matches(";", currentChar)) {
						if (species.matches(FormulaUtils.getInstance().getKeywordsRegEx())) {
							loadedReactionStr = "";
							return false;
						}
						modifiers.put(species, (coef.equals("") ? 1 : -1));
						coef = "";
						species = "";
					}
				}
				// last value: taken care at beginning of this for
			}
			loadedReactionStr = reactionStr;
			// check if formula is still compatible
			if (formula != null && formula.parse() && !formula.isCompatibleWithReaction(this)) {
				formula = null;
				loadedFormula = null;
				formulaValues.clear();
				formulaSubstratesArrayParameters.clear();
				formulaModifiersArrayParameters.clear();
			}
		} else if (reactionStr.equals("")) {
			// reset
			bothSidesSpecies.clear();
			modifiers.clear();
			leftPartSpecies.clear();
			rightPartSpecies.clear();
		}
		// LOAD FORMULA
		if (formula != null && formula.parse() && formula != loadedFormula) {
			loadedFormula = formula;
			// reset constant values
			formulaValues.clear();
			formulaSubstratesArrayParameters.clear();
			formulaModifiersArrayParameters.clear();

			// for (String constantKey : formula.getConstants().keySet())
			// formulaValues.put(constantKey, null);

			// for (String constantKey : formula.getConstantsBySpecies()) {
			// Map<String, String> mapBySpecies = new LinkedHashMap<>();
			// for (String speciesKey : bothSidesSpecies.keySet())
			// mapBySpecies.put(speciesKey, null);
			// formulaSpeciesArrayParameters.put(constantKey, mapBySpecies);
			//
			// Map<String, String> mapByModifiers = new LinkedHashMap<>();
			// for (String modKey : modifiers.keySet())
			// mapByModifiers.put(modKey, null);
			// formulaModifiersArrayParameters.put(constantKey, mapByModifiers);
			// }
		} else if (formula == null) {
			// reset
			loadedFormula = null;
			formulaValues.clear();
			formulaSubstratesArrayParameters.clear();
			formulaModifiersArrayParameters.clear();
		}
		return true;
	}

	// FORMULA METHODS

	@Override
	public Formula getFormula() {
		return formula;
	}

	@Override
	public void setFormula(Formula formula) {
		this.formula = formula;
		parse();
	}

	@Override
	public SortedMap<String, SortedMap<String, String>> getFormulaSubstratesArrayParametersNative() {
		return formulaSubstratesArrayParameters;
	}

	@Override
	public SortedMap<String, SortedMap<String, String>> getFormulaSubstratesArrayParameters() {
		if (formula == null)
			formulaSubstratesArrayParameters.clear();
		else {
			// remove unused keys
			List<String> keysToRemove = new ArrayList<>();
			for (String constantKey : formulaSubstratesArrayParameters.keySet())
				if (!formula.getParametersBySubsAndModif().contains(constantKey))
					keysToRemove.add(constantKey);
			for (String key : keysToRemove)
				formulaSubstratesArrayParameters.remove(key);
			// add missing keys
			SortedMap<String, String> mapBySpecies = null;
			for (String constantKey : formula.getParametersBySubsAndModif()) {
				if (!formulaSubstratesArrayParameters.containsKey(constantKey))
					mapBySpecies = new TreeMap<>();
				else
					mapBySpecies = formulaSubstratesArrayParameters.get(constantKey);
				// remove unused modifiers
				keysToRemove.clear();
				for (String key : mapBySpecies.keySet())
					if (!getLeftPartSpecies().containsKey(key))
						keysToRemove.add(key);
				for (String key : keysToRemove)
					mapBySpecies.remove(key);
				// add missing modifiers
				for (String key : getLeftPartSpecies().keySet()) {
					if (!mapBySpecies.containsKey(key))
						mapBySpecies.put(key, null);
				}
				formulaSubstratesArrayParameters.put(constantKey, mapBySpecies);
			}
		}
		return formulaSubstratesArrayParameters;
	}

	@Override
	public SortedMap<String, SortedMap<String, String>> getFormulaModifiersArrayParametersNative() {
		return formulaModifiersArrayParameters;
	}

	@Override
	public SortedMap<String, SortedMap<String, String>> getFormulaModifiersArrayParameters() {
		if (formula == null)
			formulaModifiersArrayParameters.clear();
		else {
			// remove unused keys
			List<String> keysToRemove = new ArrayList<>();
			for (String constantKey : formulaModifiersArrayParameters.keySet())
				if (!formula.getParametersBySubsAndModif().contains(constantKey))
					keysToRemove.add(constantKey);
			for (String key : keysToRemove)
				formulaModifiersArrayParameters.remove(key);
			// add missing keys
			SortedMap<String, String> mapByModifiers = null;
			for (String constantKey : formula.getParametersBySubsAndModif()) {
				if (!formulaModifiersArrayParameters.containsKey(constantKey))
					mapByModifiers = new TreeMap<>();
				else
					mapByModifiers = formulaModifiersArrayParameters.get(constantKey);
				// remove unused modifiers
				keysToRemove.clear();
				for (String mod : mapByModifiers.keySet())
					if (!modifiers.containsKey(mod))
						keysToRemove.add(mod);
				for (String key : keysToRemove)
					mapByModifiers.remove(key);
				// add missing modifiers
				for (String key : modifiers.keySet()) {
					if (!mapByModifiers.containsKey(key))
						mapByModifiers.put(key, null);
				}
				formulaModifiersArrayParameters.put(constantKey, mapByModifiers);
			}
		}
		return formulaModifiersArrayParameters;
	}

	@Override
	public SortedMap<String, FormulaValue> getFormulaValues() {
		if (formula == null)
			formulaValues.clear();
		else {
			// remove unused keys
			List<String> keysToRemove = new ArrayList<>();
			for (String constantKey : formulaValues.keySet())
				if (!formula.getGenericParameters().containsKey(constantKey))
					keysToRemove.add(constantKey);
			for (String key : keysToRemove)
				formulaValues.remove(key);
			// add missing keys
			for (String constantKey : formula.getGenericParameters().keySet()) {
				if (!formulaValues.containsKey(constantKey))
					formulaValues.put(constantKey, null);
				else {
					// formulaValues contains the key
					// check if value is still valid
					if (formulaValues.get(constantKey) != null && formulaValues.get(constantKey).isFilled()) {
						if (formulaValues.get(constantKey).getType() == FormulaValueType.SUBSTRATE && !this
								.getLeftPartSpecies().containsKey(formulaValues.get(constantKey).getSubstrateValue()))
							formulaValues.put(constantKey, null);
						else if (formulaValues.get(constantKey).getType() == FormulaValueType.MODIFIER
								&& !this.getModifiers().containsKey(formulaValues.get(constantKey).getModifierValue()))
							formulaValues.put(constantKey, null);
					}
				}
			}
		}
		return formulaValues;
	}

	@Override
	public SortedMap<String, SortedMap<String, String>> getFormulaModifiersArrayParametersForFormula(Formula f) {
		SortedMap<String, SortedMap<String, String>> parameters = new TreeMap<>();
		if (f != null) {
			SortedMap<String, String> mapByModifiers = null;
			for (String constantKey : f.getParametersBySubsAndModif()) {
				mapByModifiers = new TreeMap<>();
				for (String key : modifiers.keySet()) {
					mapByModifiers.put(key, null);
				}
				parameters.put(constantKey, mapByModifiers);
			}
		}
		return parameters;
	}

	@Override
	public SortedMap<String, SortedMap<String, String>> getFormulaSubstratesArrayParametersForFormula(Formula f) {
		SortedMap<String, SortedMap<String, String>> parameters = new TreeMap<>();
		if (f != null) {
			SortedMap<String, String> mapBySpecies = null;
			for (String constantKey : f.getParametersBySubsAndModif()) {
				mapBySpecies = new TreeMap<>();
				for (String key : this.getLeftPartSpecies().keySet()) {
					mapBySpecies.put(key, null);
				}
				parameters.put(constantKey, mapBySpecies);
			}
		}
		return parameters;
	}

	@Override
	public SortedMap<String, FormulaValue> getFormulaValuesForFormula(Formula f) {
		SortedMap<String, FormulaValue> values = new TreeMap<>();
		if (f != null) {
			for (String constantKey : f.getGenericParameters().keySet())
				values.put(constantKey, null);
		}
		return values;
	}

	@Override
	public boolean areFormulaValuesValid() {
		// has formula, species/modifiers values and all constants
		if (!parse())
			return false;
		if (getFormula() == null)
			return false;
		for (FormulaValue co : formulaValues.values())
			if (co == null || !co.isFilled() || co.getType() == FormulaValueType.SUBSTRATE && !leftPartSpecies.containsKey(co.getSubstrateValue()) || co.getType() == FormulaValueType.MODIFIER && !modifiers.containsKey(co.getModifierValue()))
				return false;
		for (Map<String, String> constMap : getFormulaSubstratesArrayParameters().values())
			for (String co : constMap.values())
				if (co == null)
					return false;
		for (Map<String, String> constMap : getFormulaModifiersArrayParameters().values())
			for (String co : constMap.values())
				if (co == null)
					return false;
		return true;
	}

	// //////////////////////////////////

	@Override
	public SortedMap<String, Integer> getBothSides() {
		return bothSidesSpecies;
	}

	@Override
	public SortedMap<String, Integer> getModifiers() {
		return modifiers;
	}

	@Override
	public boolean isReactionConcentrationsSet(SortedMap<String, String> concentrations) {
		for (String sp : getBothSides().keySet())
			if (!concentrations.containsKey(sp) || concentrations.get(sp) == null)
				return false;
		for (String sp : getModifiers().keySet())
			if (!concentrations.containsKey(sp) || concentrations.get(sp) == null)
				return false;
		return true;
	}

	@Override
	public SortedMap<String, Integer> getLeftPartSpecies() {
		return leftPartSpecies;
	}

	@Override
	public SortedMap<String, Integer> getRightPartSpecies() {
		return rightPartSpecies;
	}

	@Override
	public int compareTo(Reaction react2) {
		return Integer.valueOf(this.idJava).compareTo(Integer.valueOf(((Reaction) react2).getIdJava()));
	}
}
