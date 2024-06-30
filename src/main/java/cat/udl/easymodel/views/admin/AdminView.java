package cat.udl.easymodel.views.admin;

import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.views.mainlayout.MainLayout;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@PageTitle(SharedData.appName + " | Administrate")
@Route(value = "administrate", layout = MainLayout.class)
public class AdminView extends VerticalLayout {
    private SessionData sessionData;
    private SharedData sharedData;
    private VerticalLayout mainVL;

    public AdminView() {
        super();
        sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        sharedData = SharedData.getInstance();
        this.setSizeFull();
        if (!(sessionData.isUserSet() && sessionData.getUser().getUserType() == UserType.ADMIN)) {
            this.add(new Span("Access denied"));
            return;
        }
        MenuBar menuBar = new MenuBar();
        MenuItem usersItem = menuBar.addItem("Users", ev -> updateMainVL(new UsersAdminVL()));
        MenuItem modelsItem = menuBar.addItem("Models", ev -> updateMainVL(new ModelsAdminVL()));
        MenuItem formulasItem = menuBar.addItem("Formulas", ev -> updateMainVL(new FormulasAdminVL()));

        mainVL = new VerticalLayout();
        mainVL.setPadding(false);
        mainVL.setSpacing(false);
        mainVL.setClassName("scroll");
        mainVL.setSizeFull();

        this.setSizeFull();
        this.setPadding(true);
        this.setSpacing(true);
        this.add(menuBar, mainVL);
        this.expand(mainVL);
        updateMainVL(new UsersAdminVL());
    }

    private void updateMainVL(VerticalLayout vl) {
        mainVL.removeAll();
        mainVL.add(vl);
    }
}
