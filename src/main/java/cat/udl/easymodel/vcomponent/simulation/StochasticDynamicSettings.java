package cat.udl.easymodel.vcomponent.simulation;

import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.types.StochasticGradeType;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import cat.udl.easymodel.views.simulationlauncher.SimConfigToComponent;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class StochasticDynamicSettings extends VerticalLayout {
    private SimConfig simConfig;
    private StochasticGradeType stochasticGradeType;
    public StochasticDynamicSettings(SimConfig simConfig, StochasticGradeType stochasticGradeType) {
        super();
        this.simConfig=simConfig;
        this.stochasticGradeType=stochasticGradeType;

        this.setWidth("100%");
        this.setSpacing(false);
        this.setPadding(true);
        this.getElement().getStyle().set("border", "2px solid rgb(211, 211, 211)");
        this.getElement().getStyle().set("border-radius", "5px");

        Accordion accDyn = new Accordion();
        accDyn.add("Main Settings", getMainSettingsHL());

        Span title = new Span("Dynamic (Stochastic) Simulation Settings");
        title.getStyle().set("font-weight", "bold");
        this.add(title);
        if (stochasticGradeType != StochasticGradeType.UNCHECKED) {
            Span notice = new Span();
            notice.getStyle().setWidth("500px");
            if (stochasticGradeType == StochasticGradeType.NOT_COMPATIBLE) {
                notice.setText("Stochastic simulation is not available for this model.");
                notice.getStyle().set("color", "red");
            }else{
                notice.setText("Stochastic simulation is available for this model. Recommended method is preselected.");
                notice.getStyle().set("color", "blue");
            }
            this.add(notice);
        }
        this.add(accDyn);
    }

    private HorizontalLayout getMainSettingsHL() {
        VerticalLayout leftVL = new VerticalLayout();
        leftVL.setWidth("100%");
        leftVL.setSpacing(false);
        leftVL.setPadding(false);

        leftVL.add(SimConfigToComponent.convert(simConfig.getStochastic().get("Ti"),null));
        leftVL.add(SimConfigToComponent.convert(simConfig.getStochastic().get("Tf"),null));
        leftVL.add(SimConfigToComponent.convert(simConfig.getStochastic().get("Iterations"),null));
        leftVL.add(SimConfigToComponent.convert(simConfig.getStochastic().get("CellSize"),null));
        leftVL.add(SimConfigToComponent.convert(simConfig.getStochastic().get("Method"),null));
        /////////////
        VerticalLayout rightVL = new VerticalLayout();
        rightVL.setWidth("50px");
        rightVL.setSpacing(false);
        rightVL.setPadding(false);
        Button iBtn = new InfoDialogButton("Information", "Stochastic simulation provides a more realistic time course behaviour than deterministic simulation at the expense of computational time.\n" +
                ""
                , "800px", "400px");
        rightVL.setDefaultHorizontalComponentAlignment(Alignment.END);
        rightVL.add(iBtn);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth("100%");
        hl.setSpacing(true);
        hl.setPadding(true);
        hl.add(leftVL, rightVL);
        hl.expand(leftVL);
        return hl;
    }
}
