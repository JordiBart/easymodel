package cat.udl.easymodel.vcomponent.formula.window;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.main.SessionData;

public class FormulaEditWindow extends Window {

	private Formula formula = null;
	private Model selModel = null;
	private VerticalLayout windowVL = null;
	private Panel windowPanel = null;
	private CheckBox cbOneReactiveOnly = null, cbNoProductives = null, cbOneModifierOnly = null;
	private TextField nameTF;
	private TextField definitionTF;
	private SessionData sessionData;
	private boolean isSaveAndClose = false;
	private boolean isNewFormula;

	public FormulaEditWindow(Formula formula) {
		super();

		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.selModel = this.sessionData.getSelectedModel();
		if (formula != null) {
			this.formula = formula;
			isNewFormula = false;
			this.setCaption("Edit Rate");
		} else {
			this.formula = new Formula(selModel.getFormulas().getNextFormulaNameByModelShortName(), "", FormulaType.MODEL, selModel);
			isNewFormula = true;
			this.setCaption("New Rate");
		}

		this.setData(new Boolean(false)); // for window close callback
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
		definitionTF.focus();
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		Panel mainPanel = new Panel();
		mainPanel.setSizeFull();
		mainPanel.setStyleName("withoutborder");
		VerticalLayout mainPanelVL = new VerticalLayout();
		mainPanelVL.setSpacing(false);
		mainPanelVL.setMargin(false);

		mainPanelVL.addComponents(getFormulaNameVL(), getFormulaDefVL(), getFormulaOptionsVL());
		mainPanel.setContent(mainPanelVL);

		windowVL.addComponent(mainPanel);
		windowVL.addComponent(getOkCancelButtonsHL());
		windowVL.setExpandRatio(mainPanel, 1.0f);
	}

	private Component getFormulaNameVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setWidth("100%");
		vl.setSpacing(true);
		vl.setMargin(false);
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		nameTF = new TextField();
		nameTF.setWidth("100%");
		nameTF.setCaption("Rate Name");
		nameTF.setValue(formula.getNameToShow());
		hl.addComponents(nameTF);
		vl.addComponent(hl);
		return vl;
	}

	private Component getFormulaDefVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setMargin(false);
		definitionTF = new TextField();
		definitionTF.setCaption("Rate Definition");
		definitionTF.setWidth("100%");
//		definitionTF.setImmediate(true);
		definitionTF.addValueChangeListener(new ValueChangeListener<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent<String> event) {
				String newVal = (String) event.getValue();
				setTextFieldStyle(newVal, definitionTF);
			}
		});
		definitionTF.setValueChangeMode(ValueChangeMode.EAGER);
		definitionTF.setValue(formula.getFormulaDef());
		vl.addComponent(definitionTF);
		return vl;
	}

	private void setTextFieldStyle(String formulaStr, TextField tf) {
		if (FormulaUtils.isBlank(formulaStr))
			tf.setStyleName("");
		else if (FormulaUtils.isValid(formulaStr))
			tf.setStyleName("greenBG");
		else
			tf.setStyleName("redBG");
	}

	private Component getFormulaOptionsVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(false);
		vl.setMargin(false);
		vl.setCaption("Rate Options");
		vl.addComponents(cbOneReactiveOnly, cbNoProductives, cbOneModifierOnly);
		return vl;
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

	private void checkData() throws Exception {
		if ("".equals(nameTF.getValue())) {
			throw new Exception("Name can't be empty");
		}
		if (!FormulaUtils.isValid(definitionTF.getValue())) {
			throw new Exception("Invalid Rate definition");
		}
	}

	private void saveAndClose() {
		formula.setName(nameTF.getValue());
		if (!definitionTF.getValue().equals(formula.getFormulaDef())) { // new def
			if (!"".equals(formula.getFormulaDef())) { // old value != ""
				sessionData.getModels().removeFormulaFromReactions(formula);
				Notification.show("WARNING: Rate " + formula.getNameToShow() + " has been unlinked from all reactions",
						Type.TRAY_NOTIFICATION);
			}
			formula.setFormulaDef(definitionTF.getValue());
		}
		formula.setOneSubstrateOnly(cbOneReactiveOnly.getValue());
		formula.setNoProducts(cbNoProductives.getValue());
		formula.setOneModifierOnly(cbOneModifierOnly.getValue());

		if (isNewFormula) {
			selModel.getFormulas().addFormula(formula);
		}
		this.setData(new Boolean(true)); // for window close callback
		close();
	}

	private Button getOkButton() {
		Button button = new Button("Ok");
		button.setId("okButton");
		button.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
					checkData();
					saveAndClose();
				} catch (Exception e) {
					Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
				}
			}
		});
		return button;
	}
}
