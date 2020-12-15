package cat.udl.easymodel.vcomponent.results;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.app.AppPanel;

public class ResultsVL extends VerticalLayout {

	private SessionData sessionData;
	// private SharedData sharedData;
	private OutVL outVL;
	private SimStatusHL statusHL;
	// private PopupView infoPopup = new PopupView(null, getInfoLayout());

	private AppPanel mainPanel;

	public ResultsVL() {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.mainPanel = this.sessionData.getAppPanel();
		// this.sharedData = SharedData.getInstance();

		this.setSizeFull();
		this.setMargin(false);
		this.setSpacing(false);

		outVL = sessionData.getOutVL();
		HorizontalLayout bottomHL = ToolboxVaadin.getRawHL("resultsBottom");
		bottomHL.setWidth("100%");
		bottomHL.setHeight("32px");
		statusHL = sessionData.getSimStatusHL();
		HorizontalLayout wmHL = ToolboxVaadin.getRawHL("resultsBottomWM");
		wmHL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		wmHL.addComponent(getWebMathematicaLink());
		wmHL.setWidth("210px");
		wmHL.setHeight("100%");
		bottomHL.addComponents(statusHL,wmHL);
		bottomHL.setExpandRatio(statusHL, 1f);

		Panel conPanel = new Panel();
		conPanel.setStyleName("withoutborder");
		conPanel.setSizeFull();
		conPanel.setContent(outVL);

//		VerticalLayout outerPanelVL = new VerticalLayout();
//		outerPanelVL.setSizeFull();
//		outerPanelVL.setSpacing(false);
//		//outerPanelVL.setMargin(false);
//		outerPanelVL.setStyleName("resultsOuterPanel");
//		outerPanelVL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
//		outerPanelVL.addComponent(conPanel);

		this.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		this.addComponents(conPanel, bottomHL);
		this.setExpandRatio(conPanel, 1f);
	}

	private Link getWebMathematicaLink() {
		Link link = new Link("", new ExternalResource("http://www.wolfram.com/webmathematica/sitelink"));
		link.setIcon(new ThemeResource("img/webm-trans-tiny.png"));
		link.setTargetName("_blank");
		link.setStyleName("resultsWebMLogo");
		return link;
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
