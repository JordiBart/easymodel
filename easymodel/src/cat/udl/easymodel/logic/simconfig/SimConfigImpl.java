package cat.udl.easymodel.logic.simconfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class SimConfigImpl implements SimConfig {
	private Map<String, Object> dynamic = new LinkedHashMap<>();
	private Map<String, Object> steadyState = new LinkedHashMap<>();
	private Map<String, Object> commonPlot = new LinkedHashMap<>();
	private ArrayList<Map<String, Object>> plotViews = new ArrayList<>();

	public SimConfigImpl() {
		initConfig();
	}

	private void initConfig() {
		dynamic.put("Enable", true);
		dynamic.put("Ti", (String) "0");
		dynamic.put("Tf", (String) "100");
		dynamic.put("TSteps", (String) "0.1");
		dynamic.put("Gains", false);
		dynamic.put("Sensitivities", false);

		steadyState.put("Enable", false);
		steadyState.put("Stability", false);
		steadyState.put("Gains", false);
		steadyState.put("Sensitivities", false);

		commonPlot.put("LineThickness", "0.006");
		commonPlot.put("FontFamily", "Arial");
		commonPlot.put("FontSize", "14");
		commonPlot.put("FontWeight", "Bold");
		commonPlot.put("FontSlant", "Plain");
		commonPlot.put("ImageResolution", "300");
		commonPlot.put("ImageSize-Small", "500");
		commonPlot.put("ImageSize-Big", "1500");
	}

	@Override
	public Map<String, Object> getDynamic() {
		return dynamic;
	}

	@Override
	public Map<String, Object> getSteadyState() {
		return steadyState;
	}

	@Override
	public Map<String, Object> getPlot() {
		return commonPlot;
	}

	@Override
	public ArrayList<Map<String, Object>> getPlotViews() {
		return plotViews;
	}

	@Override
	public void reset() {
		dynamic.clear();
		steadyState.clear();
		commonPlot.clear();
		for (Map<String, Object> m : plotViews) {
			if (m != null)
				m.clear();
		}
		plotViews.clear();

		initConfig();
	}

	@Override
	public void checkSimConfigs() throws Exception {
		int numSimType = 0;
		if ((Boolean) dynamic.get("Enable")) {
			numSimType++;
			for (String key : dynamic.keySet())
				if (dynamic.get(key) == null)
					throw new Exception("Invalid Dynamic simulation configuration");
		}
		if ((Boolean) steadyState.get("Enable")) {
			numSimType++;
			for (String key : steadyState.keySet())
				if (steadyState.get(key) == null)
					throw new Exception("Invalid Steady State simulation configuration");
		}
		if (numSimType == 0)
			throw new Exception("No simulations are selected");
		cleanPlotViews();
	}

	@Override
	public void addPlotView() {
		Map<String, Object> aPlotConfig = new LinkedHashMap<>();
		aPlotConfig.put("DepVarsToShow", new ArrayList<String>());
		plotViews.add(aPlotConfig);
	}

	@Override
	public void removePlotView(int i) {
		if (plotViews.get(i) != null && plotViews.size() > 1) {
			plotViews.get(i).clear();
			plotViews.remove(i);
		}
	}

	@Override
	public void clearPlotViews() {
		for (Map<String, Object> aPlotConfig : plotViews)
			aPlotConfig.clear();
		plotViews.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void cleanPlotViews() {
		ArrayList<Map<String, Object>> mapsToDelete = new ArrayList<>();
		for (Map<String, Object> map : plotViews) {
			ArrayList<String> depVars = ((ArrayList<String>) map.get("DepVarsToShow"));
			if (depVars.isEmpty())
				mapsToDelete.add(map);
		}
		for (Map<String, Object> map : mapsToDelete) {
			plotViews.remove(map);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initPlotViews(SortedMap<String, String> allSpeciesTimeDependent) {
		clearPlotViews();
		addPlotView();
		Map<String, Object> map = plotViews.get(0);
		ArrayList<String> arr = (ArrayList<String>) map.get("DepVarsToShow");
		for (String dv : allSpeciesTimeDependent.keySet()) {
			arr.add(dv);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public TreeMap<String, Boolean> getUnifiedDepVarsFromPlotViews() {
		TreeMap<String, Boolean> res = new TreeMap<>();
		for (Map<String, Object> map : plotViews) {
			ArrayList<String> depVars = ((ArrayList<String>) map.get("DepVarsToShow"));
			for(String depVar : depVars) {
				res.put(depVar, null);
			}
		}
		return res;
	}
}
