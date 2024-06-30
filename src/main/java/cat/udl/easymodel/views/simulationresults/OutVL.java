package cat.udl.easymodel.views.simulationresults;

import cat.udl.easymodel.logic.results.ResultDynamicImage;
import cat.udl.easymodel.logic.results.ResultEntry;
import cat.udl.easymodel.logic.results.ResultList;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.HashMap;

public class OutVL extends VerticalLayout {
    ResultList resultsList = null;
    private HashMap<ResultEntry, Component> entryComponentHashMap = new HashMap<>();
    private int totalComponents = 0;
    private FormLayout lastFormLayout = null;

    public OutVL(ResultList resultsList) {
        super();
        this.resultsList = resultsList;
        setPadding(false);
        setSpacing(true);
        setWidth("100%");
        setHeight("500px");
//        setClassName("scroll");
    }

    public void outResultEntry(ResultEntry resultEntry) {
        Component comp = resultEntry.toComponent();
        if (resultEntry instanceof ResultDynamicImage) {
            if (lastFormLayout == null) {
                lastFormLayout = new FormLayout();
                add(lastFormLayout);
            }
            lastFormLayout.add(comp);
        } else {
            lastFormLayout = null;
            add(comp);
        }
        entryComponentHashMap.put(resultEntry,comp);
        totalComponents++;
    }

    public void update() {
        for (int k = totalComponents; k < resultsList.size(); k++)
            this.outResultEntry(resultsList.get(k));
        resultsList.updateComponents();
    }

    public boolean checkIsToUpdate() {
        return totalComponents < resultsList.size() || resultsList.isDirty();
    }

    public void reset() {
        removeAll();
        totalComponents = 0;
        lastFormLayout.removeAll();
        lastFormLayout = null;
    }
}
