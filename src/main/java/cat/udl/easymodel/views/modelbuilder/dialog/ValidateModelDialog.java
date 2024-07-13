package cat.udl.easymodel.views.modelbuilder.dialog;


import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.P;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.views.simulationlauncher.SimulationLauncherView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.server.VaadinSession;

public class ValidateModelDialog extends Dialog {
    private VerticalLayout contentVL;
    private SessionData sessionData;
    private Model selectedModel;
    private boolean isModelValid = true;
    private boolean isToSave = false;

    public ValidateModelDialog(boolean isToSave) {
        super();

        this.isToSave=isToSave;
        this.sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        this.selectedModel = sessionData.getSelectedModel();
        this.setModal(true);
        this.setResizable(true);
        this.setDraggable(true);
        if (!isToSave) {
            this.setWidth("820px");
            this.setHeight("100%");
        } else {
            this.setWidth("600px");
            this.setHeight("600px");
        }

        VerticalLayout winVL = new VerticalLayout();
        winVL.setSpacing(true);
        winVL.setPadding(false);
        winVL.setSizeFull();

        contentVL = new VerticalLayout();
        contentVL.setSpacing(false);
        contentVL.setPadding(false);
        contentVL.setWidth("100%");

        try {
            selectedModel.checkValidModel();
            contentVL.add(getNewStyledSpan("Validation: OK"));
            contentVL.add(getSpacer());
            if (!isToSave) {
                contentVL.add(getNewStyledSpan("Stoichiometric Matrix"),
                        selectedModel.getDisplayStoichiometricMatrix());
                contentVL.add(getSpacer());
                contentVL.add(getNewStyledSpan("Regulatory Matrix"), selectedModel.getDisplayRegulatoryMatrix());
            } else { // save model to private repository
                String saveMsg = "";
                String saveMsgColor = "blue";
                if ((selectedModel.getId() == null && sessionData.getModels().getModelByName(selectedModel.getName()) != null) ||
                        (selectedModel.getId() != null && sessionData.getModels().getModelByName(selectedModel.getName()) != null && selectedModel.getUser() != sessionData.getUser())
                        ) {
                    saveMsg="SAVE ERROR: model name is already in use. Please change the name.";
                    saveMsgColor = "red";
                }else {
                    try {
                        if (selectedModel.getRepositoryType()==RepositoryType.PUBLIC) {
                            selectedModel.setId(null);
                            selectedModel.setParent(null);
                        }
                        boolean saveToModelList = selectedModel.getId() == null;
                        selectedModel.setUser(sessionData.getUser());
                        selectedModel.setRepositoryType(RepositoryType.PRIVATE);
                        selectedModel.saveDB(); //insert/update
                        saveMsg="MODEL SAVED.\nModel will be automatically published in " +
                                SharedData.getInstance().getProperties().getProperty("privateWeeks") + " weeks unless it's deleted before this period.";
                        if (saveToModelList) {
                            Model newModel = new Model();
                            newModel.setId(selectedModel.getId());
                            newModel.basicLoadDB();
                            selectedModel.setParent(newModel);
                            sessionData.getModels().addModel(newModel);
                        }
                    } catch (Exception e2) {
                        saveMsg="SAVE ERROR: DB failure";
                        saveMsgColor = "red";
                    }
                }
                Span saveMsgSpan = new Span(saveMsg);
                saveMsgSpan.getStyle().setColor(saveMsgColor);
                saveMsgSpan.getStyle().setFontWeight(600);
                contentVL.add(saveMsgSpan);
            }
        } catch (Exception e) {
            Span resLab = getNewStyledSpan("Model errors found:");
            TextArea ta = new TextArea();
            ta.setMaxLength(4000);
            ta.setWidth("100%");
            ta.setHeight("300px");
            ta.setValue(e.getMessage());
            contentVL.add(resLab, ta);
            isModelValid = false;
        }
        winVL.add(ToolboxVaadin.getDialogHeader(this, (isToSave ? "Save Model":"Model Validate"), null), contentVL, getFooterHL());
        winVL.expand(contentVL);
        this.add(winVL);
    }
    private VerticalLayout getSpacer() {
        VerticalLayout spacer = new VerticalLayout();
        spacer.setPadding(false);
        spacer.setHeight("12px");
        return spacer;
    }
    private Span getNewStyledSpan(String txt){
        Span span = new Span(txt);
        span.getStyle().setFontWeight(600);
        return span;
    }

    private HorizontalLayout getFooterHL() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.setSpacing(true);
        hl.setPadding(false);
        HorizontalLayout spacer = new HorizontalLayout();
        hl.add(spacer);
        if (isModelValid)
            hl.add(getSimulationButton());
        hl.add(getCloseButton());
        hl.expand(spacer);
        return hl;
    }

    private Button getSimulationButton() {
        Button btn = new Button("Configure Simulation");
//        btn.setIcon(VaadinIcon.ROCKET.create());
        btn.setWidth("200px");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.addClickListener(event -> {
            btn.getUI().ifPresent(ui ->
                    ui.navigate(SimulationLauncherView.class));
            close();
        });
        btn.addClickShortcut(Key.ENTER);
        return btn;
    }

    private Button getCloseButton() {
        Button btn = new Button("Close");
        btn.setWidth("100px");
        btn.addClickListener(event -> {
            close();
        });
        if (!isModelValid)
            btn.addClickShortcut(Key.ENTER);
        return btn;
    }
}
