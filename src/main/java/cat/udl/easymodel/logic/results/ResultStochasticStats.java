package cat.udl.easymodel.logic.results;

import cat.udl.easymodel.vcomponent.common.GridLayout;
import cat.udl.easymodel.vcomponent.common.GridLayoutElement;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

import java.time.Duration;
import java.util.ArrayList;

public class ResultStochasticStats implements ResultEntry {
    //data
    private boolean isTauLeaping = false;
    private int numReplicates = 0;
    private long stStartMillis;
    private ArrayList<ArrayList<ResultStochasticStatsDataElement>> matrixData = new ArrayList<>();
    private boolean isMatrixDirty = true;
    private String stTimeSpanValue = "";
    private boolean stTimeSpanIsDirty = true;
    // vaadin component
    private VerticalLayout stVL = null;
    private GridLayout gridLayout = null;
    private Span stTimeSpan;

    public ResultStochasticStats(int numReplicates, boolean isTauLeaping) {
        this.isTauLeaping = isTauLeaping;
        this.numReplicates = numReplicates;
        if (!isTauLeaping) {
            int cols = 4, rows = numReplicates + 2;
            for (int i = 0; i < rows; i++) {
                matrixData.add(new ArrayList<>());
                if (i == 0) {
                    matrixData.get(i).add(new ResultStochasticStatsDataElement("spanBold", "Replicate"));
                    matrixData.get(i).add(new ResultStochasticStatsDataElement("spanBold", "Progress"));
                    matrixData.get(i).add(new ResultStochasticStatsDataElement("spanBold", "Execution time (s)"));
                    matrixData.get(i).add(new ResultStochasticStatsDataElement("spanBold", "Int. steps"));
                } else {
                    for (int j = 0; j < cols; j++) {
                        if (j == 0)
                            if (i == rows - 1)
                                matrixData.get(i).add(new ResultStochasticStatsDataElement("span", "*"));
                            else
                                matrixData.get(i).add(new ResultStochasticStatsDataElement("span", String.valueOf(i)));
                        else if (j == 1)
                            matrixData.get(i).add(new ResultStochasticStatsDataElement("progressBar", "0"));
                        else
                            matrixData.get(i).add(new ResultStochasticStatsDataElement("span", ""));
                    }
                }
            }
        } else {
            int cols = 6, rows = numReplicates + 2;
            for (int i = 0; i < rows; i++) {
                matrixData.add(new ArrayList<>());
                if (i == 0) {
                    matrixData.get(i).add(new ResultStochasticStatsDataElement("spanBold", "Replicate"));
                    matrixData.get(i).add(new ResultStochasticStatsDataElement("spanBold", "Progress"));
                    matrixData.get(i).add(new ResultStochasticStatsDataElement("spanBold", "Execution time (s)"));
                    matrixData.get(i).add(new ResultStochasticStatsDataElement("spanBold", "Int. steps"));
                    matrixData.get(i).add(new ResultStochasticStatsDataElement("spanBold", "#Leaps"));
                    matrixData.get(i).add(new ResultStochasticStatsDataElement("spanBold", "Leap time"));
                } else {
                    for (int j = 0; j < cols; j++) {
                        if (j == 0)
                            if (i == rows - 1)
                                matrixData.get(i).add(new ResultStochasticStatsDataElement("span", "*"));
                            else
                                matrixData.get(i).add(new ResultStochasticStatsDataElement("span", String.valueOf(i)));
                        else if (j == 1)
                            matrixData.get(i).add(new ResultStochasticStatsDataElement("progressBar", "0"));
                        else
                            matrixData.get(i).add(new ResultStochasticStatsDataElement("span", ""));
                    }
                }
            }
        }
        isMatrixDirty=true;
        dirtyMatrixData();
        stTimeSpanValue = "Time elapsed=00:00:00 ; Estimated time remaining=?";
        stTimeSpanIsDirty=true;
    }

    public boolean isDirty() {
        return isMatrixDirty || stTimeSpanIsDirty;
    }

    @Override
    public Component toComponent() {
        stVL = new VerticalLayout();
        stVL.setPadding(false);
        stVL.setSpacing(true);
        stStartMillis = System.currentTimeMillis();
        if (!isTauLeaping) {
            gridLayout = new GridLayout(4, numReplicates + 2);
            gridLayout.setWidthCol(0, "100px");
            gridLayout.setWidthCol(1, "300px");
            gridLayout.setWidthCol(2, "150px");
            gridLayout.setWidthCol(3, "100px");
        } else {
            gridLayout = new GridLayout(6, numReplicates + 2);
            gridLayout.setWidthCol(0, "100px");
            gridLayout.setWidthCol(1, "300px");
            gridLayout.setWidthCol(2, "150px");
            gridLayout.setWidthCol(3, "100px");
            gridLayout.setWidthCol(4, "100px");
            gridLayout.setWidthCol(5, "150px");
        }
        for (int row = 0; row < matrixData.size(); row++) {
            for (int col = 0; col < matrixData.get(row).size(); col++) {
                ResultStochasticStatsDataElement el = matrixData.get(row).get(col);
                GridLayoutElement gridLayoutElement = gridLayout.getElement(col, row);
                if (el.getType().equals("spanBold"))
                    ((Span) gridLayoutElement.getComponent()).getStyle().setFontWeight(600);
                else if (el.getType().equals("progressBar")) {
                    ProgressBar progressBar = new ProgressBar();
                    progressBar.setWidthFull();
                    gridLayout.setComponent(progressBar,col,row);
                }
            }
        }
        stTimeSpan = new Span("");
        stVL.add(gridLayout);
        stVL.add(stTimeSpan);
        isMatrixDirty=true;
        dirtyMatrixData();
        stTimeSpanIsDirty = true;
        updateLayout();
        return stVL;
    }

    private void dirtyMatrixData() {
        isMatrixDirty = true;
        for (int row = 0; row < matrixData.size(); row++) {
            for (int col = 0; col < matrixData.get(row).size(); col++) {
                ResultStochasticStatsDataElement el = matrixData.get(row).get(col);
                el.setDirty(true);
            }
        }
    }

    public void updateLayout() {
        for (int row = 0; row < matrixData.size(); row++) {
            for (int col = 0; col < matrixData.get(row).size(); col++) {
                ResultStochasticStatsDataElement el = matrixData.get(row).get(col);
                if (el.isDirty()) {
                    Component comp = gridLayout.getComponent(col, row);
                    if (comp instanceof ProgressBar)
                        ((ProgressBar) comp).setValue(Double.valueOf(el.getValue()));
                    else if (comp instanceof Span)
                        ((Span) comp).setText(el.getValue());
                    el.setDirty(false);
                }
            }
        }
        isMatrixDirty=false;
        if (stTimeSpanIsDirty) {
            stTimeSpan.setText(stTimeSpanValue);
            stTimeSpanIsDirty = false;
        }
    }

    public void updateStochasticProgressBarData(Integer numReplicates, String newValue) {
        matrixData.get(numReplicates).get(1).setValue(newValue);
        Double totalProgress = 0d;
        for (int row = 1; row < matrixData.size() - 1; row++)
            totalProgress += Double.valueOf(matrixData.get(row).get(1).getValue());
        totalProgress /= matrixData.size() - 2;
        matrixData.get(matrixData.size() - 1).get(1).setValue(String.valueOf(totalProgress));
        isMatrixDirty=true;
        long millisElapsed = System.currentTimeMillis() - stStartMillis;
//				Duration duration = DurationFormatUtils.formatDuration(Float.valueOf(((1-totalProgress)/totalProgress)).longValue()*millisElapsed, "**H:mm:ss**", true);
        long secondsElapsed = Duration.ofMillis(millisElapsed).getSeconds();
        long etrSeconds = Duration.ofMillis(Math.round(((1 - totalProgress) / totalProgress) * millisElapsed)).getSeconds();
        String etrHMS = String.format("%02d:%02d:%02d", etrSeconds / 3600, (etrSeconds % 3600) / 60,
                etrSeconds % 60);
        String elapsedHMS = String.format("%02d:%02d:%02d", secondsElapsed / 3600, (secondsElapsed % 3600) / 60,
                secondsElapsed % 60);
        stTimeSpanValue = "Time elapsed=" + elapsedHMS + " ; Estimated time remaining=" + etrHMS;
        stTimeSpanIsDirty = true;
    }

    public void updateStochasticStatisticsData(String[] vals) {
        int row;
        if (vals[0].equals("*")) {
            row = matrixData.size() - 1;
            matrixData.get(matrixData.size() - 1).get(1).setValue("1");
            long millisElapsed = System.currentTimeMillis() - stStartMillis;
            long secondsElapsed = Duration.ofMillis(millisElapsed).getSeconds();
            String elapsedHMS = String.format("%02d:%02d:%02d", secondsElapsed / 3600, (secondsElapsed % 3600) / 60,
                    secondsElapsed % 60);
            stTimeSpanValue = "Time elapsed=" + elapsedHMS + " ; Estimated time remaining=00:00:00";
            stTimeSpanIsDirty = true;
        } else {
            row = Integer.valueOf(vals[0]);
            matrixData.get(row).get(1).setValue("1");
        }
        for (int i = 1; i < vals.length; i++) {
            matrixData.get(row).get(i + 1).setValue(vals[i]);
        }
        isMatrixDirty=true;
    }
}
