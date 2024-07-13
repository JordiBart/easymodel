package cat.udl.easymodel.views.modelbuilder.dialog;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.VaadinSession;

public class FormulaEditDialog extends Dialog {

	private Formula formula = null;
	private Model selModel = null;
	private VerticalLayout mainPanelVL;
	private Checkbox cbOneReactiveOnly = null, cbNoProductives = null, cbOneModifierOnly = null;
	private TextField nameTF;
	private TextField definitionTF;
	private SessionData sessionData;
	private boolean isSaveAndClose = false;
	private boolean isNewFormula;
	private boolean isUpdateAfterClose=false;

	public FormulaEditDialog(Formula formula) {
		super();

		this.sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
		this.selModel = this.sessionData.getSelectedModel();
		String title;
		if (formula != null) {
			this.formula = formula;
			isNewFormula = false;
			title="Edit Rate";
		} else {
			this.formula = new Formula(selModel.getFormulas().getNextFormulaNameByModelShortName(), "", FormulaType.MODEL, selModel);
			isNewFormula = true;
			title="New Rate";
		}

		this.setWidth("500px");
		this.setHeight("400px");
		this.setModal(true);
		this.setDraggable(true);
		this.setResizable(true);

		VerticalLayout windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setPadding(false);
		windowVL.setSizeFull();
		windowVL.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);

		mainPanelVL = new VerticalLayout();
		mainPanelVL.setSpacing(false);
		mainPanelVL.setPadding(false);
		mainPanelVL.setClassName("scroll");
		
		windowVL.add(ToolboxVaadin.getDialogHeader(this,title,null));
		windowVL.add(mainPanelVL);
		windowVL.add(getOkCancelButtonsHL());
		windowVL.expand(mainPanelVL);

		createCheckboxes();

		this.add(windowVL);
		displayWindowContent();
		definitionTF.focus();
	}

	private void displayWindowContent() {
		mainPanelVL.removeAll();
		mainPanelVL.add(getFormulaNameVL(), getFormulaDefVL(), getFormulaOptionsVL());
	}

	private Component getFormulaNameVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setWidth("100%");
		vl.setSpacing(true);
		vl.setPadding(false);
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		nameTF = new TextField();
		nameTF.setWidth("100%");
		nameTF.setTitle("Rate Name");
		nameTF.setValue(formula.getNameToShow());
		hl.add(nameTF);
		vl.add(hl);
		return vl;
	}

	private Component getFormulaDefVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setPadding(false);
		definitionTF = new TextField();
		definitionTF.setTitle("Rate Definition");
		definitionTF.setWidth("100%");
//		definitionTF.setImmediate(true);
		definitionTF.addValueChangeListener(event-> {
				String newVal = (String) event.getValue();
				setTextFieldStyle(newVal, definitionTF);
		});
		definitionTF.setValueChangeMode(ValueChangeMode.EAGER);
		definitionTF.setValue(formula.getFormulaDef());
		vl.add(definitionTF);
		return vl;
	}

	private void setTextFieldStyle(String formulaStr, TextField tf) {
		if (FormulaUtils.isBlank(formulaStr))
			tf.setClassName("");
		else if (FormulaUtils.isValid(formulaStr))
			tf.setClassName("greenBG");
		else
			tf.setClassName("redBG");
	}

	private Component getFormulaOptionsVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(false);
		vl.setPadding(false);
		vl.add(ToolboxVaadin.getCaption("Rate Options"));
		vl.add(cbOneReactiveOnly, cbNoProductives, cbOneModifierOnly);
		return vl;
	}

	private void createCheckboxes() {
		cbOneReactiveOnly = new Checkbox();
		cbOneReactiveOnly.setLabel("One substrate only");
		cbOneReactiveOnly.setValue(formula.isOneSubstrateOnly());

		cbNoProductives = new Checkbox();
		cbNoProductives.setLabel("No products");
		cbNoProductives.setValue(formula.isNoProducts());

		cbOneModifierOnly = new Checkbox();
		cbOneModifierOnly.setLabel("One modifier only");
		cbOneModifierOnly.setValue(formula.isOneModifierOnly());
	}

	private HorizontalLayout getOkCancelButtonsHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		HorizontalLayout spacer = new HorizontalLayout();
		spacer.setWidth("100%");
		hl.add(spacer, getOkButton(), getCancelButton());
		hl.expand(spacer);
		return hl;
	}

	private Button getCancelButton() {
		Button button = new Button("Cancel");
		button.setWidth("150px");
		button.addClickListener(event-> {
				close();
		});
		return button;
	}

	private void checkData() throws Exception {
		if ("".equals(nameTF.getValue())) {
			throw new Exception("Rate name can't be empty");
		}
		if (!FormulaUtils.isValid(definitionTF.getValue())) {
			throw new Exception("Invalid Rate definition");
		}
		Formula searchFormula = selModel.getFormulas().getFormulaByName(nameTF.getValue());
		if (searchFormula != null && searchFormula != formula) {
			throw new Exception("Rate name is already in use");
		}
	}

	private void saveAndClose() {
		formula.setName(nameTF.getValue());
		if (!definitionTF.getValue().equals(formula.getFormulaDef())) { // new def
			if (!"".equals(formula.getFormulaDef())) { // old value != ""
				sessionData.getModels().removeFormulaFromReactions(formula);
				Notification.show("Rate " + formula.getNameToShow() + " has been unlinked from all reactions");
			}
			formula.setFormulaDef(definitionTF.getValue());
		}
		formula.setOneSubstrateOnly(cbOneReactiveOnly.getValue());
		formula.setNoProducts(cbNoProductives.getValue());
		formula.setOneModifierOnly(cbOneModifierOnly.getValue());

		if (isNewFormula) {
			selModel.getFormulas().addFormula(formula);
		}
		isUpdateAfterClose=true;
		close();
	}

	private Button getOkButton() {
		Button button = new Button("Ok");
		button.setWidth("150px");
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.addClickListener(event-> {
				getOkButtonFunction();
		});
		button.addClickShortcut(Key.ENTER);
		return button;
	}
	
	private void getOkButtonFunction(){
		try {
			checkData();
			saveAndClose();
		} catch (Exception e) {
			ToolboxVaadin.showWarningNotification(e.getMessage());
		}
	}

	public boolean isUpdateAfterClose() {
		return isUpdateAfterClose;
	}
}
