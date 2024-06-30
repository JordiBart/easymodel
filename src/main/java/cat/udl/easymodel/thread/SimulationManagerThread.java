//package cat.udl.easymodel.thread;
//
//import java.util.concurrent.Callable;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//
//import com.wolfram.jlink.MathLinkException;
//
//import cat.udl.easymodel.controller.SimulationCtrl;
//import cat.udl.easymodel.main.SessionData;
//import cat.udl.easymodel.main.SharedData;
//import cat.udl.easymodel.utils.CException;
//
//public class SimulationManagerThread extends Thread {
//	private String errMessage = null;
//	private SessionData sessionData = null;
//	private SharedData sharedData = SharedData.getInstance();
//	private Integer timeout;
//	private ExecutorService executor;
//	private SimCallable simCallable;
//
//	public SimulationManagerThread(SessionData sessionData) {
//		this.sessionData = sessionData;
//		this.timeout = Integer.valueOf(sharedData.getProperties().getProperty("simulationTimeoutMinutes"));
//		this.executor = Executors.newSingleThreadExecutor();
//		this.simCallable = new SimCallable(this.sessionData);
//	}
//
//	@Override
//	public void run() {
//		try {
//			sessionData.setSimCancel(false);
//			errMessage = executor.submit(this.simCallable).get(timeout, TimeUnit.MINUTES);
//			sessionData.closeMathLinkOp(); // redundance
////			if (sessionData.isSimCancel())
////				sessionData.getSimStatusHL().error("Simulation cancelled by user");
////			else if (errMessage != null) {
////				sessionData.getSimStatusHL().error(errMessage);
////			} else if (errMessage == null) {
////				sessionData.getSimStatusHL().finish();
////			}
//		} catch (TimeoutException e) {
////			sessionData.getSimStatusHL().error("Simulation timeout (" + timeout + " minutes)");
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}
//		executor.shutdown();
//		// this.interrupt();
//	}
//
////////////////////////////////////////////////////////////////
//	public void cancelSimulation() {
//		sessionData.closeMathLinkOp();
//	}
//
//	public void cancelSimulationByUser() {
//		sessionData.setSimCancel(true);
//		sessionData.closeMathLinkOp();
//	}
//}
//
//class SimCallable implements Callable<String> {
//	private SessionData sessionData = null;
//	private SimulationCtrl simCtrl = null;
//
//	public SimCallable(SessionData sessionData) {
//		this.sessionData = sessionData;
//		this.simCtrl = new SimulationCtrl(sessionData);
//	}
//
//	@Override
//	public String call() {
//		String errorMsg=null;
//		try {
//			sessionData.respawnMathLinkOp();
//			simCtrl.simulate();
//		} catch (Exception e) {
//			if (e instanceof MathLinkException) {
//				System.err.println("SimMan:MathLinkException");
//				System.err.println(e.getMessage());
//				errorMsg="Mathematica error, please try again later";
//			} else if (e instanceof CException) {
//				errorMsg=e.getMessage();
//			} else {
//				System.err.println("SimMan:Other Exception");
//				e.printStackTrace();
//				errorMsg="Unknown error";
//			}
//		} finally {
//			sessionData.closeMathLinkOp();
//		}
//		return errorMsg;
//	}
//}
