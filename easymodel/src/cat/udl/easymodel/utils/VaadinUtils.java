package cat.udl.easymodel.utils;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

public class VaadinUtils {
	public static final String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d\\.!$%@#£€*?&_]{8,50}$";
	public static final String passwordRegexInfo = "Password: 8-50 characters, at least one letter and one number";
	public static final String usernameRegex = "^(?=.*[A-Za-z])[a-zA-Z0-9]{3,20}$";
	public static final String usernameRegexInfo = "Username: 3-20 alphanumeric characters, at least one letter";
	
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
	
	public static String sanitizeHTML(String rawHtml) {
		if (rawHtml !=null)
			return rawHtml.replaceAll("\\s*<notes.*>\\s*", "").replaceAll("\\s*<\\/notes>\\s*", "").replaceAll("\\s*<body.*>\\s*", "").replaceAll("\\s*<\\/body>\\s*", "").replaceAll("<.*script.*>", "").replaceAll("<\\/script>", "").replaceAll("on(l|L)oad=", "");
		else
			return null;
	}
}
