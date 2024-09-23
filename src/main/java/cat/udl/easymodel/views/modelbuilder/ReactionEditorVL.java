package cat.udl.easymodel.views.modelbuilder;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.model.ReactionUtils;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import cat.udl.easymodel.views.modelbuilder.dialog.DescriptionEditDialog;
import cat.udl.easymodel.views.modelbuilder.dialog.LinkReactionFormulaDialog;
import cat.udl.easymodel.views.modelbuilder.dialog.SpeciesDialog;
import cat.udl.easymodel.views.modelbuilder.dialog.ValidateModelDialog;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.VaadinSession;

import java.util.ArrayList;
import java.util.HashMap;

public class ReactionEditorVL extends VerticalLayout {
    private SessionData sessionData;
    private VerticalLayout reactionsVL = new VerticalLayout();

    private Model selectedModel;
    private HashMap<Reaction, Button> linkFormulaButtons = new HashMap<>();
    private Grid<Reaction> gridReactions;
    private ArrayList<TextField> textFields = new ArrayList<>();

    private Button addReactionBtn = null;
    private TextField nameTF = null;
    private boolean isAdding = false;

    public ReactionEditorVL() {
        super();
        sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        this.selectedModel = sessionData.getSelectedModel();

        addReactionBtn = getAddReactionButton();

        this.setSizeFull();
        this.setPadding(true);
        this.setSpacing(true);

        reactionsVL.setWidth("100%");
        reactionsVL.setSpacing(false);
        reactionsVL.setPadding(true);

        gridReactions = getGridReactions();
        updateReactionsOfGrid();

        VerticalLayout headerVL = getHeaderVL();

        VerticalLayout footerVL = getFooterVL();

        this.add(headerVL, gridReactions,footerVL);
        this.expand(gridReactions);

        if (nameTF.getValue().isEmpty())
            nameTF.focus();
    }

    private Grid<Reaction> getGridReactions() {
        Grid<Reaction> grid = new Grid<>();
//        Shortcuts.addShortcutListener(grid, this::addReaction, Key.ENTER);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        grid.addColumn(Reaction::getIdJavaStr).setHeader("Name").setFlexGrow(0).setWidth("70px").setResizable(true);
        grid.addColumn(new ComponentRenderer<>(reaction -> {
            Button btn = getLinkFormulaButton(reaction);
            linkFormulaButtons.put(reaction, btn);
            return btn;
        })).setHeader("Rate Law").setWidth("90px").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(reaction -> {
//            System.out.println("tf");
            TextField tf = getReactionTextField(reaction);
            tf.addKeyDownListener(Key.ENTER, ev -> {
                if (!textFields.isEmpty() && tf == textFields.get(textFields.size() - 1))
                    addReaction();
            });
            textFields.add(tf);
            if (reaction.isBlank())
                tf.focus();
            return tf;
        })).setHeader("Reaction").setFlexGrow(1).setResizable(true);
        grid.addColumn(new ComponentRenderer<>(reaction -> {
            Button btn = getRemoveReactionButton(reaction);
            return btn;
        })).setHeader("Remove").setWidth("90px").setFlexGrow(0);
        return grid;
    }

    private void updateReactionsOfGrid() {
        linkFormulaButtons.clear();
        textFields.clear();
        gridReactions.setItems(selectedModel);
//        gridReactions.getDataProvider().refreshAll();
    }

    private VerticalLayout getHeaderVL() {
        InfoDialogButton infoBtn = new InfoDialogButton("How to use " + SharedData.appName, "How to use EasyModel\r\n" +
                "1. Define processes\r\n" +
                "    Reaction definition: Substrates -> Products ; Modifiers\r\n" +
                "    How to write: n1*A1 + n2*A2 + ... -> m1*B1 + m2*B2 + ... ; M1 M2 ...\r\n" +
                "    Legend: nX,mX: coefficient; AX,BX: species; MX: modifier\r\n" +
                "2. Define model rates\r\n" +
                "3. Select a rate for every reaction\r\n" +
                "	Press the \"Link rate\" button\r\n" +
                "4. Define initial condition (Species button)\r\n" +
                "5. Validate model\r\n" +
                "6. Run Simulation", "600px", "400px");
        nameTF = new TextField();
        nameTF.setTitle("Name");
        nameTF.setPlaceholder("Model Name");
//        nameTF.setLabel("Model Name");
        nameTF.setValue(selectedModel.getName());
        nameTF.setWidth("100%");
        nameTF.addValueChangeListener(event -> {
            String newName = event.getValue();
            newName = newName.substring(0, Math.min(newName.length(), 300));
            selectedModel.setName(newName);
        });

        Button descBtn = new Button();
        descBtn.setText("Description");
        descBtn.setIcon(VaadinIcon.BOOK.create());
        descBtn.setWidth("200px");
        //descBtn.setId("editDescription");
        descBtn.addClickListener(e -> {
            new DescriptionEditDialog(selectedModel).open();
        });

    //        HorizontalLayout hl11 = new HorizontalLayout();
    //        hl11.setPadding(false);
    //        hl11.setSpacing(false);
    //        hl11.setWidth("100%");
    //        hl11.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
    //        hl11.add(nameTF, descBtn);
    //        hl11.expand(nameTF);

        HorizontalLayout hl1 = new HorizontalLayout();
        hl1.setPadding(false);
        hl1.setSpacing(true);
        hl1.setWidthFull();
        hl1.add(nameTF, descBtn, infoBtn);
        hl1.expand(nameTF);
        hl1.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        HorizontalLayout hl2 = new HorizontalLayout();
        hl2.setPadding(false);
        hl2.setSpacing(true);
        hl2.setWidthFull();
        hl2.addAndExpand(addReactionBtn, getSpeciesButton());
        hl2.setAlignItems(Alignment.BASELINE);

        VerticalLayout vl = new VerticalLayout();
        vl.setPadding(false);
        vl.setSpacing(false);
        vl.setDefaultHorizontalComponentAlignment(Alignment.START);
        vl.setWidthFull();
        vl.add(hl1, hl2);
        return vl;
    }

    private VerticalLayout getFooterVL() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(false);
        vl.setPadding(false);
        vl.setWidth("100%");

        HorizontalLayout hl1 = new HorizontalLayout();
        hl1.setSpacing(true);
        hl1.setPadding(false);
        hl1.setWidth("100%");
        Button validateModelBtn = getValidateModelButton();
        hl1.add(validateModelBtn);
        hl1.expand(validateModelBtn);
        vl.add(hl1);

        if (sessionData.isUserSet()){
            Button saveBtn = new Button("Save to Private Repository");
            saveBtn.setIcon(VaadinIcon.DISC.create());
            saveBtn.addClickListener(ev -> {
                ValidateModelDialog dia = new ValidateModelDialog(true);
                dia.open();
            });
            hl1.add(saveBtn);
        }
        return vl;
    }

    private Button getValidateModelButton() {
        Button btn = new Button();
        btn.setText("Validate");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.setIcon(VaadinIcon.CHECK.create());
//        btn.setWidth("100%");
        btn.addClickListener(e -> {
            ValidateModelDialog dia = new ValidateModelDialog(false);
            dia.open();
        });
        return btn;
    }

    private Button getSpeciesButton() {
        Button btn = new Button("Species");
        btn.setIcon(VaadinIcon.PILLS.create());
//        btn.setWidth("100%");
        btn.addClickListener(e2 -> {
            try {
                selectedModel.checkReactions();
                new SpeciesDialog(selectedModel).open();
            } catch (Exception e) {
                ToolboxVaadin.showWarningNotification(e.getMessage());
            }
        });
        return btn;
    }

    private Button getAddReactionButton() {
        Button btn = new Button("Add Reaction");
        btn.setIcon(VaadinIcon.PLUS.create());
//        btn.setWidth("100%");
        btn.setId("addButton");
        btn.addClickListener(e -> {
            addReaction();
        });
        return btn;
    }

    private void addReaction() {
        isAdding = true;
        selectedModel.addReaction(new Reaction());
        updateReactionsOfGrid();
        gridReactions.scrollToEnd();
        isAdding = false;
    }

    private Button getRemoveReactionButton(Reaction react) {
        Button btn = new Button();
        btn.setIcon(VaadinIcon.CLOSE_CIRCLE.create());
//        btn.setWidth("37px");
        btn.addClickListener(event -> {
            if (selectedModel.size() > 1) {
                selectedModel.removeReaction(react);
                updateReactionsOfGrid();
            }
        });
        return btn;
    }

    private TextField getReactionTextField(Reaction react) {
        TextField tf = new TextField();
        tf.setPlaceholder("A -> B ; C");
        tf.setWidth("100%");
        tf.setValueChangeMode(ValueChangeMode.EAGER);
        tf.addValueChangeListener(event -> {
            String newText = event.getValue();
            setReactionTextFieldStyle(newText, tf);
        });
        tf.addBlurListener(event -> {
            react.setReactionStr(tf.getValue());
            setReactionTextFieldStyle(tf.getValue(), tf);
            refreshLinkFormulaButton(react);
        });
        tf.setValue(react.getReactionStr());
        return tf;
    }

    private void setReactionTextFieldStyle(String reactionStr, TextField tf) {
        if (ReactionUtils.isBlank(reactionStr))
            tf.setClassName("");
        else if (ReactionUtils.isValid(reactionStr)) {
            tf.setClassName("greenBG");
        } else
            tf.setClassName("redBG");
    }

    private Button getLinkFormulaButton(Reaction react) {
        Button btn = new Button();
        btn.getElement().setProperty("title", "Rate Law selection");
        btn.setWidth("50px");
        btn.setHeight("37px");
        if (react.isFormulaAndParametersSet())
            btn.setClassName("linkedRateBtn");
        else
            btn.setClassName("linkRateBtn");
        btn.addClickListener(e -> {
            if (react.isValid()) {
                LinkReactionFormulaDialog dia = new LinkReactionFormulaDialog(react, selectedModel);
                dia.addDetachListener(ev -> {
                    refreshLinkFormulaButton(react);
                });
                dia.open();
            } else {
                ToolboxVaadin.showWarningNotification("Invalid reaction: please check reaction format");
            }
        });
        return btn;
    }

    private void refreshLinkFormulaButton(Reaction react) {
        if (react.isFormulaAndParametersSet())
            linkFormulaButtons.get(react).setClassName("linkedRateBtn");
        else
            linkFormulaButtons.get(react).setClassName("linkRateBtn");
    }

    public void refreshAllLinkFormulaButtons() {
        for (Reaction r : selectedModel)
            refreshLinkFormulaButton(r);
    }

//    private TextField getReactionIdTextField(Reaction r) {
//        TextField tf = new TextField();
//        tf.setData(r);
//        tf.setWidth("60px");
//        tf.setValue("R" + r.getIdJava());
//        tf.setReadOnly(true);
//        return tf;
//    }

//    private void openLinkFormulaDialog(Reaction react, Button btn) {
//        if (!react.isValid()) {
//            Notification.show("Invalid reaction", Type.WARNING_MESSAGE);
//            return;
//        }
//        if (!react.parse()) {
//            Notification.show("Reaction cannot use Rate reserved words", Type.WARNING_MESSAGE);
//            return;
//        }
//        Formulas compatibleFormulas = selectedModel.getFormulas().getFormulasCompatibleWithReaction(react);
//        if (compatibleFormulas.isEmpty()) {
//            Notification.show("No compatible rates found.\nTip: go to \"Define Rates\" and create or import new rates.", Type.WARNING_MESSAGE);
//            return;
//        }
//         If all checks are ok show the Dialog
//        LinkReactionFormulaDialog rLinkDialog = new LinkReactionFormulaDialog(react, compatibleFormulas);
//        rLinkDialog.addCloseListener(new CloseListener() {
//            @Override
//            public void DialogClose(CloseEvent e) {
//                refreshLinkFormulaButton(react);
//            }
//        });
//        UI.getCurrent().addDialog(rLinkDialog);
//    }
//
//    public void refreshAllLinkFormulaButtons() {
//        for (Reaction r : selectedModel) {
//            refreshLinkFormulaButton(r);
//        }
//    }
//
//    private void refreshLinkFormulaButton(Reaction r) {
//        Button btn = linkFormulaButtons.get(r);
//        if (r.isAllReactionDataFullfiled()) {
//            btn.setStyleName("linkFnDone");
//            btn.setDescription("Selected rate: "+r.getFormula().getNameToShow());
//        } else {
//            btn.setStyleName("linkFn");
//            btn.setDescription("Select Rate");
//        }
//    }
}
