package cat.udl.easymodel.mathlink;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.vaadin.ui.VerticalLayout;
import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.MathLinkFactory;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.CException;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.results.OutVL;

public class MathLinkImpl implements MathLink {
	private KernelLink ml = null;
	private OutVL outVL;

	public MathLinkImpl(OutVL outVL) {
		this.outVL = outVL;
	}

	@Override
	public void openMathLink() throws MathLinkException {
		if (isOpen())
			return;
		SharedData sharedData = SharedData.getInstance();
		try {
			// Try create kernel link using properties file
			ml = MathLinkFactory.createKernelLink(sharedData.getProperties().getProperty("mathKernelPath"));
			ml.discardAnswer(); // InputNamePacket discarded
		} catch (MathLinkException e1) {
			try {
				// Try create kernel link in WINDOWS
				ml = MathLinkFactory.createKernelLink(
						"-linkmode launch -linkname 'C:\\Program Files\\Wolfram Research\\Mathematica\\10.0\\MathKernel.exe'");
				ml.discardAnswer(); // InputNamePacket discarded
			} catch (MathLinkException e2) {
				try {
					// Try create kernel link in LINUX
					ml = MathLinkFactory.createKernelLink("-linkmode launch -linkname 'math -mathlink'");
					ml.discardAnswer(); // InputNamePacket discarded
				} catch (MathLinkException e3) {
					ml = null;
					System.err.println("MathLinkException: " + e3.getMessage());
					System.err.println("HINT: edit " + SharedData.appDir + "\\" + SharedData.propertiesFile
							+ " to set MathKernel.exe path");
					throw new MathLinkException(0, "MathLink creation error, please try again later");
				}
			}
		}
		ml.addPacketListener(new MathPacketListener(this.outVL));
	}

	@Override
	public void closeMathLink() {
		if (isOpen()) {
			ml.terminateKernel();
			ml.close();
			ml = null;
		}
	}

	@Override
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

	@Override
	public void evaluate(String mlCmd) throws MathLinkException {
		if (mlCmd != null) {
			ml.connect();
			ml.evaluate(mlCmd);
			ml.discardAnswer();
		}
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
	public String checkMathCommand(String mlCmd) throws CException, MathLinkException {
		try {
			@SuppressWarnings("unused")
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
					BigDecimal bigDec = new BigDecimal(test.substring(0, test.length()-1));
					return test+"0";
				} catch (Exception e2) {
				}
			}
			return test;
		}
	}

	@Override
	public void checkMultiMathCommands(ArrayList<String> cmds) throws CException, MathLinkException {
		for (String cmd : cmds)
			checkMathCommand(cmd);
	}

	@Override
	public byte[] evaluateToImage(String mlCmd) throws MathLinkException {
		byte[] result = new byte[] { 0 };
		if (mlCmd != null) {
			ml.connect();
			try {
				result = ml.evaluateToImage(mlCmd, 0, 0);
				if (ml.getLastError() != null)
					throw new MathLinkException(ml.getLastError());
			} catch (Throwable e) {
				throw new MathLinkException(e);
			}
		}
		return result;
	}
}
