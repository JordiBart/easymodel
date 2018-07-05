package cat.udl.easymodel.main;

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
		//MYSQL CREATE CONNECTION
		try {
			sharedData.getDbManager().open();
			System.out.println("MYSQL CONNECTION CREATED SUCCESSFULLY!");
		} catch(Exception e) {
			System.out.println("MYSQL CANT CONNECT");
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("STOPPING WEBAPP");
		SharedData sharedData = SharedData.getInstance();
		sharedData.getDbManager().close();
	}
}