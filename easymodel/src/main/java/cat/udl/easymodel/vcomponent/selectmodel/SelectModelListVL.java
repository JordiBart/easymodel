package cat.udl.easymodel.vcomponent.selectmodel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
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
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.app.AppPanel;
import cat.udl.easymodel.vcomponent.common.AreYouSureWindow;

public class SelectModelListVL extends VerticalLayout {
	private SessionData sessionData;
	private NativeSelect<Model> modelListSelect;
	private Panel descriptionPanel;
	private AppPanel mainPanel;

	public SelectModelListVL(AppPanel mainPanel) {
		super();
		this.mainPanel = mainPanel;
		this.sessionData = (SessionData) UI.getCurrent().getData();

		this.setSpacing(true);
		this.setMargin(true);
		this.setSizeFull();
		this.setStyleName("panelBorder");
		this.setDefaultComponentAlignment(Alignment.TOP_LEFT);

		update();
	}

	public void update() {
		this.removeAllComponents();

		descriptionPanel = new Panel();
		descriptionPanel.setSizeFull();
		VerticalLayout descVL = new VerticalLayout();
		descVL.setCaption("Model Description");
		descVL.setSizeFull();
		descVL.setMargin(false);
		descVL.addComponent(descriptionPanel);

		modelListSelect = getModelListSelect();

		VerticalLayout contentVL = new VerticalLayout();
		contentVL.setSpacing(true);
		contentVL.setMargin(false);
		contentVL.setSizeFull();
		contentVL.addComponents(modelListSelect, descVL);
		contentVL.setExpandRatio(modelListSelect, 0.6f);
		contentVL.setExpandRatio(descVL, 0.4f);
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
		
		tryFocus();
	}
	
	public void tryFocus(){
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
						updateDescriptionPanel(selModel.getDescription());
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

	private void updateDescriptionPanel(String desc) {
		CustomLayout cl = new CustomLayout();
		try {
			String html = ToolboxVaadin.sanitizeHTML(desc);
			cl = new CustomLayout(new ByteArrayInputStream(desc.getBytes(StandardCharsets.UTF_8)));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cl.setStyleName("descriptionHTML");
		}
		descriptionPanel.setContent(cl);
	}
	
	private HorizontalLayout getFooterHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setHeight("38px");
		hl.setSpacing(true);
		if (sessionData.getRepository() != null) {
			if (sessionData.isUserSet()) {
				if (sessionData.getRepository() == RepositoryType.PRIVATE) {
					hl.addComponents(getNewModelButton(sessionData.isUserSet()), getDeleteModelButton());
				} else if (sessionData.getRepository() == RepositoryType.PUBLIC) {
					hl.addComponents(getImportPublicModelToPrivateModelsButton());
				}
			} else {
				hl.addComponent(getNewModelButton(sessionData.isUserSet()));
			}
			HorizontalLayout spacer = new HorizontalLayout();
			hl.addComponents(spacer);
			hl.setExpandRatio(spacer, 1.0f);
			hl.addComponents(getLoadModelButton());
		}
		return hl;
	}

	private Component getImportPublicModelToPrivateModelsButton() {
		Button btn = new Button("Import to private");
		btn.setDescription("Import a copy of the model to your private repository");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
					if (sessionData.getUser() == null)
						throw new Exception("A user account is required for this operation");
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

	private Button getNewModelButton(boolean isUserSet) {
		Button btn = new Button("New Model");
		btn.setId("newModelBtn");
		btn.setIcon(VaadinIcons.PLUS);
		btn.setDescription("Create a new model");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				// NEW MODEL
				Model m = new Model();
				m.setUser(sessionData.getUser());
				if (isUserSet)
					m.setRepositoryType(RepositoryType.PRIVATE);
				else
					m.setRepositoryType(RepositoryType.TEMP);	
				m.addReaction(new Reaction());
				sessionData.setSelectedModel(m);
				mainPanel.showEditModel();
			}
		});
		return btn;
	}

	private Button getDeleteModelButton() {
		Button btn = new Button("Delete Model");
		btn.setIcon(VaadinIcons.MINUS);
		btn.setId("delModelBtn");
		btn.setDescription("Delete selected model");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
					if (sessionData.getUser() == null)
						throw new Exception("A user account is required for this operation");
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
		btn.setIcon(VaadinIcons.PLAY);
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
			sessionData.cancelSimulationByUser();
			mainPanel.showEditModel();
		} catch (SQLException e) {
			sessionData.setSelectedModel(null);
			Notification.show("Error could not load model", Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
}
