package cat.udl.easymodel.vcomponent.selectmodel.window;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.sbml.SBMLMan;
import cat.udl.easymodel.utils.VaadinUtils;

public class SelectModelRepositoryWindow extends Window {
	private static final long serialVersionUID = 1L;

	private VerticalLayout windowVL;
	private OptionGroup pubPrivGroup;
	private SessionData sessionData;
	private RepositoryType lastUsedRepository=RepositoryType.PUBLIC;
	private Button publicBtn=getRepositoryButton(RepositoryType.PUBLIC);
	private Button privateBtn=getRepositoryButton(RepositoryType.PRIVATE);
	private PopupView infoPopup = new PopupView(null, getInfoLayout());
	
	private SelectModelRepositoryWindow thisWindow;
	private ByteArrayOutputStream baos=null;
	private Upload upload;

	public SelectModelRepositoryWindow() {
		super();

		this.thisWindow=this;
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.setCaption("Select model repository");
//		p.p(sessionData.getSelectedModel());
//		if (sessionData.getSelectedModel() == null)
//			this.setClosable(false);
//		else
			this.setClosable(true);
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(false);
		this.center();
		this.setWidth("280px");
		this.setHeight("310px");

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

//		this.addShortcutListener(new ShortcutListener("Shortcut enter", ShortcutAction.KeyCode.ENTER, null) {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void handleAction(Object sender, Object target) {
//				checkAndClose();
//			}
//		});
		displayWindowContent();
//		publicBtn.focus();
		this.reset();
	}

	public void reset() {
		if (lastUsedRepository==RepositoryType.PUBLIC)
			publicBtn.focus();
		else
			privateBtn.focus();
		this.setData(false); // window closed by user
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

//		pubPrivGroup = getPubPrivOptionGroup();
		VerticalLayout pubPrivButtonsVL = getPubPrivButtonsVL();
		upload = getUpload();
		
		HorizontalLayout headerHL = getHeaderHL();
		
		VerticalLayout valuesPanelVL = new VerticalLayout();
		valuesPanelVL.setSpacing(true);
		valuesPanelVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		VerticalLayout spacer = new VerticalLayout();
		spacer.setHeight("8px");
		valuesPanelVL.addComponents(headerHL,pubPrivButtonsVL,upload);

		Panel valuesPanel = new Panel();
		valuesPanel.setSizeFull();
		valuesPanel.setStyleName("withoutborder");
		valuesPanel.setContent(valuesPanelVL);

		windowVL.addComponent(valuesPanel);
//		windowVL.addComponent(getFooterHL());
		windowVL.setExpandRatio(valuesPanel, 1.0f);
	}

	private HorizontalLayout getHeaderHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setHeight("37px");
		HorizontalLayout head = new HorizontalLayout();
		head.setWidth("100%");
		head.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		hl.addComponents(head, infoPopup, getInfoButton());
		hl.setExpandRatio(head, 1f);
		return hl;
	}
	
	private VerticalLayout getPubPrivButtonsVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setCaption("Select model repository");
		vl.setSpacing(true);
		vl.setDefaultComponentAlignment(Alignment.TOP_CENTER);
		vl.addComponents(publicBtn,privateBtn);
		return vl;
	}

	private Button getRepositoryButton(RepositoryType rt) {
		Button btn = new Button(rt.getString(),new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				sessionData.setModelsRepo(rt);
				lastUsedRepository=rt;
				thisWindow.setData(true);
				close();
			}
		});
		btn.setWidth("100%");
		return btn;
	}
	
	private OptionGroup getPubPrivOptionGroup() {
		OptionGroup group = new OptionGroup("Model repository");
		if (group.addItem(RepositoryType.PUBLIC) != null)
			group.setItemCaption(RepositoryType.PUBLIC, RepositoryType.PUBLIC.getString());
		if (group.addItem(RepositoryType.PRIVATE) != null)
			group.setItemCaption(RepositoryType.PRIVATE, RepositoryType.PRIVATE.getString());
//		if (group.addItem(RepositoryType.TEMP) != null)
//			group.setItemCaption(RepositoryType.TEMP, "Import from SBML file");
		// load value
		if (sessionData.getRepository() != null)
			group.select(sessionData.getRepository());
		else
			group.select(RepositoryType.PUBLIC);
		return group;
	}

	private HorizontalLayout getFooterHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		VerticalLayout spacerVL = new VerticalLayout();
		spacerVL.setWidth("100%");
		hl.addComponents(spacerVL, getNextButton());
		hl.setExpandRatio(spacerVL, 1.0f);
		return hl;
	}

	private Button getImportSBMLButton() {
		Button btn = new Button("Import SBML");
		btn.setDescription("Import model from a local device SBML file");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				ImportSBMLWindow importSBMLW = getImportSBMLWindow();
				UI.getCurrent().addWindow(importSBMLW);
			}

			private ImportSBMLWindow getImportSBMLWindow() {
				ImportSBMLWindow importSBMLW = new ImportSBMLWindow();
				importSBMLW.addCloseListener(new CloseListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void windowClose(CloseEvent e) {
						if ((boolean) e.getWindow().getData()) {
							setData(true);
							sessionData.setModelsRepo(RepositoryType.TEMP);
							close();
						}
					}
				});
				return importSBMLW;
			}
		});
		return btn;
	}

	private Button getNextButton() {
		Button btn = new Button("Next");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				checkAndClose();
			}
		});
		return btn;
	}

	private void checkAndClose() {
		if (pubPrivGroup.getValue() != null) {
			sessionData.setModelsRepo((RepositoryType) pubPrivGroup.getValue());
			this.setData(true); // window closed after check
			close();
		} else {
			Notification.show("Please select a model repository");
		}
	}
	
	// SBML import
	
	private Upload getUpload() {
		Upload upload = new Upload("Or import SBML file from disk", getReceiver());
		upload.setButtonCaption("Import SBML");
		upload.setWidth("100%");
		upload.addSucceededListener(getSucceededListener());
		upload.addFailedListener(getFailedListener());
		upload.setErrorHandler(new ErrorHandler() {
			@Override
			public void error(com.vaadin.server.ErrorEvent event) {
				// TODO Auto-generated method stub
				
			}
		});
		return upload;
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
					StringBuilder report = new StringBuilder();
					Model m = SBMLMan.getInstance().importSBML(new ByteArrayInputStream(baos.toByteArray()), report, null);
					sessionData.setSelectedModel(m);
					sessionData.setModelsRepo(RepositoryType.TEMP);
					thisWindow.setData(true);
					thisWindow.close();
					if (report.length() > 0)
						Notification.show("SBML import report:\n"+report.toString(), Type.WARNING_MESSAGE);	
				} catch (Exception e) {
					Notification.show("SBML file contains errors: "+e.getMessage(), Type.WARNING_MESSAGE);
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
	// SBML import end
	private Button getInfoButton() {
		Button btn = new Button();
		btn.setDescription("About repositories");
		btn.setWidth("36px");
		btn.setStyleName("infoBtn");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				infoPopup.setPopupVisible(true);
			}
		});
		return btn;
	}

	private VerticalLayout getInfoLayout() {
		VerticalLayout vlt = new VerticalLayout();
		vlt.setWidth("400px");
		vlt.addComponent(new Label("·Public repository: Public data. Changes won't be saved into database."));
		vlt.addComponent(new Label("·Private repository: Private user's data. Changes will be saved into database when model is validated. Private models will automatically become public after "+SharedData.privateWeeks+" weeks since last modification. To avoid publishing a model, please delete it before this time frame."));
		vlt.addComponent(new Label("·Importing SBML model: Model will be imported but data won't be saved into database."));
		vlt.addComponent(new Label("·Guest users can't save any data."));
		return vlt;
	}
}
