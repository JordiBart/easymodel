package cat.udl.easymodel.thread;

import java.time.LocalDate;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.p;

public class DailyTaskRunnable implements Runnable {
	private int lastDailyTaskDayOfMonth = -1;

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
//				p.p("daily task");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean isOKToRunDailyTask() {
		LocalDate localDate = LocalDate.now();
		if (localDate.getDayOfMonth() != lastDailyTaskDayOfMonth) {
			lastDailyTaskDayOfMonth = localDate.getDayOfMonth();
			return true;
		} else
			return false;
	}
}
