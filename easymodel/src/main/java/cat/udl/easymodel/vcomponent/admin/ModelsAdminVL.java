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

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Models;
import cat.udl.easymodel.logic.model.Models;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.CException;

public class ModelsAdminVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	// External resources
	private SessionData sessionData = null;

	private VerticalLayout mainVL = null;
	private VerticalLayout optionButtonsVL = null;
	private VerticalLayout gridVL = null;
	private Grid<Model> grid;

	private Models loadedModels = null;

	public ModelsAdminVL() {
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
			loadedModels = new Models();
			loadedModels.semiLoadDB();
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
		grid = new Grid<Model>();
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setSizeFull();
		grid.setItems(loadedModels);
		grid.addComponentColumn(m -> {
			return getTF("name", "Name", m.getName(), m);
		}).setCaption("Name");
		grid.addComponentColumn(m -> {
			return getCB("repositorytype", m.getRepositoryType() == RepositoryType.PUBLIC, m);
		}).setCaption("Public");
		grid.addColumn(Model::getUserName).setCaption("User");
		grid.addComponentColumn(m -> {
			return getCB("delete", m.isDBDelete(), m);
		}).setCaption("Delete");
		vl.addComponent(grid);
		return vl;
	}

	private TextField getTF(String field, String prompt, String val, Model m) {
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
					m.setName(newVal);
			}
		});
		return tf;
	}

	private CheckBox getCB(String field, boolean val, Model m) {
		CheckBox cb = new CheckBox();
		cb.setWidth("50px");
		cb.setData(field);
		cb.setValue(val);
		cb.addBlurListener(new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(com.vaadin.event.FieldEvents.BlurEvent event) {
				Boolean newVal = ((CheckBox) event.getSource()).getValue();
				if (field.equals("repositorytype"))
					m.setRepositoryType(newVal ? RepositoryType.PUBLIC : RepositoryType.PRIVATE);
				else if (field.equals("delete"))
					m.setDBDelete(newVal);
			}
		});
		return cb;
	}

	private Button getSaveButton() {
		Button button = new Button("Save changes");
		button.setWidth("100%");
		button.setId("saveButton");
		button.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
					for (Model m : loadedModels) {
						if (!m.isDBDelete() && (m.getName().isEmpty())) {
							throw new CException(
									"Model \"" + m.getName() + "\" contains errors:\nModel requires a name");
						}
					}
					for (Model m : loadedModels)
						m.saveDBAdmin();
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
