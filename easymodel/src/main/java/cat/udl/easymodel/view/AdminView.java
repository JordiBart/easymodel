package cat.udl.easymodel.view;

import java.sql.SQLException;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.admin.AdminPanel;
import cat.udl.easymodel.vcomponent.app.AppPanel;

import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class AdminView extends CustomComponent implements View {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "admin";

	// Vaadin
	private VerticalLayout mainLayout = null;
	private AdminPanel mainPanel; 

	private SessionData sessionData = null;
	private SharedData sharedData = SharedData.getInstance();

	public AdminView() {
		this.sessionData = (SessionData) UI.getCurrent().getData();
		
		loadFormulasAndModels();

		mainPanel = new AdminPanel();

		mainLayout = new VerticalLayout();
		mainLayout.setId("mainLayout");
		mainLayout.setMargin(false);
		mainLayout.setSpacing(true);
		mainLayout.setSizeFull();
		mainLayout.addComponent(mainPanel);
		
		setSizeFull();
		setCompositionRoot(mainLayout);
	}

	private void loadFormulasAndModels() {
		try {
			sessionData.getModels().semiLoadDB();
		} catch (SQLException e) {
			Notification.show("Can't load models", Type.WARNING_MESSAGE);
		}
	}

	@Override
	public void enter(ViewChangeEvent event) {
	}
}