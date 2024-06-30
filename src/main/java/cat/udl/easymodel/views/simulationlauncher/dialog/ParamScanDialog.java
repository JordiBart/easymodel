package cat.udl.easymodel.views.simulationlauncher.dialog;


import cat.udl.easymodel.logic.formula.FormulaArrayValue;
import cat.udl.easymodel.logic.formula.FormulaValue;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.model.Species;
import cat.udl.easymodel.logic.simconfig.ParamScanEntry;
import cat.udl.easymodel.logic.simconfig.SimParamScanConfig;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.ParamScanType;
import cat.udl.easymodel.logic.types.SpeciesType;
import cat.udl.easymodel.logic.types.SpeciesVarTypeType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.MathematicaUtils;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.VaadinSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;

public class ParamScanDialog extends Dialog {
	private ParamScanType type = null;
	private SimParamScanConfig simParamScanConfig = null;
	private ArrayList<ParamScanEntry> paramList = null;

	private HashMap<ParamScanEntry, TextField> beginValTFMap = new HashMap<>();
	private HashMap<ParamScanEntry, TextField> endValTFMap = new HashMap<>();
	private HashMap<ParamScanEntry, TextField> numIntervalsTFMap = new HashMap<>();

	private Model selModel = null;
	private SessionData sessionData = null;
	private SharedData sharedData = SharedData.getInstance();
	private VerticalLayout mainVL = null;
	private Grid<ParamScanEntry> grid = null;
	private ArrayList<ParamScanEntry> gridItemList = new ArrayList<>();
	private ArrayList<ParamScanEntry> loadedFromParamList = new ArrayList<>();
	private TextArea textAreaReactionPopup;

	public ParamScanDialog(SimParamScanConfig simParamScanConfig, ParamScanType type) {
		super();

		this.sessionData = (SessionData) VaadinSession.getCurrent().getAttribute("s");
		this.selModel = this.sessionData.getSelectedModel();

		this.simParamScanConfig = simParamScanConfig;
		this.type = type;
		if (type == ParamScanType.PARAMETER)
			paramList = simParamScanConfig.getParameters();
		else if (type == ParamScanType.IND_VAR)
			paramList = simParamScanConfig.getIndependentVars();
		
		this.setWidth("100%");
		this.setHeight("100%");
		this.setModal(true);
		setDraggable(true);

		VerticalLayout windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setPadding(false);
		windowVL.setSizeFull();
		
		mainVL = new VerticalLayout();
		mainVL.setSpacing(true);
		mainVL.setPadding(false);
		mainVL.setSizeFull();
		mainVL.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
		windowVL.add(ToolboxVaadin.getDialogHeader(this,"Parameter Scan - "+type.getString(),new InfoDialogButton("How to select parameters for Parameter Scan",
				"1. Select all the parameters to be scanned.\n"
						+ "2. Reaction definition can be shown by clicking on its label.\n"
						+ "3. Parameter scanning will be performed between Begin and End values (included).\n"
						+ "4. The exact values to be scanned will be determined by the number of intervals within the range values.\n"
						+ "5. Each parameter will be scanned independently from the others.\n"
						+ "6. Logarithmic option will apply log10() function to the interval distribution.",
				"700px", "300px")),mainVL);
		windowVL.expand(mainVL);
		this.add(windowVL);
		updateWindowContent();
	}

	private void createGrid() {
		grid = new Grid<ParamScanEntry>();
		grid.setSelectionMode(Grid.SelectionMode.MULTI);
		grid.setSizeFull();
		fillGridItemList();
		grid.setItems(gridItemList);

		beginValTFMap.clear();
		endValTFMap.clear();
		numIntervalsTFMap.clear();
		// REACTION TYPE
		if (type == ParamScanType.PARAMETER) {
			Grid.Column<ParamScanEntry> colReaction = grid.addColumn(ParamScanEntry::getReactionId)
					.setHeader("Reaction");
			grid.addComponentColumn(entry -> {return new Html(entry.getFormulaDef().replaceAll(!entry.isArrayParameter() ? entry.getParameterName() : ".^",
					"<u>" + entry.getParameterName() + "</u>"));}).setHeader("Rate");
			grid.addItemClickListener(event -> {
				if (event.getColumn() == colReaction) {
					textAreaReactionPopup.setValue("Reaction " + event.getItem().getReactionId() + ": "
							+ event.getItem().getReaction().getReactionStr() + "\n" + "Rate "
							+ event.getItem().getReaction().getFormula().getNameToShow() + ": "
							+ event.getItem().getFormulaDef());
				}
			});
		}
		grid.addColumn(ParamScanEntry::getShowParameterName).setHeader(type.getString());
		grid.addColumn(ParamScanEntry::getParameterValue).setHeader("Original Value");
		grid.addComponentColumn(entry -> {
			TextField tf = new TextField();
			tf.setWidth("100%");
			beginValTFMap.put(entry, tf);
			tf.setTitle("Begin Value");
			tf.setValue(String.valueOf(entry.getBeginVal() != null ? entry.getBeginVal() : ""));
			tf.setValueChangeMode(ValueChangeMode.ON_BLUR);
			tf.addValueChangeListener(event -> {
				beginAndEndValBlur(entry, tf, true);
			});
			return tf;
		}).setHeader("Begin Value");

		grid.addComponentColumn(entry -> {
			TextField tf = new TextField();
			tf.setWidth("100%");
			endValTFMap.put(entry, tf);
			tf.setTitle("End Value");
			tf.setValue(String.valueOf(entry.getEndVal() != null ? entry.getEndVal() : ""));
			tf.setValueChangeMode(ValueChangeMode.ON_BLUR);
			tf.addValueChangeListener(event -> {
				beginAndEndValBlur(entry, tf, false);
			});
			return tf;
		}).setHeader("End Value");

		grid.addComponentColumn(entry -> {
			TextField tf = new TextField();
			tf.setWidth("100%");
			numIntervalsTFMap.put(entry, tf);
			tf.setTitle("Number of intervals to simulate between begin and end values");
			tf.setValue(String.valueOf(entry.getNumIntervals() != null ? entry.getNumIntervals() : ""));
			tf.setValueChangeMode(ValueChangeMode.ON_BLUR);
			tf.addValueChangeListener(event -> {
				numIntervalsBlur(entry, tf);
			});
			return tf;
		}).setHeader("#Intervals");

		grid.addComponentColumn(entry -> {
			Checkbox cb = new Checkbox();
			cb.setValue(entry.isLogarithmic());
			cb.addValueChangeListener(event -> {
				entry.setLogarithmic(event.getValue());
				if (event.getValue()) {
					TextField beginValTF = beginValTFMap.get(entry);
					TextField endValTF= endValTFMap.get(entry);
					TextField numIntervalsTF=numIntervalsTFMap.get(entry);
					beginValTF.setValue("0.00001");
					endValTF.setValue("100");
					numIntervalsTF.setValue("7");
					beginAndEndValBlur(entry, beginValTF, true);
					beginAndEndValBlur(entry, endValTF, false);
					numIntervalsBlur(entry, numIntervalsTF);
				}
			});
			return cb;
		}).setHeader("Logarithmic");
		// select loaded items
		for (ParamScanEntry gridEntry : loadedFromParamList)
			grid.select(gridEntry);
	}

	private void numIntervalsBlur(ParamScanEntry entry, TextField tf) {
		try {
			String newValStr = Utils.evalMathExpr(tf.getValue());
			Integer newValInt = Integer.valueOf(newValStr);
			if (newValInt < 1)
				throw new Exception("invalid");
			entry.setNumIntervals(newValStr);
			tf.setValue(newValStr);
		} catch (Exception e) {
			entry.setNumIntervals(null);
			tf.setValue("");
		}
	}
	
	private void beginAndEndValBlur(ParamScanEntry entry, TextField tf, boolean isBeginVal) {
		try {
			String newValStr = Utils.evalMathExpr(tf.getValue());
			if (entry.isLogarithmic()) {
				Double origNewVal = Double.valueOf(newValStr);
				Double sign = origNewVal < 0 || !isBeginVal && origNewVal==0 ? -1d : 1d;
				if (Math.abs(origNewVal) < Math.pow(10, MathematicaUtils.minLogVal))
					newValStr = Utils.doubleToString(sign * Math.pow(10, MathematicaUtils.minLogVal));
			}
			tf.setValue(newValStr);
			if (isBeginVal)
				entry.setBeginVal(newValStr);
			else
				entry.setEndVal(newValStr);
		} catch (Exception e) {
			tf.setValue("");
			if (isBeginVal)
				entry.setBeginVal(null);
			else
				entry.setEndVal(null);
		}
	}

	private void fillGridItemList() {
		gridItemList.clear();
		loadedFromParamList.clear();
		if (type == ParamScanType.PARAMETER) {
			for (Reaction r : selModel) {
				for (FormulaValue fv : r.getFormulaGeneralParameters().values()) {
					if (fv.getType() == FormulaValueType.CONSTANT && fv.isFilled()) {
						ParamScanEntry newEntry = new ParamScanEntry(r,fv.getParameterName(),fv.getStringValue());
						for (ParamScanEntry configEntry : paramList) {
							if (newEntry.equals(configEntry)) {
								newEntry.copyFrom(configEntry);
								loadedFromParamList.add(newEntry);
								break;
							}
						}
						gridItemList.add(newEntry);
					}
				}
				SortedMap<String, SortedMap<String, FormulaArrayValue>> mapByArrayPar = r.getFormulaSubstratesArrayParameters();
				for (String arrParamName : mapByArrayPar.keySet()) {
					SortedMap<String,FormulaArrayValue> mapBySpecies = mapByArrayPar.get(arrParamName);
					for (String spName : mapBySpecies.keySet()) {
						FormulaArrayValue fav = mapBySpecies.get(spName);
						if (fav.isFilled()) {
							ParamScanEntry newEntry = new ParamScanEntry(r,arrParamName,fav.getValue(), SpeciesType.REACTIVE,spName);
							for (ParamScanEntry configEntry : paramList) {
								if (newEntry.equals(configEntry)) {
									newEntry.copyFrom(configEntry);
									loadedFromParamList.add(newEntry);
									break;
								}
							}
							gridItemList.add(newEntry);
						}
					}
				}
				mapByArrayPar = r.getFormulaModifiersArrayParameters();
				for (String arrParamName : mapByArrayPar.keySet()) {
					SortedMap<String,FormulaArrayValue> mapBySpecies = mapByArrayPar.get(arrParamName);
					for (String spName : mapBySpecies.keySet()) {
						FormulaArrayValue fav = mapBySpecies.get(spName);
						if (fav.isFilled()) {
							ParamScanEntry newEntry = new ParamScanEntry(r,arrParamName,fav.getValue(),SpeciesType.MODIFIER,spName);
							for (ParamScanEntry configEntry : paramList) {
								if (newEntry.equals(configEntry)) {
									newEntry.copyFrom(configEntry);
									loadedFromParamList.add(newEntry);
									break;
								}
							}
							gridItemList.add(newEntry);
						}
					}
				}
			}
		} else if (type == ParamScanType.IND_VAR) {
			for (String spName : selModel.getAllSpecies().keySet()) {
				Species sp = selModel.getAllSpecies().get(spName);
				if (sp.getVarType() == SpeciesVarTypeType.INDEPENDENT) {
					ParamScanEntry newEntry = new ParamScanEntry(spName, sp.getConcentration());
					for (ParamScanEntry configEntry : paramList) {
						if (newEntry.equals(configEntry)) {
							newEntry.copyFrom(configEntry);
							loadedFromParamList.add(newEntry);
							break;
						}
					}
					gridItemList.add(newEntry);
				}
			}
		}
	}

	private void updateWindowContent() {
		mainVL.removeAll();

		createGrid();

		mainVL.add(grid, getOkCancelButtonsHL());
		mainVL.expand(grid);
	}

	private HorizontalLayout getOkCancelButtonsHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		HorizontalLayout spacer = new HorizontalLayout();
		spacer.setWidth("100%");
		hl.add(getResetButton(), spacer, getOkButton(), getCancelButton());
		hl.expand(spacer);
		return hl;
	}
	private Button getResetButton() {
		Button button = new Button("Reset");
		button.setWidth("150px");
		button.addClickListener(event -> {
			grid.deselectAll();
			saveAndClose();
		});
		return button;
	}
	private Button getOkButton() {
		Button button = new Button("Save");
		button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		button.setWidth("150px");
		button.addClickListener(event -> {
				saveAndClose();
		});
		return button;
	}

	private Button getCancelButton() {
		Button button = new Button("Cancel");
		button.setWidth("150px");
		button.addClickListener(event -> {
				close();
		});
		return button;
	}

	private void checkData() throws Exception {
		for (ParamScanEntry entry : gridItemList) {
			if (grid.getSelectedItems().contains(entry)) {
				if (entry.getBeginVal() == null)
					throw new Exception(entry.getMathematicaParamName() + ": Begin value is missing");
				if (entry.getEndVal() == null)
					throw new Exception(entry.getMathematicaParamName() + ": End value is missing");
				if (entry.getNumIntervals() == null)
					throw new Exception(entry.getMathematicaParamName() + ": Number of intervals is missing");
				if (Double.valueOf(entry.getBeginVal()) >= Double.valueOf(entry.getEndVal()))
					throw new Exception(entry.getMathematicaParamName() + ": End value must be higher than Begin value");
				if (entry.isLogarithmic()) {
					if (Math.abs(Double.valueOf(entry.getBeginVal())) < Math.pow(10, MathematicaUtils.minLogVal))
						throw new Exception(entry.getMathematicaParamName() + ": Abs(Begin value) must higher than "
								+ Math.pow(10, MathematicaUtils.minLogVal));
					if (Math.abs(Double.valueOf(entry.getEndVal())) < Math.pow(10, MathematicaUtils.minLogVal))
						throw new Exception(entry.getMathematicaParamName() + ": Abs(End value) must higher than "
								+ Math.pow(10, MathematicaUtils.minLogVal));
					if (Double.valueOf(entry.getBeginVal()) < 0 && Double.valueOf(entry.getEndVal()) > 0)
						throw new Exception(entry.getMathematicaParamName()
								+ ": due to log10() definition, both Begin and End values\nmust be either positive or negative");
				}
			}
		}
	}

	private void saveAndClose() {
		try {
			checkData();
		} catch (Exception e) {
			ToolboxVaadin.showWarningNotification(e.getMessage());
			return;
		}
		paramList.clear();
		for (ParamScanEntry entry : gridItemList)
			if (grid.getSelectedItems().contains(entry))
				paramList.add(entry);
		close();
	}

	public void checkToShow() throws Exception {
		if (gridItemList.isEmpty())
			throw new Exception("Item list is empty");
	}
}
