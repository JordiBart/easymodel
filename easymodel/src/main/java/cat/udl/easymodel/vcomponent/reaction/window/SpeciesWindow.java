package cat.udl.easymodel.vcomponent.reaction.window;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.renderers.ComponentRenderer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cat.udl.easymodel.logic.formula.FormulaValue;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.model.Species;
import cat.udl.easymodel.logic.simconfig.ParamScanEntry;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.InputType;
import cat.udl.easymodel.logic.types.ParamScanType;
import cat.udl.easymodel.logic.types.SpeciesVarTypeType;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.vcomponent.common.InfoPopupButton;
import cat.udl.easymodel.vcomponent.common.InfoWindowButton;

public class SpeciesWindow extends Window {
	private Model model = null;

	private Grid<Species> grid;
	private ArrayList<Species> gridItemList = new ArrayList<>();

	private VerticalLayout windowVL = null;
	private boolean isSaveAndClose = false;

	public SpeciesWindow(Model model) {
		super();

		// Set vars
		this.model = model;

		this.setCaption("Species Settings");
		this.setClosable(true);
		this.setWidth("600px");
		this.setHeight("500px");
		this.setModal(true);
		this.setResizable(true);
		this.center();

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		this.addShortcutListener(new ShortcutListener("Shortcut enter", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {
				isSaveAndClose = true;
				focus();
			}
		});
		this.addFocusListener(new FocusListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void focus(FocusEvent event) {
				if (isSaveAndClose) {
					saveAndClose();
					isSaveAndClose=false;
				}
			}
		});
		displayWindowContent();
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		grid = new Grid<Species>();
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setSizeFull();
		fillGridItemList();
		grid.setItems(gridItemList);
		grid.addColumn(Species::getName).setCaption("Name").setWidth(150);
		grid.addColumn(entry -> {
			TextField tf = new TextField();
			tf.setWidth("100%");
			tf.setDescription("Initial Concentration");
			tf.setValue(entry.getConcentration() != null ? entry.getConcentration() : "");
			tf.addBlurListener(event -> {
				try {
					String newValStr = Utils.evalMathExpr(tf.getValue());
					entry.setConcentration(newValStr);
					tf.setValue(newValStr);
				} catch (Exception e) {
					entry.setConcentration(null);
					tf.setValue("");
				}
			});
			return tf;
		}, new ComponentRenderer()).setCaption("Initial Concentration").setWidth(200);
		grid.addColumn(entry -> {
			NativeSelect<SpeciesVarTypeType> ns = new NativeSelect<>();
			ns.setWidth("100%");
			ns.setEmptySelectionAllowed(false);
			ns.setItems(SpeciesVarTypeType.values());
			ns.setItemCaptionGenerator(SpeciesVarTypeType::getString);
			if (!model.getAllSpeciesExceptModifiers().containsKey(entry.getName()))
				entry.setVarType(SpeciesVarTypeType.INDEPENDENT);
			ns.setValue(entry.getVarType());
			if (!model.getAllSpeciesExceptModifiers().containsKey(entry.getName()))
				ns.setEnabled(false);
			ns.addValueChangeListener(event -> {
				entry.setVarType(event.getValue());
			});
			return ns;
		}, new ComponentRenderer()).setCaption("Variable Type");

		windowVL.addComponents(getHeaderHL(), grid, getOkCancelButtonsHL());
		windowVL.setExpandRatio(grid, 1.0f);
	}

	private void fillGridItemList() {
		gridItemList.clear();
		for (Species sp : model.getAllSpecies().values()) {
			gridItemList.add(new Species(sp));
		}
	}

	private HorizontalLayout getHeaderHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setHeight("37px");
		HorizontalLayout spacer = new HorizontalLayout();
		hl.addComponents(spacer, getInfoPopupButton());
		hl.setExpandRatio(spacer, 1f);
		return hl;
	}

	private InfoPopupButton getInfoPopupButton() {
		return new InfoPopupButton("Setting Species",
				"1. Set the initial concentration for every species of the model.\n" + "2. Set the variable type:\n"
						+ "    -Time Dependent: variable value evolves trough time.\n"
						+ "    -Independent: variable value remains constant trough time.",
				600, null);
	}

	private HorizontalLayout getOkCancelButtonsHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		HorizontalLayout spacer = new HorizontalLayout();
		spacer.setWidth("100%");
		hl.addComponents(spacer, getOkButton(), getCancelButton());
		hl.setExpandRatio(spacer, 1.0f);
		return hl;
	}

	private Button getCancelButton() {
		Button button = new Button("Cancel");
		button.setId("cancelButton");
		button.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				close();
			}
		});
		return button;
	}

	private void saveAndClose() {
		try {
			checkValuesToSave();
		} catch (Exception e) {
			Notification.show(e.getMessage(),Type.WARNING_MESSAGE);
			return;
		}
		// update values
		for (Species sp : gridItemList) {
			model.getAllSpecies().get(sp.getName()).copyFrom(sp);
		}
		close();
	}

	private void checkValuesToSave() throws Exception {
		for (Species sp : gridItemList) {
			if (sp.getConcentration() == null)
				throw new Exception(sp.getName()+": missing concentration value");
		}
	}

	private Button getOkButton() {
		Button okButton = new Button("Ok");
		okButton.setId("okButton");
		okButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				saveAndClose();
			}
		});
		return okButton;
	}
}
