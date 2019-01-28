package cat.udl.easymodel.vcomponent.simulation;

import java.io.File;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
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
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.simconfig.SimConfigEntry;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.vcomponent.app.AppPanel;

public class SimulationVL extends VerticalLayout {
	private SessionData sessionData;
	private SharedData sharedData = SharedData.getInstance();
	private Panel conPanel;
	private VerticalLayout simVL;
	private Accordion accordion;
	private PopupView infoPopup = new PopupView(null, getInfoLayout());

	private Model selectedModel;
	private SimConfig simConfig;
	private AppPanel mainPanel;

	private SimPlotViewsVL plotViewsVL;
	
	private VerticalLayout dynVisVL;
	private VerticalLayout ssVisVL;

	private PopupView infoDynPopup = new PopupView(null, getInfoDynLayout());
	private PopupView infoSSPopup = new PopupView(null, getInfoSSLayout());

	public SimulationVL(Model selectedModel, AppPanel mainPanel) {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.selectedModel = selectedModel;
		this.simConfig = selectedModel.getSimConfig();
		this.mainPanel = mainPanel;

		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(true);

		simConfig.initPlotViews(selectedModel.getAllSpeciesTimeDependent());

		dynVisVL = new VerticalLayout();
		dynVisVL.setSpacing(false);
		dynVisVL.setMargin(false);
		dynVisVL.setSizeFull();
		ssVisVL = new VerticalLayout();
		ssVisVL.setSpacing(false);
		ssVisVL.setMargin(false);
		ssVisVL.setSizeFull();

		simVL = new VerticalLayout();
		simVL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		simVL.setSpacing(true);
		simVL.setMargin(true);
		simVL.setWidth("100%");
		updateConPanel();

		conPanel = new Panel();
		conPanel.setStyleName("reactionsPanel");
		conPanel.setHeight("100%");
		conPanel.setContent(simVL);

		this.addComponent(conPanel);
		this.setComponentAlignment(conPanel, Alignment.MIDDLE_CENTER);
	}

	private void updateConPanel() {
		simVL.removeAllComponents();

		accordion = getAccordion();
		HorizontalLayout buttonsHL = getButtonsHL();
		simVL.addComponents(getHeaderHL(), accordion, buttonsHL);
	}

	private HorizontalLayout getButtonsHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
		hl.setSpacing(false);
		hl.addComponents(getSimulateButton());
		return hl;
	}

	private HorizontalLayout getHeaderHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setHeight("37px");
		FileResource resource = new FileResource(
				new File(VaadinService.getCurrent()
						.getBaseDirectory().getAbsolutePath() + "/VAADIN/themes/easymodel/img/easymodel-logo-36.png"));
		Image image = new Image(null, resource);
		HorizontalLayout head = new HorizontalLayout();
		head.setWidth("100%");
		head.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		head.addComponent(image);
		hl.addComponents(head, infoPopup, getInfoButton());
		hl.setExpandRatio(head, 1f);
		return hl;
	}

	private Accordion getAccordion() {
		Accordion acc = new Accordion();
		acc.setId("simAccordion");
		acc.setWidth("100%");

		VerticalLayout dynVL = getDynVL();
		VerticalLayout ssVL = getSSVL();
		VerticalLayout plotVL = new SimPlotVL(simConfig.getPlotSettings());
		plotViewsVL = new SimPlotViewsVL(simConfig, selectedModel);

		Tab t = acc.addTab(dynVL, "Dynamic Simulation");
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
		((CheckBox) comp).addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Boolean newVal=(Boolean)event.getProperty().getValue();
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
		dynVisVL.setVisible((Boolean)simConfig.getDynamic().get("Enable").getValue());

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
		hl.addComponents(leftVL, infoDynPopup, rightVL);
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
				infoDynPopup.setPopupVisible(true);
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
				infoSSPopup.setPopupVisible(true);
			}
		});
		return btn;
	}

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

	private VerticalLayout getSSVL() {
		Component comp = null;
		VerticalLayout leftVL = new VerticalLayout();
		leftVL.setWidth("100%");
		leftVL.setSpacing(true);
		leftVL.setMargin(false);
		comp = SimConfigToComponent.convert(simConfig.getSteadyState().get("Enable"), null);
		((CheckBox) comp).addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Boolean newVal=(Boolean)event.getProperty().getValue();
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
		ssVisVL.setVisible((Boolean)simConfig.getSteadyState().get("Enable").getValue());

		VerticalLayout rightVL = new VerticalLayout();
		rightVL.setWidth("50px");
		rightVL.setHeight("100%");
		rightVL.setSpacing(false);
		rightVL.setMargin(false);
		Button iBtn = getInfoSSButton();
		rightVL.setDefaultComponentAlignment(Alignment.TOP_RIGHT);
		rightVL.addComponents(iBtn, infoSSPopup);

		HorizontalLayout hl = new HorizontalLayout();
		hl.setSizeFull();
		hl.setSpacing(false);
		hl.setMargin(false);
		hl.addComponents(leftVL, infoSSPopup, rightVL);
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
					simConfig.checkSimConfigs();
					plotViewsVL.updatePlotTabs();
					mainPanel.showResults();
				} catch (Exception e) {
					Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
				}
			}
		});
		return btn;
	}
	
	private VerticalLayout getInfoLayout() {
		VerticalLayout vlt = new VerticalLayout();
		vlt.addComponent(new Label("Simulation configuration:"));
		vlt.addComponent(new Label("1. Add and configure the needed types of simulations from the accordeon"));
		vlt.addComponent(new Label("2. (Optional) Configure the plot settings. Plot views allow to output multiple different plots from each generated plot"));
		vlt.addComponent(new Label("3. Run Simulation. On results tab, left click on images to view an enlarged version of them in a new tab"));
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
