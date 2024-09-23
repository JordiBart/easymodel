package cat.udl.easymodel.views.admin;


import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.logic.user.Users;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.P;
import cat.udl.easymodel.utils.ToolboxVaadin;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.VaadinSession;

public class UsersAdminVL extends VerticalLayout {
    private SessionData sessionData = null;
    private SharedData sharedData = SharedData.getInstance();

    private VerticalLayout mainVL = null;
    private VerticalLayout optionButtonsVL = null;
    private VerticalLayout gridVL = null;
    private Grid<User> grid;

    private Users usersCopy = null;

    public UsersAdminVL() {
        super();
        this.sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");

        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(true);

        optionButtonsVL = new VerticalLayout();
        optionButtonsVL.setPadding(false);
        optionButtonsVL.setWidthFull();
        optionButtonsVL.add(getBottomButtonsLayout());

        mainVL = new VerticalLayout();
        mainVL.setSpacing(true);
        mainVL.setPadding(false);
        mainVL.setWidth("100%");
        mainVL.setHeight("100%");
        updateDisplayContent();

        this.add(mainVL);
        this.setHorizontalComponentAlignment(Alignment.CENTER);
    }

    private void updateDisplayContent() {
        mainVL.removeAll();

        usersCopy = new Users(sharedData.getUsers());
//		for (User u:usersCopy)
//			p.p(u.getName());
        gridVL = getGridVL();
        mainVL.add(gridVL, optionButtonsVL);
        mainVL.expand(gridVL);
    }

    private VerticalLayout getGridVL() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);
        vl.setPadding(false);
        vl.setSizeFull();
        grid = new Grid<User>();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setSizeFull();
        grid.addColumn(new ComponentRenderer<>(u -> {
            return getTF("name", "New username", u.getName(), u);
        })).setHeader("Username").setResizable(true).setFlexGrow(1);
        grid.addColumn(new ComponentRenderer<>(u -> getTF("password", "New password", "", u))).setHeader("Password").setResizable(true).setFlexGrow(1);
        grid.addColumn(new ComponentRenderer<>(u -> getCB("usertype", (u.getUserType() == UserType.ADMIN), u))).setHeader("Admin").setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(u -> getCB("delete", u.isDBDelete(), u))).setHeader("Delete").setFlexGrow(0);
        grid.setItems(usersCopy);
        vl.add(grid);
        return vl;
    }

    private TextField getTF(String field, String prompt, String val, User u) {
        TextField tf = new TextField();
        tf.setWidth("100%");
        tf.setPlaceholder(prompt);
        tf.setValue(val);
        tf.setValueChangeMode(ValueChangeMode.ON_BLUR);
        tf.addValueChangeListener(event -> {
            String newVal = event.getValue();
            if (field.equals("name"))
                u.setName(newVal);
            else if (field.equals("password"))
                u.setPassForRegister(newVal);
        });
        return tf;
    }

    private Checkbox getCB(String field, boolean val, User u) {
        Checkbox cb = new Checkbox();
        cb.setWidth("50px");
        cb.setValue(val);
        cb.addValueChangeListener(event -> {
            Boolean newVal = event.getValue();
            if (field.equals("usertype"))
                u.setUserType(newVal ? UserType.ADMIN : UserType.USER);
            else if (field.equals("delete"))
                u.setDBDelete(newVal);
        });
        return cb;
    }

    private HorizontalLayout getBottomButtonsLayout() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(false);
        hl.setPadding(false);
        hl.setWidthFull();
        Button addUserBtn = new Button("Add New User");
        addUserBtn.setIcon(VaadinIcon.PLUS.create());
        addUserBtn.addClickListener(ev->{
            User u = new User(null, "", null, UserType.USER);
            usersCopy.add(u);
            grid.setItems(usersCopy);
            grid.scrollToEnd();
        });
        Button saveBtn = new Button("Save Changes");
        saveBtn.setIcon(VaadinIcon.HARDDRIVE.create());
        saveBtn.addClickListener(ev->{
            try {
                for (User u : usersCopy) {
                    if (!u.isDBDelete()) {
                        try {
                            u.validateForAdmin();
                        } catch (Exception e) {
                            throw new Exception("User \"" + u.getName() + "\" contains errors:\n" + e.getMessage());
                        }
                        for (User u2 : usersCopy)
                            if (u != u2 && u.getName().equals(u2.getName()))
                                throw new Exception("Username \"" + u.getName() + "\" duplicated");
                    }
                }
                sharedData.getUsers().updateFrom(usersCopy);
                try {
                    sharedData.getUsers().saveDBAdmin();
                } catch (Exception e) {
                    ToolboxVaadin.showErrorNotification("Database save error");
                }
                ToolboxVaadin.showSuccessNotification("Data saved");
                updateDisplayContent();
            } catch (Exception ce) {
                ToolboxVaadin.showWarningNotification(ce.getMessage());
//					ce.printStackTrace();
            }
        });
        VerticalLayout spacer = new VerticalLayout();
        hl.add(addUserBtn,spacer,saveBtn);
        hl.expand(spacer);
        return hl;
    }
}
