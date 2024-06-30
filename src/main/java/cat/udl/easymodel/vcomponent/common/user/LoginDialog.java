package cat.udl.easymodel.vcomponent.common.user;

import cat.udl.easymodel.logic.types.NotificationType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.vcomponent.common.PendingNotification;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinSession;

public class LoginDialog extends Dialog {
	private TextField userTF;
	private PasswordField passwordTF;
	private SessionData sessionData;
	private SharedData sharedData;

	public LoginDialog() {
		super();

		sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
		sharedData = SharedData.getInstance();
		this.setModal(true);
		this.setResizable(true);
		this.setDraggable(true);
		this.setWidth("400px");
		this.setHeight("300px");

		VerticalLayout winVL = new VerticalLayout();
		winVL.setSpacing(true);
		winVL.setPadding(false);
		winVL.setSizeFull();

		VerticalLayout mainVL = new VerticalLayout();
		mainVL.setSpacing(false);
		mainVL.setPadding(false);
		mainVL.setSizeFull();
		mainVL.setClassName("scroll");

		winVL.add(ToolboxVaadin.getDialogHeader(this,"User Access",null),mainVL);

		this.add(winVL);

		userTF = new TextField();
		userTF.setLabel("Username");
		userTF.setWidth("100%");
		passwordTF = new PasswordField();
		passwordTF.setLabel("Password");
		passwordTF.setWidth("100%");

		mainVL.add(userTF, passwordTF, getLoginButton());

		userTF.focus();
	}

	private Button getLoginButton() {
		Button btn = new Button("Login");
		btn.setWidth("100%");
		btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		btn.addClickShortcut(Key.ENTER);
		btn.addClickListener(event -> {
			String userStr = userTF.getValue();
			String passwordStr = passwordTF.getValue();
			if (!userStr.matches(ToolboxVaadin.usernameCharRegex + "*")
					|| !passwordStr.matches(ToolboxVaadin.passwordCharRegex + "*")) {
				ToolboxVaadin.showWarningNotification("Username/Password contains invalid character/s");
				return;
			}
			if (sharedData.isDebug()) {
				if (userStr.equals("t"))
					userStr = "test";
				if (userStr.equals("a"))
					userStr = "admin";
				if ("test".equals(userStr))
					passwordStr = "testing7";
				if ("admin".equals(userStr))
					passwordStr = "control7";
			}
			for (User u : sharedData.getUsers()) {
				if (u.matchLogin(userStr, passwordStr)) {
					sessionData.setUser(u);
					break;
				}
			}
			if (sessionData.isUserSet()) {
				sessionData.setPendingNotification(new PendingNotification("Login success", NotificationType.SUCCESS));
				getUI().ifPresent(ui->ui.getPage().reload());
			} else {
				ToolboxVaadin.showWarningNotification("Invalid username/password");
				if (!userStr.equals(""))
					passwordTF.focus();
				else
					userTF.focus();
			}
		});
		return btn;
	}
}
