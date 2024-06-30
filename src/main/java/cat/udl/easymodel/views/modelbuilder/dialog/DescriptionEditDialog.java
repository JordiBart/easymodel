package cat.udl.easymodel.views.modelbuilder.dialog;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import com.vaadin.flow.component.ScrollOptions;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.server.VaadinSession;

public class DescriptionEditDialog extends Dialog {
	private HorizontalLayout mainHL;
	private SessionData sessionData;
	private boolean isRenderHTML = false;
	private String desc;

	public DescriptionEditDialog(Model selModel) {
		super();

		sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
		this.desc = selModel.getDescription();
		this.setModal(true);
		this.setResizable(true);
		this.setWidth("1100px");
		this.setHeight("750px");

		mainHL = new HorizontalLayout();
		mainHL.setSizeFull();
		mainHL.setPadding(false);

		VerticalLayout dialogVL = new VerticalLayout();
		dialogVL.setSpacing(true);
		dialogVL.setPadding(false);
		dialogVL.setSizeFull();
		dialogVL.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
		dialogVL.add(ToolboxVaadin.getDialogHeader(this, "Description Editor", new InfoDialogButton("Model Description",
				"1. Edit model description\n" + "2. Press the \"H\" button to render the description as HTML", "500px",
				"300px")),mainHL, getOkCancelButtonsHL());
		dialogVL.expand(mainHL);
		this.add(dialogVL);

		desc = desc.replaceAll("\u00A0", ""); // removes null char
		updateDialogContent();
	}

	private void updateDialogContent() {
		mainHL.removeAll();

		if (isRenderHTML) {
			IFrame iFrame = getRenderedHtml();
			mainHL.add(iFrame, getDescButtonsVL());
			mainHL.expand(iFrame);
		} else {
			VerticalLayout editorVL = getEditorVL();
			mainHL.add(editorVL, getDescButtonsVL());
			mainHL.expand(editorVL);
		}
	}

	private VerticalLayout getDescButtonsVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setWidth("38px");
		vl.setHeight("100%");
		vl.setPadding(false);
		vl.setSpacing(false);
		VerticalLayout spacer = new VerticalLayout();
		vl.add(getRenderHtmlButton(), spacer);
		vl.expand(spacer);
		return vl;
	}

	private Button getRenderHtmlButton() {
		Button btn = new Button();
		btn.setWidth("36px");
		btn.setClassName("renderHtmlBtn");
		btn.getElement().setProperty("title", "Toggle editor/HTML renderer");
		btn.addClickListener(e-> {
				isRenderHTML = !isRenderHTML;
				updateDialogContent();
		});
		return btn;
	}

	private IFrame getRenderedHtml() {
		String sanitizedHtml = ToolboxVaadin.sanitizeHTML(desc);
		IFrame iFrame =new IFrame();
		iFrame.setSizeFull();
		iFrame.getStyle().set("border", "1px solid #e9e9e9");
		iFrame.setSrcdoc("<html><body style=\"font-family: Helvetica, Arial, sans-serif;\">" + sanitizedHtml + "</body></html>");
		return iFrame;
	}

	private VerticalLayout getEditorVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setPadding(false);
		vl.setSpacing(false);
		vl.setSizeFull();
		TextArea ta = new TextArea();
		ta.setWidthFull();
		ta.setHeight("600px");
		ta.setMaxLength(256*1024);
		ta.setValue(desc);
		ta.addValueChangeListener(e->{
			desc = e.getValue();
		});
		vl.add(ta);
		ta.focus();
		return vl;
	}

	private HorizontalLayout getOkCancelButtonsHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		HorizontalLayout spacer = new HorizontalLayout();
		spacer.setWidth("100%");
		hl.add(spacer, getOkButton(), getCancelButton());
		hl.expand(spacer);
		return hl;
	}

	private Button getOkButton() {
		Button button = new Button("Ok");
		button.setWidth("70px");
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.addClickListener(ev->{
			sessionData.getSelectedModel().setDescription(desc);
			close();
		});
		return button;
	}

	private Button getCancelButton() {
		Button button = new Button("Cancel");
		button.setWidth("150px");
		button.addClickListener(e->{
				close();
		});
		return button;
	}

}
