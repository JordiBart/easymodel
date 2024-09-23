package cat.udl.easymodel.thread;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathQueue;

import java.time.LocalDate;

public class NewUserVisitTaskRunnable implements Runnable {
	// this task is executed every time a user visits the web app and when the app starts
	private int lastExecutedDayOfMonth = -1;

	public NewUserVisitTaskRunnable() {
		//place code in run()
	}

	@Override
	public void run() {
		MathQueue.getInstance().cleanSimJobs();
		if (isOKToRunDailyTask()) {
			//this code will only be executed once a day
			try {
				SharedData sharedData = SharedData.getInstance();
				sharedData.removeExpiredUserCookies();
				sharedData.getDbManager().autoConvertPrivateToPublic();
				sharedData.cleanTempDir();
				//P.p("daily task");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean isOKToRunDailyTask() {
		LocalDate localDate = LocalDate.now();
		if (localDate.getDayOfMonth() != lastExecutedDayOfMonth) {
			lastExecutedDayOfMonth = localDate.getDayOfMonth();
			return true;
		} else
			return false;
	}
}
