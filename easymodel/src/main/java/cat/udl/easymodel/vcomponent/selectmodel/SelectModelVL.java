package cat.udl.easymodel.vcomponent.selectmodel;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.vcomponent.app.AppPanel;

public class SelectModelVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;

//	public AppIntroVL(ClickListener selectModelClickListener) {
//		super();
//		
//		Label lab = new Label("No model selected");
//		lab.setSizeUndefined();
//		Button selectModelBtn = new Button("Select Model");
//		selectModelBtn.addClickListener(selectModelClickListener);
//		VerticalLayout middleVL = new VerticalLayout();
//		middleVL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
//		middleVL.setSizeUndefined();
//		middleVL.addComponents(lab,selectModelBtn);
//		
//		this.setSizeFull();
//		this.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
//		this.addComponent(middleVL);
//	}
	private AppPanel mainPanel;

	public SelectModelVL(AppPanel mainPanel) {
		super();
		this.mainPanel = mainPanel;
		setSpacing(false);
		setMargin(true);
		setSizeFull();

		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.setMargin(false);
		hl.setSizeFull();
		SelectModelListVL sm = new SelectModelListVL(mainPanel);
		hl.addComponents(new SelectModelRepositoryVL(mainPanel, sm), sm);
		hl.setExpandRatio(sm, 1f);
		this.addComponent(hl);
	}
}
