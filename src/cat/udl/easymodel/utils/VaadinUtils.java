package cat.udl.easymodel.utils;

import com.vaadin.ui.HorizontalLayout;
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
}
