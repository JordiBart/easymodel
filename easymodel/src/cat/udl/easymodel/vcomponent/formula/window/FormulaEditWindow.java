package cat.udl.easymodel.vcomponent.formula.window;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification.Type;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaImpl;
import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.p;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class FormulaEditWindow extends Window {

	private Formula formula = null;
	private VerticalLayout windowVL = null;
	private Panel windowPanel = null;
	private CheckBox cbOneReactiveOnly = null, cbNoProductives = null, cbOneModifierOnly = null;
	private TextField nameTF;
	private TextField definitionTF;
	private SessionData sessionData;
	private boolean isSaveAndClose =false;
	private boolean isNewFormula;

	public FormulaEditWindow(Formula formula) {
		super();

		this.sessionData = (SessionData) UI.getCurrent().getData();
		if (formula != null) {
			this.formula = formula;
			isNewFormula=false;
		}
		else {
			this.formula = new FormulaImpl("", "", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE);
			isNewFormula=true;
		}

		this.setData(new Boolean(false)); //for window close callback
		this.setCaption("Edit Rate");
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
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		Panel mainPanel = new Panel();
		mainPanel.setSizeFull();
		mainPanel.setStyleName("withoutborder");
		VerticalLayout mainPanelVL = new VerticalLayout();

		mainPanelVL.addComponents(getFormulaNameVL(), getFormulaDefVL(), getFormulaOptionsVL());
		mainPanel.setContent(mainPanelVL);

		windowVL.addComponent(mainPanel);
		windowVL.addComponent(getOkCancelButtonsHL());
		windowVL.setExpandRatio(mainPanel, 1.0f);
	}

	private Component getFormulaNameVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setWidth("100%");
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		nameTF = new TextField();
		nameTF.setWidth("100%");
		nameTF.setCaption("Rate Name (non-editable)");
		nameTF.setValue(formula.getNameToShow());
		nameTF.setReadOnly(true);
		if (isNewFormula || formula.getNameToShow().lastIndexOf(FormulaUtils.modelShortNameSeparator) == -1) {
			NativeSelect ns = new NativeSelect();
			ns.setWidth("100%");
			ns.setNullSelectionAllowed(false);
			ns.setCaption("Select rate related model");
			Model ms = sessionData.getSelectedModel();
			if (ns.addItem(ms) != null)
				ns.setItemCaption(ms, ms.getName());
			for (Model m : sessionData.getModels()) {
				if (m != sessionData.getSelectedModel())
					if (ns.addItem(m) != null)
						ns.setItemCaption(m, m.getName());
			}
			ns.addValueChangeListener(new ValueChangeListener() {

				@Override
				public void valueChange(ValueChangeEvent event) {
					Model newVal = (Model) event.getProperty().getValue();
					nameTF.setReadOnly(false);
					nameTF.setValue(sessionData.getCustomFormulas()
							.getNextFormulaNameByModelShortName(newVal.getNameShort()));
					nameTF.setReadOnly(true);
				}
			});
			ns.select(ms);
			hl.addComponents(ns);
		}
		hl.addComponents(nameTF);
		vl.addComponent(hl);
		return vl;
	}

	private Component getFormulaDefVL() {
		VerticalLayout vl = new VerticalLayout();
		definitionTF = new TextField();
		definitionTF.setCaption("Rate Definition");
		definitionTF.setWidth("100%");
//		definitionTF.setImmediate(true);
		definitionTF.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				String newVal = (String) event.getProperty().getValue();
				setTextFieldStyle(newVal, definitionTF);
			}
		});
		definitionTF.setTextChangeEventMode(TextChangeEventMode.EAGER);
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
		if (!definitionTF.getValue().equals(formula.getFormulaDef())) { //new def
			if (!"".equals(formula.getFormulaDef())) { // old value != ""
				sessionData.getModels().removeFormulaFromReactions(formula);
				Notification.show(
						"WARNING: Rate " + formula.getNameToShow() + " has been unlinked from all reactions",
						Type.TRAY_NOTIFICATION);
			}
			formula.setFormulaDef(definitionTF.getValue());
		}
		formula.setOneSubstrateOnly(cbOneReactiveOnly.getValue());
		formula.setNoProducts(cbNoProductives.getValue());
		formula.setOneModifierOnly(cbOneModifierOnly.getValue());
		
		if (isNewFormula) {
			sessionData.getCustomFormulas().addFormula(formula);
		}
		this.setData(new Boolean(true)); //for window close callback
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
