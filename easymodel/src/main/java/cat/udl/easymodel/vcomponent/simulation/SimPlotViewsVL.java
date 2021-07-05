package cat.udl.easymodel.vcomponent.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TabSheet.CloseHandler;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.simconfig.SimConfigArray;
import cat.udl.easymodel.logic.types.SimType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.vcomponent.common.InfoWindowButton;

public class SimPlotViewsVL extends VerticalLayout {
	private SimConfig simConfig;
	private Model selectedModel;
	private SessionData sessionData;
	private TabSheet plotViewsTabs = new TabSheet();
	private boolean tabSheetListenerEnable = true;

	public SimPlotViewsVL() {
		super();

		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.selectedModel = this.sessionData.getSelectedModel();
		this.simConfig = this.selectedModel.getSimConfig();

		if (simConfig.getSimType() == SimType.DETERMINISTIC)
			simConfig.getDynamic_PlotViews().initPlotViews(selectedModel);

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
		hl.addComponents(leftVL, rightVL);
		hl.setExpandRatio(leftVL, 1f);

		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(false);
		this.addComponent(hl);
	}

	private Button getInfoPlotViewsButton() {
		return new InfoWindowButton("How to set Plot Views settings",
				"Plot Views: Define the variables to represent in each plot.", 800, 200);
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
				simConfig.getDynamic_PlotViews().setEachDepVarToOneView(selectedModel);
				updatePlotTabs();
			}
		});
		return btn;
	}

	@SuppressWarnings("unchecked")
	public void updatePlotTabs() {
		tabSheetListenerEnable = false;
		plotViewsTabs.removeAllComponents();
		for (int i = 0; i < simConfig.getDynamic_PlotViews().size(); i++) {
			SimConfigArray conf = simConfig.getDynamic_PlotViews().get(i);
			VerticalLayout tvl = new VerticalLayout();
			tvl.setData(new Integer(i));
			HorizontalLayout hl1 = new HorizontalLayout();
			tvl.addComponent(hl1);
			hl1.addComponent(SimConfigToComponent.convert(conf.get("DepVarsToShow"),
					selectedModel.getAllSpeciesTimeDependent().keySet()));
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
						simConfig.getDynamic_PlotViews().addNewPlotView();
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
					simConfig.getDynamic_PlotViews().removeAtIndex(arrIndex.intValue());
					updatePlotTabs();
					// doesn't work:
					tabSheetListenerEnable = false;
					tabsheet.setSelectedTab(selVL);
					tabSheetListenerEnable = true;
				}
			}
		});
	}
}
