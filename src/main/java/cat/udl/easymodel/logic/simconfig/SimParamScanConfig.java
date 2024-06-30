package cat.udl.easymodel.logic.simconfig;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import cat.udl.easymodel.logic.formula.FormulaArrayValue;
import cat.udl.easymodel.logic.formula.FormulaValue;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.model.Species;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.SpeciesType;
import cat.udl.easymodel.logic.types.SpeciesVarTypeType;

public class SimParamScanConfig {
	private ArrayList<ParamScanEntry> parameters = new ArrayList<>();
	private ArrayList<ParamScanEntry> independentVars = new ArrayList<>();

	SimParamScanConfig() {
	}

	SimParamScanConfig(SimParamScanConfig from, Model toModel) {
		for (ParamScanEntry pse : from.getParameters())
			this.parameters.add(new ParamScanEntry(pse,toModel));
		for (ParamScanEntry pse : from.getIndependentVars())
			this.independentVars.add(new ParamScanEntry(pse,toModel));
	}

	public ArrayList<ParamScanEntry> getParameters() {
		return parameters;
	}

	public ArrayList<ParamScanEntry> getIndependentVars() {
		return independentVars;
	}

	public boolean hasParametersToScan(Model selModel) {
		this.cleanUnusedParams(selModel);
		return parameters.size() > 0 || independentVars.size() > 0;
	}

	public void cleanUnusedParams(Model selModel) {
		ArrayList<ParamScanEntry> toDelete = new ArrayList<ParamScanEntry>();
		for (ParamScanEntry configEntry : parameters) {
			boolean isDelete = true;
			rLoop: for (Reaction r : selModel) {
				if (!configEntry.isArrayParameter()) {
					for (FormulaValue fv : r.getFormulaGeneralParameters().values()) {
						if (fv.getType() == FormulaValueType.CONSTANT && fv.isFilled()
								&& configEntry.getParameterName().equals(fv.getParameterName())) {
							isDelete = false;
							break rLoop;
						}
					}
				} else {
					Set<Entry<String, SortedMap<String, FormulaArrayValue>>> entrySet = null;
					if (configEntry.getSpeciesType() == SpeciesType.REACTIVE)
						entrySet = r.getFormulaSubstratesArrayParameters().entrySet();
					else if (configEntry.getSpeciesType() == SpeciesType.MODIFIER)
						entrySet = r.getFormulaModifiersArrayParameters().entrySet();

					for (SortedMap.Entry<String, SortedMap<String, FormulaArrayValue>> entry : entrySet) {
						for (SortedMap.Entry<String, FormulaArrayValue> entry2 : entry.getValue().entrySet()) {
							if (entry2.getValue().isFilled() && configEntry.getParameterName().equals(entry.getKey())
									&& configEntry.getParameterSpeciesName().equals(entry2.getKey())) {
								isDelete = false;
								break rLoop;
							}
						}
					}
				}
			}
			if (isDelete)
				toDelete.add(configEntry);
		}
		for (ParamScanEntry configEntry : toDelete)
			parameters.remove(configEntry);
		toDelete.clear();
		for (ParamScanEntry configEntry : independentVars) {
			boolean isDelete = true;
			for (String spName : selModel.getAllSpecies().keySet()) {
				Species sp = selModel.getAllSpecies().get(spName);
				if (sp.getVarType() == SpeciesVarTypeType.INDEPENDENT
						&& spName.equals(configEntry.getParameterName())) {
					isDelete = false;
					break;
				}
			}
			if (isDelete)
				toDelete.add(configEntry);
		}
		for (ParamScanEntry configEntry : toDelete)
			independentVars.remove(configEntry);
	}

	public void reset() {
		parameters.clear();
		independentVars.clear();
	}
}
