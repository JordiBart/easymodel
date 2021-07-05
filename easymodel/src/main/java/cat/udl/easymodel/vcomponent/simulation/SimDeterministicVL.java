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
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.simconfig.SimConfigEntry;
import cat.udl.easymodel.logic.simconfig.SimParamScanConfig;
import cat.udl.easymodel.logic.types.ParamScanType;
import cat.udl.easymodel.logic.types.WStatusType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.app.AppPanel;
import cat.udl.easymodel.vcomponent.common.AreYouSureWindow;
import cat.udl.easymodel.vcomponent.common.InfoWindowButton;

public class SimDeterministicVL extends VerticalLayout {
	private SessionData sessionData;
	private SharedData sharedData = SharedData.getInstance();

	private Model selectedModel;
	private SimConfig simConfig;
	private AppPanel mainPanel;

	private SimPlotViewsVL plotViewsVL;
	private ParamScanWindow paramScanWindow = new ParamScanWindow();

	private Accordion accDyn=new Accordion(); 
	private Accordion accSS=new Accordion();

	public SimDeterministicVL() {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.selectedModel = sessionData.getSelectedModel();
		this.simConfig = selectedModel.getSimConfig();
		this.mainPanel = this.sessionData.getAppPanel();
		simConfig.getDynamic_PlotViews().initPlotViews(selectedModel);

		this.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setSpacing(true);
		this.setMargin(false);
		this.setSizeFull();
//		this.setStyleName("simConfig");

		HorizontalLayout simsHL = new HorizontalLayout();
		simsHL.setSpacing(true);
		simsHL.setMargin(false);
		simsHL.setSizeFull();
		simsHL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		simsHL.addComponents(getDynVL(), getSeadyStateVL());
		this.addComponent(simsHL);
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

	private CheckBox getDynamicCheckBox() {
		CheckBox enableCheckBox = (CheckBox) SimConfigToComponent.convert(simConfig.getDynamic().get("Enable"),
				null);
		enableCheckBox.setStyleName("simTittleCheckBox");
		enableCheckBox.setCaption("Dynamic Simulation");
		enableCheckBox.addValueChangeListener(new ValueChangeListener<Boolean>() {
			@Override
			public void valueChange(ValueChangeEvent<Boolean> event) {
				Boolean newVal = (Boolean) event.getValue();
				accDyn.setEnabled(newVal);
			}
		});
		enableCheckBox.setValue((Boolean) simConfig.getDynamic().get("Enable").getValue());
		return enableCheckBox;
	}
	
	private VerticalLayout getDynVL() {
		VerticalLayout leftVL = new VerticalLayout();
		leftVL.setWidth("100%");
		leftVL.setSpacing(true);
		leftVL.setMargin(false);
//		CheckBox enableCheckBox = (CheckBox) SimConfigToComponent.convert(simConfig.getDynamic().get("Enable"), null);
//		enableCheckBox.setValue((Boolean) simConfig.getDynamic().get("Enable").getValue());
//		enableCheckBox.addValueChangeListener(new ValueChangeListener<Boolean>() {
//			@Override
//			public void valueChange(ValueChangeEvent<Boolean> event) {
//				Boolean newVal = (Boolean) event.getValue();
//				for (Component comp : dynComponentList)
//					comp.setEnabled(newVal);
//			}
//		});
//		leftVL.addComponent(enableCheckBox);

		for (SimConfigEntry en : simConfig.getDynamic()) {
			if (en.getId().equals("Enable"))
				continue;
			Component comp = SimConfigToComponent.convert(en, null);
			leftVL.addComponent(comp);
		}
		/////////////
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
		hl.setMargin(true);
		hl.addComponents(leftVL, rightVL);
		hl.setExpandRatio(leftVL, 1f);

		CheckBox dynCB = getDynamicCheckBox();
		accDyn.addTab(hl, "Dynamic Settings");
		accDyn.addTab(new SimPlotViewsVL(), "Dynamic Plot Views");
		accDyn.addTab(getParameterScanVL(simConfig.getDynamic_ParameterScan()), "Parameter Scan");
		accDyn.setSizeFull();
		accDyn.setEnabled(dynCB.getValue());

		VerticalLayout mainVL = new VerticalLayout();
		mainVL.setWidth("500px");
		mainVL.addComponents(dynCB,accDyn);
		return mainVL;
	}

	private VerticalLayout getParameterScanVL(SimParamScanConfig simParamScanConfig) {
		VerticalLayout leftVL = new VerticalLayout();
		leftVL.setWidth("100%");
		leftVL.setSpacing(false);
		leftVL.setMargin(false);
		leftVL.addComponents(getParamScanButtons(simParamScanConfig));

		VerticalLayout rightVL = new VerticalLayout();
		rightVL.setDefaultComponentAlignment(Alignment.TOP_RIGHT);
		rightVL.setWidth("50px");
		rightVL.setHeight("100%");
		rightVL.setSpacing(false);
		rightVL.setMargin(false);
		Button iBtn = getInfoParamScanButton();
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
	
	private Component getParamScanButtons(SimParamScanConfig simParamScanConfig) {
		VerticalLayout vl = new VerticalLayout();
		vl.setWidth("250px");
		vl.setMargin(false);
		vl.setSpacing(true);
		Button paramBtn = new Button("Select Parameters");
		paramBtn.setWidth("100%");
		paramBtn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ToolboxVaadin.removeAllWindows();
				paramScanWindow.set(simParamScanConfig, ParamScanType.PARAMETER);
				try {
					paramScanWindow.checkToShow();	
					UI.getCurrent().addWindow(paramScanWindow);
				} catch (Exception e) {
					Notification.show("Model has no parameters", Type.WARNING_MESSAGE);
				}
			}
		});
		Button indVarsBtn = new Button("Select Independent Variables");
		indVarsBtn.setWidth("100%");
		indVarsBtn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ToolboxVaadin.removeAllWindows();
				paramScanWindow.set(simParamScanConfig, ParamScanType.IND_VAR);
				try {
					paramScanWindow.checkToShow();	
					UI.getCurrent().addWindow(paramScanWindow);
				} catch (Exception e) {
					Notification.show("Model has no independent variables", Type.WARNING_MESSAGE);
				}
			}
		});
		Button resetBtn = new Button("Reset Selection");
		resetBtn.setWidth("100%");
		resetBtn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				AreYouSureWindow win = new AreYouSureWindow("Confirmation",
						"Are you sure reset Parameter Scan configuration?");
				win.addCloseListener(new CloseListener() {
					@Override
					public void windowClose(CloseEvent e) {
						if ((WStatusType) e.getWindow().getData() == WStatusType.OK) {
							simParamScanConfig.reset();
						}
					}
				});
				ToolboxVaadin.removeAllWindows();
				UI.getCurrent().addWindow(win);
			}
		});
		vl.addComponents(paramBtn,indVarsBtn,resetBtn);
		return vl;
	}

	private Button getInfoParamScanButton() {
		return new InfoWindowButton("How to set Parameter Scanning",
				"1. Press \"Select Parameters\" button to select the parameters to be scanned.\n"
				+ "2. Press \"Select Independent Variables\" button to select the Independent Variables (Constant Species) to be scanned.\n"
				+ "3. (Optional) Press \"Reset Selection\" button to delete all Parameter Scan configuration.", 700, 200);
	}

	private Button getInfoDynButton() {
		return new InfoWindowButton("How to set Dynamic simulation", "Dynamic simulation allows you to:\n"
				+ "1. Run a time course of your system\n"
				+ "2. Calculate the dynamic gains of your system with respect to its independent variables by checkboxing \"Gains\"\n"
				+ "3. Calculate the dynamic sensitivities of your system with respect to its parameters by checkboxing \"Sensitivities\"",
				800, 400);
	}

	private Button getInfoSSButton() {
		return new InfoWindowButton("How to set Steady State simulation", "Steady State simulation allows you to:\n"
				+ "1. Calculate the Steady State(s) of your system\n"
				+ "2. Analyse the stability of the Steady State(s) by checkboxing \"Stability analysis\"\n"
				+ "3. Calculate the steady state gains of your system with respect to its independent variables by checkboxing \"Gains\""
				+ "4. Calculate the steady state sensitivities of your system with respect to its parameters by checkboxing \"Sensitivities\"",
				800, 400);
	}

	private CheckBox getSteadyStateCheckBox() {
		CheckBox enableCheckBox = (CheckBox) SimConfigToComponent.convert(simConfig.getSteadyState().get("Enable"),
				null);
		enableCheckBox.setStyleName("simTittleCheckBox");
		enableCheckBox.setCaption("Steady State Simulation");
		enableCheckBox.addValueChangeListener(new ValueChangeListener<Boolean>() {
			@Override
			public void valueChange(ValueChangeEvent<Boolean> event) {
				Boolean newVal = (Boolean) event.getValue();
				accSS.setEnabled(newVal);
			}
		});
		enableCheckBox.setValue((Boolean) simConfig.getSteadyState().get("Enable").getValue());
		return enableCheckBox;
	}
	
	private VerticalLayout getSeadyStateVL() {
		VerticalLayout leftVL = new VerticalLayout();
		leftVL.setWidth("100%");
		leftVL.setSpacing(true);
		leftVL.setMargin(false);
//		CheckBox enableCheckBox = (CheckBox) SimConfigToComponent.convert(simConfig.getSteadyState().get("Enable"),
//				null);
//		enableCheckBox.setValue((Boolean) simConfig.getSteadyState().get("Enable").getValue());
//		enableCheckBox.addValueChangeListener(new ValueChangeListener<Boolean>() {
//			@Override
//			public void valueChange(ValueChangeEvent<Boolean> event) {
//				Boolean newVal = (Boolean) event.getValue();
//				for (Component comp : ssComponentList)
//					comp.setEnabled(newVal);
//			}
//		});
//		leftVL.addComponent(enableCheckBox);

		for (SimConfigEntry en : simConfig.getSteadyState()) {
			if (en.getId().equals("Enable"))
				continue;
			Component comp = SimConfigToComponent.convert(en, null);
			leftVL.addComponent(comp);
		}
		////////////
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
		hl.setMargin(true);
		hl.addComponents(leftVL, rightVL);
		hl.setExpandRatio(leftVL, 1f);

		CheckBox ssCB = getSteadyStateCheckBox();
		accSS.addTab(hl, "Steady State Settings");
		accSS.addTab(getParameterScanVL(simConfig.getSteadyState_ParameterScan()), "Parameter Scan");
		accSS.setSizeFull();
		accSS.setEnabled(ssCB.getValue());

		VerticalLayout mainVL = new VerticalLayout();
		mainVL.setWidth("500px");
		mainVL.addComponents(ssCB,accSS);
		return mainVL;
	}

	private Button getInfoButton() {
		return new InfoWindowButton("How to configure simulation", "Procedure for deterministic simulation:\r\n"
				+ "1. Configure the dynamic simulation.\r\n" + "    All parameters should be positive.\r\n"
				+ "    Final time should be larger than initial time.\r\n"
				+ "    If sensitivity analysis is to be made, please tick the appropriate checkboxes.\r\n"
				+ "2. Configure the steady state simulation. Enable the checkbox, if a steady state simulation is required.\r\n"
				+ "3. Configure plot settings.\r\n" + "4. Configure variable representation in Plot Views.\r\n"
				+ "5. Run Simulation.", 800, 400);
	}
}
