package cat.udl.easymodel.logic.simconfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import cat.udl.easymodel.logic.types.InputType;

public class SimConfig {
	private SimConfigArray dynamic = new SimConfigArray();
	private SimConfigArray steadyState = new SimConfigArray();
	private SimConfigArray commonPlot = new SimConfigArray();
	private ArrayList<SimConfigArray> plotViews = new ArrayList<>();

	public SimConfig() {
		initConfig();
	}

	private void initConfig() {
		dynamic.add(new SimConfigEntry("Enable", true, InputType.CHECKBOX, "Enable", "Enable this simulation"));
		dynamic.add(new SimConfigEntry("Ti", "0", InputType.DECIMAL, "Initial time", "Simulation starting time"));
		dynamic.add(new SimConfigEntry("Tf", "100", InputType.DECIMAL, "Final time", "Simulation ending time"));
		dynamic.add(new SimConfigEntry("TSteps", "0.1", InputType.DECIMAL, "Time step", "Simulation stepping"));
		dynamic.add(new SimConfigEntry("Gains", false, InputType.CHECKBOX, "Gains", "Add independent variables gains to simulation"));
		dynamic.add(new SimConfigEntry("Sensitivities", false, InputType.CHECKBOX, "Sensitivities", "Add kinetic rate parameters sensitivities to simulation"));

		steadyState.add(new SimConfigEntry("Enable", false, InputType.CHECKBOX, "Enable", "Enable this simulation"));
		steadyState.add(new SimConfigEntry("Threshold", "10^-30", InputType.MATHEXPRESSION, "Threshold", "Threshold condition for the Steady State finding calculus. Low values results in a low tolerance against finding the Steady State."));
		steadyState.add(new SimConfigEntry("Stability", false, InputType.CHECKBOX, "Stability analysis", "Add further stability analysis to simulation"));
		steadyState.add(new SimConfigEntry("Gains", false, InputType.CHECKBOX, "Gains", "Add independent variables gains to simulation"));
		steadyState.add(new SimConfigEntry("Sensitivities", false, InputType.CHECKBOX, "Sensitivities", "Add kinetic rate parameters sensitivities to simulation"));

		commonPlot.add(new SimConfigEntry("LineThickness", new SimConfigSlider("0.006", "1", "10", 1000), InputType.SLIDER, "Line Thickness", "Plot line thickness"));
		commonPlot.add(new SimConfigEntry("FontFamily", "Arial", InputType.STRING, "Font Family", null));
		commonPlot.add(new SimConfigEntry("FontSize", new SimConfigSlider("14", "10", "24", 1), InputType.SLIDER, "Font Size", null));
		commonPlot.add(new SimConfigEntry("FontWeight", true, InputType.CHECKBOX, "Font Bold", null)); //Bold
		commonPlot.add(new SimConfigEntry("FontSlant", false, InputType.CHECKBOX, "Font Italic", null)); //"Plain");
		commonPlot.add(new SimConfigEntry("ImageSize", new SimConfigSlider("700", "500", "3000", 1), InputType.SLIDER, "Image Width (pixels)", "Image width of plots displayed in results layout"));
	}

	public SimConfigArray getDynamic() {
		return dynamic;
	}

	public SimConfigArray getSteadyState() {
		return steadyState;
	}

	public SimConfigArray getPlotSettings() {
		return commonPlot;
	}

	public ArrayList<SimConfigArray> getPlotViews() {
		return plotViews;
	}

	public void reset() {
		dynamic.clear();
		steadyState.clear();
		commonPlot.clear();
		for (SimConfigArray m : plotViews) {
			if (m != null)
				m.clear();
		}
		plotViews.clear();

		initConfig();
	}

	public void checkSimConfigs() throws Exception {
		int numSimType = 0;
		if ((Boolean) dynamic.get("Enable").getValue()) {
			numSimType++;
			for (SimConfigEntry en : dynamic)
				if (en.isMandatory() && en.getValue() == null)
					throw new Exception("Invalid Dynamic simulation configuration");
		}
		if ((Boolean) steadyState.get("Enable").getValue()) {
			numSimType++;
			for (SimConfigEntry en : steadyState)
				if (en.isMandatory() && en.getValue() == null)
					throw new Exception("Invalid Steady State simulation configuration");
		}
		if (numSimType == 0)
			throw new Exception("No simulations are selected");
		cleanPlotViews();
	}

	public void addPlotView() {
		SimConfigArray aPlotConfig = new SimConfigArray();
		SimConfigEntry entry = new SimConfigEntry("DepVarsToShow", new ArrayList<String>(), InputType.ARRAYCHECKBOX, "Dependent variables", "Select which dependent variables will be shown in this plot view");
		aPlotConfig.add(entry);
		plotViews.add(aPlotConfig);
	}

	public void removePlotView(int i) {
		if (plotViews.get(i) != null && plotViews.size() > 1) {
			plotViews.get(i).clear();
			plotViews.remove(i);
		}
	}

	public void clearPlotViews() {
		for (SimConfigArray aPlotConfig : plotViews)
			aPlotConfig.clear();
		plotViews.clear();
	}

	@SuppressWarnings("unchecked")
	public void cleanPlotViews() {
		ArrayList<SimConfigArray> mapsToDelete = new ArrayList<>();
		for (SimConfigArray aPlotConfig : plotViews) {
			ArrayList<String> depVars = ((ArrayList<String>) aPlotConfig.get("DepVarsToShow").getValue());
			if (depVars.isEmpty())
				mapsToDelete.add(aPlotConfig);
		}
		for (SimConfigArray aPlotConfig : mapsToDelete) {
			plotViews.remove(aPlotConfig);
		}
	}

	@SuppressWarnings("unchecked")
	public void initPlotViews(SortedMap<String, String> allSpeciesTimeDependent) {
		clearPlotViews();
		addPlotView();
		SimConfigArray plotView = plotViews.get(0);
		ArrayList<String> arr = (ArrayList<String>) plotView.get("DepVarsToShow").getValue();
		for (String dv : allSpeciesTimeDependent.keySet()) {
			arr.add(dv);
		}
	}
	
	@SuppressWarnings("unchecked")
	public TreeMap<String, Boolean> getUnifiedDepVarsFromPlotViews() {
		TreeMap<String, Boolean> res = new TreeMap<>();
		for (SimConfigArray plotView : plotViews) {
			ArrayList<String> depVars = ((ArrayList<String>) plotView.get("DepVarsToShow").getValue());
			for(String depVar : depVars) {
				res.put(depVar, null);
			}
		}
		return res;
	}

	public ArrayList<String> getAllMathExpressions() {
		ArrayList<String> res = new ArrayList<String>();
		for (SimConfigEntry en: dynamic)
			if (en.getType() == InputType.MATHEXPRESSION)
				res.add((String)en.getValue());
		for (SimConfigEntry en: steadyState)
			if (en.getType() == InputType.MATHEXPRESSION)
				res.add((String)en.getValue());
		for (SimConfigEntry en: commonPlot)
			if (en.getType() == InputType.MATHEXPRESSION)
				res.add((String)en.getValue());
		for (SimConfigArray configArray: plotViews) {
			for (SimConfigEntry en: configArray)
				if (en.getType() == InputType.MATHEXPRESSION)
					res.add((String)en.getValue());
		}
		return res;
	}
}
