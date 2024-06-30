package cat.udl.easymodel.vcomponent.common;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

public class InfoDialog extends Dialog {
	private Span titleLbl;
	private TextArea ta;

	public InfoDialog(String title, String message, String w, String h){
		super();

		setModal(true);
		setDraggable(true);

		setHeight(h);
		setWidth(w);

		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setPadding(false);
		hl1.setSpacing(false);
		hl1.setWidth("100%");
		hl1.setHeight("30px");
		titleLbl = new Span();
		titleLbl.setText(title);
		titleLbl.getStyle().set("font-weight", "bold");
		HorizontalLayout spacer = new HorizontalLayout();
		Button closeBtn = new Button();
		closeBtn.setIcon(VaadinIcon.CLOSE.create());
		closeBtn.addThemeVariants(ButtonVariant.LUMO_ICON);
//        closeBtn.setWidth("30px");
//        closeBtn.setHeight("30px");
		closeBtn.addClickListener(ev ->{
			close();
		});
		hl1.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.BASELINE);
		hl1.add(titleLbl,spacer,closeBtn);
		hl1.expand(spacer);

		VerticalLayout contentVL = new VerticalLayout();
		contentVL.setPadding(false);
		contentVL.setSpacing(true);
		contentVL.setSizeFull();

		ta = new TextArea();
		ta.setValue(message);
		ta.setSizeFull();
		ta.setMaxLength(4000);

		contentVL.add(ta);

		VerticalLayout diaVL = new VerticalLayout();
		diaVL.setPadding(false);
		diaVL.setSpacing(true);
		diaVL.setSizeFull();
		diaVL.add(hl1,contentVL);

		this.add(diaVL);
	}
}
