package cat.udl.easymodel.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.logic.model.FormulaValue;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.simconfig.SimConfigArray;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathLink;
import cat.udl.easymodel.sbml.SBMLMan;
import cat.udl.easymodel.utils.CException;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.utils.buffer.ExportMathCommands;
import cat.udl.easymodel.utils.buffer.ExportMathCommandsImpl;
import cat.udl.easymodel.utils.buffer.MathBuffer;
import cat.udl.easymodel.utils.buffer.MathBufferImpl;
import cat.udl.easymodel.vcomponent.common.SpacedLabel;
import cat.udl.easymodel.vcomponent.results.OutVL;

public class SimulationCtrlImpl implements SimulationCtrl {
	// External resources
	private SessionData sessionData = null;
	private SharedData sharedData = SharedData.getInstance();
	private OutVL outVL;
	private MathLink mathLink;

	private String genContext = ContextUtils.generalContext;
	private String modelContext = ContextUtils.modelContext;
	private String indexContext = ContextUtils.indexContext;
	private String gainContext = ContextUtils.gainContext;
	private String sensContext = ContextUtils.sensitivityContext;

	private String timeVar = genContext + "t";
	private String bigSize;
	private String smallSize;

	private Model m = null;
	private SimConfig simConfig;

	private MathBuffer mathBuffer = new MathBufferImpl();
	private ExportMathCommands exportMathCommands = new ExportMathCommandsImpl();
	private String mathCommand, mathCommand2, mathCommand3;
	private String sbmlDataString;
	private long startNanoTime;

	public SimulationCtrlImpl(SessionData sessionData) {
		this.sessionData = sessionData;
		this.outVL = this.sessionData.getOutVL();
		this.mathLink = this.sessionData.getMathLinkOp();
	}

	private String getChopTolerance() {
		return "";
	}

	private void executeMathBuffer() throws MathLinkException {
		mathLink.evaluate(mathBuffer.getString());
		mathBuffer.reset();
	}

	private void addCommandToMathBufferDiffExport(String mathCommand, String exportComm) {
		exportMathCommands.addCommand(exportComm);
		mathBuffer.addCommand(mathCommand);
	}

	private void bufferCommand(String mathCommand) {
		exportMathCommands.addCommand(mathCommand);
		mathBuffer.addCommand(mathCommand);
	}

	private void bufferCheckCommand(String mathCommand) {
		exportMathCommands.addCommand(mathCommand);
		mathBuffer.addCommand(mathCommand);
	}

	private String executeMathCommandString(String mathCommand) throws MathLinkException {
		String mOut = mathLink.evaluateToString(mathCommand);
		exportMathCommands.addCommand(mathCommand);
		return mOut;
	}

	private Boolean executeMathCommandBoolean(String mathCommand) throws MathLinkException {
		Boolean mOut = mathLink.evaluateToBoolean(mathCommand);
		exportMathCommands.addCommand(mathCommand);
		return mOut;
	}

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

	private void executeFunctionGraphicGrid(String plotList, String plotLegends, String ndSolveVar, String xLabel,
			String yLabel, boolean addToGrid, String fileName, String smallSize2, String bigSize2)
			throws MathLinkException {
		String cmd = genContext + "PlotVars = " + plotList + "; Plot[Evaluate[" + genContext + "PlotVars /. "
				+ ndSolveVar + "], {" + timeVar + "," + genContext + "ti," + genContext
				+ "tf}, LabelStyle -> {FontFamily -> Arial, FontWeight -> "
				+ ((Boolean) simConfig.getPlotSettings().get("FontWeight").getValue() ? "Bold" : "Plain")
				+ ", FontSlant -> "
				+ ((Boolean) simConfig.getPlotSettings().get("FontSlant").getValue() ? "Italic" : "Plain")
				+ ", FontSize -> " + simConfig.getPlotSettings().get("FontSize").getValue() + "}, PlotLegends->"
				+ plotLegends + ", Axes -> False, Frame -> True, PlotStyle -> Table[{Dashing[0.03 k1/Length["
				+ genContext + "PlotVars]], Thickness[" + simConfig.getPlotSettings().get("LineThickness").getValue()
				+ "]}, {k1, 1, Length[" + genContext + "PlotVars]}], FrameLabel -> {\"" + xLabel + "\", \"" + yLabel
				+ "\"}, PlotRange->Full]";
		executeMathCommandImage(cmd, addToGrid, fileName, smallSize2, bigSize2, true);
	}

	private void executeMathCommandImage(String mathCommand, boolean addToGrid, String fileName, String smallSize,
			String bigSize, boolean isPlotCommand) throws MathLinkException {
		String normalSizeCmd;
		String bigSizeCmd;
		if (isPlotCommand) {
			mathCommand = mathCommand.substring(0, mathCommand.length() - 1); // remove last "]"
			normalSizeCmd = mathCommand + ", ImageSize->" + smallSize + "]";
			bigSizeCmd = mathCommand + ", ImageSize->" + bigSize + "]";
		} else {
			normalSizeCmd = mathCommand;
			bigSizeCmd = mathCommand;
		}
		normalSizeCmd = "Rasterize[" + normalSizeCmd + ", ImageResolution->"
				+ simConfig.getPlotSettings().get("ImageResolution") + ", ImageSize->" + smallSize + "]";
		bigSizeCmd = "Rasterize[" + bigSizeCmd + ", ImageResolution->"
				+ simConfig.getPlotSettings().get("ImageResolution") + ", ImageSize->" + bigSize + "]";
		exportMathCommands.addCommand(normalSizeCmd);
		byte[] mImage = mathLink.evaluateToImage(normalSizeCmd);
		byte[] mImageBig = mathLink.evaluateToImage(bigSizeCmd);
		// get mImage height
		String imHeight = "";
		try {
			ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(mImage));
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (readers.hasNext()) {
				ImageReader reader = readers.next();
				reader.setInput(iis, true);
				imHeight = String.valueOf(reader.getHeight(0)) + "px";
				reader.dispose();
			}
			// readers.remove();
			// iis.close();
		} catch (IOException e1) {
			System.err.println("IO Exception");
			e1.printStackTrace();
			throw new MathLinkException(e1);
		}
		outVL.out(fileName, addToGrid, mImage, imHeight, mImageBig);
	}

	private void executeDynamicDownloadButton(String mathCommand, String caption, String filename)
			throws MathLinkException {
		String fileString = executeMathCommandString(mathCommand);
		// String sidReplace = "";
		// for (int i = 0; i < sid.length(); i++)
		// sidReplace += " ";
		// fileString = fileString.replace(sid, sidReplace);
		// fileString = fileString.replace(timeVar+sidReplace, timeVar);
		fileString = fileString.replace("\n", sharedData.getNewLine());
		// To prevent errors
		String fileString2 = "";
		for (String line : fileString.split(sharedData.getNewLine()))
			if (!line.equals(""))
				fileString2 += line + sharedData.getNewLine();
		fileString = null;
		outVL.outFile(caption, "", filename, fileString2, null, false);
	}

	private void executeMathTable(String tableName, String columnList, String rowList, boolean isBindTableToDepVars)
			throws MathLinkException {
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
				+ "gridColor},ItemStyle->Directive[FontSize->16],Frame->All,ItemSize->9]"; // , ImageSize ->
																							// Scaled[0.4]]";
		executeMathCommandImage(mathCommand, false, tableName + m.getName() + ".gif", smallSize, bigSize, false);
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

	private void endMathCommands() throws MathLinkException {
		outVL.out("Generated Files", "textH2");
		outVL.resetHorizontalLayout();
		exportMathCommands.end();
		outVL.outFile("Mathematica", "Save the Mathematica code to modify and run locally", m.getName() + ".nb",
				exportMathCommands.getString(), "exportMathematicaBtn", true);
		try {
			generateSBML();
			outVL.outFile("SBML", "Save the SBML file to use in other Systems Biology software", m.getName() + ".xml",
					sbmlDataString, "exportSbmlBtn", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		outVL.outHorizontalLayout();

//		bufferEndContext();
//		executeMathBuffer();
		exportMathCommands.reset();
	}

	private void generateSBML() throws Exception {
		sbmlDataString = SBMLMan.getInstance().exportSBML(m, mathLink);
	}

	/////////
	private void fillSteadyStateSimulationMap(String mathListName, TreeMap<String, String> ssMap)
			throws MathLinkException {
		mathCommand = mathListName + " // TableForm";
		String fileString = executeMathCommandString(mathCommand);
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

	// private void waitUntilMathKernelIsFree() throws Exception {
	// int i = 0, waitTimeSecs =
	// Integer.valueOf(sharedData.getProperties().getProperty("waitMathKernelSecs"));
	// while (executeMathCommandString("Context[]").compareTo("Global`") != 0) {
	// if (i < waitTimeSecs) {
	// if (i % 30 == 0) {
	// outVL.out("MathLink busy, please wait...");
	// System.out.println("Waiting to finish context: " +
	// executeMathCommandString("Context[]"));
	// }
	// TimeUnit.SECONDS.sleep(1);
	// i++;
	// } else {
	// exportMathCommands.reset();
	// throw new Exception("Time to wait MathLink exceeded. Please try again
	// later");
	// }
	// }
	// exportMathCommands.reset();
	// }

	///////////
	@Override
	public void simulate() throws CException, MathLinkException, Exception {
		try {
			if (sharedData.isDebug())
				startNanoTime = System.nanoTime();
			outVL.reset();
			m = sessionData.getSelectedModel();
			m.checkIfReadyToSimulate();
			mathLink.openMathLink();
			m.checkMathExpressions(mathLink);
			// validated
			simConfig = m.getSimConfig();
			smallSize = (String) simConfig.getPlotSettings().get("ImageSize-Small").getValue().toString();
			bigSize = (String) simConfig.getPlotSettings().get("ImageSize-Big").getValue().toString();
			// begin
			sessionData.getOutVL().out("Results for " + m.getName(), "textH1");
//			sessionData.getOutVL().out("(click image/s to enlarge)", "textSmall");
			initSimulation();
			if ((Boolean) simConfig.getDynamic().get("Enable").getValue()) {
				dynamicSimulation();
			}
			if ((Boolean) simConfig.getSteadyState().get("Enable").getValue()) {
				steadyStateSimulation();
			}
			endMathCommands();
		} catch (CException | MathLinkException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mathLink.closeMathLink();
		}
		if (sharedData.isDebug())
			Utils.debug("Sim took " + ((double) ((System.nanoTime() - startNanoTime) / 1000000000d)) + "s");
	}

	private void initSimulation() throws CException {
//		bufferBeginContext();
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
		mathCommand = genContext + "DepVarsConc={";
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
			Map<String, FormulaValue> valuesByReaction = r.getFormulaValues();
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
			Map<String, FormulaValue> valuesByReaction = r.getFormulaValues();
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
			Map<String, FormulaValue> valuesByReaction = r.getFormulaValues();
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
		// mathCommand = "RateEqsAllSubs = ReleaseHold[" +
		// "RateEqsAllSubs]";
		// executeMathCommand(mathCommand);
		mathCommand = genContext + "Jac = Outer[D, " + genContext + "RateEqsWithT, " + genContext + "DepVarsWithT]";
		bufferCommand(mathCommand);
		mathCommand = genContext + "EqsToSolve = Join[Table[((" + genContext + "DepVars[[i]]))'[" + timeVar + "] == "
				+ genContext + "RateEqsAllSubs[[i]], {i, 1, Length[" + genContext + "DepVars]}], " + genContext
				+ "DepVarsConc]";
		bufferCommand(mathCommand);
	}

	private void bufferParsString() {
		mathCommand = genContext + "ParsString={";
		for (Reaction r : m) {
			// CONSTANT VALUES BY REACTIONS (ig a)
			Map<String, FormulaValue> valuesByReaction = r.getFormulaValues();
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
	private void plotDynamicSim(String ndSolveVar) throws MathLinkException {
		outVL.resetGrid();
		for (SimConfigArray plotView : simConfig.getPlotViews()) {
			mathCommand = genContext + "plotVars = {";
			mathCommand2 = genContext + "plotLegends = {";
			for (String dvToShow : (ArrayList<String>) plotView.get("DepVarsToShow").getValue()) {
				mathCommand += modelContext + dvToShow + "[" + timeVar + "],";
				mathCommand2 += "ToString[" + dvToShow + "],";
			}
			mathCommand = removeLastComma(mathCommand);
			mathCommand += "}";
			mathCommand2 = removeLastComma(mathCommand2);
			mathCommand2 += "}";
			bufferCommand(mathCommand);
			bufferCommand(mathCommand2);
			executeMathBuffer();
			executeFunctionGraphicGrid(genContext + "plotVars", genContext + "plotLegends", ndSolveVar, "t",
					"Concentration", true, "dyn" + m.getName() + ".gif", smallSize, bigSize);
		}
		outVL.outGrid();

		mathCommand = "Join[{Join[{" + timeVar + "}," + genContext + "DepVars]}, Table[Join[{" + timeVar
				+ "}, Table[FortranForm[Evaluate[" + genContext + "DepVars[[i]][" + timeVar + "] /. " + ndSolveVar
				+ "][[1]]],{i,1,Length[" + genContext + "DepVars]}]], {" + timeVar + ", " + genContext + "ti, "
				+ genContext + "tf, " + genContext + "tSteps}]] // TableForm";
		executeDynamicDownloadButton(mathCommand, "Download Dynamic Simulation Table",
				"dynamic" + m.getName() + ".txt");
	}

	@SuppressWarnings("unchecked")
	private void dynamicSimulation() throws CException, MathLinkException {
		// model must have been checked before calling this function
		outVL.out("Dynamic Simulation", "textH2");
		mathCommand = genContext + "ti = " + (String) simConfig.getDynamic().get("Ti").getValue();
		bufferCommand(mathCommand);
		mathCommand = genContext + "tf = " + (String) simConfig.getDynamic().get("Tf").getValue();
		bufferCommand(mathCommand);
		mathCommand = genContext + "tSteps = " + (String) simConfig.getDynamic().get("TSteps").getValue();
		bufferCommand(mathCommand);

		if (!((Boolean) simConfig.getDynamic().get("Sensitivities").getValue())
				&& !((Boolean) simConfig.getDynamic().get("Gains").getValue())) {
			mathCommand = genContext + "Sols = NDSolve[" + genContext + "EqsToSolve, " + genContext + "DepVars, {"
					+ timeVar + ", " + genContext + "ti, " + genContext + "tf}]";
			bufferCommand(mathCommand);
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
			mathCommand = "AugInitConds = Join[" + "DepVarsConc, " + "InitCondsG]";
			bufferCommand(mathCommand);
			mathCommand = "AugFullSystem = Join[" + "AugODES, " + "AugInitConds]";
			bufferCommand(mathCommand);
			mathCommand = "NumAugFullS = " + "AugFullSystem /. Join[" + "ParNumVals, " + "IndVarVals]";
			bufferCommand(mathCommand);
			mathCommand = "AugVNS = Join[" + "DepVars, Map[Head, Flatten[" + "DynGainVars]]]";
			bufferCommand(mathCommand);
			mathCommand = "NAugSol = NDSolve[" + "NumAugFullS, " + "AugVNS, {" + timeVar + "," + "ti," + "tf}]";
			bufferCommand(mathCommand);
			executeMathBuffer();
			plotDynamicSim("NAugSol");
			outVL.out("Absolute Dynamic Gains", "textH2");
			outVL.resetGrid();
			for (SimConfigArray plotView : simConfig.getPlotViews()) {
				mathCommand = "AG = {";
				mathCommand2 = "plotLegends = {";
				for (String dvToShow : (ArrayList<String>) plotView.get("DepVarsToShow").getValue()) {
					for (String ind : m.getAllSpeciesConstant().keySet()) {
						mathCommand += gainContext + dvToShow + ind + "[" + timeVar + "],";
						mathCommand2 += "G" + dvToShow + ind + ",";
					}
				}
				mathCommand = removeLastComma(mathCommand);
				mathCommand += "}";
				mathCommand2 = removeLastComma(mathCommand2);
				mathCommand2 += "}";
				bufferCommand(mathCommand);
				bufferCommand(mathCommand2);
				executeMathBuffer();
				executeFunctionGraphicGrid("AG", "plotLegends", "NAugSol", "t", "Gain", true,
						"dynAG" + m.getName() + ".gif", smallSize, bigSize);
			}
			outVL.outGrid();
			// mathCommand = "RG = Table[DynGainVars[[i]]*IndVars/DepVarsWithT[[i]], {i,
			// 1,Length["
			// + "DynGainVars]}] /. IndVarVals";

			outVL.out("Relative Dynamic Gains", "textH2");
			outVL.resetGrid();
			for (SimConfigArray plotView : simConfig.getPlotViews()) {
				mathCommand = "RG = {";
				mathCommand2 = "plotLegends = {";
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
				mathCommand2 += "}";
				bufferCommand(mathCommand);
				bufferCommand(mathCommand2);
				executeMathBuffer();
				executeFunctionGraphicGrid("RG", "plotLegends", "NAugSol", "t", "Relative Gain", true,
						"dynRG" + m.getName() + ".gif", smallSize, bigSize);
			}
			outVL.outGrid();
		}
		// DYNAMIC SENSITIVITIES
		if (((Boolean) simConfig.getDynamic().get("Sensitivities").getValue())) {
			mathCommand = "DynSensiVars = {";
			mathCommand2 = "DynSensiVarsDiff = {";
			for (String dep : m.getAllSpeciesTimeDependent().keySet()) {
				mathCommand += "{";
				mathCommand2 += "{";
				for (Reaction r : m) {
					for (String parName : r.getFormulaValues().keySet()) {
						if (r.getFormulaValues().get(parName) != null && r.getFormulaValues().get(parName).isFilled()
								&& r.getFormulaValues().get(parName).getType() == FormulaValueType.CONSTANT) {
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
			mathCommand = "AugInitConds = Join[" + "DepVarsConc, " + "InitCondsG]";
			bufferCommand(mathCommand);
			mathCommand = "AugFullSystem = Join[" + "AugODES, " + "AugInitConds]";
			bufferCommand(mathCommand);
			mathCommand = "NumAugFullS = " + "AugFullSystem /. Join[" + "ParNumVals, " + "IndVarVals]";
			bufferCommand(mathCommand);
			mathCommand = "AugVNS = Join[" + "DepVars, Map[Head, Flatten[" + "DynSensiVars]]]";
			bufferCommand(mathCommand);
			mathCommand = "NAugSol = NDSolve[" + "NumAugFullS, " + "AugVNS, {" + timeVar + "," + "ti," + "tf}]";
			bufferCommand(mathCommand);
			executeMathBuffer();
			if (!((Boolean) simConfig.getDynamic().get("Gains").getValue())) {
				plotDynamicSim("NAugSol");
			}

			outVL.out("Absolute Dynamic Sensitivities", "textH2");
			outVL.resetGrid();
			for (SimConfigArray plotView : simConfig.getPlotViews()) {
				mathCommand = "AS = {";
				mathCommand2 = "plotLegends = {";
				for (String dvToShow : (ArrayList<String>) plotView.get("DepVarsToShow").getValue()) {
					for (Reaction r : m) {
						for (String parName : r.getFormulaValues().keySet()) {
							if (r.getFormulaValues().get(parName) != null
									&& r.getFormulaValues().get(parName).isFilled()
									&& r.getFormulaValues().get(parName).getType() == FormulaValueType.CONSTANT) {
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
				mathCommand2 += "}";
				bufferCommand(mathCommand);
				bufferCommand(mathCommand2);
				executeMathBuffer();
				executeFunctionGraphicGrid("AS", "plotLegends", "NAugSol", "t", "Sensitivity", true,
						"dynAS" + m.getName() + ".gif", smallSize, bigSize);
			}
			outVL.outGrid();

			// mathCommand = "RS = Table[DynSensiVars[[i]]*Pars/DepVarsWithT[[i]], {i,
			// 1,Length["
			// + "DynSensiVars]}] /.ParNumVals";
			// bufferCommand(mathCommand);
			// executeMathBuffer();
			outVL.out("Relative Dynamic Sensitivities", "textH2");
			outVL.resetGrid();
			for (SimConfigArray plotView : simConfig.getPlotViews()) {
				mathCommand = "RS = {";
				mathCommand2 = "plotLegends = {";
				for (String dvToShow : (ArrayList<String>) plotView.get("DepVarsToShow").getValue()) {
					for (Reaction r : m) {
						for (String par : r.getFormulaValues().keySet()) {
							if (r.getFormulaValues().get(par) != null && r.getFormulaValues().get(par).isFilled()
									&& r.getFormulaValues().get(par).getType() == FormulaValueType.CONSTANT) {
								mathCommand += sensContext + dvToShow + r.getMathematicaContext() + par + "[" + timeVar
										+ "]*" + r.getFormulaValues().get(par).getStringValue() + "/" + modelContext
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
				mathCommand2 += "}";
				bufferCommand(mathCommand);
				bufferCommand(mathCommand2);
				executeMathBuffer();
				executeFunctionGraphicGrid("RS", "plotLegends", "NAugSol", "t", "Relative Sens.", true,
						"dynRS" + m.getName() + ".gif", smallSize, bigSize);
			}
			outVL.outGrid();
		}
	}

	private String removeLastComma(String str) {
		if (str.charAt(str.length() - 1) == ',')
			str = str.substring(0, str.length() - 1);
		return str;
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> getDepVarsToShowDynGain(Map<String, Object> map, String dynGainVars)
			throws MathLinkException {
		Map<String, String> resMap = new HashMap<>();
		String plotList = "{";
		String list = executeMathCommandString("Flatten[" + dynGainVars + "]").replaceAll("\\{", "")
				.replaceAll("\\}", "").replaceAll("\\s", "");
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
	private void steadyStateSimulation() throws CException, MathLinkException {
		// model must have been checked before calling this function
		TreeMap<String, String> ssMap = new TreeMap<>();
		executeMathBuffer();
		outVL.out("Steady State Simulation", "textH2");

		mathCommand = genContext + "ssri = 10^-32;\n" + genContext + "ssrf = 1;\n" + genContext + "thresholdCondition="
				+ simConfig.getSteadyState().get("Threshold") + ";\n" + genContext + "isSteadyState = 0;\n" + genContext
				+ "SSSol=FindRoot[" + genContext + "RateEqsAllSubs == 0, Table[{" + genContext
				+ "DepVarsWithT[[k1]], Random[Real, {" + genContext + "ssri," + genContext + "ssrf}]}, {k1, 1, Length["
				+ genContext + "DepVarsWithT]}]];\n" + "If[Select[" + genContext + "RateEqsAllSubs /. " + genContext
				+ "SSSol, (Abs[#] > " + genContext + "thresholdCondition) &] == {},\n" + genContext
				+ "isSteadyState =1\n" + ",(*else*)\n" + "" + genContext + "newTime = 500000;\n" + "" + genContext
				+ "timesTried = 0;\n" + "While[" + genContext + "timesTried < 2 && " + genContext
				+ "isSteadyState == 0,\n" + "  " + genContext + "SSSolN = NDSolve[" + genContext + "EqsToSolve, "
				+ genContext + "DepVars, {" + timeVar + ", " + genContext + "newTime-1, " + genContext
				+ "newTime+1}];\n" + "  " + genContext + "TestVar1 = (Select[Flatten[" + genContext
				+ "RateEqsAllSubs /. " + genContext + "SSSolN /. {" + timeVar + "->" + genContext
				+ "newTime}], (Abs[#] > " + genContext + "thresholdCondition) &] == {});\n" + "  If[" + genContext
				+ "TestVar1 == True,\n" + "" + genContext + "SSSol = FindRoot[" + genContext
				+ "RateEqsAllSubs == 0, Table[{" + genContext + "DepVarsWithT[[k1]], (" + genContext + "DepVars[[k1]]["
				+ genContext + "newTime] /. " + genContext + "SSSolN)[[1]]}, {k1, 1, Length[" + genContext
				+ "DepVarsWithT]}]];\n" + "       " + genContext + "TestVar2 = (Select[" + genContext
				+ "RateEqsAllSubs /. " + genContext + "SSSol, (Abs[#] > " + genContext
				+ "thresholdCondition) &] == {});\n" + "       If[" + genContext + "TestVar2 == True, " + genContext
				+ "isSteadyState = 1];\n" + "  ];\n" + "  " + genContext + "newTime = " + genContext + "newTime*2;\n"
				+ "  " + genContext + "timesTried++;\n" + "];\n" + "];\n" + "If[" + genContext + "isSteadyState==1,\n"
				+ "Evaluate[true];\n" + "If[Select[Re[Eigenvalues[" + genContext + "Jac /. Join[" + genContext
				+ "ParNumVals, " + genContext + "IndVarVals, " + genContext + "SSSol]]], (# >= 0) &] != {},\n"
				+ "	Print[\"" + SharedData.mathPrintPrefix
				+ "WARNING: Unstable Steady State!\"]; Return[\"unstable\"]\n" + ",\n" + "Print[\""
				+ SharedData.mathPrintPrefix + "Stable Steady State\"]; Return[\"stable\"]\n" + "];\n" + ",\n"
				+ "Print[\"" + SharedData.mathPrintPrefix
				+ "System doesn't reach Steady State\"]; Return[\"notFound\"];\n" + "];";
		String mathRes = executeMathCommandString(mathCommand);
		if (mathRes.equals("Return[stable]") || mathRes.equals("Return[unstable]")) {
			fillSteadyStateSimulationMap(genContext + "SSSol", ssMap);
			showSteadyStateSimulationGridAndButton("Download Steady State Simulation",
					"steadystate" + m.getName() + ".txt", ssMap);
			if (((Boolean) simConfig.getSteadyState().get("Stability").getValue())) {
				outVL.out("Stability Analysis", "textH2");
				if (mathRes.equals("Return[stable]")) {
					outVL.out("Stable: All real parts are negative");
				} else if (mathRes.equals("Return[unstable]")) {
					outVL.out("Unstable: At least one real part is non-negative");
				}

				mathCommand = "" + genContext + "eigenValues = Eigenvalues[" + genContext + "Jac /. Join[" + genContext
						+ "ParNumVals, " + genContext + "IndVarVals, " + genContext + "SSSol]]";
				bufferCommand(mathCommand);
				// String eigenValues = executeMathCommandString(mathCommand);
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

		if (mathRes.equals("Return[stable]")) {
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
				// outVL.resetGrid();
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
				for (String dv : simConfig.getUnifiedDepVarsFromPlotViews().keySet())
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
				for (String dv : simConfig.getUnifiedDepVarsFromPlotViews().keySet())
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
	}
}
