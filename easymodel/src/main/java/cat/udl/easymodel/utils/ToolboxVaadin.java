package cat.udl.easymodel.utils;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

public class ToolboxVaadin {
	public static final String usernameCharRegex = "[a-zA-Z0-9]";
	public static final String usernameRegex = "^(?=.*[A-Za-z])"+usernameCharRegex+"{3,20}$";
	public static final String usernameRegexInfo = "Username: 3-20 alphanumeric characters, at least one letter";
	public static final String passwordCharRegex = "[A-Za-z\\d\\._!]";
	public static final String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)"+passwordCharRegex+"{8,50}$";
	public static final String passwordRegexInfo = "Password: 8-50 characters, at least one letter and one number";

	private ToolboxVaadin() {
	}

//	HOW TO ADD A SPACER (HL FOR HL, VL FOR VL)
//	HorizontalLayout spacer = new HorizontalLayout();
//	allButtonsHL.addComponent(spacer);
//	allButtonsHL.setExpandRatio(spacer, 1);
	
	public static Label getHR() {
		Label hr = new Label("<hr />", ContentMode.HTML);
		hr.setWidth("100%");
		return hr;
	}
	
	public static VerticalLayout getRawVL(String styleName) {
		VerticalLayout vl = new VerticalLayout();
		vl.setMargin(false);
		vl.setSpacing(false);
		if (styleName != null)
			vl.setStyleName(styleName);
		return vl;
	}
	
	public static HorizontalLayout getRawHL(String styleName) {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setMargin(false);
		hl.setSpacing(false);
		if (styleName != null)
			hl.setStyleName(styleName);
		return hl;
	}
	
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
	
	public static Link getStandardWebMathematicaLink() {
		Link link = new Link("", new ExternalResource("http://www.wolfram.com/webmathematica/sitelink"));
		link.setIcon(new ThemeResource("img/webm-white-plain.png"));
		link.setTargetName("_blank");
		return link;
	}
	
	public static Link getTinyWebMathematicaLink() {
		Link link = new Link("", new ExternalResource("http://www.wolfram.com/webmathematica/sitelink"));
		link.setIcon(new ThemeResource("img/webm-white-tiny.png"));
		link.setTargetName("_blank");
		return link;
	}
}
