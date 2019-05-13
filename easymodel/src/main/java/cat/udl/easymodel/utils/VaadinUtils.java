package cat.udl.easymodel.utils;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

public class VaadinUtils {
	public static final String usernameCharRegex = "[a-zA-Z0-9]";
	public static final String usernameRegex = "^(?=.*[A-Za-z])"+usernameCharRegex+"{3,20}$";
	public static final String usernameRegexInfo = "Username: 3-20 alphanumeric characters, at least one letter";
	public static final String passwordCharRegex = "[A-Za-z\\d\\._!]";
	public static final String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)"+passwordCharRegex+"{8,50}$";
	public static final String passwordRegexInfo = "Password: 8-50 characters, at least one letter and one number";

	private VaadinUtils() {
	}

//	HOW TO ADD A SPACER (HL FOR HL, VL FOR VL)
//	HorizontalLayout spacer = new HorizontalLayout();
//	allButtonsHL.addComponent(spacer);
//	allButtonsHL.setExpandRatio(spacer, 1);
	
	public static HorizontalLayout getIndentedVLLayout(Layout lay) {
		VerticalLayout indentVL = new VerticalLayout();
		indentVL.setMargin(false);
		indentVL.setWidth("30px");
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
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
		if (rawHtml != null)
			return rawHtml.replaceAll("\\s*<notes.*>\\s*", "").replaceAll("\\s*<\\/notes>\\s*", "")
					.replaceAll("\\s*<body.*>\\s*", "").replaceAll("\\s*<\\/body>\\s*", "")
					.replaceAll("<.*script.*>", "").replaceAll("<\\/script>", "").replaceAll("on(l|L)oad=", "");
		else
			return null;
	}
}
