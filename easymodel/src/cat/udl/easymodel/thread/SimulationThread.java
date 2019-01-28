package cat.udl.easymodel.thread;

import javax.management.RuntimeErrorException;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.controller.SimulationCtrl;
import cat.udl.easymodel.controller.SimulationCtrlImpl;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.CException;

public class SimulationThread extends Thread {
	private SimulationCtrl simCtrl=null;
//	private SessionData sessionData=null;
	private String interruptReason=null;
	
	public SimulationThread(SessionData sessionData) {
//		this.sessionData=sessionData;
		this.simCtrl=new SimulationCtrlImpl(sessionData);
	}

	@Override
	public void run() {
		setInterruptReason(null);
		try {
			this.simCtrl.simulate();
		} catch (Exception e) {
			if (e instanceof MathLinkException)
				setInterruptReason("Mathematica Kernel Error");
			else if (e instanceof CException)
				setInterruptReason(e.getMessage());
			else
				setInterruptReason("Unknown error");
//			throw new InterruptedException();
			this.interrupt();
		}
	}

	public String getInterruptReason() {
		return interruptReason;
	}

	public void setInterruptReason(String interruptReason) {
		this.interruptReason = interruptReason;
	}
}
