package cat.udl.easymodel.logic.results;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.ArrayList;

public class ResultHL implements ResultEntry {
    ArrayList<ResultEntry> arrayList = new ArrayList<>();
    public ResultHL(){
    }

    public boolean add(ResultEntry resultEntry){
        return arrayList.add(resultEntry);
    }

    @Override
    public Component toComponent() {
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.setPadding(false);
        for (ResultEntry re : arrayList){
            hl.add(re.toComponent());
        }
        return hl;
    }
}
