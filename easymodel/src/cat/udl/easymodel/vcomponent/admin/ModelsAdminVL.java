package cat.udl.easymodel.vcomponent.admin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Models;
import cat.udl.easymodel.logic.model.ModelsImpl;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;

import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class ModelsAdminVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	// External resources
	private SessionData sessionData = null;

	private VerticalLayout mainVL = null;
	private VerticalLayout optionButtonsVL = null;
	private VerticalLayout gridVL = null;
	private Table table;

	private ArrayList<HashMap<String, Object>> valuesToSave = new ArrayList<>();

	public ModelsAdminVL() {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();

		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(true);

		optionButtonsVL = new VerticalLayout();
		optionButtonsVL.setMargin(false);
		HorizontalLayout hlOpt1 = new HorizontalLayout();
		hlOpt1.setWidth("100%");
		// hlOpt1.addComponent(getAddButton());
		hlOpt1.addComponent(getSaveButton());
		optionButtonsVL.addComponents(hlOpt1);

		mainVL = new VerticalLayout();
		mainVL.setSpacing(true);
		mainVL.setMargin(false);
		mainVL.setWidth("60%");
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
		table.addContainerProperty("Description", TextField.class, null);
		table.addContainerProperty("User", Label.class, null);
		table.addContainerProperty("Public", CheckBox.class, null);
		table.addContainerProperty("Delete", CheckBox.class, null);
		HashMap<String, Object> hm;
		valuesToSave.clear();
		Models models = new ModelsImpl();
		models.semiLoadDB();
		for (Model m : models) {
			table.addItem(
					new Object[] { getTF("name", "Name", m.getName(), valuesToSave.size()),
							getTF("description", "Description", m.getDescription(), valuesToSave.size()),
							new Label(m.getUser() != null ? m.getUser().getName() : ""),
							getPublicCB(m.getRepositoryType(), valuesToSave.size()), getDeleteCB(valuesToSave.size()) },
					m);
			hm = new HashMap<>();
			hm.put("id", m.getId());
			hm.put("name", m.getName());
			hm.put("description", m.getDescription());
			hm.put("repositorytype", m.getRepositoryType());
			hm.put("delete", false);
			valuesToSave.add(hm);
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

	private CheckBox getPublicCB(RepositoryType rt, int arrIndex) {
		CheckBox cb = new CheckBox();
		cb.setWidth("50px");
		cb.setData("repositorytype");
		cb.setValue(rt == RepositoryType.PUBLIC);
		cb.addBlurListener(new BlurListener() {
			@Override
			public void blur(com.vaadin.event.FieldEvents.BlurEvent event) {
				Boolean newVal = ((CheckBox) event.getSource()).getValue();
				if (newVal)
					valuesToSave.get(arrIndex).put("repositorytype", RepositoryType.PUBLIC);
				else
					valuesToSave.get(arrIndex).put("repositorytype", RepositoryType.PRIVATE);
			}
		});
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
				if (((String) hm.get("name")).equals("")) {
					res = false;
					break;
				}
			}
		}
		return res;
	}

	// private Button getAddButton() {
	// Button button = new Button("Add");
	// button.setWidth("100%");
	// button.setId("addButton");
	// button.addClickListener(new Button.ClickListener() {
	// private static final long serialVersionUID = 1L;
	//
	// public void buttonClick(ClickEvent event) {
	// HashMap<String, Object> hm;
	// table.addItem(new Object[] { getTF("name", "Name", "", valuesToSave.size()),
	// getTF("password", "New password", "", valuesToSave.size()),
	// getPublicCB(Rep.USER, false, valuesToSave.size()),
	// getDeleteCB(false, valuesToSave.size()) }, null);
	// table.markAsDirty();
	// hm = new HashMap<>();
	// hm.put("id", null);
	// hm.put("name", "");
	// hm.put("password", "");
	// hm.put("repositorytype", repositorytype.USER);
	// hm.put("delete", false);
	// valuesToSave.add(hm);
	// }
	// });
	// return button;
	// }

	private Button getSaveButton() {
		Button button = new Button("Save changes");
		button.setWidth("100%");
		button.setId("saveButton");
		button.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				if (checkFields()) {
					try {
						SharedData.getInstance().getDbManager().saveModelsAdmin(valuesToSave);
						Notification.show("Data saved", Type.TRAY_NOTIFICATION);
						updateDisplayContent();
					} catch (SQLException e) {
						Notification.show("Couldn't save changes due to a DB problem", Type.WARNING_MESSAGE);
					}
				} else {
					Notification.show("Some fields are incorrect", Type.WARNING_MESSAGE);
				}
			}
		});
		return button;
	}

}
