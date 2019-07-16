package cat.udl.easymodel.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.ThemeResource;
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
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import cat.udl.easymodel.controller.BioModelsLogs;
import cat.udl.easymodel.controller.SimulationCtrl;
import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.Formulas;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Models;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.sbml.SBMLMan;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.login.ContactWindow;
import cat.udl.easymodel.vcomponent.login.RegisterWindow;

public class LoginView extends CustomComponent implements View {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "login";

	private TextField user;
	private PasswordField password;
	private Button loginBtn;
	private SessionData sessionData;
	private SharedData sharedData = SharedData.getInstance();

	public LoginView() {
	}

	@Override
	public void enter(ViewChangeEvent event) {
		this.sessionData = (SessionData) UI.getCurrent().getData();
//		HorizontalLayout titleHL = new HorizontalLayout();
//		titleHL.setMargin(false);
//		Label loginLabel = new Label("Welcome to " + SharedData.appName + ", please log in");
//		titleHL.addComponents(loginLabel);
//		titleHL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		FileResource resource = new FileResource(
				new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()
						+ "/VAADIN/themes/easymodel/img/easymodel-logo-120.png"));
		Image emLogo = new Image(null, resource);

		Button guestBtn = new Button("Enter as Guest");
		guestBtn.setStyleName("enterGuest");
		guestBtn.setWidth("100%");
		guestBtn.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				accessAsGuest();
			}
		});

		user = new TextField();
		user.setWidth("100%");
		user.setPlaceholder("Username");

		password = new PasswordField();
		password.setWidth("100%");
		password.setPlaceholder("Password");

		loginBtn = new Button("Log In", getLoginButtonClickListener());
		loginBtn.setWidth("100%");
		loginBtn.setClickShortcut(KeyCode.ENTER);

		Button regBtn = new Button("Create User Account");
		regBtn.setWidth("100%");
		regBtn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				RegisterWindow registerWindow = getRegisterWindow();
				UI.getCurrent().addWindow(registerWindow);
			}
		});

		VerticalLayout pushVL = new VerticalLayout();

		VerticalLayout loginVL = new VerticalLayout();
		loginVL.setDefaultComponentAlignment(Alignment.TOP_CENTER);
		loginVL.setSpacing(true);
		loginVL.setMargin(true);
		loginVL.setWidth("400px");
		loginVL.setHeight("450px");
		loginVL.setStyleName("login");
		loginVL.addComponents(emLogo, guestBtn, ToolboxVaadin.getHR(), user, password, loginBtn, regBtn, pushVL);
		loginVL.setExpandRatio(pushVL, 1.0f);

		VerticalLayout footerVL = getFooterVL();

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		viewLayout.setSizeFull();
		viewLayout.setStyleName("loginView");
		viewLayout.setSpacing(false);
		viewLayout.setMargin(false);
		viewLayout.addComponents(loginVL, footerVL);
		viewLayout.setExpandRatio(loginVL, 1f);

		setCompositionRoot(viewLayout);
		setSizeFull();

		user.focus();

		if (sharedData.isDebug()) {
			// XXX
			// accessAsGuest();
		}
	}

	private void accessAsGuest() {
		if (sharedData.getGuestUser() != null) {
			sessionData.setUser(sharedData.getGuestUser());
			try {
				checkOkToChangeView();
				getUI().getNavigator().addView(TutorialView.NAME, TutorialView.class);
				getUI().getNavigator().navigateTo(TutorialView.NAME);
			} catch (Exception e) {
				Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
			}
		}
	}

	private RegisterWindow getRegisterWindow() {
		RegisterWindow regWindow = new RegisterWindow(sharedData.getUsers());
		regWindow.addCloseListener(new CloseListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void windowClose(CloseEvent e) {
				if ((boolean) e.getWindow().getData()) {
					try {
						checkOkToChangeView();
						getUI().getNavigator().addView(AppView.NAME, AppView.class);
						getUI().getNavigator().navigateTo(AppView.NAME);
					} catch (Exception e1) {
						Notification.show(e1.getMessage(), Type.WARNING_MESSAGE);
					}
				}
			}

		});
		return regWindow;
	}

	private void checkOkToChangeView() throws Exception {
		if (sessionData.getUser() == null)
			throw new Exception("Invalid user/password");
	}

	private ClickListener getLoginButtonClickListener() {
		return new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				String userStr = user.getValue();
				String passwordStr = password.getValue();
				if (!userStr.matches(ToolboxVaadin.usernameCharRegex + "*")
						|| !passwordStr.matches(ToolboxVaadin.passwordCharRegex + "*")) {
					Notification.show("Username/Password contains invalid character/s", Type.WARNING_MESSAGE);
					return;
				}
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
				for (User u : sharedData.getUsers()) {
					if (u.matchLogin(userStr, passwordStr)) {
						sessionData.setUser(u);
						break;
					}
				}
				try {
					checkOkToChangeView();
					if (sessionData.getUser().getUserType() == UserType.USER) {
						getUI().getNavigator().addView(AppView.NAME, AppView.class);
						getUI().getNavigator().navigateTo(AppView.NAME);
					} else if (sessionData.getUser().getUserType() == UserType.ADMIN) {
						getUI().getNavigator().addView(AdminView.NAME, AdminView.class);
						getUI().getNavigator().navigateTo(AdminView.NAME);
					}
				} catch (Exception e) {
					Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
					if (!userStr.equals("")) {
						password.focus();
					} else {
						user.focus();
					}
				}
			}
		};

	}

	private VerticalLayout getFooterVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		vl.setMargin(false);
		vl.setSpacing(true);
		vl.setStyleName("loginFooter");
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

		hl.addComponents(getUdlLink(), getWebMathematicaLink(), getSBMLLink(), getGitHubLink(), getContactButton(), getCounterVL());
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
		link.setStyleName("loginWebMLogo");
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
		btn.setStyleName("contact");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().addWindow(new ContactWindow());
			}
		});
		return btn;
	}

	private Component getMigrateToDBOldFormulasButton() {
		Button btn = new Button("Migrate");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					Formulas newGenericRates = new Formulas(FormulaType.GENERIC);
					ArrayList<Formula> allFormulas1 = sharedData.getDbManager().getAllFormulas();
//					Formulas allFormulas2 = new Formulas();
//					for (Formula f : allFormulas1) {
//						allFormulas2.addFormula(f);
//					}
					newGenericRates.mergeGenericFormulasFrom(allFormulas1);
					newGenericRates.saveDB();
				} catch (SQLException e) {
					e.printStackTrace();
				}
//				if(true)
//				return;
				// MODELS
				try {
					Models models = new Models();
					models.semiLoadDB();
					for (Model m : models) {
						m.loadDB();
						ArrayList<Formula> modelFs = new ArrayList<Formula>();
						for (Reaction r : m) {
							if (r.getId() != null) {
								Connection con = sharedData.getDbManager().getCon();
								PreparedStatement ps = null, ps2;
								String query = "SELECT f.id, f.id_model, f.name, f.formula, f.onesubstrateonly, f.noproducts, f.onemodifieronly, f.formulatype FROM reaction r, formula f WHERE r.id=? AND r.id_formula=f.id";
								ps = con.prepareStatement(query);
								int p = 1;
								ps.setInt(p++, r.getId());
								ResultSet rs = ps.executeQuery();
								if (rs.next()) {
									Formula f = new Formula(rs.getString("name"), rs.getString("formula"),
											FormulaType.fromInt(rs.getInt("formulatype")), m);
									f.setOneSubstrateOnly(Utils.intToBool(rs.getInt("onesubstrateonly")));
									f.setNoProducts(Utils.intToBool(rs.getInt("noproducts")));
									f.setOneModifierOnly(Utils.intToBool(rs.getInt("onemodifieronly")));
									f.setFormulaType(FormulaType.MODEL);
									f.setDirty(true);

									ps2 = con.prepareStatement(
											"SELECT `id`, `id_formula`, `genparam`, `formulavaluetype` FROM `formulagenparam` WHERE id_formula=?");
									p = 1;
									ps2.setInt(p++, rs.getInt("f.id"));
									ResultSet rs2 = ps2.executeQuery();
									while (rs2.next()) {
										// System.out.println("yeee");
										f.getGenericParameters().put(rs2.getString("genparam"),
												FormulaValueType.fromInt(rs2.getInt("formulavaluetype")));
									}
									rs2.close();
									ps2.close();

									Formula sameFormula = null;
									for (Formula f2 : modelFs) {
										if (f2.getNameRaw().equals(f.getNameRaw()))
											sameFormula = f2;
									}
									if (sameFormula == null) {
										f.saveDB();
										modelFs.add(f);
									} else
										f = sameFormula;
									ps2 = con.prepareStatement("UPDATE reaction SET `id_formula`=? WHERE id=?");
									p = 1;
									ps2.setInt(p++, f.getId());
									ps2.setInt(p++, r.getId());
									ps2.executeUpdate();
									ps2.close();
								}
								rs.close();
								ps.close();
							}
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				p.p("done");
			}
		});
		return btn;
	}

	private Button getImportBiomodelsSBMLButton() {
		Button btn = new Button("Biomodels SBML import");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				p.p("Batch start!");
				sessionData.setBioModelsLogs(new BioModelsLogs());
				SimulationCtrl simCtrl = new SimulationCtrl(sessionData);
				int totalLoadOK = 0, totalLoadKO = 0;
				sessionData.setUser(sharedData.getUsers().getUserByName("test"));
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
		return btn;
	}
}