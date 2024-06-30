package cat.udl.easymodel.views.admin;


import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.CException;
import cat.udl.easymodel.utils.ToolboxVaadin;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.VaadinSession;

import java.sql.SQLException;
import java.util.ArrayList;

public class FormulasAdminVL extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    // External resources
    private SessionData sessionData = null;

    private VerticalLayout mainVL = null;
    private VerticalLayout optionButtonsVL = null;
    private VerticalLayout gridVL = null;
    private Grid<Formula> grid;

    private ArrayList<Formula> loadedFormulas = null;

    public FormulasAdminVL() {
        super();
        this.sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");

        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(true);

        optionButtonsVL = new VerticalLayout();
        optionButtonsVL.setPadding(false);
        optionButtonsVL.setWidthFull();
        optionButtonsVL.add(getBottomButtonsLayout());

        mainVL = new VerticalLayout();
        mainVL.setSpacing(true);
        mainVL.setPadding(false);
        mainVL.setWidth("100%");
        mainVL.setHeight("100%");
        updateDisplayContent();

        this.add(mainVL);
        this.setHorizontalComponentAlignment(Alignment.CENTER);
    }

    private void updateDisplayContent() {
        mainVL.removeAll();

        try {
            loadedFormulas = SharedData.getInstance().getDbManager().getAllFormulas();
            gridVL = getGridVL();
        } catch (Exception e) {
            ToolboxVaadin.showErrorNotification(SharedData.dbError);
        }
        mainVL.add(gridVL, optionButtonsVL);
        mainVL.expand(gridVL);
    }

    private VerticalLayout getGridVL() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);
        vl.setPadding(false);
        vl.setSizeFull();
        grid = new Grid<Formula>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        grid.setItems(loadedFormulas);
        grid.addColumn(new ComponentRenderer<>(f -> getTF("name", "Name", f.getNameRaw(), f))).setHeader("Name");
        grid.addColumn(new ComponentRenderer<>(f -> getTF("formula", "Formula", f.getFormulaDef(), f))).setHeader("Formula");
        grid.addColumn(new ComponentRenderer<>(f -> getCB("onesubstrateonly", f.isOneSubstrateOnly(), f))).setHeader("OneSubstrateOnly");
        grid.addColumn(new ComponentRenderer<>(f -> getCB("noproducts", f.isNoProducts(), f))).setHeader("NoProducts");
        grid.addColumn(new ComponentRenderer<>(f -> getCB("onemodifieronly", f.isOneModifierOnly(), f))).setHeader("OneModifierOnly");
        grid.addColumn(new ComponentRenderer<>(f -> getCB("formulatype", (f.getFormulaType() == FormulaType.PREDEFINED), f))).setHeader("Predefined");
        grid.addColumn(new ComponentRenderer<>(f -> getCB("delete", f.isDBDelete(), f))).setHeader("Delete");
        vl.add(grid);
        return vl;
    }

    private TextField getTF(String field, String prompt, String val, Formula f) {
        TextField tf = new TextField();
        tf.setWidth("100%");
        tf.setPlaceholder(prompt);
        tf.setValue(val);
        tf.setValueChangeMode(ValueChangeMode.ON_BLUR);
        tf.addValueChangeListener(event -> {
            String newVal = event.getValue();
            if (field.equals("name"))
                f.setName(newVal);
            else if (field.equals("formula"))
                f.setFormulaDef(newVal);
        });
        return tf;
    }

    private Checkbox getCB(String field, boolean val, Formula f) {
        Checkbox cb = new Checkbox();
        cb.setWidth("50px");
        cb.setValue(val);
        cb.addValueChangeListener(event -> {
            Boolean newVal = event.getValue();
            if (field.equals("onesubstrateonly"))
                f.setOneSubstrateOnly(newVal);
            else if (field.equals("noproducts"))
                f.setNoProducts(newVal);
            else if (field.equals("onemodifieronly"))
                f.setOneModifierOnly(newVal);
            else if (field.equals("formulatype"))
                f.setFormulaType(newVal ? FormulaType.PREDEFINED : FormulaType.MODEL);
            else if (field.equals("delete"))
                f.setDBDelete(newVal);
        });
        return cb;
    }

    private HorizontalLayout getBottomButtonsLayout() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(false);
        hl.setPadding(false);
        hl.setWidthFull();
        Button addPredefinedFormulaBtn = new Button("Add Predefined Formula");
        addPredefinedFormulaBtn.setIcon(VaadinIcon.PLUS.create());
        addPredefinedFormulaBtn.addClickListener(ev->{
            Formula f = new Formula("", "", FormulaType.PREDEFINED, null);
            loadedFormulas.add(f);
            grid.setItems(loadedFormulas);
            grid.scrollToEnd();
        });
        Button saveBtn = new Button("Save Changes");
        saveBtn.setIcon(VaadinIcon.HARDDRIVE.create());
        saveBtn.addClickListener(ev->{
            try {
                for (Formula f : loadedFormulas) {
                    if (!f.isDBDelete() && (!f.isValid() || f.isBlank())) {
                        throw new CException(
                                "Formula \"" + f.getNameRaw() + "\" contains errors\n(Incorrect Mathematica syntax?)");
                    }
                }
                try {
                    for (Formula f : loadedFormulas)
                        f.saveDBAdmin();
                } catch (Exception e) {
                    ToolboxVaadin.showErrorNotification("Database save error");
                }
                ToolboxVaadin.showSuccessNotification("Data saved");
                updateDisplayContent();
            } catch (CException ce) {
                ToolboxVaadin.showWarningNotification(ce.getMessage());
            }
        });
        VerticalLayout spacer = new VerticalLayout();
        hl.add(addPredefinedFormulaBtn,spacer,saveBtn);
        hl.expand(spacer);
        return hl;
    }
}
