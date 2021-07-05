package cat.udl.easymodel.vcomponent.formula.window;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import cat.udl.easymodel.logic.formula.Formula;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class FormulaRestrictionWindowOLD extends Window {

	private Formula formula = null;
	private VerticalLayout windowVL = null;
	private Panel windowPanel = null;
	private CheckBox cbOneReactiveOnly = null, cbNoProductives = null, cbOneModifierOnly = null;
	private boolean isSaveAndClose = false;

	public FormulaRestrictionWindowOLD(Formula formula) {
		super();

		// Set vars
		this.formula = formula;

		this.setCaption("Formula restrictions");
		this.setClosable(true);
		this.setWidth("500px");
		this.setHeight("400px");
		this.setModal(true);
		this.center();

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		createCheckBoxes();
		
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

		Panel valuesPanel = new Panel();
		valuesPanel.setSizeFull();
		valuesPanel.setStyleName("withoutborder");
		VerticalLayout valuesPanelVL = new VerticalLayout();
		
		valuesPanelVL.addComponents(cbOneReactiveOnly, cbNoProductives, cbOneModifierOnly);
		valuesPanel.setContent(valuesPanelVL);

		VerticalLayout infoVL = new VerticalLayout(new Label("Formula name: " + formula.getNameToShow()), new Label(
				"Formula: " + formula.getFormulaDef()));

		windowVL.addComponent(infoVL);
		windowVL.addComponent(valuesPanel);
		windowVL.addComponent(getOkCancelButtonsHL());
		windowVL.setExpandRatio(valuesPanel, 1.0f);
	}

	private void createCheckBoxes() {
		cbOneReactiveOnly = new CheckBox();
		cbOneReactiveOnly.setCaption("One substrate only");
		cbOneReactiveOnly.setValue(formula.isOneSubstrateOnly());

		cbNoProductives = new CheckBox();
		cbNoProductives.setCaption("No products");
		cbNoProductives.setValue(formula.isNoProducts());

		cbOneModifierOnly = new CheckBox();
		cbOneModifierOnly.setCaption("One modifier only");
		cbOneModifierOnly.setValue(formula.isOneModifierOnly());
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

	private void saveAndClose() {
		formula.setOneSubstrateOnly(cbOneReactiveOnly.getValue());
		formula.setNoProducts(cbNoProductives.getValue());
		formula.setOneModifierOnly(cbOneModifierOnly.getValue());
		
		close();
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
}
