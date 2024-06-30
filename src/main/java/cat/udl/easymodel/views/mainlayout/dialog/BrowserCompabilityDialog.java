package cat.udl.easymodel.views.mainlayout.dialog;

import java.util.ArrayList;
import java.util.Collection;

import cat.udl.easymodel.utils.ToolboxVaadin;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class BrowserCompabilityDialog extends Dialog {
	private VerticalLayout mainVL;

	public BrowserCompabilityDialog() {
		super();

		this.setModal(false);
		this.setResizable(true);
		this.setDraggable(true);
		this.setWidth("900px");
		this.setHeight("400px");

		mainVL = new VerticalLayout();
		mainVL.setSpacing(true);
		mainVL.setPadding(false);
		mainVL.setSizeFull();
		mainVL.setClassName("scroll");

        VerticalLayout winVL = new VerticalLayout();
        winVL.setSpacing(true);
        winVL.setPadding(false);
        winVL.setSizeFull();
		winVL.add(ToolboxVaadin.getDialogHeader(this,"Browsers Compatibility Table",null),mainVL);
		winVL.expand(mainVL);

		this.add(winVL);

		Grid<BrowserCompabilityEntry> browserGrid = new Grid<>();
		browserGrid.setWidth("100%");
//		browserGrid.setHeight("200px");
		browserGrid.addColumn(BrowserCompabilityEntry::getOsName).setHeader("OS");
		browserGrid.addColumn(BrowserCompabilityEntry::getOsVersion).setHeader("OS Version");
		browserGrid.addColumn(BrowserCompabilityEntry::getChrome).setHeader("Chrome");
		browserGrid.addColumn(BrowserCompabilityEntry::getFirefox).setHeader("Firefox");
		browserGrid.addColumn(BrowserCompabilityEntry::getEdge).setHeader("Edge");
		browserGrid.addColumn(BrowserCompabilityEntry::getSafari).setHeader("Safari");
		browserGrid.setItems(getBrowserGridItems());

		mainVL.add(new Paragraph("This table shows a range of browsers that have been tested to be compatible with this web application."));
		mainVL.add(browserGrid);
	}

	private Collection<BrowserCompabilityEntry> getBrowserGridItems() {
		ArrayList<BrowserCompabilityEntry> ret = new ArrayList<>();
		ret.add(new BrowserCompabilityEntry("Linux", "Ubuntu 20.04.1 LTS", "87.0.4280.141", "84.0.2", "n/a", "n/a"));
		ret.add(new BrowserCompabilityEntry("macOS", "10.15 Catalina", "87.0.4280.141", "84.0.2", "n/a", "14.0.2"));
		ret.add(new BrowserCompabilityEntry("Windows", "11", "123.0.6312.58/59", "124.0", "122.0.2365.106", "n/a"));
		return ret;
	}
}
