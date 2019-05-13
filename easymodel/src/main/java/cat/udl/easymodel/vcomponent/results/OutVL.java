package cat.udl.easymodel.vcomponent.results;

import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.ByteArrayStreamSource;
import cat.udl.easymodel.utils.DownFileStreamSource;
import cat.udl.easymodel.utils.VaadinUtils;

public class OutVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	private OutVL globalThis;
	private GridLayout grid;
	private HorizontalLayout horizontal;

	private UI ui = null;
//	private SharedData sharedData = SharedData.getInstance();

	public OutVL(UI ui) {
		super();

		this.ui = ui;

		globalThis = this;
		this.setSpacing(true);
		this.setMargin(true);
		globalThis.setId("outVL");
		globalThis.setSizeUndefined();
		// globalThis.setStyleName("panelButtons"); // same as buttons
		globalThis.setDefaultComponentAlignment(Alignment.TOP_LEFT);
	}

	public void out(String msg) {
		if (msg != null) {
			for (String line : msg.split("\\n")) {
				// line = line.replace(" ", "&nbsp;");
				globalThis.addComponent(new Label(line, ContentMode.TEXT));
			}
		}
		ui.push();
	}

	public void out(String msg, String style) {
		if (msg != null) {
			for (String line : msg.split("\\n")) {
				globalThis.addComponent(VaadinUtils.getStyledLabel(line, style));
			}
		}
		ui.push();
	}

//	public void dbg(String msg) {
//		if (sharedData.isDebug()) {
//			// out("**" + msg);
//			System.out.println(msg);
//		}
//	}

	public void out(Component l) {
		if (l != null) {
			globalThis.addComponent(l);
		}
	}

	// private MouseEvents.ClickListener getOutImageClickListener() {
	// return new MouseEvents.ClickListener() {
	// private static final long serialVersionUID = 1L;
	// @Override
	// public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
	// BrowserWindowOpener opener = new
	// BrowserWindowOpener(((Image)event.getSource()).getSource());
	// opener.extend(((Image)event.getSource()));
	// }
	// };
	// }
////////////////////////////////
	public void addToLastCreatedGrid(Component c) {
		grid.addComponent(c);
	}

	public void outNewGrid() {
		grid = new GridLayout(2, 1);
		globalThis.addComponent(grid);
	}

////////////////////////////////
	public void addToLastCreatedHL(Component c) {
		horizontal.addComponent(c);
	}

	public void outNewHorizontalLayout() {
		horizontal = new HorizontalLayout();
		horizontal.setSpacing(true);
		globalThis.addComponent(horizontal);
	}

	////////////////////////////////
	public void out(String filename, boolean addToGrid, byte[] imageArray, String imWidth) {
		Image img = getImage(filename, imageArray, imWidth);
		if (img != null) {
			if (addToGrid)
				addToLastCreatedGrid(img);
			else
				globalThis.addComponent(img);
			// dbg("Image " + filename + " displayed");
			// resource = null; imageSource = null;
		}
		ui.push();
	}

	private Image getImage(String filename, byte[] imageArray, String imWidth) {
		Image img = null;
		if (imageArray != null) {
			StreamResource.StreamSource imageSource = new ByteArrayStreamSource(imageArray);
			StreamResource resource = new StreamResource(imageSource, filename);
			img = new Image(null, resource);
			if (imWidth != null)
				img.setWidth(imWidth);
			// new browser tab opener
			BrowserWindowOpener opener = new BrowserWindowOpener(img.getSource());
			opener.extend(img);
		}
		return img;
	}

	public void outFile(String caption, String tooltipButton, String filename, String fileString, String style,
			boolean addToHorizontal) {
		if (fileString != null) {
			FileDownloader fileDown = new FileDownloader(
					new StreamResource(new DownFileStreamSource(fileString), filename));
			Button downBtn = new Button();
			if (!caption.equals("Mathematica") && !caption.equals("SBML"))
				downBtn.setCaption(caption);
			downBtn.setDescription(tooltipButton);
			if (style != null)
				downBtn.setStyleName(style);
			fileDown.extend(downBtn);
			if (addToHorizontal)
				addToLastCreatedHL(downBtn);
			else
				globalThis.addComponent(downBtn);
		}
		ui.push();
	}

	public void reset() {
		this.removeAllComponents();
	}
}
