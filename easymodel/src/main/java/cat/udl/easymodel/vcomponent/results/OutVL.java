package cat.udl.easymodel.vcomponent.results;

import java.time.Duration;
import java.util.ArrayList;

import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.utils.ByteArrayStreamSource;
import cat.udl.easymodel.utils.DownFileStreamSource;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.common.SpacedLabel;

public class OutVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	private OutVL globalThis;
	private GridLayout gridLayout;
	private HorizontalLayout horizontal;
	private GridLayout stDataGrid;
	private ArrayList<ProgressBar> stProgressBars = new ArrayList<>();
	private ProgressBar stGlobalProgressBar;
	private Label stTimeLabel;
	private long stStartMillis;

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
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				if (msg != null) {
					for (String line : msg.split("\\n")) {
						// line = line.replace(" ", "&nbsp;");
						globalThis.addComponent(new Label(line, ContentMode.TEXT));
					}
				}
			}
		});
	}

	public void out(String msg, String style) {
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				if (msg != null) {
					for (String line : msg.split("\\n")) {
						globalThis.addComponent(ToolboxVaadin.getStyledLabel(line, style));
					}
				}
			}
		});
	}

//	public void dbg(String msg) {
//		if (sharedData.isDebug()) {
//			// out("**" + msg);
//			System.out.println(msg);
//		}
//	}

	public void out(Component l) {
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				if (l != null) {
					globalThis.addComponent(l);
				}
			}
		});
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
	public void addToGridLayout(Component c) {
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				if (gridLayout != null) {
					gridLayout.addComponent(c);
				}
			}
		});
	}

	public void outNewGridLayout(int cols, int numItems) {
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				gridLayout = new GridLayout(cols, (int) Math.ceil((float) numItems / (float) cols));
				gridLayout.setSpacing(true);
				gridLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
				globalThis.addComponent(gridLayout);
			}
		});
	}

	public void setCaptionToGridLayout(String caption) {
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				if (gridLayout != null) {
					gridLayout.setCaption(caption);
				}
			}
		});
	}

////////////////////////////////
	public void addToLastCreatedHL(Component c) {
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				horizontal.addComponent(c);
			}
		});
	}

	public void outNewHorizontalLayout() {
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				horizontal = new HorizontalLayout();
				horizontal.setSpacing(true);
				globalThis.addComponent(horizontal);
			}
		});
	}

/////////////////////////////////
	public void outNewStochasticGrid(Integer numIterations, boolean isTauLeaping) {
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				String progressBarWidth="400px";
				stStartMillis = System.currentTimeMillis();
				stProgressBars.clear();
				if (!isTauLeaping) {
					stDataGrid = new GridLayout(4, numIterations + 2);
					stDataGrid.addComponent(new SpacedLabel("Iteration"));
					stDataGrid.addComponent(new SpacedLabel("Progress"));
					stDataGrid.addComponent(new SpacedLabel("Execution time (s)"));
					stDataGrid.addComponent(new SpacedLabel("Int. steps"));
				} else {
					stDataGrid = new GridLayout(6, numIterations + 2);
					stDataGrid.addComponent(new SpacedLabel("Iteration"));
					stDataGrid.addComponent(new SpacedLabel("Progress"));
					stDataGrid.addComponent(new SpacedLabel("Execution time (s)"));
					stDataGrid.addComponent(new SpacedLabel("Int. steps"));
					stDataGrid.addComponent(new SpacedLabel("#Leaps"));
					stDataGrid.addComponent(new SpacedLabel("Leap time"));
				}
				stDataGrid.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
//				gridForStochastic.addStyleName("table");
				stDataGrid.setSpacing(false);
				stDataGrid.setMargin(false);
				for (int i = 1; i <= numIterations; i++) {
					stDataGrid.addComponent(new SpacedLabel(String.valueOf(i)), 0, i);
					ProgressBar pb = new ProgressBar();
					pb.setWidth(progressBarWidth);
					stProgressBars.add(pb);
					stDataGrid.addComponent(pb, 1, i);
				}
				stGlobalProgressBar = new ProgressBar();
				stGlobalProgressBar.setWidth(progressBarWidth);
				stDataGrid.addComponent(new SpacedLabel("*"), 0, stDataGrid.getRows() - 1);
				stDataGrid.addComponent(stGlobalProgressBar, 1, stDataGrid.getRows() - 1);
				
				stTimeLabel = new Label("Time elapsed=00:00:00 ; Estimated time remaining=?");
				globalThis.addComponent(stDataGrid);
				globalThis.addComponent(stTimeLabel);
			}
		});
	}

	public void updateStochasticProgressBar(Integer numIteration, Float newValue) {
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				ProgressBar pb = stProgressBars.get(numIteration - 1);
				if (pb != null)
					pb.setValue(newValue);
//				Float totalProgress = (numIteration - 1 + newValue) / stNumIterations;
				Float totalProgress = 0f;
				for (ProgressBar pb2 : stProgressBars)
					totalProgress += pb2.getValue();
				totalProgress /= stProgressBars.size();
				stGlobalProgressBar.setValue(totalProgress);
				long millisElapsed = System.currentTimeMillis() - stStartMillis;
//				Duration duration = DurationFormatUtils.formatDuration(Float.valueOf(((1-totalProgress)/totalProgress)).longValue()*millisElapsed, "**H:mm:ss**", true);
				long secondsElapsed = Duration.ofMillis(millisElapsed).getSeconds();				
				long etrSeconds = Duration.ofMillis(Math.round(((1 - totalProgress) / totalProgress) * millisElapsed)).getSeconds();
				String etrHMS = String.format("%02d:%02d:%02d", etrSeconds / 3600, (etrSeconds % 3600) / 60,
						etrSeconds % 60);
				String elapsedHMS = String.format("%02d:%02d:%02d", secondsElapsed / 3600, (secondsElapsed % 3600) / 60,
						secondsElapsed % 60);
				stTimeLabel.setValue("Time elapsed="+elapsedHMS+" ; Estimated time remaining=" + etrHMS);
			}
		});
	}

	public void updateStochasticStatistics(String[] vals) {
		int row;
		if (vals[0].equals("*")) {
			row = stDataGrid.getRows() - 1;
			stGlobalProgressBar.setValue(1f);
			long millisElapsed = System.currentTimeMillis() - stStartMillis;
			long secondsElapsed = Duration.ofMillis(millisElapsed).getSeconds();				
			String elapsedHMS = String.format("%02d:%02d:%02d", secondsElapsed / 3600, (secondsElapsed % 3600) / 60,
					secondsElapsed % 60);
			stTimeLabel.setValue("Time elapsed="+elapsedHMS+" ; Estimated time remaining=00:00:00");
		} else {
			row = Integer.valueOf(vals[0]);
			updateStochasticProgressBar(row, 1f);
		}
		for (int i = 1; i < vals.length; i++) {
			stDataGrid.addComponent(new SpacedLabel(vals[i]), i + 1, row);
		}
	}

	////////////////////////////////
	public void out(String filename, boolean addToGrid, byte[] imageArray, String imWidth) {
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				Image img = getImage(filename, imageArray, imWidth);
				if (img != null) {
					if (addToGrid)
						addToGridLayout(img);
					else
						globalThis.addComponent(img);
					// dbg("Image " + filename + " displayed");
					// resource = null; imageSource = null;
				}
			}
		});
	}

	private Image getImage(String filename, byte[] imageArray, String imWidth) {
		Image img = null;
		if (imageArray != null) {
			StreamResource.StreamSource imageSource = new ByteArrayStreamSource(imageArray);
			StreamResource resource = new StreamResource(imageSource, filename);
			img = new Image(null, resource);
			img.setStyleName("results");
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
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				if (fileString != null) {
					FileDownloader fileDown = new FileDownloader(
							new StreamResource(new DownFileStreamSource(fileString), filename));
					Button downBtn = new Button();
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
			}
		});
	}

	public void reset() {
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				removeAllComponents();
			}
		});
	}

	public void finish() {
		this.ui.access(new Runnable() {
			@Override
			public void run() {
				globalThis.addComponent(ToolboxVaadin.getStandardWebMathematicaLink());
			}
		});
	}
}
