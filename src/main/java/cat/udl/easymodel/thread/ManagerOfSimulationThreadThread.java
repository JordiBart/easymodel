//alternative version of SimulationManagerThread

//package cat.udl.easymodel.thread;
//
//import cat.udl.easymodel.main.SessionData;
//import cat.udl.easymodel.main.SharedData;
//import cat.udl.easymodel.utils.p;
//import cat.udl.easymodel.vcomponent.results.ResultsVL;
//
//public class ManagerOfSimulationThreadThread extends Thread {
//	private SessionData sessionData = null;
//	private SimulationManager simThread = null;
//	private boolean isManualCancel = false;
//
//	public ManagerOfSimulationThreadThread(SessionData sessionData) {
//		this.sessionData = sessionData;
//	}
//
//	public void cancelSim() {
//		isManualCancel = true;
//		p.p("cancel  sim");
//		sessionData.getMathLinkOp().killAllMathematica();
//	}
//	
//	@Override
//	public void run() {
//		isManualCancel = false;
//		sessionData.setManagerOfSimThreadThread(this);
//		simThread = new SimulationManager(sessionData);
//		try {
//			simThread.start();
//			simThread.join(SharedData.simulationTimeoutMinutes * 60 * 1000); // ms
//			if (simThread.isAlive()) {
//				sessionData.getMathLinkOp().killAllMathematica();
//				sessionData.getSimStatusHL().error("Simulation timeout (" + SharedData.simulationTimeoutMinutes + " minutes)");
//			} else if (isManualCancel) {
//				sessionData.getSimStatusHL().error("Simulation cancelled by user");
//			} else if (simThread.getErrorMessage() != null) {
//				sessionData.getSimStatusHL().error(simThread.getErrorMessage());
//			} else {
//				sessionData.getSimStatusHL().finish();
//			}
//		} catch (InterruptedException e) {
//			// uncontrolled interruption
//			sessionData.getSimStatusHL().error("Error 100");
//			e.printStackTrace();
//		} finally {
//			simThread = null;
//			sessionData.setManagerOfSimThreadThread(null);
//		}
//	}
//}
