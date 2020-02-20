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
import cat.udl.easymodel.thread.SimulationManagerThread;
import cat.udl.easymodel.vcomponent.app.AppPanel;

public class SimStochasticVL extends VerticalLayout {
	private SessionData sessionData;
	private SharedData sharedData = SharedData.getInstance();
	private Accordion accordion;
	private Button runSimBtn;

	private Model selectedModel;
	private SimConfig simConfig;
	private AppPanel mainPanel;

	private VerticalLayout stochVisVL;

	public SimStochasticVL(AppPanel mainPanel) {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.selectedModel = sessionData.getSelectedModel();
		this.simConfig = selectedModel.getSimConfig();
		this.mainPanel = mainPanel;
		simConfig.initPlotViews(selectedModel.getAllSpeciesTimeDependent());

		this.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		this.setSpacing(true);
		this.setMargin(false);
		this.setStyleName("simConfig");

		stochVisVL = new VerticalLayout();
		stochVisVL.setSpacing(false);
		stochVisVL.setMargin(false);
		stochVisVL.setSizeFull();

		accordion = getAccordion();
		HorizontalLayout buttonsHL = getButtonsHL();
		this.addComponents(getHeaderHL(), accordion, buttonsHL);

		if (sharedData.isDebug())
			runSimBtn.focus();
	}

	private HorizontalLayout getButtonsHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
		hl.setSpacing(false);
		runSimBtn = getSimulateButton();
		hl.addComponents(runSimBtn);
		return hl;
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

	private Accordion getAccordion() {
		Accordion acc = new Accordion();
		acc.setId("simAccordion");
		acc.setWidth("100%");

		VerticalLayout stochVL = getStochVL();
		VerticalLayout plotVL = new SimPlotVL(simConfig.getStochasticPlotSettings());

		acc.addTab(stochVL, "Stochastic Simulation");
		acc.addTab(plotVL, "Plot Settings");

		return acc;
	}

	private VerticalLayout getStochVL() {
		Component comp = null;
		VerticalLayout leftVL = new VerticalLayout();
		leftVL.setWidth("100%");
		leftVL.setSpacing(true);
		leftVL.setMargin(false);
		for (SimConfigEntry en : simConfig.getStochastic()) {
			comp = SimConfigToComponent.convert(en, null);
			stochVisVL.addComponent(comp);
		}
		leftVL.addComponent(stochVisVL);
		// stochVisVL.setVisible(true);

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
		hl.setMargin(false);
		hl.addComponents(leftVL, rightVL);
		hl.setExpandRatio(leftVL, 1f);

		VerticalLayout mainVL = new VerticalLayout();
		mainVL.setSizeFull();
		mainVL.setMargin(true);
		mainVL.setSpacing(false);
		mainVL.addComponent(hl);
		return mainVL;
	}

	private Button getInfoStochButton() {
		Button btn = new Button();
		btn.setDescription("Stochastic simulation");
		btn.setWidth("36px");
		btn.setStyleName("infoBtn");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				sessionData.showInfoWindow("Stochastic simulation calculates the number of molecules in your system at each time step.", 800,200);
			}
		});
		return btn;
	}

	private Button getSimulateButton() {
		Button btn = new Button("Run Simulation");
		// btn.setWidth("60%");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					if (!sessionData.isSimulating()) {
						simConfig.setSimType(SimType.STOCHASTIC);
						simConfig.checkSimConfigs();
						selectedModel.checkIfReadyToSimulate();
						// launch sim threads
						sessionData.launchSimulation();
						mainPanel.showResults();
					} else
						Notification.show("Please wait for the previous simulation to finish", Type.WARNING_MESSAGE);
				} catch (Exception e) {
					if (e instanceof MathLinkException)
						Notification.show("webMathematica error", Type.WARNING_MESSAGE);
					else
						Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
				}
			}
		});
		return btn;
	}

	private Button getInfoButton() {
		Button btn = new Button();
		btn.setDescription("How to configure simulation");
		btn.setWidth("36px");
		btn.setStyleName("infoBtn");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				sessionData.showInfoWindow("Procedure for stochastic simulation:\r\n" + 
						"1. Configure the simulation.\r\n" + 
						"    All parameters should be positive.\r\n" + 
						"    Final time should be larger than initial time.\r\n" + 
						"    Time step should be small.\r\n" + 
						"    Please tick the appropriate checkbox if time-dependent intrinsinc noise is to be represented.\r\n" + 
						"2. Configure plot settings.\r\n" + 
						"3. Run Simulation.",800,400);
			}
		});
		return btn;
	}
}
