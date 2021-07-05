package cat.udl.easymodel.vcomponent.results;

import java.io.File;

import com.vaadin.server.FileResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

import cat.udl.easymodel.main.SessionData;

public class SimStatusHL extends HorizontalLayout {
	public enum SimStatusType {
		FINISH, RUNNING, CANCELLING, ERROR;
	}

	private static final long serialVersionUID = -7149743614255788036L;
	private SessionData sessionData;
	private SimStatusType simState = SimStatusType.FINISH;
	private UI ui;

	public SimStatusHL(SessionData sessionData) {
		super();
		this.sessionData = sessionData;
		this.ui = this.sessionData.getUI();
		setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		setWidth("100%");
		setHeight("100%");
		setMargin(false);
		setSpacing(false);
	}

	public void error(String errorMsg) {
		ui.access(new Runnable() {
			@Override
			public void run() {
				simState = SimStatusType.ERROR;
				removeAllComponents();
				setStyleName("resultsStatusError");
				Label statusLabel = new Label("Simulation error: " + errorMsg);
				statusLabel.setStyleName("resultsStatus");
				HorizontalLayout spacer = new HorizontalLayout();
				addComponents(statusLabel, spacer);
				setExpandRatio(spacer, 1f);
			}
		});
	}

	public void finish() {
		ui.access(new Runnable() {
			@Override
			public void run() {
				simState = SimStatusType.FINISH;
				removeAllComponents();
				setStyleName("resultsStatusFinish");
				Label statusLabel = new Label("Simulation finished");
				statusLabel.setStyleName("resultsStatus");
				HorizontalLayout spacer = new HorizontalLayout();
				addComponents(statusLabel, spacer);
				setExpandRatio(spacer, 1f);
			}
		});
	}

	public void running() {
		ui.access(new Runnable() {
			@Override
			public void run() {
//				isCancelBtnClicked = false;
				simState = SimStatusType.RUNNING;
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
						sessionData.getSimulationManager().cancelSimulationByUser();
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
			}
		});
	}

//	public void cancelling() {
////		sessionData.getUI().getSession().lock();
//		ui.access(new Runnable() {
//			@Override
//			public void run() {
//				simState = SimStatusType.CANCELLING;
//				removeAllComponents();
//				setStyleName("resultsStatusRunning");
//				Label statusLabel = new Label("Cancelling");
//				statusLabel.setStyleName("resultsStatus");
//				FileResource resource = new FileResource(
//						new File(sessionData.getVaadinService().getBaseDirectory().getAbsolutePath()
//								+ "/VAADIN/themes/easymodel/img/runningSim.png"));
//				Image dots = new Image(null, resource);
//				dots.setHeight("25px");
//				HorizontalLayout spacer = new HorizontalLayout();
//				addComponents(statusLabel, dots, spacer);
//				setExpandRatio(spacer, 1f);
//			}
//		});
////		sessionData.getUI().getSession().unlock();
//	}

	public SimStatusType getSimState() {
		return simState;
	}
}
