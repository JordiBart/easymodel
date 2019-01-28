package cat.udl.easymodel.vcomponent.app;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickListener;

public class AppIntroVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	
	public AppIntroVL(ClickListener selectModelClickListener) {
		super();
		
		Label lab = new Label("No model selected");
		lab.setSizeUndefined();
		Button selectModelBtn = new Button("Select Model");
		selectModelBtn.addClickListener(selectModelClickListener);
		VerticalLayout middleVL = new VerticalLayout();
		middleVL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		middleVL.setSizeUndefined();
		middleVL.addComponents(lab,selectModelBtn);
		
		this.setSizeFull();
		this.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		this.addComponent(middleVL);
	}
}
