package cat.udl.easymodel.logic.simconfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import cat.udl.easymodel.logic.types.InputType;
import cat.udl.easymodel.logic.types.SimType;

public class SimConfig {
	private SimType simType;
	// deterministic
	private SimConfigArray dynamic = new SimConfigArray();
	private SimConfigArray steadyState = new SimConfigArray();
	private SimConfigArray plotDeterministic = new SimConfigArray();
	private ArrayList<SimConfigArray> plotViews = new ArrayList<>();
	// stochastic
	private SimConfigArray stochastic = new SimConfigArray();
	private SimConfigArray plotStochastic = new SimConfigArray();

	public SimConfig() {
		reset();
	}

	public void reset() {
		SimConfigEntry entry = null;
		setSimType(SimType.DETERMINISTIC);
		// DYNAMIC
		for (SimConfigArray m : plotViews)
			if (m != null)
				m.clear();
		plotViews.clear();

		dynamic.clear();
		dynamic.add(new SimConfigEntry("Enable", true, InputType.CHECKBOX, "Enable", "Enable this simulation"));
		entry = new SimConfigEntry("Ti", "0", InputType.DECIMAL, "Initial time", "Simulation starting time");
		entry.setValueRange("0", null);
		dynamic.add(entry);
		entry=new SimConfigEntry("Tf", "100", InputType.DECIMAL, "Final time", "Simulation ending time");
		entry.setValueRange("0", null);
		dynamic.add(entry);
		entry = new SimConfigEntry("TStep", "0.1", InputType.DECIMAL, "Time step", "Simulation stepping");
		entry.setValueRange("0.000001", "0.1");
		dynamic.add(entry);
		dynamic.add(new SimConfigEntry("Gains", false, InputType.CHECKBOX, "Gains",
				"Add independent variables gains to simulation"));
		dynamic.add(new SimConfigEntry("Sensitivities", false, InputType.CHECKBOX, "Sensitivities",
				"Add kinetic rate parameters sensitivities to simulation"));

		steadyState.clear();
		steadyState.add(new SimConfigEntry("Enable", false, InputType.CHECKBOX, "Enable", "Enable this simulation"));
		steadyState.add(new SimConfigEntry("Threshold", "10^-30", InputType.MATHEXPRESSION, "Threshold",
				"Threshold condition for the Steady State finding calculus. Low values results in a low tolerance against finding the Steady State."));
		steadyState.add(new SimConfigEntry("Stability", false, InputType.CHECKBOX, "Stability analysis",
				"Add further stability analysis to simulation"));
		steadyState.add(new SimConfigEntry("Gains", false, InputType.CHECKBOX, "Gains",
				"Add independent variables gains to simulation"));
		steadyState.add(new SimConfigEntry("Sensitivities", false, InputType.CHECKBOX, "Sensitivities",
				"Add kinetic rate parameters sensitivities to simulation"));

		plotDeterministic.clear();
		plotDeterministic.add(new SimConfigEntry("LineThickness", new SimConfigSlider("0.006", "1", "10", 1000),
				InputType.SLIDER, "Line Thickness", "Plot line thickness"));
		plotDeterministic.add(new SimConfigEntry("FontFamily", "Arial", InputType.STRING, "Font Family", null));
		plotDeterministic.add(new SimConfigEntry("FontSize", new SimConfigSlider("14", "10", "24", 1), InputType.SLIDER,
				"Font Size", null));
		plotDeterministic.add(new SimConfigEntry("FontWeight", true, InputType.CHECKBOX, "Font Bold", null)); // Bold
		plotDeterministic.add(new SimConfigEntry("FontSlant", false, InputType.CHECKBOX, "Font Italic", null)); // "Plain");
		plotDeterministic.add(new SimConfigEntry("ImageSize", new SimConfigSlider("700", "500", "3000", 1),
				InputType.SLIDER, "Image Width (pixels)", "Image width of plots displayed in results layout"));
		// STOCHASTIC
		stochastic.clear();
		entry = new SimConfigEntry("Ti", "0", InputType.DECIMAL, "Initial time", "Simulation starting time");
		entry.setValueRange("0", null);
		stochastic.add(entry);
		entry=new SimConfigEntry("Tf", "10", InputType.DECIMAL, "Final time", "Simulation ending time");
		entry.setValueRange("0", null);
		stochastic.add(entry);
		entry = new SimConfigEntry("TStep", "0.001", InputType.DECIMAL, "Time step", "Simulation stepping");
		entry.setValueRange("0.000001", "1"); //0.01
		stochastic.add(entry);
		entry=new SimConfigEntry("Iterations", "3", InputType.NATURAL, "Iterations",
				"Number of stochastic iterations");
		entry.setValueRange("1", null);
		stochastic.add(entry);
		stochastic.add(new SimConfigEntry("CellSize", "Prokaryotic Cell", InputType.SELECT, "Cell size", "Cell size"));

		plotStochastic.clear();
		plotStochastic.add(new SimConfigEntry("LineThickness", new SimConfigSlider("0.003", "1", "10", 1000),
				InputType.SLIDER, "Line Thickness", "Plot line thickness"));
		plotStochastic.add(new SimConfigEntry("FontFamily", "Arial", InputType.STRING, "Font Family", null));
		plotStochastic.add(new SimConfigEntry("FontSize", new SimConfigSlider("14", "10", "24", 1), InputType.SLIDER,
				"Font Size", null));
		plotStochastic.add(new SimConfigEntry("FontWeight", true, InputType.CHECKBOX, "Font Bold", null)); // Bold
		plotStochastic.add(new SimConfigEntry("FontSlant", false, InputType.CHECKBOX, "Font Italic", null)); // "Plain");
		plotStochastic.add(new SimConfigEntry("ImageSize", new SimConfigSlider("700", "500", "3000", 1),
				InputType.SLIDER, "Image Width (pixels)", "Image width of plots displayed in results layout"));
	}

	public void checkSimConfigs() throws Exception {
		if (getSimType() == SimType.DETERMINISTIC) {
			int numOfSims = 0;
			if ((Boolean) dynamic.get("Enable").getValue()) {
				numOfSims++;
				for (SimConfigEntry entry : dynamic) {
					entry.checkValue("Dynamic");
				}
				if (Double.valueOf((String) dynamic.get("Ti").getValue()) >= Double
						.valueOf((String) dynamic.get("Tf").getValue()))
					throw new Exception("Dynamic: Final time must be greater than Initial time");
			}
			if ((Boolean) steadyState.get("Enable").getValue()) {
				numOfSims++;
				for (SimConfigEntry entry : steadyState) {
					entry.checkValue("Steady State");
				}
			}
			if (numOfSims == 0)
				throw new Exception("No simulations are selected");
			cleanPlotViews();
		} else if (getSimType() == SimType.STOCHASTIC) {
			for (SimConfigEntry entry : stochastic) {
				entry.checkValue("Stochastic");
			}
			if (Double.valueOf((String) stochastic.get("Ti").getValue()) >= Double
					.valueOf((String) stochastic.get("Tf").getValue()))
				throw new Exception("Stochastic: Final time must be greater than Initial time");
		}
	}

//////////////////////////////////////////////////////////
	public void addPlotView() {
		SimConfigArray aPlotConfig = new SimConfigArray();
		SimConfigEntry entry = new SimConfigEntry("DepVarsToShow", new ArrayList<String>(), InputType.ARRAYCHECKBOX,
				"Dependent variables", "Select which dependent variables will be shown in this plot view");
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
			for (String depVar : depVars) {
				res.put(depVar, null);
			}
		}
		return res;
	}

//////////////////////////////////////////
	public ArrayList<String> getAllMathExpressions() {
		ArrayList<String> res = new ArrayList<String>();
		if (getSimType() == SimType.DETERMINISTIC) {
			for (SimConfigEntry en : dynamic)
				if (en.getType() == InputType.MATHEXPRESSION)
					res.add((String) en.getValue());
			for (SimConfigEntry en : steadyState)
				if (en.getType() == InputType.MATHEXPRESSION)
					res.add((String) en.getValue());
			for (SimConfigEntry en : plotDeterministic)
				if (en.getType() == InputType.MATHEXPRESSION)
					res.add((String) en.getValue());
			for (SimConfigArray configArray : plotViews) {
				for (SimConfigEntry en : configArray)
					if (en.getType() == InputType.MATHEXPRESSION)
						res.add((String) en.getValue());
			}
		} else if (getSimType() == SimType.STOCHASTIC) {
			for (SimConfigEntry en : stochastic)
				if (en.getType() == InputType.MATHEXPRESSION)
					res.add((String) en.getValue());
			for (SimConfigEntry en : plotStochastic)
				if (en.getType() == InputType.MATHEXPRESSION)
					res.add((String) en.getValue());
		}
		return res;
	}

	public SimType getSimType() {
		return simType;
	}

	public void setSimType(SimType simType) {
		this.simType = simType;
	}

	public SimConfigArray getStochastic() {
		return stochastic;
	}

	public SimConfigArray getStochasticPlotSettings() {
		return plotStochastic;
	}

	public SimConfigArray getDynamic() {
		return dynamic;
	}

	public SimConfigArray getSteadyState() {
		return steadyState;
	}

	public SimConfigArray getDeterministicPlotSettings() {
		return plotDeterministic;
	}

	public ArrayList<SimConfigArray> getDeterministicPlotViews() {
		return plotViews;
	}
}
