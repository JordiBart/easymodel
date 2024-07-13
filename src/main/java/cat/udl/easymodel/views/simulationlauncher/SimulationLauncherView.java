package cat.udl.easymodel.views.simulationlauncher;

import cat.udl.easymodel.controller.SimulationCtrl;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.types.SimType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathQueue;
import cat.udl.easymodel.mathlink.SimJob;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.vcomponent.simulation.DeterministicDynamicSettings;
import cat.udl.easymodel.vcomponent.simulation.SteadyStateSettings;
import cat.udl.easymodel.vcomponent.simulation.StochasticDynamicSettings;
import cat.udl.easymodel.views.simulationlauncher.dialog.PlotSettingsDialog;
import cat.udl.easymodel.views.simulationresults.SimulationResultsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import cat.udl.easymodel.views.mainlayout.MainLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayInputStream;

@PageTitle(SharedData.appName + " | Simulation Launcher")
@Route(value = "simulation-launcher", layout = MainLayout.class)
public class SimulationLauncherView extends VerticalLayout {
    private SessionData sessionData;
    private SharedData sharedData;
    private Model selectedModel;
    private VerticalLayout tasksVL;
    private VerticalLayout settingsVL;
    private SimConfig simConfig;
    private PlotSettingsDialog plotSettingsDialog;

    public SimulationLauncherView() {
        super();
        sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        sharedData = SharedData.getInstance();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        this.setSizeFull();
        if (sessionData.getSelectedModel() == null) {
            this.setPadding(true);
            this.setSpacing(true);
            add(new Span("Error: model is not selected."));
            return;
        }
        this.selectedModel = sessionData.getSelectedModel();
        try {
            selectedModel.checkValidModel();
        } catch (Exception e) {
            this.setPadding(true);
            this.setSpacing(true);
            add(new Span("Error: model is not valid. Validate model in the Model Builder."));
            return;
        }
        simConfig = selectedModel.getSimConfig();
        simConfig.ready(selectedModel);
        plotSettingsDialog = new PlotSettingsDialog(simConfig.getPlotSettings());
        //VIEW START
        this.setPadding(false);
        this.setSpacing(false);
        tasksVL = getTasksVL();

        settingsVL = new VerticalLayout();
        settingsVL.setPadding(true);
        settingsVL.setSpacing(true);
        settingsVL.setWidth("100%");
        settingsVL.setClassName("scroll");
        updateSettingsVL();

        SplitLayout hSplitPanel = new SplitLayout(
                getTasksVL(),
                settingsVL);
        hSplitPanel.setSizeFull();
        add(hSplitPanel);
    }

    private VerticalLayout getTasksVL() {
        VerticalLayout vl = new VerticalLayout();
        vl.setPadding(true);
        vl.setSpacing(false);
        vl.setWidth("400px");
        vl.add(ToolboxVaadin.getCaption("Select Simulation Types"));
        MultiSelectListBox<SimType> mslb = new MultiSelectListBox<>();
        mslb.setWidth("100%");
        mslb.setItems(SimType.values());
        mslb.getElement().getStyle().set("border", "2px solid rgb(211, 211, 211)");
        mslb.getElement().getStyle().set("border-radius", "5px");
        mslb.setValue(selectedModel.getSimConfig().getSimTypesToLaunch());
        mslb.addSelectionListener(multiSelectionEvent -> {
            selectedModel.getSimConfig().updateSimTypesToLaunch(multiSelectionEvent.getValue());
            updateSettingsVL();
        });
        vl.add(mslb, getPlotSettingsButton(), ToolboxVaadin.newHR(),newLaunchButton());
        return vl;
    }

    private Button getPlotSettingsButton() {
        Button btn = new Button();
        btn.setIcon(VaadinIcon.PALETTE.create());
        btn.setWidth("100%");
        btn.setText("Plot Settings");
        btn.addClickListener(ev -> {
            plotSettingsDialog.open();
        });
        return btn;
    }

    private Component newMathematicaNotebookButton() {
        Button btn = new Button();
        btn.setIcon(VaadinIcon.DOWNLOAD.create());
        btn.setWidth("100%");
        btn.setText("Mathematica Notebook");
        String notebookFilename = Utils.curateForURLFilename(SharedData.appName+"-"+selectedModel.getNameShort()+".nb");
        FileDownloadWrapper buttonWrapper = new FileDownloadWrapper(
                new StreamResource(notebookFilename, () -> {
                    String notebookContent = "error";
                    try {
                        SimulationCtrl simulationCtrl = new SimulationCtrl(selectedModel);
                        simulationCtrl.simulate();
                        notebookContent = simulationCtrl.getNotebookBuffer().getString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return new ByteArrayInputStream(notebookContent.getBytes());
                }));
        buttonWrapper.wrapComponent(btn);
        buttonWrapper.setWidth("100%");
        return buttonWrapper;
    }

    private Button newSBMLExportButton() {
        Button btn = new Button();
        btn.setIcon(VaadinIcon.DOWNLOAD.create());
        btn.setWidth("100%");
        btn.setText("Export SBML Model");
        btn.addClickListener(ev -> {
            try {
                selectedModel.checkAndAdaptToSimulate();
                Model copyModelForSim=new Model(selectedModel, 1);
                copyModelForSim.getSimConfig().getSimTypesToLaunch().clear();
                SimJob simJob = new SimJob(copyModelForSim, VaadinSession.getCurrent());
                String jobId = MathQueue.getInstance().addNewMathJob(simJob);
                ToolboxVaadin.showSuccessNotification("SBML export (SimId " + jobId + ") launched!");
                btn.getUI().ifPresent(ui -> ui.navigate(
                        SimulationResultsView.class, jobId));
            } catch (Exception e) {
                ToolboxVaadin.showErrorNotification(e.getMessage());
                e.printStackTrace();
            }
        });
        return btn;
    }

    private Button newLaunchButton() {
        Button btn = new Button();
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.setIcon(VaadinIcon.FLIGHT_TAKEOFF.create());
        btn.setWidth("100%");
        btn.setText("Launch Simulation");
        btn.addClickListener(ev -> {
            try {
                selectedModel.checkAndAdaptToSimulate();
                Model copyModelForSim=new Model(selectedModel, 1);
                SimJob simJob = new SimJob(copyModelForSim, VaadinSession.getCurrent());
                String jobId = MathQueue.getInstance().addNewMathJob(simJob);
                ToolboxVaadin.showSuccessNotification("SimId " + jobId + " launched!");
                btn.getUI().ifPresent(ui -> ui.navigate(
                        SimulationResultsView.class, jobId));
            } catch (Exception e) {
                ToolboxVaadin.showErrorNotification(e.getMessage());
                //e.printStackTrace();
            }
        });
        return btn;
    }

    private void updateSettingsVL() {
        settingsVL.removeAll();
        Span selModelTxt = new Span("Model: "+selectedModel.getName());
        selModelTxt.setClassName("textH2");
        Span title = new Span("Simulation Settings");
        title.getStyle().set("font-weight", "bold");
        settingsVL.add(selModelTxt,title);
        if (selectedModel.getSimConfig().getSimTypesToLaunch().isEmpty()) {
            settingsVL.add(new Span("No simulations are selected."));
            settingsVL.add(new Span("Launch empty simulation to generate Mathematica Notebook and SBML files."));
        } else {
            if (selectedModel.getSimConfig().getSimTypesToLaunch().contains(SimType.DYNAMIC_DETERMINISTIC)) {
                settingsVL.add(new DeterministicDynamicSettings(simConfig));
            }
            if (selectedModel.getSimConfig().getSimTypesToLaunch().contains(SimType.STEADY_STATE)) {
                settingsVL.add(new SteadyStateSettings(simConfig));
            }
            if (selectedModel.getSimConfig().getSimTypesToLaunch().contains(SimType.DYNAMIC_STOCHASTIC)) {
                settingsVL.add(new StochasticDynamicSettings(simConfig, selectedModel.getStochasticGradeType()));
            }
        }
    }
}
