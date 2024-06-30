package cat.udl.easymodel.views.modelbuilder.dialog;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ImportPredefinedFormulasDialog extends Dialog {
    private static final long serialVersionUID = 1L;
    private Model selModel = null;
    private VerticalLayout mainVL = null;
    private SharedData sharedData = SharedData.getInstance();
    private Grid<Formula> grid = null;
    private boolean isUpdate = false;

    public ImportPredefinedFormulasDialog(Model selModel) {
        super();

        this.selModel = selModel;

        this.setSizeFull();
        this.setModal(true);
        this.setDraggable(true);
        this.setResizable(true);

        mainVL = new VerticalLayout();
        mainVL.setSizeFull();
        mainVL.setPadding(false);

        VerticalLayout windowVL = new VerticalLayout();
        windowVL.setSpacing(true);
        windowVL.setMargin(true);
        windowVL.setPadding(false);
        windowVL.setSizeFull();
        windowVL.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
        windowVL.add(ToolboxVaadin.getDialogHeader(this, "Import Predefined Rates", null), mainVL, getOkCancelButtonsHL());
        windowVL.expand(mainVL);

        this.add(windowVL);

        displayWindowContent();
    }

    private void createGrid() {
        grid = new Grid<Formula>();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setSizeFull();
        grid.setItems(sharedData.getPredefinedPlusGenericFormulas());
        grid.addColumn(Formula::getNameToShow).setHeader("Name");
        grid.addColumn(Formula::getFormulaImportDef).setHeader("Rate Definition");
//		grid.addSelectionListener(event -> {
//			Set<Formula> selected = event.getAllSelectedItems();
//			Notification.show(selected.size() + " items selected");
//		});
    }

    private void displayWindowContent() {
        mainVL.removeAll();
        createGrid();
        mainVL.add(grid);
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

    private Button getOkButton() {
        Button button = new Button("Import");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.setWidth("150px");
        button.addClickListener(event -> {
            try {
                checkData();
                saveAndClose();
            } catch (Exception e) {
                ToolboxVaadin.showWarningNotification(e.getMessage());
            }
        });
        return button;
    }

    private Button getCancelButton() {
        Button button = new Button("Cancel");
        button.setWidth("150px");
        button.addClickListener(event -> {
            close();
        });
        return button;
    }

    private void checkData() throws Exception {
    }

    private void saveAndClose() {
        int max=50,i=0;
        for (Formula formulaToCopy : grid.getSelectedItems()) {
            if (i==max)
                break;
            Formula fCopy = new Formula(formulaToCopy,selModel);
            fCopy.setId(null);
            fCopy.setFormulaType(FormulaType.MODEL);
            fCopy.setDirty(true);
            selModel.getFormulas().addFormula(fCopy);
            i++;
        }
        isUpdate = true;
        close();
    }

    public boolean isUpdate() {
        return isUpdate;
    }
}
