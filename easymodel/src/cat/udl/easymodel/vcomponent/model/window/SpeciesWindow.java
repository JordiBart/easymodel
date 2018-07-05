package cat.udl.easymodel.vcomponent.model.window;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

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

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Species;
import cat.udl.easymodel.logic.types.SpeciesVarTypeType;
import cat.udl.easymodel.utils.p;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class SpeciesWindow extends Window {
	private static final long serialVersionUID = 1L;

	private Model model = null;

	private Map<String, Species> speciesMap = new LinkedHashMap<>();

	private VerticalLayout windowVL = null;
	private boolean isSaveAndClose = false;

	public SpeciesWindow(Model model) {
		super();

		// Set vars
		this.model = model;

		this.setCaption("Species");
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

		// Load reaction species values
		speciesMap.clear();
//		speciesMap.putAll(model.getAllSpecies());
		for (String sp : model.getAllSpecies().keySet())
			speciesMap.put(sp, model.getAllSpecies().get(sp).getCopy());

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
		Boolean[] first = new Boolean[] { new Boolean(true) };
		for (String species : model.getAllSpecies().keySet())
			valuesPanelVL.addComponent(getSpeciesValueLine(species, first));

		Panel valuesPanel = new Panel();
		valuesPanel.setSizeFull();
		valuesPanel.setStyleName("withoutborder");
		valuesPanel.setContent(valuesPanelVL);

		windowVL.addComponent(valuesPanel);
		windowVL.addComponent(getOkCancelButtonsHL());
		windowVL.setExpandRatio(valuesPanel, 1.0f);
	}

	private HorizontalLayout getSpeciesValueLine(String species, Boolean[] first) {
		HorizontalLayout line = new HorizontalLayout();
		line.setWidth("100%");
		TextField speciesNameTF = new TextField();
		speciesNameTF.setWidth("100%");
		speciesNameTF.setValue(species);
		speciesNameTF.setReadOnly(true);
		speciesNameTF.setDescription("1. Insert initial value 2. Select time dependency");
		TextField speciesValueTF = new TextField();
		speciesValueTF.setWidth("100%");
		speciesValueTF.setInputPrompt("Initial concentration");
		speciesValueTF.setId(species);
		speciesValueTF.setImmediate(true);
		speciesValueTF.addBlurListener(getSpeciesValueTFBlurListener());
		NativeSelect typeSelect = getTypeNativeSelect(species);

		// Load previous value
		Species prevSp = speciesMap.get(species);
		if (prevSp != null) {
			if (prevSp.getConcentration() != null)
				speciesValueTF.setValue(prevSp.getConcentration());
			if (prevSp.getVarType() != null)
				typeSelect.select(prevSp.getVarType());
		}
		
		// vars only modifiers can't be time dependent
		if (!model.getAllSpeciesExceptModifiers().containsKey(species)) {
			typeSelect.select(SpeciesVarTypeType.INDEP);
			typeSelect.setEnabled(false);
		}

		line.addComponents(speciesNameTF, speciesValueTF, typeSelect);
		line.setExpandRatio(speciesNameTF, 2.0f);
		line.setExpandRatio(speciesValueTF, 2.0f);
		line.setExpandRatio(typeSelect, 2.0f);

		if (first[0].booleanValue()) {
			speciesValueTF.focus();
			first[0] = false;
		}
		return line;
	}

	private BlurListener getSpeciesValueTFBlurListener() {
		return new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(BlurEvent event) {
				String newText = ((TextField) event.getComponent()).getValue();
				String componentId = event.getComponent().getId();
				String newVal = newText;
				try {
					BigDecimal testBigDec = new BigDecimal(newVal);
				} catch (Exception e) {
					newVal = null;
					((TextField) event.getComponent()).setValue("");
				} finally {
					if (speciesMap.get(componentId) != null)
						speciesMap.get(componentId).setConcentration(newVal);
				}
			}
		};
	}

	private NativeSelect getTypeNativeSelect(String species) {
		NativeSelect ns = new NativeSelect();
		ns.setWidth("100%");
		ns.setNullSelectionAllowed(false);
		ns.setData(species);
		for (SpeciesVarTypeType varType : SpeciesVarTypeType.values()) {
			if (ns.addItem(varType) != null) {
				ns.setItemCaption(varType, varType.getString());
			}
		}
		if (ns.getItem(SpeciesVarTypeType.TIMEDEP) != null)
			ns.select(SpeciesVarTypeType.TIMEDEP);
		ns.setImmediate(true);
		ns.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				NativeSelect ns = (NativeSelect) event.getProperty();
				Species spC = speciesMap.get(ns.getData());
				if (spC != null)
					spC.setVarType((SpeciesVarTypeType) ns.getValue());
			}
		});

		return ns;
	}

	// private HorizontalLayout getModifierValueLine(String mod) {
	// HorizontalLayout line = new HorizontalLayout();
	// line.setWidth("100%");
	// TextField constantNameTF = new TextField();
	// constantNameTF.setWidth("100%");
	// constantNameTF.setValue(mod);
	// constantNameTF.setReadOnly(true);
	// TextField constantValueTF = new TextField();
	// constantValueTF.setWidth("100%");
	// constantValueTF.setId(mod);
	// constantValueTF.setImmediate(true);
	// constantValueTF.addBlurListener(getModifierValueTFBlurListener());
	//
	// // Load previous value
	// String prevValue = modifiersValuesMap.get(mod);
	// if (prevValue != null)
	// constantValueTF.setValue(prevValue.toString());
	//
	// line.addComponents(constantNameTF, constantValueTF);
	// line.setExpandRatio(constantNameTF, 1.0f);
	// line.setExpandRatio(constantValueTF, 5.0f);
	//
	// return line;
	// }

	// private BlurListener getModifierValueTFBlurListener() {
	// return new BlurListener() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void blur(BlurEvent event) {
	// String newText = ((TextField) event.getComponent()).getValue();
	// String componentId = event.getComponent().getId();
	// String newString = null;
	// try {
	// newString = String.valueOf(newText);
	// } catch (Exception e) {
	// newString = null;
	// ((TextField) event.getComponent()).setValue("");
	// } finally {
	// modifiersValuesMap.put(componentId, newString);
	// }
	// }
	// };
	// }

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

	private void saveAndClose() {
		model.getAllSpecies().clear();
		model.getAllSpecies().putAll(speciesMap);
		close();
	}

	private Button getOkButton() {
		Button okButton = new Button("Ok");
		okButton.setId("okButton");
		okButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				saveAndClose();
			}
		});
		return okButton;
	}
}
