package cat.udl.easymodel.main;

import java.sql.SQLException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

// MathLink open/close
@WebListener
public class ContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		SharedData sharedData = SharedData.getInstance(); // read/create .properties file
		System.out.println("STARTING WEBAPP " + SharedData.appName + " v" + SharedData.appVersion);
		// MYSQL CREATE CONNECTION
		try {
			sharedData.getDbManager().open();
			System.out.println("MYSQL CONNECTION CREATED SUCCESSFULLY!");
			sharedData.getUsers().loadDB();
			sharedData.getPredefinedFormulas().loadDB();
			sharedData.getGenericFormulas().loadDB();
			sharedData.tryDailyTask();
		} catch (Exception e) {
			System.err.println("MYSQL CONNECT ERROR!");
			e.printStackTrace();
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("STOPPING WEBAPP");
		SharedData sharedData = SharedData.getInstance();
		try {
			sharedData.getDbManager().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}