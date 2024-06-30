package cat.udl.easymodel.logic.results;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;

public class ResultHtml implements ResultEntry {
    private String content=null;

    public ResultHtml(String content_){
        this.content = content_;
    }

    @Override
    public Component toComponent() {
        return new Html(content);
    }
}
