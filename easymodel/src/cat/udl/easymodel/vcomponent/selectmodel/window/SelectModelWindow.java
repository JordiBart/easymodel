package cat.udl.easymodel.vcomponent.selectmodel.window;

import java.sql.SQLException;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.ModelImpl;
import cat.udl.easymodel.logic.model.ReactionImpl;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.types.WStatusType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.vcomponent.common.AreYouSureWindow;

import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class SelectModelWindow extends Window {
	private static final long serialVersionUID = 1L;

	private VerticalLayout windowVL;
	private SessionData sessionData;
	private ListSelect modelListSelect;
	private TextArea descTA;

	public SelectModelWindow() {
		super();

		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.setCaption("Select " + sessionData.getRepository().getString().toLowerCase() + " model");
//		if (sessionData.getSelectedModel() == null)
//			this.setClosable(false);
//		else
			this.setClosable(true);
		this.setData(WStatusType.KO); // window closed by user
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(false);
		this.center();
		this.setWidth("400px");
		this.setHeight("500px");

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		this.addShortcutListener(new ShortcutListener("Shortcut enter", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {
				checkAndClose();
			}
		});
		displayWindowContent();
		if (modelListSelect.getItemIds().size() > 0)
			modelListSelect.select(modelListSelect.getItemIds().toArray()[0]);
		modelListSelect.focus();
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		modelListSelect = getModelListSelect();

		descTA = new TextArea();
		descTA.setReadOnly(true);
		descTA.setSizeFull();

		VerticalLayout contentPanelVL = new VerticalLayout();
		contentPanelVL.setSpacing(true);
		contentPanelVL.setSizeFull();
		// contentPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		contentPanelVL.addComponents(modelListSelect, descTA);
		contentPanelVL.setExpandRatio(modelListSelect, 0.70f);
		contentPanelVL.setExpandRatio(descTA, 0.30f);

		Panel conPanel = new Panel();
		conPanel.setSizeFull();
		conPanel.setStyleName("withoutborder");
		conPanel.setContent(contentPanelVL);

		windowVL.addComponent(contentPanelVL);
		windowVL.addComponent(getFooterHL());
		windowVL.setExpandRatio(contentPanelVL, 1.0f);
	}

	private ListSelect getModelListSelect() {
		ListSelect select = new ListSelect();
		select.setSizeFull();
		select.setNullSelectionAllowed(false);
		for (Model m : sessionData.getModels()) {
			if (m.getRepositoryType() == sessionData.getRepository() && (m.getRepositoryType() == RepositoryType.PUBLIC
					|| m.getRepositoryType() == RepositoryType.PRIVATE && m.getUser() == sessionData.getUser())) {
				if (select.addItem(m) != null) {
					select.setItemCaption(m, m.getName());
				}
			}
		}
		select.setRows(10);
		select.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if (event.getProperty().getValue() != null) {
					descTA.setReadOnly(false);
					descTA.setValue(((Model) event.getProperty().getValue()).getDescription());
					descTA.setReadOnly(true);
				}
			}
		});
		return select;
	}

	private HorizontalLayout getFooterHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		VerticalLayout spacerVL = new VerticalLayout();
		spacerVL.setWidth("100%");
		if (sessionData.getRepository() == RepositoryType.PRIVATE)
			hl.addComponents(getNewModelButton(), getDeleteModelButton());
		hl.addComponents(spacerVL, getBackButton(), getNextButton());
		hl.setExpandRatio(spacerVL, 1.0f);
		return hl;
	}

	private Button getNewModelButton() {
		Button btn = new Button("New");
		btn.setId("newModelBtn");
		btn.setDescription("Create a new model");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				// NEW MODEL
				Model m = new ModelImpl();
				m.setUser(sessionData.getUser());
				m.setRepositoryType(RepositoryType.PRIVATE);
				m.addReaction(new ReactionImpl());
				sessionData.setSelectedModel(m);
				setData(WStatusType.OK);
				close();
			}
		});
		return btn;
	}

	private Button getDeleteModelButton() {
		Button btn = new Button("Delete");
		btn.setId("delModelBtn");
		btn.setDescription("Delete selected model");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				// DEL MODEL
				if (modelListSelect.getValue() != null) {
					Model mToDel = (Model) modelListSelect.getValue();
					AreYouSureWindow sureWin = getAreYouSureWindow(mToDel);
					UI.getCurrent().addWindow(sureWin);
				} else {
					Notification.show("Please select model to be deleted", Type.HUMANIZED_MESSAGE);
				}
			}
		});
		return btn;
	}

	private AreYouSureWindow getAreYouSureWindow(Model m) {
		AreYouSureWindow win = new AreYouSureWindow("Confirmation",
				"Are you sure to delete model " + m.getName() + "?");
		win.addCloseListener(new CloseListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void windowClose(CloseEvent e) {
				if ((WStatusType) e.getWindow().getData() == WStatusType.OK) {
					try {
						m.deleteDB();
						sessionData.getModels().removeModel(m);
						displayWindowContent();
					} catch (SQLException e1) {
						Notification.show("Delete model error", Type.WARNING_MESSAGE);
					}
				}
			}
		});
		return win;
	}

	private Button getNextButton() {
		Button btn = new Button("Next");
		btn.setId("nextBtn");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				checkAndClose();
			}
		});
		return btn;
	}

	private Button getBackButton() {
		Button btn = new Button("Back");
		btn.setId("backBtn");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				setData(WStatusType.BACK);
				close();
			}
		});
		return btn;
	}

	private void checkAndClose() {
		if (modelListSelect.getValue() != null) {
			sessionData.setSelectedModel((Model) modelListSelect.getValue());
			try {
				sessionData.getSelectedModel().loadDB();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			setData(WStatusType.OK);
			close();
		} else {
			Notification.show("Please select a model");
		}
	}
}
