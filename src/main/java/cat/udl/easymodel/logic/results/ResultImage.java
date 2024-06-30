package cat.udl.easymodel.logic.results;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ResultImage implements ResultEntry {
    private String src, alt, href;

    public ResultImage(String src, String alt, String href) {
        this.src = src;
        this.alt = alt;
        this.href = href;
    }

    @Override
    public Component toComponent() {
        if (src != null) {
            Image img = new Image(src, alt);
            if (href != null) {
                Anchor anchor = new Anchor(href, img);
                anchor.setTarget("_blank");
                return anchor;
            } else
                return img;
        }
        return new VerticalLayout();
    }
}
