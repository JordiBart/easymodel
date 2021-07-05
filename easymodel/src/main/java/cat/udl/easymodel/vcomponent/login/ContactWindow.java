package cat.udl.easymodel.vcomponent.login;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.wcs.wcslib.vaadin.widget.recaptcha.ReCaptcha;
import com.wcs.wcslib.vaadin.widget.recaptcha.shared.ReCaptchaOptions;

import cat.udl.easymodel.main.SharedData;

public class ContactWindow extends Window {
	private static final long serialVersionUID = 1L;

	private VerticalLayout windowVL;
	private TextField nameTF;
	private TextField emailTF;
	private TextArea messageTA;
	private ReCaptcha captcha;
	private ContactWindow thisWindow;

	public ContactWindow() {
		super();

		thisWindow=this;
		this.setCaption("Contact form");
		this.setClosable(true);
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(true);
		this.center();
		this.setWidth("500px");
		this.setHeight("540px");

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		updateWindowContent();
	}

	private void updateWindowContent() {
		windowVL.removeAllComponents();

		Label label = new Label("Please use this form to contact us about errors, suggestions, compliments, information...");
		label.setWidth("100%");
		nameTF = new TextField();
		nameTF.setCaption("Your name");
		nameTF.setWidth("100%");
		nameTF.setRequiredIndicatorVisible(true);
		emailTF = new TextField();
		emailTF.setCaption("Your email to receive our answer");
		emailTF.setWidth("100%");
		emailTF.setRequiredIndicatorVisible(true);
		messageTA = new TextArea();
		messageTA.setRequiredIndicatorVisible(true);
		messageTA.setCaption("Your message");
		messageTA.setSizeFull();
		createReCaptcha();

		VerticalLayout valuesPanelVL = new VerticalLayout();
		valuesPanelVL.setSpacing(true);
		valuesPanelVL.setMargin(false);
		valuesPanelVL.setWidth("100%");
		valuesPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		valuesPanelVL.addComponents(label, nameTF, emailTF, messageTA, captcha, getSubmitButton());
		valuesPanelVL.setExpandRatio(messageTA, 1.0f);

		Panel valuesPanel = new Panel();
		valuesPanel.setSizeFull();
		valuesPanel.setStyleName("withoutborder");
		valuesPanel.setContent(valuesPanelVL);

		windowVL.addComponent(valuesPanel);
		windowVL.setExpandRatio(valuesPanel, 1.0f);
		nameTF.focus();
	}

	private void checkForm() throws Exception {
		if (nameTF.getValue().isEmpty())
			throw new Exception("Please fill your name");
		if (!emailTF.getValue().matches("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$"))
			throw new Exception("Invalid e-mail");
		if (messageTA.getValue().isEmpty())
			throw new Exception("Please fill your message");
		if (!captcha.validate()) {
			captcha.reload();
			throw new Exception("Please complete reCAPTCHA");
		}
	}

	private Component getSubmitButton() {
		Button btn = new Button("Submit");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					checkForm();
					sendMail();
					Notification.show("Message sucessfully sent", Notification.Type.TRAY_NOTIFICATION);
					thisWindow.close();
				} catch (Exception e) {
					Notification.show(e.getMessage(), Notification.Type.WARNING_MESSAGE);
				}
			}
		});
		return btn;
	}

	private void sendMail() throws Exception {
		Properties prop = SharedData.getInstance().getProperties();
		if ("".equals(prop.getProperty("contactMails")) || "".equals(prop.getProperty("hostname")))
			throw new Exception("Error: invalid server configuration");
		String from = SharedData.appName.toLowerCase()+"@"+prop.getProperty("hostname");
		String msg = "From: '"+nameTF.getValue()+"' "+emailTF.getValue()+"\n-------------------------\n"+messageTA.getValue();
		String host = "localhost";

		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", host);
		Session session = Session.getDefaultInstance(properties);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			for (String to : prop.getProperty("contactMails").split(":"))
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(SharedData.appName + " web form user feedback");
			message.setText(msg);

			Transport.send(message);
		} catch (MessagingException mex) {
			mex.printStackTrace();
			throw new Exception("Error: message could not be sent");
		}
	}

	private void createReCaptcha() {
		SharedData sharedData = SharedData.getInstance();
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
