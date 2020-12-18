package cat.udl.easymodel.logic.simconfig;

import java.util.ArrayList;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.InputType;
import cat.udl.easymodel.logic.types.SimType;

public class SimConfig {
	private SimType simType;
	private SimConfigArray plotConfig = new SimConfigArray();
	// deterministic
	private SimConfigArray dynamic = new SimConfigArray();
	private SimPlotViewsArray dynamic_PlotViews = new SimPlotViewsArray();
	private SimParamScanConfig dynamic_ParameterScan = new SimParamScanConfig();
	private SimConfigArray steadyState = new SimConfigArray();
	private SimParamScanConfig steadyState_ParameterScan = new SimParamScanConfig();
	// stochastic
	private SimConfigArray stochastic = new SimConfigArray();

	public SimConfig() {
		reset();
	}
	
	public SimConfig(SimConfig from) {
		simType=from.simType;
		plotConfig.addAll(from.getPlotSettings());
		dynamic.addAll(from.getDynamic());
		dynamic_PlotViews.addAll(from.getDynamic_PlotViews());
		dynamic_ParameterScan=new SimParamScanConfig(from.getDynamic_ParameterScan());
		steadyState.addAll(from.getSteadyState());
		steadyState_ParameterScan=new SimParamScanConfig(from.getSteadyState_ParameterScan());
		stochastic.addAll(from.getStochastic());
	}

	public void reset() {
		SimConfigEntry entry = null;
		setSimType(SimType.DETERMINISTIC);
		// DYNAMIC
		for (SimConfigArray m : dynamic_PlotViews)
			if (m != null)
				m.clear();
		dynamic_PlotViews.clear();
		dynamic.clear();
		dynamic.add(new SimConfigEntry("Enable", true, InputType.CHECKBOX, "Enable", "Enable this simulation"));
		entry = new SimConfigEntry("Ti", "0", InputType.DECIMAL, "Initial time", "Simulation starting time");
		entry.setValueRange("0", null);
		dynamic.add(entry);
		entry = new SimConfigEntry("Tf", "100", InputType.DECIMAL, "Final time", "Simulation ending time");
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
		steadyState.add(new SimConfigEntry("Threshold", "10^-12", InputType.MATHEXPRESSION, "Threshold",
				"Threshold condition for the Steady State finding calculus. Low values results in a low tolerance against finding the Steady State."));
		steadyState.add(new SimConfigEntry("Stability", false, InputType.CHECKBOX, "Stability analysis",
				"Add further stability analysis to simulation"));
		steadyState.add(new SimConfigEntry("Gains", false, InputType.CHECKBOX, "Gains",
				"Add independent variables gains to simulation"));
		steadyState.add(new SimConfigEntry("Sensitivities", false, InputType.CHECKBOX, "Sensitivities",
				"Add kinetic rate parameters sensitivities to simulation"));

		plotConfig.clear();
		plotConfig.add(new SimConfigEntry("LineThickness", new SimConfigSlider("0.006", "1", "10", 1000),
				InputType.SLIDER, "Line Thickness", "Plot line thickness"));
		plotConfig.add(new SimConfigEntry("FontFamily", "Arial", InputType.STRING, "Font Family", null));
		plotConfig.add(new SimConfigEntry("FontSize", new SimConfigSlider("14", "10", "24", 1), InputType.SLIDER,
				"Font Size", null));
		plotConfig.add(new SimConfigEntry("FontWeight", true, InputType.CHECKBOX, "Font Bold", null)); // Bold
		plotConfig.add(new SimConfigEntry("FontSlant", false, InputType.CHECKBOX, "Font Italic", null)); // "Plain");
		plotConfig.add(new SimConfigEntry("ImageSize", new SimConfigSlider("700", "500", "3000", 1),
				InputType.SLIDER, "Image Width (pixels)", "Image width of plots displayed in results layout"));
		// STOCHASTIC
		stochastic.clear();
		entry = new SimConfigEntry("Ti", "0", InputType.DECIMAL, "Initial time", "Simulation starting time");
		entry.setValueRange("0", null);
		stochastic.add(entry);
		entry = new SimConfigEntry("Tf", "100", InputType.DECIMAL, "Final time", "Simulation ending time");
		entry.setValueRange("0", null);
		stochastic.add(entry);
		entry = new SimConfigEntry("TStep", "0.1", InputType.DECIMAL, "Time step", "Simulation stepping");
		entry.setValueRange("0.000001", "100");
		stochastic.add(entry);
		entry = new SimConfigEntry("Iterations", "3", InputType.NATURAL, "Iterations",
				"Number of stochastic iterations");
		entry.setValueRange("1", null);
		stochastic.add(entry);
		stochastic.add(new SimConfigEntry("CellSize", "Prokaryotic Cell", InputType.SELECT, "Cell size", "Cell size"));
		stochastic.add(new SimConfigEntry("TauLeaping", false, InputType.CHECKBOX, "Use Tau-leaping optimization",
				"Use Tau-Leaping optimization to speed up simulation time"));
	}

	public void checkAndAdaptToSimulate(Model model) throws Exception {
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
			dynamic_PlotViews.cleanPlotViews();
			dynamic_ParameterScan.cleanUnusedParams(model);
		} else if (getSimType() == SimType.STOCHASTIC) {
			for (SimConfigEntry entry : stochastic) {
				entry.checkValue("Stochastic");
			}
			if (Double.valueOf((String) stochastic.get("Ti").getValue()) >= Double
					.valueOf((String) stochastic.get("Tf").getValue()))
				throw new Exception("Stochastic: Final time must be greater than Initial time");
		}
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
			for (SimConfigArray configArray : dynamic_PlotViews) {
				for (SimConfigEntry en : configArray)
					if (en.getType() == InputType.MATHEXPRESSION)
						res.add((String) en.getValue());
			}
		} else if (getSimType() == SimType.STOCHASTIC) {
			for (SimConfigEntry en : stochastic)
				if (en.getType() == InputType.MATHEXPRESSION)
					res.add((String) en.getValue());
		}
		for (SimConfigEntry en : plotConfig)
			if (en.getType() == InputType.MATHEXPRESSION)
				res.add((String) en.getValue());
		return res;
	}

	public SimType getSimType() {
		return simType;
	}

	public void setSimType(SimType simType) {
		this.simType = simType;
	}

	public SimConfigArray getPlotSettings() {
		return plotConfig;
	}

	public SimConfigArray getDynamic() {
		return dynamic;
	}
	
	public SimPlotViewsArray getDynamic_PlotViews() {
		return dynamic_PlotViews;
	}

	public SimParamScanConfig getDynamic_ParameterScan() {
		return dynamic_ParameterScan;
	}

	public SimConfigArray getSteadyState() {
		return steadyState;
	}

	public SimParamScanConfig getSteadyState_ParameterScan() {
		return steadyState_ParameterScan;
	}

	public SimConfigArray getStochastic() {
		return stochastic;
	}
}
