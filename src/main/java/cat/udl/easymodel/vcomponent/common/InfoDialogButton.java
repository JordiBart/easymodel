package cat.udl.easymodel.vcomponent.common;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;

public class InfoDialogButton extends Button {
	String title="Information", content="", width="500px", height = "500px";
	public InfoDialogButton(String title_, String content_, String width_, String height_) {
		super();
		this.title=title_;
		this.content=content_;
		this.width=width_;
		this.height=height_;
		this.setIcon(VaadinIcon.INFO_CIRCLE.create());
		this.setTooltipText("Information");
		this.addClickListener(e->{
			InfoDialog dialog = new InfoDialog(title, content, width, height);
			dialog.open();
		});
	}
	public void setContent(String content_){
		this.content=content_;
//		this.title=title;
//		this.width=width;
//		this.height=height;
	}
}
