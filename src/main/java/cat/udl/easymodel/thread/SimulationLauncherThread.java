//package cat.udl.easymodel.thread;
//
//public class SimulationLauncherThread extends Thread {
//	private SimulationManagerThread simulationManagerThread;
//
//	public SimulationLauncherThread(SimulationManagerThread simulationManagerThread) {
//		this.simulationManagerThread = simulationManagerThread;
//	}
//
//	@Override
//	public void run() {
//		try {
//			simulationManagerThread.cancelSimulation();
//			for (int i=0;i<100 && simulationManagerThread.isAlive();i++)
//				Thread.sleep(100);
//			if (!simulationManagerThread.isAlive())
//				simulationManagerThread.start();
//		} catch (InterruptedException e) {
//			System.err.print("SimulationLauncherThread: Sleep");
//		}
//	}
//}
