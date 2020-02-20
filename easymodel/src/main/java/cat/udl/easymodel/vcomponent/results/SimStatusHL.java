package cat.udl.easymodel.vcomponent.results;

import java.io.File;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.thread.SimulationCancelThread;
import cat.udl.easymodel.utils.p;

public class SimStatusHL extends HorizontalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7149743614255788036L;
	private SessionData sessionData;
	private boolean isCancelBtnClicked=false;

	public SimStatusHL(SessionData sessionData) {
		super();
		this.sessionData = sessionData;
		setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		setWidth("100%");
		setHeight("100%");
		setMargin(false);
		setSpacing(false);
	}

	public void error(String errorMsg) {
		sessionData.getUi().getSession().lock();
		removeAllComponents();
		setStyleName("resultsStatusError");
		Label statusLabel = new Label("Simulation error: " + errorMsg);
		statusLabel.setStyleName("resultsStatus");
		HorizontalLayout spacer = new HorizontalLayout();
		addComponents(statusLabel, spacer);
		setExpandRatio(spacer, 1f);
		//sessionData.getUi().push();
		sessionData.getUi().getSession().unlock();
	}

	public void finish() {
		sessionData.getUi().getSession().lock();
		removeAllComponents();
		setStyleName("resultsStatusFinish");
		Label statusLabel = new Label("Simulation finished");
		statusLabel.setStyleName("resultsStatus");
		HorizontalLayout spacer = new HorizontalLayout();
		addComponents(statusLabel, spacer);
		setExpandRatio(spacer, 1f);
		sessionData.getUi().getSession().unlock();
	}

	public void running() {
		sessionData.getUi().getSession().lock();
		isCancelBtnClicked=false;
		removeAllComponents();
		setStyleName("resultsStatusRunning");
		Label statusLabel = new Label("Running simulation, please wait");
		statusLabel.setStyleName("resultsStatus");
		Button cancelBtn = new Button("Cancel");
		cancelBtn.setStyleName("resultsCancel");
		cancelBtn.setWidth("64px");
		cancelBtn.setHeight("26px");
		cancelBtn.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				if (sessionData.isSimulating()) {
					if (!isCancelBtnClicked) {
						isCancelBtnClicked=true;
						cancelling();
						sessionData.cancelSimulation();
					}
				}
			}
		});
		FileResource resource = new FileResource(
				new File(sessionData.getVaadinService().getBaseDirectory().getAbsolutePath()
						+ "/VAADIN/themes/easymodel/img/runningSim.png"));
		Image dots = new Image(null, resource);
		dots.setHeight("25px");
		HorizontalLayout spacer = new HorizontalLayout();
		addComponents(statusLabel, dots, cancelBtn, spacer);
		setExpandRatio(spacer, 1f);
		sessionData.getUi().getSession().unlock();
	}
	
	public void cancelling() {
		sessionData.getUi().getSession().lock();
		removeAllComponents();
		setStyleName("resultsStatusRunning");
		Label statusLabel = new Label("Cancelling");
		statusLabel.setStyleName("resultsStatus");
		FileResource resource = new FileResource(
				new File(sessionData.getVaadinService().getBaseDirectory().getAbsolutePath()
						+ "/VAADIN/themes/easymodel/img/runningSim.png"));
		Image dots = new Image(null, resource);
		dots.setHeight("25px");
		HorizontalLayout spacer = new HorizontalLayout();
		addComponents(statusLabel, dots, spacer);
		setExpandRatio(spacer, 1f);
		sessionData.getUi().getSession().unlock();
	}
}
