package cat.udl.easymodel.mathlink;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.MathLinkFactory;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.CException;

public class MathLinkOp {
	private KernelLink ml = null;
	private SessionData sessionData;

	public MathLinkOp(SessionData sessionData) {
		this.sessionData = sessionData;
//		System.setProperty("com.wolfram.jlink.libdir", System.getProperty("user.dir") + "/" + SharedData.jLinkLibDir);
	}
	
	public void openMathLink() throws MathLinkException {
		if (isOpen())
			return;
		SharedData sharedData = SharedData.getInstance();
		try {
			// Try create kernel link using properties file
			// System.load(System.getProperty("user.dir")+"/"+SharedData.jLinkLibDir+"/JLinkNativeLibrary.dll");
			ml = MathLinkFactory.createKernelLink(sharedData.getProperties().getProperty("mathKernelPath"));
			ml.discardAnswer(); // InputNamePacket discarded
		} catch (MathLinkException | Error e1) {
			if (e1 instanceof Error) {
				e1.printStackTrace();
				System.out.println("HINT: JLinkNativeLibrary.dll must be located in "
						+ System.getProperty("com.wolfram.jlink.libdir"));
				System.out.println("HINT: Try to restart webapp server");
				throw new MathLinkException(0, "MathLink error, please contact with the administrator");
			}
			try {
				// Try create kernel link in WINDOWS
				ml = MathLinkFactory.createKernelLink(
						"-linkmode launch -linkname 'C:\\Program Files\\Wolfram Research\\Mathematica\\9.0\\MathKernel.exe'");
				ml.discardAnswer(); // InputNamePacket discarded
			} catch (MathLinkException e2) {
				try {
					// Try create kernel link in LINUX
					ml = MathLinkFactory.createKernelLink("-linkmode launch -linkname 'math -mathlink'");
					ml.discardAnswer(); // InputNamePacket discarded
				} catch (MathLinkException e3) {
					ml = null;
					System.err.println("MathLinkException: " + e3.getMessage());
					System.err.println("HINT: edit " + SharedData.propertiesFilePath + " to set MathKernel.exe path");
					throw new MathLinkException(0, "MathLink creation error, please try again later");
				}
			}
		}
		if (this.sessionData != null)
			ml.addPacketListener(new MathPacketListener(this.sessionData));
	}

	
	public void closeMathLink() {
		if (isOpen()) {
			ml.abandonEvaluation();
			ml.terminateKernel();
			ml.close();
			ml = null;
			System.gc();
		}
	}

	
	public boolean isOpen() {
		if (ml != null) {
			try {
				ml.connect();
				return true;
			} catch (MathLinkException e) {
				ml = null;
				return false;
			}
		}
		return false;
	}

	
	public void evaluate(String mlCmd) throws MathLinkException {
		if (mlCmd != null) {
			ml.connect();
			ml.evaluate(mlCmd);
			ml.discardAnswer();
		}
	}

	
	public Integer evaluateToInt(String mlCmd) throws MathLinkException {
		Integer result = 0;
		if (mlCmd != null) {
			ml.connect();
			ml.evaluate(mlCmd);
			ml.waitForAnswer();
			ml.newPacket();
			result = ml.getInteger();
			ml.newPacket();
		}
		return result;
	}

	
	public Boolean evaluateToBoolean(String mlCmd) throws MathLinkException {
		Boolean result = false;
		if (mlCmd != null) {
			ml.connect();
			ml.evaluate(mlCmd);
			ml.waitForAnswer();
			result = ml.getBoolean();
			ml.newPacket();
		}
		return result;
	}

	
	public String evaluateToString(String mlCmd) throws MathLinkException {
		String result = "";
		if (mlCmd != null) {
			ml.connect();
			try {
				result = ml.evaluateToOutputForm(mlCmd, 0);
				if (ml.getLastError() != null)
					throw ml.getLastError();
			} catch (Throwable e) {
				throw new MathLinkException(e);
			}
		}
		return result;
	}

	
	public String checkMathCommand(String mlCmd) throws CException, MathLinkException {
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

	
	public void checkMultiMathCommands(ArrayList<String> cmds) throws CException, MathLinkException {
		for (String cmd : cmds)
			checkMathCommand(cmd);
	}

	
	public byte[] evaluateToImage(String mlCmd) throws MathLinkException {
		byte[] result = new byte[] { 0 };
		if (mlCmd != null) {
			ml.connect();
			try {
				result = ml.evaluateToImage(mlCmd, 0, 0, 72, false); // 72 dpi
				if (ml.getLastError() != null)
					throw new MathLinkException(ml.getLastError());
			} catch (Throwable e) {
				throw new MathLinkException(e);
			}
		}
		return result;
	}
}
