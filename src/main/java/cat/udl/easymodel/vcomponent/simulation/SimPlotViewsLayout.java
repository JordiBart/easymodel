package cat.udl.easymodel.vcomponent.simulation;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.simconfig.SimConfigArray;
import cat.udl.easymodel.logic.simconfig.SimPlotView;
import cat.udl.easymodel.logic.simconfig.SimPlotViewArray;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import cat.udl.easymodel.views.simulationlauncher.SimConfigToComponent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.server.VaadinSession;

import java.util.ArrayList;
import java.util.Collection;

public class SimPlotViewsLayout extends VerticalLayout {
    private TabSheet tabSheetPlotViews = new TabSheet();
    private ArrayList<Component> plotTabsList = new ArrayList<>();
    private SimConfig simConfig;
    private SimPlotViewArray simPlotViewArray;
    private Model selectedModel;
    private SessionData sessionData;

    public SimPlotViewsLayout() {
        super();
        this.setPadding(false);
        this.setSpacing(false);
        this.setSizeFull();

        sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        this.selectedModel = this.sessionData.getSelectedModel();
        this.simConfig = this.selectedModel.getSimConfig();
        this.simPlotViewArray = this.simConfig.getDynamic_PlotViews();
        simPlotViewArray.fix(selectedModel);

        VerticalLayout leftVL = new VerticalLayout();
        leftVL.setWidthFull();
        leftVL.setSpacing(false);
        leftVL.setPadding(false);

        tabSheetPlotViews.setWidth("400px");
        updatePlotTabs();
        leftVL.add(tabSheetPlotViews);
        /////////////
        VerticalLayout rightVL = new VerticalLayout();
        rightVL.setWidth("50px");
        rightVL.setSpacing(false);
        rightVL.setPadding(false);

        Button iBtn = new InfoDialogButton("Information", "Plot Views: Define the variables to represent in each plot.", "800px", "400px");

        Button plusBtn = new Button();
        plusBtn.setIcon(VaadinIcon.PLUS.create());
        plusBtn.setTooltipText("Add a plot view");
        plusBtn.addClickListener(e -> {
            if (plotTabsList.size() < Math.max(selectedModel.getAllSpeciesTimeDependent().size(), 10)) {
                simConfig.getDynamic_PlotViews().addNewPlotView();
                updatePlotTabs();
                tabSheetPlotViews.setSelectedIndex(plotTabsList.size() - 1);
            }
        });

        Button minusBtn = new Button();
        minusBtn.setIcon(VaadinIcon.MINUS.create());
        minusBtn.setTooltipText("Remove current plot view");
        minusBtn.addClickListener(buttonClickEvent -> {
            if (simConfig.getDynamic_PlotViews().size() > 1) {
                simConfig.getDynamic_PlotViews().removeAtIndex(tabSheetPlotViews.getSelectedIndex());
                updatePlotTabs();
                tabSheetPlotViews.setSelectedIndex(plotTabsList.size() - 1);
            }
        });

        Button eachBtn = new Button();
        eachBtn.setIcon(VaadinIcon.GROUP.create());
        eachBtn.setTooltipText("Generate individual plot for each independent variable");
        eachBtn.addClickListener(e -> {
            simConfig.getDynamic_PlotViews().setEachDepVarToOneView(selectedModel);
            updatePlotTabs();
        });

        Button resetBtn = new Button();
        resetBtn.setIcon(VaadinIcon.REFRESH.create());
        resetBtn.setTooltipText("Reset selection");
        resetBtn.addClickListener(e -> {
            simConfig.getDynamic_PlotViews().clearPlotViews();
            simConfig.getDynamic_PlotViews().fix(selectedModel);
            updatePlotTabs();
        });

        rightVL.setDefaultHorizontalComponentAlignment(Alignment.END);
        rightVL.add(iBtn, plusBtn, minusBtn, eachBtn, resetBtn);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setMinWidth("400px");
        hl.setSpacing(true);
        hl.setPadding(true);
        hl.add(leftVL, rightVL);
        hl.expand(leftVL);
        this.add(hl);
    }

    public void updatePlotTabs() {
        for (int i = 0; i < plotTabsList.size(); i++)
            tabSheetPlotViews.remove(plotTabsList.get(i));
        plotTabsList.clear();

        for (int i = 0; i < simConfig.getDynamic_PlotViews().size(); i++) {
            SimPlotView simPlotView = simConfig.getDynamic_PlotViews().get(i);

            VerticalLayout vl = new VerticalLayout();
            vl.setSpacing(false);
            vl.setPadding(false);
            vl.setSizeFull();
            Span label = new Span("Dependent variables");
            label.setClassName("caption");
            vl.add(label);
            for (String dv : selectedModel.getAllSpeciesTimeDependent().keySet()) {
                Checkbox cb = new Checkbox();
                cb.setLabel(dv);
                cb.setValue(simPlotView.contains(dv));
                cb.addValueChangeListener(e -> {
                    if (e.getValue()) {
                        simPlotView.add(dv);
                    } else {
                        simPlotView.remove(dv);
                    }
                });
                vl.add(cb);
            }
            plotTabsList.add(vl);
            tabSheetPlotViews.add("View " + plotTabsList.size(), vl);
        }
    }
}
