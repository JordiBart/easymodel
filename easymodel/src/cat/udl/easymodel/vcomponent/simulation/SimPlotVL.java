package cat.udl.easymodel.vcomponent.simulation;

import java.util.Map;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Slider;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.utils.p;

import com.vaadin.ui.CheckBox;

public class SimPlotVL extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	private Map<String, Object> plotConfig;
	private PopupView infoPlotSetPopup = new PopupView(null, getInfoPlotSettingsLayout());

	public SimPlotVL(Map<String, Object> plotConfig) {
		super();

		this.plotConfig = plotConfig;
		VerticalLayout leftVL = new VerticalLayout();
		leftVL.setWidth("100%");
		leftVL.setSpacing(false);
		leftVL.setMargin(false);
		GridLayout gl = new GridLayout(3, 2);
		leftVL.addComponent(gl);
		gl.setWidth("100%");
		gl.setSpacing(true);
		VerticalLayout fontCBVL = new VerticalLayout();
		fontCBVL.setSpacing(true);
		fontCBVL.setWidth("80px");
		fontCBVL.addComponents(getBoldCB(), getItalicCB());
		gl.addComponents(getIntSlider("Font Size", "FontSize", 10d, 24d), fontCBVL, getLineThicknessSlider(), getIntSlider("Image Resolution (DPI)", "ImageResolution", 72d, 2000d), getIntSlider("Image Width (pixels)", "ImageSize-Small", 300d, 1000d), getIntSlider("Large Image Width (pixels)", "ImageSize-Big", 1000d, 3000d));

		VerticalLayout rightVL = new VerticalLayout();
		rightVL.setDefaultComponentAlignment(Alignment.TOP_RIGHT);
		rightVL.setWidth("50px");
		rightVL.setHeight("100%");
		rightVL.setSpacing(false);
		rightVL.setMargin(false);
		Button iBtn = getInfoPlotSetButton();
		rightVL.addComponents(iBtn);

		HorizontalLayout hl = new HorizontalLayout();
		hl.setSizeFull();
		hl.setSpacing(false);
		hl.setMargin(false);
		hl.addComponents(leftVL, infoPlotSetPopup, rightVL);
		hl.setExpandRatio(leftVL, 1f);

		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(false);
		this.addComponent(hl);
	}

	private Component getItalicCB() {
		CheckBox cb = new CheckBox("Font Italic");
		cb.setValue("Italic".equals(plotConfig.get("FontSlant")) ? true : false);
		cb.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Boolean newVal = (Boolean) event.getProperty().getValue();
				if (newVal)
					plotConfig.put("FontSlant", "Italic");
				else
					plotConfig.put("FontSlant", "Plain");
			}
		});
		return cb;
	}

	private CheckBox getBoldCB() {
		CheckBox cb = new CheckBox("Font Bold");
		cb.setValue("Bold".equals(plotConfig.get("FontWeight")) ? true : false);
		cb.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Boolean newVal = (Boolean) event.getProperty().getValue();
				if (newVal)
					plotConfig.put("FontWeight", "Bold");
				else
					plotConfig.put("FontWeight", "Plain");
			}
		});
		return cb;
	}

	private Slider getLineThicknessSlider() {
		Slider s = new Slider(1d, 10d, 0);
		s.setCaption("Line Thickness");
		s.setValue(Double.valueOf(((String) plotConfig.get("LineThickness"))) * 1000d);
		s.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Double newVal = (Double) event.getProperty().getValue();
				plotConfig.put("LineThickness", String.valueOf(newVal / 1000));
			}
		});
		return s;
	}

	private Slider getIntSlider(String caption, String simKey, double min, double max) {
		Slider s = new Slider(min, max, 0);
		s.setCaption(caption);
		s.setValue(Double.valueOf(((String) plotConfig.get(simKey))));
		s.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Double newVal = (Double) event.getProperty().getValue();
				plotConfig.put(simKey, String.valueOf(newVal.intValue()));
			}
		});
		return s;
	}

	private Button getInfoPlotSetButton() {
		Button btn = new Button();
		btn.setDescription("How to set Plot settings");
		btn.setWidth("36px");
		btn.setStyleName("infoBtn");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				infoPlotSetPopup.setPopupVisible(true);
			}
		});
		return btn;
	}

	private Component getInfoPlotSettingsLayout() {
		VerticalLayout vlt = new VerticalLayout();
		vlt.addComponent(new Label("Define or select the general graphical representation options for your results"));
		return vlt;
	}
}
