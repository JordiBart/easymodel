package cat.udl.easymodel.views.modelbuilder.dialog;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Species;
import cat.udl.easymodel.logic.types.SpeciesVarTypeType;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.ArrayList;

public class SpeciesDialog extends Dialog {
	private Model model = null;
	private VerticalLayout centerVL;

	private Grid<Species> grid;
	private ArrayList<Species> gridItemList = new ArrayList<>();

	private boolean isSaveAndClose = false;

	public SpeciesDialog(Model model) {
        super();

        this.model = model;
        this.setWidth("700px");
        this.setHeight("600px");
        this.setModal(true);
        this.setResizable(true);
        setDraggable(true);

        VerticalLayout globalVL = new VerticalLayout();
        globalVL.setSpacing(true);
        globalVL.setPadding(false);
        globalVL.setSizeFull();
        globalVL.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
        globalVL.add(ToolboxVaadin.getDialogHeader(this,"Species Settings",new InfoDialogButton("Species Settings", "1. Set the initial concentration for every species of the model.\n" + "2. Set the variable type:\n" +
				"\t-Time Dependent: variable value evolves trough time.\n" +
				"\t-Independent: variable value remains constant trough time.",
				"600px", "600px")));

        centerVL = new VerticalLayout();
        centerVL.setSizeFull();
		centerVL.setPadding(false);
		centerVL.setSpacing(false);
//        centerVL.setClassName("scroll");

//        HorizontalLayout headHL = new HorizontalLayout();
//        headHL.setWidth("100%");
//        VerticalLayout spacer = new VerticalLayout();
//        headHL.add(spacer);
//        headHL.add();
//        headHL.expand(spacer);

        globalVL.add(centerVL,getOkCancelButtonsHL());
        globalVL.expand(centerVL);

        this.add(globalVL);
//		p.p("dbg-end linkreaction create");

		displayDialogContent();
	}

	private void displayDialogContent() {
		centerVL.removeAll();

		grid = new Grid<>();
		grid.setSelectionMode(Grid.SelectionMode.NONE);
		grid.setSizeFull();
		fillGridItemList();
		grid.setItems(gridItemList);
		grid.addColumn(Species::getName).setHeader("Name").setResizable(true);
		grid.addColumn(new ComponentRenderer<>(entry -> {
			TextField tf = new TextField();
			tf.setWidth("100%");
			tf.setPlaceholder("Numeric value");
			tf.setValue(entry.getConcentration() != null ? entry.getConcentration() : "");
			tf.setValueChangeMode(ValueChangeMode.ON_BLUR);
			tf.addValueChangeListener(event -> {
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
		})).setHeader("Initial Concentration").setResizable(true);
		grid.addColumn(new ComponentRenderer<>(entry -> {
			Select<SpeciesVarTypeType> ns = new Select<>();
			ns.setWidth("100%");
			ns.setEmptySelectionAllowed(false);
			ns.setItems(SpeciesVarTypeType.values());
			ns.setTextRenderer(SpeciesVarTypeType::getString);
			if (!model.getAllSpeciesExceptModifiers().containsKey(entry.getName()))
				entry.setVarType(SpeciesVarTypeType.INDEPENDENT);
			ns.setValue(entry.getVarType());
			if (!model.getAllSpeciesExceptModifiers().containsKey(entry.getName()))
				ns.setEnabled(false);
			ns.addValueChangeListener(event -> {
				entry.setVarType(event.getValue());
			});
			return ns;
		})).setHeader("Variable Type").setResizable(true);
		centerVL.add(grid);
		centerVL.expand(grid);
	}

	private void fillGridItemList() {
		gridItemList.clear();
		for (Species sp : model.getAllSpecies().values()) {
			gridItemList.add(new Species(sp));
		}
	}

	private HorizontalLayout getOkCancelButtonsHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		HorizontalLayout spacer = new HorizontalLayout();
		hl.add(spacer, getOkButton(), getCancelButton());
		hl.expand(spacer);
		return hl;
	}

	private Button getCancelButton() {
		Button btn = new Button("Cancel");
		btn.setWidth("150px");
		btn.addClickListener(event-> {
				close();
		});
		return btn;
	}

	private void saveAndClose() {
		try {
			checkValuesToSave();
		} catch (Exception e) {
			ToolboxVaadin.showWarningNotification(e.getMessage());
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
		Button btn = new Button("Ok");
		btn.setWidth("150px");
		btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		btn.addClickListener(event-> {
				saveAndClose();
		});
		return btn;
	}
}
