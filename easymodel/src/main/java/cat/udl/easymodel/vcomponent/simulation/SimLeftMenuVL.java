package cat.udl.easymodel.vcomponent.simulation;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.controller.SimulationCtrl;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.SimType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.app.AppPanel;
import cat.udl.easymodel.vcomponent.common.InfoWindowButton;

public class SimLeftMenuVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	private SessionData sessionData;
	private PlotSettingsWindow plotW;

	private SimulationVL simVL;
	private AppPanel mainPanel;
	private Model selectedModel;
	private Button runButton = null;

	public SimLeftMenuVL(SimulationVL simVL) {
		super();
		this.simVL = simVL;
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.mainPanel = this.sessionData.getAppPanel();
		this.selectedModel = sessionData.getSelectedModel();

		plotW = new PlotSettingsWindow(selectedModel.getSimConfig().getPlotSettings());

		this.setWidth("200px");
		this.setHeight("100%");
		this.setSpacing(false);
		this.setMargin(true);
		this.addStyleName("panelBorder");
		this.addStyleName("v-scrollable");
		this.setDefaultComponentAlignment(Alignment.TOP_LEFT);

		update();
	}

	private void update() {
		HorizontalLayout headerHL = getHeaderHL();
		VerticalLayout spacer = new VerticalLayout();

		removeAllComponents();
		runButton = getRunSimButton();
		addComponents(headerHL, getSimTypeOptionGroup(), ToolboxVaadin.getHR(), getPlotSettingsButton(),
				ToolboxVaadin.getHR(), runButton, spacer);
		setExpandRatio(spacer, 1f);
	}

	private Button getRunSimButton() {
		Button btn = new Button("Run Simulation");
		btn.setIcon(VaadinIcons.PLAY);
		btn.setWidth("100%");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ToolboxVaadin.removeAllWindows();
				try {
					selectedModel.checkAndAdaptToSimulate();

					sessionData.respawnSimulationManager();
					sessionData.getSimulationManager().start();
					sessionData.getOutVL().reset();
					sessionData.getOutVL().out("Results for " + selectedModel.getName(), "textH1");
					sessionData.getSimStatusHL().running();
					mainPanel.showResults();
				} catch (Exception e) {
					if (e instanceof MathLinkException)
						Notification.show("webMathematica error", Type.WARNING_MESSAGE);
					else
						Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
				}
//				plotViewsVL.updatePlotTabs();
			}
		});
		return btn;
	}

	private Button getPlotSettingsButton() {
		Button btn = new Button("Plot Settings");
		btn.setIcon(VaadinIcons.PALETE);
		btn.setWidth("100%");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (!UI.getCurrent().getWindows().contains(plotW))
					UI.getCurrent().addWindow(plotW);
			}
		});
		return btn;
	}

	private RadioButtonGroup<SimType> getSimTypeOptionGroup() {
		RadioButtonGroup<SimType> group = new RadioButtonGroup<>("Simulation Type");
		group.setWidth("100%");
		group.setItems(SimType.DETERMINISTIC, SimType.STOCHASTIC);
		group.setItemCaptionGenerator(SimType::getString);
		group.setSelectedItem(selectedModel.getSimConfig().getSimType());
		group.addValueChangeListener(new ValueChangeListener<SimType>() {
			@Override
			public void valueChange(ValueChangeEvent<SimType> event) {
				if (event.isUserOriginated()) {
					selectedModel.getSimConfig().setSimType(event.getValue());
					simVL.updateConPanel();
				}
			}
		});
		return group;
	}

	private HorizontalLayout getHeaderHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setHeight("37px");
		HorizontalLayout spacer = new HorizontalLayout();
		hl.addComponents(spacer, getInfoButton());
		hl.setExpandRatio(spacer, 1f);
		return hl;
	}

	private Button getInfoButton() {
		return new InfoWindowButton("About simulation types", "Simulation Configuration\r\n"
				+ "1-Select simulation type:\r\n"
				+ "  -Deterministic: Simulation uses algorithms that assume concentrations are continuous. "
				+ "Repeated simulations with the same initial conditions always have the same response curve and end result. "
				+ "Faster to execute at the expense of inaccuracies when the number of molecules is low.\r\n"
				+ "  -Stochastic: Simulation uses algorithms that consider only integer changes in the number of molecules. "
				+ "Repeated simulations with the same initial conditions will leads to response curves and end results that are slightly different. "
				+ "Slower to execute but more accurate when the number of molecules is low.\n"
				+ "2-Configure selected simulation type in the main panel.\n"
				+ "3-Configure Plot Settings (optional): Select the graphical representation options for your simulation.\n"
				+ "4-Run Simulation.", 800, 400);
	}
	
	public void setEnableRunButton(boolean val) {
		runButton.setEnabled(val);
	}
}
