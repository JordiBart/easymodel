package cat.udl.easymodel.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import cat.udl.easymodel.logic.formula.FormulaValue;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.results.*;
import cat.udl.easymodel.logic.simconfig.*;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.SimType;
import cat.udl.easymodel.logic.types.StochasticGradeType;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathPacketListenerOp;
import cat.udl.easymodel.mathlink.MathLinkOp;
import cat.udl.easymodel.mathlink.SimJob;
import cat.udl.easymodel.sbml.SBMLMan;
import cat.udl.easymodel.utils.*;
import cat.udl.easymodel.utils.buffer.NotebookMathBuffer;
import cat.udl.easymodel.utils.buffer.MathBuffer;
import jdk.jshell.execution.Util;

public class SimulationCtrl {
    private String genContext = ContextUtils.generalContext;
    private String modelContext = ContextUtils.modelContext;
    private String indexContext = ContextUtils.indexContext;
    private String gainContext = ContextUtils.gainContext;
    private String sensContext = ContextUtils.sensitivityContext;
    private final String imageExtension = ".gif";
    private final String imageWidthForResults = "660px";
    private final Integer plotXPoints = 2000;

    private String timeVar = genContext + "t";
    private String normalSize;

    private SharedData sharedData = SharedData.getInstance();
    private ResultList results = null;
    private MathLinkOp mathLink = null;
    private Model m = null;
    private SimConfig simConfig;

    private MathBuffer mathBuffer = new MathBuffer();
    private NotebookMathBuffer notebookBuffer = new NotebookMathBuffer();
    private String mathCommand, mathCommand2, mathCommand3;
    private String sbmlDataString;
    private long startNanoTime;
    private SimJob simulationJob;
    private boolean useMathLink;
    private final String stochasticNotCompatibleErrorMessage = "This model, with the current specification, is not compatible with stochastic simulation.";

    public SimulationCtrl(SimJob simulationJob) {
        this.simulationJob = simulationJob;
        this.results = simulationJob.getResultList();
        this.m = simulationJob.getModel();
        this.simConfig = this.m.getSimConfig();
        this.mathLink = simulationJob.getMathLinkOp();
        useMathLink = true;
    }

    public SimulationCtrl(Model model) {
        this.m = model;
        this.simConfig = this.m.getSimConfig();
        useMathLink = false;
    }

    public NotebookMathBuffer getNotebookBuffer() {
        return notebookBuffer;
    }

    private String getChopTolerance() {
        return "";
    }

    private void executeMathBuffer() throws Exception {
//		p.p(mathBuffer.getString());
        if (this.useMathLink) {
            mathLink.evaluate(mathBuffer.getString());
        }
        mathBuffer.reset();
    }

    private void bufferCommandButWithDifferenExportCmd(String mathCommand, String exportComm) {
        notebookBuffer.addCommand(exportComm);
        mathBuffer.addCommand(mathCommand);
    }

    private void bufferCommand(String mathCommand) {
        notebookBuffer.addCommand(mathCommand);
        mathBuffer.addCommand(mathCommand);
    }

    private void bufferMathTxt(String filename) throws CException {
        String txtContent = sharedData.getMathematicaCodeMap().get(filename);
        if (txtContent == null)
            throw new CException("Missing Mathematica code");
        notebookBuffer.addCommand(txtContent);
        mathBuffer.addCommandRaw(txtContent);
    }

    private String mathGetString(String mathCommand) throws Exception {
        String mOut = "Null";
        if (useMathLink)
            mOut = mathLink.evaluateToString(mathCommand);
        notebookBuffer.addCommand(mathCommand);
        return mOut;
    }

    private String mathGetStringNoMathBuffer(String mathCommand) throws Exception {
        String mOut = "Null";
        if (useMathLink)
            mOut = mathLink.evaluateToString(mathCommand);
        return mOut;
    }

    private void showTxtFileDownloadButton(String mathCommand, String caption, String filename) throws Exception {
        String resMath = mathGetString(mathCommand);
        StringBuilder fileStringBuilder = new StringBuilder();
        boolean isFirstLine = true;
        for (String line : resMath.split("\n")) {
            if (!line.equals("")) {
                if (!isFirstLine)
                    fileStringBuilder.append(line.replaceAll("\\.\\s", "  ") + sharedData.getNewLine());
                else
                    fileStringBuilder.append(line + sharedData.getNewLine());
                isFirstLine = false;
            }
        }
        resMath = null;
        resultsAdd(new ResultDownloadButton(fileStringBuilder.toString(), caption, filename));
    }

    private void resultsAdd(ResultEntry entry) {
        if (results != null && entry != null)
            results.add(entry);
    }

    private String getFinishedMathList(String mathCommand) {
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        return mathCommand;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getDepVarsToShow(Map<String, Object> map) {
        Map<String, String> res = new HashMap<>();
        String plotList = "{";
        for (String dv : (ArrayList<String>) map.get("DepVarsToShow"))
            plotList += dv + "[" + timeVar + "],";
        if (plotList.charAt(plotList.length() - 1) == ',')
            plotList = plotList.substring(0, plotList.lastIndexOf(","));
        plotList += "}";
        res.put("plotList", plotList);
        String plotLegends = plotList.replace("[t]", "");
        res.put("plotLegends", plotLegends);
        return res;
    }

    private void executePlot(String plotList, String plotLegends, String ndSolveVar, String xLabel, String yLabel,
                             String fileName) throws Exception {
        String cmd = "Plot[Evaluate[" + plotList + " /. " + ndSolveVar + "], {" + timeVar + "," + genContext + "ti,"
                + genContext + "tf}, LabelStyle -> {" + /* "FontFamily -> Arial," + */ "FontWeight -> "
                + ((Boolean) simConfig.getPlotSettings().get("FontWeight").getBooleanValue() ? "Bold" : "Plain")
                + ", FontSlant -> "
                + ((Boolean) simConfig.getPlotSettings().get("FontSlant").getBooleanValue() ? "Italic" : "Plain")
                + ", FontSize -> " + simConfig.getPlotSettings().get("FontSize").getValue() + "}, PlotLegends->"
                + plotLegends
                + ", Axes -> False, Frame -> True, FrameTicks -> True, PlotStyle -> Table[{Dashing[0.03 k1/Length["
                + plotList + "]], Thickness[" + simConfig.getPlotSettings().get("LineThickness").getValue()
                + "]}, {k1, 1, Length[" + plotList + "]}], FrameLabel -> {\"" + xLabel + "\", \"" + yLabel
                + "\"}, PlotRange->Full, ImageSize->" + normalSize + "]";
        getNotebookBuffer().addCommand(cmd);
        showImageOfMathCmd(cmd, fileName, imageWidthForResults);
    }

    private void showImageOfMathCmd(String cmd, String fileName, String width) throws Exception {
        cmd = "Rasterize[" + cmd + ",RasterSize->2000,ImageResolution->72]";
        if (useMathLink) {
            byte[] mImage = mathLink.evaluateToImage(cmd);
            ResultDynamicImage resultDynamicImage = new ResultDynamicImage(fileName, mImage, fileName, width);
            resultsAdd(resultDynamicImage);
        }
    }

    private void executeMathTable(String tableName, String columnList, String rowList, boolean isBindTableToDepVars)
            throws Exception {
        if (columnList != null)
            mathCommand = "columnList = {Join[{\" \"},Map[ToString," + columnList + "]]}";
        else
            mathCommand = "columnList = {}";
        bufferCommand(mathCommand);
        if (rowList != null)
            mathCommand = "rowList = Map[ToString," + rowList + "]";
        else
            mathCommand = "rowList = {}";
        bufferCommand(mathCommand);
        if (!isBindTableToDepVars)
            mathCommand = "tab = Join[columnList, Table[Join[If[rowList != {}, List[rowList[[j]]],{}]," + tableName
                    + "[[j]]], {j, 1, Length[rowList]}]]";
        else
            mathCommand = "tab = Join[columnList, Table[Join[If[rowList != {}, List[rowList[[j]]],{}]," + tableName
                    + "[[(Position[DepVarsString,rowList[[j]]][[1]][[1]])]]], {j, 1, Length[rowList]}]]";
        bufferCommand(mathCommand);
        mathCommand = "gridColor = Flatten[Table[{i, j} -> If[i == If[columnList != {}, 1,0] || j == If[rowList != {}, 1,0], RGBColor[0.86, 0.86, 0.86],If[(Chop["
                + "tab[[i, j]]]) < 0., RGBColor[1.0, 0.43, 0.43],If[("
                + "tab[[i, j]]) > 0., RGBColor[0.62, 1.0, 0.43], RGBColor[0.97, 0.97, 0.97]]]], {i, 1, Dimensions["
                + "tab][[1]]}, {j, 1, Dimensions[" + "tab][[2]]}]]";
        bufferCommand(mathCommand);
        executeMathBuffer();

        mathCommand = "Grid[" + "tab, Background -> {None, None, "
                + "gridColor},ItemStyle->Directive[FontSize->16],Frame->All,ItemSize->9]";
        getNotebookBuffer().addCommand(mathCommand);
        showImageOfMathCmd(mathCommand, getGeneralDownloadFilename(tableName, imageExtension), imageWidthForResults);
    }

    private void bufferBeginContext() {
        // context = "S" + VaadinSession.getCurrent().getSession().getId();
//		context = "R" + ThreadLocalRandom.current().nextInt(10000, 99999) + "`";
        bufferCommand("Begin[\"" + genContext + "\"]");
    }

    private void bufferEndContext() {
        bufferCommand("End[]");
        bufferCommand("ClearAll[\"" + genContext + "*\"]");
        // addCommandToMathBuffer("ClearAll[\"Global`*\"]");
    }

    private void executeInitMathCommands() throws Exception {
    }

    private void executeEndMathCommands() throws Exception {
        executeMathBuffer();
    }

    private void showGeneratedFiles() throws Exception {
        resultsAdd(new ResultText("Generated Files", "textH2"));
        ResultHL resultHL = new ResultHL();
        resultHL.add(new ResultDownloadButton(notebookBuffer.getString(), "Mathematica Notebook", getGeneralDownloadFilename(null, ".nb")));
        if (useMathLink) {
            try {
                SBMLMan sbmlMan = new SBMLMan();
                sbmlDataString = sbmlMan.exportSBML(m, mathLink);
                resultHL.add(new ResultDownloadButton(sbmlDataString, "SBML Model", getGeneralDownloadFilename(null, ".xml")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        resultsAdd(resultHL);
    }

    /////////
    private void fillSteadyStateSimulationMap(String mathListName, TreeMap<String, String> ssMap) throws Exception {
        mathCommand = mathListName + " // TableForm";
        String fileString = mathGetString(mathCommand);
        fileString = fileString.replace("\n", sharedData.getNewLine());
        boolean first = true;
        String eValue = "";
        String sp = "";
        for (String line : fileString.split(sharedData.getNewLine())) {
            if (!line.equals("")) {
                if (!line.matches(".*\\[" + timeVar + "\\]\\s*\\->.*")) {
                    eValue = line;
                    eValue = eValue.replace(" ", "");
                } else {
                    first = true;
                    for (String word : line.split("\\[" + timeVar + "\\] -> ")) {
                        if (first) { // species
                            sp = ContextUtils.removeContext(word);
                            first = false;
                        } else { // concentration value
                            if (!eValue.equals("")) {
                                word = word.replace(" 10", "E" + eValue);
                                eValue = "";
                            }
                            // word = concentration value
                            ssMap.put(sp, word);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void showSteadyStateSimulationTableAndButton(TreeMap<String, String> ssMap) {
        String header1 = "Variable";
        header1 += "     ";
        String header2 = "Steady State Value";
        // find variable with the longest name length
        int maxVarLength = 0;
        for (String depVar : ssMap.keySet())
            if (depVar.length() > maxVarLength)
                maxVarLength = depVar.length();
        // add missing spaces to header 1
        int spacesToAdd = maxVarLength - header1.length() + 1;
        for (int i = 0; i < spacesToAdd; i++)
            header1 += " ";
        // replace "[t] -> " for the required number of spaces
        // get number of real lines

        // create text file
        String file2Download = header1 + header2 + sharedData.getNewLine();
        String spaces;
        String ssValue = "";
        for (String depVar : ssMap.keySet()) {
            ssValue = ssMap.get(depVar) != null ? ssMap.get(depVar) : "-";
            // create spaces
            spaces = "";
            for (int is = 0; is < header1.length() - depVar.length(); is++)
                spaces += " ";
            file2Download += depVar + spaces;
            // concentration value
            file2Download += ssValue + sharedData.getNewLine();
        }

        // create visual table
        HtmlTableBuilder tableBuilder = new HtmlTableBuilder("matrix");
        tableBuilder.addCell(header1, 1);
        tableBuilder.addCell(header2, 1);
        tableBuilder.setCols();
        for (String depVar : ssMap.keySet()) {
            ssValue = ssMap.get(depVar) != null ? ssMap.get(depVar) : "-";
            tableBuilder.addCell(depVar, 1);
            tableBuilder.addCell(ssValue, 0);
        }
        tableBuilder.finish();
        resultsAdd(new ResultHtml(tableBuilder.getHtmlCode()));
        resultsAdd(new ResultDownloadButton(file2Download, "Download Steady State Simulation",
                getGeneralDownloadFilename("steadystate", ".txt")));
    }

    public void checkCancel() throws Exception {
        if (useMathLink && simulationJob.isCancelJob()) {
            throw new Exception("Cancelled");
        }
    }

    ///////////
    public void simulate() throws Exception {
//		m = new Model(sessionData.getSelectedModel(), 0);
        checkCancel();
        if (sharedData.isDebug())
            startNanoTime = System.nanoTime();
        executeInitMathCommands();
        checkCancel();
        if (useMathLink)
            m.checkMathExpressions(mathLink);
        checkCancel();
        initSimulation();
        checkCancel();
        checkStochasticGrade();
        checkCancel();
        if (simConfig.getSimTypesToLaunch().contains(SimType.DYNAMIC_DETERMINISTIC) || simConfig.getSimTypesToLaunch().contains(SimType.STEADY_STATE)) {
            initDeterministicSimulation();
            checkCancel();
        }
        if (simConfig.getSimTypesToLaunch().contains(SimType.DYNAMIC_DETERMINISTIC)) {
            dynamicSimulation();
            checkCancel();
        }
        if (simConfig.getSimTypesToLaunch().contains(SimType.STEADY_STATE)) {
            steadyStateSimulation();
            checkCancel();
        }
        if (simConfig.getSimTypesToLaunch().contains(SimType.DYNAMIC_STOCHASTIC)) {
            stochasticSimulation();
            checkCancel();
        }
        executeEndMathCommands();
        checkCancel();
        notebookBuffer.end();
        mathBuffer.reset();
        showGeneratedFiles();
        resultsAdd(new ResultImage("img/mathematica-white-plain-border.png", "Mathematica", "https://www.wolfram.com/mathematica/"));
        checkCancel();
        if (sharedData.isDebug())
            P.d("Sim took " + ((double) ((System.nanoTime() - startNanoTime) / 1000000000d)) + "s");
    }

    public void checkStochasticGrade() throws Exception {
        Model modelToUpdate = m.getParent() != null ? m.getParent() : m;
        if (modelToUpdate.getStochasticGradeType() != StochasticGradeType.UNCHECKED || modelToUpdate.getId() == null || !useMathLink)
            return;
        long startStochasticGrade = System.nanoTime();
        bufferCommand("cellSize = " +
                CellSizes.getInstance().nameToNum(simConfig.getStochastic().get("CellSize").getOriginalValue()));
        bufferMathTxt("quickStochasticGradeCheck.txt");
        executeMathBuffer();
        checkCancel();
        if (sharedData.isDebug())
            P.d("Quick Stochastic Grade Check took " + ((double) ((System.nanoTime() - startStochasticGrade) / 1000000000d)) + "s");
        if (mathGetString("isSSAPassOK").equals("False"))
            modelToUpdate.setStochasticGradeType(StochasticGradeType.NOT_COMPATIBLE);
        else {
            if (mathGetString("isTauLeapingEffective").equals("True"))
                modelToUpdate.setStochasticGradeType(StochasticGradeType.TAU_LEAPING);
            else
                modelToUpdate.setStochasticGradeType(StochasticGradeType.SSA);
        }
        P.d("quickStochasticGradeCheck: " + modelToUpdate.getStochasticGradeType().toString());
        modelToUpdate.saveStochasticGradeDB();
    }

    private void initDeterministicSimulation() {
        normalSize = (String) simConfig.getPlotSettings().get("ImageSize").getValue().toString();
        mathCommand = genContext + "EqsToSolve = Join[Table[((" + genContext + "DepVars[[i]]))'[" + timeVar + "] == "
                + genContext + "RateEqsAllSubs[[i]], {i, 1, Length[" + genContext + "DepVars]}], " + genContext
                + "InitCondDepVars]";
        bufferCommand(mathCommand);
        if (((Boolean) simConfig.getDynamic().get("Sensitivities").getBooleanValue())
                || ((Boolean) simConfig.getDynamic().get("Gains").getBooleanValue())
                || simConfig.getSimTypesToLaunch().contains(SimType.STEADY_STATE)) {
            mathCommand = genContext + "Jac = Outer[D, " + genContext + "RateEqsWithT, " + genContext + "DepVarsWithT]";
            bufferCommand(mathCommand);
        }
    }

    private void initSimulation() throws CException {
//		bufferBeginContext();
        notebookBuffer.addCommand("(*Model: " + m.getName() + " (Generated with " + SharedData.fullAppName + ")*)");
        // DECLARE DEPVARS
        mathCommand = genContext + "DepVars={";
        for (String sp : m.getAllSpeciesTimeDependent().keySet())
            mathCommand += modelContext + sp + ",";
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);

        mathCommand = genContext + "DepVarsString={";
        for (String sp : m.getAllSpeciesTimeDependent().keySet())
            mathCommand += "ToString[" + sp + "],";
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);

        // MAKE DEPVARS DEPEND OF T
        mathCommand = genContext + "SubsDepVarsWithT={";
        for (String sp : m.getAllSpeciesTimeDependent().keySet()) {
            mathCommand += modelContext + sp + "->" + modelContext + sp + "[" + timeVar + "],";
        }
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);
        mathCommand = genContext + "DepVarsWithT=" + genContext + "DepVars /. " + genContext + "SubsDepVarsWithT";
        bufferCommand(mathCommand);
        mathCommand = genContext + "DepVarsWithTDiff=Table[" + genContext + "DepVars[[i]]'[" + timeVar
                + "],{i,1,Length[" + genContext + "DepVars]}]";
        bufferCommand(mathCommand);
        // CONCENTRATIONS
        mathCommand = genContext + "InitCondDepVars={";
        for (String sp : m.getAllSpeciesTimeDependent().keySet())
            mathCommand += modelContext + sp + "[0]==" + m.getAllSpecies().get(sp).getConcentration() + ",";
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);
        // STOICHIOMETRIC MATRIX
        try {
            mathCommand = genContext + "SM=" + m.getStoichiometricMatrix();
        } catch (Exception e) {
            resultsAdd(new ResultText("Error creating Stoichiometric Matrix" + e.getMessage(), null));
        }
        // mathCommand += ";\n";
        bufferCommand(mathCommand);
        bufferCommand("numDepVars = Dimensions[SM][[1]]");
        bufferCommand("numReactions = Dimensions[SM][[2]]");
        // DECLARE FORMULAS
        mathCommand = genContext + "Rates={";
        for (Reaction r : m) {
            mathCommand += "Hold[" + r.getFormula().getMathematicaReadyFormula(r.getMathematicaContext(), m) + "],";
        }
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);
        // DECLARE FORMULAS built-in vars (X,M...)
        mathCommand = genContext + "SubsFormVars={";
        for (Reaction r : m) {
            if (r.getLeftPartSpecies().size() != 0) {
                mathCommand += r.getMathematicaContext() + ContextUtils.builtInContext + "X->{";
                for (String sp : r.getLeftPartSpecies().keySet())
                    mathCommand += modelContext + sp + ",";
                if (mathCommand.charAt(mathCommand.length() - 1) == ',')
                    mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
                mathCommand += "},";
                mathCommand += r.getMathematicaContext() + ContextUtils.builtInContext + "A->{";
                for (String sp : r.getLeftPartSpecies().keySet())
                    mathCommand += r.getLeftPartSpecies().get(sp) + ",";
                if (mathCommand.charAt(mathCommand.length() - 1) == ',')
                    mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
                mathCommand += "},";
                mathCommand += r.getMathematicaContext() + ContextUtils.builtInContext + "XF->" + modelContext
                        + r.getLeftPartSpecies().firstKey() + ",";
            }
            if (r.getModifiers().size() != 0) {
                mathCommand += r.getMathematicaContext() + ContextUtils.builtInContext + "M->{";
                for (String sp : r.getModifiers().keySet())
                    mathCommand += modelContext + sp + ",";
                if (mathCommand.charAt(mathCommand.length() - 1) == ',')
                    mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
                mathCommand += "},";
                mathCommand += r.getMathematicaContext() + ContextUtils.builtInContext + "MF->" + modelContext
                        + r.getModifiers().firstKey() + ",";
            }
        }
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);

        // DECLARE FORMULAS PARAMETERS VALUES WHICH ARE SPECIES (a...)
        mathCommand = genContext + "ParVarVals={";
        for (Reaction r : m) {
            Map<String, FormulaValue> valuesByReaction = r.getFormulaGeneralParameters();
            for (String key : valuesByReaction.keySet()) {
                if (valuesByReaction.get(key).getType() != FormulaValueType.CONSTANT)
                    mathCommand += r.getMathematicaContext() + key + "->" + modelContext
                            + valuesByReaction.get(key).getStringValue() + ",";
            }
        }
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);

        // DECLARE FORMULAS PARAMETERS WHICH DEFINE NUMBERS (a, "g array",...)
        mathCommand = genContext + "Pars={";
        for (Reaction r : m) {
            // CONSTANT VALUES BY REACTIONS (ig a)
            Map<String, FormulaValue> valuesByReaction = r.getFormulaGeneralParameters();
            for (String key : valuesByReaction.keySet()) {
                if (valuesByReaction.get(key).getType() == FormulaValueType.CONSTANT)
                    mathCommand += r.getMathematicaContext() + key + ",";
            }
            // substrates and modifiers constants (ig "g array")
            for (String co : r.getFormulaSubstratesArrayParameters().keySet()) {
                // loop could be over getFormulaModifiersArrayParameters() too
                for (String sp : r.getFormulaSubstratesArrayParameters().get(co).keySet()) {
                    mathCommand += r.getMathematicaContext() + ContextUtils.arrayContext + co + "`"
                            + ContextUtils.substrateContext + sp + ",";
                }
                for (String sp : r.getFormulaModifiersArrayParameters().get(co).keySet()) {
                    mathCommand += r.getMathematicaContext() + ContextUtils.arrayContext + co + "`"
                            + ContextUtils.modifierContext + sp + ",";
                }
            }
        }
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);

        bufferParsString();

        // DECLARE FORMULAS PARAMETERS ARRAYS WITH VARS ("g array",...)
        mathCommand = genContext + "ParArrayInVars={";
        for (Reaction r : m) {
            for (String co : r.getFormulaSubstratesArrayParameters().keySet()) {
                // loop could be over getFormulaModifiersArrayParameters() too
                mathCommand += r.getMathematicaContext() + co + "->{";
                for (String sp : r.getFormulaSubstratesArrayParameters().get(co).keySet()) {
                    mathCommand += r.getMathematicaContext() + ContextUtils.arrayContext + co + "`"
                            + ContextUtils.substrateContext + sp + ",";
                }
                for (String sp : r.getFormulaModifiersArrayParameters().get(co).keySet()) {
                    mathCommand += r.getMathematicaContext() + ContextUtils.arrayContext + co + "`"
                            + ContextUtils.modifierContext + sp + ",";
                }
                if (mathCommand.charAt(mathCommand.length() - 1) == ',')
                    mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
                mathCommand += "},";
            }
        }
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);

        // DECLARE FORMULAS PARAMETERS VALUES WHICH ARE NUMBERS (a, "g array",...)
        mathCommand = genContext + "ParNumVals={";
        for (Reaction r : m) {
            // CONSTANT VALUES BY REACTIONS (ig a)
            Map<String, FormulaValue> valuesByReaction = r.getFormulaGeneralParameters();
            for (String key : valuesByReaction.keySet()) {
                if (valuesByReaction.get(key).getType() == FormulaValueType.CONSTANT)
                    mathCommand += r.getMathematicaContext() + key + "->" + valuesByReaction.get(key).getStringValue()
                            + ",";
            }
            // species and modifiers constants (ig "g array vars")
            for (String co : r.getFormulaSubstratesArrayParameters().keySet()) {
                // loop could be over getFormulaModifiersArrayParameters() too
                for (String sp : r.getFormulaSubstratesArrayParameters().get(co).keySet()) {
                    mathCommand += r.getMathematicaContext() + ContextUtils.arrayContext + co + "`"
                            + ContextUtils.substrateContext + sp + "->"
                            + r.getFormulaSubstratesArrayParameters().get(co).get(sp).getValue() + ",";
                }
                for (String sp : r.getFormulaModifiersArrayParameters().get(co).keySet()) {
                    mathCommand += r.getMathematicaContext() + ContextUtils.arrayContext + co + "`"
                            + ContextUtils.modifierContext + sp + "->"
                            + r.getFormulaModifiersArrayParameters().get(co).get(sp).getValue() + ",";
                }
            }
        }
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);

        mathCommand = genContext + "IndVars={";
        for (String sp : m.getAllSpeciesConstant().keySet())
            mathCommand += modelContext + sp + ",";
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);

        mathCommand = genContext + "IndVarsString={";
        for (String sp : m.getAllSpeciesConstant().keySet())
            mathCommand += "ToString[" + sp + "],";
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);

        mathCommand = genContext + "IndVarVals={";
        for (String sp : m.getAllSpeciesConstant().keySet())
            mathCommand += modelContext + sp + "->" + m.getAllSpecies().get(sp).getConcentration() + ",";
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);

        mathCommand = genContext + "RateEqs = " + genContext + "SM.(" + genContext + "Rates/. Join[" + genContext
                + "SubsFormVars," + genContext + "ParVarVals," + genContext + "ParArrayInVars])";
        bufferCommand(mathCommand);
        mathCommand = genContext + "RateEqs = ReleaseHold[" + genContext + "RateEqs]";
        bufferCommand(mathCommand);
        mathCommand = genContext + "RateEqsWithT = " + genContext + "RateEqs/." + genContext + "SubsDepVarsWithT";
        bufferCommand(mathCommand);
        mathCommand = genContext + "RateEqsAllSubs = " + genContext + "RateEqsWithT /." + genContext + "ParNumVals/."
                + genContext + "IndVarVals";
        bufferCommand(mathCommand);
        mathCommand = "ToStringNumber[x0_] := Module[{num = N@x0},\r\n" + "	If[num > 10^6 || num < 10^-6,\r\n"
                + "		ToString@ScientificForm[num, 10, NumberFormat->(Row[{#1, \"e\", #3}] &)]\r\n" + "	,\r\n"
                + "		ToString@DecimalForm[num]\r\n" + "	]\r\n" + "]";
        bufferCommand(mathCommand);
    }

    private void bufferParsString() {
        mathCommand = genContext + "ParsString={";
        for (Reaction r : m) {
            // CONSTANT VALUES BY REACTIONS (ig a)
            Map<String, FormulaValue> valuesByReaction = r.getFormulaGeneralParameters();
            for (String key : valuesByReaction.keySet()) {
                if (valuesByReaction.get(key).getType() == FormulaValueType.CONSTANT)
                    mathCommand += "ToString[" + r.getIdJavaStr() + key + "],";
            }
            // substrates and modifiers constants (ig "g array")
            for (String co : r.getFormulaSubstratesArrayParameters().keySet()) {
                // loop could be over getFormulaModifiersArrayParameters() too
                int i = 1;
                for (String sp : r.getFormulaSubstratesArrayParameters().get(co).keySet()) {
                    mathCommand += "ToString[" + r.getIdJavaStr() + co + sp + "],";
                    i++;
                }
                for (String sp : r.getFormulaModifiersArrayParameters().get(co).keySet()) {
                    mathCommand += "ToString[" + r.getIdJavaStr() + co + sp + "],";
                    i++;
                }
            }
        }
        if (mathCommand.charAt(mathCommand.length() - 1) == ',')
            mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
        mathCommand += "}";
        bufferCommand(mathCommand);
    }

    private String getGeneralDownloadFilename(String category, String ext) {
        //ext example: ".txt"
        if (category == null || category.isBlank())
            category = "";
        else
            category = "-" + category;
        if (ext == null)
            ext = "";
        return Utils.curateForURLFilename(m.getName() + category + ext);
    }

    private String getTxtTable(String simTable) {
        StringBuilder fileContent = new StringBuilder();
        for (String line : simTable.split("[\\n]+"))
            fileContent.append(line.replaceAll("\\.\\s", "  ").replaceAll("\\.\\n", " \n") + "\r\n");
        return fileContent.toString();
    }

    private void dynamicSimulation() throws Exception {
        // model must have been checked before calling this function
        resultsAdd(new ResultText("Dynamic Simulation", "textH2"));
        mathCommand = "ti = " + (String) simConfig.getDynamic().get("Ti").getValue();
        bufferCommand(mathCommand);
        mathCommand = "tf = " + (String) simConfig.getDynamic().get("Tf").getValue();
        bufferCommand(mathCommand);
        mathCommand = "tStep = " + (String) simConfig.getDynamic().get("TStep").getValue();
        bufferCommand(mathCommand);
        if (!((Boolean) simConfig.getDynamic().get("Sensitivities").getBooleanValue())
                && !((Boolean) simConfig.getDynamic().get("Gains").getBooleanValue())) {
            bufferCommand("$t0 = AbsoluteTime[]");
            bufferCommand(genContext + "Sols = First@NDSolve[" + genContext + "EqsToSolve, " + genContext + "DepVars, {"
                    + timeVar + ", " + genContext + "ti, " + genContext + "tf}]");
            bufferCommand("Print[\"Execution time=\",AbsoluteTime[]-$t0,\"s\"]");
            executeMathBuffer();
            plotDynamicSim(genContext + "Sols");
        }
        // DYNAMIC GAINS
        if ((Boolean) simConfig.getDynamic().get("Gains").getBooleanValue()) {
            mathCommand = "DynGainVars = {";
            mathCommand2 = "DynGainVarsDiff = {";
            for (String dep : m.getAllSpeciesTimeDependent().keySet()) {
                mathCommand += "{";
                mathCommand2 += "{";
                for (String ind : m.getAllSpeciesConstant().keySet()) {
                    mathCommand += gainContext + dep + ind + "[" + timeVar + "],";
                    mathCommand2 += gainContext + dep + ind + "'[" + timeVar + "],";
                }
                mathCommand = removeLastComma(mathCommand);
                mathCommand2 = removeLastComma(mathCommand2);
                mathCommand += "},";
                mathCommand2 += "},";
            }
            mathCommand = removeLastComma(mathCommand);
            mathCommand2 = removeLastComma(mathCommand2);
            mathCommand += "}";
            mathCommand2 += "}";
            bufferCommand(mathCommand);
            bufferCommand(mathCommand2);

            mathCommand = "DynGainVarsDiff = {";
            for (String dep : m.getAllSpeciesTimeDependent().keySet()) {
                mathCommand += "{";
                for (String ind : m.getAllSpeciesConstant().keySet())
                    mathCommand += gainContext + dep + ind + "'[" + timeVar + "],";
                if (mathCommand.charAt(mathCommand.length() - 1) == ',')
                    mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
                mathCommand += "},";
            }
            if (mathCommand.charAt(mathCommand.length() - 1) == ',')
                mathCommand = mathCommand.substring(0, mathCommand.length() - 1);
            mathCommand += "}";
            bufferCommand(mathCommand);
            mathCommand = "JacIV = Outer[D, " + "RateEqsWithT, " + "IndVars]";
            bufferCommand(mathCommand);
            mathCommand = "EqGains = Flatten[" + "Jac." + "DynGainVars+" + "JacIV]";
            bufferCommand(mathCommand);
            mathCommand = "DerJ1 = Join[" + "DepVarsWithTDiff, Flatten[" + "DynGainVarsDiff]]";
            bufferCommand(mathCommand);
            mathCommand = "JEqs = Join[" + "RateEqsWithT, " + "EqGains]";
            bufferCommand(mathCommand);
            mathCommand = "AugODES = Table[" + "DerJ1[[i]] == " + "JEqs[[i]], {i, 1, Length[" + "DerJ1]}]";
            bufferCommand(mathCommand);
            mathCommand = "InitCondsG=Table[Flatten["
                    + "DynGainVars][[i]] == Random[Real, {0, 0.001}], {i, 1,Length[Flatten["
                    + "DynGainVarsDiff]]}] /. {" + timeVar + " -> 0}";
            bufferCommand(mathCommand);
            mathCommand = "AugInitConds = Join[" + "InitCondDepVars, " + "InitCondsG]";
            bufferCommand(mathCommand);
            mathCommand = "AugFullSystem = Join[" + "AugODES, " + "AugInitConds]";
            bufferCommand(mathCommand);
            mathCommand = "NumAugFullS = " + "AugFullSystem /. Join[" + "ParNumVals, " + "IndVarVals]";
            bufferCommand(mathCommand);
            mathCommand = "AugVNS = Join[" + "DepVars, Map[Head, Flatten[" + "DynGainVars]]]";
            bufferCommand(mathCommand);
            mathCommand = "NAugSol = First@NDSolve[" + "NumAugFullS, " + "AugVNS, {" + timeVar + "," + "ti," + "tf}]";
            bufferCommand(mathCommand);

            executeMathBuffer();
            plotDynamicSim("NAugSol");
            resultsAdd(new ResultText("Absolute Dynamic Gains", "textH2"));
            for (SimPlotView plotView : simConfig.getDynamic_PlotViews()) {
                mathCommand = "AG = {";
                mathCommand2 = "plotLegends = PointLegend[{";
                for (String dvToShow : plotView) {
                    for (String ind : m.getAllSpeciesConstant().keySet()) {
                        mathCommand += gainContext + dvToShow + ind + "[" + timeVar + "],";
                        mathCommand2 += "\"G_" + dvToShow + "_" + ind + "\",";
                    }
                }
                mathCommand = removeLastComma(mathCommand);
                mathCommand += "}";
                mathCommand2 = removeLastComma(mathCommand2);
                mathCommand2 += "}]";
                bufferCommand(mathCommand);
                bufferCommand(mathCommand2);
                executeMathBuffer();
                executePlot("AG", "plotLegends",
                        "NAugSol", "t", "Gain", getGeneralDownloadFilename("dynamic-ag", imageExtension));
            }

            // mathCommand = "RG = Table[DynGainVars[[i]]*IndVars/DepVarsWithT[[i]], {i,
            // 1,Length["
            // + "DynGainVars]}] /. IndVarVals";

            resultsAdd(new ResultText("Relative Dynamic Gains", "textH2"));
            for (SimPlotView plotView : simConfig.getDynamic_PlotViews()) {
                mathCommand = "RG = {";
                mathCommand2 = "plotLegends = PointLegend[{";
                for (String dvToShow : plotView) {
                    for (String ind : m.getAllSpeciesConstant().keySet()) {
                        mathCommand += gainContext + dvToShow + ind + "[" + timeVar + "]*"
                                + m.getAllSpecies().get(ind).getConcentration() + " / " + modelContext + dvToShow + "["
                                + timeVar + "],";
                        mathCommand2 += "\"G_" + dvToShow + "_" + ind + "\",";
                    }
                }
                mathCommand = removeLastComma(mathCommand);
                mathCommand += "}";
                mathCommand2 = removeLastComma(mathCommand2);
                mathCommand2 += "}]";
                bufferCommand(mathCommand);
                bufferCommand(mathCommand2);
                executeMathBuffer();
                executePlot("RG", "plotLegends",
                        "NAugSol", "t", "Relative Gain", getGeneralDownloadFilename("dynamic-rg", imageExtension));
            }

        }
        // DYNAMIC SENSITIVITIES
        if (((Boolean) simConfig.getDynamic().get("Sensitivities").getBooleanValue())) {
            mathCommand = "DynSensiVars = {";
            mathCommand2 = "DynSensiVarsDiff = {";
            for (String dep : m.getAllSpeciesTimeDependent().keySet()) {
                mathCommand += "{";
                mathCommand2 += "{";
                for (Reaction r : m) {
                    for (String parName : r.getFormulaGeneralParameters().keySet()) {
                        if (r.getFormulaGeneralParameters().get(parName) != null && r.getFormulaGeneralParameters().get(parName).isFilled()
                                && r.getFormulaGeneralParameters().get(parName).getType() == FormulaValueType.CONSTANT) {
                            mathCommand += sensContext + dep + r.getMathematicaContext() + parName + "[" + timeVar
                                    + "],";
                            mathCommand2 += sensContext + dep + r.getMathematicaContext() + parName + "'[" + timeVar
                                    + "],";
                        }
                    }
                    for (String parName : r.getFormulaSubstratesArrayParameters().keySet()) {
                        for (String sp : r.getFormulaSubstratesArrayParameters().get(parName).keySet()) {
                            if (r.getFormulaSubstratesArrayParameters().get(parName).get(sp) != null
                                    && r.getFormulaSubstratesArrayParameters().get(parName).get(sp).isFilled()) {
                                mathCommand += sensContext + dep + r.getMathematicaContext() + ContextUtils.arrayContext
                                        + parName + "`" + ContextUtils.substrateContext + sp + "[" + timeVar + "],";
                                mathCommand2 += sensContext + dep + r.getMathematicaContext()
                                        + ContextUtils.arrayContext + parName + "`" + ContextUtils.substrateContext + sp
                                        + "'[" + timeVar + "],";
                            }
                        }
                        for (String sp : r.getFormulaModifiersArrayParameters().get(parName).keySet()) {
                            if (r.getFormulaModifiersArrayParameters().get(parName).get(sp) != null
                                    && r.getFormulaModifiersArrayParameters().get(parName).get(sp).isFilled()) {
                                mathCommand += sensContext + dep + r.getMathematicaContext() + ContextUtils.arrayContext
                                        + parName + "`" + ContextUtils.modifierContext + sp + "[" + timeVar + "],";
                                mathCommand2 += sensContext + dep + r.getMathematicaContext()
                                        + ContextUtils.arrayContext + parName + "`" + ContextUtils.modifierContext + sp
                                        + "[" + timeVar + "],";
                            }
                        }
                    }
                }
                mathCommand = removeLastComma(mathCommand);
                mathCommand2 = removeLastComma(mathCommand2);
                mathCommand += "},";
                mathCommand2 += "},";
            }
            mathCommand = removeLastComma(mathCommand);
            mathCommand2 = removeLastComma(mathCommand2);
            mathCommand += "}";
            mathCommand2 += "}";
            bufferCommand(mathCommand);
            bufferCommand(mathCommand2);

//			mathCommand = "DynSensiVarsDiff = Table[ToExpression[ToString[S] <> ToString["
//					+ "DepVars[[i]]] <> ToString[" + "Pars[[j]]]]'[t], {i, 1, Dimensions["
//					+ "DepVars][[1]]}, {j, 1, Dimensions[" + "Pars][[1]]}]";
//			bufferCommand(mathCommand);
            mathCommand = "JacPars = Outer[D, " + "RateEqsWithT, " + "Pars]";
            bufferCommand(mathCommand);
            mathCommand = "EqSensis = Flatten[" + "Jac." + "DynSensiVars+" + "JacPars]";
            bufferCommand(mathCommand);
            mathCommand = "DerJ1 = Join[" + "DepVarsWithTDiff, Flatten[" + "DynSensiVarsDiff]]";
            bufferCommand(mathCommand);
            mathCommand = "JEqs = Join[" + "RateEqsWithT, " + "EqSensis]";
            bufferCommand(mathCommand);
            mathCommand = "AugODES = Table[" + "DerJ1[[i]] == " + "JEqs[[i]], {i, 1, Length[" + "DerJ1]}]";
            bufferCommand(mathCommand);
            mathCommand = "InitCondsG=Table[Flatten["
                    + "DynSensiVars][[i]] == Random[Real, {0, 0.001}], {i, 1,Length[Flatten["
                    + "DynSensiVarsDiff]]}] /. {" + timeVar + " -> 0}";
            bufferCommand(mathCommand);
            mathCommand = "AugInitConds = Join[" + "InitCondDepVars, " + "InitCondsG]";
            bufferCommand(mathCommand);
            mathCommand = "AugFullSystem = Join[" + "AugODES, " + "AugInitConds]";
            bufferCommand(mathCommand);
            mathCommand = "NumAugFullS = " + "AugFullSystem /. Join[" + "ParNumVals, " + "IndVarVals]";
            bufferCommand(mathCommand);
            mathCommand = "AugVNS = Join[" + "DepVars, Map[Head, Flatten[" + "DynSensiVars]]]";
            bufferCommand(mathCommand);
            mathCommand = "NAugSol = First@NDSolve[" + "NumAugFullS, " + "AugVNS, {" + timeVar + "," + "ti," + "tf}]";
            bufferCommand(mathCommand);
            executeMathBuffer();
            if (!((Boolean) simConfig.getDynamic().get("Gains").getBooleanValue())) {
                plotDynamicSim("NAugSol");
            }
            resultsAdd(new ResultText("Absolute Dynamic Sensitivities", "textH2"));
            for (SimPlotView plotView : simConfig.getDynamic_PlotViews()) {
                mathCommand = "AS = {";
                mathCommand2 = "plotLegends = PointLegend[{";
                for (String dvToShow : plotView) {
                    for (Reaction r : m) {
                        for (String parName : r.getFormulaGeneralParameters().keySet()) {
                            if (r.getFormulaGeneralParameters().get(parName) != null
                                    && r.getFormulaGeneralParameters().get(parName).isFilled()
                                    && r.getFormulaGeneralParameters().get(parName).getType() == FormulaValueType.CONSTANT) {
                                mathCommand += sensContext + dvToShow + r.getMathematicaContext() + parName + "["
                                        + timeVar + "],";
                                mathCommand2 += "\"S_" + dvToShow + "_" + r.getIdJavaStr() + "_" + parName + "\",";
                            }
                        }
                        for (String parName : r.getFormulaSubstratesArrayParameters().keySet()) {
                            for (String sp : r.getFormulaSubstratesArrayParameters().get(parName).keySet()) {
                                if (r.getFormulaSubstratesArrayParameters().get(parName).get(sp) != null
                                        && r.getFormulaSubstratesArrayParameters().get(parName).get(sp).isFilled()) {
                                    mathCommand += sensContext + dvToShow + r.getMathematicaContext()
                                            + ContextUtils.arrayContext + parName + "`" + ContextUtils.substrateContext
                                            + sp + "[" + timeVar + "],";
                                    mathCommand2 += "\"S_" + dvToShow + "_" + r.getIdJavaStr() + "_" + parName + "_Sub_" + sp
                                            + "\",";
                                }
                            }
                            for (String sp : r.getFormulaModifiersArrayParameters().get(parName).keySet()) {
                                if (r.getFormulaModifiersArrayParameters().get(parName).get(sp) != null
                                        && r.getFormulaModifiersArrayParameters().get(parName).get(sp).isFilled()) {
                                    mathCommand += sensContext + dvToShow + r.getMathematicaContext()
                                            + ContextUtils.arrayContext + parName + "`" + ContextUtils.modifierContext
                                            + sp + "[" + timeVar + "],";
                                    mathCommand2 += "\"S_" + dvToShow + "_" + r.getIdJavaStr() + "_" + parName + "_Mod_" + sp
                                            + "\",";
                                }
                            }
                        }
                    }
                }
                mathCommand = removeLastComma(mathCommand);
                mathCommand += "}";
                mathCommand2 = removeLastComma(mathCommand2);
                mathCommand2 += "}]";
                bufferCommand(mathCommand);
                bufferCommand(mathCommand2);
                executeMathBuffer();
                executePlot("AS", "plotLegends", "NAugSol", "t", "Sensitivity", getGeneralDownloadFilename("dynamic-as", imageExtension));
            }

            // mathCommand = "RS = Table[DynSensiVars[[i]]*Pars/DepVarsWithT[[i]], {i,
            // 1,Length["
            // + "DynSensiVars]}] /.ParNumVals";
            // bufferCommand(mathCommand);
            // executeMathBuffer();
            resultsAdd(new ResultText("Relative Dynamic Sensitivities", "textH2"));
            for (SimPlotView plotView : simConfig.getDynamic_PlotViews()) {
                mathCommand = "RS = {";
                mathCommand2 = "plotLegends = PointLegend[{";
                for (String dvToShow : plotView) {
                    for (Reaction r : m) {
                        for (String par : r.getFormulaGeneralParameters().keySet()) {
                            if (r.getFormulaGeneralParameters().get(par) != null && r.getFormulaGeneralParameters().get(par).isFilled()
                                    && r.getFormulaGeneralParameters().get(par).getType() == FormulaValueType.CONSTANT) {
                                mathCommand += sensContext + dvToShow + r.getMathematicaContext() + par + "[" + timeVar
                                        + "]*" + r.getFormulaGeneralParameters().get(par).getStringValue() + "/" + modelContext
                                        + dvToShow + "[" + timeVar + "],";
                                mathCommand2 += "\"S_" + dvToShow + "_" + r.getIdJavaStr() + "_" + par + "\",";
                            }
                        }
                        for (String parName : r.getFormulaSubstratesArrayParameters().keySet()) {
                            for (String sp : r.getFormulaSubstratesArrayParameters().get(parName).keySet()) {
                                if (r.getFormulaSubstratesArrayParameters().get(parName).get(sp) != null
                                        && r.getFormulaSubstratesArrayParameters().get(parName).get(sp).isFilled()) {
                                    mathCommand += sensContext + dvToShow + r.getMathematicaContext()
                                            + ContextUtils.arrayContext + parName + "`" + ContextUtils.substrateContext
                                            + sp + "[" + timeVar + "]*"
                                            + r.getFormulaSubstratesArrayParameters().get(parName).get(sp).getValue()
                                            + "/" + modelContext + dvToShow + "[" + timeVar + "],";
                                    mathCommand2 += "\"S_" + dvToShow + "_" + r.getIdJavaStr() + "_" + parName + "_Sub_" + sp
                                            + "\",";
                                }
                            }
                            for (String sp : r.getFormulaModifiersArrayParameters().get(parName).keySet()) {
                                if (r.getFormulaModifiersArrayParameters().get(parName).get(sp) != null
                                        && r.getFormulaModifiersArrayParameters().get(parName).get(sp).isFilled()) {
                                    mathCommand += sensContext + dvToShow + r.getMathematicaContext()
                                            + ContextUtils.arrayContext + parName + "`" + ContextUtils.modifierContext
                                            + sp + "[" + timeVar + "]*"
                                            + r.getFormulaModifiersArrayParameters().get(parName).get(sp).getValue()
                                            + "/" + modelContext + dvToShow + "[" + timeVar + "],";
                                    mathCommand2 += "\"S" + dvToShow + "_" + r.getIdJavaStr() + "_" + parName + "_Mod_" + sp
                                            + "\",";
                                }
                            }
                        }
                    }
                }
                mathCommand = removeLastComma(mathCommand);
                mathCommand += "}";
                mathCommand2 = removeLastComma(mathCommand2);
                mathCommand2 += "}]";
                bufferCommand(mathCommand);
                bufferCommand(mathCommand2);
                executeMathBuffer();
                executePlot("RS", "plotLegends", "NAugSol", "t", "Relative Sens.", getGeneralDownloadFilename("dynamic-rs", imageExtension));
            }

        }
        // PARAMETER SCAN
        if (simConfig.getDynamic_ParameterScan().hasParametersToScan(m)) {
            resultsAdd(new ResultText("Dynamic Parameter Scan", "textH2"));
            ArrayList<ParamScanEntry> paramScanEntries = null;
            for (int iType = 1; iType <= 2; iType++) {
                // select param scan entries
                if (iType == 1) {
                    if (simConfig.getDynamic_ParameterScan().getParameters().size() == 0)
                        continue;
                    paramScanEntries = simConfig.getDynamic_ParameterScan().getParameters();
                } else if (iType == 2) {
                    if (simConfig.getDynamic_ParameterScan().getIndependentVars().size() == 0)
                        continue;
                    paramScanEntries = simConfig.getDynamic_ParameterScan().getIndependentVars();
                }
                // scan task
                for (ParamScanEntry entry : paramScanEntries) {
                    mathCommand = "paramName=" + entry.getMathematicaParamName() + ";\r\n" + "paramType=\""
                            + entry.getType().getString() + "\";\r\n" + "beginVal=" + entry.getBeginVal() + ";\r\n"
                            + "endVal=" + entry.getEndVal() + ";\r\n" + "numIntervals = " + entry.getNumIntervals()
                            + ";\r\n" + "isLogarithmic=" + MathematicaUtils.java2Math(entry.isLogarithmic()) + ";\n"
                            + "minLogVal=" + MathematicaUtils.minLogVal;
                    bufferCommand(mathCommand);
                    bufferMathTxt("param-scan-dyn.txt");
                    executeMathBuffer();
                    for (int i = 1; i <= m.getAllSpeciesTimeDependent().size(); i++) {
                        // Legend: 1 column only: LegendLayout -> {"Column", 1} or
                        // LegendLayout->TableForm
                        mathCommand = "plotList = Table[Transpose[solsList][[" + i
                                + ", All, 2]][[i]][t], {i, Length@ParamScanVals}];\r\n"
                                + "plotVar = StringDrop[ToString[Transpose[solsList][[" + i
                                + ", All, 1]][[1]]], 2];\r\n"
                                + "plotLegends = PointLegend[Table[ToString@ParamScanVals[[i,2,1]]<>\"=\"<>ToStringNumber@ParamScanVals[[i,2,2]], {i, Length@ParamScanVals}]];\r\n"
                                + "Plot[plotList, {t, ti, tf}," + "LabelStyle -> {FontWeight -> "
                                + ((Boolean) simConfig.getPlotSettings().get("FontWeight").getBooleanValue() ? "Bold"
                                : "Plain")
                                + ", FontSlant -> "
                                + ((Boolean) simConfig.getPlotSettings().get("FontSlant").getBooleanValue() ? "Italic"
                                : "Plain")
                                + ", " + "FontSize -> " + simConfig.getPlotSettings().get("FontSize").getValue()
                                + "}, PlotLegends -> plotLegends, Axes -> False, "
                                + "Frame -> True, FrameTicks -> {{True,False},{True,False}}, " + "  PlotStyle -> "
                                + "Table[{Dashing[0.03*(k1/Length[plotVars])], " + "Thickness["
                                + simConfig.getPlotSettings().get("LineThickness").getValue()
                                + "]}, {k1, Length[plotVars]}], "
                                + "FrameLabel -> {\"t\", StringJoin[plotVar,\" (concentration)\"]}, "
                                + "PlotRange -> Full, ImageSize -> " + normalSize + "]";
                        getNotebookBuffer().addCommand(mathCommand);
                        showImageOfMathCmd(mathCommand, getGeneralDownloadFilename("dyn-parscan-" + i, imageExtension),
                                imageWidthForResults);
                    }
                }
            }
        }
    }

    private void plotDynamicSim(String ndSolveVar) throws Exception {
        for (SimPlotView plotView : simConfig.getDynamic_PlotViews()) {
            mathCommand = genContext + "plotVars = {";
            mathCommand2 = genContext + "plotLegends = PointLegend[{";
            for (String dvToShow : plotView) {
                mathCommand += modelContext + dvToShow + "[" + timeVar + "],";
                mathCommand2 += "ToString[" + dvToShow + "],";
            }
            mathCommand = removeLastComma(mathCommand);
            mathCommand += "}";
            mathCommand2 = removeLastComma(mathCommand2);
            mathCommand2 += "}]";
            bufferCommand(mathCommand);
            bufferCommand(mathCommand2);
            executeMathBuffer();
            executePlot(genContext + "plotVars", genContext + "plotLegends", ndSolveVar, "t", "Concentration",
                    getGeneralDownloadFilename("dyn", imageExtension));
        }

        mathCommand = "tableHeader={Join[{\"t\"}, DepVarsString]};\r\n" + "txtTable= Join[tableHeader,"
                + " Table[Join[{timeI},Table[ToStringNumber[DepVars[[i]][timeI] /. " + ndSolveVar
                + "], {i, Length[DepVars]}]], {timeI, ti, tf, tStep}]] // TableForm";
        showTxtFileDownloadButton(mathCommand, "Download Dynamic Simulation Table", getGeneralDownloadFilename("dynamic", ".txt"));
    }

    private void stochasticSimulation() throws Exception {
        String method = (String) simConfig.getStochastic().get("Method").getValue();
        boolean isTauLeaping = method.equals("Tau-leaping");
        normalSize = (String) simConfig.getPlotSettings().get("ImageSize").getValue().toString();
        String head = "Stochastic Simulation";
        if (isTauLeaping)
            head += " (Method=Tau-leaping;";
        else
            head += " (Method=SSA;";
        head += " Final time=" + (String) simConfig.getStochastic().get("Tf").getValue() + ")";
        resultsAdd(new ResultText(head, "textH2"));
        resultsAdd(new ResultStochasticStats(Integer.valueOf((String) simConfig.getStochastic().get("Replicates").getValue()), isTauLeaping));
        if (isTauLeaping)
            stochasticTauLeaping();
        else
            stochasticSSA();
    }

    private void stochasticTauLeaping() throws Exception {
        bufferCommand(genContext + "ti = " + (String) simConfig.getStochastic().get("Ti").getValue());
        bufferCommand(genContext + "tf = " + (String) simConfig.getStochastic().get("Tf").getValue());
        bufferCommand("tStep = (tf-ti)/1000");
        bufferCommand(
                genContext + "stochasticReps = " + (String) simConfig.getStochastic().get("Replicates").getValue());
        bufferCommand(genContext + "cellSize = " + String.valueOf(
                CellSizes.getInstance().nameToNum((String) simConfig.getStochastic().get("CellSize").getValue())));
        bufferMathTxt("stochastic-tau-leaping.txt");
        executeMathBuffer();

//		if (useMathLink
//				&& mathGetString("Catch[If[Length[stPlotLists] < 1,Throw[\"error\"]]]").equals("error"))
//			throw new CException("Stochastic simulation procedure error");
//		boolean isMoreThanOneReplicate = useMathLink
//				&& mathGetString("Catch[If[Dimensions[stPlotLists][[2]]>1,Throw[\"CalculateNoise\"]]]")
//						.equals("CalculateNoise");
//		if (isMoreThanOneReplicate) {
//			bufferCommand(
//					"NoiseQ025=Table[TimeSeries[Quantile[Table[stPlotLists[[k]][[k2]][\"Values\"],{k2,1,Length[stPlotLists[[k]]]}],0.25],{stPlotLists[[k]][[1]][\"Times\"]}],{k,1,Length[stPlotLists]}]");
//			bufferCommand(
//					"NoiseMedian=Table[TimeSeries[Median[Table[stPlotLists[[k]][[k2]][\"Values\"],{k2,1,Length[stPlotLists[[k]]]}]],{stPlotLists[[k]][[1]][\"Times\"]}],{k,1,Length[stPlotLists]}]");
//			bufferCommand(
//					"NoiseQ075=Table[TimeSeries[Quantile[Table[stPlotLists[[k]][[k2]][\"Values\"],{k2,1,Length[stPlotLists[[k]]]}],0.75],{stPlotLists[[k]][[1]][\"Times\"]}],{k,1,Length[stPlotLists]}]");
//			bufferCommand(
//					"NoiseStdDev=Table[TimeSeries[StandardDeviation[Table[stPlotLists[[k]][[k2]][\"Values\"],{k2,1,Length[stPlotLists[[k]]]}]],{stPlotLists[[k]][[1]][\"Times\"]}],{k,1,Length[stPlotLists]}]");
//			bufferCommand(
//					"NoiseMean=Table[TimeSeries[Mean[Table[stPlotLists[[k]][[k2]][\"Values\"],{k2,1,Length[stPlotLists[[k]]]}]],{stPlotLists[[k]][[1]][\"Times\"]}],{k,1,Length[stPlotLists]}]");
//			bufferCommand("Off[Infinity::indet];Off[Power::infy]");
//			bufferCommand(
//					"PIN=Table[TimeSeries[(NoiseStdDev[[k]][\"Values\"]/NoiseMean[[k]][\"Values\"])/.{Indeterminate -> 0},{NoiseMean[[1]][\"Times\"]}],{k,1,Length[NoiseMean]}]");
//			bufferCommand(
//					"NPIN=Table[TimeSeries[((NoiseQ075[[k]][\"Values\"]-NoiseQ025[[k]][\"Values\"])/NoiseMedian[[k]][\"Values\"])/.{Indeterminate -> 0},{NoiseQ075[[1]][\"Times\"]}],{k,1,Length[NoiseQ075]}]");
//			bufferCommand("On[Infinity::indet];On[Power::infy]");
//			executeMathBuffer();
//		}
        if (useMathLink) {
            Integer numPlotsByDepVars = Integer.valueOf(mathGetString("Length[stPlotLists]"));
            if (numPlotsByDepVars > 0) {
                boolean isPlotNoise = mathGetString("ValueQ[NoiseQ025]").equals("True");
                plotStochasticSim(numPlotsByDepVars, isPlotNoise);

                mathCommand = "tableHeader = {Flatten[{\"t\"," + "    Table[Table["
                        + "      DepVarsString[[k]] <> \"_\" <> ToString[k2], {k2, 1,"
                        + "       Length[stPlotLists[[k]]]}], {k, 1, Length[stPlotLists]}]}]};\r\n"
                        + "txtTable=Join[tableHeader, " + " Transpose[" + "  Join[{N@stPlotLists[[1]][[1]][\"Times\"]},"
                        + "   Flatten[Table[Table[stPlotLists[[k]][[k2]][\"Values\"], {k2, 1,"
                        + "       Length[stPlotLists[[k]]]}], {k, 1, Length[stPlotLists]}], 1]]]] //TableForm";
                resultsAdd(new ResultDownloadButton(mathGetString(mathCommand).replaceAll("(\\r?\\n){2,}","\n"), "Stochastic Simulation Text Table", getGeneralDownloadFilename("tauleaping", ".txt")));
            } else {
                resultsAdd(new ResultText(stochasticNotCompatibleErrorMessage, "warningH3"));
            }
        }
    }

    private void stochasticSSA() throws Exception {
        bufferCommand("ti = " + (String) simConfig.getStochastic().get("Ti").getValue());
        bufferCommand("tf = " + (String) simConfig.getStochastic().get("Tf").getValue());
        bufferCommand("tStep = (tf-ti)/1000");
        bufferCommand(
                genContext + "stochasticReps = " + (String) simConfig.getStochastic().get("Replicates").getValue());
        bufferCommand(genContext + "cellSize = " + String.valueOf(
                CellSizes.getInstance().nameToNum((String) simConfig.getStochastic().get("CellSize").getValue())));
        bufferMathTxt("stochastic-ssa.txt");
        executeMathBuffer();
//		if (mathGetString("Catch[If[ValueQ[stPlotLists]==False,Throw[\"error\"]]]").equals("error"))
//			throw new CException("Stochastic simulation procedure error");
//		if (isCalcNoise) {
//			bufferCommand(
//					"NoiseQ025=Table[TimeSeries[Quantile[Table[stPlotLists[[k]][[k2]][\"Values\"],{k2,1,Length[stPlotLists[[k]]]}],0.25],{stPlotLists[[k]][[1]][\"Times\"]}],{k,1,Length[stPlotLists]}]");
//			bufferCommand(
//					"NoiseMedian=Table[TimeSeries[Median[Table[stPlotLists[[k]][[k2]][\"Values\"],{k2,1,Length[stPlotLists[[k]]]}]],{stPlotLists[[k]][[1]][\"Times\"]}],{k,1,Length[stPlotLists]}]");
//			bufferCommand(
//					"NoiseQ075=Table[TimeSeries[Quantile[Table[stPlotLists[[k]][[k2]][\"Values\"],{k2,1,Length[stPlotLists[[k]]]}],0.75],{stPlotLists[[k]][[1]][\"Times\"]}],{k,1,Length[stPlotLists]}]");
//			bufferCommand(
//					"NoiseStdDev=Table[TimeSeries[StandardDeviation[Table[stPlotLists[[k]][[k2]][\"Values\"],{k2,1,Length[stPlotLists[[k]]]}]],{stPlotLists[[k]][[1]][\"Times\"]}],{k,1,Length[stPlotLists]}]");
//			bufferCommand(
//					"NoiseMean=Table[TimeSeries[Mean[Table[stPlotLists[[k]][[k2]][\"Values\"],{k2,1,Length[stPlotLists[[k]]]}]],{stPlotLists[[k]][[1]][\"Times\"]}],{k,1,Length[stPlotLists]}]");
//			bufferCommand("Off[Infinity::indet];Off[Power::infy]");
//			bufferCommand(
//					"PIN=Table[TimeSeries[(NoiseStdDev[[k]][\"Values\"]/NoiseMean[[k]][\"Values\"])/.{Indeterminate -> 0},{NoiseMean[[1]][\"Times\"]}],{k,1,Length[NoiseMean]}]");
//			bufferCommand(
//					"NPIN=Table[TimeSeries[((NoiseQ075[[k]][\"Values\"]-NoiseQ025[[k]][\"Values\"])/NoiseMedian[[k]][\"Values\"])/.{Indeterminate -> 0},{NoiseQ075[[1]][\"Times\"]}],{k,1,Length[NoiseQ075]}]");
//			bufferCommand("On[Infinity::indet];On[Power::infy]");
//			executeMathBuffer();
//		}
        if (useMathLink) {
            //Integer numPlotsByDepVars = Integer.valueOf(simConfig.getStochastic().get("Replicates").getValue());
            Integer numPlotsByDepVars = Integer.valueOf(mathGetString("Length[stPlotLists]"));
            if (numPlotsByDepVars > 0) {
                boolean isPlotNoise = mathGetString("ValueQ[NoiseQ025]").equals("True");
                plotStochasticSim(numPlotsByDepVars, isPlotNoise);

                mathCommand = "tableHeader = {Flatten[{\"t\"," + "    Table[Table["
                        + "      DepVarsString[[k]] <> \"_\" <> ToString[k2], {k2, 1,"
                        + "       Length[stPlotLists[[k]]]}], {k, 1, Length[stPlotLists]}]}]};\r\n"
                        + "txtTable=Join[tableHeader, " + " Transpose[" + "  Join[{N@stPlotLists[[1]][[1]][\"Times\"]},"
                        + "   Flatten[Table[" + "     Table[stPlotLists[[k]][[k2]][\"Values\"], {k2, 1,"
                        + "       Length[stPlotLists[[k]]]}], {k, 1, Length[stPlotLists]}], 1]]]] // TableForm";
                resultsAdd(new ResultDownloadButton(mathGetString(mathCommand).replaceAll("(\\r?\\n){2,}","\n"), "Simulation Text Table", getGeneralDownloadFilename("ssa", ".txt")));
            } else {
                resultsAdd(new ResultText(stochasticNotCompatibleErrorMessage, "warningH3"));
            }
        }
    }

    private void
    plotStochasticSim(Integer numPlotsByDepVars, boolean isPlotNoise) throws Exception {
        //bufferCommand("Print[\"" + MathPacketListenerOp.printPrefix + "Plotting graphics...\"];");
        //executeMathBuffer();
        resultsAdd(new ResultText("All trajectories medians plot", "textH3"));
        executeListLinePlot("NoiseMedian", "\"t\"", "\"# Molecules \"",
                "PointLegend[Table[DepVarsString[[i]],{i,Length@DepVarsString}],LegendMarkers->Graphics@Disk[]]", getGeneralDownloadFilename("stoch", imageExtension));

        ArrayList<String> depVarList = m.getDepVarArrayList();
        for (int i = 1; i <= numPlotsByDepVars; i++) {
            resultsAdd(new ResultText("Graphics related to: " + depVarList.get(i - 1), "textH3"));
            executeListLinePlot("stPlotLists[[" + i + "]]", "\"t\"", "\"# Molecules \"<>DepVarsString[[" + i + "]]",
                    "{}", getGeneralDownloadFilename("stoch-" + depVarList.get(i - 1), imageExtension));
            if (isPlotNoise) {
                executeListLinePlot("{NoiseQ025[[" + i + "]],NoiseMedian[[" + i + "]],NoiseQ075[[" + i + "]]}", "\"t\"",
                        "\"# Molecules \"<>DepVarsString[[" + i + "]]",
                        "Placed[PointLegend[{\"Quantile_0.25\", \"Median\", \"Quantile_0.75\"},LegendMarkers->Graphics@Disk[]],Below]", getGeneralDownloadFilename("stoch-noise-" + depVarList.get(i - 1), imageExtension));
                executeListLinePlot("{PIN[[" + i + "]],NPIN[[" + i + "]]}", "\"t\"",
                        "\"Coefficient of Variation \"<>DepVarsString[[" + i + "]]",
                        "Placed[PointLegend[{\"Parametric [\\[Sigma]/\\[Mu]]\", \"Non parametric [(Quantile_0.75-Quantile_0.25)/Median]\"},LegendMarkers->Graphics@Disk[]], Below]", getGeneralDownloadFilename("stoch-var-" + depVarList.get(i - 1), imageExtension));
            }
        }
    }

    private void executeListLinePlot(String symbolToPlot, String xLabel, String yLabel, String legend, String fileName)
            throws Exception {
        String cmd = "ListLinePlot[" + symbolToPlot + ", LabelStyle -> {" + "FontWeight -> "
                + ((Boolean) simConfig.getPlotSettings().get("FontWeight").getBooleanValue() ? "Bold" : "Plain")
                + ", FontSlant -> "
                + ((Boolean) simConfig.getPlotSettings().get("FontSlant").getBooleanValue() ? "Italic" : "Plain")
                + ", FontSize -> " + simConfig.getPlotSettings().get("FontSize").getValue() + "}, PlotLegends->"
                + legend + ", Axes -> False, Frame -> True, FrameTicks -> True, PlotStyle -> Thickness["
                + simConfig.getPlotSettings().get("LineThickness").getValue() + "], FrameLabel -> {" + xLabel + ", "
                + yLabel + "}, PlotRange->Full, ImageSize->" + normalSize + "]";
        getNotebookBuffer().addCommand(cmd);
        showImageOfMathCmd(cmd, fileName, imageWidthForResults);
    }
    ///////// utils

    private String removeLastComma(String str) {
        if (str.charAt(str.length() - 1) == ',')
            str = str.substring(0, str.length() - 1);
        return str;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getDepVarsToShowDynGain(Map<String, Object> map, String dynGainVars) throws Exception {
        Map<String, String> resMap = new HashMap<>();
        String plotList = "{";
        String list = mathGetString("Flatten[" + dynGainVars + "]").replaceAll("\\{", "").replaceAll("\\}", "")
                .replaceAll("\\s", "");
        for (String part : list.split(",")) {
            for (String dv : (ArrayList<String>) map.get("DepVarsToShow")) {
                if (part.startsWith("G" + dv))
                    plotList += part + ",";
            }
        }
        if (plotList.charAt(plotList.length() - 1) == ',')
            plotList = plotList.substring(0, plotList.lastIndexOf(","));
        plotList += "}";
        resMap.put("plotList", plotList);
        String plotLegends = plotList.replace("[t]", "");
        resMap.put("plotLegends", plotLegends);
        return resMap;
    }

    private void steadyStateSimulation() throws Exception {
        // model must have been checked before calling this function
        TreeMap<String, String> ssMap = new TreeMap<>();
        executeMathBuffer();
        resultsAdd(new ResultText("Steady State Simulation", "textH2"));

        bufferCommand("thresholdCondition=" + simConfig.getSteadyState().get("Threshold"));
        bufferMathTxt("steady-state.txt");
        executeMathBuffer();
        String steadyStateType = mathGetString("typeSS");
        if (steadyStateType.equals("stable") || steadyStateType.equals("unstable")) {
            fillSteadyStateSimulationMap(genContext + "SSSol", ssMap);
            showSteadyStateSimulationTableAndButton(ssMap);
            if (((Boolean) simConfig.getSteadyState().get("Stability").getBooleanValue())) {
                resultsAdd(new ResultText("Stability Analysis", "textH2"));
                if (steadyStateType.equals("stable")) {
                    resultsAdd(new ResultText("Stable: all real parts are negative.", null));
                } else if (steadyStateType.equals("unstable")) {
                    resultsAdd(new ResultText("Unstable: at least one real part is non-negative.", null));
                }

                mathCommand = "" + genContext + "eigenValues = Eigenvalues[" + genContext + "Jac /. Join[" + genContext
                        + "ParNumVals, " + genContext + "IndVarVals, " + genContext + "SSSol]]";
                bufferCommand(mathCommand);
                mathCommand = "" + genContext + "eigenValues=Table[Join[{Re[" + genContext + "eigenValues[[i]]]},{Im["
                        + genContext + "eigenValues[[i]]]}], {i,1,Length[" + genContext + "eigenValues]}]";
                bufferCommand(mathCommand);
                executeMathBuffer();
                executeMathTable("" + genContext + "eigenValues", "{ToString[Real],ToString[Im]}",
                        "Table[ToString[Eigenvalue]<>ToString[i], {i,1,Length[" + genContext + "DepVars]}]", false);
            }
        }

        // mathCommand = "Select[Im[Eigenvalues[Evaluate[Jac /. Join[ParNumVals,
        // IndVarVals, setTime] /. NAugSol]]], (# != 0) &] == {}";
        // if (executeMathCommandBoolean(false, mathCommand)) {
        // outVL.out("Steady State: Overshooting attenuated
        // oscillations");

        // Steady State Gains (D=>derivative)
        if (((Boolean) simConfig.getSteadyState().get("Gains").getBooleanValue())) {
            if (!steadyStateType.equals("stable")) {
                resultsAdd(new ResultText("Can't calculate Gains for unstable Steady State", "textH2"));
            } else {
                mathCommand = "JacIV = Outer[D, " + "RateEqsWithT, " + "IndVars]";
                bufferCommand(mathCommand);
                mathCommand = "NJacIV = Chop[" + "JacIV /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]"
                        + getChopTolerance() + "]";
                bufferCommand(mathCommand);
                mathCommand = "GA = -Inverse[" + "Jac /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]]."
                        + "NJacIV";
                bufferCommand(mathCommand);
                resultsAdd(new ResultText("Absolute Steady State Gains", "textH2"));
                // outVL.outNewGrid();
                // for (Map<String, Object> plotView : simConfig.getPlotViews()) {
                // mathCommand = "DepVarsToShow = {";
                // for (String dvToShow : (ArrayList<String>) plotView.get("DepVarsToShow"))
                // mathCommand += dvToShow+",";
                // mathCommand = removeLastComma(mathCommand);
                // mathCommand += "}";
                // bufferCommand(mathCommand);
                // executeMathTableGrid("GA", "IndVars", "DepVarsToShow", true);
                // }
                // outVL.showGrid();

                mathCommand = "DepVarsToShow = {";
                for (String dv : simConfig.getDynamic_PlotViews().getUnifiedDepVars().keySet())
                    mathCommand += dv + ",";
                mathCommand = removeLastComma(mathCommand);
                mathCommand += "}";
                bufferCommand(mathCommand);
                executeMathTable("GA", "IndVarsString", "DepVarsToShow", true);

                mathCommand = "GR = Table[" + "GA[[i, j]]*(" + "IndVars[[j]]/"
                        + "DepVarsWithT[[i]]), {i, 1, Dimensions[" + "DepVarsWithT][[1]]}, {j, 1, Dimensions["
                        + "IndVars][[1]]}] /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]";
                bufferCommand(mathCommand);
                resultsAdd(new ResultText("Relative Steady State Gains", "textH2"));
//				mathCommand = "DepVarsToShow = {";
//				for (String dv : simConfig.getUnifiedDepVarsFromPlotViews().keySet())
//					mathCommand += dv + ",";
//				mathCommand = removeLastComma(mathCommand);
//				mathCommand += "}";
//				bufferCommand(mathCommand);
                executeMathTable("GR", "IndVarsString", "DepVarsToShow", true);
            }
        }
        // Steady State Sensitivities
        if (((Boolean) simConfig.getSteadyState().get("Sensitivities").getBooleanValue())) {
            if (!steadyStateType.equals("stable")) {
                resultsAdd(new ResultText("Can't calculate Sensitivities for unstable Steady State", "textH2"));
            } else {
                mathCommand = "JacP = Outer[D, " + "RateEqsWithT, " + "Pars]";
                bufferCommand(mathCommand);
                mathCommand = "NJacP = Chop[" + "JacP /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]"
                        + getChopTolerance() + "]";
                bufferCommand(mathCommand);
                mathCommand = "SSSA = -Inverse[" + "Jac /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]]."
                        + "NJacP";
                bufferCommand(mathCommand);
                resultsAdd(new ResultText("Absolute Steady State Sensitivities", "textH2"));
                mathCommand = "DepVarsToShow = {";
                for (String dv : simConfig.getDynamic_PlotViews().getUnifiedDepVars().keySet())
                    mathCommand += dv + ",";
                mathCommand = removeLastComma(mathCommand);
                mathCommand += "}";
                bufferCommand(mathCommand);
                executeMathTable("SSSA", "ParsString", "DepVarsToShow", true);

                mathCommand = "SSSR = Table[" + "SSSA[[i, j]]*(" + "Pars[[j]]/"
                        + "DepVarsWithT[[i]]), {i, 1, Dimensions[" + "DepVarsWithT][[1]]}, {j, 1, Dimensions["
                        + "Pars][[1]]}] /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]";
                bufferCommand(mathCommand);
                resultsAdd(new ResultText("Relative Steady State Sensitivities", "textH2"));
                executeMathTable("SSSR", "ParsString", "DepVarsToShow", true);
            }
        }
        // PARAMETER SCAN
        if (simConfig.getSteadyState_ParameterScan().hasParametersToScan(m)) {
            resultsAdd(new ResultText("Steady State Parameter Scan", "textH2"));
            ArrayList<ParamScanEntry> paramScanEntries = null;
            for (int iType = 1; iType <= 2; iType++) {
                // select param scan entries
                if (iType == 1) {
                    if (simConfig.getSteadyState_ParameterScan().getParameters().size() == 0)
                        continue;
                    paramScanEntries = simConfig.getSteadyState_ParameterScan().getParameters();
                } else if (iType == 2) {
                    if (simConfig.getSteadyState_ParameterScan().getIndependentVars().size() == 0)
                        continue;
                    paramScanEntries = simConfig.getSteadyState_ParameterScan().getIndependentVars();
                }
                // scan task
                for (ParamScanEntry entry : paramScanEntries) {
                    resultsAdd(new ResultText("Parameter=" + entry.getMathematicaParamName() + " ; Scanning range=[" + entry.getBeginVal() + "," + entry.getEndVal() + "] ; #Intervals=" + entry.getNumIntervals() + (entry.isLogarithmic() ? " ; Logarithmic)" : ""), "textH3"));
                    mathCommand = "paramName=" + entry.getMathematicaParamName() + ";\r\n" + "paramType=\""
                            + entry.getType().getString() + "\";\r\n" + "beginVal=" + entry.getBeginVal() + ";\r\n"
                            + "endVal=" + entry.getEndVal() + ";\r\n" + "numIntervals = " + entry.getNumIntervals()
                            + ";\r\n" + "isLogarithmic=" + MathematicaUtils.java2Math(entry.isLogarithmic()) + ";\n"
                            + "minLogVal=" + MathematicaUtils.minLogVal;
                    bufferCommand(mathCommand);
                    bufferMathTxt("param-scan-steady-state.txt");
                    executeMathBuffer();
                    if ("False".equals(mathGetString("isEmptyResults"))) {
                        for (int i = 1; i <= m.getAllSpeciesTimeDependent().size(); i++) {
                            mathCommand = "plotLegends = PointLegend[{\"Stable\", \"Unstable\"}];\n"
                                    + "If[!isLogarithmic,ListPlot,ListLogLinearPlot][{stablePoints[[" + i
                                    + "]], unstablePoints[[" + i + "]]}, " + "LabelStyle -> {FontWeight -> "
                                    + ((Boolean) simConfig.getPlotSettings().get("FontWeight").getBooleanValue() ? "Bold"
                                    : "Plain")
                                    + ", FontSlant -> "
                                    + ((Boolean) simConfig.getPlotSettings().get("FontSlant").getBooleanValue() ? "Italic"
                                    : "Plain")
                                    + ", " + "FontSize -> " + simConfig.getPlotSettings().get("FontSize").getValue()
                                    + "}, PlotLegends -> plotLegends, Axes -> False, "
                                    + "Frame -> True, FrameTicks -> {{True,False},{True,False}}, "
                                    + "PlotStyle -> {RGBColor[0.18, 0.63, 1], Hue[0, 0.54, 1]}, "
                                    + "FrameLabel -> {ToString[paramName], " + "DepVarsString[[" + i
                                    + "]] <> \" (concentration)\"}, PlotRange -> Full, " + "ImageSize -> " + normalSize
                                    + "]";
                            getNotebookBuffer().addCommand(mathCommand);
                            showImageOfMathCmd(mathCommand, getGeneralDownloadFilename("ss-parscan-" + i, imageExtension),
                                    imageWidthForResults);
                        }
                    } else {
                        mathCommand = "Grid[{{\"No Steady State found for " + entry.getMathematicaParamName() + "\"}}, Frame -> All, Spacings -> {30, 20}]";
                        getNotebookBuffer().addCommand(mathCommand);
                        showImageOfMathCmd(mathCommand, getGeneralDownloadFilename("ss-parscan-", imageExtension),
                                imageWidthForResults);
                    }
                }
            }
        }
    }
}
