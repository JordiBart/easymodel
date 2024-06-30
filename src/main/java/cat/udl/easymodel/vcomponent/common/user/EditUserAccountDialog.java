package cat.udl.easymodel.vcomponent.common.user;

import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.VaadinSession;

public class EditUserAccountDialog extends Dialog {
    private SessionData sessionData;
    private SharedData sharedData;

    private User user;
    private PasswordField currentPass, pass, pass2;

    public EditUserAccountDialog() {
        super();

        sharedData = SharedData.getInstance();
        this.sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        user = this.sessionData.getUser();

        this.setModal(true);
        this.setResizable(false);
        this.setDraggable(true);
        this.setWidth("400px");
        this.setHeight("440px");

        VerticalLayout mainVL = new VerticalLayout();
        mainVL.setSpacing(false);
        mainVL.setPadding(false);
        mainVL.setWidth("100%");
        mainVL.setClassName("scroll");

        VerticalLayout windowVL = new VerticalLayout();
        windowVL.setSpacing(true);
        windowVL.setPadding(false);
        windowVL.setSizeFull();
        windowVL.add(ToolboxVaadin.getDialogHeader(this, "User Account Settings", null), mainVL);

        Span userLabel = new Span("Account Username: " + sessionData.getUser().getName());

        currentPass = new PasswordField("Type your current password");
        currentPass.setWidth("100%");
        currentPass.setRequiredIndicatorVisible(true);
        currentPass.setValueChangeMode(ValueChangeMode.ON_BLUR);
        currentPass.setTitle(ToolboxVaadin.passwordRegexInfo);

        pass = new PasswordField("New password");
        pass.setWidth("100%");
        pass.setRequiredIndicatorVisible(true);
        pass.setValueChangeMode(ValueChangeMode.ON_BLUR);
        pass.setTitle(ToolboxVaadin.passwordRegexInfo);
        pass.addValueChangeListener(event -> {
            user.setPassForRegister(event.getValue());
        });

        pass2 = new PasswordField("Retype new password");
        pass2.setWidth("100%");
        pass2.setRequiredIndicatorVisible(true);
        pass2.setValueChangeMode(ValueChangeMode.ON_BLUR);
        pass2.setTitle("Retype previous password");
        pass2.addValueChangeListener(event -> {
            user.setRetypePassForRegister(event.getValue());
        });
        Span changePasswordSpan = new Span("Change Password");
        changePasswordSpan.getStyle().setFontWeight(600);
        mainVL.add(userLabel, changePasswordSpan, currentPass, pass, pass2, getSaveButton());

        this.add(windowVL);
        currentPass.focus();
    }

    private void checkForm() throws Exception {
        if (!user.matchLogin(user.getName(), currentPass.getValue()))
            throw new Exception("Incorrect current password");
        user.validateForRegister(null);
    }

    private Button getSaveButton() {
        Button btn = new Button("Save");
        btn.setWidth("100%");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.addClickListener(event -> {
            try {
                checkForm();
                try {
                    user.saveDB();
                    close();
                    ToolboxVaadin.showSuccessNotification("User Account updated");
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
