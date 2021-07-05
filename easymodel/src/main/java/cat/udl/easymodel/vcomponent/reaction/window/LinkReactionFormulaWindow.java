package cat.udl.easymodel.vcomponent.reaction.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaArrayValue;
import cat.udl.easymodel.logic.formula.FormulaValue;
import cat.udl.easymodel.logic.formula.Formulas;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.common.InfoPopupButton;

public class LinkReactionFormulaWindow extends Window {
	private static final long serialVersionUID = 1L;

	private SessionData sessionData = null;
	private SharedData sharedData = SharedData.getInstance();
	private Reaction reaction = null;
	private Formulas compatibleFormulas = null;
	private boolean isSaveAndClose = false; // DON'T TRY TO REMOVE

	private VerticalLayout windowVL = new VerticalLayout();
	private VerticalLayout genParamPanelVL = new VerticalLayout();
	private InfoPopupButton ratePopupButton;

	private NativeSelect<Formula> formulaSelect = null;
	private SortedMap<String, FormulaValue> parameterMap = new TreeMap<String, FormulaValue>();
	private SortedMap<String, SortedMap<FormulaValueType, Component>> paramAndComponentRowMap = new TreeMap<>();
	private ArrayList<NativeSelect<String>> substrateSelects = new ArrayList<NativeSelect<String>>();
	private ArrayList<NativeSelect<String>> modifierSelects = new ArrayList<NativeSelect<String>>();
	private ArrayList<String> availableSubstrates = new ArrayList<>();
	private ArrayList<String> availableModifiers = new ArrayList<>();

	private SortedMap<String, SortedMap<String, FormulaArrayValue>> formulaSubstratesArrayParametersMap = new TreeMap<>();
	private SortedMap<String, SortedMap<String, FormulaArrayValue>> formulaModifiersArrayParametersMap = new TreeMap<>();

	private boolean disableSelectValueChange = false;

	public LinkReactionFormulaWindow(Reaction reaction, Formulas compatibleFormulas) {
		super();

		this.setData(false);

		// Set vars
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.reaction = reaction;
		this.compatibleFormulas = compatibleFormulas;

		this.setCaption("Rate -> Reaction (" + this.reaction.getIdJavaStr() + ")");
		this.setClosable(true);
		this.setWidth("500px");
		this.setHeight("500px");
		this.setModal(true);
		this.setResponsive(true);
		this.center();

		windowVL.setResponsive(true);
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		formulaSelect = getFormulaSelect();

		displayWindowOutline();
		// Load reaction formula
		if (reaction.getFormula() != null && ((Formulas) formulaSelect.getData()).contains(reaction.getFormula()))
			formulaSelect.setSelectedItem(reaction.getFormula());
		else
			formulaSelect.setSelectedItem(getFirstCompatibleFormula());

		this.addShortcutListener(new ShortcutListener("Shortcut enter", ShortcutAction.KeyCode.ENTER, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				isSaveAndClose = true;
				focus();
			}
		});
		this.addFocusListener(new FocusListener() {
			@Override
			public void focus(FocusEvent event) {
				if (isSaveAndClose) {
					isSaveAndClose = false;
					saveAndClose();
				}
			}
		});
//		p.p("dbg-end linkreaction create");
	}

	private Formula getFirstCompatibleFormula() {
		Formula r = null;
		for (Formula f : (Formulas) formulaSelect.getData()) {
			r = f;
			break;
		}
		return r;
	}

	private void displayWindowOutline() {
		Panel valuesPanel = new Panel();
		valuesPanel.setSizeFull();
		valuesPanel.setStyleName("withoutborder");
		valuesPanel.setContent(genParamPanelVL);

		HorizontalLayout headHL = new HorizontalLayout();
		headHL.setWidth("100%");
		ratePopupButton = getRatePopupButton();
		headHL.addComponents(formulaSelect, ratePopupButton, getInfoPopupButton());
		headHL.setExpandRatio(formulaSelect, 1f);

		windowVL.addComponent(headHL);
		windowVL.addComponent(valuesPanel);
		windowVL.addComponent(getOkCancelButtonsHL());
		windowVL.setExpandRatio(valuesPanel, 1.0f);
	}

	private void updateDisplayContent() {
		genParamPanelVL.removeAllComponents();
		if (formulaSelect.getValue() == null) {
			Label selectFormulaLbl = new Label("Please select a rate");
			genParamPanelVL.addComponent(selectFormulaLbl);
			genParamPanelVL.setComponentAlignment(selectFormulaLbl, Alignment.MIDDLE_CENTER);
		} else {
			genParamPanelVL.setSpacing(false);
			genParamPanelVL.setMargin(false);
			genParamPanelVL.setWidth("100%");
			genParamPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
//			genParamPanelVL.addComponent(new Label("Generic parameter values"));
			genParamPanelVL.addComponent(getFormulaParsVL());
			if (formulaSubstratesArrayParametersMap.size() != 0 && reaction.getLeftPartSpecies().size() > 0) {
				genParamPanelVL.addComponent(getConstantsBySubstratesVL());
			}
			if (formulaModifiersArrayParametersMap.size() != 0 && reaction.getModifiers().size() > 0) {
				genParamPanelVL.addComponent(getConstantsByModifiersVL());
			}

			for (SortedMap<FormulaValueType, Component> map : paramAndComponentRowMap.values()) {
				if (((TextField) map.get(FormulaValueType.CONSTANT)).isEnabled()) {
					((TextField) map.get(FormulaValueType.CONSTANT)).focus();
					break;
				}
			}
		}
//		p.p("dbg-end linkreaction display");
	}

	private InfoPopupButton getRatePopupButton() {
		InfoPopupButton infoPopupButton = new InfoPopupButton("Rate and Reaction definitions", "", 700, 100);
		infoPopupButton.getButton().setStyleName("notebookBtn");
		infoPopupButton.getButton().addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String content = "";
				if (formulaSelect.getValue() != null)
					content += "Selected Rate: " + formulaSelect.getValue().getFormulaDef() + "\n";
				content += "Selected Reaction: " + reaction.getReactionStr();
				infoPopupButton.getTextArea().setValue(content);
			}
		});
		return infoPopupButton;
	}

	private InfoPopupButton getInfoPopupButton() {
		return new InfoPopupButton("How to define Reaction Rate",
				"Defining the Reaction Rate:\n" + "1. Use the drop-down menu to select alternative rate functions\n"
						+ "2. Input the numerical values for the parameters\n"
						+ "3. In multi-substrate reactions assign each substrate to its corresponding symbol\n"
						+ "4. In multi-modifier reactions assign each modifier to its corresponding symbol",
				700, null);
	}

	// FORMULA VALUES

	private VerticalLayout getFormulaParsVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(false);
		vl.setMargin(false);
		if (formulaSelect.getValue() != null) {
			if (paramAndComponentRowMap.size() > 0) {
				vl.addComponent(getFormulaValuesHeaderHL());
				for (String valueStr : paramAndComponentRowMap.keySet()) {
					vl.addComponent(getFormulaValueLineHL(valueStr));
				}
			}
		}
		if (vl.getComponentCount() > 0)
			vl.setCaption("Generic parameter values");
		return vl;
	}

	private HorizontalLayout getFormulaValuesHeaderHL() {
		HorizontalLayout line = new HorizontalLayout();
		line.setSpacing(false);
		line.setMargin(false);
		line.setWidth("100%");

		line.addComponents(new Label("Parameter"), new Label("Numeric"), new Label("Substrate"), new Label("Modifier"));
		return line;
	}

	private HorizontalLayout getFormulaValueLineHL(String parName) {
		HorizontalLayout line = new HorizontalLayout();
		line.setSpacing(false);
		line.setMargin(false);
		line.setWidth("100%");
		TextField valueNameTF = new TextField();
		valueNameTF.setWidth("100%");
		valueNameTF.setHeight("37px");
		valueNameTF.setValue(parName);
		valueNameTF.setReadOnly(true);

		line.addComponent(valueNameTF);
		for (Component comp : paramAndComponentRowMap.get(parName).values())
			line.addComponent(comp);

		// line.setExpandRatio(valueNameTF, 1.0f);

		return line;
	}

	// FORMULA VALUES MAP

	private NativeSelect<Formula> getFormulaSelect() {
		NativeSelect<Formula> ns = new NativeSelect<>();
		ns.setResponsive(true);
		ns.setEmptySelectionAllowed(true);
		ns.setDescription("Select Rate function");
		ns.setWidth("100%");

		ns.setData(compatibleFormulas);
		ns.setItems(compatibleFormulas);
		ns.setItemCaptionGenerator(Formula::getNameToShow);

		ns.addValueChangeListener(new ValueChangeListener<Formula>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent<Formula> event) {
//				p.p(formulaSelect.getValue().getFormulaDef());
//				p.p(formulaSelect.getValue().getFormulaElementsString());
//				formulaSelect.getValue().transformFormulaToStandard();
				resetOrLoadAllParametersMaps();
				loadOrResetParamAndComponentRowMap();
				updateDisplayContent();
//				p.p("dbg-end change formula");
			}
		});

		return ns;
	}

	@SuppressWarnings("unchecked")
	private void loadOrResetParamAndComponentRowMap() {
		clearParamAndComponentRowMap();
		availableSubstrates.clear();
		availableModifiers.clear();
		substrateSelects.clear();
		modifierSelects.clear();
		Formula f = formulaSelect.getValue();
		if (f == null)
			return;
		for (String sp : reaction.getLeftPartSpecies().keySet())
			availableSubstrates.add(sp);
		for (String sp : reaction.getModifiers().keySet())
			availableModifiers.add(sp);
		for (String parName : f.getParameters().keySet()) {
			FormulaValueType parFVT = f.getParameters().get(parName).getForcedFormulaValueType();
			// prepare map values
			TextField constantValueTF = getFormulaValueConstantTF(parName, parFVT, "37px");
			NativeSelect<String> subsSelect = getFormulaParSubOrModSelect(parName, FormulaValueType.SUBSTRATE, parFVT,
					"37px");
			substrateSelects.add(subsSelect);
			NativeSelect<String> modifiersSelect = getFormulaParSubOrModSelect(parName, FormulaValueType.MODIFIER,
					parFVT, "37px");
			modifierSelects.add(modifiersSelect);

			SortedMap<FormulaValueType, Component> componentMap = new TreeMap<>();
			componentMap.put(FormulaValueType.CONSTANT, constantValueTF);
			componentMap.put(FormulaValueType.SUBSTRATE, subsSelect);
			componentMap.put(FormulaValueType.MODIFIER, modifiersSelect);
			paramAndComponentRowMap.put(parName, componentMap);
		}
//		if (true)
//			return;
		// load previous values
		if (f == reaction.getFormula()) {
			for (String par : paramAndComponentRowMap.keySet()) {
				FormulaValue prevValue = reaction.getFormulaGenPars().get(par);
				if (prevValue != null && prevValue.isFilled()) {
					switch (prevValue.getType()) {
					case CONSTANT:
						TextField constantTF = ((TextField) paramAndComponentRowMap.get(par)
								.get(FormulaValueType.CONSTANT));
						constantTF.setValue(prevValue.getStringValue());
						getConstantTFBlurListenerAction(constantTF);
						break;
					case SUBSTRATE:
						((NativeSelect<String>) paramAndComponentRowMap.get(par).get(FormulaValueType.SUBSTRATE))
								.setValue(prevValue.getSubstrateValue());
						break;
					case MODIFIER:
						((NativeSelect<String>) paramAndComponentRowMap.get(par).get(FormulaValueType.MODIFIER))
								.setValue(prevValue.getModifierValue());
						break;
					}
				}
			}
		}
		// preload missing values
		for (String par : paramAndComponentRowMap.keySet()) {
			if (parameterMap.get(par) == null || !parameterMap.get(par).isFilled()) {
				if (availableSubstrates.contains(par)) {
					((NativeSelect<String>) paramAndComponentRowMap.get(par).get(FormulaValueType.SUBSTRATE))
							.setValue(par);
				} else if (availableModifiers.contains(par)) {
					((NativeSelect<String>) paramAndComponentRowMap.get(par).get(FormulaValueType.MODIFIER))
							.setValue(par);
				} else {
					TextField constantTF = (TextField) paramAndComponentRowMap.get(par).get(FormulaValueType.CONSTANT);
					constantTF.setValue(SharedData.defaultParameterValue);
					getConstantTFBlurListenerAction(constantTF);
				}
			}
		}
	}

	private void clearParamAndComponentRowMap() {
		for (String key : paramAndComponentRowMap.keySet())
			paramAndComponentRowMap.get(key).clear();
		paramAndComponentRowMap.clear();
	}

	private TextField getFormulaValueConstantTF(String parName, FormulaValueType parFVT, String height) {
		TextField tf = new TextField();
		tf.setWidth("100%");
		tf.setHeight(height);
		tf.setId("r:" + parName);
		// tf.setResponsive(true);
		tf.addBlurListener(getConstantTFBlurListener());
		if (parFVT != null && parFVT != FormulaValueType.CONSTANT)
			tf.setEnabled(false);
		return tf;
	}

	private NativeSelect<String> getFormulaParSubOrModSelect(String parName, FormulaValueType sub_mod_ofSelect,
			FormulaValueType parFVT, String height) {
		NativeSelect<String> ns = new NativeSelect<>();
		ns.setData(parName);
		ns.setWidth("100%");
		ns.setHeight(height);
		ns.setEmptySelectionAllowed(true);
		if (sub_mod_ofSelect == FormulaValueType.SUBSTRATE)
			ns.setItems(getCopyOfArrayWithExtraItem(availableSubstrates, null));
		else if (sub_mod_ofSelect == FormulaValueType.MODIFIER)
			ns.setItems(getCopyOfArrayWithExtraItem(availableModifiers, null));
		// ns.setResponsive(true);
		ns.addValueChangeListener(new ValueChangeListener<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent<String> event) {
//				if (true)
//				return;
				if (disableSelectValueChange)
					return;
				String newVal = event.getValue();
				String oldVal = event.getOldValue();
//				p.p(parName + " set " + newVal + " from " + oldVal);
				ArrayList<String> availableList = availableSubstrates;
				ArrayList<NativeSelect<String>> selects = substrateSelects;
				if (sub_mod_ofSelect == FormulaValueType.MODIFIER) {
					availableList = availableModifiers;
					selects = modifierSelects;
				}
				if (newVal != null)
					availableList.remove(newVal);
				if (oldVal != null)
					availableList.add(oldVal);
				// update other selects
				disableSelectValueChange = true;
				for (NativeSelect<String> ns2 : selects) {
					if (ns2 != ns) {
						String ns2Val = ns2.getValue();
						ns2.setItems(getCopyOfArrayWithExtraItem(availableList, ns2Val));
						ns2.setValue(ns2Val);
					}
				}
				disableSelectValueChange = false;
				// enable/disable other components from the same row
				for (Component comp : paramAndComponentRowMap.get(parName).values())
					if (ns != comp)
						comp.setEnabled(newVal == null);
				// save value
				parameterMap.put(parName, new FormulaValue(parName, sub_mod_ofSelect, newVal));
//				p.p("set "+parName+" to "+ newVal);
			}
		});
		if (parFVT != null && parFVT != sub_mod_ofSelect)
			ns.setEnabled(false);
		return ns;
	}

	private ArrayList<String> getCopyOfArrayWithExtraItem(ArrayList<String> src, String addedItem) {
		ArrayList<String> res = new ArrayList<String>();
		res.addAll(src);
		if (addedItem != null)
			res.add(addedItem);
		Collections.sort(res);
//		p.p(res);
		return res;
	}

	// CONSTANTS BY SUBSTRATES

	private VerticalLayout getConstantsBySubstratesVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(false);
		vl.setMargin(false);
		if (formulaSelect.getValue() != null) {
			for (String constantStr : formulaSelect.getValue().getParametersBySubsAndModif()) {
				vl.addComponent(getConstantBySubstrateLineHL(constantStr));
			}
		}
		if (vl.getComponentCount() > 0)
			vl.setCaption("Substrate dependent numeric parameters");
		return vl;
	}

	private HorizontalLayout getConstantBySubstrateLineHL(String constantStr) {
		HorizontalLayout line = new HorizontalLayout();
		line.setSpacing(false);
		line.setMargin(false);
		line.setWidth("100%");
		HorizontalLayout subLine = new HorizontalLayout();
		subLine.setSpacing(false);
		subLine.setMargin(false);
		subLine.setWidth("100%");
		TextField constantNameTF = new TextField();
		constantNameTF.setWidth("100%");
		constantNameTF.setValue(constantStr);
		constantNameTF.setReadOnly(true);
		for (String subStr : reaction.getLeftPartSpecies().keySet()) {
			TextField constantValueTF = new TextField();
			constantValueTF.setWidth("100%");
			constantValueTF.setPlaceholder(subStr);
			constantValueTF.setDescription(subStr);
			constantValueTF.setId("s:" + constantStr + "-" + subStr);
			// constantValueTF.setResponsive(true);
			constantValueTF.addBlurListener(getConstantTFBlurListener());

			// Load previous value
			String prevValue = null;
			if (formulaSubstratesArrayParametersMap.get(constantStr) != null
					&& formulaSubstratesArrayParametersMap.get(constantStr).get(subStr) != null)
				prevValue = formulaSubstratesArrayParametersMap.get(constantStr).get(subStr).getValue();
			if (prevValue != null)
				constantValueTF.setValue(prevValue.toString());
			else {
				constantValueTF.setValue(SharedData.defaultParameterValue);
				getConstantTFBlurListenerAction(constantValueTF);
			}

			subLine.addComponent(constantValueTF);
		}
		line.addComponents(constantNameTF, subLine);
		line.setExpandRatio(constantNameTF, 1.0f);
		line.setExpandRatio(subLine, 5.0f);

		return line;
	}

	// CONSTANTS BY MODIFIERS

	private VerticalLayout getConstantsByModifiersVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(false);
		vl.setMargin(false);
		if (formulaSelect.getValue() != null) {
			for (String constantStr : formulaSelect.getValue().getParametersBySubsAndModif()) {
				vl.addComponent(getConstantByModifierLineHL(constantStr));
			}
		}
		if (vl.getComponentCount() > 0)
			vl.setCaption("Modifier dependent numeric parameters");
		return vl;
	}

	private HorizontalLayout getConstantByModifierLineHL(String constantStr) {
		HorizontalLayout line = new HorizontalLayout();
		line.setSpacing(false);
		line.setMargin(false);
		line.setWidth("100%");
		HorizontalLayout subLine = new HorizontalLayout();
		subLine.setSpacing(false);
		subLine.setMargin(false);
		subLine.setWidth("100%");
		TextField constantNameTF = new TextField();
		constantNameTF.setWidth("100%");
		constantNameTF.setValue(constantStr);
		constantNameTF.setReadOnly(true);
		for (String modStr : reaction.getModifiers().keySet()) {
			TextField constantValueTF = new TextField();
			constantValueTF.setWidth("100%");
			constantValueTF.setPlaceholder(modStr);
			constantValueTF.setDescription(modStr);
			constantValueTF.setId("m:" + constantStr + "-" + modStr);
			// constantValueTF.setResponsive(true);
			constantValueTF.addBlurListener(getConstantTFBlurListener());

			// Load previous value
			String prevValue = null;
			if (formulaModifiersArrayParametersMap.get(constantStr) != null
					&& formulaModifiersArrayParametersMap.get(constantStr).get(modStr) != null)
				prevValue = formulaModifiersArrayParametersMap.get(constantStr).get(modStr).getValue();
			if (prevValue != null)
				constantValueTF.setValue(prevValue.toString());
			else {
				constantValueTF.setValue(SharedData.defaultParameterValue);
				getConstantTFBlurListenerAction(constantValueTF);
			}

			subLine.addComponent(constantValueTF);
		}
		line.addComponents(constantNameTF, subLine);
		line.setExpandRatio(constantNameTF, 1.0f);
		line.setExpandRatio(subLine, 5.0f);

		return line;
	}

	private BlurListener getConstantTFBlurListener() {
		return new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(BlurEvent event) {
				getConstantTFBlurListenerAction((TextField) event.getComponent());
			}
		};
	}

	private void getConstantTFBlurListenerAction(TextField eventTF) {
		String newText = eventTF.getValue();
		String newVal = newText;
		String constantType = eventTF.getId().substring(0, 2);
		String constantIdOrPar = eventTF.getId().substring(2);
		try {
			newVal = Utils.evalMathExpr(newText);
			eventTF.setValue(newVal);
		} catch (Exception e) {
			newVal = null;
			eventTF.setValue("");
		}

		// save newVal
		if (constantType.equals("r:")) {
			// constant by reaction
			FormulaValueType parFVT = formulaSelect.getValue().getParameters().get(constantIdOrPar)
					.getForcedFormulaValueType();
			if (parFVT == null) {
				// disable other row components
				for (Component comp : paramAndComponentRowMap.get(constantIdOrPar).values())
					if (eventTF != comp)
						comp.setEnabled(newVal == null);
			}
			parameterMap.put(constantIdOrPar, new FormulaValue(constantIdOrPar, FormulaValueType.CONSTANT, newVal));
		} else if (constantType.equals("s:")) {
			// constant by species
			String constName = constantIdOrPar.split("-")[0];
			String speciesName = constantIdOrPar.split("-")[1];
			formulaSubstratesArrayParametersMap.get(constName).put(speciesName, new FormulaArrayValue(newVal));
		} else if (constantType.equals("m:")) {
			// constant by modifier
			String constName = constantIdOrPar.split("-")[0];
			String modName = constantIdOrPar.split("-")[1];
			formulaModifiersArrayParametersMap.get(constName).put(modName, new FormulaArrayValue(newVal));
		}
	}

	private void resetOrLoadAllParametersMaps() {
		parameterMap.clear();
		for (SortedMap.Entry<String, SortedMap<String,FormulaArrayValue>> entry : formulaSubstratesArrayParametersMap.entrySet())
			entry.getValue().clear();
		formulaSubstratesArrayParametersMap.clear();
		for (SortedMap.Entry<String, SortedMap<String,FormulaArrayValue>> entry : formulaModifiersArrayParametersMap.entrySet())
			entry.getValue().clear();
		formulaModifiersArrayParametersMap.clear();
		if (formulaSelect.getValue() == null)
			return;
		if (formulaSelect.getValue() == reaction.getFormula()) {
			// load pars vals
			SortedMap<String,FormulaValue> genPars = reaction.getFormulaGenPars();
			SortedMap<String,SortedMap<String,FormulaArrayValue>> subArrPars = reaction.getFormulaSubstratesArrayParameters();
			SortedMap<String,SortedMap<String,FormulaArrayValue>> modArrPars = reaction.getFormulaModifiersArrayParameters();
			for (SortedMap.Entry<String, FormulaValue> entry : genPars.entrySet())
				parameterMap.put(entry.getKey(), new FormulaValue(entry.getValue()));
			for (SortedMap.Entry<String, SortedMap<String,FormulaArrayValue>> entry : subArrPars.entrySet()) {
				formulaSubstratesArrayParametersMap.put(entry.getKey(), new TreeMap<String,FormulaArrayValue>());
				for (SortedMap.Entry<String,FormulaArrayValue> entry2 : entry.getValue().entrySet())
					formulaSubstratesArrayParametersMap.get(entry.getKey()).put(entry2.getKey(), new FormulaArrayValue(entry2.getValue()));
			}
			for (SortedMap.Entry<String, SortedMap<String,FormulaArrayValue>> entry : modArrPars.entrySet()) {
				formulaModifiersArrayParametersMap.put(entry.getKey(), new TreeMap<String,FormulaArrayValue>());
				for (SortedMap.Entry<String,FormulaArrayValue> entry2 : entry.getValue().entrySet())
					formulaModifiersArrayParametersMap.get(entry.getKey()).put(entry2.getKey(), new FormulaArrayValue(entry2.getValue()));
			}
		} else {
			// set array parameters
			formulaSubstratesArrayParametersMap
					.putAll(reaction.getGeneratedFormulaSubstrateArrayParametersForFormula(formulaSelect.getValue()));
			formulaModifiersArrayParametersMap
					.putAll(reaction.getGeneratedFormulaModifierArrayParametersForFormula(formulaSelect.getValue()));
		}
	}

	private HorizontalLayout getOkCancelButtonsHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		hl.setMargin(false);
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

	private Button getOkButton() {
		Button button = new Button("Ok");
		button.setId("okButton");
		button.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				saveAndClose();
			}
		});
		return button;
	}

	private void saveAndClose() {
		this.setData(true);
		reaction.setFormula((Formula) formulaSelect.getValue());
		reaction.getFormulaGenPars().clear();
		reaction.getFormulaSubstratesArrayParameters().clear();
		reaction.getFormulaModifiersArrayParameters().clear();
//		p.p("parameter map to save: ");
//		for (String par: parameterMap.keySet())
//			p.p("par "+(parameterMap.get(par)==null?"null":parameterMap.get(par).getStringValue()));
		reaction.getFormulaGenPars().putAll(parameterMap);
		reaction.getFormulaSubstratesArrayParameters().putAll(formulaSubstratesArrayParametersMap);
		reaction.getFormulaModifiersArrayParameters().putAll(formulaModifiersArrayParametersMap);
		close();
	}
}
