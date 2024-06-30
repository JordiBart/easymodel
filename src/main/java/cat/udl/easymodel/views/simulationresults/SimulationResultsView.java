package cat.udl.easymodel.views.simulationresults;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathJobStatus;
import cat.udl.easymodel.mathlink.MathQueue;
import cat.udl.easymodel.mathlink.SimJob;
import cat.udl.easymodel.utils.P;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.views.mainlayout.MainLayout;
import cat.udl.easymodel.views.simulationresults.dialog.ResultsShareDialog;
import cat.udl.easymodel.views.simulationresults.dialog.RunStatsDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinSession;
import org.vaadin.olli.ClipboardHelper;

import java.util.HashMap;
import java.util.LinkedList;

@PageTitle(SharedData.appName + " | Simulation Results")
@Route(value = "simulation-results", layout = MainLayout.class)
public class SimulationResultsView extends VerticalLayout implements HasUrlParameter<String> {
    private SessionData sessionData;
    private SharedData sharedData;
    private SimJob simJob = null;
    private Grid<SimJob> grid;
    private OutVL outVL;
    private Span simStatusText;
    private ProgressBar progressBar;
    private MenuBar rightMenuBar;
    private MenuItem cancelMenuItem;

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        //run after constructor
        this.removeAll();
        simJob = null;
        if (parameter != null) {
            SimJob sj = MathQueue.getInstance().getAllSimJobs().get(parameter);
            if (sj != null)
                simJob = sj;
            else {
                event.forwardTo("/");
//                ToolboxVaadin.showErrorNotification("SimId " + parameter + " not found");
            }
        }
        if (simJob == null) {
            //list all simjobs
            this.setHeightFull();
            createJobGrid();
            add(newJobListHeaderHL(), grid);
        } else {
            outVL = new OutVL(simJob.getResultList());
            //outVL will be filled in refresh thread
            add(newSimHeader(), outVL);
        }
        launchRefreshThread();
    }

    public SimulationResultsView() {
        super();
        sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        sharedData = SharedData.getInstance();
        this.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        this.setWidthFull();
        this.setPadding(true);
        this.setSpacing(true);
//        p.p("contructor "+this);
    }

    private HorizontalLayout newJobListHeaderHL() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        hl.setWidth("100%");
        hl.setHeight("40px");
        hl.setPadding(false);
        hl.setSpacing(false);
        VerticalLayout hlTitle = new VerticalLayout();
        hlTitle.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        hlTitle.setWidth("100%");
        hlTitle.setPadding(false);
        hlTitle.add(new H5("Your Launched Simulations"));

        hl.add(hlTitle);
        hl.expand(hlTitle);
        return hl;
    }

    private void createJobGrid() {
        grid = new Grid<>();
//        grid.addColumn(new ComponentRenderer<>(job->{
//            Button btn = new Button(job.getJobId());
//            btn.addClickListener(e -> {
//                getUI().ifPresent(ui->ui.navigate(SimulationResultsView.class,job.getJobId()));
//            });
//            return btn;
//        })).setHeader("Open simulation");
        grid.addColumn(new ComponentRenderer<>(job -> {
            RouterLink routerLink = new RouterLink(job.getJobId(), SimulationResultsView.class, job.getJobId());
            return routerLink;
        })).setHeader("Simulation Id");
        grid.addColumn(SimJob::getMathJobStatus).setHeader("Status");
        grid.addColumn(SimJob::getQueuePositionString).setHeader("Position in Queue");
        grid.addColumn(new ComponentRenderer<>(job -> {
            String url = SharedData.getInstance().getProperties().getProperty("web-protocol")+ SharedData.getInstance().getProperties().getProperty("hostname") + "/" + RouteConfiguration.forSessionScope().getUrl(SimulationResultsView.class, job.getJobId());
            Button btn = new Button();
            btn.setIcon(VaadinIcon.LINK.create());
            btn.addClickListener(e -> {
                ToolboxVaadin.showSuccessNotification("URL copied to clipboard!");
            });
            ClipboardHelper clipboardHelper = new ClipboardHelper(url, btn);
            return clipboardHelper;
        })).setHeader("Copy URL");
        grid.addColumn(new ComponentRenderer<>(job -> {
            Button btn = new Button(VaadinIcon.CLOSE.create());
            btn.addClickListener(e -> {
                if (job.getMathJobStatus() == MathJobStatus.FINISHED) {
                    ToolboxVaadin.showInfoNotification("Simulation has finished");
                } else {
                    MathQueue.getInstance().cancelSimulationByUser(job);
                    ToolboxVaadin.showSuccessNotification("Cancel request sent");
                }
            });
            return btn;
        })).setHeader("Cancel");
        grid.setWidthFull();
        grid.setHeightFull();
        //grid.setItems(jobs); will be set in refresh thread
    }

    private VerticalLayout newSimHeader() {
        VerticalLayout vl = new VerticalLayout();
        vl.setWidth("100%");
        vl.setPadding(false);
        vl.setSpacing(true);
        HorizontalLayout hl1 = new HorizontalLayout();
        hl1.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        hl1.setWidth("100%");
        hl1.setPadding(false);
        hl1.setSpacing(true);

        Span simIdText = new Span("SimId: " + simJob.getJobId());
        simIdText.setClassName("bold");
        simStatusText = new Span();
        simStatusText.setClassName("bold");

        progressBar = new ProgressBar();
        progressBar.setWidth("10px"); // DO NOT DELETE
        rightMenuBar = newRightSimMenuBar();
        MenuBar leftMenuBar = new MenuBar();
        leftMenuBar.addItem(VaadinIcon.ARROW_BACKWARD.create(), e -> {
            getUI().ifPresent(ui -> ui.navigate(SimulationResultsView.class, ""));
        });
        hl1.add(leftMenuBar, simIdText, simStatusText, progressBar, rightMenuBar);
        hl1.expand(progressBar);

        HorizontalLayout hl2 = new HorizontalLayout();
        hl2.setDefaultVerticalComponentAlignment(Alignment.START);
        hl2.setWidth("100%");
        hl2.setPadding(false);
        hl2.setSpacing(true);
        Span modelName = new Span("Model: " + simJob.getModel().getName());
        modelName.setClassName("bold");
        hl2.add(modelName);
        vl.add(hl1, hl2);
        return vl;
    }

    private MenuBar newRightSimMenuBar() {
        MenuBar menuBar = new MenuBar();
        if (simJob != null) {
            cancelMenuItem = menuBar.addItem("Cancel", e -> {
                MathQueue.getInstance().cancelSimulationByUser(simJob);
                ToolboxVaadin.showSuccessNotification("Cancel request sent");
            });
            menuBar.addItem("Share", e -> {
                new ResultsShareDialog(simJob).open();
            });
            menuBar.addItem("Run Stats", e -> {
                new RunStatsDialog(simJob).open();
            });
        }
        return menuBar;
    }

    //////////////////////////////////////////////
    private void launchRefreshThread() {
        SimulationResultsViewRefreshRunnable prevRunnable = sessionData.getSimulationResultsViewRunnable();
        Thread prevThread = sessionData.getSimulationResultsViewThread();
        SimulationResultsViewRefreshRunnable newRunnable = new SimulationResultsViewRefreshRunnable();
        Thread newThread = new Thread(newRunnable);
        sessionData.setSimulationResultsViewRunnable(newRunnable);
        sessionData.setSimulationResultsViewThread(newThread);
        Thread launchThread = new Thread(() -> {
            if (prevRunnable != null && prevThread != null) {
                prevRunnable.doStop();
                for (int i = 0; i < 3000 && prevThread.isAlive(); i++) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            newThread.start();
        });
        launchThread.start();
    }

    public class SimulationResultsViewRefreshRunnable implements Runnable {
        protected UI ui;
        protected VaadinSession vaadinSession;
        private boolean doStop = false;

        public synchronized void doStop() {
            this.doStop = true;
        }

        private synchronized boolean keepRunning() {
            return this.doStop == false;
        }

        public SimulationResultsViewRefreshRunnable() {
            this.ui = UI.getCurrent();
            this.vaadinSession = VaadinSession.getCurrent();
        }

        @Override
        public void run() {
            P.d("start thread resultview " + this.hashCode());
            boolean isTimeout = true;
            boolean isToFinishThread = false;
            if (simJob == null) {
                // sim list
                TrackerSimJob prevSimJobMap = new TrackerSimJob(MathQueue.getInstance().getSimJobsByVaadinSession(vaadinSession));
                for (int i = 0; i < 3600 && keepRunning(); i++) {
                    final boolean finalIsUpdateSimJobList;
                    final LinkedList<SimJob> f1 = MathQueue.getInstance().getSimJobsByVaadinSession(vaadinSession);
                    if (prevSimJobMap.hasChanged(f1) || i == 0) {
                        prevSimJobMap.update(f1);
                        finalIsUpdateSimJobList = true;
                        if (MathQueue.getInstance().areAllSimJobsFinishedByVaadinSession(vaadinSession))
                            isToFinishThread = true;
                    } else finalIsUpdateSimJobList = false;
                    if (finalIsUpdateSimJobList)
                        ui.access(() -> {
                            if (finalIsUpdateSimJobList)
                                grid.setItems(f1);
                        });
                    if (isToFinishThread) {
                        isTimeout = false;
                        break;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (isTimeout && keepRunning()) {
                }
            } else { // sim view selected
                MathJobStatus lastStatus = simJob.getMathJobStatus();
                Integer lastQueuePos = simJob.getQueuePosition();
                for (int i = 0; i < 7200 && keepRunning(); i++) {
                    final boolean finalIsUpdateOutVL;
                    final boolean finalIsUpdateSimHeader;
                    if (lastStatus != simJob.getMathJobStatus() || lastQueuePos != simJob.getQueuePosition() || i == 0) {
                        lastStatus = simJob.getMathJobStatus();
                        lastQueuePos = simJob.getQueuePosition();
                        finalIsUpdateSimHeader = true;
                        if (simJob.getMathJobStatus() == MathJobStatus.FINISHED)
                            isToFinishThread = true;
                    } else finalIsUpdateSimHeader = false;
                    if (outVL.checkIsToUpdate()) {
                        finalIsUpdateOutVL = true;
                    } else finalIsUpdateOutVL = false;
                    if (finalIsUpdateOutVL || finalIsUpdateSimHeader)
                        ui.access(() -> {
                            //update outVL
                            if (finalIsUpdateOutVL) {
                                    outVL.update();
                            }
                            //update simHeader
                            if (finalIsUpdateSimHeader) {
                                if (simJob.getMathJobStatus() == MathJobStatus.PENDING) {
                                    progressBar.setValue(0d);
                                    progressBar.setIndeterminate(false);
                                    simStatusText.setText("In queue (" + simJob.getQueuePosition() + ")");
                                } else if (simJob.getMathJobStatus() == MathJobStatus.RUNNING) {
                                    progressBar.setIndeterminate(true);
                                    simStatusText.setText(simJob.getMathJobStatus().toString());
                                } else if (simJob.getMathJobStatus() == MathJobStatus.FINISHED) {
                                    simStatusText.setText(simJob.getMathJobStatus().toString());
                                    rightMenuBar.remove(cancelMenuItem);
                                    progressBar.setIndeterminate(false);
                                    progressBar.setValue(1d);
                                }
                            }
                        });
                    if (isToFinishThread) {
                        isTimeout = false;
                        break;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (isTimeout && keepRunning()) {
                    ui.access(() -> {
                        progressBar.setIndeterminate(false);
                    });
                }
            }
            P.d("finish thread resultview " + this.hashCode());
        }
    }

    private class TrackerSimJob {
        private class Pojo {
            MathJobStatus mathJobStatus;
            Integer position;

            public Pojo(MathJobStatus mathJobStatus1, Integer position1) {
                mathJobStatus = mathJobStatus1;
                position = position1;
            }
        }

        private HashMap<String, Pojo> map = new HashMap<>();

        TrackerSimJob(LinkedList<SimJob> simJobs) {
            update(simJobs);
        }

        protected void update(LinkedList<SimJob> simJobs) {
            map.clear();
            for (SimJob sj : simJobs) {
                map.put(sj.getJobId(), new Pojo(sj.getMathJobStatus(), sj.getQueuePosition()));
            }
        }

        protected boolean hasChanged(LinkedList<SimJob> simJobs) {
            if (simJobs.size() != map.keySet().size()) {
                return true;
            }
            for (SimJob sj : simJobs) {
                Pojo t = map.get(sj.getJobId());
                if (t != null) {
                    if (sj.getMathJobStatus() != t.mathJobStatus || sj.getQueuePosition() != t.position) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            return false;
        }
    }
}
