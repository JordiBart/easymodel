package cat.udl.easymodel.view;

import java.io.File;

import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.login.AppInfoWindow;
import cat.udl.easymodel.vcomponent.login.BrowserCompabilityWindow;
import cat.udl.easymodel.vcomponent.login.ContactWindow;
import cat.udl.easymodel.vcomponent.login.RegisterWindow;

public class CoverView extends CustomComponent implements View {
	public static final String NAME = "";

	private TextField userTF;
	private PasswordField passwordTF;
	private Button loginBtn;
	private SessionData sessionData;
	private SharedData sharedData = SharedData.getInstance();
	VerticalLayout pushVL;

	public CoverView() {
	}

	@Override
	public void enter(ViewChangeEvent enterEvent) {
//		p.p("loginview enter");
		this.sessionData = (SessionData) UI.getCurrent().getData();
		Image emLogo = new Image(null,
				new FileResource(new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()
						+ "/VAADIN/themes/easymodel/img/easymodel-logo-236.png")));
		emLogo.setSizeFull();

		Button startBtn = new Button("Start using " + SharedData.appName + "!");
//		startBtn.setStyleName("enterGuest");
		startBtn.setIcon(VaadinIcons.PLAY);
		startBtn.setWidth("100%");
		startBtn.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				openAccess();
			}
		});

		userTF = new TextField();
		userTF.setWidth("100%");
		userTF.setPlaceholder("Username");
		userTF.addValueChangeListener(getUserAndPassValueChangeListener());
		userTF.setCaption("User Access");
		userTF.setIcon(VaadinIcons.USER);
		userTF.setStyleName("coverUserTextField");

		passwordTF = new PasswordField();
		passwordTF.setWidth("100%");
		passwordTF.setPlaceholder("Password");
		passwordTF.addValueChangeListener(getUserAndPassValueChangeListener());

		loginBtn = new Button("Log In", getLoginButtonClickListener());
		loginBtn.setIcon(VaadinIcons.SIGN_IN);
		loginBtn.setWidth("100%");
		loginBtn.setClickShortcut(KeyCode.ENTER);
		loginBtn.setEnabled(false);

		Button regBtn = new Button("Create Account");
		regBtn.setWidth("100%");
		regBtn.setIcon(VaadinIcons.OPEN_BOOK);
		regBtn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				RegisterWindow registerWindow = getRegisterWindow();
				ToolboxVaadin.removeAllWindows();
				UI.getCurrent().addWindow(registerWindow);
			}
		});

		VerticalLayout userAccessVL = new VerticalLayout();
		userAccessVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		userAccessVL.setSpacing(true);
		userAccessVL.setMargin(true);
		userAccessVL.setWidth("500px");
		userAccessVL.setStyleName("coverBox");
		userAccessVL.addComponents(userTF, passwordTF, loginBtn, regBtn);

		VerticalLayout emVL = new VerticalLayout();
		emVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		emVL.setSpacing(true);
		emVL.setMargin(true);
		emVL.setWidth("500px");
		emVL.setStyleName("coverBox");
		emVL.addComponents(emLogo, getWelcomeLabel(), startBtn);

		pushVL = new VerticalLayout();
		VerticalLayout contentVL = new VerticalLayout();
		contentVL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		contentVL.setSpacing(true);
		contentVL.setWidth("100%");
		contentVL.setStyleName("coverContent");
		contentVL.addComponents(emVL, userAccessVL);

		Panel mainPanel = new Panel();
		mainPanel.setSizeFull();
		mainPanel.setContent(contentVL);

		VerticalLayout footerVL = getFooterVL();

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		viewLayout.setSizeFull();
		viewLayout.setStyleName("coverBackground");
		viewLayout.setSpacing(false);
		viewLayout.setMargin(false);
		viewLayout.addComponents(mainPanel, footerVL);
		viewLayout.setExpandRatio(mainPanel, 1f);

		setCompositionRoot(viewLayout);
		setSizeFull();

		startBtn.focus();

		if (sharedData.isDebug()) {
		}
	}
	
	private ValueChangeListener<String> getUserAndPassValueChangeListener() {
		return (event -> {
			if (userTF.getValue().length() > 0 && passwordTF.getValue().length() > 0)
				loginBtn.setEnabled(true);
			else
				loginBtn.setEnabled(false);
		});
	}

	private Component getWelcomeLabel() {
		Label lbl = new Label();
		lbl.setContentMode(ContentMode.HTML);
		lbl.setValue(
				"Welcome to "+SharedData.appName+", a user-friendly web server for model building, simulation, and analysis in systems biology. Calculus core powered by webMathematica.");
		lbl.setWidth("440px");
		lbl.setStyleName("coverAppDescription");
		return lbl;
	}

	private Component getUserAccessLabel() {
		Label lbl = new Label();
		lbl.setContentMode(ContentMode.HTML);
		lbl.setValue(VaadinIcons.USER.getHtml() + " User Access");
		lbl.setStyleName("coverUserAccessLabel");
		return lbl;
	}

	private void openAccess() {
		getUI().getNavigator().navigateTo(TutorialView.NAME);
	}

	private RegisterWindow getRegisterWindow() {
		RegisterWindow regWindow = new RegisterWindow(sharedData.getUsers());
		regWindow.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				if ((boolean) e.getWindow().getData()) {
					if (sessionData.isUserSet()) {
						getUI().getNavigator().navigateTo(AppView.NAME);
					}
				}
			}

		});
		return regWindow;
	}

	private ClickListener getLoginButtonClickListener() {
		return new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				String userStr = userTF.getValue();
				String passwordStr = passwordTF.getValue();
				if (!userStr.matches(ToolboxVaadin.usernameCharRegex + "*")
						|| !passwordStr.matches(ToolboxVaadin.passwordCharRegex + "*")) {
					Notification.show("Username/Password contains invalid character/s", Type.WARNING_MESSAGE);
					return;
				}
				if (sharedData.isDebug()) {
					if (userStr.equals("t"))
						userStr = "test";
					if (userStr.equals("a"))
						userStr = "admin";
					if ("test".equals(userStr))
						passwordStr = "testing7";
					if ("admin".equals(userStr))
						passwordStr = "control7";
				}
				for (User u : sharedData.getUsers()) {
					if (u.matchLogin(userStr, passwordStr)) {
						sessionData.setUser(u);
						break;
					}
				}
				if (sessionData.isUserSet()) {
					if (sessionData.getUser().getUserType() == UserType.USER) {
						getUI().getNavigator().navigateTo(AppView.NAME);
					} else if (sessionData.getUser().getUserType() == UserType.ADMIN) {
						getUI().getNavigator().navigateTo(AdminView.NAME);
					}
				} else {
					Notification.show("Invalid username/password", Type.WARNING_MESSAGE);
					if (!userStr.equals(""))
						passwordTF.focus();
					else
						userTF.focus();
				}
			}
		};

	}

	private VerticalLayout getFooterVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		vl.setMargin(false);
		vl.setSpacing(true);
		vl.setStyleName("coverFooter");
		HorizontalLayout specialHL = new HorizontalLayout();
		specialHL.setMargin(false);
		specialHL.setSpacing(false);
//		specialButtonsHL.addComponent(getImportBiomodelsSBMLButton());
//		specialButtonsHL.addComponents(getMigrateToDBOldFormulasButton());

		if (sharedData.isDebug()) {
			Label dbgLab = new Label("DEBUG MODE ON (user=a->admin panel)");
			dbgLab.setSizeUndefined();
			specialHL.addComponent(dbgLab);
			vl.addComponents(specialHL);
		}

		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidthUndefined();
		hl.setMargin(false);
		hl.setSpacing(false);
		hl.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		hl.addComponents(getUdlLink(), getWebMathematicaLink(), getSBMLLink(), getGitHubLink(), getBrowserCompatibilityButton(),
				getContactButton(), getCounterVL());
		vl.addComponents(hl);
		return vl;
	}

	private Component getCounterVL() {
		VerticalLayout counterVL = new VerticalLayout();
		counterVL.setDescription("Visit counter");
		counterVL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		counterVL.setMargin(false);
		counterVL.setSpacing(false);
		counterVL.setWidth("70px");
		counterVL.setHeight("40px");
		counterVL.setStyleName("visitCounter");
		counterVL.addComponent(new Label(sharedData.getVisitCounterRunnable().getTotalCounter().toString()));
		return counterVL;
	}

	private Link getUdlLink() {
		Link link = new Link("", new ExternalResource("http://www.udl.cat/ca/en/"));
		link.setIcon(new ThemeResource("img/udl-logo.jpg"));
		link.setTargetName("_blank");
		link.setStyleName("udlLogo");
		return link;
	}

	private Link getWebMathematicaLink() {
		Link link = new Link("", new ExternalResource("http://www.wolfram.com/webmathematica/sitelink"));
		link.setIcon(new ThemeResource("img/webm-white-plain-no-border.png"));
		link.setTargetName("_blank");
		link.setStyleName("coverWebMathLogo");
		return link;
	}

	private Link getSBMLLink() {
		Link link = new Link("", new ExternalResource("http://sbml.org/"));
		link.setIcon(new ThemeResource("img/sbml-logo.png"));
		link.setTargetName("_blank");
		link.setStyleName("sbmlLogo");
		return link;
	}

	private Link getGitHubLink() {
		Link link = new Link("", new ExternalResource("https://github.com/jordibart/easymodel/"));
		link.setIcon(new ThemeResource("img/github-logo.png"));
		link.setTargetName("_blank");
		link.setDescription("GitHub project source-code repository");
		link.setStyleName("githubLogo");
		return link;
	}

	private Component getContactButton() {
		Button btn = new Button();
		btn.setDescription("Contact us");
		btn.setStyleName("coverContact");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ToolboxVaadin.removeAllWindows();
				UI.getCurrent().addWindow(new ContactWindow());
			}
		});
		return btn;
	}
	
	private Component getBrowserCompatibilityButton() {
		Button btn = new Button();
		btn.setDescription("Browser Compability");
		btn.setStyleName("coverBrowserCompability");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ToolboxVaadin.removeAllWindows();
				UI.getCurrent().addWindow(new BrowserCompabilityWindow());
			}
		});
		return btn;
	}
	
	private Component getInfoButton() {
		Button btn = new Button();
		btn.setDescription(SharedData.appName + " information");
		btn.setStyleName("coverInfo");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ToolboxVaadin.removeAllWindows();
				UI.getCurrent().addWindow(new AppInfoWindow());
			}
		});
		return btn;
	}
}