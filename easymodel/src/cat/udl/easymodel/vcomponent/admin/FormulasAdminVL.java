package cat.udl.easymodel.vcomponent.admin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.formula.Formulas;
import cat.udl.easymodel.logic.formula.FormulasImpl;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;

import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class FormulasAdminVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	// External resources
	private SessionData sessionData = null;

	private VerticalLayout mainVL = null;
	private VerticalLayout optionButtonsVL = null;
	private VerticalLayout gridVL = null;
	private Table table;

	private ArrayList<HashMap<String, Object>> valuesToSave = new ArrayList<>();

	public FormulasAdminVL() {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();

		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(true);

		optionButtonsVL = new VerticalLayout();
		optionButtonsVL.setMargin(false);
		HorizontalLayout hlOpt1 = new HorizontalLayout();
		hlOpt1.setWidth("100%");
		hlOpt1.addComponent(getAddButton());
		hlOpt1.addComponent(getSaveButton());
		optionButtonsVL.addComponents(hlOpt1);

		mainVL = new VerticalLayout();
		mainVL.setSpacing(true);
		mainVL.setMargin(false);
		mainVL.setWidth("100%");
		mainVL.setHeight("100%");
		updateDisplayContent();

		this.addComponent(mainVL);
		this.setComponentAlignment(mainVL, Alignment.MIDDLE_CENTER);
	}

	private void updateDisplayContent() {
		mainVL.removeAllComponents();

		try {
			gridVL = getTableVL();
		} catch (SQLException e) {
			Notification.show(SharedData.dbError, Type.WARNING_MESSAGE);
		}
		mainVL.addComponent(gridVL);
		mainVL.addComponent(optionButtonsVL);
		mainVL.setExpandRatio(gridVL, 1.0f);
	}

	private VerticalLayout getTableVL() throws SQLException {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setSizeFull();
		table = new Table();
		table.setWidth("100%");
		table.setHeight("100%");
		table.addContainerProperty("Name", TextField.class, null);
		table.addContainerProperty("Formula", TextField.class, null);
		table.addContainerProperty("OneSubstrateOnly", CheckBox.class, null);
		table.addContainerProperty("NoProducts", CheckBox.class, null);
		table.addContainerProperty("OneModifierOnly", CheckBox.class, null);
		table.addContainerProperty("Predefined", CheckBox.class, null);
		table.addContainerProperty("User", Label.class, null);
		table.addContainerProperty("Public", CheckBox.class, null);
		table.addContainerProperty("Delete", CheckBox.class, null);
		HashMap<String, Object> hm;
		valuesToSave.clear();
		for (Formula f : SharedData.getInstance().getDbManager().getAllFormulas()) {
			hm = new HashMap<>();
			hm.put("id", f.getId());
			hm.put("name", f.getName());
			hm.put("formula", f.getFormulaDef());
			hm.put("onesubstrateonly", f.isOneSubstrateOnly());
			hm.put("noproducts", f.isNoProducts());
			hm.put("onemodifieronly", f.isOneModifierOnly());
			hm.put("formulatype", f.getFormulaType());
			hm.put("repositorytype", f.getRepositoryType());
			hm.put("delete", false);
			valuesToSave.add(hm);
			CheckBox publicCB = getPublicCB(f.getRepositoryType(), valuesToSave.size() - 1);
			table.addItem(new Object[] { getTF("name", "Name", f.getName(), valuesToSave.size() - 1),
					getTF("formula", "Formula", f.getFormulaDef(), valuesToSave.size() - 1),
					getCB("onesubstrateonly", f.isOneSubstrateOnly(), valuesToSave.size() - 1),
					getCB("noproducts", f.isNoProducts(), valuesToSave.size() - 1),
					getCB("onemodifieronly", f.isOneModifierOnly(), valuesToSave.size() - 1),
					getPredefinedCB(f.getFormulaType(), publicCB, valuesToSave.size() - 1),
					new Label(f.getUser() != null ? f.getUser().getName() : ""), publicCB,
					getDeleteCB(valuesToSave.size() - 1) }, f);
		}
		vl.addComponent(table);
		return vl;
	}

	private TextField getTF(String name, String prompt, String val, int arrIndex) {
		TextField tf = new TextField();
		tf.setWidth("100%");
		tf.setData(name);
		tf.setInputPrompt(prompt);
		tf.setValue(val);
		tf.addBlurListener(new BlurListener() {
			@Override
			public void blur(com.vaadin.event.FieldEvents.BlurEvent event) {
				String newVal = ((TextField) event.getSource()).getValue();
				valuesToSave.get(arrIndex).put(name, newVal);
			}
		});
		return tf;
	}

	private CheckBox getCB(String name, boolean val, int arrIndex) {
		CheckBox cb = new CheckBox();
		cb.setWidth("50px");
		cb.setData(name);
		cb.setValue(val);
		cb.addBlurListener(new BlurListener() {
			@Override
			public void blur(com.vaadin.event.FieldEvents.BlurEvent event) {
				Boolean newVal = ((CheckBox) event.getSource()).getValue();
				valuesToSave.get(arrIndex).put(name, newVal);
			}
		});
		return cb;
	}

	private CheckBox getPredefinedCB(FormulaType ft, CheckBox publicCB, int arrIndex) {
		CheckBox cb = new CheckBox();
		cb.setWidth("50px");
		cb.setData("formulatype");
		cb.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Boolean newVal = (Boolean) event.getProperty().getValue();
				if (newVal) {
					valuesToSave.get(arrIndex).put("formulatype", FormulaType.PREDEFINED);
					publicCB.setValue(true);
					publicCB.setReadOnly(true);
				} else {
					valuesToSave.get(arrIndex).put("formulatype", FormulaType.CUSTOM);
					publicCB.setReadOnly(false);
				}
			}
		});
		cb.setValue(ft == FormulaType.PREDEFINED);
		return cb;
	}

	private CheckBox getPublicCB(RepositoryType rt, int arrIndex) {
		CheckBox cb = new CheckBox();
		cb.setWidth("50px");
		cb.setData("repositorytype");
		cb.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Boolean newVal = (Boolean) event.getProperty().getValue();
				if (newVal)
					valuesToSave.get(arrIndex).put("repositorytype", RepositoryType.PUBLIC);
				else
					valuesToSave.get(arrIndex).put("repositorytype", RepositoryType.PRIVATE);
			}
		});
		cb.setValue(rt == RepositoryType.PUBLIC);
		return cb;
	}

	private CheckBox getDeleteCB(int arrIndex) {
		CheckBox cb = new CheckBox();
		cb.setWidth("50px");
		cb.setData("delete");
		cb.addBlurListener(new BlurListener() {
			@Override
			public void blur(com.vaadin.event.FieldEvents.BlurEvent event) {
				Boolean newVal = ((CheckBox) event.getSource()).getValue();
				valuesToSave.get(arrIndex).put("delete", newVal);
			}
		});
		return cb;
	}

	private boolean checkFields() {
		boolean res = true;
		for (int i = 0; i < valuesToSave.size(); i++) {
			HashMap<String, Object> hm = valuesToSave.get(i);
			if (!((Boolean) hm.get("delete"))) {
				if (FormulaUtils.isBlank((String) hm.get("formula"))
						|| !FormulaUtils.isValid((String) hm.get("formula"))) {
					res = false;
					break;
				}
			}
		}
		return res;
	}

	private Button getAddButton() {
		Button button = new Button("Add");
		button.setWidth("100%");
		button.setId("addButton");
		button.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				HashMap<String, Object> hm = new HashMap<>();
				hm.put("id", null);
				hm.put("name", "");
				hm.put("formula", "");
				hm.put("onesubstrateonly", false);
				hm.put("noproducts", false);
				hm.put("onemodifieronly", false);
				hm.put("formulatype", FormulaType.PREDEFINED);
				hm.put("repositorytype", RepositoryType.PUBLIC);
				hm.put("delete", false);
				valuesToSave.add(hm);
				CheckBox publicCB = getPublicCB(RepositoryType.PUBLIC, valuesToSave.size() - 1);
				table.addItem(new Object[] { getTF("name", "Name", "", valuesToSave.size() - 1),
						getTF("formula", "Formula", "", valuesToSave.size() - 1),
						getCB("onesubstrateonly", false, valuesToSave.size() - 1),
						getCB("noproducts", false, valuesToSave.size() - 1),
						getCB("onemodifieronly", false, valuesToSave.size() - 1),
						getPredefinedCB(FormulaType.PREDEFINED, publicCB, valuesToSave.size() - 1),
						new Label(sessionData.getUser().getName()),
						publicCB,
						getDeleteCB(valuesToSave.size() - 1) }, null);
				table.markAsDirty();
			}
		});
		return button;
	}

	private Button getSaveButton() {
		Button button = new Button("Save changes");
		button.setWidth("100%");
		button.setId("saveButton");
		button.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				if (checkFields()) {
					try {
						SharedData.getInstance().getDbManager().saveFormulasAdmin(valuesToSave);
						Notification.show("Data saved", Type.TRAY_NOTIFICATION);
						updateDisplayContent();
					} catch (SQLException e) {
						Notification.show("Couldn't save changes due to a DB problem", Type.WARNING_MESSAGE);
					}
				} else {
					Notification.show("Some fields are incorrect\n(incorrect Mathematica syntax?)",
							Type.WARNING_MESSAGE);
				}
			}
		});
		return button;
	}

}
