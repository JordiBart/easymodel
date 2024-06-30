package cat.udl.easymodel.views.modelbuilder.dialog;


import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.views.simulationlauncher.SimulationLauncherView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.server.VaadinSession;

public class ValidateModelDialog extends Dialog {
    private VerticalLayout contentVL;
    private SessionData sessionData;
    private Model selectedModel;
    private boolean isModelValid = true;

    public ValidateModelDialog() {
        super();

        this.sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        this.selectedModel = sessionData.getSelectedModel();
        this.setModal(true);
        this.setResizable(true);
        setDraggable(true);
        this.setWidth("600px");
        this.setHeight("600px");

        VerticalLayout winVL = new VerticalLayout();
        winVL.setSpacing(true);
        winVL.setPadding(false);
        winVL.setSizeFull();

        contentVL = new VerticalLayout();
        contentVL.setSpacing(true);
        contentVL.setPadding(false);
        contentVL.setWidth("100%");
        contentVL.setClassName("scroll");

        try {
            selectedModel.checkValidModel();
            Span resLab = new Span("Validation: OK");
            contentVL.add(resLab);
            contentVL.add(new Span("Stoichiometric Matrix"),
                    selectedModel.getDisplayStoichiometricMatrix());
            contentVL.add(new Span("Regulatory Matrix"), selectedModel.getDisplayRegulatoryMatrix());
        } catch (Exception e) {
            Span resLab = new Span("Model errors found:");
            TextArea ta = new TextArea();
            ta.setMaxLength(4000);
            ta.setWidth("100%");
            ta.setHeight("300px");
            ta.setValue(e.getMessage());
            contentVL.add(resLab, ta);
            isModelValid = false;
        }
        winVL.add(ToolboxVaadin.getDialogHeader(this, "Model validation", null), contentVL, getFooterHL());
        winVL.expand(contentVL);
        this.add(winVL);
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
        Button btn = new Button("Configure simulation");
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
