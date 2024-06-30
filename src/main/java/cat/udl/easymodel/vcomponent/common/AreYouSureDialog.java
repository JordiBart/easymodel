package cat.udl.easymodel.vcomponent.common;

import cat.udl.easymodel.utils.ToolboxVaadin;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class AreYouSureDialog extends Dialog {
    private boolean isAnswerYes = false;

    public AreYouSureDialog(String title, String message) {
        super();

        this.setWidth("600px");
        this.setHeight("300px");
        this.setModal(true);
        this.setDraggable(true);

        VerticalLayout windowVL = new VerticalLayout();
        windowVL.setSpacing(true);
        windowVL.setPadding(false);
        windowVL.setSizeFull();

        Div mainComponent = new Div();
        mainComponent.setClassName("scroll");
        mainComponent.setText(message);

        windowVL.add(ToolboxVaadin.getDialogHeader(this, title, null));
        windowVL.addAndExpand(mainComponent);
        windowVL.add(getButtonsHL());

        this.add(windowVL);
    }

    private HorizontalLayout getButtonsHL() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.setSpacing(true);
        HorizontalLayout spacer = new HorizontalLayout();
        hl.add(spacer, getYesButton(), getNoButton());
        hl.expand(spacer);
        return hl;
    }

    private Button getNoButton() {
        Button button = new Button("No");
        button.setWidth("100px");
        button.addClickListener(event -> {
            isAnswerYes = false;
            close();
        });
        return button;
    }

    private Button getYesButton() {
        Button button = new Button("Yes");
        button.setWidth("100px");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(event -> {
            isAnswerYes = true;
            close();
        });
        return button;
    }

    public boolean isAnswerYes() {
        return isAnswerYes;
    }
}
