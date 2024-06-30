package cat.udl.easymodel.logic.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaArrayValue;
import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.formula.FormulaValue;
import cat.udl.easymodel.logic.stochastic.ReactantReactionLevel;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.SpeciesType;

public class Reaction implements Comparable<Reaction> {
	private Integer id = null;
	private Integer idJava = null;
	private String reactionStr = "";
	private String loadedReactionStr = "";
	// species stored with coefficients/stoichiometry:
	private SortedMap<String, Integer> leftPartSpecies = new TreeMap<>();
	private SortedMap<String, Integer> rightPartSpecies = new TreeMap<>();
	private SortedMap<String, Integer> modifiers = new TreeMap<>();
	// map with only keys, no values
	private SortedMap<String, Integer> bothSidesSpecies = new TreeMap<>();

	// FORMULA
	private Formula formula = null;
	private Formula loadedFormula = null;
	// <constant, constantValue>>
	private SortedMap<String, FormulaValue> formulaValues = new TreeMap<>();
	// <constant, <species, constantValue>>
	private SortedMap<String, SortedMap<String, FormulaArrayValue>> formulaSubstratesArrayParameters = new TreeMap<>();
	// <constant, <modifier, constantValue>>
	private SortedMap<String, SortedMap<String, FormulaArrayValue>> formulaModifiersArrayParameters = new TreeMap<>();

	public Reaction() {
	}

	public Reaction(Reaction from,Model refModel, int forModelFullCopy) {
		id = from.id;
		idJava = from.idJava;
		reactionStr = from.reactionStr;
		loadedReactionStr = from.loadedReactionStr;
		for (String key : from.leftPartSpecies.keySet())
			leftPartSpecies.put(key, from.leftPartSpecies.get(key));
		for (String key : from.rightPartSpecies.keySet())
			rightPartSpecies.put(key, from.rightPartSpecies.get(key));
		for (String key : from.modifiers.keySet())
			modifiers.put(key, from.modifiers.get(key));
		for (String key : from.bothSidesSpecies.keySet())
			bothSidesSpecies.put(key, from.bothSidesSpecies.get(key));
		this.formula = refModel.getFormulas().getFormulaByIdJava(from.getFormula().getIdJava());
		this.loadedFormula = this.formula;
		for (String key : from.formulaValues.keySet())
			formulaValues.put(key, new FormulaValue(from.formulaValues.get(key)));
//		for (String key : formulaValues.keySet())
//			p.p(key+" "+formulaValues.get(key));
		for (String key1 : from.formulaSubstratesArrayParameters.keySet()) {
			SortedMap<String, FormulaArrayValue> map1 = new TreeMap<>();
			for (String key2 : from.formulaSubstratesArrayParameters.get(key1).keySet())
				map1.put(key2, new FormulaArrayValue(from.formulaSubstratesArrayParameters.get(key1).get(key2)));
			formulaSubstratesArrayParameters.put(key1, map1);
		}
		for (String key1 : from.formulaModifiersArrayParameters.keySet()) {
			SortedMap<String, FormulaArrayValue> map1 = new TreeMap<String, FormulaArrayValue>();
			for (String key2 : from.formulaModifiersArrayParameters.get(key1).keySet())
				map1.put(key2, new FormulaArrayValue(from.formulaModifiersArrayParameters.get(key1).get(key2)));
			formulaModifiersArrayParameters.put(key1, map1);
		}
		//parse();
	}
	
	public Reaction(String reactionStr) {
		setReactionStr(reactionStr);
	}

	public SortedMap<String, FormulaValue> getFormulaGenParsRAW() {
		return formulaValues;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIdJava() {
		return idJava;
	}

	public String getIdJavaStr() {
		return "R" + idJava;
	}

	public void setIdJava(Integer idJava) {
		this.idJava = idJava;
	}

	public String getMathematicaContext() {
		return "r" + idJava + "`";
	}

	public String getReactionStr() {
		return reactionStr;
	}

	public void setReactionStr(String reactionStr) {
		this.reactionStr = reactionStr;
		parse();
	}

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

	public boolean isValid() {
		return ReactionUtils.isValid(reactionStr);
	}

	public boolean isBlank() {
		return ReactionUtils.isBlank(reactionStr);
	}

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
				currentChar = i < reactionStr.length() ? String.valueOf(reactionStr.charAt(i)) : " ";
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
						if (Pattern.matches("-", currentChar)) { //->
							step = SpeciesType.PRODUCTIVE;
						} else if (Pattern.matches(";", currentChar))
							step = SpeciesType.MODIFIER;
					}
				} else {//if (step == SpeciesType.MODIFIER) {
					// modifier step
					if (Pattern.matches("-", currentChar))
						coef = "-1";
					else if (Pattern.matches("\\w", currentChar))
						species += currentChar;
					else if (Pattern.matches("\\s", currentChar)) {
						if (species.matches(FormulaUtils.getInstance().getKeywordsRegEx())) {
							loadedReactionStr = "";
							return false;
						}
						if (!species.isBlank())
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

	public Formula getFormula() {
		return formula;
	}

	public void setFormula(Formula formula) {
		this.formula = formula;
		parse();
	}

	public SortedMap<String, SortedMap<String, FormulaArrayValue>> getFormulaSubstratesArrayParametersRAW() {
		return formulaSubstratesArrayParameters;
	}

	public SortedMap<String, SortedMap<String, FormulaArrayValue>> getFormulaSubstratesArrayParameters() {
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
			SortedMap<String, FormulaArrayValue> mapBySpecies = null;
			for (String constantKey : formula.getParametersBySubsAndModif()) {
				if (!formulaSubstratesArrayParameters.containsKey(constantKey))
					mapBySpecies = new TreeMap<>();
				else
					mapBySpecies = formulaSubstratesArrayParameters.get(constantKey);
				// remove unused substrates
				keysToRemove.clear();
				for (String key : mapBySpecies.keySet())
					if (!getLeftPartSpecies().containsKey(key))
						keysToRemove.add(key);
				for (String key : keysToRemove)
					mapBySpecies.remove(key);
				// add missing substrates
				for (String key : getLeftPartSpecies().keySet()) {
					if (!mapBySpecies.containsKey(key))
						mapBySpecies.put(key, null);
				}
				formulaSubstratesArrayParameters.put(constantKey, mapBySpecies);
			}
		}
		return formulaSubstratesArrayParameters;
	}

	public SortedMap<String, SortedMap<String, FormulaArrayValue>> getFormulaModifiersArrayParametersRAW() {
		return formulaModifiersArrayParameters;
	}

	public SortedMap<String, SortedMap<String, FormulaArrayValue>> getFormulaModifiersArrayParameters() {
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
			SortedMap<String, FormulaArrayValue> mapByModifiers = null;
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

	public SortedMap<String, FormulaValue> getFormulaGeneralParameters() {
		if (formula == null)
			formulaValues.clear();
		else {
			// remove unused keys
			List<String> keysToRemove = new ArrayList<>();
			for (String constantKey : formulaValues.keySet())
				if (!formula.getParameters().containsKey(constantKey))
					keysToRemove.add(constantKey);
			for (String key : keysToRemove)
				formulaValues.remove(key);
			// add missing keys
			for (String constantKey : formula.getParameters().keySet()) {
				if (!formulaValues.containsKey(constantKey))
					formulaValues.put(constantKey, null);
				else {
					// formulaValues contains the key
					// check if value is still valid
					if (formulaValues.get(constantKey) != null && formulaValues.get(constantKey).isFilled()) {
						if (formulaValues.get(constantKey).getType() == FormulaValueType.SUBSTRATE && !this
								.getLeftPartSpecies().containsKey(formulaValues.get(constantKey).getValue()))
							formulaValues.put(constantKey, null);
						else if (formulaValues.get(constantKey).getType() == FormulaValueType.MODIFIER
								&& !this.getModifiers().containsKey(formulaValues.get(constantKey).getValue()))
							formulaValues.put(constantKey, null);
					}
				}
			}
		}
		return formulaValues;
	}

	public SortedMap<String, SortedMap<String, FormulaArrayValue>> getGeneratedFormulaModifierArrayParametersForFormula(
			Formula f) {
		SortedMap<String, SortedMap<String, FormulaArrayValue>> parameters = new TreeMap<>();
		if (f != null) {
			SortedMap<String, FormulaArrayValue> mapByModifiers = null;
			for (String parName : f.getParametersBySubsAndModif()) {
				mapByModifiers = new TreeMap<>();
				for (String sp : modifiers.keySet()) {
					mapByModifiers.put(sp, new FormulaArrayValue(parName,sp,null));
				}
				parameters.put(parName, mapByModifiers);
			}
		}
		return parameters;
	}

	public SortedMap<String, SortedMap<String, FormulaArrayValue>> getGeneratedFormulaSubstrateArrayParametersForFormula(
			Formula f) {
		SortedMap<String, SortedMap<String, FormulaArrayValue>> parameters = new TreeMap<>();
		if (f != null) {
			SortedMap<String, FormulaArrayValue> mapBySpecies = null;
			for (String parName : f.getParametersBySubsAndModif()) {
				mapBySpecies = new TreeMap<>();
				for (String sp : this.getLeftPartSpecies().keySet()) {
					mapBySpecies.put(sp, new FormulaArrayValue(parName,sp,null));
				}
				parameters.put(parName, mapBySpecies);
			}
		}
		return parameters;
	}

	public SortedMap<String, FormulaValue> getFormulaGenParsForFormula(Formula f) {
		SortedMap<String, FormulaValue> values = new TreeMap<>();
		if (f != null) {
			for (String constantKey : f.getParameters().keySet())
				values.put(constantKey, null);
		}
		return values;
	}

	public boolean isFormulaAndParametersSet() {
		// has formula with parameter values
		if (!parse() || getFormula() == null)
			return false;
		for (FormulaValue co : getFormulaGeneralParameters().values()) {
			if (co == null || !co.isFilled()
					|| co.getType() == FormulaValueType.SUBSTRATE
							&& !leftPartSpecies.containsKey(co.getValue())
					|| co.getType() == FormulaValueType.MODIFIER && !modifiers.containsKey(co.getValue()))
				return false;
		}
		for (Map<String, FormulaArrayValue> constMap : getFormulaSubstratesArrayParameters().values())
			for (FormulaArrayValue co : constMap.values())
				if (co == null || !co.isFilled())
					return false;
		for (Map<String, FormulaArrayValue> constMap : getFormulaModifiersArrayParameters().values())
			for (FormulaArrayValue co : constMap.values())
				if (co == null || !co.isFilled())
					return false;
		return true;
	}

	// //////////////////////////////////

	/**
	 * only keyset is used, not values! Values/Concentrations are stored in
	 * getRightPartSpecies() getLeftPartSpecies() getModifiers()
	 */
	public SortedMap<String, Integer> getBothSides() {
		return bothSidesSpecies;
	}

	public SortedMap<String, Integer> getModifiers() {
		getBothSides();
		return modifiers;
	}

	public boolean isReactionConcentrationsSet(SortedMap<String, String> concentrations) {
		for (String sp : getBothSides().keySet())
			if (!concentrations.containsKey(sp) || concentrations.get(sp) == null)
				return false;
		for (String sp : getModifiers().keySet())
			if (!concentrations.containsKey(sp) || concentrations.get(sp) == null)
				return false;
		return true;
	}

	public SortedMap<String, Integer> getLeftPartSpecies() {
		return leftPartSpecies;
	}

	public SortedMap<String, Integer> getRightPartSpecies() {
		return rightPartSpecies;
	}

	@Override
	public int compareTo(Reaction react2) {
		return Integer.valueOf(this.idJava).compareTo(Integer.valueOf((react2).getIdJava()));
	}

	///////////// REACTION ORDER
	public SortedMap<String, ReactantReactionLevel> getReactants() {
		SortedMap<String, ReactantReactionLevel> res = new TreeMap<>();
		if (!this.isFormulaAndParametersSet())
			return res;
		for (String substrate : this.leftPartSpecies.keySet()) {
			for (String par : this.formulaValues.keySet()) {
				FormulaValue fv = this.formulaValues.get(par);
				if (fv.getType() == FormulaValueType.SUBSTRATE && substrate.equals(fv.getValue())) {
					Integer powerInt;
					String powerStr = this.getFormula().getParameters().get(par).getPower();
					try {
						powerInt = Integer.valueOf(powerStr); // powerStr contains a number
					} catch (Exception e1) {
						try {
							powerInt = Integer.valueOf(this.formulaValues.get(powerStr).getValue()); //try to substitute numerical parameter
						} catch (Exception e2) {
							powerInt = 1;
						}
					}
					if (powerInt < 1)
						powerInt = 1;
					res.put(substrate,new ReactantReactionLevel(substrate, powerInt, this.leftPartSpecies.get(substrate)));
				}
			}
		}
		return res;
	}
	
	public int getReactionTotalOrder() {
		int res = 0;
		for (ReactantReactionLevel r : this.getReactants().values())
			res+=r.getOrder();
		return res;
	}
}
