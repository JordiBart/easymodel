package cat.udl.easymodel.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.controller.SimulationCtrl;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathLinkOp;
import cat.udl.easymodel.utils.CException;
import cat.udl.easymodel.utils.p;

public class SimulationManagerThread extends Thread {
	private String errMessage = null;
	private SessionData sessionData = null;
	private SharedData sharedData;
	private boolean isCancel;
	private Integer timeout;
	private ExecutorService executor;
	private SimCallable simCallable;

	public SimulationManagerThread(SessionData sessionData) {
		this.sessionData = sessionData;
		this.sharedData = SharedData.getInstance();
		this.timeout = Integer.valueOf(sharedData.getProperties().getProperty("simulationTimeoutMinutes"));
		this.executor = Executors.newSingleThreadExecutor();
		this.simCallable = new SimCallable(this.sessionData);
	}

	@Override
	public void run() {
		isCancel = false;
		try {
			errMessage = executor.submit(this.simCallable).get(timeout, TimeUnit.MINUTES);
			if (isCancel)
				sessionData.getSimStatusHL().error("Simulation cancelled by user");
			else if (errMessage != null) {
				sessionData.getSimStatusHL().error(errMessage);
			} else if (errMessage == null) {
				sessionData.getSimStatusHL().finish();
			}
		} catch (TimeoutException e) {
			sessionData.getSimStatusHL().error("Simulation timeout (" + timeout + " minutes)");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		sessionData.respawnSimulationManager();
		sessionData.closeMathLinkOp();
		executor.shutdownNow();
		this.interrupt();
//		sessionData.simulationManager = null;
	}

	public void cancelSim() {
		MathLinkOp mathLinkOp = sessionData.getMathLinkOp();
		if (mathLinkOp != null) {
			isCancel = true;
			mathLinkOp.closeMathLink();
		}
	}
}

class SimCallable implements Callable<String> {
	private SessionData sessionData = null;
	private SimulationCtrl simCtrl = null;

	public SimCallable(SessionData sessionData) {
		this.sessionData = sessionData;
		this.simCtrl = new SimulationCtrl(this.sessionData);
	}

	@Override
	public String call() {
		try {
			if (!sessionData.createMathLinkOp())
				throw new CException("webMathematica is busy, please try again later");
			simCtrl.simulate();
			this.sessionData.closeMathLinkOp();
			return null;
		} catch (Exception e) {
			if (e instanceof MathLinkException) {
				System.err.println("SimMan:MathLinkException");
				System.err.println(e.getMessage());
				return "webMathematica error, please try again later";
			} else if (e instanceof CException)
				return e.getMessage();
			else {
				System.err.println("SimMan:Other Exception");
				e.printStackTrace();
				return "Unknown error";
			}
		}
	}
}
