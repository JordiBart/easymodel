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
import cat.udl.easymodel.utils.CException;

public class SimulationManagerThread extends Thread {
	private String errMessage = null;
	private SessionData sessionData = null;
	private SharedData sharedData;
	private boolean isCancel;
	private Integer timeout;

	public SimulationManagerThread(SessionData sessionData) {
		this.sessionData = sessionData;
		sharedData = SharedData.getInstance();
		timeout = Integer.valueOf(sharedData.getProperties().getProperty("simulationTimeoutMinutes"));
	}

	@Override
	public void run() {
		isCancel = false;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> future = executor.submit(new SimCallable(sessionData));
		try {
			errMessage = future.get(timeout, TimeUnit.MINUTES);
			if (isCancel)
				sessionData.getSimStatusHL().error("Simulation cancelled by user");
			else if (errMessage != null) {
				sessionData.getSimStatusHL().error(errMessage);
			} else if (errMessage == null) {
				sessionData.getSimStatusHL().finish();
			}
		} catch (TimeoutException e) {
			sessionData.getMathLinkOp().closeMathLink();
			sessionData.getSimStatusHL().error("Simulation timeout (" + timeout + " minutes)");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			sessionData.getMathLinkOp().closeMathLink();
			executor.shutdownNow();
			sessionData.setSimulationManager(null);
		}
	}

	public void cancelSim() {
		isCancel = true;
		sessionData.getMathLinkOp().closeMathLink();
	}
}

class SimCallable implements Callable<String> {
	private SessionData sessionData = null;

	public SimCallable(SessionData sessionData) {
		this.sessionData = sessionData;
	}

	@Override
	public String call() throws Exception {
		SimulationCtrl simCtrl = new SimulationCtrl(sessionData);
		try {
			simCtrl.simulate();
		} catch (Exception e) {
			if (e instanceof MathLinkException)
				return "WebMathematica error, please try again later";
			else if (e instanceof CException)
				return e.getMessage();
			else
				return "Unknown error";
		}
		return null;
	}
}
