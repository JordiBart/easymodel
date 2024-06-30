package cat.udl.easymodel.mathlink;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

import cat.udl.easymodel.controller.BioModelsLogs;
import cat.udl.easymodel.logic.results.ResultList;
import cat.udl.easymodel.views.simulationresults.OutVL;
import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.MathLinkFactory;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.CException;

public class MathLinkOp {
	private KernelLink ml = null;
	private Timestamp openTimestamp = null;
	private boolean isClosingMathLink = false;

	public MathLinkOp() throws MathLinkException {
//		System.setProperty("com.wolfram.jlink.libdir", System.getProperty("user.dir") + "/" + SharedData.jLinkLibDir);
		SharedData sharedData = SharedData.getInstance();
		try {
			// System.load(System.getProperty("user.dir")+"/"+SharedData.jLinkLibDir+"/JLinkNativeLibrary.dll");
			// Try create kernel link using properties file
			ml = MathLinkFactory.createKernelLink(sharedData.getProperties().getProperty("mathKernelCmdLine"));
			ml.discardAnswer(); // InputNamePacket discarded
			// mathlink opened
			openTimestamp = Timestamp.from(Instant.now());
		} catch (MathLinkException | Error e1) {
			ml = null;
			if (e1 instanceof Error) {
				e1.printStackTrace();
				System.out.println("HINT: JLinkNativeLibrary.dll must be located in "
						+ System.getProperty("com.wolfram.jlink.libdir")+" or in the working dir");
				System.out.println("HINT: Try to restart webapp server");
				throw new MathLinkException(0, "Mathematica configuration error, please contact with the administrator");
			} else {
				System.err.println("Cannot create MathLink: " + e1.getMessage());
				System.err.println("HINT: edit " + SharedData.propertiesFilePath
						+ " to set MathKernel.exe path (or max no. of kernels reached?");
				throw new MathLinkException(0, "Can't open Mathematica");
			}
//				WINDOWS example MathLinkFactory.createKernelLink("-linkmode launch -linkname 'C:\\Program Files\\Wolfram Research\\Mathematica\\14.0\\MathKernel.exe'"
//				LINUX example: MathLinkFactory.createKernelLink("-linkmode launch -linkname 'math -mathlink'");
		}
	}

	public void addPacketListener(ResultList resultList, BioModelsLogs bioModelsLogs){
		if (isOpen()) {
			ml.addPacketListener(new MathPacketListenerOp(resultList, bioModelsLogs));
		}
	}

	public void closeMathLink() {
		if (isOpen()) {
			isClosingMathLink = true;
			ml.abortEvaluation();
			ml.interruptEvaluation();
			ml.abandonEvaluation();

			ml.terminateKernel();
			ml.close();
			ml = null;
			isClosingMathLink = false;
			System.gc();
		}
	}

	public boolean isOpen() {
		return (ml != null && !isClosingMathLink);
	}

	public boolean isAlive() {
		if (!isOpen())
			return false;
		try {
			ml.connect();
			this.evaluate("1");
			return true;
		} catch (Exception e) {
			ml = null;
			return false;
		}
	}

	public void evaluate(String mlCmd) throws Exception {
		ml.evaluate(mlCmd);
		ml.discardAnswer();
	}

	public String evaluateToString(String mlCmd) throws Exception {
		String result = "";
			result = ml.evaluateToOutputForm(mlCmd, 0);
			if (result == null)
				throw new MathLinkException(1, "Evaluate error");
			else if (ml.getLastError() != null)
				throw new MathLinkException(ml.getLastError());
		return result;
	}

	public byte[] evaluateToImage(String mlCmd) throws Exception {
		byte[] result = new byte[] { 0 };
		result = ml.evaluateToImage(mlCmd, 0, 0, 0, false); // width, height, dpi, use front end
		if (ml.getLastError() != null)
			throw new MathLinkException(ml.getLastError());
		return result;
	}

	public String checkMathCommand(String mlCmd) throws Exception {
		try {
			BigDecimal bigDec = new BigDecimal(mlCmd);
			return bigDec.toString();
		} catch (Exception e) {
			String test = evaluateToString("Check[" + mlCmd + ", err]");
			// p.p("t:"+test+":e");
			if (test.equals("$Failed") || test.equals("err") || test.equals("ComplexInfinity") || test.equals(""))
				throw new CException("BAD MATHEMATICA EXPRESSION: " + mlCmd);
			if (test.endsWith(".")) {
				try {
					@SuppressWarnings("unused")
					BigDecimal bigDec = new BigDecimal(test.substring(0, test.length() - 1));
					return test + "0";
				} catch (Exception e2) {
				}
			}
			return test;
		}
	}

	public void checkMultiMathCommands(ArrayList<String> cmds) throws Exception {
		for (String cmd : cmds)
			checkMathCommand(cmd);
	}

	public void tryTimeout() {
		SharedData sharedData = SharedData.getInstance();
		if (openTimestamp != null && openTimestamp.getTime()
				+ ((Integer.valueOf(sharedData.getProperties().getProperty("simulationTimeoutMinutes")) + 1) * 60
						* 1000) < Timestamp.from(Instant.now()).getTime()) {
			this.closeMathLink();
		}
	}

	// not used-not needed anymore
//	public void killAllMathematica() {
//		ml = null;
//		System.gc();
//		try {
//			Runtime rt = Runtime.getRuntime();
//			if (SharedData.isDialogsSystem()) {
//				rt.exec("taskkill /F /IM MathKernel.exe");
//				rt.exec("taskkill /F /IM Mathematica.exe");
//			} else {
//				rt.exec("killall -9 WolframKernel | killall -9 Mathematica");
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
