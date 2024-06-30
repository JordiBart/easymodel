package cat.udl.easymodel.vcomponent.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

public class GridLayoutElement {
    private final VerticalLayout verticalLayout;
    private Component component;

    public GridLayoutElement(VerticalLayout verticalLayout, Component component) {
        this.verticalLayout = verticalLayout;
        this.component = component;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public VerticalLayout getVerticalLayout() {
        return verticalLayout;
    }
}