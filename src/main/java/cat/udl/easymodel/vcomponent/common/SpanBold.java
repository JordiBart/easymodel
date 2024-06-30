package cat.udl.easymodel.vcomponent.common;

import com.vaadin.flow.component.html.Span;

public class SpanBold extends Span {
	public SpanBold(String msg) {
		super(msg);
		this.getStyle().setFontWeight("600");
	}
}
