package cat.udl.easymodel.ui;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Notification.Type;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.view.TutorialView;

@Theme("easymodel")
public class TutorialUI extends UI {
	private static final long serialVersionUID = 5450243963241053258L;

//	@WebServlet(urlPatterns = "/tutorial", name = "TutorialUIServlet", asyncSupported = true)
//	@VaadinServletConfiguration(ui = TutorialUI.class, productionMode = true)
//	public static class TutorialUIServlet extends VaadinServlet {
//	}

	@Override
	protected void init(VaadinRequest request) {
		getPage().setTitle("Tutorial");
		this.setNavigator(new Navigator(this, this));
		this.getNavigator().addView(TutorialView.NAME, TutorialView.class);
		this.getNavigator().navigateTo(TutorialView.NAME);
		this.getNavigator().addViewChangeListener(new ViewChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void afterViewChange(ViewChangeEvent event) {
				Notification.show("This tutorial has been opened in a new window", Type.WARNING_MESSAGE);
			}

			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				return true;
			}
			
		});
	}
}
