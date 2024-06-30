package cat.udl.easymodel.logic.results;

import cat.udl.easymodel.utils.ToolboxVaadin;
import com.vaadin.flow.component.Component;

public class ResultHR implements ResultEntry {
    public ResultHR(){

    }

    @Override
    public Component toComponent() {
        return ToolboxVaadin.newHR();
    }
}
