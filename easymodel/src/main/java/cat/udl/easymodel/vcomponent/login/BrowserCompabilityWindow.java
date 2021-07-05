package cat.udl.easymodel.vcomponent.login;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.vaadin.shared.ui.ContentMode;
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

public class BrowserCompabilityWindow extends Window{
	private VerticalLayout windowVL;
	private BrowserCompabilityWindow thisWindow;

	public BrowserCompabilityWindow() {
		super();

		thisWindow=this;
		this.setCaption("Browser Compability");
		this.setClosable(true);
		this.setModal(false);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(true);
		this.center();
		this.setWidth("800px");
		this.setHeight("300px");

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

		Grid<BrowserCompabilityEntry> browserGrid = new Grid<BrowserCompabilityEntry>();
		browserGrid.setCaption("Tested Browsers Table");
//		browserGrid.setWidth("100%");
//		browserGrid.setHeight("200px");
		browserGrid.setSizeFull();
		browserGrid.addColumn(BrowserCompabilityEntry::getOsName).setCaption("OS");
		browserGrid.addColumn(BrowserCompabilityEntry::getOsVersion).setCaption("OS Version");
		browserGrid.addColumn(BrowserCompabilityEntry::getChrome).setCaption("Chrome");
		browserGrid.addColumn(BrowserCompabilityEntry::getFirefox).setCaption("Firefox");
		browserGrid.addColumn(BrowserCompabilityEntry::getEdge).setCaption("Microsoft Edge");
		browserGrid.addColumn(BrowserCompabilityEntry::getSafari).setCaption("Safari");
		browserGrid.setItems(getBrowserGridItems());
		
//		VerticalLayout valuesPanelVL = new VerticalLayout();
//		valuesPanelVL.setSpacing(true);
//		valuesPanelVL.setMargin(false);
//		valuesPanelVL.setWidth("100%");
//		valuesPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
//		valuesPanelVL.addComponents(browserTable);
//		valuesPanelVL.setExpandRatio(browserTable, 1.0f);
//
//		Panel valuesPanel = new Panel();
//		valuesPanel.setSizeFull();
//		valuesPanel.setStyleName("withoutborder");
//		valuesPanel.setContent(valuesPanelVL);

//		Label lbl = new Label(SharedData.appName+" is compatible with most browsers", ContentMode.HTML);
//		lbl.setWidth("750px");
//		windowVL.addComponent(lbl);
		
		windowVL.addComponent(browserGrid);
		windowVL.setExpandRatio(browserGrid, 1.0f);
	}

	private Collection<BrowserCompabilityEntry> getBrowserGridItems() {
		ArrayList<BrowserCompabilityEntry> ret = new ArrayList<>();
		ret.add(new BrowserCompabilityEntry("Linux", "Ubuntu 20.04.1 LTS", "87.0.4280.141", "84.0.2", "n/a", "n/a"));
		ret.add(new BrowserCompabilityEntry("MacOS", "10.15 Catalina", "87.0.4280.141", "84.0.2", "n/a", "14.0.2"));
		ret.add(new BrowserCompabilityEntry("Windows", "10", "87.0.4280.141", "84.0.2", "87.0.664.75", "n/a"));
		return ret;
	}
}
