package cat.udl.easymodel.logic.results;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;

public class ResultText implements ResultEntry {
    private String value;
    private String style;
    public ResultText(String text, String style) {
        this.value = text;
        this.style=style;
    }

    @Override
    public Component toComponent() {
        Span txt = new Span(value);
        if (style!=null)
            txt.setClassName(style);
        return txt;
    }
}
