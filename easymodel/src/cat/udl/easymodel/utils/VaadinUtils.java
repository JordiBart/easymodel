package cat.udl.easymodel.utils;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

public class VaadinUtils {
	
	private VaadinUtils() {}
	
	public static HorizontalLayout getIndentedVLLayout(Layout lay) {
		VerticalLayout indentVL = new VerticalLayout();
		indentVL.setWidth("30px");
		HorizontalLayout hl = new HorizontalLayout();
		hl.addComponents(indentVL, lay);
		return hl;
	}
	
	public static Label getStyledLabel(String text, String style) {
		Label lab = new Label();
		lab.setContentMode(ContentMode.TEXT);
		if (text != null) {
			lab.setValue(text);
			if (style != null)
				lab.setStyleName(style);
		}
		return lab;
	}
}
