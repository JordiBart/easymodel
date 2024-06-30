//package cat.udl.easymodel.thread;
//
//import java.util.concurrent.Callable;
//import java.util.concurrent.CancellationException;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//
//import com.wolfram.jlink.MathLinkException;
//
//import cat.udl.easymodel.controller.SimulationCtrl;
//import cat.udl.easymodel.main.SessionData;
//import cat.udl.easymodel.main.SharedData;
//import cat.udl.easymodel.mathlink.MathLinkOp;
//import cat.udl.easymodel.utils.CException;
//import cat.udl.easymodel.utils.p;
//
//public class QuickStochasticMathematicaCheck {
//	private Boolean checkResult = null;
//	private SessionData sessionData = null;
//	private SharedData sharedData;
//	private Integer timeout;
//
//	public QuickStochasticMathematicaCheck(SessionData sessionData) {
//		this.sessionData = sessionData;
//		sharedData = SharedData.getInstance();
//		timeout = 20;
//	}
//
//	public boolean checkStochastic() {
//		ExecutorService executor = Executors.newSingleThreadExecutor();
//		Future<Boolean> future = executor.submit(new QuickStochasticMathematicaCheckCallable(sessionData));
//		try {
//			checkResult = future.get(timeout, TimeUnit.SECONDS);
//			return checkResult;
//		} catch (TimeoutException e) {
//			System.err.println("QuickStochasticMathematicaCheck timeout (" + timeout + " seconds)");
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}
//		sessionData.freeMathLinkOp();
//		executor.shutdownNow();
//		return true;
//	}
//}
//
//class QuickStochasticMathematicaCheckCallable implements Callable<Boolean> {
//	private SessionData sessionData = null;
//
//	public QuickStochasticMathematicaCheckCallable(SessionData sessionData) {
//		this.sessionData = sessionData;
//	}
//
//	@Override
//	public Boolean call() throws Exception {
//		SimulationCtrl simCtrl = new SimulationCtrl(sessionData);
//		try {
//			return simCtrl.quickStochasticSimulationCheck();
//		} catch (Exception e) {
//			return true;
//		}
//	}
//}
