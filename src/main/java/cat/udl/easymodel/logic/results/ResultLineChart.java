//package cat.udl.easymodel.logic.results;
//
//import cat.udl.easymodel.utils.p;
//import com.storedobject.chart.*;
//import com.vaadin.flow.component.Component;
//
//import java.util.ArrayList;
//
//public class ResultLineChart implements ResultEntry {
//    private String chartTitle, xTitle, yTitle;
//    private ArrayList<ArrayList<Double>> depVarVals = new ArrayList<>();
//    private ArrayList<Double> timeVals = new ArrayList<>();
//    private ArrayList<String> depVarNames = new ArrayList<>();
//
//    public ResultLineChart(String simList, String chartTitle, String xTitle, String yTitle) {
//        this.chartTitle = chartTitle;
//        this.xTitle = xTitle;
//        this.yTitle = yTitle;
//        simList=simList.replaceAll("\\s","");
//        String[] lines = simList.substring(2, simList.length() - 2).split("\\},\\{");
//        for (int i = 0; i < lines.length; i++) {
//            String line = lines[i];
//            String[] fields = line.split(",");
//            if (i == 0) {
//                for (int j = 0; j < fields.length; j++) {
//                    String field = fields[j];
//                    if (j > 0) {
//                        depVarVals.add(new ArrayList<>());
//                        depVarNames.add(field);
//                    }
//                }
//            } else {
//                for (int j = 0; j < fields.length; j++) {
//                    String field = fields[j];
//                    try {
//                        if (j == 0) {
//                            timeVals.add(Double.valueOf(field));
//                        } else {
//                            depVarVals.get(j - 1).add(Double.valueOf(field));
//                        }
//                    } catch (Exception e) {
//                        System.err.println("FIXME1-ResultLineChart " + field +"|"+ line);
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    public String getEntryType() {
//        return "LineChart";
//    }
//
//    @Override
//    public Component toComponent() {
//        SOChart soChart = new SOChart();
//        soChart.setSize("100%", "600px");
//
//        if (depVarVals.isEmpty())
//            return soChart;
//        LineChart[] lineCharts = new LineChart[depVarVals.size()];
//        Data[] xValues = new Data[lineCharts.length];
//        Data[] yValues = new Data[lineCharts.length];
//        int i;
//        for (i = 0; i < lineCharts.length; i++) {
//            xValues[i] = new Data();
//            xValues[i].setName("X (" + depVarNames.get(i) + ")");
//            yValues[i] = new Data();
//            yValues[i].setName("Y (" + depVarNames.get(i) + ")");
//        }
//        for (i = 0; i < depVarVals.size(); i++) {
//            for (int j = 0; j < depVarVals.get(i).size(); j++) {
//                xValues[i].add(timeVals.get(j));
//                yValues[i].add(depVarVals.get(i).get(j));
//            }
//        }
//        for (i = 0; i < lineCharts.length; i++) {
//            lineCharts[i] = new LineChart(xValues[i], yValues[i]);
//            lineCharts[i].setName(depVarNames.get(i));
//        }
//        XAxis xAxis = new XAxis(DataType.NUMBER);
//        xAxis.setName(xTitle);
//        YAxis yAxis = new YAxis(DataType.NUMBER);
//        yAxis.setName(yTitle);
//        RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
//        for (i = 0; i < lineCharts.length; i++) {
//            lineCharts[i].plotOn(rc);
//            soChart.add(lineCharts[i]);
//        }
//        Position tPosition = new Position();
//        tPosition.justifyCenter();
//        Title title = new Title(this.chartTitle);
//        title.setPosition(tPosition);
//
//        Position legendPosition = new Position();
//        legendPosition.alignBottom();
//        soChart.getDefaultLegend().setPosition(legendPosition);
//        Toolbox toolbox = new Toolbox();
//        Toolbox.Zoom zoom = new Toolbox.Zoom();
//        toolbox.addButton(new Toolbox.Download(),zoom);
//        soChart.add(title, toolbox);
//        soChart.getDefaultTooltip().setType(Tooltip.Type.Item);
//        return soChart;
//    }
//}
