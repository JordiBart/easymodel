package cat.udl.easymodel.logic.simconfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.InputType;
import cat.udl.easymodel.logic.types.SimType;
import cat.udl.easymodel.logic.types.SimStochasticMethodType;
import cat.udl.easymodel.logic.types.StochasticGradeType;

public class SimConfig {
    private Set<SimType> simTypesToLaunch = new HashSet<>();
    private SimConfigArray plotConfig = new SimConfigArray();
    // deterministic
    private SimConfigArray dynamic = new SimConfigArray();
    private SimPlotViewArray dynamic_PlotViews = new SimPlotViewArray();
    private SimParamScanConfig dynamic_ParameterScan = new SimParamScanConfig();
    private SimConfigArray steadyState = new SimConfigArray();
    private SimParamScanConfig steadyState_ParameterScan = new SimParamScanConfig();
    // stochastic
    private SimConfigArray stochastic = new SimConfigArray();
    private boolean isStochasticMethodPreselected=false;

    public SimConfig() {
        reset();
    }

    public SimConfig(SimConfig from, Model toModel) {
        this.simTypesToLaunch.addAll(from.simTypesToLaunch);
        this.plotConfig = new SimConfigArray(from.getPlotSettings());
        this.dynamic = new SimConfigArray(from.getDynamic());
        this.dynamic_PlotViews = new SimPlotViewArray(from.getDynamic_PlotViews());
        this.dynamic_ParameterScan = new SimParamScanConfig(from.getDynamic_ParameterScan(),toModel);
        this.steadyState = new SimConfigArray(from.getSteadyState());
        this.steadyState_ParameterScan = new SimParamScanConfig(from.getSteadyState_ParameterScan(),toModel);
        this.stochastic = new SimConfigArray(from.getStochastic());
    }

    public void reset() {
        simTypesToLaunch.clear();
        simTypesToLaunch.add(SimType.DYNAMIC_DETERMINISTIC);
        SimConfigEntry entry = null;
        // DYNAMIC
        dynamic_PlotViews.clearPlotViews();
        dynamic.clear();
        entry = new SimConfigEntry("Ti", "0", InputType.DECIMAL, "Initial Time", "Simulation starting time");
        entry.setValueRange("0", null);
        dynamic.add(entry);
        entry = new SimConfigEntry("Tf", "100", InputType.DECIMAL, "Final Time", "Simulation ending time");
        entry.setValueRange("0", null);
        dynamic.add(entry);
        entry = new SimConfigEntry("TStep", "0.1", InputType.DECIMAL, "Time Step", "Simulation stepping");
        entry.setValueRange("0.000001", "0.1");
        dynamic.add(entry);
        dynamic.add(new SimConfigEntry("Gains", "0", InputType.CHECKBOX, "Gains",
                "Add independent variables gains to simulation"));
        dynamic.add(new SimConfigEntry("Sensitivities", "0", InputType.CHECKBOX, "Sensitivities",
                "Add kinetic rate parameters sensitivities to simulation"));

        steadyState.clear();
        steadyState.add(new SimConfigEntry("Threshold", "10^-12", InputType.MATHEXPRESSION, "Threshold",
                "Threshold condition for the Steady State finding calculus. Low values results in a low tolerance against finding the Steady State."));
        steadyState.add(new SimConfigEntry("Stability", "0", InputType.CHECKBOX, "Stability",
                "Add further stability analysis to simulation"));
        steadyState.add(new SimConfigEntry("Gains", "0", InputType.CHECKBOX, "Gains",
                "Add independent variables gains to simulation"));
        steadyState.add(new SimConfigEntry("Sensitivities", "0", InputType.CHECKBOX, "Sensitivities",
                "Add kinetic rate parameters sensitivities to simulation"));

        plotConfig.clear();
        entry=new SimConfigEntry("LineThickness", "0.006", InputType.DECIMAL, "Line Thickness", "Plot line thickness");
        entry.setValueRange("0.001", "0.1");
        plotConfig.add(entry);
        plotConfig.add(new SimConfigEntry("FontFamily", "Arial", InputType.STRING, "Font Family", null));
        entry=new SimConfigEntry("FontSize", "18", InputType.NATURAL, "Font Size", null);
        entry.setValueRange("10", "24");
        plotConfig.add(entry);
        plotConfig.add(new SimConfigEntry("FontWeight", "1", InputType.CHECKBOX, "Font Bold", null)); // Bold
        plotConfig.add(new SimConfigEntry("FontSlant", "0", InputType.CHECKBOX, "Font Italic", null)); // "Plain");
        entry=new SimConfigEntry("ImageSize", "700", InputType.NATURAL, "Image Width (pixels)", "Image width in pixels of the plots displayed in the results page");
        entry.setValueRange("500", "3000");
        plotConfig.add(entry);
        // STOCHASTIC
        stochastic.clear();
        entry = new SimConfigEntry("Ti", "0", InputType.DECIMAL, "Initial Time", "Simulation starting time");
        entry.setValueRange("0", null);
        entry.setEnabled(false);
        stochastic.add(entry);
        entry = new SimConfigEntry("Tf", "100", InputType.DECIMAL, "Final Time", "Simulation ending time");
        entry.setValueRange("0", null);
        stochastic.add(entry);
//		entry = new SimConfigEntry("TStep", "0.1", InputType.DECIMAL, "Time step", "Simulation stepping");
//		entry.setValueRange("0.000001", "100");
//		stochastic.add(entry);
//		entry = new SimConfigEntry("Replicates", new SimConfigSlider("3", "1", "10", 1),
//				InputType.SLIDER, "Replicates", "Number of stochastic replicates");
        entry = new SimConfigEntry("Replicates", "3", InputType.NATURAL, "#Replicates", "Number of run passes.");
        entry.setValueRange("1", "32");
        stochastic.add(entry);
        entry = new SimConfigEntry("CellSize", "Prokaryotic Cell", InputType.SELECT, "Cell Size", "Cell Size");
        entry.setOptionSet(CellSizes.getInstance().getCellSizeNames());
        stochastic.add(entry);
        entry = new SimConfigEntry("Method", SimStochasticMethodType.SSA.toString(), InputType.SELECT, "Stochastic Method",
                "Stochastic simulation algorithm.");
        entry.setOptionSet(SimStochasticMethodType.getSet());
        stochastic.add(entry);
    }

    public void checkAndPrepareToSimulate(Model model) throws Exception {
        int numOfSims = 0;
        if (simTypesToLaunch.contains(SimType.DYNAMIC_DETERMINISTIC)) {
            numOfSims++;
            for (SimConfigEntry entry : dynamic)
                entry.checkValue("Dynamic");
            if (Double.valueOf((String) dynamic.get("Ti").getValue()) >= Double
                    .valueOf((String) dynamic.get("Tf").getValue()))
                throw new Exception("Dynamic: Final time must be greater than Initial time");
        }
        if (simTypesToLaunch.contains(SimType.STEADY_STATE)) {
            numOfSims++;
            for (SimConfigEntry entry : steadyState)
                entry.checkValue("Steady State");

        }
        if (simTypesToLaunch.contains(SimType.DYNAMIC_STOCHASTIC)) {
            numOfSims++;
            for (SimConfigEntry entry : stochastic)
                entry.checkValue("Stochastic");
            if (Double.valueOf((String) stochastic.get("Ti").getValue()) >= Double
                    .valueOf((String) stochastic.get("Tf").getValue()))
                throw new Exception("Stochastic: Final time must be greater than Initial time");
        }
//        if (numOfSims == 0)
//            throw new Exception("No simulations are selected");
        ready(model);
    }

    public void ready(Model model) {
        dynamic_PlotViews.fix(model);
        dynamic_ParameterScan.cleanUnusedParams(model);
        if (!isStochasticMethodPreselected && model.getStochasticGradeType() == StochasticGradeType.TAU_LEAPING){
            this.getStochastic().get("Method").setValue("Tau-leaping");
            isStochasticMethodPreselected=true;
        }
    }

    //////////////////////////////////////////
    public ArrayList<String> getAllMathExpressions() {
        ArrayList<String> res = new ArrayList<>();
        if (simTypesToLaunch.contains(SimType.DYNAMIC_DETERMINISTIC)) {
            for (SimConfigEntry en : dynamic)
                if (en.getType() == InputType.MATHEXPRESSION)
                    res.add((String) en.getValue());
        }
        if (simTypesToLaunch.contains(SimType.STEADY_STATE)) {
            for (SimConfigEntry en : steadyState)
                if (en.getType() == InputType.MATHEXPRESSION)
                    res.add((String) en.getValue());
        }
        if (simTypesToLaunch.contains(SimType.DYNAMIC_STOCHASTIC)) {
            for (SimConfigEntry en : stochastic)
                if (en.getType() == InputType.MATHEXPRESSION)
                    res.add((String) en.getValue());
        }
        for (SimConfigEntry en : plotConfig)
            if (en.getType() == InputType.MATHEXPRESSION)
                res.add((String) en.getValue());
        return res;
    }

    public SimConfigArray getPlotSettings() {
        return plotConfig;
    }

    public SimConfigArray getDynamic() {
        return dynamic;
    }

    public SimPlotViewArray getDynamic_PlotViews() {
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

    public Set<SimType> getSimTypesToLaunch() {
        return simTypesToLaunch;
    }

    public void updateSimTypesToLaunch(Set<SimType> set) {
        simTypesToLaunch.clear();
        simTypesToLaunch.addAll(set);
    }
}
