package cat.udl.easymodel.mathlink;

import java.util.ArrayList;

import com.vaadin.ui.VerticalLayout;
import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.MathLinkFactory;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.CException;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.results.OutVL;

public class MathLinkOpImpl implements MathLinkOp {
	private KernelLink ml = null;
	private OutVL outVL;

	public MathLinkOpImpl(OutVL outVL) {
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
	public void checkMathCode(String mlCmd) throws CException, MathLinkException {
		String test = evaluateToString("Check[" + mlCmd + ", err]");
		if (test.equals("") || test.equals("err"))
			throw new CException("BAD EXPRESSION: " + mlCmd);
	}

	@Override
	public void checkMultiCommands(ArrayList<String> cmds) throws CException, MathLinkException {
		for (String cmd : cmds)
			checkMathCode(cmd);
	}

	@Override
	public byte[] evaluateToImage(String mlCmd) throws MathLinkException {
		byte[] result = new byte[] { 0 };
		if (mlCmd != null) {
			ml.connect();
			try {
				result = ml.evaluateToImage(mlCmd, 0, 0);
				if (ml.getLastError() != null)
					throw ml.getLastError();
			} catch (Throwable e) {
				throw new MathLinkException(e);
			}
		}
		return result;
	}
}
