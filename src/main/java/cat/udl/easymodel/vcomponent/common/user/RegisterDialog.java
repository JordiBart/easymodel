package cat.udl.easymodel.vcomponent.common.user;

import cat.udl.easymodel.logic.types.NotificationType;
import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.vcomponent.common.PendingNotification;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.common.ReCaptcha;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.VaadinSession;

import java.util.ArrayList;

public class RegisterDialog extends Dialog {
    private static final long serialVersionUID = 1L;

    private VerticalLayout windowVL;
    private SessionData sessionData;
    private SharedData sharedData;

    private TextField user;
    private PasswordField pass, pass2;
    private ReCaptcha captcha;
    private VerticalLayout reCaptchaVL;
    private ArrayList<User> allUsers;
    private User newUser = new User(null, "", "", UserType.USER);

    public RegisterDialog() {
        super();

        sharedData = SharedData.getInstance();
        this.allUsers = sharedData.getUsers();
        this.sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        this.setModal(true);
        this.setResizable(true);
        this.setDraggable(true);
        this.setWidth("400px");
        this.setHeight("460px");

        reCaptchaVL = new VerticalLayout();
        reCaptchaVL.setHeight("77px");
        reCaptchaVL.setWidth("200px");
        reCaptchaVL.setPadding(false);
        reCaptchaVL.setSpacing(false);

        VerticalLayout mainVL = new VerticalLayout();
        mainVL.setSpacing(false);
        mainVL.setPadding(false);
        mainVL.setWidth("100%");
        mainVL.setClassName("scroll");

        windowVL = new VerticalLayout();
        windowVL.setSpacing(true);
        windowVL.setPadding(false);
        windowVL.setSizeFull();
        windowVL.add(ToolboxVaadin.getDialogHeader(this, "Register New User Account", null), mainVL);
        windowVL.expand(mainVL);

        user = new TextField("Username");
        user.setWidth("100%");
        user.setRequiredIndicatorVisible(true);
        user.setTitle(ToolboxVaadin.usernameRegexInfo);
        user.setValueChangeMode(ValueChangeMode.ON_BLUR);
        user.addValueChangeListener(event -> {
            newUser.setName(event.getValue());
        });

        pass = new PasswordField("Password");
        pass.setWidth("100%");
        pass.setRequiredIndicatorVisible(true);
        pass.setValue("");
        pass.setTitle(ToolboxVaadin.passwordRegexInfo);
        pass.setValueChangeMode(ValueChangeMode.ON_BLUR);
        pass.addValueChangeListener(event -> {
            newUser.setPassForRegister(event.getValue());
        });

        pass2 = new PasswordField("Retype Password");
        pass2.setWidth("100%");
        pass2.setRequiredIndicatorVisible(true);
        pass2.setValue("");
        ComponentUtil.setData(pass2, "isOK", false);
        pass2.setTitle("Retype previous password");
        pass2.setValueChangeMode(ValueChangeMode.ON_BLUR);
        pass2.addValueChangeListener(event -> {
            newUser.setRetypePassForRegister(event.getValue());
        });

        reloadReCaptchaVL();

        mainVL.add(user, pass, pass2, reCaptchaVL, getRegisterButton());
        this.add(windowVL);
    }

    private void checkForm() throws Exception {
        newUser.validateForRegister(allUsers);
        if (!SharedData.getInstance().isDebug()) {
            if (!captcha.isValid()) {
                reloadReCaptchaVL();
                throw new Exception("Please complete reCAPTCHA");
            }
        }
    }

    private void reloadReCaptchaVL() {
        reCaptchaVL.removeAll();
        captcha = new ReCaptcha(sharedData.getProperties().getProperty("reCaptcha-public-key"), sharedData.getProperties().getProperty("reCaptcha-private-key"));
        reCaptchaVL.add(captcha);
    }

    private Component getRegisterButton() {
        Button btn = new Button("Register");
        btn.setWidth("100%");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.addClickListener(event -> {
            try {
                checkForm();
                try {
                    newUser.saveDB();
                    allUsers.add(newUser);
                    sessionData.setUser(newUser);
                    sessionData.setPendingNotification(new PendingNotification("User Registration Success", NotificationType.SUCCESS));
                    getUI().ifPresent(ui -> ui.getPage().reload());
                } catch (Exception sqlE) {
                    sqlE.printStackTrace();
                    ToolboxVaadin.showErrorNotification(SharedData.dbError);
                }
            } catch (Exception e) {
                ToolboxVaadin.showWarningNotification(e.getMessage());
            }
        });
        return btn;
    }
}
