package cat.udl.easymodel.views.admin;


import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Models;
import cat.udl.easymodel.logic.types.RepositoryType;
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

public class ModelsAdminVL extends VerticalLayout {
    // External resources
    private SessionData sessionData = null;

    private VerticalLayout mainVL = null;
    private VerticalLayout optionButtonsVL = null;
    private VerticalLayout gridVL = null;
    private Grid<Model> grid;

    private Models loadedModels = null;

    public ModelsAdminVL() {
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
        this.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

    private void updateDisplayContent() {
        mainVL.removeAll();

        try {
            loadedModels = new Models();
            loadedModels.semiLoadDB();
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
        grid = new Grid<Model>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        grid.setItems(loadedModels);
        grid.addColumn(new ComponentRenderer<>(m -> getTF("name", "Name", m.getName(), m))).setHeader("Name");
        grid.addColumn(new ComponentRenderer<>(m -> getCB("repositorytype", m.getRepositoryType() == RepositoryType.PUBLIC, m))).setHeader("Public");
        grid.addColumn(Model::getUserName).setHeader("User");
        grid.addColumn(new ComponentRenderer<>(m -> getCB("delete", m.isDBDelete(), m))).setHeader("Delete");
        vl.add(grid);
        return vl;
    }

    private TextField getTF(String field, String prompt, String val, Model m) {
        TextField tf = new TextField();
        tf.setWidth("100%");
        tf.setPlaceholder(prompt);
        tf.setValue(val);
        tf.setValueChangeMode(ValueChangeMode.ON_BLUR);
        tf.addValueChangeListener(event -> {
            String newVal = event.getValue();
            if (field.equals("name"))
                m.setName(newVal);
        });
        return tf;
    }

    private Checkbox getCB(String field, boolean val, Model m) {
        Checkbox cb = new Checkbox();
        cb.setWidth("50px");
        cb.setValue(val);
        cb.addValueChangeListener(event -> {
            Boolean newVal = event.getValue();
            if (field.equals("repositorytype"))
                m.setRepositoryType(newVal ? RepositoryType.PUBLIC : RepositoryType.PRIVATE);
            else if (field.equals("delete"))
                m.setDBDelete(newVal);
        });
        return cb;
    }

    private HorizontalLayout getBottomButtonsLayout() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(false);
        hl.setPadding(false);
        hl.setWidthFull();
        Button saveBtn = new Button("Save Changes");
        saveBtn.setIcon(VaadinIcon.HARDDRIVE.create());
        saveBtn.addClickListener(ev->{
            try {
                for (Model m : loadedModels) {
                    if (!m.isDBDelete() && (m.getName().isEmpty())) {
                        throw new CException(
                                "Model \"" + m.getName() + "\" contains errors:\nModel requires a name");
                    }
                }
                try {
                    for (Model m : loadedModels)
                        m.saveDBAdmin();
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
        hl.add(spacer,saveBtn);
        hl.expand(spacer);
        return hl;
    }
}
