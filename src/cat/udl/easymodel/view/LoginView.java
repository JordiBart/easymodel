package cat.udl.easymodel.view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
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

public class LoginView extends CustomComponent implements View {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "login";

	private TextField user;
	private PasswordField password;
	private Button loginButton;
	private SessionData sessionData;
	private SharedData sharedData = SharedData.getInstance();
	private ArrayList<User> allUsers;

	public LoginView() {
		this.sessionData = (SessionData) UI.getCurrent().getData();

		try {
			allUsers = sharedData.getAllUsers();
		} catch (SQLException e) {
			Notification.show(SharedData.dbError, Type.WARNING_MESSAGE);
		}

		user = new TextField("Username");
		user.setWidth("150px");
		user.setRequired(true);

		password = new PasswordField("Password");
		password.setWidth("150px");
		// password.addValidator(new PasswordValidator());
		password.setRequired(true);
		password.setValue("");
		password.setNullRepresentation("");

		loginButton = new Button("Login", getLoginButtonClickListener());
		loginButton.setWidth("100px");
		loginButton.setClickShortcut(KeyCode.ENTER);

		HorizontalLayout titleHL = new HorizontalLayout();
		titleHL.setMargin(false);
		Label loginLabel = new Label("Welcome to "+SharedData.appName+", please log in below");
		Label titleLabel = new Label(SharedData.appName);
		VerticalLayout spacerTitle = new VerticalLayout();
		spacerTitle.setWidth((460 - loginLabel.getWidth() - titleLabel.getWidth()) + "px");
		titleHL.addComponents(loginLabel, spacerTitle, titleLabel);
		titleHL.setExpandRatio(spacerTitle, 1.0f);

		HorizontalLayout fieldsHL = new HorizontalLayout();
//		fieldsHL.setHeight("50px");
		fieldsHL.setSpacing(true);
		fieldsHL.setMargin(false);
		fieldsHL.setDefaultComponentAlignment(Alignment.BOTTOM_CENTER);
		fieldsHL.addComponents(user, password, loginButton);
		
		HorizontalLayout accessAsGuestHL = getAccessAsGuestHL();

		VerticalLayout loginPanelVL = new VerticalLayout();
		loginPanelVL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		VerticalLayout spacerPanelVL = new VerticalLayout();
		Label orLab = new Label("or");
		orLab.setSizeUndefined();
		loginPanelVL.addComponents(titleHL, fieldsHL, orLab, accessAsGuestHL);
		loginPanelVL.setSpacing(true);
		loginPanelVL.setMargin(true);
		loginPanelVL.setSizeFull();

		Panel loginPanel = new Panel(loginPanelVL);
		loginPanel.setWidth("475px");
		loginPanel.setHeight("205px");
		
		if (sharedData.isDebug()) {
			Label dbgLab = new Label("TEST MODE: user=empty->application; user=\"a\"->admin");
			dbgLab.setSizeUndefined();
			loginPanelVL.addComponent(dbgLab);
			loginPanel.setHeight("230px");
		}
		loginPanelVL.addComponents(spacerPanelVL);
		loginPanelVL.setExpandRatio(spacerPanelVL, 1.0f);

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		viewLayout.addComponent(loginPanel);
		viewLayout.setStyleName(Reindeer.LAYOUT_BLUE);
		viewLayout.setSizeFull();
		setCompositionRoot(viewLayout);
		setSizeFull();
		
	}

	private HorizontalLayout getOrHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.setMargin(false);
		hl.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		hl.addComponents(new Label("or"));
		return hl;
	}

	private HorizontalLayout getAccessAsGuestHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.setMargin(false);
		hl.setDefaultComponentAlignment(Alignment.BOTTOM_CENTER);
		Button btn = new Button("Login as guest");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (sharedData.getGuestUser() != null) {
					sessionData.setUser(sharedData.getGuestUser());
					getUI().getNavigator().navigateTo(AppView.NAME);
				}
			}
		});
		hl.addComponents(btn);
		return hl;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		user.focus();
	}

	private ClickListener getLoginButtonClickListener() {
		return new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (allUsers == null)
					return;
				String userStr = user.getValue();
				String passwordStr = password.getValue();

				if (sharedData.isDebug()) {
					if (userStr.equals(""))
						userStr = "test";
					if (userStr.equals("a"))
						userStr = "admin";
					if ("test".equals(userStr))
						passwordStr = "test";
					if ("admin".equals(userStr))
						passwordStr = "admin";
				}
				if ((Pattern.matches("\\w+", userStr) && Pattern.matches("\\w+", passwordStr))) {
					for (User u : allUsers) {
						// JBcrypt uses 2a prefix!!!
						// gen: https://asecuritysite.com/encryption/bcrypt
						if (u.getName().equals(userStr)) {
							if (BCrypt.checkpw(passwordStr, u.getEncPassword())) {
								sessionData.setUser(u);
							}
							break;
						}
					}
					if (sessionData.getUser() != null) {
						if (sessionData.getUser().getUserType() == UserType.USER) {
							getUI().getNavigator().navigateTo(AppView.NAME);
						} else if (sessionData.getUser().getUserType() == UserType.ADMIN) {
							getUI().getNavigator().navigateTo(AdminView.NAME);
						}
					} else {
						Notification.show("Invalid user/password", "", Notification.Type.WARNING_MESSAGE);
					}
				} else {
					Notification.show("Invalid user/password", "", Notification.Type.WARNING_MESSAGE);
					if (!userStr.equals("")) {
						password.setValue("");
						password.focus();
					} else {
						user.focus();
					}
				}
			}
		};
	}

	private static final class PasswordValidator extends AbstractValidator<String> {
		public PasswordValidator() {
			super("Password must be at least 8 characters long and contain at least one number");
		}

		@Override
		protected boolean isValidValue(String value) {
			// Password must be at least 8 characters long and contain at least
			// one number
			if (value != null && (value.length() < 8 || !value.matches(".*\\d.*"))) {
				return false;
			}
			return true;
		}

		@Override
		public Class<String> getType() {
			return String.class;
		}
	}
}