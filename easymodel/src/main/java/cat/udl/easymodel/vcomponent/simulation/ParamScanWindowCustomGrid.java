package cat.udl.easymodel.vcomponent.simulation;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import com.vaadin.event.ShortcutAction;
//import com.vaadin.event.ShortcutListener;
//import com.vaadin.event.FieldEvents.FocusEvent;
//import com.vaadin.event.FieldEvents.FocusListener;
//import com.vaadin.event.selection.MultiSelectionEvent;
//import com.vaadin.server.FileResource;
//import com.vaadin.shared.ui.window.WindowMode;
//import com.vaadin.ui.Alignment;
//import com.vaadin.ui.Button;
//import com.vaadin.ui.Button.ClickEvent;
//import com.vaadin.ui.CheckBox;
//import com.vaadin.ui.Component;
//import com.vaadin.ui.Grid;
//import com.vaadin.ui.Grid.Column;
//import com.vaadin.ui.Grid.ItemClick;
//import com.vaadin.ui.Grid.SelectionMode;
//import com.vaadin.ui.HorizontalLayout;
//import com.vaadin.ui.Image;
//import com.vaadin.ui.Label;
//import com.vaadin.ui.Notification;
//import com.vaadin.ui.Notification.Type;
//import com.vaadin.ui.components.grid.ItemClickListener;
//import com.vaadin.ui.PopupView;
//import com.vaadin.ui.StyleGenerator;
//import com.vaadin.ui.TextArea;
//import com.vaadin.ui.TextField;
//import com.vaadin.ui.UI;
//import com.vaadin.ui.VerticalLayout;
//import com.vaadin.ui.Window;
//import com.vaadin.ui.renderers.ComponentRenderer;
//import com.vaadin.ui.renderers.HtmlRenderer;
//
//import cat.udl.easymodel.logic.formula.FormulaValue;
//import cat.udl.easymodel.logic.model.Model;
//import cat.udl.easymodel.logic.model.Reaction;
//import cat.udl.easymodel.logic.model.Species;
//import cat.udl.easymodel.logic.simconfig.ParamScanEntry;
//import cat.udl.easymodel.logic.simconfig.SimParamScanConfig;
//import cat.udl.easymodel.logic.types.FormulaValueType;
//import cat.udl.easymodel.logic.types.ParamScanType;
//import cat.udl.easymodel.logic.types.SpeciesVarTypeType;
//import cat.udl.easymodel.main.SessionData;
//import cat.udl.easymodel.main.SharedData;
//import cat.udl.easymodel.utils.Utils;
//import cat.udl.easymodel.utils.p;
//import cat.udl.easymodel.vcomponent.common.InfoPopupButton;
//
//public class ParamScanWindow extends Window {
//	private static final long serialVersionUID = -3283891545609430237L;
//	private ParamScanType type = null;
//	private SimParamScanConfig simParamScanConfig = null;
//	private ArrayList<ParamScanEntry> paramList = null;
//
//	private HashMap<ParamScanEntry, TextField> beginValTFMap = new HashMap<>();
//	private HashMap<ParamScanEntry, TextField> endValTFMap = new HashMap<>();
//	private HashMap<ParamScanEntry, TextField> numIntervalsTFMap = new HashMap<>();
//
//	private Model selModel = null;
//	private SessionData sessionData = null;
//	private SharedData sharedData = SharedData.getInstance();
//	private VerticalLayout windowVL = null;
//	private Grid<ParamScanEntry> grid = null;
//	private ArrayList<ParamScanEntry> gridItemList = new ArrayList<>();
//	private PopupView reactionPopupView;
//	private TextArea textAreaReactionPopup;
//	private boolean isSaveAndClose = false;
//
//	private Column<ParamScanEntry, ?> colSelect;
//
//	public ParamScanWindow() {
//		super();
//
//		this.sessionData = (SessionData) UI.getCurrent().getData();
//		this.selModel = this.sessionData.getSelectedModel();
//		this.reactionPopupView = new PopupView(null, getLargeComponent());
//
//		this.setData(false); // for window close callback
//		this.setClosable(true);
//		this.setWidth("800px");
//		this.setHeight("600px");
//		this.setWindowMode(WindowMode.MAXIMIZED);
//		this.setModal(true);
//		this.center();
//
//		windowVL = new VerticalLayout();
//		windowVL.setSpacing(true);
//		windowVL.setMargin(true);
//		windowVL.setSizeFull();
//		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
//		this.setContent(windowVL);
//
//		this.addShortcutListener(new ShortcutListener("Shortcut enter", ShortcutAction.KeyCode.ENTER, null) {
//			@Override
//			public void handleAction(Object sender, Object target) {
//				isSaveAndClose = true;
//				focus();
//			}
//		});
//		this.addFocusListener(new FocusListener() {
//			@Override
//			public void focus(FocusEvent event) {
//				if (isSaveAndClose) {
//					isSaveAndClose = false;
//					saveAndClose();
//				}
//			}
//		});
//	}
//
//	public void set(SimParamScanConfig simParamScanConfig, ParamScanType type) {
//		this.simParamScanConfig = simParamScanConfig;
//		this.type = type;
//		if (type == ParamScanType.PARAMETER)
//			paramList = simParamScanConfig.getParameters();
//		else if (type == ParamScanType.IND_VAR)
//			paramList = simParamScanConfig.getIndependentVars();
//		updateWindowContent();
//	}
//
//	private void updateSelectStyles() {
//		for (Column<ParamScanEntry, ?> col : grid.getColumns()) {
//			if (col == colSelect)
//				col.setStyleGenerator(getColSelectStyleGenerator());
//		}
//	}
//
//	private void createGrid() {
//		grid = new Grid<ParamScanEntry>();
//		grid.setSelectionMode(SelectionMode.NONE);
//		grid.setSizeFull();
//		fillGridItemList();
//		grid.setItems(gridItemList);
//
//		beginValTFMap.clear();
//		endValTFMap.clear();
//		numIntervalsTFMap.clear();
//		// SELECT COLUMN
//		colSelect = grid.addColumn(ParamScanEntry::getSelectRowContent).setCaption("Select").setMaximumWidth(90)
//				.setStyleGenerator(getColSelectStyleGenerator());
//		grid.addItemClickListener(event -> {
//			if (event.getColumn() == colSelect) {
//				event.getItem().setSelected(!event.getItem().isSelected());
//				updateSelectStyles();
//			}
//		});
//		// REACTION TYPE
//		if (type == ParamScanType.PARAMETER) {
//			Column<ParamScanEntry, ?> colReaction = grid.addColumn(ParamScanEntry::getReactionId).setCaption("Reaction")
//					;
//			grid.addColumn(entry -> entry.getFormulaDef().replaceAll(entry.getParamName(),
//					"<u>" + entry.getParamName() + "</u>"), new HtmlRenderer()).setCaption("Formula").setMaximumWidth(420)
//					;
//			grid.addItemClickListener(event -> {
//				if (event.getColumn() == colReaction) {
//					textAreaReactionPopup.setValue("Reaction " + event.getItem().getReactionId() + ": "
//							+ event.getItem().getReaction().getReactionStr() + "\n" + "Formula "
//							+ event.getItem().getReaction().getFormula().getNameToShow() + ": "
//							+ event.getItem().getFormulaDef());
//					reactionPopupView.setPopupVisible(true);
//				}
//			});
//		}
//		grid.addColumn(ParamScanEntry::getParamName).setCaption("Parameter")
//				;
//		grid.addColumn(ParamScanEntry::getOriginalValue).setCaption("Original Value")
//				;
//		Column<ParamScanEntry, ?> colBegin = grid.addColumn(entry -> {
//			TextField tf = new TextField();
//			beginValTFMap.put(entry, tf);
//			tf.setDescription("Begin Value");
//			tf.setValue(String.valueOf(entry.getBeginVal() != null ? entry.getBeginVal() : ""));
//			tf.addFocusListener(event -> {
//				if (!entry.isSelected()) {
//					entry.setSelected(true);
//					updateSelectStyles();
//				}
//			});
//			tf.addBlurListener(event -> {
//				try {
//					String newValStr = Utils.evalMathExpr(tf.getValue());
//					entry.setBeginVal(newValStr);
//					tf.setValue(newValStr);
//				} catch (Exception e) {
//					tf.setValue("");
//					entry.setBeginVal(null);
//				}
//			});
//			return tf;
//		}, new ComponentRenderer()).setCaption("Begin Value");
//
//		Column<ParamScanEntry, ?> colEnd = grid.addColumn(entry -> {
//			TextField tf = new TextField();
//			endValTFMap.put(entry, tf);
//			tf.setDescription("End Value");
//			tf.setValue(String.valueOf(entry.getEndVal() != null ? entry.getEndVal() : ""));
//			tf.addFocusListener(event -> {
//				if (!entry.isSelected()) {
//					entry.setSelected(true);
//					updateSelectStyles();
//				}
//			});
//			tf.addBlurListener(event -> {
//				try {
//					String newValStr = Utils.evalMathExpr(tf.getValue());
//					entry.setEndVal(newValStr);
//					tf.setValue(newValStr);
//				} catch (Exception e) {
//					tf.setValue("");
//					entry.setEndVal(null);
//				}
//			});
//			return tf;
//		}, new ComponentRenderer()).setCaption("End Value");
//
//		Column<ParamScanEntry, ?> colNumIntervals = grid.addColumn(entry -> {
//			TextField tf = new TextField();
//			numIntervalsTFMap.put(entry, tf);
//			tf.setDescription("Number of intervals to simulate between begin and end values");
//			tf.setValue(String.valueOf(entry.getNumIntervals() != null ? entry.getNumIntervals() : ""));
//			tf.addFocusListener(event -> {
//				if (!entry.isSelected()) {
//					entry.setSelected(true);
//					updateSelectStyles();
//				}
//			});
//			tf.addBlurListener(event -> {
//				try {
//					String newValStr = Utils.evalMathExpr(tf.getValue());
//					Integer newValInt = Integer.valueOf(newValStr);
//					if (newValInt < 1)
//						throw new Exception("invalid");
//					entry.setNumIntervals(newValStr);
//					tf.setValue(newValStr);
//				} catch (Exception e) {
//					tf.setValue("");
//					entry.setNumIntervals(null);
//				}
//			});
//			return tf;
//		}, new ComponentRenderer()).setCaption("#Intervals");
//
//		Column<ParamScanEntry, ?> colLog = grid.addColumn(entry -> {
//			CheckBox cb = new CheckBox();
//			cb.addValueChangeListener(event -> {
//				entry.setLogarithmic(event.getValue());
//				// update textfield values
//				((TextField) beginValTFMap.get(entry)).setValue(entry.getBeginVal());
//				((TextField) endValTFMap.get(entry)).setValue(entry.getEndVal());
//				((TextField) numIntervalsTFMap.get(entry)).setValue(entry.getNumIntervals());
//				//select entry
//				if (!entry.isSelected()) {
//					entry.setSelected(true);
//					updateSelectStyles();
//				}
//			});
//			cb.setValue(entry.isLogarithmic());
//			return cb;
//		}, new ComponentRenderer()).setCaption("Logarithm");
//	}
//
//	private StyleGenerator<ParamScanEntry> getColSelectStyleGenerator() {
//		return new StyleGenerator<ParamScanEntry>() {
//			@Override
//			public String apply(ParamScanEntry entry) {
//				if (entry.isSelected())
//					return "paramScanSelectedIcon";
//				else
//					return "paramScanUnselectedIcon";
//			}
//		};
//	}
//
//	private void fillGridItemList() {
//		gridItemList.clear();
//		if (type == ParamScanType.PARAMETER) {
//			for (Reaction r : selModel) {
//				for (FormulaValue fv : r.getFormulaGenPars().values())
//					if (fv.getType() == FormulaValueType.CONSTANT && fv.isFilled()) {
//						ParamScanEntry newEntry = new ParamScanEntry(fv, r);
//						for (ParamScanEntry configEntry : paramList) {
//							if (newEntry.equals(configEntry)) {
//								newEntry = configEntry;
//								break;
//							}
//						}
//						gridItemList.add(newEntry);
//					}
//			}
//		} else if (type == ParamScanType.IND_VAR) {
//			for (String spName : selModel.getAllSpecies().keySet()) {
//				Species sp = selModel.getAllSpecies().get(spName);
//				if (sp.getVarType() == SpeciesVarTypeType.INDEPENDENT) {
//					ParamScanEntry newEntry = new ParamScanEntry(spName, sp.getConcentration());
//					for (ParamScanEntry configEntry : paramList) {
//						if (newEntry.equals(configEntry)) {
//							newEntry = configEntry;
//							break;
//						}
//					}
//					gridItemList.add(newEntry);
//				}
//			}
//		}
//	}
//
////	private StyleGenerator<ParamScanEntry> getDefaultStyleGenerator() {
////		return new StyleGenerator<ParamScanEntry>() {
////			@Override
////			public String apply(ParamScanEntry entry) {
////				if (entry.isSelected())
////					return "paramScanSelectedCell";
////				else
////					return null;
////			}
////		};
////	}
//
//	private void updateWindowContent() {
//		if (type == ParamScanType.PARAMETER) {
//			this.setCaption("Parameter Scan - Formula Parameters");
//		} else if (type == ParamScanType.IND_VAR) {
//			this.setCaption("Parameter Scan - Independent Variables");
//		}
//		windowVL.removeAllComponents();
//
//		createGrid();
//
//		windowVL.addComponents(getHeader(), grid, getOkCancelButtonsHL());
//		windowVL.setExpandRatio(grid, 1.0f);
//	}
//
//	private Component getHeader() {
//		HorizontalLayout hl = new HorizontalLayout();
//		hl.setWidth("100%");
//		hl.setSpacing(true);
//		HorizontalLayout spacer = new HorizontalLayout();
//		spacer.setWidth("100%");
//		hl.addComponents(reactionPopupView, spacer, new InfoPopupButton("How to select parameters for Parameter Scan",
//				"1. Select all the parameters to be scanned.\n"
//						+ "2. You can see reaction definition by clicking on it.\n"
//						+ "3. Parameter scanning will be performed between Begin and End values (included).\n"
//						+ "4. The exact values to scan will be determined by the number of intervals within the range values.\n"
//						+ "5. Each parameter will be scanned independently from the other.\n"
//						+ "6. Logarithmic option will apply log10() to interval distribution.",
//				700, 300));
//		hl.setExpandRatio(spacer, 1.0f);
//		return hl;
//	}
//
//	private HorizontalLayout getOkCancelButtonsHL() {
//		HorizontalLayout hl = new HorizontalLayout();
//		hl.setWidth("100%");
//		hl.setSpacing(true);
//		HorizontalLayout spacer = new HorizontalLayout();
//		spacer.setWidth("100%");
//		hl.addComponents(spacer, getOkButton(), getCancelButton());
//		hl.setExpandRatio(spacer, 1.0f);
//		return hl;
//	}
//
//	private Button getOkButton() {
//		Button button = new Button("Save");
//		button.setId("okButton");
//		button.addClickListener(new Button.ClickListener() {
//			private static final long serialVersionUID = 1L;
//
//			public void buttonClick(ClickEvent event) {
//				saveAndClose();
//			}
//		});
//		return button;
//	}
//
//	private Button getCancelButton() {
//		Button button = new Button("Cancel");
//		button.setId("cancelButton");
//		button.addClickListener(new Button.ClickListener() {
//			private static final long serialVersionUID = 1L;
//
//			public void buttonClick(ClickEvent event) {
//				close();
//			}
//		});
//		return button;
//	}
//
//	private void checkData() throws Exception {
//		for (ParamScanEntry entry : gridItemList) {
//			if (entry.isSelected()) {
//				if (entry.getBeginVal() == null)
//					throw new Exception("Begin value is missing");
//				if (entry.getNumIntervals() == null)
//					throw new Exception("Number of intervals is missing");
//				if (!entry.isLogarithmic()) {
//					if (entry.getEndVal() == null)
//						throw new Exception("End value is missing");
//					if (Double.valueOf(entry.getBeginVal()) >= Double.valueOf(entry.getEndVal()))
//						throw new Exception("End value must be higher than Begin value");
//				}
//			}
//		}
//	}
//
//	private void saveAndClose() {
//		try {
//			checkData();
//		} catch (Exception e) {
//			Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
//			return;
//		}
//		paramList.clear();
//		for (ParamScanEntry entry : gridItemList)
//			if (entry.isSelected())
//				paramList.add(entry);
//		this.setData(true); // for window close callback
//		close();
//	}
//
//	private Component getLargeComponent() {
//		VerticalLayout vl = new VerticalLayout();
//		vl.setSpacing(true);
//		vl.setMargin(true);
//
//		HorizontalLayout header = new HorizontalLayout();
//		header.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
//		header.setSpacing(true);
//		header.setMargin(false);
//		header.setWidth("100%");
//		if (this.sessionData.getVaadinService() != null) {
//			FileResource iconResource = new FileResource(
//					new File(this.sessionData.getVaadinService().getBaseDirectory().getAbsolutePath()
//							+ "/VAADIN/themes/easymodel/img/info.png"));
//			Image im = new Image(null, iconResource);
//			im.setWidth("36px");
//			im.setHeight("36px");
//			header.addComponents(im);
//		}
//		Label tittle = new Label("Reaction and Rate definitions");
//		tittle.setStyleName("popupHeader");
//		header.addComponent(tittle);
//		VerticalLayout spacer = new VerticalLayout();
//		header.addComponent(spacer);
//		header.setExpandRatio(spacer, 1.0f);
//
//		textAreaReactionPopup = new TextArea();
//		textAreaReactionPopup.setReadOnly(true);
//		textAreaReactionPopup.setWidth("550px");
//		textAreaReactionPopup.setHeight("200px");
//
//		vl.addComponents(header, textAreaReactionPopup);
//		return vl;
//	}
//
//	public void checkToShow() throws Exception {
//		if (gridItemList.isEmpty())
//			throw new Exception("Item list is empty");
//	}
//}
