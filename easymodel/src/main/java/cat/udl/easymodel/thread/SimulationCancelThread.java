package cat.udl.easymodel.thread;

public class SimulationCancelThread extends Thread {
	private SimulationManagerThread simulationManagerThread;

	public SimulationCancelThread(SimulationManagerThread simulationManagerThread) {
		this.simulationManagerThread = simulationManagerThread;
	}
	
	@Override
	public void run() {
		simulationManagerThread.cancelSim();
	}
}
