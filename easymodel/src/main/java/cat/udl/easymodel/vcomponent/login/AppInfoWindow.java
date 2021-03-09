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
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.wcs.wcslib.vaadin.widget.recaptcha.ReCaptcha;
import com.wcs.wcslib.vaadin.widget.recaptcha.shared.ReCaptchaOptions;

import cat.udl.easymodel.main.SharedData;

public class AppInfoWindow extends Window{
	private VerticalLayout windowVL;
	private AppInfoWindow thisWindow;

	public AppInfoWindow() {
		super();

		thisWindow=this;
		this.setCaption(SharedData.appName + " information");
		this.setClosable(true);
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(true);
		this.center();
		this.setWidth("600px");
		this.setHeight("600px");

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

		TextArea infoTA = new TextArea();
		infoTA.setCaption("Application description");
		infoTA.setValue(SharedData.appName + " is a user-friendly web server for model building, simulation, and analysis in systems biology.\n"+
	"Calculus core is relied on Wolfram webMathematica.\n\n"+
	"It allows to:\n"+
	"-Create or load preexisting models; including the import of SBML file models.\n"+
	"-Define complex kinetic rates or importing them from the predefined rate list.\n"+
	"-Displaying of the Stoichiometry matrix and the Regulatory matrix.\n"+
	"-Performing deterministic simulations including both Dynamic time course simulation and Steady State finding simulation.\n"+
	"-Performing deterministic gains and sensitivities analysis.\n"+
	"-Performing Steady State stability analysis.\n"+
	"-Performing stochastic simulations; including both the Gillespie method and an efficient tau leaping method for time course simulation.\n"+
	"-Performing parameter scan and independent variable scan.\n"+
	"-Exporting the model in the SBML file format.\n"+
	"-Exporting the model and simulation code in a Mathematica nootebook file.\n"+
	"-Store user models\n"+
	"-And many more\n"
	);
		infoTA.setReadOnly(true);
		infoTA.setSizeFull();

		VerticalLayout valuesPanelVL = new VerticalLayout();
		valuesPanelVL.setSpacing(true);
		valuesPanelVL.setMargin(false);
		valuesPanelVL.setWidth("100%");
		valuesPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		valuesPanelVL.addComponents(infoTA);
//		valuesPanelVL.setExpandRatio(essageTA, 1.0f);

		Panel valuesPanel = new Panel();
		valuesPanel.setSizeFull();
		valuesPanel.setStyleName("withoutborder");
		valuesPanel.setContent(valuesPanelVL);

		windowVL.addComponent(valuesPanel);
		windowVL.setExpandRatio(valuesPanel, 1.0f);
	}
}
