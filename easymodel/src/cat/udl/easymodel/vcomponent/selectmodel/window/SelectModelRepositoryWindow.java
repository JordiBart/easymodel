package cat.udl.easymodel.vcomponent.selectmodel.window;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.p;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class SelectModelRepositoryWindow extends Window {
	private static final long serialVersionUID = 1L;

	private VerticalLayout windowVL;
	private OptionGroup pubPrivGroup;
	private SessionData sessionData;

	public SelectModelRepositoryWindow() {
		super();

		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.setCaption("Select model repository");
//		p.p(sessionData.getSelectedModel());
		if (sessionData.getSelectedModel() == null)
			this.setClosable(false);
		else
			this.setClosable(true);
		this.reset();
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(false);
		this.center();
		this.setWidth("300px");
		this.setHeight("220px");

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
		pubPrivGroup.focus();
	}

	public void reset() {
		this.setData(false); // window closed by user
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		pubPrivGroup = getPubPrivOptionGroup();

		VerticalLayout valuesPanelVL = new VerticalLayout();
		valuesPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		valuesPanelVL.addComponent(pubPrivGroup);

		Panel valuesPanel = new Panel();
		valuesPanel.setSizeFull();
		valuesPanel.setStyleName("withoutborder");
		valuesPanel.setContent(valuesPanelVL);

		windowVL.addComponent(valuesPanel);
		windowVL.addComponent(getFooterHL());
		windowVL.setExpandRatio(valuesPanel, 1.0f);
	}

	private OptionGroup getPubPrivOptionGroup() {
		OptionGroup group = new OptionGroup("Model repository");
		if (group.addItem(RepositoryType.PUBLIC) != null)
			group.setItemCaption(RepositoryType.PUBLIC, RepositoryType.PUBLIC.getString());
		if (group.addItem(RepositoryType.PRIVATE) != null)
			group.setItemCaption(RepositoryType.PRIVATE, RepositoryType.PRIVATE.getString());
		if (group.addItem(RepositoryType.SBML) != null)
			group.setItemCaption(RepositoryType.SBML, "Import from SBML file");
		// load value
		if (sessionData.getRepository() != null)
			group.select(sessionData.getRepository());
		else
			group.select(RepositoryType.PUBLIC);
		return group;
	}

	private HorizontalLayout getFooterHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		VerticalLayout spacerVL = new VerticalLayout();
		spacerVL.setWidth("100%");
		hl.addComponents(spacerVL, getNextButton());
		hl.setExpandRatio(spacerVL, 1.0f);
		return hl;
	}

	private Button getImportSBMLButton() {
		Button btn = new Button("Import SBML");
		btn.setDescription("Import model from a local device SBML file");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				ImportSBMLWindow importSBMLW = getImportSBMLWindow();
				UI.getCurrent().addWindow(importSBMLW);
			}

			private ImportSBMLWindow getImportSBMLWindow() {
				ImportSBMLWindow importSBMLW = new ImportSBMLWindow();
				importSBMLW.addCloseListener(new CloseListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void windowClose(CloseEvent e) {
						if ((boolean) e.getWindow().getData()) {
							setData(true);
							sessionData.setModelsRepo(RepositoryType.SBML);
							close();
						}
					}
				});
				return importSBMLW;
			}
		});
		return btn;
	}

	private Button getNextButton() {
		Button btn = new Button("Next");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				checkAndClose();
			}
		});
		return btn;
	}

	private void checkAndClose() {
		if (pubPrivGroup.getValue() != null) {
			sessionData.setModelsRepo((RepositoryType) pubPrivGroup.getValue());
			this.setData(true); // window closed after check
			close();
		} else {
			Notification.show("Please select a model repository");
		}
	}
}
