package cat.udl.easymodel.vcomponent.login;

import java.sql.SQLException;
import java.util.ArrayList;

import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.wcs.wcslib.vaadin.widget.recaptcha.ReCaptcha;
import com.wcs.wcslib.vaadin.widget.recaptcha.shared.ReCaptchaOptions;

import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.ToolboxVaadin;

public class RegisterWindow extends Window {
	private static final long serialVersionUID = 1L;

	private VerticalLayout windowVL;
	private RegisterWindow thisClass;
	private SessionData sessionData;
	private SharedData sharedData;

	private TextField user;
	private PasswordField pass, pass2;
	private ReCaptcha captcha;
	private ArrayList<User> allUsers;
	private User newUser = new User(null, "", "", UserType.USER);

	public RegisterWindow(ArrayList<User> allUsers) {
		super();

		thisClass = this;
		this.allUsers = allUsers;
		sharedData = SharedData.getInstance();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.setCaption("Create New User Account");
		this.setClosable(true);
		this.setData(false); // window closed by user
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(false);
		this.center();
		this.setWidth("400px");
		this.setHeight("400px");

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		// this.addShortcutListener(new ShortcutListener("Shortcut enter",
		// ShortcutAction.KeyCode.ENTER, null) {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void handleAction(Object sender, Object target) {
		// checkAndClose();
		// }
		// });
		displayWindowContent();
		user.focus();
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		user = new TextField("Username");
		user.setData(false);
		user.setWidth("100%");
		user.setRequiredIndicatorVisible(true);
		user.setDescription(ToolboxVaadin.usernameRegexInfo);
		user.addBlurListener(new BlurListener() {

			@Override
			public void blur(BlurEvent event) {
				String newVal = ((TextField) event.getComponent()).getValue();
				newUser.setName(newVal);
			}
		});

		pass = new PasswordField("Password");
		pass.setWidth("100%");
		pass.setRequiredIndicatorVisible(true);
		pass.setValue("");
		pass.setDescription(ToolboxVaadin.passwordRegexInfo);
		pass.addBlurListener(new BlurListener() {

			@Override
			public void blur(BlurEvent event) {
				String newVal = ((PasswordField) event.getComponent()).getValue();
				newUser.setPassForRegister(newVal);
			}
		});

		pass2 = new PasswordField("Retype Password");
		pass2.setWidth("100%");
		pass2.setRequiredIndicatorVisible(true);
		pass2.setValue("");
		pass2.setData(false);
		pass2.setDescription("Retype previous password");
		pass2.addBlurListener(new BlurListener() {

			@Override
			public void blur(BlurEvent event) {
				String newVal = ((PasswordField) event.getComponent()).getValue();
				newUser.setRetypePassForRegister(newVal);
			}
		});

		createReCaptcha();

		VerticalLayout valuesPanelVL = new VerticalLayout();
		valuesPanelVL.setSpacing(true);
		valuesPanelVL.setMargin(false);
		valuesPanelVL.setWidth("100%");
		valuesPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		valuesPanelVL.addComponents(user, pass, pass2, captcha, getRegisterButton());

		Panel valuesPanel = new Panel();
		valuesPanel.setSizeFull();
		valuesPanel.setStyleName("withoutborder");
		valuesPanel.setContent(valuesPanelVL);

		windowVL.addComponent(valuesPanel);
		windowVL.setExpandRatio(valuesPanel, 1.0f);
	}

	private void checkForm() throws Exception {
		newUser.validateForRegister(allUsers);
		if (!captcha.validate()) {
			captcha.reload();
			throw new Exception("Please complete reCAPTCHA");
		}
	}
	
	private Component getRegisterButton() {
		Button btn = new Button("Register");
		btn.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					checkForm();
					try {
						newUser.saveDB();
						allUsers.add(newUser);
						sessionData.setUser(newUser);
						thisClass.setData(true);
						Notification.show("User Registration Success", Type.TRAY_NOTIFICATION);
						thisClass.close();
					} catch (SQLException sqlE) {
						sqlE.printStackTrace();
						Notification.show(SharedData.dbError, Notification.Type.WARNING_MESSAGE);
					}
				} catch (Exception e) {
					Notification.show(e.getMessage(), Notification.Type.WARNING_MESSAGE);
				}
			}
		});
		return btn;
	}

	private void createReCaptcha() {
		captcha = new ReCaptcha(sharedData.getProperties().getProperty("reCaptcha-private-key"),
				new ReCaptchaOptions() {
					{
						// your options
						theme = "light";
						sitekey = sharedData.getProperties().getProperty("reCaptcha-public-key");
					}
				});
		captcha.setHeight("77px");
	}
}
