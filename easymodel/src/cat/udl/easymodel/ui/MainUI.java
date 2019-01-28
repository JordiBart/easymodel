package cat.udl.easymodel.ui;

import java.math.BigDecimal;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.view.AdminView;
import cat.udl.easymodel.view.AppView;
import cat.udl.easymodel.view.ErrorView;
import cat.udl.easymodel.view.LoginView;

@SuppressWarnings("serial")
@Theme("easymodel")
public class MainUI extends UI {
	private SessionData sessionData;
	private SharedData sharedData;
	
	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = true, ui = MainUI.class, widgetset = "cat.udl.easymodel.main.widgetset.ReactionsWidgetset")
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {
		// automatic tasks
		sharedData = SharedData.getInstance();	
		try {
			sharedData.getDbManager().autoConvertPrivateToPublic();
			sharedData.cleanTempDir();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.sessionData = new SessionData();
		this.setData(this.sessionData);
		this.sessionData.init();
		
		getPage().setTitle(SharedData.fullAppName);
		this.setNavigator(new Navigator(this, this));
		// Navigator views
		this.getNavigator().addView(LoginView.NAME, LoginView.class);
		this.getNavigator().navigateTo(LoginView.NAME);
//		this.getNavigator().addView(RegisterView.NAME, RegisterView.class);
//		this.getNavigator().addView(AppView.NAME, AppView.class);
//		this.getNavigator().addView(AdminView.NAME, AdminView.class);
		// user logged check
		this.getNavigator().addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				boolean isLoggedIn = sessionData.isUserSet();
				// views
				boolean isLoginView = event.getNewView() instanceof LoginView;
				boolean isAppiew = event.getNewView() instanceof AppView;
				boolean isAdminView = event.getNewView() instanceof AdminView;
				//sharedData.dbgPrint("UI "+event.getNewView());

				if (isAdminView && isLoggedIn && sessionData.getUser().getUserType() != UserType.ADMIN) {
					// user wants to access admin view but it is not admin
					getNavigator().navigateTo(AppView.NAME);
					return false;
				}
				else if (!isLoggedIn && !isLoginView) {
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
