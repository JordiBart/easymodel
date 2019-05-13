package cat.udl.easymodel.vcomponent.results;

import java.io.File;

import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import cat.udl.easymodel.main.SessionData;

public class SimStatusHL extends HorizontalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7149743614255788036L;
	private SessionData sessionData;
	public SimStatusHL() {
		super();
		sessionData = (SessionData) UI.getCurrent().getData();
		setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);
		setWidth("100%");
		setMargin(false);
		setSpacing(false);
	}

	public void error(String errorMsg) {
		removeAllComponents();
		setStyleName("resultsStatusError");
		Label statusLabel = new Label("Simulation error: " + errorMsg);
		statusLabel.setStyleName("resultsStatus");
		HorizontalLayout spacer = new HorizontalLayout();
		addComponents(statusLabel, spacer);
		setExpandRatio(spacer, 1f);
		sessionData.getUi().push();
	}

	public void finish() {
		removeAllComponents();
		setStyleName("resultsStatusFinish");
		Label statusLabel = new Label("Simulation finished");
		statusLabel.setStyleName("resultsStatus");
		HorizontalLayout spacer = new HorizontalLayout();
		addComponents(statusLabel, spacer);
		setExpandRatio(spacer, 1f);
		sessionData.getUi().push();
	}
	
	public void reset() {
		removeAllComponents();
		setStyleName("resultsStatusRunning");
		Label statusLabel = new Label("Running simulation, please wait");
		statusLabel.setStyleName("resultsStatus");
		Button cancelBtn = new Button("Cancel");
		cancelBtn.setStyleName("resultsCancel");
		cancelBtn.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				sessionData.getSimulationThread().cancel();
			}
		});
		FileResource resource = new FileResource(
				new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()
						+ "/VAADIN/themes/easymodel/img/runningSim.png"));
		Image dots = new Image(null, resource);
		dots.setHeight("25px");
		HorizontalLayout spacer = new HorizontalLayout();
		addComponents(statusLabel, dots, cancelBtn, spacer);
		setExpandRatio(spacer, 1f);
	}
}
