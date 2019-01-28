package cat.udl.easymodel.vcomponent.model.window;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.ProgressListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.sbml.SBMLMan;
import cat.udl.easymodel.utils.VaadinUtils;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class RenderDescriptionHTMLWindow extends Window {
	private static final long serialVersionUID = 1L;

	private VerticalLayout windowVL;
	private SessionData sessionData;

	public RenderDescriptionHTMLWindow() {
		super();

		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.setCaption("Model description rendered as HTML");
		this.setClosable(true);
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(true);
		this.center();
		this.setWidth("90%");
		this.setHeight("90%");

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		displayWindowContent();
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		Panel valuesPanel = new Panel();
		valuesPanel.setSizeFull();
		valuesPanel.setStyleName("withoutborder");
		valuesPanel.setContent(getDescriptionLayout());

		windowVL.addComponent(valuesPanel);
		windowVL.setExpandRatio(valuesPanel, 1.0f);
	}

	private Component getDescriptionLayout() {
		CustomLayout cl;
		try {
			String html = VaadinUtils.sanitizeHTML(sessionData.getSelectedModel().getDescription());
			cl = new CustomLayout(new ByteArrayInputStream(html.getBytes()));
		} catch (IOException e) {
			cl = new CustomLayout();
			e.printStackTrace();
		}
		return cl;
	}
}
