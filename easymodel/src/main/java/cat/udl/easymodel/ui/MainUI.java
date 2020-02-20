package cat.udl.easymodel.ui;

import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.sbml.SBMLMan;
import cat.udl.easymodel.view.AdminView;
import cat.udl.easymodel.view.AppView;
import cat.udl.easymodel.view.ErrorView;
import cat.udl.easymodel.view.LoginView;

/**
 * This UI is the application entry point. A UI may either represent a browser
 * window (or tab) or some part of an HTML page where a Vaadin application is
 * embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is
 * intended to be overridden to add component to the user interface and
 * initialize non-component functionality.
 */
@Theme("easymodel")
@Push
public class MainUI extends UI {
	private static final long serialVersionUID = 7898982385148930284L;
	private SessionData sessionData;
	private SharedData sharedData;

	@WebServlet(urlPatterns = "/*", name = "MainUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = MainUI.class, productionMode = true)
	// XXX WHEN USING TOMCAT FROM ECLIPSE: PRODUCTION=TRUE MAY PREVENT COMPILATION OF CSS
	
	public static class MainUIServlet extends VaadinServlet {
	}
	
	@Override
	protected void init(VaadinRequest vaadinRequest) {
//		System.load(System.getProperty("user.dir")+"\\JLinkNativeLibrary.dll");
//		UI.getCurrent().setPollInterval(1000); //ui push every 1000ms
		sharedData = SharedData.getInstance();
		String ip = getPage().getWebBrowser().getAddress();
		sharedData.getVisitCounterRunnable().setVisitorIp(ip);
		new Thread(sharedData.getVisitCounterRunnable()).start();

		try {
			sharedData.getDbManager().open();
		} catch (SQLException e) {
			Notification.show(SharedData.dbError, Type.ERROR_MESSAGE);
			System.err.println(e.getMessage());
			return;
		}
		sharedData.tryDailyTask();

		this.sessionData = new SessionData(this.getUI());
		this.setData(this.sessionData);
		this.sessionData.init();

		getPage().setTitle(SharedData.fullAppName);
		this.setNavigator(new Navigator(this, this));
		// Navigator views
		this.getNavigator().addView(LoginView.NAME, LoginView.class);
		this.getNavigator().navigateTo(LoginView.NAME);
//    			this.getNavigator().addView(RegisterView.NAME, RegisterView.class);
//    			this.getNavigator().addView(AppView.NAME, AppView.class);
//    			this.getNavigator().addView(AdminView.NAME, AdminView.class);
		// user logged check
		this.getNavigator().addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				boolean isLoggedIn = sessionData.isUserSet();
				// views
				boolean isLoginView = event.getNewView() instanceof LoginView;
				boolean isAppiew = event.getNewView() instanceof AppView;
				boolean isAdminView = event.getNewView() instanceof AdminView;
				// sharedData.dbgPrint("UI "+event.getNewView());

				if (isAdminView && isLoggedIn && sessionData.getUser().getUserType() != UserType.ADMIN) {
					// user wants to access admin view but it is not admin
					getNavigator().navigateTo(AppView.NAME);
					return false;
				} else if (!isLoggedIn && !isLoginView) {
					// user not logged and wants to acces app
					getNavigator().navigateTo(LoginView.NAME);
					return false;
				} else if (isLoggedIn && isLoginView) {
					// user logged and wants to login again
					return false;
				}
				// grant access
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
			}
		});
		this.getNavigator().setErrorView(ErrorView.class);
	}
}
