package cat.udl.easymodel.vcomponent.useraccount;

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
import com.vaadin.ui.Label;
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

public class EditUserAccountWindow extends Window {
	private static final long serialVersionUID = 1L;

	private VerticalLayout windowVL;
	private EditUserAccountWindow thisClass;
	private SessionData sessionData;
	private SharedData sharedData;

	private User user;
	private PasswordField currentPass, pass, pass2;

	public EditUserAccountWindow() {
		super();

		thisClass = this;
		sharedData = SharedData.getInstance();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		user=this.sessionData.getUser();
		
		this.setCaption("User Account Settings");
		this.setClosable(true);
		this.setData(false); // window closed by user
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(false);
		this.center();
		this.setWidth("400px");
		this.setHeight("350px");

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		displayWindowContent();
		currentPass.focus();
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		Label userLabel = new Label("Your username: "+sessionData.getUser().getName());
		
		Label changePassword = new Label("Change password");

		currentPass = new PasswordField("Type your current password");
		currentPass.setWidth("100%");
		currentPass.setRequiredIndicatorVisible(true);
		currentPass.setValue("");
		currentPass.setDescription(ToolboxVaadin.passwordRegexInfo);
		
		pass = new PasswordField("New password");
		pass.setWidth("100%");
		pass.setRequiredIndicatorVisible(true);
		pass.setValue("");
		pass.setDescription(ToolboxVaadin.passwordRegexInfo);
		pass.addBlurListener(new BlurListener() {

			@Override
			public void blur(BlurEvent event) {
				String newVal = ((PasswordField) event.getComponent()).getValue();
				user.setPass(newVal);
			}
		});

		pass2 = new PasswordField("Retype new password");
		pass2.setWidth("100%");
		pass2.setRequiredIndicatorVisible(true);
		pass2.setValue("");
		pass2.setData(false);
		pass2.setDescription("Retype previous password");
		pass2.addBlurListener(new BlurListener() {

			@Override
			public void blur(BlurEvent event) {
				String newVal = ((PasswordField) event.getComponent()).getValue();
				user.setRetypePass(newVal);
			}
		});

		VerticalLayout valuesPanelVL = new VerticalLayout();
		valuesPanelVL.setSpacing(true);
		valuesPanelVL.setMargin(false);
		valuesPanelVL.setWidth("100%");
		valuesPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		VerticalLayout passVL = ToolboxVaadin.getRawVL(null);
		passVL.setCaption("Password");
		passVL.addComponents(currentPass, pass, pass2);
		valuesPanelVL.addComponents(userLabel, passVL, getSaveButton());

		Panel valuesPanel = new Panel();
		valuesPanel.setSizeFull();
		valuesPanel.setStyleName("withoutborder");
		valuesPanel.setContent(valuesPanelVL);

		windowVL.addComponent(valuesPanel);
		windowVL.setExpandRatio(valuesPanel, 1.0f);
	}

	private void checkForm() throws Exception {
		if (user==SharedData.getInstance().getGuestUser())
			throw new Exception("Guest user can't change password");
		if (!user.matchLogin(user.getName(), currentPass.getValue()))
			throw new Exception("Incorrect current password");
		user.validateForRegister(null);
	}
	
	private Component getSaveButton() {
		Button btn = new Button("Save");
		btn.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					checkForm();
					try {
						user.saveDB();
						thisClass.setData(true);
						Notification.show("User Account updated", Type.TRAY_NOTIFICATION);
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
}
