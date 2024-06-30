package cat.udl.easymodel.views.mainlayout.dialog;

import java.util.Properties;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.common.ReCaptcha;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class ContactDialog extends Dialog {
	private VerticalLayout reCaptchaVL;
	private TextField nameTF;
	private TextField emailTF;
	private TextArea messageTA;
	private ReCaptcha captcha;
	private ContactDialog thisDialog;

	public ContactDialog() {
		super();

		thisDialog=this;
		this.setModal(true);
		this.setResizable(true);
		this.setDraggable(true);
		this.setWidth("500px");
		this.setHeight("500px");

		VerticalLayout winVL = new VerticalLayout();
		winVL.setSpacing(true);
		winVL.setPadding(false);
		winVL.setSizeFull();

		VerticalLayout mainVL = new VerticalLayout();
		mainVL.setSpacing(false);
		mainVL.setPadding(false);
		mainVL.setSizeFull();
		mainVL.setClassName("scroll");

		reCaptchaVL = new VerticalLayout();
		reCaptchaVL.setHeight("77px");
		reCaptchaVL.setWidth("200px");
		reCaptchaVL.setPadding(false);
		reCaptchaVL.setSpacing(false);

		winVL.add(ToolboxVaadin.getDialogHeader(this,"Contact Form",null),mainVL);

		this.add(winVL);

		Span headLabel = new Span("Please use this form to contact us about errors, suggestions, compliments, etc.");
		headLabel.setWidth("100%");
		nameTF = new TextField();
		nameTF.setLabel("Your Name");
		nameTF.setWidth("100%");
		nameTF.setRequiredIndicatorVisible(true);
		emailTF = new TextField();
		emailTF.setLabel("Your Email (to receive reply)");
		emailTF.setWidth("100%");
		emailTF.setRequiredIndicatorVisible(true);
		messageTA = new TextArea();
		messageTA.setLabel("Your Message");
		messageTA.setRequiredIndicatorVisible(true);
		messageTA.setMinHeight("100px");
		messageTA.setWidth("100%");
		messageTA.setMaxLength(5000);
		reloadReCaptcha();

		mainVL.add(headLabel, nameTF, emailTF, messageTA, reCaptchaVL, getSubmitButton());
		mainVL.expand(messageTA);

		nameTF.focus();
	}

	private void checkForm() throws Exception {
		if (nameTF.getValue().isEmpty())
			throw new Exception("Please fill your name");
		if (!emailTF.getValue().matches("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$"))
			throw new Exception("Invalid e-mail");
		if (messageTA.getValue().isEmpty())
			throw new Exception("Please fill your message");
		if (!captcha.isValid()) {
			reloadReCaptcha();
			throw new Exception("Please complete reCAPTCHA");
		}
	}

	private void reloadReCaptcha() {
		reCaptchaVL.removeAll();
		SharedData sharedData = SharedData.getInstance();
		captcha = new ReCaptcha(sharedData.getProperties().getProperty("reCaptcha-public-key"),sharedData.getProperties().getProperty("reCaptcha-private-key"));
		reCaptchaVL.add(captcha);
	}

	private Button getSubmitButton() {
		Button btn = new Button("Submit");
		btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		btn.addClickListener(event -> {
				try {
					checkForm();
					sendMail();
					ToolboxVaadin.showSuccessNotification("Message successfully sent");
					thisDialog.close();
				} catch (Exception e) {
					ToolboxVaadin.showWarningNotification(e.getMessage());
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
}
