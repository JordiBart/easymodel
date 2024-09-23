package cat.udl.easymodel.views.modelbuilder.dialog;

import cat.udl.easymodel.logic.formula.*;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.P;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.server.VaadinSession;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.jsoup.nodes.Document.OutputSettings.Syntax.html;

public class LinkReactionFormulaDialog extends Dialog {

    private SessionData sessionData;
    private SharedData sharedData = SharedData.getInstance();
    private Reaction reaction = null;
    private Formulas compatibleFormulas = null;
    private VerticalLayout parametersVL;
    private InfoDialogButton showFormulaDefButton;

    private Select<Formula> formulaSelect;
    private SortedMap<String, FormulaValue> parameterMap = new TreeMap<>();
    private ArrayList<ComboBox> genParamComboBoxList = new ArrayList<>();
    private ArrayList<String> availableSubstratesAndModifiers = new ArrayList<>();

    private SortedMap<String, SortedMap<String, FormulaArrayValue>> formulaSubstratesArrayParametersMap = new TreeMap<>();
    private SortedMap<String, SortedMap<String, FormulaArrayValue>> formulaModifiersArrayParametersMap = new TreeMap<>();

    private boolean disableSelectValueChange = false;

    public LinkReactionFormulaDialog(Reaction reaction, Model selectedModel) {
        super();

        this.sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        this.reaction = reaction;
        this.compatibleFormulas = selectedModel.getFormulas().getFormulasCompatibleWithReaction(reaction);

        this.setWidth("700px");
        this.setHeight("700px");
//        this.setHeightFull();
        this.setModal(true);
        this.setResizable(true);
        this.setDraggable(true);

        VerticalLayout globalVL = new VerticalLayout();
        globalVL.setSpacing(true);
        globalVL.setPadding(false);
        globalVL.setSizeFull();
        globalVL.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
        globalVL.add(ToolboxVaadin.getDialogHeader(this, "Rate Law Selection for "+reaction.getIdJavaStr()+" Reaction", new InfoDialogButton("How to define Reaction Rate",
                "Defining the Reaction Rate:\n" + "1. Use the drop-down menu to select alternative rate functions\n"
                        + "2. Input the numerical values for the parameters\n"
                        + "3. In multi-substrate reactions assign each substrate to its corresponding symbol\n"
                        + "4. In multi-modifier reactions assign each modifier to its corresponding symbol",
                "700px", "300px")));

        formulaSelect = getFormulaSelect();

        parametersVL = new VerticalLayout();
        parametersVL.setSizeFull();
        parametersVL.setPadding(false);
        parametersVL.setSpacing(false);
        parametersVL.setClassName("scroll");

        HorizontalLayout headHL = new HorizontalLayout();
        headHL.setWidth("100%");
        headHL.add(formulaSelect);
        showFormulaDefButton = new InfoDialogButton("Reaction and Rate expressions",
                "",
                "700px", "300px");
        showFormulaDefButton.setIcon(VaadinIcon.NOTEBOOK.create());
        headHL.add(showFormulaDefButton);
        headHL.expand(formulaSelect);

        globalVL.add(headHL, parametersVL, getOkCancelButtonsHL());
        globalVL.expand(parametersVL);

        // Load formula and previous parameters
        if (reaction.getFormula() != null && compatibleFormulas.contains(reaction.getFormula())) {
            formulaSelect.setValue(reaction.getFormula());
        } else if (!compatibleFormulas.isEmpty()) {
            formulaSelect.setValue(compatibleFormulas.get(0));
        } else {
            parametersVL.removeAll();
            parametersVL.add(new Span("No compatible rates found for this reaction in the model rate list"));
            parametersVL.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        }

        this.add(globalVL);
//		p.p("dbg-end linkreaction create");
    }

    private void updateAllParametersVL() {
        parametersVL.removeAll();
        if (formulaSelect.getValue() == null) {
            parametersVL.add(new Span("Please select a Rate Law"));
            parametersVL.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        } else {
            boolean isAddSubstratesArray = formulaSubstratesArrayParametersMap.size() > 0 && ((SortedMap<String, FormulaArrayValue>) formulaSubstratesArrayParametersMap.values().toArray()[0]).size() > 0;
            boolean isAddModifiersArray = formulaModifiersArrayParametersMap.size() > 0 && ((SortedMap<String, FormulaArrayValue>) formulaModifiersArrayParametersMap.values().toArray()[0]).size() > 0;
            parametersVL.add(getFormulaGenericParametersVL(isAddSubstratesArray||isAddModifiersArray));
            if (isAddSubstratesArray) {
                parametersVL.add(getConstantsBySubstratesVL());
            }
            if (isAddModifiersArray) {
                parametersVL.add(getConstantsByModifiersVL());
            }
        }
//		p.p("dbg-end linkreaction display");
    }

    private Select<Formula> getFormulaSelect() {
        Select<Formula> select = new Select<>();
        select.setEmptySelectionAllowed(true);
        select.getElement().setProperty("title", "Select Rate Function");
        select.setWidth("100%");
        select.setTextRenderer(Formula::getNameToShow);
        select.setItems(compatibleFormulas);
        select.addValueChangeListener(event -> {
            showFormulaDefButton.setContent(
                            "Selected Reaction " + reaction.getIdJavaStr() + ":\n" + reaction.getReactionStr()
                    + "\n\n" +
                    (formulaSelect.getValue() != null ? "Selected Rate "+ formulaSelect.getValue().getNameRaw() + ":\n" + formulaSelect.getValue().getFormulaDef() : "")
            );
            resetAndLoadAllParametersMaps();
            updateAllParametersVL();
        });
        return select;
    }

    // FORMULA VALUES

    private VerticalLayout getFormulaGenericParametersVL(boolean isWithOtherVL) {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(false);
        vl.setPadding(false);
        if (!isWithOtherVL)
            vl.setSizeFull();
        else {
            this.setWidth("900px");
            this.setHeightFull();
        }

        Grid<FormulaValue> genParsGrid = new Grid<>();
        if (!isWithOtherVL)
            genParsGrid.setSizeFull();
        else {
            genParsGrid.setWidthFull();
            genParsGrid.setHeight("200px");
        }
        genParsGrid.addColumn(FormulaValue::getParameterName).setHeader("Parameter").setResizable(true);
        ArrayList<String> allSubstratesAndModifiers = new ArrayList<>();
        for (String sp : reaction.getLeftPartSpecies().keySet())
            allSubstratesAndModifiers.add(sp);
        for (String sp : reaction.getModifiers().keySet())
            allSubstratesAndModifiers.add(sp);
        genParsGrid.addColumn(new ComponentRenderer<>(fv -> {
            ComboBox<String> cb = new ComboBox<>();
            cb.setWidthFull();
            if (!fv.isForceConstant()) {
                cb.setPlaceholder("Number/Substrate/Modifier");
                cb.setItems(allSubstratesAndModifiers);
            } else {
                cb.setPlaceholder("Number");
                cb.setItems("Only numbers allowed");
            }
            cb.setAutoOpen(false);
            cb.setAllowCustomValue(true);
            cb.addCustomValueSetListener(ev -> {
                // Only numbers
                // Called only on custom values
                try {
                    String newVal = Utils.evalMathExpr(ev.getDetail());
                    cb.setValue(newVal);
                    fv.setType(FormulaValueType.CONSTANT);
                    fv.setValue(newVal);
                } catch (Exception e) {
                    cb.setValue("");
                    fv.setType(null);
                    fv.setValue(null);
                }
            });
            cb.addValueChangeListener(ev -> {
                // Only substates, modifiers
                // Called ALWAYS
                if (cb.getValue() == null) {
                    fv.setType(null);
                    fv.setValue(null);
                } else {
                    if (fv.isForceConstant()) {
                        try {
                            Utils.evalMathExpr(cb.getValue());
                        } catch (Exception e) {
                            cb.setValue("");
                            fv.setValue(null);
                            fv.setType(null);
                        }
                    } else if (reaction.getLeftPartSpecies().containsKey(cb.getValue())) {
                        fv.setType(FormulaValueType.SUBSTRATE);
                        fv.setValue(cb.getValue());
                    } else if (reaction.getModifiers().containsKey(cb.getValue())) {
                        fv.setType(FormulaValueType.MODIFIER);
                        fv.setValue(cb.getValue());
                    }
                }
            });
            if (fv.getValue() != null)
                cb.setValue(fv.getValue());
            genParamComboBoxList.add(cb);
            return cb;
        })).setHeader("Value (Number/Substrate/Modifier)").setResizable(true);
        genParsGrid.setItems(parameterMap.values());
        vl.add(ToolboxVaadin.getCaption("Parameters values"), genParsGrid);
        return vl;
    }

    // CONSTANTS BY SUBSTRATES

    private VerticalLayout getConstantsBySubstratesVL() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(false);
        vl.setPadding(false);

        Grid<String> grid = new Grid<>();
        grid.setWidthFull();
        grid.setHeight("200px");
        grid.addColumn(String::toString).setHeader("Parameter").setResizable(true);
        SortedMap<String, FormulaArrayValue> firstMap = null;
        for (SortedMap<String, FormulaArrayValue> spMap : formulaSubstratesArrayParametersMap.values()) {
            firstMap = spMap;
            break;
        }
        for (String sp : firstMap.keySet()) {
            grid.addColumn(new ComponentRenderer<>(par -> {
                TextField tf = new TextField();
                tf.setWidthFull();
                tf.setPlaceholder("Number");
                tf.setValueChangeMode(ValueChangeMode.ON_BLUR);
                tf.addValueChangeListener(ev -> {
                    try {
                        String newVal = Utils.evalMathExpr(tf.getValue());
                        tf.setValue(newVal);
                        formulaSubstratesArrayParametersMap.get(par).get(sp).setValue(newVal);
                    } catch (Exception e) {
                        tf.setValue("");
                        formulaSubstratesArrayParametersMap.get(par).get(sp).setValue(null);
                    }
                });
                if (formulaSubstratesArrayParametersMap.get(par).get(sp).getValue() != null)
                    tf.setValue(formulaSubstratesArrayParametersMap.get(par).get(sp).getValue());
                return tf;
            }
            )).setHeader(sp).setResizable(true);
        }
        grid.setItems(formulaSubstratesArrayParametersMap.keySet());
        vl.add(ToolboxVaadin.getCaption("Parameters dependent on substrates"), grid);
        return vl;
    }

    // CONSTANTS BY MODIFIERS

    private VerticalLayout getConstantsByModifiersVL() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(false);
        vl.setPadding(false);

        Grid<String> grid = new Grid<>();
        grid.setWidthFull();
        grid.setHeight("200px");
        grid.addColumn(String::toString).setHeader("Parameter").setResizable(true);
        SortedMap<String, FormulaArrayValue> firstMap = null;
        for (SortedMap<String, FormulaArrayValue> spMap : formulaModifiersArrayParametersMap.values()) {
            firstMap = spMap;
            break;
        }
        for (String sp : firstMap.keySet()) {
            grid.addColumn(new ComponentRenderer<>(par -> {
                TextField tf = new TextField();
                tf.setWidthFull();
                tf.setPlaceholder("Number");
                tf.setValueChangeMode(ValueChangeMode.ON_BLUR);
                tf.addValueChangeListener(ev -> {
                    try {
                        String newVal = Utils.evalMathExpr(tf.getValue());
                        tf.setValue(newVal);
                        formulaModifiersArrayParametersMap.get(par).get(sp).setValue(newVal);
                    } catch (Exception e) {
                        tf.setValue("");
                        formulaModifiersArrayParametersMap.get(par).get(sp).setValue(null);
                    }
                });
                if (formulaModifiersArrayParametersMap.get(par).get(sp).getValue() != null)
                    tf.setValue(formulaModifiersArrayParametersMap.get(par).get(sp).getValue());
                return tf;
            }
            )).setHeader(sp).setResizable(true);
        }
        grid.setItems(formulaModifiersArrayParametersMap.keySet());
        vl.add(ToolboxVaadin.getCaption("Parameters dependent on modifiers"), grid);
        return vl;
    }
    //////////////////////////////////////////////////

    private void resetAndLoadAllParametersMaps() {
        Formula selectedFormula = formulaSelect.getValue();
        //reset par values maps
        parameterMap.clear();
        for (SortedMap.Entry<String, SortedMap<String, FormulaArrayValue>> entry : formulaSubstratesArrayParametersMap.entrySet())
            entry.getValue().clear();
        formulaSubstratesArrayParametersMap.clear();
        for (SortedMap.Entry<String, SortedMap<String, FormulaArrayValue>> entry : formulaModifiersArrayParametersMap.entrySet())
            entry.getValue().clear();
        formulaModifiersArrayParametersMap.clear();
        if (selectedFormula == null)
            return;
        //fill maps with empty values
        for (FormulaParameter fp : selectedFormula.getParameters().values()) {
            FormulaValue fv = new FormulaValue(fp.getName());
            if (fp.getForcedFormulaValueType() != null && fp.getForcedFormulaValueType() == FormulaValueType.CONSTANT)
                fv.setForceConstantToTrue();
            parameterMap.put(fp.getName(), fv);
        }
        formulaSubstratesArrayParametersMap
                .putAll(reaction.getGeneratedFormulaSubstrateArrayParametersForFormula(selectedFormula));
        formulaModifiersArrayParametersMap
                .putAll(reaction.getGeneratedFormulaModifierArrayParametersForFormula(selectedFormula));
        //try to load values
        if (selectedFormula == reaction.getFormula()) {
            SortedMap<String, FormulaValue> genParsLoadedFromReaction = reaction.getFormulaGeneralParameters();
            SortedMap<String, SortedMap<String, FormulaArrayValue>> subArrParsLoadedFromReaction = reaction.getFormulaSubstratesArrayParameters();
            SortedMap<String, SortedMap<String, FormulaArrayValue>> modArrLoadedFromReaction = reaction.getFormulaModifiersArrayParameters();
            for (SortedMap.Entry<String, FormulaValue> entry : genParsLoadedFromReaction.entrySet())
                if (parameterMap.containsKey(entry.getKey()) && entry.getValue() != null)
                    parameterMap.get(entry.getKey()).copyFrom(entry.getValue());
            for (SortedMap.Entry<String, SortedMap<String, FormulaArrayValue>> entry : subArrParsLoadedFromReaction.entrySet()) {
                if (formulaSubstratesArrayParametersMap.containsKey(entry.getKey()))
                    for (SortedMap.Entry<String, FormulaArrayValue> entry2 : entry.getValue().entrySet())
                        if (formulaSubstratesArrayParametersMap.get(entry.getKey()).containsKey(entry2.getKey()) && entry2.getValue() != null)
                            formulaSubstratesArrayParametersMap.get(entry.getKey()).get(entry2.getKey()).copyFrom(entry2.getValue());
            }
            for (SortedMap.Entry<String, SortedMap<String, FormulaArrayValue>> entry : modArrLoadedFromReaction.entrySet()) {
                if (formulaModifiersArrayParametersMap.containsKey(entry.getKey()))
                    for (SortedMap.Entry<String, FormulaArrayValue> entry2 : entry.getValue().entrySet())
                        if (formulaModifiersArrayParametersMap.get(entry.getKey()).containsKey(entry2.getKey()) && entry2.getValue() != null)
                            formulaModifiersArrayParametersMap.get(entry.getKey()).get(entry2.getKey()).copyFrom(entry2.getValue());
            }
        } else {
            // fill parameter values with default values
            for (FormulaValue fv : parameterMap.values()) {
                if (!fv.isForceConstant()) {
                    if (reaction.getLeftPartSpecies().containsKey(fv.getParameterName())) {
                        fv.setType(FormulaValueType.SUBSTRATE);
                        fv.setValue(fv.getParameterName());
                    } else if (reaction.getModifiers().containsKey(fv.getParameterName())) {
                        fv.setType(FormulaValueType.MODIFIER);
                        fv.setValue(fv.getParameterName());
                    }
                }
                if (!fv.isFilled()) {
                    fv.setType(FormulaValueType.CONSTANT);
                    fv.setValue(SharedData.defaultParameterValue);
                }
            }
            for (String par : formulaSubstratesArrayParametersMap.keySet())
                for (String sp : formulaSubstratesArrayParametersMap.get(par).keySet())
                    if (!formulaSubstratesArrayParametersMap.get(par).get(sp).isFilled())
                        formulaSubstratesArrayParametersMap.get(par).get(sp).setValue(SharedData.defaultParameterValue);
            for (String par : formulaModifiersArrayParametersMap.keySet())
                for (String sp : formulaModifiersArrayParametersMap.get(par).keySet())
                    if (!formulaModifiersArrayParametersMap.get(par).get(sp).isFilled())
                        formulaModifiersArrayParametersMap.get(par).get(sp).setValue(SharedData.defaultParameterValue);
        }
    }

    private HorizontalLayout getOkCancelButtonsHL() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.setSpacing(true);
        hl.setPadding(false);
        HorizontalLayout spacer = new HorizontalLayout();
        hl.add(spacer, getOkButton(), getCancelButton());
        hl.expand(spacer);
        return hl;
    }

    private Button getCancelButton() {
        Button button = new Button("Cancel");
        button.setWidth("150px");
        button.addClickListener(event -> {
            close();
        });
        return button;
    }

    private Button getOkButton() {
        Button button = new Button("Save");
        button.setWidth("150px");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(event -> {
            saveAndClose();
        });
        button.addClickShortcut(Key.ENTER);
        return button;
    }

    private void saveAndClose() {
        reaction.setFormula(formulaSelect.getValue());
        reaction.getFormulaGeneralParameters().clear();
        reaction.getFormulaSubstratesArrayParameters().clear();
        reaction.getFormulaModifiersArrayParameters().clear();
        reaction.getFormulaGeneralParameters().putAll(parameterMap);
        reaction.getFormulaSubstratesArrayParameters().putAll(formulaSubstratesArrayParametersMap);
        reaction.getFormulaModifiersArrayParameters().putAll(formulaModifiersArrayParametersMap);
        close();
    }
}
