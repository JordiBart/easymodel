package cat.udl.easymodel.vcomponent.common;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

public class SpacedLabel extends Label {
	private static final long serialVersionUID = 1L;

	public SpacedLabel(String msg) {
		super();
		this.setContentMode(ContentMode.HTML);
		this.setValue("&nbsp;"+msg+"&nbsp;");
	}
}
