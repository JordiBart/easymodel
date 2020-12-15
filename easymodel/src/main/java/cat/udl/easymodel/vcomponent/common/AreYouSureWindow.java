package cat.udl.easymodel.vcomponent.common;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import cat.udl.easymodel.logic.types.WStatusType;
import cat.udl.easymodel.main.SessionData;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class AreYouSureWindow extends Window {
	private static final long serialVersionUID = 1L;

	private VerticalLayout windowVL;
	private SessionData sessionData;
	private String title;
	private String message;

	public AreYouSureWindow(String title, String message) {
		super();

		this.title = title;
		this.message = message;
		this.sessionData = (SessionData) UI.getCurrent().getData();

		this.setData(WStatusType.KO);
		this.setCaption(this.title);
		this.setClosable(true);
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(false);
		this.center();
		this.setWidth("400px");
		this.setHeight("300px");

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		this.addShortcutListener(new ShortcutListener("Shortcut enter", ShortcutAction.KeyCode.ENTER, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				setData(WStatusType.OK);
				close();
			}
		});
//		this.addShortcutListener(new ShortcutListener("Shortcut esc", ShortcutAction.KeyCode.ESCAPE, null) {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void handleAction(Object sender, Object target) {
//				setData(WStatusType.KO);
//				close();
//			}
//		});
		displayWindowContent();
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		VerticalLayout contentPanelVL = new VerticalLayout();
		contentPanelVL.setSpacing(true);
		contentPanelVL.setMargin(false);
		contentPanelVL.setSizeFull();
		TextArea ta =new TextArea();
		ta.setValue(message);
		ta.setReadOnly(true);
		ta.setSizeFull();
		contentPanelVL.addComponents(ta);

		Panel conPanel = new Panel();
		conPanel.setSizeFull();
		conPanel.setStyleName("withoutborder");
		conPanel.setContent(contentPanelVL);

		windowVL.addComponent(conPanel);
		windowVL.addComponent(getFooterHL());
		windowVL.setExpandRatio(conPanel, 1.0f);
	}

	private HorizontalLayout getFooterHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		hl.setMargin(false);
		HorizontalLayout spacer = new HorizontalLayout();
		hl.addComponents(spacer, getYesButton(), getNoButton());
		hl.setExpandRatio(spacer, 1.0f);
		return hl;
	}

	private Button getYesButton() {
		Button btn = new Button("Yes");
		btn.addClickListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				setData(WStatusType.OK);
				close();
			}
		});
		return btn;
	}

	private Button getNoButton() {
		Button btn = new Button("No");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				close();
			}
		});
		return btn;
	}

}
