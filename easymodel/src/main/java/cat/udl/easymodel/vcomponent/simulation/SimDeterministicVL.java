package cat.udl.easymodel.vcomponent.simulation;

import java.io.File;

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

public class SimDeterministicVL extends VerticalLayout {
	private SessionData sessionData;
	private SharedData sharedData = SharedData.getInstance();
	private Accordion accordion;
	private Button runSimBtn;

	private Model selectedModel;
	private SimConfig simConfig;
	private AppPanel mainPanel;

	private SimPlotViewsVL plotViewsVL;

	private VerticalLayout dynVisVL;
	private VerticalLayout ssVisVL;

	public SimDeterministicVL(AppPanel mainPanel) {
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

		dynVisVL = new VerticalLayout();
		dynVisVL.setSpacing(false);
		dynVisVL.setMargin(false);
		dynVisVL.setSizeFull();
		ssVisVL = new VerticalLayout();
		ssVisVL.setSpacing(false);
		ssVisVL.setMargin(false);
		ssVisVL.setSizeFull();

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

		VerticalLayout dynVL = getDynVL();
		VerticalLayout ssVL = getSSVL();
		VerticalLayout plotVL = new SimPlotVL(simConfig.getDeterministicPlotSettings());
		plotViewsVL = new SimPlotViewsVL(simConfig, selectedModel);

		acc.addTab(dynVL, "Dynamic Simulation");
		acc.addTab(ssVL, "Steady State Simulation");
		acc.addTab(plotVL, "Plot Settings");
		acc.addTab(plotViewsVL, "Plot Views");

		return acc;
	}

	private VerticalLayout getDynVL() {
		Component comp = null;
		VerticalLayout leftVL = new VerticalLayout();
		leftVL.setWidth("100%");
		leftVL.setSpacing(true);
		leftVL.setMargin(false);
		comp = SimConfigToComponent.convert(simConfig.getDynamic().get("Enable"), null);
		((CheckBox) comp).addValueChangeListener(new ValueChangeListener<Boolean>() {
			@Override
			public void valueChange(ValueChangeEvent<Boolean> event) {
				Boolean newVal = (Boolean) event.getValue();
				dynVisVL.setVisible(newVal);
			}
		});
		leftVL.addComponent(comp);
		for (SimConfigEntry en : simConfig.getDynamic()) {
			if (!en.getId().equals("Enable")) {
				comp = SimConfigToComponent.convert(en, null);
				dynVisVL.addComponent(comp);
			}
		}
		leftVL.addComponent(dynVisVL);
		dynVisVL.setVisible((Boolean) simConfig.getDynamic().get("Enable").getValue());

		VerticalLayout rightVL = new VerticalLayout();
		rightVL.setWidth("50px");
		rightVL.setHeight("100%");
		rightVL.setSpacing(false);
		rightVL.setMargin(false);
		Button iBtn = getInfoDynButton();
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

	private Button getInfoDynButton() {
		Button btn = new Button();
		btn.setDescription("How to set Dynamic simulation");
		btn.setWidth("36px");
		btn.setStyleName("infoBtn");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				sessionData.showInfoWindow("Dynamic simulation allows you to:\n"
						+ "1. Run a time course of your system\n"
						+ "2. Calculate the dynamic gains of your system with respect to its independent variables by checkboxing \"Gains\"\n"
						+ "3. Calculate the dynamic sensitivities of your system with respect to its parameters by checkboxing \"Sensitivities\"",
						800, 400);
			}
		});
		return btn;
	}

	private Button getInfoSSButton() {
		Button btn = new Button();
		btn.setDescription("How to set Steady State simulation");
		btn.setWidth("36px");
		btn.setStyleName("infoBtn");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				sessionData.showInfoWindow("Steady State simulation allows you to:\n"
						+ "1. Calculate the Steady State(s) of your system\n"
						+ "2. Analyse the stability of the Steady State(s) by checkboxing \"Stability analysis\"\n"
						+ "3. Calculate the steady state gains of your system with respect to its independent variables by checkboxing \"Gains\""
						+ "4. Calculate the steady state sensitivities of your system with respect to its parameters by checkboxing \"Sensitivities\"",
						800, 400);
			}
		});
		return btn;
	}

	private VerticalLayout getSSVL() {
		Component comp = null;
		VerticalLayout leftVL = new VerticalLayout();
		leftVL.setWidth("100%");
		leftVL.setSpacing(true);
		leftVL.setMargin(false);
		comp = SimConfigToComponent.convert(simConfig.getSteadyState().get("Enable"), null);
		((CheckBox) comp).addValueChangeListener(new ValueChangeListener<Boolean>() {
			@Override
			public void valueChange(ValueChangeEvent<Boolean> event) {
				Boolean newVal = (Boolean) event.getValue();
				ssVisVL.setVisible(newVal);
			}
		});
		leftVL.addComponent(comp);
		for (SimConfigEntry en : simConfig.getSteadyState()) {
			if (!en.getId().equals("Enable")) {
				comp = SimConfigToComponent.convert(en, null);
				ssVisVL.addComponent(comp);
			}
		}
		leftVL.addComponent(ssVisVL);
		ssVisVL.setVisible((Boolean) simConfig.getSteadyState().get("Enable").getValue());

		VerticalLayout rightVL = new VerticalLayout();
		rightVL.setWidth("50px");
		rightVL.setHeight("100%");
		rightVL.setSpacing(false);
		rightVL.setMargin(false);
		Button iBtn = getInfoSSButton();
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

	private Button getSimulateButton() {
		Button btn = new Button("Run Simulation");
		// btn.setWidth("60%");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					if (!sessionData.isSimulating()) {
						simConfig.setSimType(SimType.DETERMINISTIC);
						simConfig.checkSimConfigs();
						plotViewsVL.updatePlotTabs();
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
				sessionData.showInfoWindow("Procedure for deterministic simulation:\r\n"
						+ "1. Configure the dynamic simulation.\r\n" + "    All parameters should be positive.\r\n"
						+ "    Final time should be larger than initial time.\r\n"
						+ "    If sensitivity analysis is to be made, please tick the appropriate checkboxes.\r\n"
						+ "2. Configure the steady state simulation. Enable the checkbox, if a steady state simulation is required.\r\n"
						+ "3. Configure plot settings.\r\n" + "4. Configure variable representation in Plot Views.\r\n"
						+ "5. Run Simulation.", 800, 400);
			}
		});
		return btn;
	}
}
