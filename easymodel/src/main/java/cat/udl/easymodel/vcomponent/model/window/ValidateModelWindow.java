package cat.udl.easymodel.vcomponent.model.window;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.p;

public class ValidateModelWindow extends Window {
	private static final long serialVersionUID = 1L;

	private VerticalLayout windowVL;
	private SessionData sessionData;
	private Model selectedModel;
	private int width = 500;
	private int height = 500;

	public ValidateModelWindow(Model selMod) {
		super();

		this.setData(false); // for window close callback
		this.selectedModel = selMod;
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.setCaption("Model validation");
		this.setClosable(true);
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(true);
		this.center();
		this.setWidth(width + "px");
		this.setHeight(height + "px");

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
	}

	private void displayWindowContent() {
		VerticalLayout conPanelVL = new VerticalLayout();
		conPanelVL.setSpacing(true);
		conPanelVL.setMargin(true);
		conPanelVL.setSizeUndefined();
		conPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);

		Panel conPanel = new Panel();
		conPanel.setSizeFull();
		// conPanel.setStyleName("withoutborder");
		conPanel.setContent(conPanelVL);

		windowVL.addComponent(conPanel);
		windowVL.addComponent(getFooterHL());
		windowVL.setExpandRatio(conPanel, 1.0f);

		try {
			if (selectedModel.getRepositoryType() == RepositoryType.PRIVATE
					&& sessionData.getModels().getModelByName(selectedModel.getName()) != null
					&& sessionData.getModels().getModelByName(selectedModel.getName()) != selectedModel.getParent()) {
				throw new Exception("Model name \"" + selectedModel.getName() + "\" is already in use");
			}
			selectedModel.checkValidModel();
			this.setData(true);
			Label resLab = new Label("Validation: OK");
			conPanelVL.addComponents(resLab);
			if (sessionData.getRepository() == RepositoryType.PRIVATE) {
				try {
					boolean saveToModels = selectedModel.getId() == null;
					selectedModel.saveDB();
					Label lbl = new Label("MODEL SAVED (model will be automatically published in " +
						SharedData.getInstance().getProperties().getProperty("privateWeeks") + " weeks unless it's deleted before this time period)");
					lbl.setWidth("450px");
					conPanelVL.addComponent(lbl);
					if (saveToModels) {
						Model copy = new Model(selectedModel);
						copy.setParent(null);
						selectedModel.setParent(copy);
						sessionData.getModels().addModel(copy);
					}
				} catch (Exception e2) {
					conPanelVL.addComponents(new Label("ERROR: MODEL COULD NOT BE SAVED"));
				}
			}
			conPanelVL.addComponents(new Label("Stoichiometric Matrix"),
					selectedModel.getDisplayStoichiometricMatrix());
			conPanelVL.addComponents(new Label("Regulatory Matrix"), selectedModel.getDisplayRegulatoryMatrix());
		} catch (Exception e) {
			Label resLab = new Label("Model errors found:");
			TextArea ta = new TextArea();
			ta.setWidth((width - 50) + "px");
			ta.setHeight("300px");
			ta.setValue(e.getMessage());
			ta.setReadOnly(true);
			conPanelVL.addComponents(resLab, ta);
			this.setData(false);
		}
	}

	private void checkAndClose() {
		close();
	}

	private HorizontalLayout getFooterHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(false);
		hl.setMargin(false);
		HorizontalLayout spacerHL = new HorizontalLayout();
		spacerHL.setWidth("100%");
		hl.addComponents(spacerHL, getCloseButton());
		hl.setExpandRatio(spacerHL, 1.0f);
		return hl;
	}

	private Button getCloseButton() {
		Button btn = new Button("Close");
		btn.setId("nextButton");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				checkAndClose();
			}
		});
		return btn;
	}
}
