package cat.udl.easymodel.views.tutorial;

import cat.udl.easymodel.main.SharedData;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.HashMap;

@Route("tutorial")
//@Theme(themeFolder = "easymodel")
//@CssImport("./themes/easymodel/tutorial.css")
@PageTitle(SharedData.appName+" | Tutorial")
public class Tutorial extends VerticalLayout {
    private HorizontalLayout slidesHL;
    private Span titleLabel;
    private HorizontalLayout centerHL;
    private HorizontalLayout progressHL;
    private int curSlide = 1;
    private int totalSlide = 8;
    private HashMap<Integer, String> titleMap = new HashMap<>();

    public Tutorial() {
        int i = 1;
        titleMap.put(i++, "");
        titleMap.put(i++, "Create or select a model");
        titleMap.put(i++, "Define the model reactions and rates I");
        titleMap.put(i++, "Define the model reactions and rates II");
        titleMap.put(i++, "Configure Deterministic Simulation I");
        titleMap.put(i++, "Configure Deterministic Simulation II");
        titleMap.put(i++, "Configure Stochastic Simulation");
        titleMap.put(i++, "Simulation results");
        titleMap.put(i++, "");

        addShortcuts();

        this.setClassName("tutorialView");
        this.setSizeFull();
        this.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        this.setPadding(false);
        this.setSpacing(false);

        VerticalLayout vl = new VerticalLayout();
        vl.setClassName("tutorialMain");
        vl.setSizeFull();
        vl.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        vl.setPadding(false);
        vl.setSpacing(false);

        HorizontalLayout headerHL = new HorizontalLayout();
        headerHL.setWidth("100%");
        headerHL.setHeight("60px");
        headerHL.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        headerHL.setPadding(false);
        headerHL.setSpacing(false);
        Image emLogo = new Image("img/easymodel-logo-120.png", SharedData.appName);
        emLogo.setHeight("60px");
        HorizontalLayout logoL = new HorizontalLayout();
        logoL.setHeight("60px");
        logoL.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        logoL.setPadding(false);
        logoL.add(emLogo);
        VerticalLayout titleVL = new VerticalLayout();
        titleVL.setSizeFull();
        titleVL.setPadding(false);
        titleVL.setSpacing(false);
        titleVL.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        titleLabel = new Span();
        titleLabel.setClassName("tutorialTitle");
        titleVL.add(titleLabel);
        headerHL.add(emLogo, titleVL, getSkipBtn());
        headerHL.expand(titleVL);
        //vl.add(headerHL);

        slidesHL = new HorizontalLayout();
        slidesHL.setSizeFull();
        slidesHL.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        slidesHL.setPadding(false);
        slidesHL.setSpacing(false);
        centerHL = new HorizontalLayout();
        centerHL.setSpacing(false);
        centerHL.setPadding(false);
        slidesHL.add(getPrevBtn(), centerHL, getNextBtn());
        slidesHL.expand(centerHL);
        vl.add(slidesHL);

        HorizontalLayout footerHL = new HorizontalLayout();
        footerHL.setWidth("100%");
        footerHL.setHeight("5px");
        footerHL.setClassName("tutorialFooter");
        footerHL.setPadding(false);
        footerHL.setSpacing(false);
        progressHL = new HorizontalLayout();
        progressHL.setSpacing(false);
        progressHL.setPadding(false);
        progressHL.setClassName("tutorialProgress");
        progressHL.setWidth("0%");
        progressHL.setHeight("100%");
        footerHL.add(progressHL);
        vl.add(footerHL);

        vl.expand(slidesHL);
        this.add(vl);
        updateSlide();
    }

    private VerticalLayout getSkipBtn() {
        VerticalLayout vl = new VerticalLayout();
        vl.setPadding(false);
        vl.setWidth("152px");
        vl.setHeight("60px");
        return vl;
    }

    private Component getNextBtn() {
        Button btn = new Button();
        btn.getElement().setProperty("innerHTML", "&#10095;");
        btn.setClassName("tutorialNext");
        btn.setWidth("100px");
        btn.setHeight("100%");
        btn.addClickListener(ev->{
                next();
        });
        return btn;
    }

    private Component getPrevBtn() {
        Button btn = new Button();
        btn.getElement().setProperty("innerHTML", "&#10094;");
        btn.setClassName("tutorialPrev");
        btn.setWidth("100px");
        btn.setHeight("100%");//
        btn.addClickListener(ev->{
                prev();
        });
        return btn;
    }

    private void prev() {
        if (curSlide > 1) {
            curSlide--;
            updateSlide();
        }
    }

    private void next() {
        if (curSlide < totalSlide) {
            curSlide++;
            updateSlide();
        }
    }

    private void updateSlide() {
        titleLabel.setText(titleMap.get(curSlide));

        centerHL.removeAll();
        Span numSlideLabel = new Span(String.valueOf(curSlide));
        numSlideLabel.setClassName("tutorialNumSlide");
        centerHL.add(numSlideLabel);

        slidesHL.setClassName("tutorialSlides tutorial" + curSlide);
        progressHL.setWidth(String.format("%.0f", ((double) curSlide / totalSlide) * 100) + "%");
    }

    private void addShortcuts() {
        UI.getCurrent().addShortcutListener(this::prev, Key.ARROW_LEFT);
        UI.getCurrent().addShortcutListener(this::next, Key.ARROW_RIGHT);
        UI.getCurrent().addShortcutListener(this::next, Key.SPACE);
    }
}
