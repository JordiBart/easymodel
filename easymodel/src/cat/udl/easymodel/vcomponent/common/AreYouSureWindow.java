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

		this.setCaption(this.title);
		this.setClosable(false);
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(false);
		this.center();
		this.setWidth("300px");
		this.setHeight("200px");

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		this.addShortcutListener(new ShortcutListener("Shortcut enter", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {
				setData(WStatusType.OK);
				close();
			}
		});
		displayWindowContent();
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		VerticalLayout contentPanelVL = new VerticalLayout();
		contentPanelVL.setSpacing(true);
		contentPanelVL.setSizeFull();
		// contentPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		contentPanelVL.addComponents(new Label(message));

		Panel conPanel = new Panel();
		conPanel.setSizeFull();
		conPanel.setStyleName("withoutborder");
		conPanel.setContent(contentPanelVL);

		windowVL.addComponent(contentPanelVL);
		windowVL.addComponent(getFooterHL());
		windowVL.setExpandRatio(contentPanelVL, 1.0f);
	}

	private HorizontalLayout getFooterHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		VerticalLayout spacerVL = new VerticalLayout();
		// spacerVL.setWidth("100%");
		hl.addComponents(spacerVL, getYesButton(), getNoButton());
		hl.setExpandRatio(spacerVL, 1.0f);
		return hl;
	}

	private Button getYesButton() {
		Button btn = new Button("Yes");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

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
				setData(WStatusType.KO);
				close();
			}
		});
		return btn;
	}

}
