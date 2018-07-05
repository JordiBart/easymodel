package cat.udl.easymodel.logic.simconfig;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public interface SimConfig {

	Map<String, Object> getDynamic();

	Map<String, Object> getSteadyState();

	Map<String, Object> getPlot();

	ArrayList<Map<String, Object>> getPlotViews();
	
	void reset();

	void checkSimConfigs() throws Exception;

	void addPlotView();

	void removePlotView(int arrIndex);

	void cleanPlotViews();

	void clearPlotViews();

	void initPlotViews(SortedMap<String, String> allSpeciesTimeDependent);

	TreeMap<String, Boolean> getUnifiedDepVarsFromPlotViews();

}