package cat.udl.easymodel.vcomponent.selectmodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.sbml.SBMLMan;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.app.AppPanel;

public class SelectModelRepositoryVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	private RadioButtonGroup<RepositoryType> pubPrivGroup;
	private SessionData sessionData;
	private PopupView infoPopup = new PopupView(null, getInfoLayout());

	private ByteArrayOutputStream baos = null;
	private Upload upload;
	private SelectModelListVL selectModelListVL;
	private AppPanel mainPanel;

	public SelectModelRepositoryVL(AppPanel mainPanel, SelectModelListVL sm) {
		super();

		this.mainPanel = mainPanel;
		this.selectModelListVL = sm;
		this.sessionData = (SessionData) UI.getCurrent().getData();

		this.setWidth("250px");
		this.setHeight("100%");
		this.setSpacing(true);
		this.setMargin(true);
		this.setStyleName("selectModel");
		this.setDefaultComponentAlignment(Alignment.TOP_LEFT);

		update();
	}

	private void update() {
		HorizontalLayout headerHL = getHeaderHL();
		pubPrivGroup = getPubPrivOptionGroup();
		upload = getUpload();
		VerticalLayout spacer = new VerticalLayout();

		removeAllComponents();
		addComponents(headerHL, pubPrivGroup, ToolboxVaadin.getHR(), upload, spacer);
		setExpandRatio(spacer, 1f);
	}

	private RadioButtonGroup<RepositoryType> getPubPrivOptionGroup() {
		RadioButtonGroup<RepositoryType> group = new RadioButtonGroup<>("Model Repository");
		group.setItems(RepositoryType.PUBLIC, RepositoryType.PRIVATE);
		group.setItemCaptionGenerator(RepositoryType::getString);
		group.addValueChangeListener(new ValueChangeListener<RepositoryType>() {
			@Override
			public void valueChange(ValueChangeEvent<RepositoryType> event) {
				sessionData.setRepository(event.getValue());
				selectModelListVL.update();
			}
		});
		if (sessionData.getRepository() != null)
			group.setSelectedItem(sessionData.getRepository());
		else
			group.setSelectedItem(RepositoryType.PUBLIC);
		return group;
	}

	private HorizontalLayout getHeaderHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setHeight("37px");
		HorizontalLayout spacer = new HorizontalLayout();
		hl.addComponents(spacer, infoPopup, getInfoButton());
		hl.setExpandRatio(spacer, 1f);
		return hl;
	}
	// SBML import

	private Upload getUpload() {
		Upload upload = new Upload("Import SBML file from disk", getReceiver());
		upload.setButtonCaption("Import SBML");
		upload.setDescription("Import SBML model file from disk");
		upload.setWidth("100%");
		upload.addSucceededListener(getSucceededListener());
		upload.addFailedListener(getFailedListener());
		upload.setErrorHandler(new ErrorHandler() {
			@Override
			public void error(com.vaadin.server.ErrorEvent event) {
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
					Model m = SBMLMan.getInstance().importSBML(new ByteArrayInputStream(baos.toByteArray()), report,
							null);
					if (report.length() > 0)
						Notification.show("SBML import report:\n" + report.toString(), Type.WARNING_MESSAGE);
					sessionData.setSelectedModel(m);
					sessionData.setRepository(RepositoryType.TEMP);
					mainPanel.showEditModel();
				} catch (Exception e) {
					Notification.show("SBML file contains errors: " + e.getMessage(), Type.WARNING_MESSAGE);
					e.printStackTrace();
				}
			}
		};
	}

	private FailedListener getFailedListener() {
		return new FailedListener() {

			@Override
			public void uploadFailed(FailedEvent event) {
				new Notification("Upload failed (is file XML/SBML?)", Notification.Type.WARNING_MESSAGE)
						.show(Page.getCurrent());
			}
		};
	}

	// INFO
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
		vlt.setWidth("420px");
		Label lab;
		lab = new Label("-Public repository: Public data. Changes won't be saved into database.");
		lab.setWidth("400px");
		vlt.addComponent(lab);
		lab = new Label(
				"-Private repository: Private user's data. Changes will be saved into database when model is validated. Private models will automatically become public after "
						+ SharedData.getInstance().getProperties().getProperty("privateWeeks")
						+ " weeks since last modification. To avoid publishing a model, please delete it before this time frame.");
		lab.setWidth("400px");
		vlt.addComponent(lab);
		lab = new Label("-Importing SBML model: Model will be imported but data won't be saved into database.");
		lab.setWidth("400px");
		vlt.addComponent(lab);
		lab = new Label("(Guest users can't save any data)");
		lab.setWidth("400px");
		vlt.addComponent(lab);
		return vlt;
	}
}
