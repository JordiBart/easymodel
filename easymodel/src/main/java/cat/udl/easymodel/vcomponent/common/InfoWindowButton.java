package cat.udl.easymodel.vcomponent.common;

import com.vaadin.ui.Button;
import com.vaadin.ui.UI;

import cat.udl.easymodel.main.SessionData;

public class InfoWindowButton extends Button {
	private SessionData sessionData;

	public InfoWindowButton(String tittle, String content, int width, int height) {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.setDescription(tittle);
		this.setWidth("36px");
		this.setStyleName("infoBtn");
		this.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				sessionData.showInfoWindow(tittle, content, width, height);
			}
		});
	}
}
