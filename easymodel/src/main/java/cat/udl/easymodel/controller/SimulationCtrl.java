package cat.udl.easymodel.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.antlr.v4.parse.ANTLRParser.throwsSpec_return;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;

import cat.udl.easymodel.logic.formula.FormulaValue;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.simconfig.CellSizes;
import cat.udl.easymodel.logic.simconfig.ParamScanEntry;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.simconfig.SimConfigArray;
import cat.udl.easymodel.logic.stochastic.ReactantModelLevel;
import cat.udl.easymodel.logic.stochastic.ReactantReactionLevel;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.ParamScanType;
import cat.udl.easymodel.logic.types.SimType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathPacketListenerOp;
import cat.udl.easymodel.mathlink.MathLinkOp;
import cat.udl.easymodel.sbml.SBMLMan;
import cat.udl.easymodel.utils.CException;
import cat.udl.easymodel.utils.MathematicaUtils;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.utils.buffer.ExportMathCommands;
import cat.udl.easymodel.utils.buffer.ExportMathCommands;
import cat.udl.easymodel.utils.buffer.MathBuffer;
import cat.udl.easymodel.utils.buffer.MathBuffer;
import cat.udl.easymodel.vcomponent.common.SpacedLabel;
import cat.udl.easymodel.vcomponent.results.OutVL;

public class SimulationCtrl {
	// External resources
	private SessionData sessionData = null;
	private SharedData sharedData = SharedData.getInstance();
	private OutVL outVL;
	private MathLinkOp mathLink = null;

	private String genContext = ContextUtils.generalContext;
	private String modelContext = ContextUtils.modelContext;
	private String indexContext = ContextUtils.indexContext;
	private String gainContext = ContextUtils.gainContext;
	private String sensContext = ContextUtils.sensitivityContext;
	private final String imageExtension = ".gif";
	private final String imageWidthForResults = "660px";

	private String timeVar = genContext + "t";
	private String normalSize;

	private Model m = null;
	private SimConfig simConfig;

	private MathBuffer mathBuffer = new MathBuffer();
	private ExportMathCommands exportMathCommands = new ExportMathCommands();
	private String mathCommand, mathCommand2, mathCommand3;
	private String sbmlDataString;
	private long startNanoTime;

	public SimulationCtrl(SessionData sessionData) {
		this.sessionData = sessionData;
		this.outVL = this.sessionData.getOutVL();
	}

	private String getChopTolerance() {
		return "";
	}

	private void executeMathBuffer() throws Exception {
//		p.p(mathBuffer.getString());
		if (SharedData.enableMathExecution) {
			mathLink.evaluate(mathBuffer.getString());
		}
		mathBuffer.reset();
	}

	private void bufferCommandButWithDifferenExportCmd(String mathCommand, String exportComm) {
		exportMathCommands.addCommand(exportComm);
		mathBuffer.addCommand(mathCommand);
	}

	private void bufferCommand(String mathCommand) {
		exportMathCommands.addCommand(mathCommand);
		mathBuffer.addCommand(mathCommand);
	}

	private void bufferMathTxt(String filename) throws CException {
		String txtContent = sharedData.getMathematicaCodeMap().get(filename);
		if (txtContent == null)
			throw new CException("Missing Mathematica code");
		exportMathCommands.addCommand(txtContent);
		mathBuffer.addCommandRaw(txtContent);
	}

	private String mathGetString(String mathCommand) throws Exception {
		String mOut = "Null";
		if (SharedData.enableMathExecution) {
			mOut = mathLink.evaluateToString(mathCommand);
		}
		exportMathCommands.addCommand(mathCommand + ";");
		return mOut;
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
				+ ((Boolean) simConfig.getPlotSettings().get("FontWeight").getValue() ? "Bold" : "Plain")
				+ ", FontSlant -> "
				+ ((Boolean) simConfig.getPlotSettings().get("FontSlant").getValue() ? "Italic" : "Plain")
				+ ", FontSize -> " + simConfig.getPlotSettings().get("FontSize").getValue() + "}, PlotLegends->"
				+ plotLegends
				+ ", Axes -> False, Frame -> True, FrameTicks -> True, PlotStyle -> Table[{Dashing[0.03 k1/Length["
				+ plotList + "]], Thickness[" + simConfig.getPlotSettings().get("LineThickness").getValue()
				+ "]}, {k1, 1, Length[" + plotList + "]}], FrameLabel -> {\"" + xLabel + "\", \"" + yLabel
				+ "\"}, PlotRange->Full, ImageSize->" + normalSize + "]";
		showImageOfMathCmd(cmd, true, fileName, imageWidthForResults);
	}

	private void showImageOfMathCmd(String cmd, boolean addToGrid, String fileName, String width) throws Exception {
		cmd = "Rasterize[" + cmd + ",RasterSize->2000,ImageResolution->72]";
		exportMathCommands.addCommand(cmd);
		if (SharedData.enableMathExecution) {
			byte[] mImage = mathLink.evaluateToImage(cmd);
			outVL.out(fileName, addToGrid, mImage, width);
		}
	}

	private void showTableTxtDownloadButton(String mathCommand, String caption, String filename) throws Exception {
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
		outVL.outFile(caption, "", filename, fileStringBuilder.toString(), null, false);
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
		showImageOfMathCmd(mathCommand, false, tableName + "-" + m.getName() + imageExtension, imageWidthForResults);
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
		if (SharedData.enableMathExecution) {
			// p.p(mathLink.evaluateToString("JLink`$FrontEndLaunchCommand =
			// FileNameJoin[{$InstallationDirectory, \"Mathematica.exe\"}]"));
//			mathLink.evaluate("JLink`ConnectToFrontEnd[]");
//			mathLink.evaluate("JLink`UseFrontEnd[1+1]");
//			mathLink.evaluate("JLink`$DefaultImageFormat = \"JPEG\"");
		}
	}

	private void executeEndMathCommands() throws Exception {
		if (SharedData.enableMathExecution) {
//			mathLink.evaluate("JLink`CloseFrontEnd[]");
		}
	}

	private void outGeneratedFiles() throws Exception {
		exportMathCommands.end();
		outVL.out("Generated Files", "textH2");
		outVL.outNewHorizontalLayout();
		outVL.outFile("Mathematica Notebook",
				"Download the Mathematica Notebook including the model and the simulation", m.getName() + ".nb",
				exportMathCommands.getString(), null, true);
		if (SharedData.enableMathExecution) {
			try {
				generateSBML();
				outVL.outFile("SBML", "Download the SBML file including the model", m.getName() + ".xml",
						sbmlDataString, null, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void generateSBML() throws Exception {
		SBMLMan sbmlMan = new SBMLMan(sessionData);
		sbmlDataString = sbmlMan.exportSBML(m, mathLink);
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

	private void showSteadyStateSimulationGridAndButton(String caption, String filename,
			TreeMap<String, String> ssMap) {
		String header1 = "Variable     "; // spaces will be added by method if needed
		String header2 = "Steady State Value";
		// find variable with longest name length
		int maxVarLength = 0;
		for (String key : ssMap.keySet())
			if (key.length() > maxVarLength)
				maxVarLength = key.length();
		// add missing spaces to header 1
		int spacesToAdd = maxVarLength - header1.length() + 1; // DO NOT DELETE
																// THIS!
		for (int i = 0; i < spacesToAdd; i++)
			header1 += " ";
		// replace "[t] -> " for the required number of spaces
		// get number of real lines
		int numRealLines = ssMap.size();
		GridLayout gl = new GridLayout(2, numRealLines + 1);
		gl.addStyleName("table");
		gl.setSpacing(false);
		gl.setMargin(false);
		gl.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		gl.addComponent(new SpacedLabel(header1.replace(" ", "")), 0, 0);
		gl.addComponent(new SpacedLabel(header2), 1, 0);
		int j = 1;
		String file2Download = header1 + header2 + sharedData.getNewLine();
		String spaces;
		String val = "";
		for (String key : ssMap.keySet()) {
			val = ssMap.get(key) != null ? ssMap.get(key) : "-";
			// species
			// create spaces
			spaces = "";
			for (int is = 0; is < header1.length() - key.length(); is++)
				spaces += " ";
			file2Download += key + spaces;
			gl.addComponent(new SpacedLabel(key), 0, j);
			// concentration value
			file2Download += val + sharedData.getNewLine();
			if (!"".equals(val))
				gl.addComponent(new SpacedLabel(val), 1, j);

			j++;
		}
		outVL.out(gl);
		outVL.outFile(caption, "", filename, file2Download, null, false);
	}

	public void checkCancel() throws Exception {
		if (sessionData.isSimCancel()) {
			throw new Exception("Cancelled");
		}
	}

	///////////
	public void simulate() throws Exception {
//		m = new Model(sessionData.getSelectedModel(), 0);
		checkCancel();
		this.mathLink = sessionData.getMathLinkOp();
		checkCancel();
		m = sessionData.getSelectedModel();
		if (sharedData.isDebug())
			startNanoTime = System.nanoTime();
		checkCancel();
		executeInitMathCommands();
		m.checkMathExpressions(mathLink);
		simConfig = m.getSimConfig();
//			sessionData.getOutVL().out("(click image/s to enlarge)", "textSmall");
		checkCancel();
		initSimulation();
//		System.out.println("1");
		checkCancel();
		if (simConfig.getSimType() == SimType.DETERMINISTIC) {
			initDynamicSimulation();
			if ((Boolean) simConfig.getDynamic().get("Enable").getValue()) {
				dynamicSimulation();
			}
			if ((Boolean) simConfig.getSteadyState().get("Enable").getValue()) {
				steadyStateSimulation();
			}
		} else if (simConfig.getSimType() == SimType.STOCHASTIC) {
			stochasticSimulation();
		}
		checkCancel();
		executeEndMathCommands();
		checkCancel();
		outGeneratedFiles();
		exportMathCommands.reset();
		mathBuffer.reset();
		checkCancel();
		outVL.finish();
		if (sharedData.isDebug())
			Utils.debug("Sim took " + ((double) ((System.nanoTime() - startNanoTime) / 1000000000d)) + "s");
	}

	public boolean quickStochasticSimulationCheck() throws Exception {
		if (!SharedData.enableMathExecution)
			return false;
		this.mathLink = this.sessionData.getMathLinkOp();
		if (sharedData.isDebug())
			startNanoTime = System.nanoTime();
		m = sessionData.getSelectedModel();
		m.checkMathExpressions(mathLink);
		simConfig = m.getSimConfig();
		simConfig.checkAndAdaptToSimulate(m);
		initSimulation();
		bufferCommand("cellSize = " + String.valueOf(
				CellSizes.getInstance().nameToNum((String) simConfig.getStochastic().get("CellSize").getValue())));
		bufferMathTxt("quickStochasticCheck.txt");
		executeMathBuffer();
		if (sharedData.isDebug())
			Utils.debug("QuickStSim took " + ((double) ((System.nanoTime() - startNanoTime) / 1000000000d)) + "s");
		if (mathGetString("isSSAPassOK").equals("False"))
			throw new Exception("Model is not valid for stochastic simulation");
		return mathGetString("isTauLeapingEffective").equals("True");
	}

	private void initDynamicSimulation() {
		normalSize = (String) simConfig.getPlotSettings().get("ImageSize").getValue().toString();
		mathCommand = genContext + "EqsToSolve = Join[Table[((" + genContext + "DepVars[[i]]))'[" + timeVar + "] == "
				+ genContext + "RateEqsAllSubs[[i]], {i, 1, Length[" + genContext + "DepVars]}], " + genContext
				+ "InitCondDepVars]";
		bufferCommand(mathCommand);
		if (((Boolean) simConfig.getDynamic().get("Sensitivities").getValue())
				|| ((Boolean) simConfig.getDynamic().get("Gains").getValue())
				|| (Boolean) simConfig.getSteadyState().get("Enable").getValue()) {
			mathCommand = genContext + "Jac = Outer[D, " + genContext + "RateEqsWithT, " + genContext + "DepVarsWithT]";
			bufferCommand(mathCommand);
		}
	}

	private void initSimulation() throws CException {
//		bufferBeginContext();
		exportMathCommands.addCommand("(*Model: " + m.getName() + " (Generated with " + SharedData.fullAppName + ")*)");
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
			outVL.out("Error creating Stoichiometric Matrix" + e.getMessage());
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
			Map<String, FormulaValue> valuesByReaction = r.getFormulaGenPars();
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
			Map<String, FormulaValue> valuesByReaction = r.getFormulaGenPars();
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
			Map<String, FormulaValue> valuesByReaction = r.getFormulaGenPars();
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
			Map<String, FormulaValue> valuesByReaction = r.getFormulaGenPars();
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

	@SuppressWarnings("unchecked")
	private void plotDynamicSim(String ndSolveVar) throws Exception {
		outVL.outNewGridLayout(2, 1);
		for (SimConfigArray plotView : simConfig.getDynamic_PlotViews()) {
			mathCommand = genContext + "plotVars = {";
			mathCommand2 = genContext + "plotLegends = PointLegend[{";
			for (String dvToShow : (ArrayList<String>) plotView.get("DepVarsToShow").getValue()) {
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
					"dyn-" + m.getName() + imageExtension);
		}

		mathCommand = "tableHeader={Join[{\"t\"}, DepVarsString]};\r\n" + "txtTable= Join[tableHeader,"
				+ " Table[Join[{timeI},Table[ToStringNumber[DepVars[[i]][timeI] /. " + ndSolveVar
				+ "], {i, Length[DepVars]}]], {timeI, ti, tf, tStep}]] // TableForm";
		showTableTxtDownloadButton(mathCommand, "Download Dynamic Simulation Table", "dynamic-" + m.getName() + ".txt");
	}

	@SuppressWarnings("unchecked")
	private void dynamicSimulation() throws Exception {
		// model must have been checked before calling this function
		outVL.out("Dynamic Simulation", "textH2");
		mathCommand = genContext + "ti = " + (String) simConfig.getDynamic().get("Ti").getValue();
		bufferCommand(mathCommand);
		mathCommand = genContext + "tf = " + (String) simConfig.getDynamic().get("Tf").getValue();
		bufferCommand(mathCommand);
		mathCommand = genContext + "tStep = " + (String) simConfig.getDynamic().get("TStep").getValue();
		bufferCommand(mathCommand);
		if (!((Boolean) simConfig.getDynamic().get("Sensitivities").getValue())
				&& !((Boolean) simConfig.getDynamic().get("Gains").getValue())) {
			bufferCommand("$t0 = AbsoluteTime[]");
			bufferCommand(genContext + "Sols = First@NDSolve[" + genContext + "EqsToSolve, " + genContext + "DepVars, {"
					+ timeVar + ", " + genContext + "ti, " + genContext + "tf}]");
			bufferCommand("Print[\"Execution time=\",AbsoluteTime[]-$t0,\"s\"]");
			executeMathBuffer();
			plotDynamicSim(genContext + "Sols");
		}
		// DYNAMIC GAINS
		if ((Boolean) simConfig.getDynamic().get("Gains").getValue()) {
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
			outVL.out("Absolute Dynamic Gains", "textH2");
			outVL.outNewGridLayout(2, 1);
			for (SimConfigArray plotView : simConfig.getDynamic_PlotViews()) {
				mathCommand = "AG = {";
				mathCommand2 = "plotLegends = PointLegend[{";
				for (String dvToShow : (ArrayList<String>) plotView.get("DepVarsToShow").getValue()) {
					for (String ind : m.getAllSpeciesConstant().keySet()) {
						mathCommand += gainContext + dvToShow + ind + "[" + timeVar + "],";
						mathCommand2 += "G" + dvToShow + ind + ",";
					}
				}
				mathCommand = removeLastComma(mathCommand);
				mathCommand += "}";
				mathCommand2 = removeLastComma(mathCommand2);
				mathCommand2 += "}]";
				bufferCommand(mathCommand);
				bufferCommand(mathCommand2);
				executeMathBuffer();
				executePlot("AG", "plotLegends", "NAugSol", "t", "Gain",
						"dyn-abs-gains-" + m.getName() + imageExtension);
			}

			// mathCommand = "RG = Table[DynGainVars[[i]]*IndVars/DepVarsWithT[[i]], {i,
			// 1,Length["
			// + "DynGainVars]}] /. IndVarVals";

			outVL.out("Relative Dynamic Gains", "textH2");
			outVL.outNewGridLayout(2, 1);
			for (SimConfigArray plotView : simConfig.getDynamic_PlotViews()) {
				mathCommand = "RG = {";
				mathCommand2 = "plotLegends = PointLegend[{";
				for (String dvToShow : (ArrayList<String>) plotView.get("DepVarsToShow").getValue()) {
					for (String ind : m.getAllSpeciesConstant().keySet()) {
						mathCommand += gainContext + dvToShow + ind + "[" + timeVar + "]*"
								+ m.getAllSpecies().get(ind).getConcentration() + " / " + modelContext + dvToShow + "["
								+ timeVar + "],";
						mathCommand2 += "G" + dvToShow + ind + ",";
					}
				}
				mathCommand = removeLastComma(mathCommand);
				mathCommand += "}";
				mathCommand2 = removeLastComma(mathCommand2);
				mathCommand2 += "}]";
				bufferCommand(mathCommand);
				bufferCommand(mathCommand2);
				executeMathBuffer();
				executePlot("RG", "plotLegends", "NAugSol", "t", "Relative Gain",
						"dyn-rel-gains-" + m.getName() + imageExtension);
			}

		}
		// DYNAMIC SENSITIVITIES
		if (((Boolean) simConfig.getDynamic().get("Sensitivities").getValue())) {
			mathCommand = "DynSensiVars = {";
			mathCommand2 = "DynSensiVarsDiff = {";
			for (String dep : m.getAllSpeciesTimeDependent().keySet()) {
				mathCommand += "{";
				mathCommand2 += "{";
				for (Reaction r : m) {
					for (String parName : r.getFormulaGenPars().keySet()) {
						if (r.getFormulaGenPars().get(parName) != null && r.getFormulaGenPars().get(parName).isFilled()
								&& r.getFormulaGenPars().get(parName).getType() == FormulaValueType.CONSTANT) {
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
			if (!((Boolean) simConfig.getDynamic().get("Gains").getValue())) {
				plotDynamicSim("NAugSol");
			}

			outVL.out("Absolute Dynamic Sensitivities", "textH2");
			outVL.outNewGridLayout(2, 1);
			for (SimConfigArray plotView : simConfig.getDynamic_PlotViews()) {
				mathCommand = "AS = {";
				mathCommand2 = "plotLegends = PointLegend[{";
				for (String dvToShow : (ArrayList<String>) plotView.get("DepVarsToShow").getValue()) {
					for (Reaction r : m) {
						for (String parName : r.getFormulaGenPars().keySet()) {
							if (r.getFormulaGenPars().get(parName) != null
									&& r.getFormulaGenPars().get(parName).isFilled()
									&& r.getFormulaGenPars().get(parName).getType() == FormulaValueType.CONSTANT) {
								mathCommand += sensContext + dvToShow + r.getMathematicaContext() + parName + "["
										+ timeVar + "],";
								mathCommand2 += "ToString[S" + dvToShow + r.getIdJavaStr() + parName + "],";
							}
						}
						for (String parName : r.getFormulaSubstratesArrayParameters().keySet()) {
							for (String sp : r.getFormulaSubstratesArrayParameters().get(parName).keySet()) {
								if (r.getFormulaSubstratesArrayParameters().get(parName).get(sp) != null
										&& r.getFormulaSubstratesArrayParameters().get(parName).get(sp).isFilled()) {
									mathCommand += sensContext + dvToShow + r.getMathematicaContext()
											+ ContextUtils.arrayContext + parName + "`" + ContextUtils.substrateContext
											+ sp + "[" + timeVar + "],";
									mathCommand2 += "ToString[S" + dvToShow + r.getIdJavaStr() + parName + "Sub" + sp
											+ "],";
								}
							}
							for (String sp : r.getFormulaModifiersArrayParameters().get(parName).keySet()) {
								if (r.getFormulaModifiersArrayParameters().get(parName).get(sp) != null
										&& r.getFormulaModifiersArrayParameters().get(parName).get(sp).isFilled()) {
									mathCommand += sensContext + dvToShow + r.getMathematicaContext()
											+ ContextUtils.arrayContext + parName + "`" + ContextUtils.modifierContext
											+ sp + "[" + timeVar + "],";
									mathCommand2 += "ToString[S" + dvToShow + r.getIdJavaStr() + parName + "Mod" + sp
											+ "],";
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
				executePlot("AS", "plotLegends", "NAugSol", "t", "Sensitivity",
						"dyn-abs-sens-" + m.getName() + imageExtension);
			}

			// mathCommand = "RS = Table[DynSensiVars[[i]]*Pars/DepVarsWithT[[i]], {i,
			// 1,Length["
			// + "DynSensiVars]}] /.ParNumVals";
			// bufferCommand(mathCommand);
			// executeMathBuffer();
			outVL.out("Relative Dynamic Sensitivities", "textH2");
			outVL.outNewGridLayout(2, 1);
			for (SimConfigArray plotView : simConfig.getDynamic_PlotViews()) {
				mathCommand = "RS = {";
				mathCommand2 = "plotLegends = PointLegend[{";
				for (String dvToShow : (ArrayList<String>) plotView.get("DepVarsToShow").getValue()) {
					for (Reaction r : m) {
						for (String par : r.getFormulaGenPars().keySet()) {
							if (r.getFormulaGenPars().get(par) != null && r.getFormulaGenPars().get(par).isFilled()
									&& r.getFormulaGenPars().get(par).getType() == FormulaValueType.CONSTANT) {
								mathCommand += sensContext + dvToShow + r.getMathematicaContext() + par + "[" + timeVar
										+ "]*" + r.getFormulaGenPars().get(par).getStringValue() + "/" + modelContext
										+ dvToShow + "[" + timeVar + "],";
								mathCommand2 += "ToString[S" + dvToShow + par + "],";
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
									mathCommand2 += "ToString[S" + dvToShow + r.getIdJavaStr() + parName + "Sub" + sp
											+ "],";
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
									mathCommand2 += "ToString[S" + dvToShow + r.getIdJavaStr() + parName + "Mod" + sp
											+ "],";
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
				executePlot("RS", "plotLegends", "NAugSol", "t", "Relative Sens.",
						"dyn-rel-sens-" + m.getName() + imageExtension);
			}

		}
		// PARAMETER SCAN
		if (simConfig.getDynamic_ParameterScan().hasParametersToScan(m)) {
			outVL.out("Dynamic Parameter Scan", "textH2");
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
					outVL.outNewGridLayout(2, m.getAllSpeciesTimeDependent().size());
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
								+ ((Boolean) simConfig.getPlotSettings().get("FontWeight").getValue() ? "Bold"
										: "Plain")
								+ ", FontSlant -> "
								+ ((Boolean) simConfig.getPlotSettings().get("FontSlant").getValue() ? "Italic"
										: "Plain")
								+ ", " + "FontSize -> " + simConfig.getPlotSettings().get("FontSize").getValue()
								+ "}, PlotLegends -> plotLegends, Axes -> False, "
								+ "Frame -> True, FrameTicks -> {{True,False},{True,False}}, " + "  PlotStyle -> "
								+ "Table[{Dashing[0.03*(k1/Length[plotVars])], " + "Thickness["
								+ simConfig.getPlotSettings().get("LineThickness").getValue()
								+ "]}, {k1, Length[plotVars]}], "
								+ "FrameLabel -> {\"t\", StringJoin[plotVar,\" (concentration)\"]}, "
								+ "PlotRange -> Full, ImageSize -> " + normalSize + "]";
						showImageOfMathCmd(mathCommand, true, "dyn-parscan" + i + "-" + m.getName() + imageExtension,
								imageWidthForResults);
					}
				}
			}
		}
	}

	private void stochasticSimulation() throws Exception {
		Boolean isTauLeaping = (Boolean) simConfig.getStochastic().get("TauLeaping").getValue();
		normalSize = (String) simConfig.getPlotSettings().get("ImageSize").getValue().toString();
		String head = "Stochastic Simulation";
		if (isTauLeaping)
			head += " (Method=Tau-leaping;";
		else
			head += " (Method=SSA;";
		head += " Final time=" + (String) simConfig.getStochastic().get("Tf").getValue() + ")";
		outVL.out(head, "textH2");
		outVL.outNewStochasticGrid(Integer.valueOf((String) simConfig.getStochastic().get("Iterations").getValue()),
				isTauLeaping);
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
				genContext + "stochasticReps = " + (String) simConfig.getStochastic().get("Iterations").getValue());
		bufferCommand(genContext + "cellSize = " + String.valueOf(
				CellSizes.getInstance().nameToNum((String) simConfig.getStochastic().get("CellSize").getValue())));
		bufferMathTxt("stochastic-tau-leaping.txt");
		executeMathBuffer();

//		if (SharedData.enableMathExecution
//				&& mathGetString("Catch[If[Length[stPlotLists] < 1,Throw[\"error\"]]]").equals("error"))
//			throw new CException("Stochastic simulation procedure error");
//		boolean isMoreThanOneIteration = SharedData.enableMathExecution
//				&& mathGetString("Catch[If[Dimensions[stPlotLists][[2]]>1,Throw[\"CalculateNoise\"]]]")
//						.equals("CalculateNoise");
//		if (isMoreThanOneIteration) {
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
		if (SharedData.enableMathExecution) {
			Integer numPlotsByDepVars = Integer.valueOf(mathGetString("Length[stPlotLists]"));
			boolean isPlotNoise = mathGetString("ValueQ[NoiseQ025]").equals("True");
			plotStochasticSim(numPlotsByDepVars, isPlotNoise);

			mathCommand = "tableHeader = {Flatten[{\"t\"," + "    Table[Table["
					+ "      DepVarsString[[k]] <> \"_\" <> ToString[k2], {k2, 1,"
					+ "       Length[stPlotLists[[k]]]}], {k, 1, Length[stPlotLists]}]}]};\r\n"
					+ "txtTable=Join[tableHeader, " + " Transpose[" + "  Join[{stPlotLists[[1]][[1]][\"Times\"]},"
					+ "   Flatten[Table[" + "     Table[stPlotLists[[k]][[k2]][\"Values\"], {k2, 1,"
					+ "       Length[stPlotLists[[k]]]}], {k, 1, Length[stPlotLists]}], 1]]]] // TableForm";
			showTableTxtDownloadButton(mathCommand, "Download Simulation Table", "stochastic-" + m.getName() + ".txt");
		}
	}

	private void stochasticSSA() throws Exception {
		bufferCommand("ti = " + (String) simConfig.getStochastic().get("Ti").getValue());
		bufferCommand("tf = " + (String) simConfig.getStochastic().get("Tf").getValue());
		bufferCommand("tStep = (tf-ti)/1000");
		bufferCommand(
				genContext + "stochasticReps = " + (String) simConfig.getStochastic().get("Iterations").getValue());
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
		Integer numPlotsByDepVars = Integer.valueOf(mathGetString("Length[stPlotLists]"));
		boolean isPlotNoise = mathGetString("ValueQ[NoiseQ025]").equals("True");
		plotStochasticSim(numPlotsByDepVars, isPlotNoise);

		mathCommand = "tableHeader = {Flatten[{\"t\"," + "    Table[Table["
				+ "      DepVarsString[[k]] <> \"_\" <> ToString[k2], {k2, 1,"
				+ "       Length[stPlotLists[[k]]]}], {k, 1, Length[stPlotLists]}]}]};\r\n"
				+ "txtTable=Join[tableHeader, " + " Transpose[" + "  Join[{stPlotLists[[1]][[1]][\"Times\"]},"
				+ "   Flatten[Table[" + "     Table[stPlotLists[[k]][[k2]][\"Values\"], {k2, 1,"
				+ "       Length[stPlotLists[[k]]]}], {k, 1, Length[stPlotLists]}], 1]]]] // TableForm";
		showTableTxtDownloadButton(mathCommand, "Download Simulation Table", "stochastic-" + m.getName() + ".txt");
	}

	private void plotStochasticSim(Integer numPlotsByDepVars, boolean isPlotNoise) throws Exception {
		bufferCommand("Print[\"" + MathPacketListenerOp.printPrefix + "Plotting graphics...\"];");
		executeMathBuffer();
		outVL.outNewGridLayout(1, 1);
		outVL.setCaptionToGridLayout("All iterations medians plot");
		executeListLinePlot("NoiseMedian", "\"t\"", "\"# Molecules \"",
				"PointLegend[Table[DepVarsString[[i]],{i,Length@DepVarsString}],LegendMarkers->Graphics@Disk[]]",
				"stoch-" + m.getName() + imageExtension);
		if (isPlotNoise) {
			ArrayList<String> depVarList = m.getDepVarArrayList();
			for (int i = 1; i <= numPlotsByDepVars; i++) {
				outVL.outNewGridLayout(2, 3);
				outVL.setCaptionToGridLayout("Graphics related to: " + depVarList.get(i - 1));
				executeListLinePlot("stPlotLists[[" + i + "]]", "\"t\"", "\"# Molecules \"<>DepVarsString[[" + i + "]]",
						"{}", "stoch-" + depVarList.get(i - 1) + "-" + m.getName() + imageExtension);
				executeListLinePlot("{NoiseQ025[[" + i + "]],NoiseMedian[[" + i + "]],NoiseQ075[[" + i + "]]}", "\"t\"",
						"\"# Molecules \"<>DepVarsString[[" + i + "]]",
						"Placed[PointLegend[{\"Quantile_0.25\", \"Median\", \"Quantile_0.75\"},LegendMarkers->Graphics@Disk[]],Below]",
						"stoch-noise" + depVarList.get(i - 1) + "-" + m.getName() + imageExtension);
				executeListLinePlot("{PIN[[" + i + "]],NPIN[[" + i + "]]}", "\"t\"",
						"\"Coefficient of Variation \"<>DepVarsString[[" + i + "]]",
						"Placed[PointLegend[{\"Parametric [\\[Sigma]/\\[Mu]]\", \"Non parametric [(Quantile_0.75-Quantile_0.25)/Median]\"},LegendMarkers->Graphics@Disk[]], Below]",
						"stoch-var-" + depVarList.get(i - 1) + "-" + m.getName() + imageExtension);
			}
		}
	}

	private void executeListLinePlot(String symbolToPlot, String xLabel, String yLabel, String legend, String fileName)
			throws Exception {
		String cmd = "ListLinePlot[" + symbolToPlot + ", LabelStyle -> {" + "FontWeight -> "
				+ ((Boolean) simConfig.getPlotSettings().get("FontWeight").getValue() ? "Bold" : "Plain")
				+ ", FontSlant -> "
				+ ((Boolean) simConfig.getPlotSettings().get("FontSlant").getValue() ? "Italic" : "Plain")
				+ ", FontSize -> " + simConfig.getPlotSettings().get("FontSize").getValue() + "}, PlotLegends->"
				+ legend + ", Axes -> False, Frame -> True, FrameTicks -> True, PlotStyle -> Thickness["
				+ simConfig.getPlotSettings().get("LineThickness").getValue() + "], FrameLabel -> {" + xLabel + ", "
				+ yLabel + "}, PlotRange->Full, ImageSize->" + normalSize + "]";
		showImageOfMathCmd(cmd, true, fileName, imageWidthForResults);
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

	@SuppressWarnings("unchecked")
	private void steadyStateSimulation() throws Exception {
		// model must have been checked before calling this function
		TreeMap<String, String> ssMap = new TreeMap<>();
		executeMathBuffer();
		outVL.out("Steady State Simulation", "textH2");

		bufferCommand("thresholdCondition=" + simConfig.getSteadyState().get("Threshold"));
		bufferMathTxt("steady-state.txt");
		executeMathBuffer();
		String steadyStateType = mathGetString("typeSS");
		if (steadyStateType.equals("stable") || steadyStateType.equals("unstable")) {
			fillSteadyStateSimulationMap(genContext + "SSSol", ssMap);
			showSteadyStateSimulationGridAndButton("Download Steady State Simulation",
					"steadystate-" + m.getName() + ".txt", ssMap);
			if (((Boolean) simConfig.getSteadyState().get("Stability").getValue())) {
				outVL.out("Stability Analysis", "textH2");
				if (steadyStateType.equals("stable")) {
					outVL.out("Stable: All real parts are negative");
				} else if (steadyStateType.equals("unstable")) {
					outVL.out("Unstable: At least one real part is non-negative");
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

		if (steadyStateType.equals("stable")) {
			// Steady State Gains (D=>derivative)
			if (((Boolean) simConfig.getSteadyState().get("Gains").getValue())) {
				mathCommand = "JacIV = Outer[D, " + "RateEqsWithT, " + "IndVars]";
				bufferCommand(mathCommand);
				mathCommand = "NJacIV = Chop[" + "JacIV /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]"
						+ getChopTolerance() + "]";
				bufferCommand(mathCommand);
				mathCommand = "GA = -Inverse[" + "Jac /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]]."
						+ "NJacIV";
				bufferCommand(mathCommand);
				outVL.out("Absolute Steady State Gains", "textH2");
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
				outVL.out("Relative Steady State Gains", "textH2");
//				mathCommand = "DepVarsToShow = {";
//				for (String dv : simConfig.getUnifiedDepVarsFromPlotViews().keySet())
//					mathCommand += dv + ",";
//				mathCommand = removeLastComma(mathCommand);
//				mathCommand += "}";
//				bufferCommand(mathCommand);
				executeMathTable("GR", "IndVarsString", "DepVarsToShow", true);
			}
			// Steady State Sensitivities
			if (((Boolean) simConfig.getSteadyState().get("Sensitivities").getValue())) {
				mathCommand = "JacP = Outer[D, " + "RateEqsWithT, " + "Pars]";
				bufferCommand(mathCommand);
				mathCommand = "NJacP = Chop[" + "JacP /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]"
						+ getChopTolerance() + "]";
				bufferCommand(mathCommand);
				mathCommand = "SSSA = -Inverse[" + "Jac /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]]."
						+ "NJacP";
				bufferCommand(mathCommand);
				outVL.out("Absolute Steady State Sensitivities", "textH2");
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
				outVL.out("Relative Steady State Sensitivities", "textH2");
				executeMathTable("SSSR", "ParsString", "DepVarsToShow", true);
			}
		}
		// PARAMETER SCAN
		if (simConfig.getSteadyState_ParameterScan().hasParametersToScan(m)) {
			outVL.out("Steady State Parameter Scan", "textH2");
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
					outVL.outNewGridLayout(2, m.getAllSpeciesTimeDependent().size());
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
									+ ((Boolean) simConfig.getPlotSettings().get("FontWeight").getValue() ? "Bold"
											: "Plain")
									+ ", FontSlant -> "
									+ ((Boolean) simConfig.getPlotSettings().get("FontSlant").getValue() ? "Italic"
											: "Plain")
									+ ", " + "FontSize -> " + simConfig.getPlotSettings().get("FontSize").getValue()
									+ "}, PlotLegends -> plotLegends, Axes -> False, "
									+ "Frame -> True, FrameTicks -> {{True,False},{True,False}}, "
									+ "PlotStyle -> {RGBColor[0.18, 0.63, 1], Hue[0, 0.54, 1]}, "
									+ "FrameLabel -> {ToString[paramName], " + "DepVarsString[[" + i
									+ "]] <> \" (concentration)\"}, PlotRange -> Full, " + "ImageSize -> " + normalSize
									+ "]";
							showImageOfMathCmd(mathCommand, true, "ss-parscan" + i + "-" + m.getName() + imageExtension,
									imageWidthForResults);
						}
					} else {
						outVL.addToGridLayout(
								new Label("No Steady States found for " + entry.getMathematicaParamName()));
					}
				}
			}
		}
	}
}
