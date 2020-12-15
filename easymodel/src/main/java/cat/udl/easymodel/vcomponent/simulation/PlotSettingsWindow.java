package cat.udl.easymodel.vcomponent.simulation;

import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import cat.udl.easymodel.logic.simconfig.SimConfigArray;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.vcomponent.common.InfoPopupButton;

public class PlotSettingsWindow extends Window {
	private static final long serialVersionUID = 1L;
	private SimConfigArray plotConfig;
	private SessionData sessionData;
	private VerticalLayout windowVL;

	public PlotSettingsWindow(SimConfigArray plotConfig) {
		super();

		this.setCaption("Plot Settings");
		this.setClosable(true);
		this.setResizable(false);
		this.setWindowMode(WindowMode.NORMAL);
		this.setWidth("510px");
		this.setHeight("250px");
		this.setModal(false);
		this.setPosition(100, 200);
		
		this.sessionData = (SessionData) UI.getCurrent().getData();
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
		
		rightVL.addComponents(new InfoPopupButton("How to set Plot Settings", "Select the graphical representation options for your simulation.", null, null));

		HorizontalLayout hl = new HorizontalLayout();
		hl.setSizeFull();
		hl.setSpacing(false);
		hl.setMargin(false);
		hl.addComponents(leftVL, rightVL);
		hl.setExpandRatio(leftVL, 1f);

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		windowVL.addComponent(hl);
		this.setContent(windowVL);
	}
}
