package cat.udl.easymodel.utils;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Span;
import jakarta.servlet.http.Cookie;

import cat.udl.easymodel.logic.types.NotificationType;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinService;

public class ToolboxVaadin {
	public static final String usernameCharRegex = "[a-zA-Z0-9]";
	public static final String usernameRegex = "^(?=.*[A-Za-z])" + usernameCharRegex + "{3,20}$";
	public static final String usernameRegexInfo = "Username: 3-20 alphanumeric characters, at least one letter";
	public static final String passwordCharRegex = "[A-Za-z\\d\\._!]";
	public static final String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)" + passwordCharRegex + "{8,50}$";
	public static final String passwordRegexInfo = "Password: 8-50 characters, at least one letter and one number";

	private ToolboxVaadin() {
	}

	public static VerticalLayout getComponentWithCaption(String caption, Component comp){
		VerticalLayout vl1 = new VerticalLayout();
		vl1.setPadding(false);
		vl1.setSpacing(false);
		VerticalLayout vl2 = new VerticalLayout();
		vl2.setPadding(false);
		vl2.setSpacing(false);
		Span cap = new Span(caption);
		vl2.add(cap);
		vl2.getStyle().set("border-bottom", "1px solid #e9e9e9");
		vl2.getStyle().set("background-color", "#eee");
		VerticalLayout vl3 = new VerticalLayout();
		vl3.setPadding(true);
		vl3.setSpacing(false);
		vl3.add(comp);
		vl1.add(vl2,vl3);
		return vl1;
	}

	public static VerticalLayout getRawVL(){
		VerticalLayout vl = new VerticalLayout();
		vl.setPadding(false);
		return vl;
	}

	public static HorizontalLayout getRawHL(){
		HorizontalLayout hl = new HorizontalLayout();
		hl.setPadding(false);
		return hl;
	}

//	public static VerticalLayout newHR(String msg) {
//		VerticalLayout vl = new VerticalLayout();
//		vl.setPadding(false);
//		vl.setSpacing(false);
//		vl.setWidth("100%");
//		vl.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
//		vl.add(new Hr(), ToolboxVaadin.getCaption(msg));
//		return vl;
//	}

	public static Html newHR(){
		return new Html("<hr style=\"border: 1px solid #adcaef;\">");
	}

	public static Span getStyledLabel(String text, String style) {
		Span lab = new Span();
		if (text != null) {
			lab.setText(text);
			if (style != null)
				lab.setClassName(style);
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

	public static Anchor getStandardMathematicaAnchor() {
		Anchor link = new Anchor("https://www.wolfram.com/mathematica/","");
		link.setTarget("_blank");
		return link;
	}

	public static Cookie getClientCookieByName(String name) {
		if (name == null)
			return null;
		Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName() != null && name.equals(cookie.getName()))
					return cookie;
			}
		}
		return null;
	}

	public static VerticalLayout getCaption(String msg) {
		VerticalLayout vl = new VerticalLayout();
		vl.setPadding(false);
		vl.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
		Span lbl = new Span(msg);
		lbl.setClassName("caption");
		vl.add(lbl);
		return vl;
	}

	public static HorizontalLayout getDialogHeader(Dialog dialog, String titleText, InfoDialogButton infoDialogButton) {
		Span title = new Span(titleText);
		title.getStyle().set("font-weight", "bold");

		HorizontalLayout spacer = new HorizontalLayout();

		Button closeBtn = new Button();
		closeBtn.setIcon(VaadinIcon.CLOSE.create());
		closeBtn.addThemeVariants(ButtonVariant.LUMO_ICON);
//        closeBtn.setWidth("30px");
//        closeBtn.setHeight("30px");
		closeBtn.addClickListener(ev ->{
			dialog.close();
		});

		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setPadding(false);
		hl1.setSpacing(true);
		hl1.setWidth("100%");
		hl1.setHeight("30px");
		hl1.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
		hl1.add(title,spacer);
		if (infoDialogButton != null)
			hl1.add(infoDialogButton);
		hl1.add(closeBtn);
		hl1.expand(spacer);
		return hl1;
	}

	public static void showNotification(String msg, NotificationType notificationType) {
		switch (notificationType) {
			case WARNING:
				showWarningNotification(msg);
				break;
			case ERROR:
				showErrorNotification(msg);
				break;
			case SUCCESS:
				showSuccessNotification(msg);
				break;
			case INFO:
				showInfoNotification(msg);
				break;
		}
	}

	public static void showInfoNotification(String msg) {
		Div d = new Div();
//		d.getStyle().set("color","white");
		d.setText(msg);
		Notification notification = new Notification();
		notification.setDuration(3000);
		notification.setPosition(Notification.Position.BOTTOM_END);
		notification.add(d);
		notification.open();
	}

	public static void showWarningNotification(String msg) {
		Notification notification = new Notification();
		Div d = new Div();
		d.getStyle().set("color","#ff6200");
		d.setText(msg);
		notification.add(d);
		notification.setDuration(4000);
		notification.setPosition(Notification.Position.MIDDLE);
		notification.open();
	}

	public static void showErrorNotification(String msg) {
		Div d = new Div();
		d.getStyle().set("color","white");
		d.setText(msg);
		Notification notification = new Notification();
		notification.setDuration(5000);
		notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
		notification.setPosition(Notification.Position.MIDDLE);
		notification.add(d);
		notification.open();
	}

	public static void showSuccessNotification(String msg) {
		Div d = new Div();
		d.getStyle().set("color","white");
		d.setText(msg);
		Notification notification = new Notification();
		notification.setDuration(3000);
		notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		notification.setPosition(Notification.Position.MIDDLE);
		notification.add(d);
		notification.open();
	}

    public static Span newResultsLabel(String s) {
		Span lab = new Span(s);
		return lab;
    }
}
