package cat.udl.easymodel.vcomponent.model.window;

import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
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

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.model.FormulaValue;
import cat.udl.easymodel.logic.model.FormulaValueImpl;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class LinkReactionFormulaWindow extends Window {
	private static final long serialVersionUID = 1L;

	private SessionData sessionData = null;
	private SharedData sharedData = SharedData.getInstance();
	private Reaction reaction = null;
	private Formula selectedFormula = null;
	private boolean isSaveAndClose = false; // DON'T TRY TO REMOVE

	private VerticalLayout windowVL = null;
	private PopupView eyePopup;
	private PopupView infoPopup = new PopupView(null, getInfoLayout());
	private NativeSelect formulaSelect = null;
	private SortedMap<String, SortedMap<FormulaValueType, Component>> formulaValuesMap = new TreeMap<>();
	private SortedMap<String, SortedMap<String, String>> formulaSubstratesArrayParametersMap = new TreeMap<>();
	private SortedMap<String, SortedMap<String, String>> formulaModifiersArrayParametersMap = new TreeMap<>();

	public LinkReactionFormulaWindow(SessionData sessionData, Reaction reaction) {
		super();

		this.setData(1);

		// Set vars
		this.sessionData = sessionData;
		this.reaction = reaction;

		this.setCaption("Reaction Rate");
		this.setClosable(true);
		this.setWidth("500px");
		this.setHeight("500px");
		this.setModal(true);
		this.center();

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		formulaSelect = getFormulaSelect();

		// Load reaction formula
		if (reaction.getFormula() != null && formulaSelect.getItem(reaction.getFormula()) != null) {
			selectedFormula = reaction.getFormula();
			formulaSelect.select(selectedFormula);
			formulaSubstratesArrayParametersMap.putAll(reaction.getFormulaSubstratesArrayParameters());
			formulaModifiersArrayParametersMap.putAll(reaction.getFormulaModifiersArrayParameters());
		} else if (formulaSelect.getValue() != null) {
			resetConstantMaps((Formula) formulaSelect.getValue(), this.reaction);
		}

		this.addShortcutListener(new ShortcutListener("Shortcut enter", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {
				isSaveAndClose = true;
				focus();
			}
		});
		this.addFocusListener(new FocusListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void focus(FocusEvent event) {
				if (isSaveAndClose)
					saveAndClose();
			}
		});
		displayWindowContent();
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		VerticalLayout valuesPanelVL = new VerticalLayout();
		valuesPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		valuesPanelVL.addComponent(new Label("Generic parameter values"));
		valuesPanelVL.addComponent(getFormulaValuesVL());
		if (formulaSubstratesArrayParametersMap.size() != 0) {
			valuesPanelVL.addComponent(new Label("Numeric parameters by substrates"));
			valuesPanelVL.addComponent(getConstantsBySpeciesVL());
		}
		if (formulaModifiersArrayParametersMap.size() != 0) {
			valuesPanelVL.addComponent(new Label("Numeric parameters by modifiers"));
			valuesPanelVL.addComponent(getConstantsByModifiersVL());
		}

		Panel valuesPanel = new Panel();
		valuesPanel.setSizeFull();
		valuesPanel.setStyleName("withoutborder");
		valuesPanel.setContent(valuesPanelVL);

		VerticalLayout eyePopVL = new VerticalLayout();
		if (selectedFormula != null)
			eyePopVL.addComponent(new Label("Rate definition: " + selectedFormula.getFormulaDef()));
		eyePopVL.addComponent(
				new Label("Selected Reaction " + reaction.getIdJavaStr() + ": " + reaction.getReactionStr()));
		eyePopup = new PopupView(null, eyePopVL);

		HorizontalLayout headHL = new HorizontalLayout();
		headHL.setWidth("100%");
		headHL.addComponents(formulaSelect, eyePopup, getEyeButton(), infoPopup, getInfoButton());
		headHL.setExpandRatio(formulaSelect, 1f);

		windowVL.addComponent(headHL);
		windowVL.addComponent(valuesPanel);
		windowVL.addComponent(getOkCancelButtonsHL());
		windowVL.setExpandRatio(valuesPanel, 1.0f);

		for (SortedMap<FormulaValueType, Component> map : formulaValuesMap.values()) {
			if (((TextField) map.get(FormulaValueType.CONSTANT)).isEnabled()) {
				((TextField) map.get(FormulaValueType.CONSTANT)).focus();
				break;
			}
		}
	}

	private VerticalLayout getInfoLayout() {
		VerticalLayout vl = new VerticalLayout();
		vl.addComponent(new Label("Defining the Reaction Rate:"));
		vl.addComponent(new Label("1. Use the drop-down menu to select alternative rate functions"));
		vl.addComponent(new Label("2. Input the numerical values for the parameters"));
		vl.addComponent(new Label("3. In multi-substrate reactions assign each substrate to its corresponding symbol"));
		vl.addComponent(new Label("4. In multi-modifier reactions assign each modifier to its corresponding symbol"));
		return vl;
	}

	private NativeButton getEyeButton() {
		NativeButton btn = new NativeButton();
		btn.setDescription("Show Rate and Reaction definitions");
		btn.setHeight("36px");
		btn.setWidth("36px");
		btn.setStyleName("eyeBtn");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				eyePopup.setPopupVisible(true);
			}
		});
		return btn;
	}

	private NativeButton getInfoButton() {
		NativeButton btn = new NativeButton();
		btn.setDescription("How to define Reaction Rate");
		btn.setHeight("36px");
		btn.setWidth("36px");
		btn.setStyleName("infoNoBorderBtn");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				infoPopup.setPopupVisible(true);
			}
		});
		return btn;
	}

	// FORMULA VALUES

	private VerticalLayout getFormulaValuesVL() {
		VerticalLayout vl = new VerticalLayout();
		if (selectedFormula != null) {
			loadFormulaValuesMap();
			if (formulaValuesMap.size() > 0) {
				vl.addComponent(getFormulaValuesHeaderHL());
				for (String valueStr : formulaValuesMap.keySet()) {
					vl.addComponent(getFormulaValueLineHL(valueStr));
				}
			}
		}
		return vl;
	}

	private HorizontalLayout getFormulaValuesHeaderHL() {
		HorizontalLayout line = new HorizontalLayout();
		line.setWidth("100%");

		line.addComponents(new Label("Parameter"), new Label("Numeric"), new Label("Substrate"), new Label("Modifier"));
		return line;
	}

	private HorizontalLayout getFormulaValueLineHL(String parName) {
		HorizontalLayout line = new HorizontalLayout();
		line.setWidth("100%");
		TextField valueNameTF = new TextField();
		valueNameTF.setWidth("100%");
		valueNameTF.setHeight("37px");
		valueNameTF.setValue(parName);
		valueNameTF.setReadOnly(true);

		line.addComponent(valueNameTF);
		for (Component comp : formulaValuesMap.get(parName).values())
			line.addComponent(comp);

		// line.setExpandRatio(valueNameTF, 1.0f);

		return line;
	}

	// FORMULA VALUES MAP

	private void loadFormulaValuesMap() {
		formulaValuesMap.clear();
		for (String parName : selectedFormula.getGenericParameters().keySet()) {
			FormulaValueType parFVT = selectedFormula.getGenericParameters().get(parName);
			// prepare map values
			TextField constantValueTF = getFormulaValueConstantTF(parName, parFVT);
			constantValueTF.setHeight("37px");
			NativeSelect nsSubs = getFormulaValueSubstratesSelect(parName, parFVT);
			nsSubs.setHeight("37px");
			NativeSelect nsModifiers = getFormulaValueModifiersSelect(parName, parFVT);
			nsModifiers.setHeight("37px");

			SortedMap<FormulaValueType, Component> componentMap = new TreeMap<>();
			componentMap.put(FormulaValueType.CONSTANT, constantValueTF);
			componentMap.put(FormulaValueType.SUBSTRATE, nsSubs);
			componentMap.put(FormulaValueType.MODIFIER, nsModifiers);
			formulaValuesMap.put(parName, componentMap);
		}
		// load previous value
		if (selectedFormula == reaction.getFormula()) {
			for (String valueName : formulaValuesMap.keySet()) {
				FormulaValue prevValue = reaction.getFormulaValues().get(valueName);
				if (prevValue != null && prevValue.isFilled()) {
					switch (prevValue.getType()) {
					case CONSTANT:
						TextField constantTF = ((TextField) formulaValuesMap.get(valueName)
								.get(FormulaValueType.CONSTANT));
						constantTF.setValue(prevValue.getStringValue());
						getConstantTFBlurListenerFunction(constantTF);
						break;
					case SUBSTRATE:
						((NativeSelect) formulaValuesMap.get(valueName).get(FormulaValueType.SUBSTRATE))
								.select(prevValue.getSubstrateValue());
						break;
					case MODIFIER:
						((NativeSelect) formulaValuesMap.get(valueName).get(FormulaValueType.MODIFIER))
								.select(prevValue.getModifierValue());
						break;
					}
				}
			}
		} else { // formula is not yet selected, preload automatic values if
					// possible
			preLoadLoop: for (String valueName : formulaValuesMap.keySet()) {
				// for (Reaction r : model) {
				// if (r != reaction)
				// for (String rValue : r.getFormulaValues(model).keySet())
				// if (valueName.equals(rValue) &&
				// r.getFormulaValues(model).get(rValue).getType() == FormulaValueType.CONSTANT)
				// {
				// ((TextField)
				// formulaValuesMap.get(valueName).get(FormulaValueType.CONSTANT)).setValue(r.getFormulaValues(model).get(rValue).getStringValue());
				// continue preLoadLoop;
				// }
				// }
				if (reaction.getLeftPartSpecies().containsKey(valueName)
						&& ((NativeSelect) formulaValuesMap.get(valueName).get(FormulaValueType.SUBSTRATE)) != null)
					((NativeSelect) formulaValuesMap.get(valueName).get(FormulaValueType.SUBSTRATE)).select(valueName);
				else if (reaction.getModifiers().containsKey(valueName)
						&& ((NativeSelect) formulaValuesMap.get(valueName).get(FormulaValueType.MODIFIER)) != null)
					((NativeSelect) formulaValuesMap.get(valueName).get(FormulaValueType.MODIFIER)).select(valueName);
			}
		}
	}

	private TextField getFormulaValueConstantTF(String parName, FormulaValueType parFVT) {
		TextField tf = new TextField();
		tf.setWidth("100%");
		tf.setId("r:" + parName);
		tf.setImmediate(true);
		tf.addBlurListener(getConstantTFBlurListener());
		if (parFVT != null && parFVT != FormulaValueType.CONSTANT)
			tf.setEnabled(false);
		return tf;
	}

	private NativeSelect getFormulaValueSubstratesSelect(String parName, FormulaValueType parFVT) {
		NativeSelect ns = new NativeSelect();
		ns.setData(parName);
		ns.setWidth("100%");
		ns.setNullSelectionAllowed(true);
		loadFormulaValueSubstratesSelect(ns);
		ns.setImmediate(true);
		ns.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				NativeSelect eventComp = (NativeSelect) event.getProperty();
				String selectedValue = (String) eventComp.getValue();
				String eventParName = (String) eventComp.getData();
				for (String parName : formulaValuesMap.keySet()) {
					if (eventParName.equals(parName)) {
						// row where the event occurred
						if (parFVT == null) {
							if (selectedValue != null) {
								for (Component comp : formulaValuesMap.get(parName).values())
									if (eventComp != comp) {
										if (comp instanceof TextField)
											((TextField) comp).setValue("");
										if (comp instanceof NativeSelect)
											((NativeSelect) comp).select(null);
										comp.setEnabled(false);
									}
							} else {
								for (Component comp : formulaValuesMap.get(parName).values())
									if (eventComp != comp)
										comp.setEnabled(true);
							}
						}
					} else {
						// other rows
						for (FormulaValueType fvt : formulaValuesMap.get(parName).keySet()) {
							if (fvt == FormulaValueType.SUBSTRATE) {
								NativeSelect subSelect = (NativeSelect) formulaValuesMap.get(parName).get(fvt);
								// add missing values
								for (Object selectEventId : eventComp.getItemIds()) {
									if (!subSelect.containsId(selectEventId)) {
										if (subSelect.addItem(selectEventId) != null) {
											subSelect.setItemCaption(selectEventId, (String) selectEventId);
											break;
										}
									}
								}
								// remove selected value
								if (selectedValue != null) {
									subSelect.removeItem(selectedValue);
								}
							}
						}
					}
				}
			}
		});
		if (parFVT != null && parFVT != FormulaValueType.SUBSTRATE)
			ns.setEnabled(false);
		return ns;
	}

	private void loadFormulaValueSubstratesSelect(NativeSelect ns) {
		ns.removeAllItems();
		if (ns.addItem(-1) != null) {
			ns.setItemCaption(-1, "");
			ns.setNullSelectionItemId(-1);
		}
		for (String species : reaction.getLeftPartSpecies().keySet()) {
			if (ns.addItem(species) != null)
				ns.setItemCaption(species, species);
		}
		ns.setEnabled(true);
	}

	private NativeSelect getFormulaValueModifiersSelect(String parName, FormulaValueType parFVT) {
		NativeSelect ns = new NativeSelect();
		ns.setWidth("100%");
		ns.setData(parName);
		ns.setNullSelectionAllowed(true);
		loadFormulaValueModifiersSelect(ns);
		ns.setImmediate(true);
		ns.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				NativeSelect eventComp = (NativeSelect) event.getProperty();
				String selectedValue = (String) eventComp.getValue();
				String eventParName = (String) eventComp.getData();
				for (String parName : formulaValuesMap.keySet()) {
					if (eventParName.equals(parName)) {
						// row where the event occurred
						if (parFVT == null) {
							if (selectedValue != null) {
								for (Component comp : formulaValuesMap.get(parName).values())
									if (eventComp != comp) {
										if (comp instanceof TextField)
											((TextField) comp).setValue("");
										if (comp instanceof NativeSelect)
											((NativeSelect) comp).select(null);
										comp.setEnabled(false);
									}
							} else {
								for (Component comp : formulaValuesMap.get(parName).values())
									if (eventComp != comp)
										comp.setEnabled(true);
							}
						}
					} else {
						// other rows
						for (FormulaValueType fvt : formulaValuesMap.get(parName).keySet()) {
							if (fvt == FormulaValueType.MODIFIER) {
								NativeSelect modSelect = (NativeSelect) formulaValuesMap.get(parName).get(fvt);
								// add missing values
								for (Object selectEventId : eventComp.getItemIds()) {
									if (!modSelect.containsId(selectEventId)) {
										if (modSelect.addItem(selectEventId) != null) {
											modSelect.setItemCaption(selectEventId, (String) selectEventId);
											break;
										}
									}
								}
								// remove selected value
								if (selectedValue != null) {
									modSelect.removeItem(selectedValue);
								}
							}
						}
					}
				}
			}
		});
		if (parFVT != null && parFVT != FormulaValueType.MODIFIER)
			ns.setEnabled(false);
		return ns;
	}

	private void loadFormulaValueModifiersSelect(NativeSelect ns) {
		ns.removeAllItems();
		if (ns.addItem(-1) != null) {
			ns.setItemCaption(-1, "");
			ns.setNullSelectionItemId(-1);
		}
		for (String modifier : reaction.getModifiers().keySet()) {
			if (ns.addItem(modifier) != null) {
				ns.setItemCaption(modifier, modifier);
			}
		}
		ns.setEnabled(true);
	}

	// CONSTANTS BY SPECIES

	private VerticalLayout getConstantsBySpeciesVL() {
		VerticalLayout vl = new VerticalLayout();
		if (selectedFormula != null) {
			for (String constantStr : selectedFormula.getParametersBySubsAndModif()) {
				vl.addComponent(getConstantBySpeciesLineHL(constantStr));
			}
		}
		return vl;
	}

	private HorizontalLayout getConstantBySpeciesLineHL(String constantStr) {
		HorizontalLayout line = new HorizontalLayout();
		line.setWidth("100%");
		HorizontalLayout subLine = new HorizontalLayout();
		subLine.setWidth("100%");
		TextField constantNameTF = new TextField();
		constantNameTF.setWidth("100%");
		constantNameTF.setValue(constantStr);
		constantNameTF.setReadOnly(true);
		for (String speciesStr : reaction.getLeftPartSpecies().keySet()) {
			TextField constantValueTF = new TextField();
			constantValueTF.setWidth("100%");
			constantValueTF.setInputPrompt(speciesStr);
			constantValueTF.setId("s:" + constantStr + "-" + speciesStr);
			constantValueTF.setImmediate(true);
			constantValueTF.addBlurListener(getConstantTFBlurListener());

			// Load previous value
			String prevValue = null;
			if (formulaSubstratesArrayParametersMap.get(constantStr) != null)
				prevValue = formulaSubstratesArrayParametersMap.get(constantStr).get(speciesStr);
			if (prevValue != null)
				constantValueTF.setValue(prevValue.toString());

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
		if (selectedFormula != null) {
			for (String constantStr : selectedFormula.getParametersBySubsAndModif()) {
				vl.addComponent(getConstantByModifierLineHL(constantStr));
			}
		}
		return vl;
	}

	private HorizontalLayout getConstantByModifierLineHL(String constantStr) {
		HorizontalLayout line = new HorizontalLayout();
		line.setWidth("100%");
		HorizontalLayout subLine = new HorizontalLayout();
		subLine.setWidth("100%");
		TextField constantNameTF = new TextField();
		constantNameTF.setWidth("100%");
		constantNameTF.setValue(constantStr);
		constantNameTF.setReadOnly(true);
		for (String modStr : reaction.getModifiers().keySet()) {
			TextField constantValueTF = new TextField();
			constantValueTF.setWidth("100%");
			constantValueTF.setInputPrompt(modStr);
			constantValueTF.setId("m:" + constantStr + "-" + modStr);
			constantValueTF.setImmediate(true);
			constantValueTF.addBlurListener(getConstantTFBlurListener());

			// Load previous value
			String prevValue = null;
			if (formulaModifiersArrayParametersMap.get(constantStr) != null)
				prevValue = formulaModifiersArrayParametersMap.get(constantStr).get(modStr);
			if (prevValue != null)
				constantValueTF.setValue(prevValue.toString());

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
				getConstantTFBlurListenerFunction((TextField) event.getComponent());
			}
		};
	}

	private void getConstantTFBlurListenerFunction(TextField eventTF) {
		String newText = eventTF.getValue();
		String constantType = eventTF.getId().substring(0, 2);
		String constantId = eventTF.getId().substring(2);
		String newVal = newText;
		try {
			BigDecimal testBigDec = new BigDecimal(newVal);
		} catch (Exception e) {
			newVal = null;
			eventTF.setValue("");
		} finally {
			if (constantType.equals("r:")) {
				// constant by reaction
				FormulaValueType parFVT = selectedFormula.getGenericParameters().get(constantId);
				if (parFVT == null) {
					// disable other selects
					for (Component comp : formulaValuesMap.get(constantId).values()) {
						if (eventTF != comp) {
							comp.setEnabled(newVal == null);
						}
					}
				}
			} else if (constantType.equals("s:")) {
				// constant by species
				String constName = constantId.split("-")[0];
				String speciesName = constantId.split("-")[1];
				formulaSubstratesArrayParametersMap.get(constName).put(speciesName, newVal);
			} else if (constantType.equals("m:")) {
				// constant by modifier
				String constName = constantId.split("-")[0];
				String modName = constantId.split("-")[1];
				formulaModifiersArrayParametersMap.get(constName).put(modName, newVal);
			}
		}
	}

	private NativeSelect getFormulaSelect() {
		NativeSelect ns = new NativeSelect();
		ns.setDescription("Select Rate function");
		ns.setWidth("100%");
		ns.setNullSelectionAllowed(false);
		// Set custom and predefined formulas
		if (sessionData.getCustomFormulas() != null) {
			for (Formula f : sessionData.getCustomFormulas()) {
				if (f.parse() && f.isCompatibleWithReaction(reaction)) {
					if (ns.addItem(f) != null)
						ns.setItemCaption(f, f.getNameToShow());
					if (selectedFormula == null) {
						ns.select(f);
						selectedFormula = f;
					}
				}
			}
		}
		if (sessionData.getPredefinedFormulas() != null) {
			for (Formula f : sessionData.getPredefinedFormulas()) {
				if (f.parse() && f.isCompatibleWithReaction(reaction)) {
					if (ns.addItem(f) != null)
						ns.setItemCaption(f, f.getNameToShow());
					if (selectedFormula == null) {
						ns.select(f);
						selectedFormula = f;
					}
				}
			}
		}
		ns.setImmediate(true);
		ns.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				selectedFormula = (Formula) event.getProperty().getValue();
				if (selectedFormula == reaction.getFormula()) {
					formulaSubstratesArrayParametersMap.clear();
					formulaSubstratesArrayParametersMap.putAll(reaction.getFormulaSubstratesArrayParameters());
					formulaModifiersArrayParametersMap.clear();
					formulaModifiersArrayParametersMap.putAll(reaction.getFormulaModifiersArrayParameters());
				} else
					resetConstantMaps(selectedFormula, reaction);
				displayWindowContent();
			}
		});

		return ns;
	}

	private HorizontalLayout getOkCancelButtonsHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		VerticalLayout spacerVL = new VerticalLayout();
		spacerVL.setWidth("100%");
		hl.addComponents(spacerVL, getOkButton(), getCancelButton());
		hl.setExpandRatio(spacerVL, 1.0f);
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
		this.setData(0);
		if (formulaSelect.getValue() != null) {
			reaction.setFormula((Formula) formulaSelect.getValue());
			fillWithFormulaValuesMap(reaction.getFormulaValues());
			reaction.getFormulaSubstratesArrayParameters().putAll(formulaSubstratesArrayParametersMap);
			reaction.getFormulaModifiersArrayParameters().putAll(formulaModifiersArrayParametersMap);
		}
		close();
	}

	private void fillWithFormulaValuesMap(Map<String, FormulaValue> map) {
		// map.clear();
		for (String formulaValue : formulaValuesMap.keySet()) {
			TextField constantTF = ((TextField) formulaValuesMap.get(formulaValue).get(FormulaValueType.CONSTANT));
			NativeSelect speciesSelect = ((NativeSelect) formulaValuesMap.get(formulaValue)
					.get(FormulaValueType.SUBSTRATE));
			NativeSelect modifiersSelect = ((NativeSelect) formulaValuesMap.get(formulaValue)
					.get(FormulaValueType.MODIFIER));
			if (!"".equals(constantTF.getValue()))
				map.put(formulaValue, new FormulaValueImpl(FormulaValueType.CONSTANT, constantTF.getValue()));
			else if (speciesSelect.getValue() != null)
				map.put(formulaValue, new FormulaValueImpl(FormulaValueType.SUBSTRATE, speciesSelect.getValue()));
			else if (modifiersSelect.getValue() != null)
				map.put(formulaValue, new FormulaValueImpl(FormulaValueType.MODIFIER, modifiersSelect.getValue()));
			else
				map.put(formulaValue, new FormulaValueImpl());
		}
	}

	private void resetConstantMaps(Formula f, Reaction r) {
		formulaSubstratesArrayParametersMap.clear();
		formulaModifiersArrayParametersMap.clear();

		formulaSubstratesArrayParametersMap.putAll(r.getFormulaSubstratesArrayParametersForFormula(f));
		formulaModifiersArrayParametersMap.putAll(r.getFormulaModifiersArrayParametersForFormula(f));

		// for (String constantKey : f.getConstantsBySpecies()) {
		// Map<String, String> mapBySpecies = new LinkedHashMap<>();
		// for (String speciesKey : r.getParticipants().keySet())
		// mapBySpecies.put(speciesKey, null);
		// formulaSpeciesArrayParametersMap.put(constantKey, mapBySpecies);
		//
		// Map<String, String> mapByModifiers = new LinkedHashMap<>();
		// for (String modKey : r.getModifiers().keySet())
		// mapByModifiers.put(modKey, null);
		// formulaModifiersArrayParametersMap.put(constantKey, mapByModifiers);
		// }
	}
}
