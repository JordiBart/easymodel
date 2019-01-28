package cat.udl.easymodel.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.controller.BioModelsLogs;
import cat.udl.easymodel.controller.SimulationCtrl;
import cat.udl.easymodel.controller.SimulationCtrlImpl;
import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.types.WStatusType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.logic.user.UserImpl;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.sbml.SBMLMan;
import cat.udl.easymodel.utils.BCrypt;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.register.window.RegisterWindow;
import cat.udl.easymodel.vcomponent.selectmodel.window.ImportSBMLWindow;
import cat.udl.easymodel.vcomponent.selectmodel.window.SelectModelRepositoryWindow;
import cat.udl.easymodel.vcomponent.selectmodel.window.SelectModelWindow;

public class LoginView extends CustomComponent implements View {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "login";

	private TextField user;
	private PasswordField password;
	private Button loginButton;
	private SessionData sessionData;
	private SharedData sharedData = SharedData.getInstance();
	private ArrayList<User> allUsers;
	private boolean isError = false;

	public LoginView() {
	}

	@Override
	public void enter(ViewChangeEvent event) {
		this.sessionData = (SessionData) UI.getCurrent().getData();

		try {
			allUsers = sharedData.getAllUsers();
		} catch (SQLException e) {
			isError = true;
			Notification.show(SharedData.dbError, Type.ERROR_MESSAGE);
		}

		user = new TextField("Username");
		user.setWidth("150px");
		user.setRequired(true);

		password = new PasswordField("Password");
		password.setWidth("150px");
		// password.addValidator(new PasswordValidator());
		password.setRequired(true);
		password.setValue("");
		password.setNullRepresentation("");

		loginButton = new Button("Login", getLoginButtonClickListener());
		if (isError)
			loginButton.setEnabled(false);
		loginButton.setWidth("100px");
		loginButton.setClickShortcut(KeyCode.ENTER);

		HorizontalLayout titleHL = new HorizontalLayout();
		titleHL.setMargin(false);
		Label loginLabel = new Label("Welcome to " + SharedData.appName + ", please log in below");
		Label titleLabel = new Label(SharedData.appName);
		VerticalLayout spacerTitle = new VerticalLayout();
		spacerTitle.setWidth((460 - loginLabel.getWidth() - titleLabel.getWidth()) + "px");
		titleHL.addComponents(loginLabel, spacerTitle, titleLabel);
		titleHL.setExpandRatio(spacerTitle, 1.0f);

		HorizontalLayout fieldsHL = new HorizontalLayout();
		// fieldsHL.setHeight("50px");
		fieldsHL.setSpacing(true);
		fieldsHL.setMargin(false);
		fieldsHL.setDefaultComponentAlignment(Alignment.BOTTOM_CENTER);
		fieldsHL.addComponents(user, password, loginButton);

		HorizontalLayout accessAsGuestHL = getOrButtonsHL();

		VerticalLayout loginPanelVL = new VerticalLayout();
		loginPanelVL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		VerticalLayout spacerPanelVL = new VerticalLayout();
		Label orLab = new Label("or");
		orLab.setSizeUndefined();
		loginPanelVL.addComponents(titleHL, fieldsHL, orLab, accessAsGuestHL);
		loginPanelVL.setSpacing(true);
		loginPanelVL.setMargin(true);
		loginPanelVL.setSizeFull();

		Panel loginPanel = new Panel(loginPanelVL);
		loginPanel.setWidth("475px");
		loginPanel.setHeight("205px");

		if (sharedData.isDebug()) {
			Label dbgLab = new Label("TEST MODE: user=empty->application; user=\"a\"->admin");
			dbgLab.setSizeUndefined();
			loginPanelVL.addComponent(dbgLab);
			loginPanel.setHeight("230px");
		}
		loginPanelVL.addComponents(spacerPanelVL);
		loginPanelVL.setExpandRatio(spacerPanelVL, 1.0f);

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		HorizontalLayout headerHL = getHeaderHL();
		HorizontalLayout footerHL = getFooterHL();
		viewLayout.addComponents(headerHL, loginPanel, footerHL);
		viewLayout.setExpandRatio(loginPanel, 1f);
		viewLayout.setStyleName(Reindeer.LAYOUT_BLUE);
		viewLayout.setSizeFull();
		viewLayout.setStyleName("loginView");
		viewLayout.setSpacing(false);
		viewLayout.setMargin(false);
		setCompositionRoot(viewLayout);
		setSizeFull();
		user.focus();
	}

	private HorizontalLayout getHeaderHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		FileResource resource = new FileResource(
				new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()
						+ "/VAADIN/themes/easymodel/img/easymodel-logo-80.png"));
		Image image = new Image(null, resource);
		hl.addComponent(image);
		return hl;
	}

	private HorizontalLayout getFooterHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		FileResource resource = new FileResource(
				new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()
						+ "/VAADIN/themes/easymodel/img/sbml-logo-40.png"));
		Image image = new Image(null, resource);
		hl.addComponent(image);
		return hl;
	}

	private HorizontalLayout getImportTestVL() {
		HorizontalLayout hl = new HorizontalLayout();
		Button btn = new Button("Biomodels batch");
//		hl.addComponent(btn);
		btn.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				p.p("Batch start!");
				sessionData.setBioModelsLogs(new BioModelsLogs());
				SimulationCtrl simCtrl = new SimulationCtrlImpl(sessionData);
				int totalLoadOK = 0, totalLoadKO = 0;
				try {
					sessionData.setUser(sharedData.getUserByName("test"));
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				try {
					FileInputStream fileInputStream = null;
					byte[] bFile;
					File sbmlDir = new File(SharedData.appDir + "/biomodels");
					for (File file : sbmlDir.listFiles()) {
						// avoid RAM eater models
//						if (file.getName().equals("BIOMD0000000033.xml")||file.getName().equals("BIOMD0000000072.xml")||file.getName().equals("BIOMD0000000093.xml")||file.getName().equals("BIOMD0000000094.xml")||file.getName().equals("BIOMD0000000595.xml"))
//							continue;
//						if (!file.getName().equals("BIOMD0000000063.xml"))
//							continue;
//						if (totalLoadOK >= 3)
//							break;
						StringBuilder report = new StringBuilder();
						try {
							bFile = Files.readAllBytes(file.toPath());
							ByteArrayInputStream bais = new ByteArrayInputStream(bFile);
							Model m = SBMLMan.getInstance().importSBML(bais, report,
									"BIOMD" + file.getName().substring(12, 15) + " ");
							m.checkValidModel();
//							p.p(file.getName() + " OK");
							sessionData.getBioModelsLogs().loadLogFile.write(file.getName() + "\n");
							if (report.length() > 0)
								sessionData.getBioModelsLogs().loadLogFile.write(report.toString());
							totalLoadOK++;
							// store to db
							for (Formula tf : sessionData.getTempFormulas()) {
								tf.setFormulaType(FormulaType.CUSTOM);
								tf.setRepositoryType(RepositoryType.PRIVATE);
								tf.saveDB();//
							}
							m.saveDB();
							// simulate
//							System.out.println("Simulating " + file.getName());
//							sessionData.getBioModelsLogs().simLogFile.append(file.getName()+" ############################################\n");
//							sessionData.setSelectedModel(m);
//							try {
//								simCtrl.simulate();
//							}catch(Exception e) {
//								System.out.println("sim exception");
//								e.printStackTrace();
//							}
						} catch (Exception e) {
							// p.p(file.getName() + " : " + e.getMessage());
							// if (!e.getMessage().startsWith("C ")) {
							sessionData.getBioModelsLogs().errorLogFile.write(file.getName() + "\n");
							sessionData.getBioModelsLogs().errorLogFile.write(e.getMessage() + "\n");
							if (report.length() > 0)
								sessionData.getBioModelsLogs().errorLogFile.write("report:\n" + report.toString());
							totalLoadKO++;
							// }
							if (e.getMessage() == null) {
								e.printStackTrace();
								break;
							}
						}
					}
					sessionData.getBioModelsLogs().loadLogFile.write("total " + totalLoadOK + "\n");
					sessionData.getBioModelsLogs().errorLogFile.write("total " + totalLoadKO + "\n");
				} catch (IOException e) {
					p.p("error IO file/SQL " + e.getMessage());
				} finally {
					sessionData.getBioModelsLogs().close();
					p.p("SBML batch finish");
				}
				sessionData.setUser(null);
			}
		});
		return hl;
	}

	private HorizontalLayout getOrHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.setMargin(false);
		hl.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		hl.addComponents(new Label("or"));
		return hl;
	}

	private HorizontalLayout getOrButtonsHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.setMargin(false);
		hl.setDefaultComponentAlignment(Alignment.BOTTOM_CENTER);
		Button guestBtn = new Button("Enter as Guest");
		guestBtn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (sharedData.getGuestUser() != null) {
					sessionData.setUser(sharedData.getGuestUser());
					getUI().getNavigator().addView(AppView.NAME, AppView.class);
					getUI().getNavigator().navigateTo(AppView.NAME);
				}
			}
		});
		if (isError)
			guestBtn.setEnabled(false);
		Button regBtn = new Button("Create New Account");
		regBtn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				RegisterWindow registerWindow = getRegisterWindow();
				UI.getCurrent().addWindow(registerWindow);
			}
		});
		if (isError)
			regBtn.setEnabled(false);
		hl.addComponents(guestBtn, regBtn);
		hl.addComponent(getImportTestVL());
		return hl;
	}

	private RegisterWindow getRegisterWindow() {
		RegisterWindow regWindow = new RegisterWindow(allUsers);
		regWindow.addCloseListener(new CloseListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void windowClose(CloseEvent e) {
				if ((boolean) e.getWindow().getData() && sessionData.getUser() != null) {
					getUI().getNavigator().addView(AppView.NAME, AppView.class);
					getUI().getNavigator().navigateTo(AppView.NAME);
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
				String userStr = user.getValue();
				String passwordStr = password.getValue();

				if (sharedData.isDebug()) {
					if (userStr.equals(""))
						userStr = "test";
					if (userStr.equals("a"))
						userStr = "admin";
					if ("test".equals(userStr))
						passwordStr = "testing7";
					if ("admin".equals(userStr))
						passwordStr = "control7";
				}
				if ((Pattern.matches("\\w+", userStr) && Pattern.matches("\\w+", passwordStr))) {
					for (User u : allUsers) {
						// JBcrypt uses 2a prefix!!!
						// gen: https://asecuritysite.com/encryption/bcrypt
						if (u.getName().equals(userStr)) {
							if (BCrypt.checkpw(passwordStr, u.getEncPassword())) {
								sessionData.setUser(u);
							}
							break;
						}
					}
					if (sessionData.getUser() != null) {
						if (sessionData.getUser().getUserType() == UserType.USER) {
							getUI().getNavigator().addView(AppView.NAME, AppView.class);
							getUI().getNavigator().navigateTo(AppView.NAME);
						} else if (sessionData.getUser().getUserType() == UserType.ADMIN) {
							getUI().getNavigator().addView(AdminView.NAME, AdminView.class);
//							getUI().getNavigator().addView(AppView.NAME, AppView.class);
							getUI().getNavigator().navigateTo(AdminView.NAME);
						}
					} else {
						Notification.show("Invalid user/password", "", Notification.Type.WARNING_MESSAGE);
					}
				} else {
					Notification.show("Invalid user/password", "", Notification.Type.WARNING_MESSAGE);
					if (!userStr.equals("")) {
						password.setValue("");
						password.focus();
					} else {
						user.focus();
					}
				}
			}
		};
	}

}