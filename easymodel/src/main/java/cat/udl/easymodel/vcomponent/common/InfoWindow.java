package cat.udl.easymodel.vcomponent.common;

import java.io.File;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import cat.udl.easymodel.logic.types.WStatusType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.p;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class InfoWindow extends Window {
	private static final long serialVersionUID = 1L;

	private VerticalLayout windowVL;
	private SessionData sessionData;

	public InfoWindow(SessionData sessionData) {
		super();
		this.sessionData = sessionData;

//		this.setCaption("Information");
		this.setClosable(true);
		this.setStyleName("info");
		// this.setModal(true);
		this.setDraggable(true);
		this.setResizable(true);
		if (this.sessionData.getVaadinService() != null) {
			FileResource iconResource = new FileResource(
					new File(this.sessionData.getVaadinService().getBaseDirectory().getAbsolutePath()
							+ "/VAADIN/themes/easymodel/img/info.png"));
			this.setIcon(iconResource);
		}
		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		this.addShortcutListener(new ShortcutListener("Shortcut escape", ShortcutAction.KeyCode.ESCAPE, null) {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {
				close();
			}
		});
		this.addShortcutListener(new ShortcutListener("Shortcut enter", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {
				close();
			}
		});
		this.addShortcutListener(new ShortcutListener("Shortcut space", ShortcutAction.KeyCode.SPACEBAR, null) {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {
				close();
			}
		});
	}

	public void updateContent(String message, int w, int h) {
		// reset position/size
		this.setWindowMode(WindowMode.NORMAL);
		this.setWidth(w + "px");
		this.setHeight(h + "px");
		this.center();

		windowVL.removeAllComponents();

		TextArea ta = new TextArea();
		ta.setSizeFull();
		ta.setValue(message);
		ta.setReadOnly(true);

		windowVL.addComponent(ta);
	}
}
