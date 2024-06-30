package cat.udl.easymodel.thread;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.P;

import java.time.LocalDate;

public class DailyTaskRunnable implements Runnable {
	private int lastExecutedDayOfMonth = -1;

	public DailyTaskRunnable() {
	}

	@Override
	public void run() {
		if (isOKToRunDailyTask()) {
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
