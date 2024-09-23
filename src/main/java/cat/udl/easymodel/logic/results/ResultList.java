package cat.udl.easymodel.logic.results;

import java.util.ArrayList;

public class ResultList extends ArrayList<ResultEntry> {
    private boolean isDirty = false; // to mark that some dpecial vaadin component wants to update
    ResultStochasticStats resultStochasticStats = null;

    public ResultList() {
        super();
    }

    @Override
    public boolean add(ResultEntry resultEntry) {
        if (resultEntry instanceof ResultStochasticStats)
            this.resultStochasticStats = (ResultStochasticStats) resultEntry;
        return super.add(resultEntry);
    }

    public void updateComponents() {
        if (isDirty()) {
            if (resultStochasticStats != null) {
                resultStochasticStats.updateLayout();
            }
            setDirty(false);
        }
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public void updateStochasticProgressBar(Integer numReplicate, String newValue) {
        if (resultStochasticStats != null) {
            resultStochasticStats.updateStochasticProgressBarData(numReplicate, newValue);
            setDirty(true);
        }
    }

    public void updateStochasticStatistics(String[] vals) {
        if (resultStochasticStats != null) {
            resultStochasticStats.updateStochasticStatisticsData(vals);
            setDirty(true);
        }
    }
}
