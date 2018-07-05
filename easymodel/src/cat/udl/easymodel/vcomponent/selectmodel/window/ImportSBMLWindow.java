package cat.udl.easymodel.vcomponent.selectmodel.window;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
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
import cat.udl.easymodel.sbml.SBMLTools;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ImportSBMLWindow extends Window {
	private static final long serialVersionUID = 1L;

	private VerticalLayout windowVL;
	private ImportSBMLWindow thisClass;
	private SessionData sessionData;

	private ByteArrayOutputStream baos=null;
	private Upload upload;
	private ProgressBar progressBar;

	public ImportSBMLWindow() {
		super();

		thisClass = this;
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.setCaption("Import SBML file");
		this.setClosable(true);
		this.setData(false); // window closed by user
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(false);
		this.center();
		this.setWidth("300px");
		this.setHeight("210px");

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		// this.addShortcutListener(new ShortcutListener("Shortcut enter",
		// ShortcutAction.KeyCode.ENTER, null) {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void handleAction(Object sender, Object target) {
		// checkAndClose();
		// }
		// });
		displayWindowContent();
		upload.focus();
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		upload = getUpload();
		progressBar = getProgressBar();

		VerticalLayout valuesPanelVL = new VerticalLayout();
		valuesPanelVL.setSpacing(true);
		valuesPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		valuesPanelVL.addComponents(upload, progressBar);

		Panel valuesPanel = new Panel();
		valuesPanel.setSizeFull();
		valuesPanel.setStyleName("withoutborder");
		valuesPanel.setContent(valuesPanelVL);

		windowVL.addComponent(valuesPanel);
		windowVL.setExpandRatio(valuesPanel, 1.0f);
	}

	private ProgressBar getProgressBar() {
		ProgressBar bar = new ProgressBar(0.0f);
		bar.setCaption("Upload progress");
		return bar;
	}

	private Upload getUpload() {
		Upload upload = new Upload("Select SBML file from disk (or drag here)", getReceiver());
		upload.setButtonCaption("Import SBML");
		upload.addSucceededListener(getSucceededListener());
		upload.addFailedListener(getFailedListener());
		upload.addProgressListener(getProgressListener());
		upload.setErrorHandler(new ErrorHandler() {
			
			@Override
			public void error(com.vaadin.server.ErrorEvent event) {
				// TODO Auto-generated method stub
				
			}
		});
		return upload;
	}

	private ProgressListener getProgressListener() {
		return new ProgressListener() {
			@Override
			public void updateProgress(long readBytes, long contentLength) {
				float newVal = readBytes / contentLength;
				progressBar.setValue(newVal);
			}
		};
	}

	private Receiver getReceiver() {
		return new Receiver() {

			@Override
			public OutputStream receiveUpload(String filename, String mimeType) {
				if (!"text/xml".equals(mimeType))
					return null;
				try {
					baos = new ByteArrayOutputStream();
				} catch (Exception e) {
					return null;
				}
				return baos;
			}
		};
	}

	private SucceededListener getSucceededListener() {
		return new SucceededListener() {

			public void uploadSucceeded(SucceededEvent event) {
				try {
					Model m = SBMLTools.importSBML(new ByteArrayInputStream(baos.toByteArray()));
					sessionData.setSelectedModel(m);
					thisClass.setData(true);
					thisClass.close();
				} catch (Exception e) {
					Notification.show("SBML file contains errors", Type.WARNING_MESSAGE);
					e.printStackTrace();
				}
			}
		};
	}

	private FailedListener getFailedListener() {
		return new FailedListener() {

			@Override
			public void uploadFailed(FailedEvent event) {
				new Notification("Upload failed (is file an SBML/XML?)", Notification.Type.WARNING_MESSAGE)
						.show(Page.getCurrent());
			}
		};
	}
}
