package cat.udl.easymodel.vcomponent.admin;

import java.sql.SQLException;
import java.util.ArrayList;

import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.CException;

public class FormulasAdminVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	// External resources
	private SessionData sessionData = null;

	private VerticalLayout mainVL = null;
	private VerticalLayout optionButtonsVL = null;
	private VerticalLayout gridVL = null;
	private Grid<Formula> grid;

	private ArrayList<Formula> loadedFormulas = null;

	public FormulasAdminVL() {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();

		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(true);

		optionButtonsVL = new VerticalLayout();
		optionButtonsVL.setMargin(false);
		HorizontalLayout hlOpt1 = new HorizontalLayout();
		hlOpt1.setSpacing(false);
		hlOpt1.setMargin(false);
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
			loadedFormulas = SharedData.getInstance().getDbManager().getAllFormulas();
			gridVL = getGridVL();
		} catch (SQLException e) {
			Notification.show(SharedData.dbError, Type.WARNING_MESSAGE);
		}
		mainVL.addComponent(gridVL);
		mainVL.addComponent(optionButtonsVL);
		mainVL.setExpandRatio(gridVL, 1.0f);
	}

	private VerticalLayout getGridVL() throws SQLException {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setMargin(false);
		vl.setSizeFull();
		grid = new Grid<Formula>();
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setSizeFull();
		grid.setItems(loadedFormulas);
		grid.addComponentColumn(f -> {
			return getTF("name", "Name", f.getNameRaw(), f);
		}).setCaption("Name");
		grid.addComponentColumn(f -> {
			return getTF("formula", "Formula", f.getFormulaDef(), f);
		}).setCaption("Formula");
		grid.addComponentColumn(f -> {
			return getCB("onesubstrateonly", f.isOneSubstrateOnly(), f);
		}).setCaption("OneSubstrateOnly");
		grid.addComponentColumn(f -> {
			return getCB("noproducts", f.isNoProducts(), f);
		}).setCaption("NoProducts");
		grid.addComponentColumn(f -> {
			return getCB("onemodifieronly", f.isOneModifierOnly(), f);
		}).setCaption("OneModifierOnly");
		grid.addComponentColumn(f -> {
			return getCB("formulatype", (f.getFormulaType() == FormulaType.PREDEFINED), f);
		}).setCaption("Predefined");
		grid.addComponentColumn(f -> {
			return getCB("delete", f.isDBDelete(), f);
		}).setCaption("Delete");
		vl.addComponent(grid);
		return vl;
	}

	private TextField getTF(String field, String prompt, String val, Formula f) {
		TextField tf = new TextField();
		tf.setWidth("100%");
		tf.setData(field);
		tf.setPlaceholder(prompt);
		tf.setValue(val);
		tf.addBlurListener(new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(com.vaadin.event.FieldEvents.BlurEvent event) {
				String newVal = ((TextField) event.getSource()).getValue();
				if (field.equals("name"))
					f.setName(newVal);
				else if (field.equals("formula"))
					f.setFormulaDef(newVal);
			}
		});
		return tf;
	}

	private CheckBox getCB(String field, boolean val, Formula f) {
		CheckBox cb = new CheckBox();
		cb.setWidth("50px");
		cb.setData(field);
		cb.setValue(val);
		cb.addBlurListener(new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(com.vaadin.event.FieldEvents.BlurEvent event) {
				Boolean newVal = ((CheckBox) event.getSource()).getValue();
				if (field.equals("onesubstrateonly"))
					f.setOneSubstrateOnly(newVal);
				else if (field.equals("noproducts"))
					f.setNoProducts(newVal);
				else if (field.equals("onemodifieronly"))
					f.setOneModifierOnly(newVal);
				else if (field.equals("formulatype"))
					f.setFormulaType(newVal ? FormulaType.PREDEFINED : FormulaType.MODEL);
				else if (field.equals("delete"))
					f.setDBDelete(newVal);
			}
		});
		return cb;
	}

	private Button getAddButton() {
		Button button = new Button("Add");
		button.setWidth("100%");
		button.setId("addButton");
		button.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				Formula f = new Formula("", "", FormulaType.PREDEFINED, null);
				loadedFormulas.add(f);
				grid.setItems(loadedFormulas);
				grid.markAsDirty();
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
				try {
					for (Formula f : loadedFormulas) {
						if (!f.isDBDelete() && (!f.isValid() || f.isBlank())) {
							throw new CException(
									"Formula \"" + f.getNameRaw() + "\" contains errors\n(Incorrect Mathematica syntax?)");
						}
					}
					for (Formula f : loadedFormulas)
						f.saveDBAdmin();
					Notification.show("Data saved", Type.TRAY_NOTIFICATION);
					updateDisplayContent();
				} catch (SQLException e) {
					Notification.show("Database save error", Type.WARNING_MESSAGE);
				} catch (CException ce) {
					Notification.show(ce.getMessage(), Type.WARNING_MESSAGE);
				}
			}
		});
		return button;
	}

}
