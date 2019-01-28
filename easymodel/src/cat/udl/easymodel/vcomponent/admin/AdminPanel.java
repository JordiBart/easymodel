package cat.udl.easymodel.vcomponent.admin;

import java.util.HashMap;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.vcomponent.model.window.ValidateModelWindow;
import cat.udl.easymodel.view.AdminView;
import cat.udl.easymodel.view.AppView;
import cat.udl.easymodel.view.LoginView;

public class AdminPanel extends Panel {
	private static final long serialVersionUID = 1L;

	private Panel conPanel = null;
	private UsersAdminVL usersAdminVL = new UsersAdminVL();
	private ModelsAdminVL modelsAdminVL = new ModelsAdminVL();
	private FormulasAdminVL formulasAdminVL = new FormulasAdminVL();
	private HashMap<String, Button> allButtons = new HashMap<>();
	private Button selectedButton;
	private AdminPanel globalThis = this;

	private SessionData sessionData = null;
	private SharedData sharedData = SharedData.getInstance();

	private HorizontalLayout inputTitleHL = new HorizontalLayout();

	public AdminPanel() {
		super();

		this.sessionData = (SessionData) UI.getCurrent().getData();

		HorizontalLayout headerHL = new HorizontalLayout();
		headerHL.setMargin(false);
		headerHL.setSpacing(true);
		headerHL.setWidth("100%");
		headerHL.setHeight("35px");
		headerHL.setStyleName("panelHeader");
		headerHL.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		setInputTitle("Editor - Models");

		HorizontalLayout allButtonsHL = new HorizontalLayout();
		allButtonsHL.setMargin(false);
		allButtonsHL.setSpacing(false);
		allButtonsHL.setHeight("30px");
		allButtonsHL.setWidth("100%");
		allButtonsHL.setStyleName("panelButtons");
		// allButtonsHL.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		allButtons.put("Users", getUsersButton());
		allButtons.put("Models", getModelsButton());
		allButtons.put("Formulas", getFormulasButton());
		allButtonsHL.addComponent(allButtons.get("Users"));
		allButtonsHL.addComponent(allButtons.get("Models"));
		allButtonsHL.addComponent(allButtons.get("Formulas"));
		HorizontalLayout spacer = new HorizontalLayout();
		allButtonsHL.addComponent(spacer);
		allButtonsHL.setExpandRatio(spacer, 1);
		allButtonsHL.addComponent(getSwitchAdminAppButton());
		allButtonsHL.addComponent(getExitButton());

		conPanel = new Panel();
		conPanel.setSizeFull();
		conPanel.setStyleName("withoutborder");

		VerticalLayout mainPanelVL = new VerticalLayout();
		mainPanelVL.setMargin(false);
		mainPanelVL.setSpacing(false);
		mainPanelVL.setSizeFull();
		mainPanelVL.addComponents(allButtonsHL, conPanel);
		mainPanelVL.setExpandRatio(conPanel, 1.0f);

		this.setId("mainPanel");
		this.setContent(mainPanelVL);
		this.setSizeFull();

		selectedButton = allButtons.get("Users");
		updateStepButtonsStyle();
		conPanel.setContent(usersAdminVL);
	}

	private void updateStepButtonsStyle() {
		for (Button btn : allButtons.values()) {
			if (btn == selectedButton) {
				btn.setStyleName("stepSelected");
			} else {
				btn.setStyleName("stepNotSelected");
			}
		}
	}

	private Button getUsersButton() {
		Button btn = new Button();
		btn.setCaption("Users");
		btn.setHeight("30px");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				selectedButton = allButtons.get("Users");
				updateStepButtonsStyle();
				conPanel.setContent(usersAdminVL);
			}
		});
		return btn;
	}

	private Button getModelsButton() {
		Button btn = new Button();
		btn.setCaption("Models");
		btn.setHeight("30px");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				selectedButton = allButtons.get("Models");
				updateStepButtonsStyle();
				conPanel.setContent(modelsAdminVL);
			}

		});
		return btn;
	}

	private Button getFormulasButton() {
		Button btn = new Button();
		btn.setCaption("Formulas");
		btn.setHeight("30px");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				selectedButton = allButtons.get("Formulas");
				updateStepButtonsStyle();
				conPanel.setContent(formulasAdminVL);
			}
		});
		return btn;
	}

	private Button getSwitchAdminAppButton() {
		Button btn = new Button();
		btn.setCaption("Go App");
		btn.setHeight("30px");
		btn.setStyleName("stepNotSelected");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().removeView(AdminView.NAME);
				UI.getCurrent().getNavigator().addView(AppView.NAME, AppView.class);
				UI.getCurrent().getNavigator().navigateTo(AppView.NAME);
			}

		});
		return btn;
	}
	private Button getExitButton() {
		Button btn = new Button();
		btn.setStyleName("exitBtn");
		btn.setHeight("30px");
		btn.setWidth("30px");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				sessionData.setUser(null);
				UI.getCurrent().getNavigator().removeView(AdminView.NAME);
				UI.getCurrent().getNavigator().navigateTo(LoginView.NAME);
			}
		});
		return btn;
	}
	private void setInputTitle(String title) {
		inputTitleHL.removeAllComponents();
		inputTitleHL.addComponent(new Label(title));
		inputTitleHL.markAsDirty();
	}

	public void validateModel() {
		ValidateModelWindow vmw = new ValidateModelWindow(sessionData.getSelectedModel());
		vmw.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				selectedButton = allButtons.get("Simulation");
				updateStepButtonsStyle();
				conPanel.setContent(modelsAdminVL);
			}
		});
		UI.getCurrent().addWindow(vmw);
	}

}
