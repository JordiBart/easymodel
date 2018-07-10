package cat.udl.easymodel.vcomponent.results;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.vcomponent.AppPanel;

public class ResultsVL extends VerticalLayout {

	private SessionData sessionData;
	// private SharedData sharedData;
	private Panel conPanel;
	private OutVL resVL;
	// private PopupView infoPopup = new PopupView(null, getInfoLayout());

	private AppPanel mainPanel;

	public ResultsVL(AppPanel mainPanel) {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		// this.sharedData = SharedData.getInstance();
		this.mainPanel = mainPanel;

		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(true);

		resVL = sessionData.getOutVL();

		conPanel = new Panel();
		conPanel.setSizeFull();
		// conPanel.setStyleName("reactionsPanel");
		conPanel.setContent(resVL);

		this.addComponent(conPanel);
		this.setComponentAlignment(conPanel, Alignment.MIDDLE_CENTER);
	}

	// private VerticalLayout getInfoLayout() {
	// VerticalLayout vlt = new VerticalLayout();
	// vlt.addComponent(new Label("Simulation results are displayed in this
	// layout"));
	// vlt.addComponent(new Label("Left click on images to view an enlarged version
	// of them in a new tab"));
	// return vlt;
	// }
	//
	// private Button getInfoButton() {
	// Button btn = new Button();
	// btn.setDescription("Results information");
	// btn.setWidth("36px");
	// btn.setStyleName("infoBtn");
	// btn.addClickListener(new ClickListener() {
	// private static final long serialVersionUID = 1L;
	//
	// public void buttonClick(ClickEvent event) {
	// infoPopup.setPopupVisible(true);
	// }
	// });
	// return btn;
	// }
}
