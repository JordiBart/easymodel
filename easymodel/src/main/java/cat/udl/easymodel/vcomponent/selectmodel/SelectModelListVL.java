package cat.udl.easymodel.vcomponent.selectmodel;

import java.sql.SQLException;
import java.util.ArrayList;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.types.WStatusType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.vcomponent.app.AppPanel;
import cat.udl.easymodel.vcomponent.common.AreYouSureWindow;

public class SelectModelListVL extends VerticalLayout {
	private SessionData sessionData;
	private NativeSelect<Model> modelListSelect;
	private TextArea descTA;
	private Button newModelButton;
	private AppPanel mainPanel;

	public SelectModelListVL(AppPanel mainPanel) {
		super();
		this.mainPanel = mainPanel;
		this.sessionData = (SessionData) UI.getCurrent().getData();

		this.setSpacing(true);
		this.setMargin(true);
		this.setSizeFull();
		this.setStyleName("selectModel");
		this.setDefaultComponentAlignment(Alignment.TOP_LEFT);

		update();
	}

	public void update() {
		this.removeAllComponents();

		descTA = new TextArea();
		descTA.setCaption("Model Description");
		descTA.setReadOnly(true);
		descTA.setSizeFull();

		modelListSelect = getModelListSelect();

		VerticalLayout contentVL = new VerticalLayout();
		contentVL.setSpacing(true);
		contentVL.setMargin(false);
		contentVL.setSizeFull();
		contentVL.addComponents(modelListSelect, descTA);
		contentVL.setExpandRatio(modelListSelect, 0.6f);
		contentVL.setExpandRatio(descTA, 0.4f);
		contentVL.addLayoutClickListener(new LayoutClickListener() {

			@Override
			public void layoutClick(LayoutClickEvent event) {
				if (event.getClickedComponent() == modelListSelect && event.isDoubleClick())
					checkAndClose();
			}
		});

		this.addComponent(contentVL);
		this.addComponent(getFooterHL());
		this.setExpandRatio(contentVL, 1.0f);

		if (modelListSelect.getValue() != null)
			modelListSelect.focus();
	}

	private NativeSelect<Model> getModelListSelect() {
		NativeSelect<Model> select = new NativeSelect<>();
		select.setCaption("Select Model");
		ArrayList<Model> selectItems = new ArrayList<>();
		select.setEmptySelectionAllowed(false);
		select.setSizeFull();
		select.setVisibleItemCount(10);
		if (sessionData.getRepository() != null) {
			for (Model m : sessionData.getModels()) {
				if (m.getRepositoryType() == sessionData.getRepository()
						&& (m.getRepositoryType() == RepositoryType.PUBLIC
								|| m.getRepositoryType() == RepositoryType.PRIVATE
										&& m.getUser() == sessionData.getUser())) {
					selectItems.add(m);
				}
			}
			select.setItems(selectItems);
			select.setItemCaptionGenerator(Model::getName);
			select.setData(selectItems);
			select.addValueChangeListener(new ValueChangeListener<Model>() {
				private static final long serialVersionUID = 96713853087046209L;

				@Override
				public void valueChange(ValueChangeEvent<Model> event) {
					Model selModel = event.getValue();
					if (selModel != null) {
						descTA.setReadOnly(false);
						descTA.setValue(selModel.getDescription());
						descTA.setReadOnly(true);
					}
				}
			});
			select.addShortcutListener(new ShortcutListener("enter", ShortcutAction.KeyCode.ENTER, null) {
				private static final long serialVersionUID = 1L;

				@Override
				public void handleAction(Object sender, Object target) {
					checkAndClose();
				}
			});
			if (selectItems.size() > 0)
				select.setSelectedItem(selectItems.get(0));
		}
		return select;
	}

	private HorizontalLayout getFooterHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setHeight("38px");
		hl.setSpacing(true);
		if (sessionData.getRepository() != null) {
			if (sessionData.getRepository() == RepositoryType.PRIVATE) {
				newModelButton = getNewModelButton();
				hl.addComponents(newModelButton, getDeleteModelButton());
			} else {
				hl.addComponents(getImportPublicModelToPrivateModels());
			}
			HorizontalLayout spacer = new HorizontalLayout();
			hl.addComponents(spacer);
			hl.addComponents(getLoadModelButton());
			hl.setExpandRatio(spacer, 1.0f);
		}
		return hl;
	}

	private Component getImportPublicModelToPrivateModels() {
		Button btn = new Button("Import to private");
		btn.setDescription("Import a copy of the model to your private repository");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
					if (sessionData.getUser().isGuest())
						throw new Exception("Guest user cannot do this operation");
					if (modelListSelect.getValue() == null)
						throw new Exception("Please select a model");
					Model m = modelListSelect.getValue();
					AreYouSureWindow sureWin = getConfirmImportWindow(m);
					UI.getCurrent().addWindow(sureWin);
				} catch (Exception e) {
					Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
				}
			}
		});
		return btn;
	}

	private AreYouSureWindow getConfirmImportWindow(Model m) {
		AreYouSureWindow win = new AreYouSureWindow("Confirmation",
				"Are you sure to import a copy of the model \"" + m.getName() + "\" to your private repository?");
		win.addCloseListener(new CloseListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void windowClose(CloseEvent e) {
				if ((WStatusType) e.getWindow().getData() == WStatusType.OK) {
					try {
						sessionData.setSelectedModel(
								sessionData.getModels().getPrivateModelCopy(m, sessionData.getUser()));
						sessionData.setRepository(RepositoryType.PRIVATE);
						mainPanel.showEditModel();
					} catch (SQLException e1) {
						Notification.show("Import model error", Type.WARNING_MESSAGE);
					}
				}
			}
		});
		return win;
	}

	private Button getNewModelButton() {
		Button btn = new Button("New Model");
		btn.setId("newModelBtn");
		btn.setDescription("Create a new model");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				// NEW MODEL
				Model m = new Model();
				m.setUser(sessionData.getUser());
				m.setRepositoryType(RepositoryType.PRIVATE);
				m.addReaction(new Reaction());
				sessionData.setSelectedModel(m);
				mainPanel.showEditModel();
			}
		});
		return btn;
	}

	private Button getDeleteModelButton() {
		Button btn = new Button("Delete Model");
		btn.setId("delModelBtn");
		btn.setDescription("Delete selected model");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
					if (sessionData.getUser().isGuest())
						throw new Exception("Guest user cannot do this operation");
					if (modelListSelect.getValue() == null)
						throw new Exception("Please select a model");
					Model m = modelListSelect.getValue();
					AreYouSureWindow sureWin = getConfirmDeleteWindow(m);
					UI.getCurrent().addWindow(sureWin);
				} catch (Exception e) {
					Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
				}
			}
		});
		return btn;
	}

	private AreYouSureWindow getConfirmDeleteWindow(Model m) {
		AreYouSureWindow win = new AreYouSureWindow("Confirmation",
				"Are you sure to delete the model \"" + m.getName() + "\"?");
		win.addCloseListener(new CloseListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void windowClose(CloseEvent e) {
				if ((WStatusType) e.getWindow().getData() == WStatusType.OK) {
					try {
						m.deleteDB();
						sessionData.getModels().removeModel(m);
						update();
					} catch (SQLException e1) {
						Notification.show("Delete model error", Type.WARNING_MESSAGE);
					}
				}
			}
		});
		return win;
	}

	private Button getLoadModelButton() {
		Button btn = new Button("Load Model");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				checkAndClose();
			}
		});
		return btn;
	}

	private void checkAndClose() {
		if (modelListSelect.getValue() == null) {
			Notification.show("Please select a model", Type.WARNING_MESSAGE);
			return;
		}
		try {
			Model copy = new Model(modelListSelect.getValue());
			copy.loadDB();
			sessionData.setSelectedModel(copy);
			mainPanel.showEditModel();
		} catch (SQLException e) {
			sessionData.setSelectedModel(null);
			Notification.show("Error could not load model", Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
}
