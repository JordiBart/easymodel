package cat.udl.easymodel.vcomponent.register.window;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.logic.user.UserImpl;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.sbml.SBMLMan;
import cat.udl.easymodel.utils.BCrypt;
import cat.udl.easymodel.utils.VaadinUtils;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.wcs.wcslib.vaadin.widget.recaptcha.ReCaptcha;
import com.wcs.wcslib.vaadin.widget.recaptcha.shared.ReCaptchaOptions;

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
		user.setWidth("150px");
		user.setRequired(true);
		user.setDescription(VaadinUtils.usernameRegexInfo);
		user.addBlurListener(new BlurListener() {

			@Override
			public void blur(BlurEvent event) {
				String newVal = ((TextField) event.getComponent()).getValue();
				if (newVal.isEmpty()) {
					user.setData(false);
					user.setStyleName("");
				} else {
					boolean found = false;
					for (User u : allUsers) {
						if (u.getName().equals(newVal)) {
							found = true;
							break;
						}
					}
					if (!found && newVal.matches(VaadinUtils.usernameRegex)) {
						user.setData(true);
						user.setStyleName("greenBG");
					} else {
						user.setData(false);
						user.setStyleName("redBG");
					}
				}
			}
		});

		pass = new PasswordField("Password");
		pass.setWidth("150px");
		pass.addValidator(new PasswordValidator());
		pass.setRequired(true);
		pass.setValue("");
		pass.setNullRepresentation("");
		pass.setDescription(VaadinUtils.passwordRegexInfo);
		pass.addBlurListener(new BlurListener() {

			@Override
			public void blur(BlurEvent event) {
				String newVal = ((PasswordField) event.getComponent()).getValue();

				if (newVal.isEmpty()) {
					pass.setStyleName("");
				} else if (pass.isValid()) {
					pass.setStyleName("greenBG");
				} else {
					pass.setStyleName("redBG");
				}
			}
		});
		
		pass2 = new PasswordField("Retype Password");
		pass2.setWidth("150px");
		pass2.setRequired(true);
		pass2.setValue("");
		pass2.setNullRepresentation("");
		pass2.setData(false);
		pass2.setDescription("Retype previous password");
		pass2.addBlurListener(new BlurListener() {

			@Override
			public void blur(BlurEvent event) {
				String newVal = ((PasswordField) event.getComponent()).getValue();

				if (newVal.isEmpty()) {
					pass2.setData(false);
					pass2.setStyleName("");
				} else if (newVal.equals(pass.getValue())) {
					pass2.setData(true);
					pass2.setStyleName("greenBG");
				} else {
					pass2.setData(false);
					pass2.setStyleName("redBG");
				}
			}
		});

		createReCaptcha();

		VerticalLayout valuesPanelVL = new VerticalLayout();
		valuesPanelVL.setSpacing(true);
		valuesPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		valuesPanelVL.addComponents(user, pass, pass2, captcha, getRegisterButton());

		Panel valuesPanel = new Panel();
		valuesPanel.setSizeFull();
		valuesPanel.setStyleName("withoutborder");
		valuesPanel.setContent(valuesPanelVL);

		windowVL.addComponent(valuesPanel);
		windowVL.setExpandRatio(valuesPanel, 1.0f);
	}

	private Component getRegisterButton() {
		Button btn = new Button("Register");
		btn.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					checkForm();
					User newUser = new UserImpl(null, user.getValue(), BCrypt.hashpw(pass.getValue(), BCrypt.gensalt()), UserType.USER);
					try {
						sharedData.getDbManager().insertNewUserDB(newUser);
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

	private void checkForm() throws Exception {
		if (!(boolean) user.getData())
			throw new Exception("Invalid Username");
		if (!pass.isValid())
			throw new Exception("Invalid Password");
		if (!(boolean) pass2.getData())
			throw new Exception("Retype Password doesn't match Password");
		if (!captcha.validate()) {
			captcha.reload();
			throw new Exception("Please complete reCAPTCHA");
		}
	}

	private void createReCaptcha() {
		captcha = new ReCaptcha(sharedData.getProperties().getProperty("reCaptcha-private-key"),
				new ReCaptchaOptions() {
					{// your options
						theme = "light";
						sitekey = sharedData.getProperties().getProperty("reCaptcha-public-key");
					}
				});
	}

	private static final class PasswordValidator extends AbstractValidator<String> {
		public PasswordValidator() {
			super(VaadinUtils.passwordRegexInfo);
		}

		@Override
		protected boolean isValidValue(String value) {
			if (value != null && !value.matches(VaadinUtils.passwordRegex)) {
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
