package cat.udl.easymodel.views.modelselect;

import cat.udl.easymodel.logic.model.Model;
//import cat.udl.easymodel.logic.results.ResultLineChart;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.sbml.SBMLMan;
import cat.udl.easymodel.utils.P;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.common.AreYouSureDialog;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import cat.udl.easymodel.views.mainlayout.MainLayout;
import cat.udl.easymodel.views.modelbuilder.ModelBuilderView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

@PageTitle(SharedData.appName + " | Model Select")
@Route(value = "model-select", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class ModelSelectView extends VerticalLayout {

    private enum SelectStep {
        SOURCE, PUBLIC_DB, PRIVATE_DB;
    }

    private VerticalLayout contentVL;
    private VerticalLayout navHL;
    private SelectStep selectStep = SelectStep.SOURCE;
    private SessionData sessionData;
    private SharedData sharedData;
    private IFrame descriptionIFrame;
    private Grid<Model> gridModels;
    private ByteArrayOutputStream baos = null;
    private String mimeTypeGlobal = "";

    public ModelSelectView() {
        super();
        sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        sharedData = SharedData.getInstance();
        navHL = ToolboxVaadin.getRawVL();
        navHL.setWidth("100%");
        navHL.setHeight("30px");
        navHL.setDefaultHorizontalComponentAlignment(Alignment.START);
        contentVL = ToolboxVaadin.getRawVL();
        contentVL.setSizeFull();
        contentVL.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        contentVL.setSpacing(false);

//        addClassName("select-model-view");
        this.setSizeFull();
        this.add(navHL, contentVL);
        this.expand(contentVL);
        update();
    }

    private void update() {
        navHL.removeAll();
        contentVL.removeAll();
        switch (selectStep) {
            case SOURCE:
                contentVL.add(getSourceVL());
                break;
            case PUBLIC_DB:
                sessionData.setRepository(RepositoryType.PUBLIC);
                navHL.add(getBackToSourceButton());
                contentVL.add(getModelsVL());
                break;
            case PRIVATE_DB:
                sessionData.setRepository(RepositoryType.PRIVATE);
                navHL.add(getBackToSourceButton());
                contentVL.add(getModelsVL());
                break;
        }
    }

    private VerticalLayout getModelsVL() {
        VerticalLayout vl = new VerticalLayout();
//        vl.setClassName("bordered");
        vl.setSpacing(false);
        vl.setPadding(false);
        vl.setSizeFull();
        vl.setDefaultHorizontalComponentAlignment(Alignment.START);
//        vl.setMaxWidth("500px");

        descriptionIFrame = getDescIFrame();

//        VerticalLayout gridCaption = (sessionData.getRepository() == RepositoryType.PUBLIC) ? ToolboxVaadin.getCaption("Select a Public Model") : ToolboxVaadin.getCaption("Select one of your private models");
        gridModels = getModelListGrid();

        VerticalLayout vl1 = new VerticalLayout();
        vl1.setSpacing(false);
        vl1.setPadding(false);
        vl1.setSizeFull();
        vl1.add(gridModels);

        SplitLayout splitL = new SplitLayout(vl1, descriptionIFrame);
        splitL.setOrientation(SplitLayout.Orientation.VERTICAL);
        splitL.setSizeFull();
        splitL.setSplitterPosition(60);
        splitL.setClassName("bordered");
        vl.add(splitL, getListFooter());
        vl.expand(splitL);
        return vl;
    }

    private void loadModelAndGoToEditView(Model model) {
        try {
            Model copy = new Model(model);
            copy.loadDB();
            //copy.setRepositoryType(RepositoryType.TEMP);
            sessionData.setSelectedModel(copy);
            getUI().ifPresent(ui -> ui.navigate(
                    ModelBuilderView.class));
        } catch (Exception ex) {
            ex.printStackTrace();
            ToolboxVaadin.showErrorNotification("Error loading model from database");
        }
    }

    private HorizontalLayout getListFooter() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setPadding(false);
//        hl.setHeight("60px");
        hl.setWidth("100%");
        Button btnLoad = new Button("Load Model");
        btnLoad.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnLoad.setWidth("200px");
        btnLoad.addClickListener(e -> {
            if (!gridModels.getSelectedItems().isEmpty()) {
                Model selModel = gridModels.getSelectedItems().iterator().next();
                loadModelAndGoToEditView(selModel);
            }
        });
        if (sessionData.getRepository() == RepositoryType.PUBLIC) {
//            if (sessionData.isUserSet()) {
//                Button copyButton = new Button("Copy to Private Models");
//                copyButton.setWidth("240px");
//                copyButton.addClickListener(ev -> {
//                    try {
//                        if (gridModels.getSelectedItems().isEmpty())
//                            throw new Exception("Please select a model");
//                        Model m = gridModels.getSelectedItems().iterator().next();
//                        AreYouSureDialog dia = new AreYouSureDialog("Confirmation", "Are you sure to make a copy of the model \"" + m.getName() + "\" to your private repository?");
//                        dia.addDetachListener(ev2 -> {
//                            if (dia.isAnswerYes()) {
//                                try {
//                                    if (sessionData.getUser() == null)
//                                        throw new Exception("A user account is required for this operation");
//                                    sessionData.setSelectedModel(
//                                            sessionData.getModels().getPrivateModelCopy(m, sessionData.getUser()));
//                                    sessionData.setRepository(RepositoryType.PRIVATE);
//                                    getUI().ifPresent(ui -> ui.navigate(
//                                            ModelBuilderView.class));
//                                } catch (Exception e1) {
//                                    ToolboxVaadin.showErrorNotification("Model copy error");
//                                }
//                            }
//                        });
//                        dia.open();
//                    } catch (Exception e) {
//                        ToolboxVaadin.showWarningNotification(e.getMessage());
//                    }
//                });
//                hl.add(copyButton);
//            }
            VerticalLayout spacer = new VerticalLayout();
            hl.add(spacer, btnLoad);
            hl.expand(spacer);
        } else if (sessionData.getRepository() == RepositoryType.PRIVATE) {
            Button deleteButton = new Button("Delete Model");
            deleteButton.setWidth("200px");
            deleteButton.addClickListener(ev -> {
                try {
                    if (sessionData.getUser() == null)
                        throw new Exception("A user account is required for this operation");
                    if (gridModels.getSelectedItems().isEmpty())
                        throw new Exception("Please select a model");
                    Model m = gridModels.getSelectedItems().iterator().next();
                    AreYouSureDialog dia = new AreYouSureDialog("Confirmation", "Are you sure to delete the model \"" + m.getName() + "\" from your private repository?");
                    dia.addDetachListener(ev2 -> {
                        if (dia.isAnswerYes()) {
                            try {
                                m.deleteDB();
                                sessionData.getModels().removeModel(m);
                                if (m==sessionData.getSelectedModel().getParent())
                                    sessionData.setSelectedModel(null);
                                m.reset();
                                update();
                            } catch (Exception e1) {
                                ToolboxVaadin.showErrorNotification("Delete model error");
                            }
                        }
                    });
                    dia.open();
                } catch (Exception e) {
                    ToolboxVaadin.showWarningNotification(e.getMessage());
                }
            });
            hl.add(deleteButton);
            VerticalLayout spacer = new VerticalLayout();
            hl.add(spacer, btnLoad);
            hl.expand(spacer);
        }
        return hl;
    }

    private Grid<Model> getModelListGrid() {
        Grid<Model> grid = new Grid<>();
//        lb.setHeight("100px");
//        lb.setWidth("100%");
        grid.getStyle().set("text-align", "left");
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        ArrayList<Model> lbItems = new ArrayList<>();
        for (Model m : sessionData.getModels()) {
            if (m.getRepositoryType() == sessionData.getRepository()
                    && (m.getRepositoryType() == RepositoryType.PUBLIC
                    || m.getRepositoryType() == RepositoryType.PRIVATE
                    && m.getUser() == sessionData.getUser())) {
                lbItems.add(m);
            }
        }
        grid.setItems(lbItems);
        if (sessionData.getRepository() == RepositoryType.PUBLIC)
            grid.addColumn(Model::getName).setHeader("Public Models");
        else
            grid.addColumn(Model::getName).setHeader("Your Models");
        grid.addSelectionListener(e -> {
            e.getFirstSelectedItem().ifPresent(sel -> {
                updateDescription(sel.getDescription());
            });
        });
        grid.addItemDoubleClickListener(e -> {
            Model selModel = e.getItem();
            loadModelAndGoToEditView(selModel);
        });
        if (lbItems.size() > 0)
            grid.select(lbItems.get(0));
        return grid;
    }

    private IFrame getDescIFrame() {
        IFrame iFrame = new IFrame();
        iFrame.setSizeFull();
        iFrame.getStyle().set("border", "1px solid #e9e9e9");
        return iFrame;
    }

    private void updateDescription(String desc) {
        String sanitizedHtml = ToolboxVaadin.sanitizeHTML(desc);
        descriptionIFrame.setSrcdoc("<html><body style=\"font-family: Helvetica, Arial, sans-serif;\">" + sanitizedHtml + "</body></html>");
    }

    private Button getBackToSourceButton() {
        Button btn = new Button(VaadinIcon.ARROW_BACKWARD.create());
        btn.setText("Back to Model Source");
        btn.addClickListener(e -> {
            selectStep = SelectStep.SOURCE;
            update();
        });
        return btn;
    }

    /////////////////////////////////////////////////////
    private VerticalLayout getSourceVL() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(false);
        vl.setClassName("bordered");
        vl.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        vl.setWidth("350px");
        H4 title = new H4("Model Source");
        Button btnNewModel = new Button("Create New Model");
        btnNewModel.setWidth("100%");
        btnNewModel.addClickListener(e -> {
            Model m = new Model();
            m.initNewModel();
            sessionData.setSelectedModel(m);
            getUI().ifPresent(ui -> ui.navigate(
                    ModelBuilderView.class));
        });
        Button publicBtn = new Button("Public Repository");
        publicBtn.setWidth("100%");
        publicBtn.addClickListener(e -> {
            selectStep = SelectStep.PUBLIC_DB;
            update();
        });
        Button yourModelsBtn = new Button("Private Repository");
        yourModelsBtn.setWidth("100%");
        yourModelsBtn.addClickListener(e -> {
            selectStep = SelectStep.PRIVATE_DB;
            update();
        });
        vl.add(getInfoHL(), title, btnNewModel, publicBtn);
        if (sessionData.isUserSet())
            vl.add(yourModelsBtn);
        vl.add(getUploadSBML());
        //vl.add(new ResultLineChart("","Title","Xtest","Ytest").test());
        return vl;
    }

    private HorizontalLayout getInfoHL() {
        InfoDialogButton infoBtn = new InfoDialogButton("Model Source", "SBML model import:\n" +
                "Please note that several SBML features are not supported in "+SharedData.appName+": " +
                "initial amounts of species, discrete events, assignment expressions, ode expressions, etc.\nThis is due to the nature of EasyModel design.", "600px", "400px");
        HorizontalLayout infoHL = new HorizontalLayout();
        infoHL.setSpacing(false);
        infoHL.setPadding(false);
        infoHL.setWidthFull();
        HorizontalLayout spacer = new HorizontalLayout();
        infoHL.add(spacer,infoBtn);
        infoHL.expand(spacer);
        return infoHL;
    }

    private VerticalLayout getUploadSBML() {
        VerticalLayout vl = ToolboxVaadin.getRawVL();
        vl.setSizeFull();
        vl.setSpacing(false);
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setReceiver((Receiver) (filename, mimeType) -> {
            mimeTypeGlobal = mimeType;
            try {
                baos = new ByteArrayOutputStream();
            } catch (Exception e) {
                baos = null;
            }
            return baos;
        });
//        upload.setWidth("100%");
        upload.setUploadButton(new Button("Import SBML Model File"));
        upload.setMaxFiles(1);
        upload.setMaxFileSize(20000000);
        upload.addSucceededListener(event -> {
            if (baos==null) {
                upload.clearFileList();
                ToolboxVaadin.showErrorNotification("Upload error");
                return;
            }
            if (!"text/xml".equals(mimeTypeGlobal)) {
                upload.clearFileList();
                ToolboxVaadin.showErrorNotification("File is not in SBML format");
                return;
            }
            try {
                StringBuilder report = new StringBuilder();
                SBMLMan sbmlMan = new SBMLMan();
                Model m = sbmlMan.importSBML(new ByteArrayInputStream(baos.toByteArray()), report, null, false);
                if (report.length() > 0)
                    ToolboxVaadin.showWarningNotification("SBML import report:\n" + report);
                sessionData.setSelectedModel(m);
                sessionData.setRepository(RepositoryType.TEMP);
                getUI().ifPresent(ui->ui.navigate(ModelBuilderView.class));
//                ToolboxVaadin.showInfoNotification("Please note that some SBML features are not supported:\nInitial amounts, discrete events, assignment expressions, ode expressions...");
                ToolboxVaadin.showSuccessNotification("SBML upload success");
            } catch (Exception e) {
                upload.clearFileList();
                ToolboxVaadin.showErrorNotification("SBML file contains errors: " + e.getMessage());
                e.printStackTrace();
            }
        });
        upload.addFileRejectedListener(event -> {
            ToolboxVaadin.showErrorNotification("SBML upload failed (is file XML/SBML?)");
        });
        vl.add(upload);
        return vl;
    }

}
