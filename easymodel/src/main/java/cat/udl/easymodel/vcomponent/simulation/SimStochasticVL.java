package cat.udl.easymodel.vcomponent.simulation;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.simconfig.SimConfigEntry;
import cat.udl.easymodel.logic.types.SimType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.thread.SimulationLauncherThread;
import cat.udl.easymodel.thread.SimulationManagerThread;
import cat.udl.easymodel.utils.CException;
import cat.udl.easymodel.vcomponent.app.AppPanel;
import cat.udl.easymodel.vcomponent.common.InfoWindowButton;

public class SimStochasticVL extends VerticalLayout {
	private SessionData sessionData;
	private SharedData sharedData = SharedData.getInstance();
	private Accordion accordion;
	private Button runSimBtn;
	private CheckBox tauLeapingCB;

	private Model selectedModel;
	private SimConfig simConfig;
	private AppPanel mainPanel;

	public SimStochasticVL() {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.selectedModel = sessionData.getSelectedModel();
		this.simConfig = selectedModel.getSimConfig();
		this.mainPanel = this.sessionData.getAppPanel();

		this.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		this.setSpacing(true);
		this.setMargin(false);
		this.setStyleName("simConfig");

		this.addComponents(getStochVL());
	}

	private HorizontalLayout getHeaderHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setHeight("37px");
		FileResource resource = new FileResource(
				new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()
						+ "/VAADIN/themes/easymodel/img/easymodel-logo-120.png"));
		Image image = new Image(null, resource);
		image.setHeight("36px");
		HorizontalLayout head = new HorizontalLayout();
		head.setWidth("100%");
		head.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		head.addComponent(image);
		hl.addComponents(head, getInfoButton());
		hl.setExpandRatio(head, 1f);
		return hl;
	}

	private VerticalLayout getStochVL() {
		Component comp = null;
		VerticalLayout leftVL = new VerticalLayout();
		leftVL.setWidth("100%");
		leftVL.setSpacing(true);
		leftVL.setMargin(false);
		for (SimConfigEntry en : simConfig.getStochastic()) {
			comp = SimConfigToComponent.convert(en, null);
			leftVL.addComponent(comp);
			if (en.getId().equals("TauLeaping"))
				tauLeapingCB = (CheckBox) comp;
		}

		VerticalLayout rightVL = new VerticalLayout();
		rightVL.setWidth("50px");
		rightVL.setHeight("100%");
		rightVL.setSpacing(false);
		rightVL.setMargin(false);
		Button iBtn = getInfoStochButton();
		rightVL.setDefaultComponentAlignment(Alignment.TOP_RIGHT);
		rightVL.addComponents(iBtn);

		HorizontalLayout hl = new HorizontalLayout();
		hl.setSizeFull();
		hl.setSpacing(false);
		hl.setMargin(true);
		hl.addComponents(leftVL, rightVL);
		hl.setExpandRatio(leftVL, 1f);

		Accordion acc = new Accordion();
		acc.setCaption("Stochastic Simulation");
		acc.addTab(hl, "Stochastic Settings");
		acc.setSizeFull();

		VerticalLayout mainVL = new VerticalLayout();
		mainVL.setWidth("500px");
		mainVL.addComponent(acc);
		return mainVL;
	}

	public void setTauLeapingCBValue(boolean val) {
		tauLeapingCB.setValue(val);
	}
	
	private Button getInfoStochButton() {
		return new InfoWindowButton("Stochastic simulation",
				"Stochastic simulation calculates the number of molecules in your system at each time step.", 800, 200);
	}

	private Button getInfoButton() {
		return new InfoWindowButton("How to configure simulation", "Procedure for stochastic simulation:\r\n"
				+ "1. Configure the simulation.\r\n" + "    All parameters should be positive.\r\n"
				+ "    Final time should be larger than initial time.\r\n" + "    Time step should be small.\r\n"
				+ "    Please tick the appropriate checkbox if time-dependent intrinsinc noise is to be represented.\r\n"
				+ "2. Configure plot settings.\r\n" + "3. Run Simulation.", 800, 400);
	}
}
