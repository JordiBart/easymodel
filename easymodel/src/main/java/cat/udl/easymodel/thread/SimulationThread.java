package cat.udl.easymodel.thread;

import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.controller.SimulationCtrl;
import cat.udl.easymodel.controller.SimulationCtrlImpl;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.CException;

public class SimulationThread extends Thread {
	private SessionData sessionData = null;
	private String errorMessage = null;
	private boolean isCancel = false;

	public SimulationThread(SessionData sessionData) {
		this.sessionData = sessionData;
	}

	@Override
	public void run() {
		SimulationCtrl simCtrl = new SimulationCtrlImpl(sessionData);
		setErrorMessage(null);
		isCancel = false;
		try {
			simCtrl.simulate();
		} catch (Exception e) {
//			p.p("exc");
			if (!isCancel) {
				if (e instanceof MathLinkException)
					setErrorMessage("Mathematica Kernel Error");
				else if (e instanceof CException)
					setErrorMessage(e.getMessage());
				else
					setErrorMessage("Unknown error");
			}
		}
	}

	@Override
	public void interrupt() {
		try {
			// close connections
			sessionData.getMathLinkOp().closeMathLink();
		} finally {
			super.interrupt();
		}
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void cancel() {
		isCancel = true;
		setErrorMessage("Simulation cancelled by user");
		this.interrupt();
	}
}
