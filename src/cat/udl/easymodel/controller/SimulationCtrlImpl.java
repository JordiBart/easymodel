package cat.udl.easymodel.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.sun.media.sound.FFT;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.logic.model.FormulaValue;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathLinkOp;
import cat.udl.easymodel.sbml.SBMLTools;
import cat.udl.easymodel.utils.CException;
import cat.udl.easymodel.utils.p;
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
	private MathLinkOp mathLinkOp;

	private String timeVar = "t";
	private String bigSize;
	private String smallSize;

	private Model m = null;
	private SimConfig simConfig;

	private String genContext = "g`";
	private String exportContext = "";
	private MathBuffer mathBuffer = new MathBufferImpl();
	private ExportMathCommands exportMathCommands = new ExportMathCommandsImpl();
	private String mathCommand, mathCommand2, mathCommand3;
	private String sbmlDataString;

	public SimulationCtrlImpl(SessionData sessionData) {
		this.sessionData = sessionData;
		this.outVL = this.sessionData.getOutVL();
		this.mathLinkOp = this.sessionData.getMathLinkOp();
	}

	private String getChopTolerance() {
		return "";
	}

	private void executeMathBuffer() throws MathLinkException {
		mathLinkOp.evaluate(mathBuffer.getString());
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
		String mOut = mathLinkOp.evaluateToString(mathCommand);
		exportMathCommands.addCommand(mathCommand);
		return mOut;
	}

	private Boolean executeMathCommandBoolean(String mathCommand) throws MathLinkException {
		Boolean mOut = mathLinkOp.evaluateToBoolean(mathCommand);
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
			String yLabel, boolean addToGrid, String fileName, String smallSize2, String bigSize2) throws MathLinkException {
		String cmd = "PlotVars = " + plotList + "; Plot[Evaluate[PlotVars /. " + ndSolveVar + "], {" + timeVar + ",ti,"
				+ "tf}, LabelStyle -> {FontFamily -> Arial, FontWeight -> " + simConfig.getPlot().get("FontWeight")
				+ ", FontSlant -> " + simConfig.getPlot().get("FontSlant") + ", FontSize -> "
				+ simConfig.getPlot().get("FontSize") + "}, PlotLegends->" + plotLegends
				+ ", Axes -> False, Frame -> True, PlotStyle -> Table[{Dashing[0.03 k1/Length["
				+ "PlotVars]], Thickness[" + simConfig.getPlot().get("LineThickness")
				+ "]}, {k1, 1, Length[PlotVars]}], FrameLabel -> {\"" + xLabel + "\", \"" + yLabel
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
				+ simConfig.getPlot().get("ImageResolution") + ", ImageSize->" + smallSize + "]";
		bigSizeCmd = "Rasterize[" + bigSizeCmd + ", ImageResolution->" + simConfig.getPlot().get("ImageResolution")
				+ ", ImageSize->" + bigSize + "]";
		exportMathCommands.addCommand(normalSizeCmd);
		byte[] mImage = mathLinkOp.evaluateToImage(normalSizeCmd);
		byte[] mImageBig = mathLinkOp.evaluateToImage(bigSizeCmd);
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
			e1.printStackTrace();
		}
		outVL.out(fileName, addToGrid, mImage, imHeight, mImageBig);
	}

	private void executeDynamicDownloadButton(String mathCommand, String caption, String filename) throws MathLinkException {
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
		outVL.outFile(caption, "", filename, fileString2, false);
	}

	private void executeMathTable(String tableName, String columnList, String rowList, boolean isBindTableToDepVars) throws MathLinkException {
		if (columnList != null)
			mathCommand = "columnList = {Join[{\" \"},Map[TextString," + columnList + "]]}";
		else
			mathCommand = "columnList = {}";
		bufferCommand(mathCommand);
		if (rowList != null)
			mathCommand = "rowList = Map[TextString," + rowList + "]";
		else
			mathCommand = "rowList = {}";
		bufferCommand(mathCommand);
		if (!isBindTableToDepVars)
			mathCommand = "tab = Join[columnList, Table[Join[If[rowList != {}, List[rowList[[j]]],{}]," + tableName
					+ "[[j]]], {j, 1, Length[rowList]}]]";
		else
			mathCommand = "tab = Join[columnList, Table[Join[If[rowList != {}, List[rowList[[j]]],{}]," + tableName
					+ "[[(FirstPosition[DepVarsString,rowList[[j]]][[1]])]]], {j, 1, Length[rowList]}]]";
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
		outVL.outFile("Mathematica", "Save the Mathematica code to modify and run locally", m.getName() + ".nb", exportMathCommands.getString(), true);
		try {
			generateSBML();
			outVL.outFile("SBML", "Save the SBML file to use in other Systems Biology software", m.getName() + ".xml", sbmlDataString, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		outVL.outHorizontalLayout();
		
		bufferEndContext();
		executeMathBuffer();
		exportMathCommands.reset();
	}

	private void generateSBML() throws Exception {
		sbmlDataString = SBMLTools.exportSBML(m, mathLinkOp);
	}

	/////////
	private void fillSteadyStateSimulationMap(String mathListName, TreeMap<String, String> ssMap) throws MathLinkException {
		mathCommand = mathListName + " // TableForm";
		String fileString = executeMathCommandString(mathCommand);
		fileString = fileString.replace("\n", sharedData.getNewLine());
		boolean first = true;
		String eValue = "";
		String sp = "";
		for (String line : fileString.split(sharedData.getNewLine())) {
			if (!line.equals("")) {
				if (!line.matches(".*\\[t\\]\\s*\\->.*")) {
					eValue = line;
					eValue = eValue.replace(" ", "");
				} else {
					first = true;
					for (String word : line.split("\\[t\\] -> ")) {
						if (first) { // species
							sp = word;
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
		outVL.outFile(caption,"", filename, file2Download,false);
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

	private void checkModelFormulas() throws MathLinkException, CException {
		bufferBeginContext();
		executeMathBuffer();
		mathLinkOp.checkMultiCommands(m.getAllUsedFormulaStrings());
		bufferEndContext();
		executeMathBuffer();
		exportMathCommands.reset();
	}

	///////////
	@Override
	public void simulate() throws CException, MathLinkException,Exception {
		try {
			outVL.reset();
			mathLinkOp.openMathLink();
			m = sessionData.getSelectedModel();
			m.checkIfReadyToSimulate();
			checkModelFormulas();
			// validated
			simConfig = m.getSimConfig();
			smallSize = (String) simConfig.getPlot().get("ImageSize-Small");
			bigSize = (String) simConfig.getPlot().get("ImageSize-Big");
			// begin
			sessionData.getOutVL().out("Results for " + m.getName(), "textH1");
//			sessionData.getOutVL().out("(click image/s to enlarge)", "textSmall");
			initSimulation();
			if ((Boolean) simConfig.getDynamic().get("Enable")) {
				dynamicSimulation();
			}
			if ((Boolean) simConfig.getSteadyState().get("Enable")) {
				steadyStateSimulation();
			}
			endMathCommands();
		} catch (CException|MathLinkException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mathLinkOp.closeMathLink();			
		}
	}

	private void initSimulation() throws CException {
		bufferBeginContext();
		// DECLARE DEPVARS
		mathCommand = "DepVars={";
		for (String sp : m.getAllSpeciesTimeDependent().keySet())
			mathCommand += sp + ",";
		if (mathCommand.charAt(mathCommand.length() - 1) == ',')
			mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
		mathCommand += "}";
		bufferCommand(mathCommand);
		mathCommand = "DepVarsString = Map[TextString,DepVars]";
		bufferCommand(mathCommand);
		// MAKE DEPVARS DEPEND OF T
		mathCommand = "SubsDepVarsWithT={";
		for (String sp : m.getAllSpeciesTimeDependent().keySet()) {
			mathCommand += sp + "->" + sp + "[" + timeVar + "],";
		}
		if (mathCommand.charAt(mathCommand.length() - 1) == ',')
			mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
		mathCommand += "}";
		bufferCommand(mathCommand);
		mathCommand = "DepVarsWithT=" + "DepVars /. " + "SubsDepVarsWithT";
		bufferCommand(mathCommand);
		mathCommand = "DepVarsWithTDiff=Table[" + "DepVars[[i]]'[t],{i,1,Length[" + "DepVars]}]";
		bufferCommand(mathCommand);
		// CONCENTRATIONS
		mathCommand = "DepVarsConc={";
		for (String sp : m.getAllSpeciesTimeDependent().keySet())
			mathCommand += sp + "[0]==" + m.getAllSpecies().get(sp).getConcentration() + ",";
		if (mathCommand.charAt(mathCommand.length() - 1) == ',')
			mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
		mathCommand += "}";
		bufferCommand(mathCommand);
		// STOICHIOMETRIC MATRIX
		try {
			mathCommand = "SM=" + m.getStoichiometricMatrix();
		} catch (Exception e) {
			outVL.out("Error creating Stoichiometric Matrix" + e.getMessage());
		}
		// mathCommand += ";\n";
		bufferCommand(mathCommand);
		// DECLARE FORMULAS
		mathCommand = "Rates={";
		for (Reaction r : m)
			mathCommand += "Hold[" + r.getFormula().getFormulaWithAddedPrefix("", r.getIdJavaStr()) + "],";
		if (mathCommand.charAt(mathCommand.length() - 1) == ',')
			mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
		mathCommand += "}";
		bufferCommand(mathCommand);
		// DECLARE FORMULAS built-in vars (X,M...)
		mathCommand = "SubsFormVars={";
		for (Reaction r : m) {
			if (r.getLeftPartSpecies().size() != 0) {
				mathCommand += r.getIdJavaStr() + "X->{";
				for (String sp : r.getLeftPartSpecies().keySet())
					mathCommand += sp + ",";
				if (mathCommand.charAt(mathCommand.length() - 1) == ',')
					mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
				mathCommand += "},";
				mathCommand += r.getIdJavaStr() + "A->{";
				for (String sp : r.getLeftPartSpecies().keySet())
					mathCommand += r.getLeftPartSpecies().get(sp) + ",";
				if (mathCommand.charAt(mathCommand.length() - 1) == ',')
					mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
				mathCommand += "},";
				mathCommand += r.getIdJavaStr() + "XF->" + r.getLeftPartSpecies().firstKey() + ",";
			}
			if (r.getModifiers().size() != 0) {
				mathCommand += r.getIdJavaStr() + "M->{";
				for (String sp : r.getModifiers().keySet())
					mathCommand += sp + ",";
				if (mathCommand.charAt(mathCommand.length() - 1) == ',')
					mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
				mathCommand += "},";
				mathCommand += r.getIdJavaStr() + "MF->" + r.getModifiers().firstKey() + ",";
			}
		}
		if (mathCommand.charAt(mathCommand.length() - 1) == ',')
			mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
		mathCommand += "}";
		bufferCommand(mathCommand);

		// DECLARE FORMULAS PARAMETERS VALUES WHICH ARE SPECIES (a...)
		mathCommand = "ParVarVals={";
		for (Reaction r : m) {
			Map<String, FormulaValue> valuesByReaction = r.getFormulaValues();
			for (String key : valuesByReaction.keySet()) {
				if (valuesByReaction.get(key).getType() != FormulaValueType.CONSTANT)
					mathCommand += r.getIdJavaStr() + key + "->" + valuesByReaction.get(key).getStringValue() + ",";
			}
		}
		if (mathCommand.charAt(mathCommand.length() - 1) == ',')
			mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
		mathCommand += "}";
		bufferCommand(mathCommand);

		// DECLARE FORMULAS PARAMETERS WHICH DEFINE NUMBERS (a, "g array",...)
		mathCommand = "Pars={";
		for (Reaction r : m) {
			// CONSTANT VALUES BY REACTIONS (ig a)
			Map<String, FormulaValue> valuesByReaction = r.getFormulaValues();
			for (String key : valuesByReaction.keySet()) {
				if (valuesByReaction.get(key).getType() == FormulaValueType.CONSTANT)
					mathCommand += r.getIdJavaStr() + key + ",";
			}
			// substrates and modifiers constants (ig "g array")
			for (String co : r.getFormulaSubstratesArrayParameters().keySet()) {
				// loop could be over getFormulaModifiersArrayParameters() too
				int i = 1;
				for (String sp : r.getFormulaSubstratesArrayParameters().get(co).keySet()) {
					mathCommand += r.getIdJavaStr() + co + sp + ",";
					i++;
				}
				for (String sp : r.getFormulaModifiersArrayParameters().get(co).keySet()) {
					mathCommand += r.getIdJavaStr() + co + sp + ",";
					i++;
				}
			}
		}
		if (mathCommand.charAt(mathCommand.length() - 1) == ',')
			mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
		mathCommand += "}";
		bufferCommand(mathCommand);

		// DECLARE FORMULAS PARAMETERS ARRAYS WITH VARS ("g array",...)
		mathCommand = "ParArrayInVars={";
		for (Reaction r : m) {
			for (String co : r.getFormulaSubstratesArrayParameters().keySet()) {
				// loop could be over getFormulaModifiersArrayParameters() too
				mathCommand += r.getIdJavaStr() + co + "->{";
				int i = 1;
				for (String sp : r.getFormulaSubstratesArrayParameters().get(co).keySet()) {
					mathCommand += r.getIdJavaStr() + co + sp + ",";
					i++;
				}
				for (String sp : r.getFormulaModifiersArrayParameters().get(co).keySet()) {
					mathCommand += r.getIdJavaStr() + co + sp + ",";
					i++;
				}
				if (mathCommand.charAt(mathCommand.length() - 1) == ',')
					mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
				mathCommand += "},";
			}
		}
		if (mathCommand.charAt(mathCommand.length() - 1) == ',')
			mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
		mathCommand += "}";
		bufferCommand(mathCommand);

		// DECLARE FORMULAS PARAMETERS VALUES WHICH ARE NUMBERS (a, "g array",...)
		mathCommand = "ParNumVals={";
		for (Reaction r : m) {
			// CONSTANT VALUES BY REACTIONS (ig a)
			Map<String, FormulaValue> valuesByReaction = r.getFormulaValues();
			for (String key : valuesByReaction.keySet()) {
				if (valuesByReaction.get(key).getType() == FormulaValueType.CONSTANT)
					mathCommand += r.getIdJavaStr() + key + "->" + valuesByReaction.get(key).getStringValue() + ",";
			}
			// species and modifiers constants (ig "g array vars")
			for (String co : r.getFormulaSubstratesArrayParameters().keySet()) {
				// loop could be over getFormulaModifiersArrayParameters() too
				int i = 1;
				for (String sp : r.getFormulaSubstratesArrayParameters().get(co).keySet()) {
					mathCommand += r.getIdJavaStr() + co + sp + "->"
							+ r.getFormulaSubstratesArrayParameters().get(co).get(sp) + ",";
					i++;
				}
				for (String sp : r.getFormulaModifiersArrayParameters().get(co).keySet()) {
					mathCommand += r.getIdJavaStr() + co + sp + "->"
							+ r.getFormulaModifiersArrayParameters().get(co).get(sp) + ",";
					i++;
				}
			}
		}
		if (mathCommand.charAt(mathCommand.length() - 1) == ',')
			mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
		mathCommand += "}";
		bufferCommand(mathCommand);

		mathCommand = "IndVars={";
		for (String sp : m.getAllSpeciesConstant().keySet())
			mathCommand += sp + ",";
		if (mathCommand.charAt(mathCommand.length() - 1) == ',')
			mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
		mathCommand += "}";
		bufferCommand(mathCommand);

		mathCommand = "IndVarVals={";
		for (String sp : m.getAllSpeciesConstant().keySet())
			mathCommand += sp + "->" + m.getAllSpecies().get(sp).getConcentration() + ",";
		if (mathCommand.charAt(mathCommand.length() - 1) == ',')
			mathCommand = mathCommand.substring(0, mathCommand.lastIndexOf(","));
		mathCommand += "}";
		bufferCommand(mathCommand);

		mathCommand = "RateEqs = SM.(Rates/. Join[SubsFormVars,ParVarVals,ParArrayInVars])";
		bufferCommand(mathCommand);
		mathCommand = "RateEqs = ReleaseHold[" + "RateEqs]";
		bufferCommand(mathCommand);
		mathCommand = "RateEqsWithT = " + "RateEqs/." + "SubsDepVarsWithT";
		bufferCommand(mathCommand);
		mathCommand = "RateEqsAllSubs = " + "RateEqsWithT /." + "ParNumVals/." + "IndVarVals";
		bufferCommand(mathCommand);
		// mathCommand = "RateEqsAllSubs = ReleaseHold[" +
		// "RateEqsAllSubs]";
		// executeMathCommand(mathCommand);
		mathCommand = "Jac = Outer[D, RateEqsWithT, DepVarsWithT]";
		bufferCommand(mathCommand);
		mathCommand = "EqsToSolve = Join[Table[((" + "DepVars[[i]]))'[" + timeVar + "] == "
				+ "RateEqsAllSubs[[i]], {i, 1, Length[" + "DepVars]}], " + "DepVarsConc]";
		bufferCommand(mathCommand);
	}

	@SuppressWarnings("unchecked")
	private void plotDynamicSim(String ndSolveVar) throws MathLinkException {
		outVL.resetGrid();
		for (Map<String, Object> plotViewMap : simConfig.getPlotViews()) {
			mathCommand = "plotVars = {";
			mathCommand2 = "plotLegends = {";
			for (String dvToShow : (ArrayList<String>) plotViewMap.get("DepVarsToShow")) {
				mathCommand += dvToShow + "[" + timeVar + "],";
				mathCommand2 += dvToShow + ",";
			}
			mathCommand = removeLastComma(mathCommand);
			mathCommand += "}";
			mathCommand2 = removeLastComma(mathCommand2);
			mathCommand2 += "}";
			bufferCommand(mathCommand);
			bufferCommand(mathCommand2);
			executeMathBuffer();
			executeFunctionGraphicGrid("plotVars", "plotLegends", ndSolveVar, "t", "Concentration", true,
					"dyn" + m.getName() + ".gif", smallSize, bigSize);
		}
		outVL.outGrid();

		mathCommand = "Join[{Join[{" + timeVar + "}," + "DepVars]}, Table[Join[{" + timeVar
				+ "}, Table[FortranForm[Evaluate[" + "DepVars[[i]][" + timeVar + "] /. " + ndSolveVar
				+ "][[1]]],{i,1,Length[" + "DepVars]}]], {" + timeVar + ", " + "ti, " + "tf, "
				+ "tSteps}]] // TableForm";
		executeDynamicDownloadButton(mathCommand, "Download Dynamic Simulation Table",
				"dynamic" + m.getName() + ".txt");
	}

	@SuppressWarnings("unchecked")
	private void dynamicSimulation() throws CException, MathLinkException {
		// model must have been checked before calling this function
		outVL.out("Dynamic Simulation","textH2");
		mathCommand = "ti = " + (String) simConfig.getDynamic().get("Ti");
		bufferCommand(mathCommand);
		mathCommand = "tf = " + (String) simConfig.getDynamic().get("Tf");
		bufferCommand(mathCommand);
		mathCommand = "tSteps = " + (String) simConfig.getDynamic().get("TSteps");
		bufferCommand(mathCommand);

		if (!((Boolean) simConfig.getDynamic().get("Sensitivities"))
				&& !((Boolean) simConfig.getDynamic().get("Gains"))) {
			mathCommand = "Sols = NDSolve[" + "EqsToSolve, " + "DepVars, {" + timeVar + ", " + "ti, " + "tf}]";
			bufferCommand(mathCommand);
			executeMathBuffer();
			plotDynamicSim("Sols");
		}

		// DYNAMIC GAINS
		if ((Boolean) simConfig.getDynamic().get("Gains")) {
			mathCommand = "DynGainVars = Table[ToExpression[ToString[" + "G] <> ToString["
					+ "DepVars[[i]]] <> ToString[" + "IndVars[[j]]]][t], {i, 1, Dimensions["
					+ "DepVarsWithT][[1]]}, {j, 1,Dimensions[" + "IndVars][[1]]}]";
			bufferCommand(mathCommand);
			mathCommand = "DynGainVarsDiff = Table[ToExpression[ToString[" + "G] <> ToString["
					+ "DepVars[[i]]] <> ToString[" + "IndVars[[j]]]]'[t], {i, 1, Dimensions["
					+ "DepVarsWithT][[1]]}, {j, 1, Dimensions[" + "IndVars][[1]]}]";
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

			outVL.out("Absolute Dynamic Gains","textH2");
			outVL.resetGrid();
			for (Map<String, Object> plotViewMap : simConfig.getPlotViews()) {
				mathCommand = "AG = {";
				mathCommand2 = "plotLegends = {";
				for (String dvToShow : (ArrayList<String>) plotViewMap.get("DepVarsToShow")) {
					for (String ind : m.getAllSpeciesConstant().keySet()) {
						mathCommand += "G" + dvToShow + ind + "[" + timeVar + "],";
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

			outVL.out("Relative Dynamic Gains","textH2");
			outVL.resetGrid();
			for (Map<String, Object> plotViewMap : simConfig.getPlotViews()) {
				mathCommand = "RG = {";
				mathCommand2 = "plotLegends = {";
				for (String dvToShow : (ArrayList<String>) plotViewMap.get("DepVarsToShow")) {
					for (String ind : m.getAllSpeciesConstant().keySet()) {
						mathCommand += "G" + dvToShow + ind + "[" + timeVar + "]*"
								+ m.getAllSpecies().get(ind).getConcentration() + " / " + dvToShow + "[" + timeVar
								+ "],";
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
		if (((Boolean) simConfig.getDynamic().get("Sensitivities"))) {
			mathCommand = "DynSensiVars = Table[ToExpression[ToString[S] <> ToString["
					+ "DepVars[[i]]] <> ToString[Pars[[j]]]][t], {i, 1, Length[" + "DepVars]}, {j, 1,Dimensions["
					+ "Pars][[1]]}]";
			bufferCommand(mathCommand);
			mathCommand = "DynSensiVarsDiff = Table[ToExpression[ToString[S] <> ToString["
					+ "DepVars[[i]]] <> ToString[" + "Pars[[j]]]]'[t], {i, 1, Dimensions["
					+ "DepVars][[1]]}, {j, 1, Dimensions[" + "Pars][[1]]}]";
			bufferCommand(mathCommand);
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
			if (!((Boolean) simConfig.getDynamic().get("Gains"))) {
				plotDynamicSim("NAugSol");
			}

			outVL.out("Absolute Dynamic Sensitivities","textH2");
			outVL.resetGrid();
			for (Map<String, Object> plotViewMap : simConfig.getPlotViews()) {
				mathCommand = "AS = {";
				mathCommand2 = "plotLegends = {";
				for (String dvToShow : (ArrayList<String>) plotViewMap.get("DepVarsToShow")) {
					for (String par : m.getAllFormulaParameters().keySet()) {
						mathCommand += "S" + dvToShow + par + "[" + timeVar + "],";
						mathCommand2 += "S" + dvToShow + par + ",";
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
			outVL.out("Relative Dynamic Sensitivities","textH2");
			outVL.resetGrid();
			for (Map<String, Object> plotViewMap : simConfig.getPlotViews()) {
				mathCommand = "RS = {";
				mathCommand2 = "plotLegends = {";
				for (String dvToShow : (ArrayList<String>) plotViewMap.get("DepVarsToShow")) {
					for (String par : m.getAllFormulaParameters().keySet()) {
						mathCommand += "S" + dvToShow + par + "[" + timeVar + "]*"
								+ m.getAllFormulaParameters().get(par).getStringValue() + "/" + dvToShow + "[" + timeVar
								+ "],";
						mathCommand2 += "S" + dvToShow + par + ",";
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
			str = str.substring(0, str.lastIndexOf(","));
		return str;
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> getDepVarsToShowDynGain(Map<String, Object> map, String dynGainVars) throws MathLinkException {
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

		mathCommand = "ssri = 10^-32;\n" + "ssrf = 1;\n" + "thresholdCondition=10^-30;\n" + "isSteadyState = 0;\n"
				+ "SSSol=FindRoot[RateEqsAllSubs == 0, Table[{DepVarsWithT[[k1]], Random[Real, {ssri,ssrf}]}, {k1, 1, Length[DepVarsWithT]}]];\n"
				+ "If[Select[RateEqsAllSubs /. SSSol, (Abs[#] > thresholdCondition) &] == {},\n" + "isSteadyState =1\n"
				+ ",(*else*)\n" + "newTime = 500000;\n" + "timesTried = 0;\n"
				+ "While[timesTried < 2 && isSteadyState == 0,\n"
				+ "  SSSolN = NDSolve[EqsToSolve, DepVars, {t, newTime-1, newTime+1}];\n"
				+ "  TestVar1 = (Select[Flatten[RateEqsAllSubs /. SSSolN /. {t->newTime}], (Abs[#] > thresholdCondition) &] == {});\n"
				+ "  If[TestVar1 == True,\n"
				+ "SSSol = FindRoot[RateEqsAllSubs == 0, Table[{DepVarsWithT[[k1]], (DepVars[[k1]][newTime] /. SSSolN)[[1]]}, {k1, 1, Length[DepVarsWithT]}]];\n"
				+ "       TestVar2 = (Select[RateEqsAllSubs /. SSSol, (Abs[#] > thresholdCondition) &] == {});\n"
				+ "       If[TestVar2 == True, isSteadyState = 1];\n" + "  ];\n" + "  newTime = newTime*2;\n"
				+ "  timesTried++;\n" + "];\n" + "];\n" + "If[isSteadyState==1,\n" + "Evaluate[true];\n"
				+ "If[Select[Re[Eigenvalues[Jac /. Join[ParNumVals, IndVarVals, SSSol]]], (# >= 0) &] != {},\n"
				+ "	Print[\""+SharedData.mathPrintPrefix+"WARNING: Unstable Steady State!\"]; Return[\"unstable\"]\n" + ",\n"
				+ "Print[\""+SharedData.mathPrintPrefix+"Stable Steady State\"]; Return[\"stable\"]\n" + "];\n" + ",\n"
				+ "Print[\""+SharedData.mathPrintPrefix+"System doesn't reach Steady State\"]; Return[\"notFound\"];\n" + "];";
		String mathRes = executeMathCommandString(mathCommand);
		if (mathRes.equals("Return[stable]") || mathRes.equals("Return[unstable]")) {
			fillSteadyStateSimulationMap("SSSol", ssMap);
			showSteadyStateSimulationGridAndButton("Download Steady State Simulation",
					"steadystate" + m.getName() + ".txt", ssMap);
			if (((Boolean) simConfig.getSteadyState().get("Stability"))) {
				outVL.out("Stability Analysis", "textH2");
				if (mathRes.equals("Return[stable]")) {
					outVL.out("Stable: All real parts are negative");
				} else if (mathRes.equals("Return[unstable]")) {
					outVL.out("Unstable: At least one real part is non-negative");
				}

				mathCommand = "eigenValues = Eigenvalues[Jac /. Join[ParNumVals, IndVarVals, SSSol]]";
				bufferCommand(mathCommand);
				// String eigenValues = executeMathCommandString(mathCommand);
				mathCommand = "eigenValues=Table[Join[{Re[eigenValues[[i]]]},{Im[eigenValues[[i]]]}], {i,1,Length[eigenValues]}]";
				bufferCommand(mathCommand);
				executeMathBuffer();
				executeMathTable("eigenValues", "{ToString[Real],ToString[Im]}",
						"Table[ToString[Eigenvalue]<>ToString[i], {i,1,Length[DepVars]}]", false);
			}
		}

		// mathCommand = "Select[Im[Eigenvalues[Evaluate[Jac /. Join[ParNumVals,
		// IndVarVals, setTime] /. NAugSol]]], (# != 0) &] == {}";
		// if (executeMathCommandBoolean(false, mathCommand)) {
		// outVL.out("Steady State: Overshooting attenuated
		// oscillations");

		if (mathRes.equals("Return[stable]")) {
			// Steady State Gains (D=>derivative)
			if (((Boolean) simConfig.getSteadyState().get("Gains"))) {
				mathCommand = "JacIV = Outer[D, " + "RateEqsWithT, " + "IndVars]";
				bufferCommand(mathCommand);
				mathCommand = "NJacIV = Chop[" + "JacIV /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]"
						+ getChopTolerance() + "]";
				bufferCommand(mathCommand);
				mathCommand = "GA = -Inverse[" + "Jac /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]]."
						+ "NJacIV";
				bufferCommand(mathCommand);
				outVL.out("Absolute Steady State Gains","textH2");
				// outVL.resetGrid();
				// for (Map<String, Object> plotViewMap : simConfig.getPlotViews()) {
				// mathCommand = "DepVarsToShow = {";
				// for (String dvToShow : (ArrayList<String>) plotViewMap.get("DepVarsToShow"))
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
				executeMathTable("GA", "IndVars", "DepVarsToShow", true);

				mathCommand = "GR = Table[" + "GA[[i, j]]*(" + "IndVars[[j]]/"
						+ "DepVarsWithT[[i]]), {i, 1, Dimensions[" + "DepVarsWithT][[1]]}, {j, 1, Dimensions["
						+ "IndVars][[1]]}] /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]";
				bufferCommand(mathCommand);
				outVL.out("Relative Steady State Gains","textH2");
//				mathCommand = "DepVarsToShow = {";
//				for (String dv : simConfig.getUnifiedDepVarsFromPlotViews().keySet())
//					mathCommand += dv + ",";
//				mathCommand = removeLastComma(mathCommand);
//				mathCommand += "}";
//				bufferCommand(mathCommand);
				executeMathTable("GR", "IndVars", "DepVarsToShow", true);
			}
			// Steady State Sensitivities
			if (((Boolean) simConfig.getSteadyState().get("Sensitivities"))) {
				mathCommand = "JacP = Outer[D, " + "RateEqsWithT, " + "Pars]";
				bufferCommand(mathCommand);
				mathCommand = "NJacP = Chop[" + "JacP /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]"
						+ getChopTolerance() + "]";
				bufferCommand(mathCommand);
				mathCommand = "SSSA = -Inverse[" + "Jac /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]]."
						+ "NJacP";
				bufferCommand(mathCommand);
				outVL.out("Absolute Steady State Sensitivities","textH2");
				mathCommand = "DepVarsToShow = {";
				for (String dv : simConfig.getUnifiedDepVarsFromPlotViews().keySet())
					mathCommand += dv + ",";
				mathCommand = removeLastComma(mathCommand);
				mathCommand += "}";
				bufferCommand(mathCommand);
				executeMathTable("SSSA", "Pars", "DepVarsToShow", true);

				mathCommand = "SSSR = Table[" + "SSSA[[i, j]]*(" + "Pars[[j]]/"
						+ "DepVarsWithT[[i]]), {i, 1, Dimensions[" + "DepVarsWithT][[1]]}, {j, 1, Dimensions["
						+ "Pars][[1]]}] /. Join[" + "ParNumVals, " + "IndVarVals, " + "SSSol]";
				bufferCommand(mathCommand);
				outVL.out("Relative Steady State Sensitivities","textH2");
				executeMathTable("SSSR", "Pars", "DepVarsToShow", true);
			}
		}
	}
}
