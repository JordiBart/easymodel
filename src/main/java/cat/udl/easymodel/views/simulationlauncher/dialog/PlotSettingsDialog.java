package cat.udl.easymodel.views.simulationlauncher.dialog;


import cat.udl.easymodel.logic.formula.FormulaArrayValue;
import cat.udl.easymodel.logic.formula.FormulaValue;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.model.Species;
import cat.udl.easymodel.logic.simconfig.ParamScanEntry;
import cat.udl.easymodel.logic.simconfig.SimConfigArray;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.ParamScanType;
import cat.udl.easymodel.logic.types.SpeciesType;
import cat.udl.easymodel.logic.types.SpeciesVarTypeType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.MathematicaUtils;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import cat.udl.easymodel.views.simulationlauncher.SimConfigToComponent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.VaadinSession;

import java.util.SortedMap;

public class PlotSettingsDialog extends Dialog {
	private SimConfigArray plotConfig;

	private Model selModel = null;
	private SessionData sessionData = null;
	private SharedData sharedData = SharedData.getInstance();
	private VerticalLayout mainVL = null;

	public PlotSettingsDialog(SimConfigArray plotConfig) {
		super();

		this.plotConfig = plotConfig;
		this.sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
		this.selModel = this.sessionData.getSelectedModel();

		this.setWidth("480px");
		this.setHeight("380");
		this.setModal(true);
		this.setResizable(true);
		this.setDraggable(true);

		VerticalLayout windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setPadding(false);
		windowVL.setSizeFull();
		
		mainVL = new VerticalLayout();
		mainVL.setSpacing(true);
		mainVL.setPadding(false);
		mainVL.setSizeFull();
		mainVL.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
		windowVL.add(ToolboxVaadin.getDialogHeader(this,"Plot Settings",new InfoDialogButton("How to set Plot Settings",
				"Select the graphical representation options for your simulation.",
				"700px", "300px")),mainVL);
		windowVL.expand(mainVL);
		this.add(windowVL);
		updateWindowContent();
	}

	private void updateWindowContent() {
		mainVL.removeAll();

		VerticalLayout vl1 = new VerticalLayout();
		vl1.setSpacing(false);
		vl1.setPadding(false);
		vl1.setSizeFull();

		VerticalLayout fontCBVL = new VerticalLayout();
		fontCBVL.setSpacing(true);
		fontCBVL.setWidth("80px");
		fontCBVL.add(SimConfigToComponent.convert(plotConfig.get("FontWeight"), null), SimConfigToComponent.convert(plotConfig.get("FontSlant"), null));
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setPadding(true);
		hl1.setSpacing(true);
		hl1.add(SimConfigToComponent.convert(plotConfig.get("FontSize"), null),fontCBVL);
//		gl.addComponents(, fontCBVL, SimConfigToComponent.convert(plotConfig.get("LineThickness"), null),SimConfigToComponent.convert(plotConfig.get("ImageSize"), null));
		HorizontalLayout hl2 = new HorizontalLayout();
		hl2.setPadding(true);
		hl2.setSpacing(true);
		vl1.add(SimConfigToComponent.convert(plotConfig.get("LineThickness"), null),SimConfigToComponent.convert(plotConfig.get("ImageSize"), null),
				SimConfigToComponent.convert(plotConfig.get("FontSize"), null),SimConfigToComponent.convert(plotConfig.get("FontWeight"), null),
				SimConfigToComponent.convert(plotConfig.get("FontSlant"), null));

		mainVL.add(vl1, getOkCancelButtonsHL());
		mainVL.expand(vl1);
	}

	private HorizontalLayout getOkCancelButtonsHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		HorizontalLayout spacer = new HorizontalLayout();
		spacer.setWidth("100%");
		hl.add(getDefaultButton(),spacer, getCloseButton());
		hl.expand(spacer);
		return hl;
	}

	private Button getDefaultButton() {
		Button button = new Button("Default");
		button.setWidth("200px");
		button.addClickListener(event -> {
			plotConfig.get("FontWeight").resetValue();
		    plotConfig.get("FontSlant").resetValue();
			plotConfig.get("FontSize").resetValue();
			plotConfig.get("ImageSize").resetValue();
			plotConfig.get("LineThickness").resetValue();
			updateWindowContent();
		});
		return button;
	}

	private Button getCloseButton() {
		Button button = new Button("Close");
		button.setWidth("150px");
		button.addClickListener(event -> {
				close();
		});
		return button;
	}
}
