package cat.udl.easymodel.thread;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.app.AppPanel;

public class SimulationLauncherThread extends Thread {
	private SessionData sessionData;
	private AppPanel mainPanel;

	public SimulationLauncherThread(SessionData sessionData, AppPanel mainPanel) {
		this.sessionData = sessionData;
		this.mainPanel=mainPanel;
	}

	@Override
	public void run() {
		try {
			sessionData.cancelSimulationByCode();
			for (int i=0;i<100 && sessionData.isSimulating();i++)
				Thread.sleep(100);
			if (!sessionData.isSimulating())
				sessionData.launchSimulation();
		} catch (InterruptedException e) {
			System.err.print("SimulationLauncherThread: Sleep");
		}
	}
}
