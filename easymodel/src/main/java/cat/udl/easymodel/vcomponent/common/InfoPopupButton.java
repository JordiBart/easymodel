package cat.udl.easymodel.vcomponent.common;

import java.io.File;

import com.vaadin.server.FileResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.main.SessionData;

public class InfoPopupButton extends HorizontalLayout {
	private PopupView popup;
	private SessionData sessionData;
	private Button button;
	private TextArea textArea;
	
	public InfoPopupButton(String caption, String content, Integer width, Integer height){
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.setSpacing(false);
		this.setMargin(false);
		this.popup = new PopupView(null,getLargeComponent(caption, content, width, height));
		button = generateButton(caption);
		this.addComponents(popup,button);
	}
	
	private Button generateButton(String caption) {
		Button btn =new Button();
		btn.setDescription(caption);
		btn.setWidth("36px");
		btn.setStyleName("infoBtn");
		btn.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				popup.setPopupVisible(true);
			}
		});
		return btn;
	}

	private Component getLargeComponent(String caption, String content, Integer width, Integer height) {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setMargin(true);
		
		HorizontalLayout header = new HorizontalLayout();
		header.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		header.setSpacing(true);
		header.setMargin(false);
		header.setWidth("100%");
		if (this.sessionData.getVaadinService() != null) {
			FileResource iconResource = new FileResource(
					new File(this.sessionData.getVaadinService().getBaseDirectory().getAbsolutePath()
							+ "/VAADIN/themes/easymodel/img/info.png"));
			Image im = new Image(null,iconResource);
			im.setWidth("36px");
			im.setHeight("36px");
			header.addComponents(im);
		}
		if (caption != null) {
			Label tittle = new Label(caption);
			tittle.setStyleName("popupHeader");
			header.addComponent(tittle);
		}
		VerticalLayout spacer = new VerticalLayout();
		header.addComponent(spacer);
		header.setExpandRatio(spacer, 1.0f);
		
		textArea = new TextArea(null, content);
		textArea.setReadOnly(true);
		textArea.setWidth("550px");
		textArea.setHeight("200px");
		if (width != null)
			textArea.setWidth(width + "px");
		if (height != null)
			textArea.setHeight(height + "px");
		
		vl.addComponents(header,textArea);
		return vl;
	}

	public Button getButton() {
		return button;
	}

	public TextArea getTextArea() {
		return textArea;
	}
}
