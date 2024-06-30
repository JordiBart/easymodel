package cat.udl.easymodel.views.mainlayout.dialog;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class WelcomeDialog extends Dialog {
    public WelcomeDialog(){
        super();

        this.setHeight("560px");
        this.setWidth("500px");
        this.setModal(true);

        HorizontalLayout hl1 = ToolboxVaadin.getDialogHeader(this, "Welcome to "+SharedData.fullAppName+"!",null);

        VerticalLayout contentVL = new VerticalLayout();
        contentVL.setPadding(false);
        contentVL.setSpacing(true);
        contentVL.setSizeFull();

        Image emLogo = new Image("img/easymodel-logo-236.png", SharedData.appName);
        emLogo.setSizeFull();
        VerticalLayout logoVL = new VerticalLayout();
        logoVL.setPadding(false);
        logoVL.setSpacing(false);
        logoVL.setWidth("100%");
        logoVL.add(emLogo);

        Span lbl = new Span(SharedData.appName+" is a user-friendly web server for model building, simulation, and analysis in systems biology. Calculus core powered by Mathematica.");
        lbl.getStyle().set("word-wrap", "break-word");
        lbl.getStyle().set("text-align", "justify");
        lbl.getStyle().set("font-style", "italic");

        Anchor link = new Anchor("tutorial","Open Tutorial in new tab");
        link.setWidth("270px");
        link.setTarget("_blank");
        link.setClassName("likeButton");
        link.getStyle().set("font-weight", "bold");

        Span lbl2 = new Span
                ("First-time users are advised to complete the tutorial before using "+SharedData.appName+".");
        lbl2.getStyle().set("word-wrap", "break-word");
        lbl2.getStyle().set("text-align", "justify");
        lbl2.getStyle().set("font-weight", "bold");

        Button startBtn = new Button("Dismiss");
        startBtn.setWidth("270px");
        startBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        startBtn.addClickListener(ev->{
           close();
        });

        contentVL.add(logoVL,lbl,lbl2,link,startBtn);
        contentVL.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER,link);
        contentVL.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER,startBtn);

        VerticalLayout diaVL = new VerticalLayout();
        diaVL.setPadding(false);
        diaVL.setSpacing(true);
        diaVL.setSizeFull();
        diaVL.add(hl1,contentVL);

        this.add(diaVL);
    }
}
