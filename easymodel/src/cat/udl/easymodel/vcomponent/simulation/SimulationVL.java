package cat.udl.easymodel.vcomponent.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.CloseHandler;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.VaadinUtils;
import cat.udl.easymodel.vcomponent.AppPanel;

public class SimulationVL extends VerticalLayout {
	private SessionData sessionData;
	private SharedData sharedData = SharedData.getInstance();
	private Panel conPanel;
	private VerticalLayout simVL;
	private Accordion accordion;
	private boolean tabSheetListenerEnable = true;
	private PopupView infoPopup = new PopupView(null, getInfoLayout());

	private Model selectedModel;
	private SimConfig simConfig;
	private AppPanel mainPanel;

	private VerticalLayout dynVisVL;
	private VerticalLayout ssVisVL;

	private TabSheet plotViewsTabs = new TabSheet();

	private PopupView infoDynPopup = new PopupView(null, getInfoDynLayout());
	private PopupView infoSSPopup = new PopupView(null, getInfoSSLayout());
	private PopupView infoPlotViewsPopup = new PopupView(null, getInfoPlotViewsLayout());

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
		HorizontalLayout spacer = new HorizontalLayout();
		hl.addComponents(spacer, infoPopup, getInfoButton());
		hl.setExpandRatio(spacer, 1f);
		return hl;
	}

	private void updateDynVis() {
		Boolean enO = (Boolean) simConfig.getDynamic().get("Enable");
		if (enO == null)
			enO = false;
		dynVisVL.setVisible(enO);
	}

	private void updateSSVis() {
		Boolean enO = (Boolean) simConfig.getSteadyState().get("Enable");
		if (enO == null)
			enO = false;
		ssVisVL.setVisible(enO);
	}

	private Accordion getAccordion() {
		Accordion acc = new Accordion();
		acc.setId("simAccordion");
		acc.setWidth("100%");

		VerticalLayout dynVL = getDynVL();
		VerticalLayout ssVL = getSSVL();
		VerticalLayout plotVL = new SimPlotVL(simConfig.getPlot());
		VerticalLayout plotViewsVL = getPlotViewsVL();

		Tab t = acc.addTab(dynVL, "Dynamic Simulation");
		acc.addTab(ssVL, "Steady State Simulation");
		acc.addTab(plotVL, "Plot Settings");
		acc.addTab(plotViewsVL, "Plot Views");

		return acc;
	}

	private VerticalLayout getPlotViewsVL() {
		VerticalLayout leftVL = new VerticalLayout();
		leftVL.setWidth("100%");
		leftVL.setSpacing(true);
		leftVL.setMargin(false);
		initPlotTabs();
		leftVL.addComponents(plotViewsTabs);
		updatePlotTabs();

		VerticalLayout rightVL = new VerticalLayout();
		rightVL.setDefaultComponentAlignment(Alignment.TOP_RIGHT);
		rightVL.setWidth("50px");
		rightVL.setHeight("100%");
		rightVL.setSpacing(false);
		rightVL.setMargin(false);
		Button iBtn = getInfoPlotViewsButton();
		Button eachBtn = getSetEachDepVarToOneViewButton();
		rightVL.addComponents(iBtn, eachBtn);

		HorizontalLayout hl = new HorizontalLayout();
		hl.setSizeFull();
		hl.setSpacing(false);
		hl.setMargin(false);
		hl.addComponents(leftVL, infoPlotViewsPopup, rightVL);
		hl.setExpandRatio(leftVL, 1f);

		VerticalLayout mainVL = new VerticalLayout();
		mainVL.setSizeFull();
		mainVL.setMargin(true);
		mainVL.setSpacing(false);
		mainVL.addComponent(hl);
		return mainVL;
	}

	private Button getSetEachDepVarToOneViewButton() {
		Button btn = new Button();
		btn.setDescription("Generate individual plot for each dependent variable");
		btn.setWidth("36px");
		btn.setStyleName("eachBtn");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			public void buttonClick(ClickEvent event) {
				simConfig.clearPlotViews();
				for (String dv : selectedModel.getAllSpeciesTimeDependent().keySet()) {
					simConfig.addPlotView();
					((ArrayList<String>) simConfig.getPlotViews().get(simConfig.getPlotViews().size() - 1)
							.get("DepVarsToShow")).add(dv);
				}
				updatePlotTabs();
			}
		});
		return btn;
	}

	@SuppressWarnings("unchecked")
	private void updatePlotTabs() {
		tabSheetListenerEnable = false;
		plotViewsTabs.removeAllComponents();
		for (int i = 0; i < simConfig.getPlotViews().size(); i++) {
			Map<String, Object> map = simConfig.getPlotViews().get(i);
			VerticalLayout tvl = new VerticalLayout();
			tvl.setData(new Integer(i));
			HorizontalLayout hl1 = new HorizontalLayout();
			tvl.addComponent(hl1);
			VerticalLayout vl1 = new VerticalLayout();
			hl1.addComponent(vl1);
			vl1.addComponent(new Label("Dependent variables"));
			for (String depVar : selectedModel.getAllSpeciesTimeDependent().keySet()) {
				vl1.addComponent(getDepVarToShowCheckBox(depVar, (ArrayList<String>) map.get("DepVarsToShow")));
			}
			Tab tab = plotViewsTabs.addTab(tvl);
			tab.setClosable(true);
			tab.setCaption("View " + plotViewsTabs.getComponentCount());
		}
		VerticalLayout newViewVL = ((Map<String, VerticalLayout>) plotViewsTabs.getData()).get("newViewVL");
		Tab tab = plotViewsTabs.addTab(newViewVL);
		tab.setCaption("New View");
		tab.setClosable(false);

		tabSheetListenerEnable = true;
	}

	private void initPlotTabs() {
		VerticalLayout newViewVL = new VerticalLayout();
		VerticalLayout oneDepVarPerViewVL = new VerticalLayout();
		Map<String, VerticalLayout> map = new HashMap<>();
		map.put("newViewVL", newViewVL);
		map.put("oneDepVarPerViewVL", oneDepVarPerViewVL);
		plotViewsTabs.setData(map);
		plotViewsTabs.addSelectedTabChangeListener(new SelectedTabChangeListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				if (tabSheetListenerEnable) {
					TabSheet ts = event.getTabSheet();
					Component selVL = ts.getSelectedTab();
					if (selVL == ((Map<String, VerticalLayout>) ts.getData()).get("newViewVL")) {
						simConfig.addPlotView();
						updatePlotTabs();
					}
				}
			}
		});
		plotViewsTabs.setCloseHandler(new CloseHandler() {
			@Override
			public void onTabClose(TabSheet tabsheet, Component tabContent) {
				if (tabSheetListenerEnable) {// && tabsheet.getSelectedTab() != tabContent) {
					Component selVL = tabsheet.getSelectedTab();
					Integer arrIndex = (Integer) ((VerticalLayout) tabContent).getData();
					simConfig.removePlotView(arrIndex.intValue());
					updatePlotTabs();
					// doesn't work:
					tabSheetListenerEnable = false;
					tabsheet.setSelectedTab(selVL);
					tabSheetListenerEnable = true;
				}
			}
		});
	}

	private CheckBox getDepVarToShowCheckBox(String depVar, ArrayList<String> arrDepVar) {
		CheckBox cb = new CheckBox(depVar);
		cb.setData(depVar);
		if (arrDepVar.contains(depVar))
			cb.setValue(true);
		cb.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Boolean newVal = (Boolean) event.getProperty().getValue();
				if (newVal && !arrDepVar.contains(depVar))
					arrDepVar.add(depVar);
				else if (!newVal && arrDepVar.contains(depVar))
					arrDepVar.remove(depVar);
			}
		});
		return cb;
	}

	private VerticalLayout getDynVL() {
		Component comp = null;
		VerticalLayout leftVL = new VerticalLayout();
		leftVL.setWidth("100%");
		leftVL.setSpacing(true);
		leftVL.setMargin(false);
		comp = getBooleanParamHL("Enable", simConfig.getDynamic());
		leftVL.addComponent(comp);
		for (String key : simConfig.getDynamic().keySet()) {
			if (!key.equals("Enable")) {
				Object o = simConfig.getDynamic().get(key);
				if (o instanceof String) {
					comp = getDecimalParamHL(key, simConfig.getDynamic());
				}
				if (o instanceof Boolean) {
					comp = getBooleanParamHL(key, simConfig.getDynamic());
				}
				dynVisVL.addComponent(comp);
			}
		}
		leftVL.addComponent(dynVisVL);
		updateDynVis();

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

	private Button getInfoPlotViewsButton() {
		Button btn = new Button();
		btn.setDescription("How to set Plot Views settings");
		btn.setWidth("36px");
		btn.setStyleName("infoBtn");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				infoPlotViewsPopup.setPopupVisible(true);
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

	private Component getInfoPlotViewsLayout() {
		VerticalLayout vlt = new VerticalLayout();
		vlt.addComponent(
				new Label("Define or select the graphical representation options for the different result views"));
		return vlt;
	}

	private VerticalLayout getSSVL() {
		Component comp = null;
		VerticalLayout leftVL = new VerticalLayout();
		leftVL.setWidth("100%");
		leftVL.setSpacing(true);
		leftVL.setMargin(false);
		comp = getBooleanParamHL("Enable", simConfig.getSteadyState());
		leftVL.addComponent(comp);
		for (String key : simConfig.getSteadyState().keySet()) {
			if (!key.equals("Enable")) {
				Object o = simConfig.getSteadyState().get(key);
				if (o instanceof String) {
					comp = getDecimalParamHL(key, simConfig.getSteadyState());
				}
				if (o instanceof Boolean) {
					comp = getBooleanParamHL(key, simConfig.getSteadyState());
				}
				ssVisVL.addComponent(comp);
			}
		}
		leftVL.addComponent(ssVisVL);
		updateSSVis();

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

	private Component getBooleanParamHL(String configKey, Map<String, Object> cMap) {
		// Boolean o = (Boolean) cMap.get(configKey);
		HorizontalLayout line = new HorizontalLayout();
		line.setWidth("100%");
		String caption = "";
		if (configKey.equals("Enable") && cMap == simConfig.getDynamic()) {
			caption = "Add to simulation";
		} else if (configKey.equals("Enable") && cMap == simConfig.getSteadyState()) {
			caption = "Add to simulation";
		}
		if (configKey.equals("Gains")) {
			caption = "Gains";
		} else if (configKey.equals("Sensitivities")) {
			caption = "Sensitivities";
		} else if (configKey.equals("Stability")) {
			caption = "Stability analysis";
		}
		CheckBox parValueCB = new CheckBox(caption);
		parValueCB.setId(configKey);
		parValueCB.setImmediate(true);
		parValueCB.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Boolean newVal = (Boolean) event.getProperty().getValue();
				if (newVal == null)
					newVal = false;
				cMap.put(configKey, newVal);
				if (configKey.equals("Enable") && cMap == simConfig.getDynamic())
					updateDynVis();
				else if (configKey.equals("Enable") && cMap == simConfig.getSteadyState())
					updateSSVis();
			}
		});
		// Load previous value
		Boolean prevValue = (Boolean) cMap.get(configKey);
		if (prevValue == null)
			prevValue = false;
		parValueCB.setValue(prevValue);

		line.addComponents(parValueCB);

		return line;
	}

	private Component getDecimalParamHL(String configKey, Map<String, Object> cMap) {
		// String o = (String) cMap.get(configKey);
		HorizontalLayout line = new HorizontalLayout();
		line.setImmediate(true);
		line.setWidth("100%");
		String caption = "";
		TextField parNameTF = new TextField();
		parNameTF.setWidth("100%");
		if (configKey.equals("Ti")) {
			parNameTF.setValue("Initial time");
			caption = "0";
		} else if (configKey.equals("Tf")) {
			parNameTF.setValue("Final time");
			caption = "50";
		} else if (configKey.equals("TSteps")) {
			parNameTF.setValue("Steps");
			caption = "0.1";
		}
		parNameTF.setReadOnly(true);
		TextField parValueTF = new TextField();
		parValueTF.setInputPrompt(caption);
		parValueTF.setWidth("100%");
		parValueTF.setId(configKey);
		parValueTF.setImmediate(true);
		parValueTF.addBlurListener(new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(BlurEvent event) {
				String newText = ((TextField) event.getComponent()).getValue();
				String newVal = newText;
				try {
					BigDecimal testBigDec = new BigDecimal(newVal);
				} catch (Exception e) {
					newVal = null;
					((TextField) event.getComponent()).setValue("");
				} finally {
					cMap.put(configKey, newVal);
				}
			}
		});
		// Load previous value
		String prevValue = (String) cMap.get(configKey);
		if (prevValue != null)
			parValueTF.setValue(prevValue);

		line.addComponents(parNameTF, parValueTF);
		line.setExpandRatio(parNameTF, 1.0f);
		line.setExpandRatio(parValueTF, 2.0f);

		return line;
	}

	private Button getSimulateButton() {
		Button btn = new Button("Run Simulation");
		// btn.setWidth("60%");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					simConfig.checkSimConfigs();
					updatePlotTabs();
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
