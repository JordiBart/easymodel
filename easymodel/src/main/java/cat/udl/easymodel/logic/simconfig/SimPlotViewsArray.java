package cat.udl.easymodel.logic.simconfig;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.InputType;

public class SimPlotViewsArray extends ArrayList<SimConfigArray> {
	SimPlotViewsArray() {

	}

	@SuppressWarnings("unchecked")
	public void initPlotViews(Model m) {
		clearPlotViews();
		addNewPlotView();
		SimConfigArray plotView = this.get(0);
		ArrayList<String> arr = (ArrayList<String>) plotView.get("DepVarsToShow").getValue();
		for (String dv : m.getAllSpeciesTimeDependent().keySet()) {
			arr.add(dv);
		}
	}

	public void addNewPlotView() {
		SimConfigArray aPlotConfig = new SimConfigArray();
		SimConfigEntry entry = new SimConfigEntry("DepVarsToShow", new ArrayList<String>(), InputType.ARRAYCHECKBOX,
				"Dependent variables", "Select which dependent variables will be shown in this plot view");
		aPlotConfig.add(entry);
		this.add(aPlotConfig);
	}

	public void removeAtIndex(int i) {
		if (this.get(i) != null && this.size() > 1) {
			this.get(i).clear();
			this.remove(i);
		}
	}

	@SuppressWarnings("unchecked")
	public void cleanPlotViews() {
		ArrayList<SimConfigArray> mapsToDelete = new ArrayList<>();
		for (SimConfigArray aPlotConfig : this) {
			ArrayList<String> depVars = ((ArrayList<String>) aPlotConfig.get("DepVarsToShow").getValue());
			if (depVars.isEmpty())
				mapsToDelete.add(aPlotConfig);
		}
		for (SimConfigArray aPlotConfig : mapsToDelete) {
			this.remove(aPlotConfig);
		}
	}

	public void clearPlotViews() {
		for (SimConfigArray aPlotConfig : this)
			aPlotConfig.clear();
		this.clear();
	}

	@SuppressWarnings("unchecked")
	public void setEachDepVarToOneView(Model selectedModel) {
		clearPlotViews();
		for (String dv : selectedModel.getAllSpeciesTimeDependent().keySet()) {
			addNewPlotView();
			((ArrayList<String>) this.get(this.size() - 1).get("DepVarsToShow").getValue()).add(dv);
		}
	}

	@SuppressWarnings("unchecked")
	public TreeMap<String, Boolean> getUnifiedDepVars() {
		TreeMap<String, Boolean> res = new TreeMap<>();
		for (SimConfigArray plotView : this) {
			ArrayList<String> depVars = ((ArrayList<String>) plotView.get("DepVarsToShow").getValue());
			for (String depVar : depVars) {
				res.put(depVar, null);
			}
		}
		return res;
	}
}
