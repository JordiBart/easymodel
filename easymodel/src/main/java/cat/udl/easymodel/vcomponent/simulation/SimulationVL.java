package cat.udl.easymodel.vcomponent.simulation;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.controller.SimulationCtrl;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.types.SimType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.CException;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.app.AppPanel;

public class SimulationVL extends VerticalLayout {
	private SessionData sessionData;
	private SharedData sharedData = SharedData.getInstance();
	private Panel conPanel;
	private SimLeftMenuVL simLeftMenuVL;
	private VerticalLayout conVL;
	private SimDeterministicVL simDeterministicVL;
	private SimStochasticVL simStochasticVL;
	private PopupView infoPopup = new PopupView(null, getDeterministicInfoLayout());
	private Button runSimBtn;

	private Model selectedModel;
	private SimConfig simConfig;
	private AppPanel mainPanel;

	private SimPlotViewsVL plotViewsVL;

	private VerticalLayout dynVisVL;
	private VerticalLayout ssVisVL;

	private PopupView infoDynPopup = new PopupView(null, getInfoDynLayout());
	private PopupView infoSSPopup = new PopupView(null, getInfoSSLayout());

	public SimulationVL() {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.mainPanel = this.sessionData.getAppPanel();
		this.selectedModel = sessionData.getSelectedModel();
		this.simDeterministicVL = new SimDeterministicVL();
		this.simStochasticVL = new SimStochasticVL();

		setSpacing(false);
		setMargin(true);
		setSizeFull();

		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setMargin(false);
		vl.setSizeFull();

		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.setMargin(false);
		hl.setSizeFull();
		conVL = new VerticalLayout();
		conVL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		conVL.setSpacing(false);
		conVL.setMargin(true);
		conVL.setSizeUndefined();
		conPanel = new Panel();
		// conPanel.setStyleName("reactionsPanel");
		conPanel.setSizeFull();
		conPanel.setContent(conVL);
		conPanel.getContent().setSizeUndefined();

		simLeftMenuVL = new SimLeftMenuVL(this);

		hl.addComponents(simLeftMenuVL, conPanel);
		hl.setExpandRatio(conPanel, 1f);

		VerticalLayout headerVL = getHeaderVL();
		vl.addComponents(headerVL, hl);
		vl.setExpandRatio(hl, 1f);
		this.addComponent(vl);

		updateConPanel();

//		super();
//		this.sessionData = (SessionData) UI.getCurrent().getData();
//		this.selectedModel = selectedModel;
//		this.simConfig = selectedModel.getSimConfig();
//		this.mainPanel = mainPanel;
//
//		this.setSizeFull();
//		this.setMargin(true);
//		this.setSpacing(true);
//
//		simConfig.initPlotViews(selectedModel.getAllSpeciesTimeDependent());
//
//		dynVisVL = new VerticalLayout();
//		dynVisVL.setSpacing(false);
//		dynVisVL.setMargin(false);
//		dynVisVL.setSizeFull();
//		ssVisVL = new VerticalLayout();
//		ssVisVL.setSpacing(false);
//		ssVisVL.setMargin(false);
//		ssVisVL.setSizeFull();
//
//		simVL = new VerticalLayout();
//		simVL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
//		simVL.setSpacing(true);
//		simVL.setMargin(true);
//		simVL.setWidth("100%");
//		updateConPanel();
//		if (sharedData.isDebug())
//			runSimBtn.focus();
//		conPanel = new Panel();
//		conPanel.setStyleName("reactionsPanel");
//		conPanel.setHeight("100%");
//		conPanel.setContent(simVL);
//
//		this.addComponent(conPanel);
//		this.setComponentAlignment(conPanel, Alignment.MIDDLE_CENTER);
	}

	private VerticalLayout getHeaderVL() {
		VerticalLayout vl = new VerticalLayout();
//		vl.setMargin(true);
		vl.setSpacing(false);
		vl.setWidth("100%");
		vl.setHeight("38px");
		vl.setStyleName("simHeaderVL");
		vl.addComponent(
				ToolboxVaadin.getStyledLabel("Simulation configuration for " + selectedModel.getName(), "textH2"));
		return vl;
	}

	void updateConPanel() {
		conVL.removeAllComponents();
		if (selectedModel.getSimConfig().getSimType() == SimType.DETERMINISTIC) {
			simLeftMenuVL.setEnableRunButton(true);
			conVL.addComponent(simDeterministicVL);
		} else if (selectedModel.getSimConfig().getSimType() == SimType.STOCHASTIC) {
			SimulationCtrl simCtrl = new SimulationCtrl(sessionData);
			try {
				sessionData.respawnMathLinkOp();
				boolean isTauLeapEfficient = simCtrl.quickStochasticSimulationCheck();
				simStochasticVL.setTauLeapingCBValue(isTauLeapEfficient);
				simLeftMenuVL.setEnableRunButton(true);
			} catch (Exception e) {
				// e.printStackTrace();
				if (sessionData.getMathLinkOp() != null) {
					simLeftMenuVL.setEnableRunButton(false);
					Notification.show(
							"This model isn't supported for stochastic simulation with the current parameters",
							Type.WARNING_MESSAGE);
				}
			} finally {
				sessionData.closeMathLinkOp();
			}
			conVL.addComponent(simStochasticVL);
		}
	}

//	private Component getSimSelectTypeVL() {
//		VerticalLayout vl = ToolboxVaadin.getRawVL("");
//		VerticalLayout panelVL = ToolboxVaadin.getRawVL("");
//		Button deterministicBtn = new Button("Deterministic");
//		deterministicBtn.addClickListener(new ClickListener() {
//			@Override
//			public void buttonClick(ClickEvent event) {
//				simType = SimType.DETERMINISTIC;
//				updateConPanel();
//			}
//		});
//		Button stochasticBtn = new Button("Stochastic");
//		stochasticBtn.addClickListener(new ClickListener() {
//			@Override
//			public void buttonClick(ClickEvent event) {
//				simType = SimType.STOCHASTIC;
//				updateConPanel();
//			}
//		});
//		panelVL.addComponents(deterministicBtn,stochasticBtn);
//		Panel panel = new Panel();
//		panel.setContent(panelVL);
//		vl.addComponents(getHeaderHL(), panel);
//		return vl;
//	}

//	private HorizontalLayout getButtonsHL() {
//		HorizontalLayout hl = new HorizontalLayout();
//		hl.setMargin(false);
//		hl.setSpacing(false);
//		runSimBtn = getSimulateButton();
//		hl.addComponents(runSimBtn);
//		return hl;
//	}

//	private HorizontalLayout getHeaderHL() {
//		HorizontalLayout hl = new HorizontalLayout();
//		hl.setWidth("100%");
//		hl.setHeight("37px");
//		FileResource resource = new FileResource(
//				new File(VaadinService.getCurrent()
//						.getBaseDirectory().getAbsolutePath() + "/VAADIN/themes/easymodel/img/easymodel-logo-120.png"));
//		Image image = new Image(null, resource);
//		image.setHeight("36px");
//		HorizontalLayout head = new HorizontalLayout();
//		head.setWidth("100%");
//		head.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
//		head.addComponent(image);
//		hl.addComponents(head, infoPopup, getInfoButton());
//		hl.setExpandRatio(head, 1f);
//		return hl;
//	}

//	private Accordion getAccordion() {
//		Accordion acc = new Accordion();
//		acc.setId("simAccordion");
//		acc.setWidth("100%");
//
//		VerticalLayout dynVL = getDynVL();
//		VerticalLayout ssVL = getSSVL();
//		VerticalLayout plotVL = new SimPlotVL(simConfig.getDeterministicPlotSettings());
//		plotViewsVL = new SimPlotViewsVL(simConfig, selectedModel);
//
//		acc.addTab(dynVL, "Dynamic Simulation");
//		acc.addTab(ssVL, "Steady State Simulation");
//		acc.addTab(plotVL, "Plot Settings");
//		acc.addTab(plotViewsVL, "Plot Views");
//
//		return acc;
//	}

//	private VerticalLayout getDynVL() {
//		Component comp = null;
//		VerticalLayout leftVL = new VerticalLayout();
//		leftVL.setWidth("100%");
//		leftVL.setSpacing(true);
//		leftVL.setMargin(false);
//		comp = SimConfigToComponent.convert(simConfig.getDynamic().get("Enable"), null);
//		((CheckBox) comp).addValueChangeListener(new ValueChangeListener<Boolean>() {
//			@Override
//			public void valueChange(ValueChangeEvent<Boolean> event) {
//				Boolean newVal=(Boolean)event.getValue();
//				dynVisVL.setVisible(newVal);
//			}
//		});
//		leftVL.addComponent(comp);
//		for (SimConfigEntry en : simConfig.getDynamic()) {
//			if (!en.getId().equals("Enable")) {
//				comp = SimConfigToComponent.convert(en, null);
//				dynVisVL.addComponent(comp);
//			}
//		}
//		leftVL.addComponent(dynVisVL);
//		dynVisVL.setVisible((Boolean)simConfig.getDynamic().get("Enable").getValue());
//
//		VerticalLayout rightVL = new VerticalLayout();
//		rightVL.setWidth("50px");
//		rightVL.setHeight("100%");
//		rightVL.setSpacing(false);
//		rightVL.setMargin(false);
//		Button iBtn = getInfoDynButton();
//		rightVL.setDefaultComponentAlignment(Alignment.TOP_RIGHT);
//		rightVL.addComponents(iBtn);
//
//		HorizontalLayout hl = new HorizontalLayout();
//		hl.setSizeFull();
//		hl.setSpacing(false);
//		hl.setMargin(false);
//		hl.addComponents(leftVL, infoDynPopup, rightVL);
//		hl.setExpandRatio(leftVL, 1f);
//
//		VerticalLayout mainVL = new VerticalLayout();
//		mainVL.setSizeFull();
//		mainVL.setMargin(true);
//		mainVL.setSpacing(false);
//		mainVL.addComponent(hl);
//		return mainVL;
//	}
//
//	private Button getInfoDynButton() {
//		Button btn = new Button();
//		btn.setDescription("How to set Dynamic simulation");
//		btn.setWidth("36px");
//		btn.setStyleName("infoBtn");
//		btn.addClickListener(new ClickListener() {
//			private static final long serialVersionUID = 1L;
//
//			public void buttonClick(ClickEvent event) {
//				infoDynPopup.setPopupVisible(true);
//			}
//		});
//		return btn;
//	}

//	private Button getInfoSSButton() {
//		Button btn = new Button();
//		btn.setDescription("How to set Steady State simulation");
//		btn.setWidth("36px");
//		btn.setStyleName("infoBtn");
//		btn.addClickListener(new ClickListener() {
//			private static final long serialVersionUID = 1L;
//
//			public void buttonClick(ClickEvent event) {
//				infoSSPopup.setPopupVisible(true);
//			}
//		});
//		return btn;
//	}
//
	private VerticalLayout getInfoDynLayout() {
		VerticalLayout vlt = new VerticalLayout();
		vlt.addComponent(new Label("Dynamic simulation allows you to:"));
		vlt.addComponent(new Label("1. Run a time course of your system"));
		vlt.addComponent(new Label(
				"2. Calculate the dynamic gains of your system with respect to its independent variables by checkboxing \"Gains\""));
		vlt.addComponent(new Label(
				"3. Calculate the dynamic sensitivities of your system with respect to its parameters by checkboxing \"Sensitivities\""));
		return vlt;
	}

	private Component getInfoSSLayout() {
		VerticalLayout vlt = new VerticalLayout();
		vlt.addComponent(new Label("Steady State simulation allows you to:"));
		vlt.addComponent(new Label("1. Calculate the Steady State(s) of your system"));
		vlt.addComponent(
				new Label("2. Analyse the stability of the Steady State(s) by checkboxing \"Stability analysis\""));
		vlt.addComponent(new Label(
				"3. Calculate the steady state gains of your system with respect to its independent variables by checkboxing \"Gains\""));
		vlt.addComponent(new Label(
				"4. Calculate the steady state sensitivities of your system with respect to its parameters by checkboxing \"Sensitivities\""));
		return vlt;
	}
//
//	private VerticalLayout getSSVL() {
//		Component comp = null;
//		VerticalLayout leftVL = new VerticalLayout();
//		leftVL.setWidth("100%");
//		leftVL.setSpacing(true);
//		leftVL.setMargin(false);
//		comp = SimConfigToComponent.convert(simConfig.getSteadyState().get("Enable"), null);
//		((CheckBox) comp).addValueChangeListener(new ValueChangeListener<Boolean>() {
//			@Override
//			public void valueChange(ValueChangeEvent<Boolean> event) {
//				Boolean newVal=(Boolean)event.getValue();
//				ssVisVL.setVisible(newVal);
//			}
//		});
//		leftVL.addComponent(comp);
//		for (SimConfigEntry en : simConfig.getSteadyState()) {
//			if (!en.getId().equals("Enable")) {
//				comp = SimConfigToComponent.convert(en, null);
//				ssVisVL.addComponent(comp);
//			}
//		}
//		leftVL.addComponent(ssVisVL);
//		ssVisVL.setVisible((Boolean)simConfig.getSteadyState().get("Enable").getValue());
//
//		VerticalLayout rightVL = new VerticalLayout();
//		rightVL.setWidth("50px");
//		rightVL.setHeight("100%");
//		rightVL.setSpacing(false);
//		rightVL.setMargin(false);
//		Button iBtn = getInfoSSButton();
//		rightVL.setDefaultComponentAlignment(Alignment.TOP_RIGHT);
//		rightVL.addComponents(iBtn, infoSSPopup);
//
//		HorizontalLayout hl = new HorizontalLayout();
//		hl.setSizeFull();
//		hl.setSpacing(false);
//		hl.setMargin(false);
//		hl.addComponents(leftVL, infoSSPopup, rightVL);
//		hl.setExpandRatio(leftVL, 1f);
//
//		VerticalLayout mainVL = new VerticalLayout();
//		mainVL.setSizeFull();
//		mainVL.setMargin(true);
//		mainVL.setSpacing(false);
//		mainVL.addComponent(hl);
//		return mainVL;
//	}

//	private Button getSimulateButton() {
//		Button btn = new Button("Run Simulation");
//		// btn.setWidth("60%");
//		btn.addClickListener(new ClickListener() {
//			@Override
//			public void buttonClick(ClickEvent event) {
//				try {
//					if (sessionData.getSimulationManager() == null) {
//						simConfig.checkSimConfigs();
//						plotViewsVL.updatePlotTabs();
//						selectedModel.checkIfReadyToSimulate();
//						// launch sim threads
//						sessionData.setSimulationManager(new SimulationManagerThread(sessionData));
//						sessionData.getSimulationManager().start();
//						mainPanel.showResults();
//					}
//					else
//						Notification.show("Please wait for the previous simulation to finish", Type.WARNING_MESSAGE);
//				} catch (Exception e) {
//					if (e instanceof MathLinkException)
//						Notification.show("webMathematica error", Type.WARNING_MESSAGE);
//					else
//						Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
//				}
//			}
//		});
//		return btn;
//	}

	private VerticalLayout getSelectTypeInfoLayout() {
		VerticalLayout vlt = new VerticalLayout();
		vlt.addComponent(new Label("Select simulation type:"));
		vlt.addComponent(new Label("-Deterministic: Linear behaviour aproximation. Faster calculation"));
		vlt.addComponent(new Label("-Stochastic: More reallistic simulation based on random reactions sequence"));
		return vlt;
	}

	private VerticalLayout getDeterministicInfoLayout() {
		VerticalLayout vlt = new VerticalLayout();
		vlt.addComponent(new Label("Deterministic simulation configuration:"));
		vlt.addComponent(new Label("1. Add and configure the needed types of simulations from the accordeon"));
		vlt.addComponent(new Label(
				"2. (Optional) Configure the plot settings. Plot views allow to output multiple different plots from each generated plot"));
		vlt.addComponent(
				new Label("3. Run Simulation (in results tab, left click on any image to view it on a new tab)"));
		return vlt;
	}

	private VerticalLayout getStochasticInfoLayout() {
		VerticalLayout vlt = new VerticalLayout();
		vlt.addComponent(new Label("Stochastic simulation configuration:"));
		vlt.addComponent(new Label("1. Configure the stochastic simulation"));
		vlt.addComponent(new Label(
				"2. (Optional) Configure the plot settings. Plot views allow to output multiple different plots from each generated plot"));
		vlt.addComponent(
				new Label("3. Run Simulation (in results tab, left click on any image to view it on a new tab)"));
		return vlt;
	}

	private Button getInfoButton() {
		Button btn = new Button();
		btn.setDescription("How to configure simulation");
		btn.setWidth("36px");
		btn.setStyleName("infoBtn");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				infoPopup.setPopupVisible(true);
			}
		});
		return btn;
	}
}
