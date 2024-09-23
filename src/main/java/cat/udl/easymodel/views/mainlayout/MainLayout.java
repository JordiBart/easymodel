package cat.udl.easymodel.views.mainlayout;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.NotificationType;
import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.vcomponent.common.PendingNotification;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.thread.NewUserVisitTaskRunnable;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.common.user.EditUserAccountDialog;
import cat.udl.easymodel.vcomponent.common.user.LoginDialog;
import cat.udl.easymodel.vcomponent.common.user.RegisterDialog;
import cat.udl.easymodel.views.admin.AdminView;
import cat.udl.easymodel.views.mainlayout.dialog.AboutAppDialog;
import cat.udl.easymodel.views.mainlayout.dialog.WelcomeDialog;
import cat.udl.easymodel.views.modelbuilder.ModelBuilderView;
import cat.udl.easymodel.views.modelselect.ModelSelectView;
import cat.udl.easymodel.views.simulationlauncher.SimulationLauncherView;
import cat.udl.easymodel.views.simulationresults.SimulationResultsView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
@PageTitle(SharedData.appName)
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    private SharedData sharedData;
    private SessionData sessionData;

    private SubMenu userSubMenu = null;
    private SubMenu toolsSubMenu = null;
//    private final Tabs menu;
    private H2 viewTitle;

    public MainLayout() {
        super();
        sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        sharedData = SharedData.getInstance();
        if (this.sessionData == null) {
            //this code will only run at first enter
            initSessionCode();
            if (sharedData.isDebug()) {
                sessionData.setWelcomeDialogClosed(true);
                try {
                    Model testModel = new Model(sessionData.getModels().get(2));
                    testModel.loadDB();
                    //testModel.setRepositoryType(RepositoryType.TEMP);
                    sessionData.setSelectedModel(testModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //create layout
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
        if (!sessionData.isWelcomeDialogClosed()) {
            WelcomeDialog dia = new WelcomeDialog();
            dia.addDetachListener(detachEvent -> sessionData.setWelcomeDialogClosed(true));
            dia.open();
        }
    }
    private void addHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSizeFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        HorizontalLayout spacer = new HorizontalLayout();

        MenuBar mb = getMenuBar();

        layout.add(toggle, viewTitle, spacer, mb);
        layout.expand(spacer);

        addToNavbar(true, layout);
    }

    private MenuBar getMenuBar() {
        MenuBar mb = new MenuBar();
        mb.setOpenOnHover(false);
        MenuItem optionsItem = mb.addItem(VaadinIcon.TOOLBOX.create());
        MenuItem userItem = mb.addItem(VaadinIcon.USER.create());
        toolsSubMenu = optionsItem.getSubMenu();
        userSubMenu = userItem.getSubMenu();
        updateMenuBarSubMenues();
        return mb;
    }

    private void addDrawerContent() {
//        H1 appName = new H1("EasyModel");
//        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Image imgLogo = new Image("img/easymodel-logo-120.png", SharedData.appName);
        imgLogo.setWidthFull();
        Header header = new Header(imgLogo);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("1. Model Select", ModelSelectView.class, LineAwesomeIcon.FILE.create()));
        nav.addItem(new SideNavItem("2. Model Builder", ModelBuilderView.class, LineAwesomeIcon.EDIT.create()));
        nav.addItem(new SideNavItem("3. Simulation Launcher", SimulationLauncherView.class, LineAwesomeIcon.ROCKET_SOLID.create()));
        nav.addItem(new SideNavItem("4. Simulation Results", SimulationResultsView.class, LineAwesomeIcon.FILE_ALT.create()));
        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        return layout;
    }
    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
        if (sessionData.getPendingNotification() != null) {
            ToolboxVaadin.showNotification(sessionData.getPendingNotification().getNotificationMessage(), sessionData.getPendingNotification().getPendingNotificationType());
            sessionData.setPendingNotification(null);
        }
    }

    private void initSessionCode() {
        //create new session data
        this.sessionData = new SessionData();
        VaadinSession.getCurrent().setAttribute("s", this.sessionData);
        this.sessionData.init();
        try {
            sharedData.getDbManager().open();
            sessionData.getModels().semiLoadDB();
            new Thread(new NewUserVisitTaskRunnable()).start();
        } catch (Exception e) {
            ToolboxVaadin.showErrorNotification(SharedData.dbError);
            System.err.println(e.getMessage());
        }
        //log ip
        String ip = VaadinSession.getCurrent().getBrowser().getAddress();
        sharedData.getVisitCounterRunnable().setVisitorIp(ip);
        new Thread(sharedData.getVisitCounterRunnable()).start();
    }

    private void updateMenuBarSubMenues() {
        // tools
        toolsSubMenu.removeAll();
        if (sessionData.isUserSet()) {
            if (sessionData.getUser().getUserType() == UserType.ADMIN)
                toolsSubMenu.addItem("Administrate", e -> {
                    getUI().ifPresent(ui -> ui.navigate(AdminView.class));
                });
        }
//        Anchor tutorialLink = new Anchor("tutorial","Tutorial");
//        tutorialLink.setTarget("_blank");
//        tutorialLink.getStyle().set("color", "#263445").set("text-decoration", "none").set("cursor", "default");
//        toolsSubMenu.addItem(tutorialLink);
        toolsSubMenu.addItem("Tutorial", e -> {
            UI.getCurrent().getPage().open("tutorial","_blank"); //requires popup permission
        });
        toolsSubMenu.addItem("About", e -> {
            new AboutAppDialog().open();
        });
        // user
        userSubMenu.removeAll();
        if (!sessionData.isUserSet()) {
            userSubMenu.addItem("Login", e -> {
                LoginDialog dia = new LoginDialog();
                dia.open();
            });
            userSubMenu.addItem("Register", e -> {
                RegisterDialog dia = new RegisterDialog();
                dia.open();
            });
        } else {
            userSubMenu.addItem("Settings", e -> {
                EditUserAccountDialog dia = new EditUserAccountDialog();
                dia.open();
            });
            userSubMenu.addItem("Logout", e -> {
                sessionData.unsetUser();
                sessionData.setPendingNotification(new PendingNotification("Logout success", NotificationType.SUCCESS));
                getUI().ifPresent(ui -> ui.getPage().reload());
            });
        }
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle().replaceAll(SharedData.appName + " \\| ", ""));
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
