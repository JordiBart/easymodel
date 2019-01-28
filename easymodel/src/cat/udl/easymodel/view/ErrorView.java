package cat.udl.easymodel.view;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.logic.user.UserImpl;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.BCrypt;

public class ErrorView extends CustomComponent implements View {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "login";

	private TextField user;
	private PasswordField password;
	private Button loginButton;
	private SessionData sessionData;
	private SharedData sharedData = SharedData.getInstance();
	private ArrayList<User> allUsers;

	public ErrorView() {
	}

	@Override
	public void enter(ViewChangeEvent event) {
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.sessionData.reset();
		
		VerticalLayout viewLayout = new VerticalLayout();
		Button btn = new Button("Go back to Login page");
		btn.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().navigateTo(LoginView.NAME);
			}
		});
		viewLayout.addComponents(new Label("Ups there was some error"), btn);
		setCompositionRoot(viewLayout);
		setSizeFull();
	}
}