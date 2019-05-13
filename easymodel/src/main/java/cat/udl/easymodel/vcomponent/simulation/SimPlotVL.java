package cat.udl.easymodel.vcomponent.simulation;

import java.util.Map;

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
import cat.udl.easymodel.logic.simconfig.SimConfigArray;
import cat.udl.easymodel.utils.p;

import com.vaadin.ui.CheckBox;

public class SimPlotVL extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	private SimConfigArray plotConfig;
	private PopupView infoPlotSetPopup = new PopupView(null, getInfoPlotSettingsLayout());

	public SimPlotVL(SimConfigArray plotConfig) {
		super();

		this.plotConfig = plotConfig;
		VerticalLayout leftVL = new VerticalLayout();
		leftVL.setWidth("100%");
		leftVL.setSpacing(false);
		leftVL.setMargin(false);
		GridLayout gl = new GridLayout(3, 2);
		leftVL.addComponent(gl);
		gl.setWidth("100%");
		gl.setSpacing(false);
		gl.setMargin(false);
		VerticalLayout fontCBVL = new VerticalLayout();
		fontCBVL.setSpacing(true);
		fontCBVL.setMargin(false);
		fontCBVL.setWidth("80px");
		fontCBVL.addComponents(SimConfigToComponent.convert(plotConfig.get("FontWeight"), null), SimConfigToComponent.convert(plotConfig.get("FontSlant"), null));
		gl.addComponents(SimConfigToComponent.convert(plotConfig.get("FontSize"), null), fontCBVL, SimConfigToComponent.convert(plotConfig.get("LineThickness"), null),SimConfigToComponent.convert(plotConfig.get("ImageSize"), null));

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
