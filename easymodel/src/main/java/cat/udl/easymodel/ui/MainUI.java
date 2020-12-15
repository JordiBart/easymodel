package cat.udl.easymodel.ui;

import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.sbml.SBMLMan;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.view.AdminView;
import cat.udl.easymodel.view.AppView;
import cat.udl.easymodel.view.ErrorView;
import cat.udl.easymodel.view.TutorialView;
import cat.udl.easymodel.view.CoverView;

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
@Push(transport=Transport.WEBSOCKET_XHR)
public class MainUI extends UI {
	private static final long serialVersionUID = 7898982385148930284L;
	private SessionData sessionData;
	private SharedData sharedData;

	@WebServlet(urlPatterns = "/*", name = "MainUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = MainUI.class, productionMode = true)

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
		new Thread(sharedData.getDailyTaskRunnable()).start();

		this.sessionData = new SessionData(this.getUI());
		this.setData(this.sessionData);
		this.sessionData.init();
		
//		Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
//		for (Cookie cookie : cookies) {
//			p.p(cookie.getName()+"="+cookie.getValue());
//		}
////////////////////////////////////
		addDetachListener(new DetachListener() {
			@Override
			public void detach(DetachEvent event) {
//                sessionData.clear();
//                p.p("main ui detach");
			}
		});

		getPage().setTitle(SharedData.fullAppName);
		this.setNavigator(new Navigator(this, this));
		this.getNavigator().setErrorView(ErrorView.class);

		this.getNavigator().addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				// views
				boolean isCoverView = event.getNewView() instanceof CoverView;
				boolean isAppView = event.getNewView() instanceof AppView;
				boolean isAdminView = event.getNewView() instanceof AdminView;

				if (isAdminView) {
					if (!sessionData.isUserSet()) {
						getNavigator().navigateTo(CoverView.NAME);
						return false;
					} else if (sessionData.getUser().getUserType() != UserType.ADMIN) {
						getNavigator().navigateTo(AppView.NAME);
						return false;
					}
				} else if (isCoverView && sessionData.isUserSet()) {
					// user logged and wants to login again
					getNavigator().navigateTo(AppView.NAME);
					return false;
				}
				// grant access
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
			}
		});
		// Show cover page
		this.getNavigator().addView(CoverView.NAME, CoverView.class);
		this.getNavigator().addView(TutorialView.NAME, TutorialView.class);
		this.getNavigator().addView(AppView.NAME, AppView.class);
		this.getNavigator().addView(AdminView.NAME, AdminView.class);
		// user will access cover view as URL=CoverView.NAME=""
	}
}
