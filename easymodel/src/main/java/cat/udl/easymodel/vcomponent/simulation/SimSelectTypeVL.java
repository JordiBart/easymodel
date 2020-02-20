package cat.udl.easymodel.vcomponent.simulation;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.SimType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.ToolboxVaadin;

public class SimSelectTypeVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	private RadioButtonGroup<SimType> simTypeGroup;
	private SessionData sessionData;

	private SimulationVL simVL;
	private Model selectedModel;

	public SimSelectTypeVL(SimulationVL simVL) {
		super();
		this.simVL = simVL;
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.selectedModel = sessionData.getSelectedModel();

		this.setWidth("170px");
		this.setHeight("100%");
		this.setSpacing(true);
		this.setMargin(true);
		this.setStyleName("selectModel");
		this.setDefaultComponentAlignment(Alignment.TOP_LEFT);

		update();
	}

	private void update() {
		HorizontalLayout headerHL = getHeaderHL();
		simTypeGroup = getSimTypeOptionGroup();
		VerticalLayout spacer = new VerticalLayout();

		removeAllComponents();
		addComponents(headerHL, simTypeGroup, spacer);
		setExpandRatio(spacer, 1f);
	}

	private RadioButtonGroup<SimType> getSimTypeOptionGroup() {
		RadioButtonGroup<SimType> group = new RadioButtonGroup<>("Simulation Type");
		group.setItems(SimType.DETERMINISTIC, SimType.STOCHASTIC);
		group.setItemCaptionGenerator(SimType::getString);
		group.addValueChangeListener(new ValueChangeListener<SimType>() {
			@Override
			public void valueChange(ValueChangeEvent<SimType> event) {
				selectedModel.getSimConfig().setSimType(event.getValue());
				simVL.updateConPanel();
			}
		});
		group.setSelectedItem(selectedModel.getSimConfig().getSimType());
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

	// INFO
	private Button getInfoButton() {
		Button btn = new Button();
		btn.setDescription("About simulation types");
		btn.setWidth("36px");
		btn.setStyleName("infoBtn");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				sessionData.showInfoWindow("Simulation types\r\n" + 
						"-Deterministic:\r\n" + 
						"    Simulation uses algorithms that assume concentrations are continuous.\r\n" + 
						"    Repeated simulations with the same initial conditions always have the same response curve and end result.\r\n" + 
						"    Faster to execute at the expense of inaccuracies when the number of molecules is low.\r\n" + 
						"\r\n" + 
						"-Stochastic:\r\n" + 
						"    Simulation uses algorithms that consider only integer changes in the number of molecules.\r\n" + 
						"    Repeated simulations with the same initial conditions will leads to response curves and end results that are slightly different.\r\n" + 
						"    Slower to execute but more accurate when the number of molecules is low.", 800,400);
			}
		});
		return btn;
	}
}
