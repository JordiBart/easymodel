package cat.udl.easymodel.logic.simconfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.utils.Utils;

public class SimPlotViewArray extends ArrayList<SimPlotView> {
    SimPlotViewArray() {
        super();
    }

    SimPlotViewArray(SimPlotViewArray from) {
        super();
        for (SimPlotView arr1 : from) {
            SimPlotView arr2 = new SimPlotView();
            for (String s : arr1)
                arr2.add(s);
            this.add(arr2);
        }
    }

    public void fix(Model m) {
        ArrayList<SimPlotView> viewsToDelete = new ArrayList<>();
        //remove repeated views
        for (int i = 0; i < this.size(); i++) {
            for (int j = i + 1; j < this.size(); j++) {
                if (Utils.compareArrayLists(this.get(i),this.get(j))) {
                    this.get(j).clear();
                }
            }
        }
        //remove blank views
        for (SimPlotView aPlotView : this) {
            if (aPlotView.isEmpty())
                viewsToDelete.add(aPlotView);
        }
        //remove views
        for (SimPlotView aPlotView : viewsToDelete) {
            this.remove(aPlotView);
        }
        if (this.isEmpty()) {
            SimPlotView arrDv = addNewPlotView();
            for (String dv : m.getAllSpeciesTimeDependent().keySet())
                arrDv.add(dv);
        }
    }

    public SimPlotView addNewPlotView() {
        SimPlotView arrDv = new SimPlotView();
        this.add(arrDv);
        return arrDv;
    }

    public void removeAtIndex(int i) {
        try {
            this.get(i).clear();
            this.remove(i);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void clearPlotViews() {
        for (SimPlotView aPlotConfig : this)
            aPlotConfig.clear();
        this.clear();
    }

    public void setEachDepVarToOneView(Model selectedModel) {
        clearPlotViews();
        for (String dv : selectedModel.getAllSpeciesTimeDependent().keySet()) {
            SimPlotView arrDv = new SimPlotView();
            arrDv.add(dv);
            this.add(arrDv);
        }
    }

    public TreeMap<String, Boolean> getUnifiedDepVars() {
        TreeMap<String, Boolean> res = new TreeMap<>();
        for (SimPlotView plotView : this) {
            for (String depVar : plotView) {
                res.put(depVar, null);
            }
        }
        return res;
    }
}
