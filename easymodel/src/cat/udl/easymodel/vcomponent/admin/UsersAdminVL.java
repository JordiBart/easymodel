package cat.udl.easymodel.vcomponent.admin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;

import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class UsersAdminVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	// External resources
	private SessionData sessionData = null;

	private VerticalLayout mainVL = null;
	private VerticalLayout optionButtonsVL = null;
	private VerticalLayout gridVL = null;
	private Table table;

	private ArrayList<HashMap<String, Object>> valuesToSave = new ArrayList<>();

	public UsersAdminVL() {
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
		table.addContainerProperty("New password", TextField.class, null);
		table.addContainerProperty("Admin", CheckBox.class, null);
		table.addContainerProperty("Delete", CheckBox.class, null);
		HashMap<String, Object> hm;
		valuesToSave.clear();
		for (User u : SharedData.getInstance().getDbManager().getAllUsers()) {
			table.addItem(new Object[] { getTF("name", "Name", u.getName(), valuesToSave.size()),
					getTF("password", "New password", "", valuesToSave.size()),
					getUserTypeCB(u.getUserType(), (u.getId() == sessionData.getUser().getId()), valuesToSave.size()),
					getDeleteCB(u.getId() == sessionData.getUser().getId(), valuesToSave.size()) }, u);
			hm = new HashMap<>();
			hm.put("id", u.getId());
			hm.put("name", u.getName());
			hm.put("password", "");
			hm.put("usertype", u.getUserType());
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
				if (name.equals("password")) {
					if (!newVal.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d\\.!$%@#£€*?&]{8,}$")) {
						((TextField) event.getSource()).setValue("");
						valuesToSave.get(arrIndex).put(name, "");
						Notification.show(
								"Password: Minimum eight characters, at least one letter and one number",
								Type.WARNING_MESSAGE);
					} else
						valuesToSave.get(arrIndex).put(name, newVal);
				} else if (name.equals("name")) {
					boolean found = false;
					if (!newVal.equals("")) {
						for (HashMap<String, Object> hm : valuesToSave) {
							if (hm.get("name").equals(newVal)) {
								found = true;
								break;
							}
						}
					}
					if (!found)
						valuesToSave.get(arrIndex).put(name, newVal);
					else {
						((TextField) event.getSource()).setValue("");
						valuesToSave.get(arrIndex).put(name, "");
						Notification.show("User name already exists!!", Type.WARNING_MESSAGE);
					}
				}
			}
		});
		return tf;
	}

	private CheckBox getUserTypeCB(UserType ut, Boolean readOnly, int arrIndex) {
		CheckBox cb = new CheckBox();
		cb.setWidth("50px");
		cb.setData("usertype");
		cb.setValue(ut == UserType.ADMIN);
		cb.setReadOnly(readOnly);
		cb.addBlurListener(new BlurListener() {
			@Override
			public void blur(com.vaadin.event.FieldEvents.BlurEvent event) {
				Boolean newVal = ((CheckBox) event.getSource()).getValue();
				if (newVal)
					valuesToSave.get(arrIndex).put("usertype", UserType.ADMIN);
				else
					valuesToSave.get(arrIndex).put("usertype", UserType.USER);
			}
		});
		return cb;
	}

	private CheckBox getDeleteCB(Boolean readOnly, int arrIndex) {
		CheckBox cb = new CheckBox();
		cb.setWidth("50px");
		cb.setData("delete");
		cb.setReadOnly(readOnly);
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
				if (hm.get("id") != null) {
					if (((String) hm.get("name")).equals("")) {
						res = false;
						break;
					}
				} else { // new entry
					if (((String) hm.get("name")).equals("") || ((String) hm.get("password")).equals("")) {
						res = false;
						break;
					}
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
				HashMap<String, Object> hm;
				table.addItem(new Object[] { getTF("name", "Name", "", valuesToSave.size()),
						getTF("password", "New password", "", valuesToSave.size()),
						getUserTypeCB(UserType.USER, false, valuesToSave.size()),
						getDeleteCB(false, valuesToSave.size()) }, null);
				table.markAsDirty();
				hm = new HashMap<>();
				hm.put("id", null);
				hm.put("name", "");
				hm.put("password", "");
				hm.put("usertype", UserType.USER);
				hm.put("delete", false);
				valuesToSave.add(hm);
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
						SharedData.getInstance().getDbManager().saveUsersAdmin(valuesToSave);
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
