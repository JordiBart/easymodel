package cat.udl.easymodel.vcomponent.formula.window;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.Formulas;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.main.SharedData;

public class ImportPredefinedFormulasWindow extends Window {
	private static final long serialVersionUID = 1L;
	private Model selModel = null;
	private VerticalLayout windowVL = null;
	private SharedData sharedData = SharedData.getInstance();
	private Grid<Formula> grid = null;

	public ImportPredefinedFormulasWindow(Model selModel) {
		super();

		this.selModel = selModel;

		this.setData(new Boolean(false)); // for window close callback
		this.setCaption("Import Predefined Rates");
		this.setClosable(true);
		this.setWidth("800px");
		this.setHeight("600px");
		this.setWindowMode(WindowMode.MAXIMIZED);
		this.setModal(true);
		this.center();

		windowVL = new VerticalLayout();
		windowVL.setSpacing(true);
		windowVL.setMargin(true);
		windowVL.setSizeFull();
		windowVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		this.setContent(windowVL);

		this.addShortcutListener(new ShortcutListener("Shortcut enter", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {
				saveAndClose();
			}
		});

		displayWindowContent();
	}

	private void createGrid() {
		grid = new Grid<Formula>();
		grid.setSelectionMode(SelectionMode.MULTI);
		grid.setSizeFull();
		grid.setItems(sharedData.getPredefinedPlusGenericFormulas());
		grid.addColumn(Formula::getNameToShow).setCaption("Name");//.setWidth(250);//.setExpandRatio(1).setMaximumWidth(50);
		grid.addColumn(Formula::getFormulaDef).setCaption("Rate Definition");//.setWidth(1000);
//		grid.addSelectionListener(event -> {
//			Set<Formula> selected = event.getAllSelectedItems();
//			Notification.show(selected.size() + " items selected");
//		});
	}

	private void displayWindowContent() {
		windowVL.removeAllComponents();

		createGrid();

		windowVL.addComponents(grid, getOkCancelButtonsHL());
		windowVL.setExpandRatio(grid, 1.0f);
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
					checkData();
					saveAndClose();
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

	private void checkData() throws Exception {
	}

	private void saveAndClose() {
		for (Formula formulaToCopy : grid.getSelectedItems()) {
			Formula fCopy = new Formula(formulaToCopy);
			fCopy.setId(null);
			fCopy.setFormulaType(FormulaType.MODEL);
			fCopy.setDirty(true);
			selModel.getFormulas().addFormula(fCopy);
		}
		this.setData(new Boolean(true)); // for window close callback
		close();
	}
}
