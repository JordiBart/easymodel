package cat.udl.easymodel.vcomponent.selectmodel.window;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.ContextClickEvent;
import com.vaadin.event.ContextClickEvent.ContextClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.types.WStatusType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.common.AreYouSureWindow;

public class SelectModelWindow extends Window {
	private static final long serialVersionUID = 1L;

	private VerticalLayout windowVL;
	private SessionData sessionData;
	private ListSelect<Model> modelListSelect;
	private TextArea descTA;
	private Button newModelButton;
	private Button backButton;

	@SuppressWarnings("unchecked")
	public SelectModelWindow() {
		super();

		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.setCaption("Select " + sessionData.getRepository().getString().toLowerCase() + " model");
		this.setWidth("400px");
		this.setHeight("500px");
		this.center();
		this.setClosable(true);
		this.setData(WStatusType.KO); // window closed by user
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(false);

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
		if (((ArrayList<Model>) modelListSelect.getData()).size() > 0)
			modelListSelect.select(((ArrayList<Model>) modelListSelect.getData()).get(0));
		if (modelListSelect.getValue().size() > 0)
			modelListSelect.focus();
		else if (sessionData.getRepository() == RepositoryType.PRIVATE)
			newModelButton.focus();
		else
			backButton.focus();
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		modelListSelect = getModelListSelect();

		descTA = new TextArea();
		descTA.setReadOnly(true);
		descTA.setSizeFull();

		VerticalLayout contentPanelVL = new VerticalLayout();
		contentPanelVL.setSpacing(true);
		contentPanelVL.setMargin(false);
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

	private Model getFirstSelectedItem(Set<Model> set) {
		if (set != null && set.size() > 0)
			return (Model) set.toArray()[0];
		else
			return null;
	}

	private ListSelect<Model> getModelListSelect() {
		ListSelect<Model> select = new ListSelect<>();
		ArrayList<Model> selectItems = new ArrayList<>();
		select.setSizeFull();
		for (Model m : sessionData.getModels()) {
			if (m.getRepositoryType() == sessionData.getRepository() && (m.getRepositoryType() == RepositoryType.PUBLIC
					|| m.getRepositoryType() == RepositoryType.PRIVATE && m.getUser() == sessionData.getUser())) {
				selectItems.add(m);
			}
		}
		select.setItems(selectItems);
		select.setItemCaptionGenerator(Model::getName);
		select.setData(selectItems);
		select.setRows(10);
		select.addContextClickListener(new ContextClickListener() {
			private static final long serialVersionUID = 2885298494923728038L;

			@Override
			public void contextClick(ContextClickEvent event) {
				// doesn't work
				System.out.println("ContextClickEvent");
				if (event.getButton() == MouseButton.LEFT && event.getMouseEventDetails().isDoubleClick()
						&& modelListSelect.getValue().size() > 0)
					checkAndClose();
			}
		});
		select.addValueChangeListener(new ValueChangeListener<Set<Model>>() {
			private static final long serialVersionUID = -7658446458315094477L;

			@Override
			public void valueChange(ValueChangeEvent<Set<Model>> event) {
				Model selModel = getFirstSelectedItem(event.getValue());
				if (selModel != null) {
					descTA.setReadOnly(false);
					descTA.setValue(selModel.getDescription());
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
		HorizontalLayout spacer = new HorizontalLayout();
		spacer.setWidth("100%");
		if (sessionData.getRepository() == RepositoryType.PRIVATE) {
			newModelButton = getNewModelButton();
			hl.addComponents(newModelButton, getDeleteModelButton());
		}
		backButton = getBackButton();
		hl.addComponents(spacer, backButton, getNextButton());
		hl.setExpandRatio(spacer, 1.0f);
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
				Model m = new Model();
				m.setUser(sessionData.getUser());
				m.setRepositoryType(RepositoryType.PRIVATE);
				m.addReaction(new Reaction());
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
				if (modelListSelect.getValue().size() > 0) {
					Model mToDel = getFirstSelectedItem(modelListSelect.getValue());
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
		if (modelListSelect.getValue().size() > 0) {
			sessionData.setSelectedModel(getFirstSelectedItem(modelListSelect.getValue()));
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
