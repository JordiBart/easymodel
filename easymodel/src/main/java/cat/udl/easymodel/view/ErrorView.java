package cat.udl.easymodel.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;

public class ErrorView extends CustomComponent implements View {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "error";

	private SessionData sessionData;
	private SharedData sharedData = SharedData.getInstance();

	public ErrorView() {
	}

	@Override
	public void enter(ViewChangeEvent event) {
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.sessionData.clear();

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		Button btn = new Button("Go back to start page");
		btn.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().navigateTo(CoverView.NAME);
			}
		});
		viewLayout.addComponents(new Label("Ups, there was some error :("), btn);
		setCompositionRoot(viewLayout);
		setSizeFull();
	}
}