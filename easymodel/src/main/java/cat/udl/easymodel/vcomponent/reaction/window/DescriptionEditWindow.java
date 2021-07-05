package cat.udl.easymodel.vcomponent.reaction.window;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.common.InfoPopupButton;

public class DescriptionEditWindow extends Window {
	private static final long serialVersionUID = 1L;

	private VerticalLayout windowVL;
	private SessionData sessionData;
	private boolean isRenderHTML = false;
	private String desc;

	public DescriptionEditWindow() {
		super();

		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.desc = sessionData.getSelectedModel().getDescription();
		this.setCaption("Edit model description");
		this.setClosable(true);
		this.setModal(true);
		this.setWindowMode(WindowMode.NORMAL);
		this.setResizable(true);
		this.center();
		this.setWidth("90%");
		this.setHeight("90%");

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		desc = desc.replaceAll("\u00A0", ""); // removes null char
//		desc = desc.replaceAll("\u00C2", "");
//		byte[] xxx;
//		xxx = desc.getBytes();
//		for (byte x : xxx) {
//			System.out.println(Integer.toHexString(x & 0xFF));
//		}

//		isRenderHTML = desc.contains("</div>");
		updateWindowContent();
	}

	private void updateWindowContent() {
		windowVL.removeAllComponents();

		HorizontalLayout mainHL = new HorizontalLayout();
		mainHL.setSizeFull();
		mainHL.setMargin(false);

		if (isRenderHTML) {
			Panel panel = getRenderedPanel();
			mainHL.addComponents(panel, getDescButtonsVL());
			mainHL.setExpandRatio(panel, 1f);
		} else {
			VerticalLayout editorVL = getEditorVL();
			mainHL.addComponents(editorVL, getDescButtonsVL());
			mainHL.setExpandRatio(editorVL, 1f);
		}

		windowVL.addComponents(mainHL, getOkCancelButtonsHL());
		windowVL.setExpandRatio(mainHL, 1.0f);
	}

	private VerticalLayout getDescButtonsVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setWidth("38px");
		vl.setHeight("100%");
		vl.setMargin(false);
		vl.setSpacing(false);
		VerticalLayout spacer = new VerticalLayout();
		vl.addComponents(getInfoButton(), getRenderHtmlButton(), spacer);
		vl.setExpandRatio(spacer, 1f);
		return vl;
	}

	private InfoPopupButton getInfoButton() {
		return new InfoPopupButton("Model Description",
				"1. Edit model description\n" + "2. Press the \"H\" button to render the description as HTML", 500,
				null);
	}

	private Button getRenderHtmlButton() {
		Button btn = new Button();
		btn.setDescription("Switch between editor and HTML renderer");
		btn.setWidth("36px");
		btn.setStyleName("renderHtmlBtn");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				isRenderHTML = !isRenderHTML;
				updateWindowContent();
			}
		});
		return btn;
	}

	private Panel getRenderedPanel() {
		CustomLayout cl = new CustomLayout();
		try {
			String html = ToolboxVaadin.sanitizeHTML(desc);
			cl = new CustomLayout(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cl.setStyleName("descriptionHTML");
		}
		Panel pan = new Panel();
		pan.setSizeFull();
		pan.setContent(cl);
		return pan;
	}

	private VerticalLayout getEditorVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setMargin(false);
		vl.setSpacing(false);
		vl.setSizeFull();
		TextArea ta = new TextArea();
		ta.setSizeFull();
		ta.setValue(desc);
		ta.addValueChangeListener(new ValueChangeListener<String>() {

			@Override
			public void valueChange(ValueChangeEvent<String> event) {
				desc = event.getValue();
			}
		});
		vl.addComponent(ta);
		ta.focus();
		return vl;
	}

	private HorizontalLayout getOkCancelButtonsHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		HorizontalLayout spacer = new HorizontalLayout();
		spacer.setWidth("100%");
		hl.addComponents(spacer, getOkButton(), getCancelButton());
		hl.setExpandRatio(spacer, 1.0f);
		return hl;
	}

	private Button getOkButton() {
		Button button = new Button("Ok");
		button.setId("okButton");
		button.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				try {
					sessionData.getSelectedModel().setDescription(desc);
					close();
				} catch (Exception e) {
					Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
				}
			}
		});
		return button;
	}

	private Button getCancelButton() {
		Button button = new Button("Cancel");
		button.setId("cancelButton");
		button.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				close();
			}
		});
		return button;
	}

}
