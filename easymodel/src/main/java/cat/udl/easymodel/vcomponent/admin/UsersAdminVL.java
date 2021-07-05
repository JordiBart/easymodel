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

import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.logic.user.Users;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.p;

public class UsersAdminVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	private SessionData sessionData = null;
	private SharedData sharedData = SharedData.getInstance();

	private VerticalLayout mainVL = null;
	private VerticalLayout optionButtonsVL = null;
	private VerticalLayout gridVL = null;
	private Grid<User> grid;

	private Users usersCopy = null;

	public UsersAdminVL() {
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

		usersCopy = new Users(sharedData.getUsers());
//		for (User u:usersCopy)
//			p.p(u.getName());
		gridVL = getGridVL();
		mainVL.addComponent(gridVL);
		mainVL.addComponent(optionButtonsVL);
		mainVL.setExpandRatio(gridVL, 1.0f);
	}

	private VerticalLayout getGridVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setMargin(false);
		vl.setSizeFull();
		grid = new Grid<User>();
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setSizeFull();
		grid.setItems(usersCopy);
		grid.addComponentColumn(u -> {
			return getTF("name", "Username", u.getName(), u);
		}).setCaption("Username");
		grid.addComponentColumn(u -> {
			return getTF("password", "New password", "", u);
		}).setCaption("Password");
		grid.addComponentColumn(u -> {
			return getCB("usertype", (u.getUserType() == UserType.ADMIN), u);
		}).setCaption("Admin");
		grid.addComponentColumn(u -> {
			return getCB("delete", u.isDBDelete(), u);
		}).setCaption("Delete");
		vl.addComponent(grid);
		return vl;
	}

	private TextField getTF(String field, String prompt, String val, User u) {
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
					u.setName(newVal);
				else if (field.equals("password"))
					u.setPassForRegister(newVal);
			}
		});
		return tf;
	}

	private CheckBox getCB(String field, boolean val, User u) {
		CheckBox cb = new CheckBox();
		cb.setWidth("50px");
		cb.setData(field);
		cb.setValue(val);
		cb.addBlurListener(new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(com.vaadin.event.FieldEvents.BlurEvent event) {
				Boolean newVal = ((CheckBox) event.getSource()).getValue();
				if (field.equals("usertype"))
					u.setUserType(newVal ? UserType.ADMIN : UserType.USER);
				else if (field.equals("delete"))
					u.setDBDelete(newVal);
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
				User u = new User(null, "", null, UserType.USER);
				usersCopy.add(u);
				grid.setItems(usersCopy);
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
					for (User u : usersCopy) {
						if (!u.isDBDelete()) {
							try {
								u.validateForAdmin();
							} catch (Exception e) {
								throw new Exception("User \"" + u.getName() + "\" contains errors:\n" + e.getMessage());
							}
							for (User u2 : usersCopy)
								if (u != u2 && u.getName().equals(u2.getName()))
									throw new Exception("Username \"" + u.getName() + "\" duplicated");
						}
					}
					sharedData.getUsers().updateFrom(usersCopy);
					sharedData.getUsers().saveDBAdmin();
					Notification.show("Data saved", Type.TRAY_NOTIFICATION);
					updateDisplayContent();
				} catch (SQLException e) {
					Notification.show("Database save error", Type.WARNING_MESSAGE);
				} catch (Exception ce) {
					Notification.show(ce.getMessage(), Type.WARNING_MESSAGE);
//					ce.printStackTrace();
				}
			}
		});
		return button;
	}

}
