//package cat.udl.easymodel.main;
//
//import javax.servlet.ServletContextEvent;
//import javax.servlet.ServletContextListener;
//import javax.servlet.annotation.WebListener;
//import java.sql.SQLException;
//
//// MathLink open/close
//@WebListener
//public class ContextListener implements ServletContextListener {
//
//	@Override
//	public void contextInitialized(ServletContextEvent arg0) {
//
//	}
//
//	@Override
//	public void contextDestroyed(ServletContextEvent arg0) {
//		System.out.println("STOPPING WEBAPP");
//		SharedData sharedData = SharedData.getInstance();
//		sharedData.getMathLinkFactory().closeMathLinks();
//		try {
//			sharedData.getDbManager().close();
//		} catch (Exception e) {
//		}
//	}
//}