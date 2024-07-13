package cat.udl.easymodel.views.modelbuilder;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import cat.udl.easymodel.views.mainlayout.MainLayout;
import com.vaadin.flow.server.VaadinSession;

@PageTitle(SharedData.appName + " | Model Builder")
@Route(value = "model-builder", layout = MainLayout.class)
//@CssImport(value = "./themes/easymodel/components/vaadin-text-field.css", themeFor = "vaadin-text-field")
public class ModelBuilderView extends VerticalLayout {
    private SessionData sessionData;
    private SharedData sharedData;
    private Model selectedModel;

    public ModelBuilderView() {
        super();
        sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
        sharedData = SharedData.getInstance();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        this.setSizeFull();
        if (sessionData.getSelectedModel() != null) {
            this.setPadding(false);
            this.setSpacing(false);
            this.selectedModel = sessionData.getSelectedModel();
//            if (sessionData.isUserSet())
//                this.selectedModel.setRepositoryType(RepositoryType.PRIVATE);
//        reactionsEditorVL = new ReactionEditorVL(selectedModel, mainPanel);
//        formulasEditorVL = new FormulasEditorVL(reactionsEditorVL);

            ReactionEditorVL reactionEditorVL = new ReactionEditorVL();
            SplitLayout hSplitPanel = new SplitLayout(
                    reactionEditorVL,
                    new FormulaEditorVL(reactionEditorVL));
            hSplitPanel.setSizeFull();
            add(hSplitPanel);
        } else {
            this.setPadding(true);
            this.setSpacing(true);
            add(new Span
                    ("Error: model is not selected"));
        }

    }
}
