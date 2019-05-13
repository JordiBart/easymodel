package cat.udl.easymodel.thread;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.vcomponent.results.ResultsVL;

public class SimulationThreadManagerThread extends Thread {
	private SessionData sessionData = null;
	private ResultsVL resultsVL = null;

	public SimulationThreadManagerThread(SessionData sessionData, ResultsVL resultsVL) {
		this.sessionData = sessionData;
		this.resultsVL = resultsVL;
	}

	@Override
	public void run() {
		SimulationThread simThread = new SimulationThread(sessionData);
		sessionData.setSimulationThread(simThread);
		try {
			simThread.start();
			simThread.join(SharedData.simulationTimeoutMinutes * 60 * 1000); // ms
			if (simThread.isAlive()) {
				simThread.interrupt();
				sessionData.getSimStatusHL().error("Simulation timeout (" + SharedData.simulationTimeoutMinutes + " minutes)");
			} else if (simThread.getErrorMessage() != null) {
				sessionData.getSimStatusHL().error(simThread.getErrorMessage());
			} else {
				sessionData.getSimStatusHL().finish();
			}
		} catch (InterruptedException e) {
			// uncontrolled interruption
			sessionData.getSimStatusHL().error("Error 100");
			e.printStackTrace();
		} finally {
			sessionData.setSimulationThread(null);
		}
	}
}
