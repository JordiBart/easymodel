package cat.udl.easymodel.vcomponent.selectmodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.SerializablePredicate;
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
import cat.udl.easymodel.vcomponent.common.InfoWindow;
import cat.udl.easymodel.vcomponent.common.InfoWindowButton;

public class SelectModelRepositoryVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	private RadioButtonGroup<RepositoryType> pubPrivGroup;
	private SessionData sessionData;

	private ByteArrayOutputStream baos = null;
	private Upload upload;
	private SelectModelListVL selectModelListVL;
	private AppPanel mainPanel;

	public SelectModelRepositoryVL(AppPanel mainPanel, SelectModelListVL sm) {
		super();

		this.mainPanel = mainPanel;
		this.selectModelListVL = sm;
		this.sessionData = (SessionData) UI.getCurrent().getData();

		this.setWidth("200px");
		this.setHeight("100%");
		this.setSpacing(false);
		this.setMargin(true);
		this.addStyleName("panelBorder");
		this.addStyleName("v-scrollable");
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
		group.setIcon(VaadinIcons.DATABASE);
		group.setItems(RepositoryType.PUBLIC, RepositoryType.PRIVATE);
		group.setItemEnabledProvider(new SerializablePredicate<RepositoryType>() {
			@Override
			public boolean test(RepositoryType t) {
				if (!sessionData.isUserSet() && t == RepositoryType.PRIVATE)
					return false;
				return true;
			}
		});
		group.setItemCaptionGenerator(RepositoryType::getString);
		group.addValueChangeListener(new ValueChangeListener<RepositoryType>() {
			@Override
			public void valueChange(ValueChangeEvent<RepositoryType> event) {
				sessionData.setRepository(event.getValue());
				selectModelListVL.update();
			}
		});
		if (sessionData.isUserSet())
			group.setSelectedItem(RepositoryType.PRIVATE);
		else
			group.setSelectedItem(RepositoryType.PUBLIC);
		return group;
	}

	private HorizontalLayout getHeaderHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setHeight("37px");
		HorizontalLayout spacer = new HorizontalLayout();
		hl.addComponents(spacer, getInfoButton());
		hl.setExpandRatio(spacer, 1f);
		return hl;
	}
	
	// SBML import
	private Upload getUpload() {
		Upload upload = new Upload("Import SBML file", getReceiver());
		upload.setButtonCaption("Select SBML");
		upload.setDescription("Import SBML model file");
		upload.setIcon(VaadinIcons.FILE);
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
					SBMLMan sbmlMan = new SBMLMan(sessionData);
					Model m = sbmlMan.importSBML(new ByteArrayInputStream(baos.toByteArray()), report, null, false);
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
		return new InfoWindowButton("About repositories",
				"-Public repository: Public data. Changes won't be saved into database.\n"
						+ "-Private repository: Private user's data. Changes will be saved into database when model is validated. Private models will automatically become public after "
						+ SharedData.getInstance().getProperties().getProperty("privateWeeks")
						+ " weeks since last modification. To avoid publishing a model, please delete it before this time frame.\n"
						+ "-Importing SBML model: Model will be imported but data won't be saved into database.\n"
						+ "(Guest users can't save any data)\n",
				800, 300);
	}
}
