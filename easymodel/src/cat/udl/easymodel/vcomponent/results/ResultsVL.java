package cat.udl.easymodel.vcomponent.results;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import cat.udl.easymodel.controller.SimulationCtrl;
import cat.udl.easymodel.controller.SimulationCtrlImpl;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.vcomponent.AppPanel;

import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class ResultsVL extends VerticalLayout {

	private SessionData sessionData;
//	private SharedData sharedData;
	private Panel conPanel;
	private OutVL resVL;
	private Accordion accordion;

	private Model selectedModel;
	private AppPanel mainPanel;

	public ResultsVL(Model selectedModel, AppPanel mainPanel) {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
//		this.sharedData = SharedData.getInstance();
		this.selectedModel = selectedModel;
		this.mainPanel = mainPanel;

		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(true);

		resVL = sessionData.getOutVL();

		conPanel = new Panel();
		conPanel.setSizeFull();
//		conPanel.setStyleName("reactionsPanel");
		conPanel.setContent(resVL);

		this.addComponent(conPanel);
		this.setComponentAlignment(conPanel, Alignment.MIDDLE_CENTER);
	}
}
