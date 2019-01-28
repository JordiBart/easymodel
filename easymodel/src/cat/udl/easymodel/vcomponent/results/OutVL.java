package cat.udl.easymodel.vcomponent.results;

import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.label.ContentMode;
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

	private SessionData sessionData = null;
	private SharedData sharedData = SharedData.getInstance();

	public OutVL() {
		super();

		this.sessionData = (SessionData) UI.getCurrent().getData();

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
	}
	
	public void out(String msg, String style) {
		if (msg != null) {
			for (String line : msg.split("\\n")) {
				globalThis.addComponent(VaadinUtils.getStyledLabel(line, style));
			}
		}
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
	public void resetGrid() {
		grid = new GridLayout(2, 1);
	}

	public void addToGrid(Component c) {
		grid.addComponent(c);
	}
	
	public void outGrid() {
		globalThis.addComponent(grid);
	}
////////////////////////////////
	public void resetHorizontalLayout() {
		horizontal = new HorizontalLayout();
		horizontal.setSpacing(true);
	}

	public void addToHorizontalLayout(Component c) {
		horizontal.addComponent(c);
	}
	
	public void outHorizontalLayout() {
		globalThis.addComponent(horizontal);
	}
	////////////////////////////////
	public void out(String filename, boolean addToGrid, byte[] imageArray, String imHeight, byte[] imageArrayBig) {
		Image img = getImage(filename, imageArray, imHeight, imageArrayBig);
		if (img != null) {
			if (addToGrid)
				addToGrid(img);
			else
				globalThis.addComponent(img);
			// dbg("Image " + filename + " displayed");
			// resource = null; imageSource = null;
		}
	}

	private Image getImage(String filename, byte[] imageArray, String imHeight, byte[] imageArrayBig) {
		Image img = null;
		if (imageArray != null) {
			StreamResource.StreamSource imageSource = new ByteArrayStreamSource(imageArray);
			StreamResource resource = new StreamResource(imageSource, filename);
			img = new Image(null, resource);
			// setWidth("100%") would be optimal, but there's problem with the scrolling
			if (imHeight != null)
				img.setHeight(imHeight);
			// new browser tab opener
			if (imageArrayBig == null) {
				// open same image when click
				BrowserWindowOpener opener = new BrowserWindowOpener(img.getSource());
				opener.extend(img);
			} else {
				// open big image when click
				imageSource = new ByteArrayStreamSource(imageArrayBig);
				resource = new StreamResource(imageSource, filename);
				Image imgBig = new Image(null, resource);
				BrowserWindowOpener opener = new BrowserWindowOpener(imgBig.getSource());
				opener.extend(img);
			}
		}
		return img;
	}

	public void outFile(String caption, String tooltipButton, String filename, String fileString, String style, boolean addToHorizontal) {
		if (fileString != null) {
			FileDownloader fileDown = new FileDownloader(
					new StreamResource(new DownFileStreamSource(fileString), filename));
			Button downBtn = new Button();
			if (caption.equals("Mathematica"))
				downBtn.setWidth("78px");
			else if (caption.equals("SBML"))
				downBtn.setWidth("78px");
			else
				downBtn.setCaption(caption);
			downBtn.setDescription(tooltipButton);
			if (style != null)
				downBtn.setStyleName(style);
			fileDown.extend(downBtn);
			if (addToHorizontal)
				addToHorizontalLayout(downBtn);
			else
				globalThis.addComponent(downBtn);
		}
	}

	public void reset() {
		this.removeAllComponents();
	}
}
