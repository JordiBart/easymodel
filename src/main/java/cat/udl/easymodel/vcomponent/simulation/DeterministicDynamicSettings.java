package cat.udl.easymodel.vcomponent.simulation;

import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.simconfig.SimParamScanConfig;
import cat.udl.easymodel.logic.types.ParamScanType;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.common.AreYouSureDialog;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import cat.udl.easymodel.views.simulationlauncher.dialog.ParamScanDialog;
import cat.udl.easymodel.views.simulationlauncher.SimConfigToComponent;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class DeterministicDynamicSettings extends VerticalLayout {
    private SimConfig simConfig;

    public DeterministicDynamicSettings(SimConfig simConfig) {
        super();
        this.simConfig=simConfig;

        this.setWidth("100%");
        this.setSpacing(false);
        this.setPadding(true);
        this.getElement().getStyle().set("border", "2px solid rgb(211, 211, 211)");
        this.getElement().getStyle().set("border-radius", "5px");

        Accordion accDyn = new Accordion();
        accDyn.add("Main Settings", getDynMainSettingsHL());
        accDyn.add("Analysis", getDynDetAnalysisHL());
        accDyn.add("Plot Views", new SimPlotViewsLayout());
        accDyn.add("Parameter Scan", getParameterScanHL(simConfig.getDynamic_ParameterScan()));
//        accDyn.setSizeFull();

        Span title = new Span("Dynamic (Deterministic) Simulation Settings");
        title.getStyle().set("font-weight", "bold");
        this.add(title,accDyn);
    }

    private HorizontalLayout getDynMainSettingsHL() {
        VerticalLayout leftVL = new VerticalLayout();
        leftVL.setWidth("100%");
        leftVL.setSpacing(false);
        leftVL.setPadding(false);

        leftVL.add(SimConfigToComponent.convert(simConfig.getDynamic().get("Ti"),null));
        leftVL.add(SimConfigToComponent.convert(simConfig.getDynamic().get("Tf"),null));
        leftVL.add(SimConfigToComponent.convert(simConfig.getDynamic().get("TStep"),null));
        /////////////
        VerticalLayout rightVL = new VerticalLayout();
        rightVL.setWidth("50px");
        rightVL.setSpacing(false);
        rightVL.setPadding(false);
        Button iBtn = new InfoDialogButton("Information", "All values must be positive.\r\n"
                + "Step size is the time interval between simulation time points.", "800px", "400px");
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

    private HorizontalLayout getDynDetAnalysisHL() {
        VerticalLayout leftVL = new VerticalLayout();
        leftVL.setWidth("400px");
        leftVL.setSpacing(false);
        leftVL.setPadding(false);

        leftVL.add(SimConfigToComponent.convert(simConfig.getDynamic().get("Gains"),null));
        leftVL.add(SimConfigToComponent.convert(simConfig.getDynamic().get("Sensitivities"),null));
        /////////////
        VerticalLayout rightVL = new VerticalLayout();
        rightVL.setWidth("50px");
        rightVL.setHeight("100%");
        rightVL.setSpacing(false);
        rightVL.setPadding(false);
        Button iBtn = new InfoDialogButton("Information", "Gains analysis: system sensitivity against changes in independent variables (linear approximation).\r\n"
                + "Sensitivities analysis: system sensitivity against changes in the rate parameters (linear approximation).", "800px", "400px");
        rightVL.setDefaultHorizontalComponentAlignment(Alignment.END);
        rightVL.add(iBtn);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        hl.setSpacing(true);
        hl.setPadding(true);
        hl.add(leftVL, rightVL);
        hl.expand(leftVL);
        return hl;
    }

    private HorizontalLayout getParameterScanHL(SimParamScanConfig simParamScanConfig) {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSizeFull();
        hl.setSpacing(true);
        hl.setPadding(true);

        VerticalLayout leftVL = new VerticalLayout();
        leftVL.setWidth("100%");
        leftVL.setSpacing(false);
        leftVL.setPadding(false);
        Button paramBtn = new Button("Select Parameters");
        paramBtn.setWidth("100%");
        paramBtn.addClickListener(event-> {
            ParamScanDialog dia = new ParamScanDialog(simParamScanConfig, ParamScanType.PARAMETER);
            try {
                dia.checkToShow();
                dia.open();
            } catch (Exception e) {
                ToolboxVaadin.showWarningNotification("Model has no parameters");
            }
        });
        Button indVarsBtn = new Button("Select Independent Variables");
        indVarsBtn.setWidth("100%");
        indVarsBtn.addClickListener(event-> {
            ParamScanDialog dia = new ParamScanDialog(simParamScanConfig, ParamScanType.IND_VAR);
            try {
                dia.checkToShow();
                dia.open();
            } catch (Exception e) {
                ToolboxVaadin.showWarningNotification("Model has no independent variables");
            }
        });
        Button resetBtn = new Button("Reset Selection");
        resetBtn.setWidth("100%");
        resetBtn.addClickListener(event-> {
            AreYouSureDialog win = new AreYouSureDialog("Confirmation",
                    "Are you sure reset Parameter Scan configuration?");
            win.addDetachListener(ev2 -> {
                if (win.isAnswerYes()) {
                    simParamScanConfig.reset();
                }
            });
            win.open();
        });
        leftVL.add(paramBtn, indVarsBtn,resetBtn);

        VerticalLayout rightVL = new VerticalLayout();
        rightVL.setDefaultHorizontalComponentAlignment(Alignment.END);
        rightVL.setWidth("50px");
        rightVL.setHeight("100%");
        rightVL.setSpacing(false);
        rightVL.setPadding(false);
        Button iBtn = new InfoDialogButton("Parameter Scanning",
                "Parameter scanning performs multiple passes on the same simulation using different numerical values for the selected rate parameters or independent variables.", "700px", "200px");
        rightVL.add(iBtn);

        hl.add(leftVL, rightVL);
        hl.expand(leftVL);

        return hl;
    }
}
