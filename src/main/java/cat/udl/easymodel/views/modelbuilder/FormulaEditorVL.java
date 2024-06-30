package cat.udl.easymodel.views.modelbuilder;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import cat.udl.easymodel.views.modelbuilder.dialog.FormulaEditDialog;
import cat.udl.easymodel.views.modelbuilder.dialog.ImportPredefinedFormulasDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.VaadinSession;

public class FormulaEditorVL extends VerticalLayout {
    // External resources
    private SessionData sessionData = null;
    private Model selModel = null;

    private Grid<Formula> grid;
    private VerticalLayout contentVL = null;
    private VerticalLayout formulaListVL = null;
    private ReactionEditorVL reactionEditorVL = null;

    public FormulaEditorVL(ReactionEditorVL reactionEditorVL) {
        super();

        this.reactionEditorVL = reactionEditorVL;

        this.sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        this.selModel = this.sessionData.getSelectedModel();
        this.setSpacing(true);
        this.setPadding(true);
        this.setSizeFull();
        this.setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        contentVL = new VerticalLayout();
        contentVL.setSpacing(true);
        contentVL.setPadding(false);
        contentVL.setClassName("scroll");
        contentVL.setSizeFull();

        this.add(getHeaderVL(),contentVL);
        this.expand(contentVL);
        updateDisplayContent();
    }

    private void createGrid() {
        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        grid.setItems(selModel.getFormulas());
        grid.addColumn(Formula::getNameRaw).setHeader("Name");
        grid.addColumn(Formula::getFormulaDef).setHeader("Rate Definition");
        grid.addColumn(new ComponentRenderer<>(formula -> getEditButton(formula))).setHeader("Edit");
        grid.addColumn(new ComponentRenderer<>(formula -> getRemoveFormulaButton(formula))).setHeader("Remove");
    }

    private void updateDisplayContent() {
        contentVL.removeAll();
        createGrid();
        contentVL.add(grid);
    }

    private VerticalLayout getHeaderVL() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(false);
        vl.setPadding(false);
        HorizontalLayout hl1 = new HorizontalLayout();
        hl1.setWidthFull();

        hl1.setSpacing(true);
        hl1.setPadding(false);
        InfoDialogButton infoBtn = new InfoDialogButton("How to use Rate editor", "i- How to define rate expressions\r\n" +
                "    Usable operators: +-/*^()\r\n" +
                "    Reserved symbols:\r\n" +
                "        Mathematica functions/constants: m:<Mathematica function>\r\n" +
                "        Mathematica function indexes: i:<index>\r\n" +
                "        Special variables: b:t (time)\r\n" +
                "        b:X[]: Mathematica substrate list\r\n" +
                "        b:A[]: Mathematica substrate coefficient list\r\n" +
                "        b:M[]: Mathematica modifier list\r\n" +
                "        b:XF: first substrate\r\n" +
                "        b:MF: first modifier\r\n" +
                "    Example: m:Product[b:X[[i:j]]^g[[i:j]],{i:j,1,m:Length[b:X]}]\r\n" +
                "ii - Defining a new Rate expression\r\n" +
                "    1. Press \"Add Rate\" button\r\n" +
                "    2. Write the Mathematica expression for the Rate\r\n" +
                "iii - Edit rate expressions\r\n" +
                "    Press \"Edit\" button\r\n" +
                "iv - Importing predefined rate expressions\r\n" +
                "    1. Press \"Import Rates\" button\r\n" +
                "    2. Select the rates to import into the model", "600px", "600px");
        Button newRateBtn = getNewRateButton();
        Button importRatesBtn = getImportRatesButton();
        hl1.add(newRateBtn, importRatesBtn, infoBtn);
        hl1.expand(newRateBtn, importRatesBtn);
        hl1.setJustifyContentMode(JustifyContentMode.EVENLY);
        vl.add(hl1);

        return vl;
    }

    // BUTTONS
    private Button getEditButton(Formula f) {
        Button btn = new Button();
        btn.getElement().setAttribute("title", "Edit Rate");
        btn.setIcon(VaadinIcon.EDIT.create());
        btn.addClickListener(event -> {
            FormulaEditDialog few = new FormulaEditDialog(f);
            few.addDetachListener(e -> {
                if (few.isUpdateAfterClose()) {
                    updateDisplayContent();
                    reactionEditorVL.refreshAllLinkFormulaButtons();
                }
            });
            few.open();
        });
        return btn;
    }

    private Button getRemoveFormulaButton(Formula f) {
        Button btn = new Button();
        btn.getElement().setAttribute("title", "Remove Rate (only owned rates can be deleted from database)");
        btn.setIcon(VaadinIcon.CLOSE_CIRCLE.create());
        btn.addClickListener(event -> {
            selModel.unlinkFormulaFromReactions(f);
            selModel.getFormulas().removeFormula(f);
            grid.setItems(selModel.getFormulas());
            Notification.show("Rate " + f.getNameToShow() + " has been unlinked from all model reactions", 5000, Notification.Position.BOTTOM_END);
            reactionEditorVL.refreshAllLinkFormulaButtons();
        });
        return btn;
    }

    private Button getNewRateButton() {
        Button btn = new Button("New Rate");
        btn.setIcon(VaadinIcon.PLUS.create());
        btn.getElement().setAttribute("title", "Add a new rate");
        btn.addClickListener(event -> {
            FormulaEditDialog few = new FormulaEditDialog(null);
            few.addDetachListener(e -> {
                if (few.isUpdateAfterClose()) {
                    updateDisplayContent();
                }
            });
            few.open();
        });
        return btn;
    }

    private Button getImportRatesButton() {
        Button btn = new Button("Import Rates");
        btn.setIcon(VaadinIcon.HARDDRIVE.create());
        btn.getElement().setAttribute("title", "Import predifined rates");
        btn.addClickListener(event -> {
                ImportPredefinedFormulasDialog dia = new ImportPredefinedFormulasDialog(selModel);
                dia.addDetachListener(e -> {
                        if (dia.isUpdate()) {
                            updateDisplayContent();
                        }
                });
                dia.open();
        });
        return btn;
    }
}
