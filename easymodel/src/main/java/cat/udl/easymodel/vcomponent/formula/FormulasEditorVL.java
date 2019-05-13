package cat.udl.easymodel.vcomponent.formula;

import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.VaadinUtils;
import cat.udl.easymodel.vcomponent.formula.window.FormulaEditWindow;
import cat.udl.easymodel.vcomponent.formula.window.ImportPredefinedFormulasWindow;
import cat.udl.easymodel.vcomponent.model.ModelEditorVL;

public class FormulasEditorVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	// External resources
	private SessionData sessionData = null;
	private Model selModel = null;

	private Grid<Formula> grid;
	private VerticalLayout thisVL = null;
	private VerticalLayout formulaListVL = null;
	private PopupView infoPopup = new PopupView(null, getInfoLayout());

	private ModelEditorVL modelEditorVL;

	public FormulasEditorVL(ModelEditorVL modelEditorVL) {
		super();

		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.selModel = this.sessionData.getSelectedModel();
		this.modelEditorVL = modelEditorVL;

		thisVL = this;
		thisVL.setSpacing(true);
		thisVL.setMargin(true);
		thisVL.setSizeFull();
		updateDisplayContent();
	}

	private VerticalLayout getFormulaButtonsVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setMargin(false);
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidth("100%");
		hl1.addComponent(getNewRateButton());
		vl.addComponents(hl1);
		return vl;
	}

	private Button getInfoButton() {
		Button btn = new Button();
		btn.setDescription("How to use Rate editor");
		btn.setWidth("36px");
		btn.setStyleName("infoBtn");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				infoPopup.setPopupVisible(true);
			}
		});
		return btn;
	}

	private VerticalLayout getInfoLayout() {
		VerticalLayout vlt = new VerticalLayout();
		VerticalLayout vl1 = new VerticalLayout();
		VerticalLayout vl11 = new VerticalLayout();
		vl1.setMargin(false);
		vl11.setMargin(false);
		vlt.addComponent(new Label("i - How to define rate expressions"));
		vl11.addComponents(new Label(
				"Mathematica functions/constants: "+FormulaUtils.mathematicaSymbolPrefix+"<Mathematica function>"),
				new Label("Mathematica function indexes: "+FormulaUtils.mathematicaIndexPrefix+"<index>"),
				new Label("Special variables: "+FormulaUtils.mathematicaBuiltInPrefix+"t (time)"), new Label(FormulaUtils.mathematicaBuiltInPrefix+"X[]: Mathematica substrate list"),
				new Label(FormulaUtils.mathematicaBuiltInPrefix+"A[]: Mathematica substrate coefficient list"), new Label(FormulaUtils.mathematicaBuiltInPrefix+"M[]: Mathematica modifier list"),
				new Label(FormulaUtils.mathematicaBuiltInPrefix+"XF: first substrate"), new Label(FormulaUtils.mathematicaBuiltInPrefix+"MF: first modifier"));
		vl1.addComponents(new Label("Usable operators: +-/*^()"), new Label("Reserved symbols:"),
				VaadinUtils.getIndentedVLLayout(vl11), new Label("Example: "+FormulaUtils.mathematicaSymbolPrefix+"Product["+FormulaUtils.mathematicaBuiltInPrefix+"X[["+FormulaUtils.mathematicaIndexPrefix+"j]]^g[["+FormulaUtils.mathematicaIndexPrefix+"j]],{"+FormulaUtils.mathematicaIndexPrefix+"j,1,"+FormulaUtils.mathematicaSymbolPrefix+"Length["+FormulaUtils.mathematicaBuiltInPrefix+"X]}]"));
		vlt.addComponent(VaadinUtils.getIndentedVLLayout(vl1));
		vlt.addComponent(new Label("ii - Defining a new Rate expression"));
		VerticalLayout vl2 = new VerticalLayout();
		vl2.setMargin(false);
		vl2.addComponents(new Label("1. Press \"Add Rate\" button"),
				new Label("2. Write the Mathematica expression for the Rate"));
		vlt.addComponent(VaadinUtils.getIndentedVLLayout(vl2));
		vlt.addComponent(new Label("iii - Edit rate expressions"));
		HorizontalLayout hl3 = new HorizontalLayout();
		hl3.setMargin(false);
		hl3.setSpacing(true);
		Button setBtn = new Button();
		setBtn.setWidth("37px");
		setBtn.setStyleName("editBtn");
		hl3.addComponents(new Label("Press"), setBtn);
		vlt.addComponent(VaadinUtils.getIndentedVLLayout(hl3));
		vlt.addComponent(new Label("iv - Importing predefined rate expressions"));
		VerticalLayout vl3 = new VerticalLayout();
		vl3.setMargin(false);
		vl3.addComponents(new Label("1. Press \"Import Rates\" button"),
				new Label("2. Select the rates to import into the model"));
		vlt.addComponent(VaadinUtils.getIndentedVLLayout(vl3));
		return vlt;
	}

	private void createGrid() {
		grid = new Grid<Formula>();
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setSizeFull();
		grid.setItems(selModel.getFormulas());
		grid.addColumn(Formula::getNameRaw).setCaption("Name").setWidth(150);
		grid.addColumn(Formula::getFormulaDef).setCaption("Rate Definition").setWidth(420);
		grid.addComponentColumn(formula -> {
		      return getEditButton(formula);
		}).setCaption("Edit").setWidth(90).setStyleGenerator(item -> "v-align-middle").setWidth(90);
		grid.addComponentColumn(formula -> {
		      return getRemoveFormulaButton(formula);
		}).setCaption("Remove").setWidth(90).setStyleGenerator(item -> "v-align-middle").setExpandRatio(1);
	}
	
	private void updateDisplayContent() {
		thisVL.removeAllComponents();

		createGrid();
		thisVL.addComponent(getNavigationVL());
		thisVL.addComponent(grid);
		thisVL.setExpandRatio(grid, 1.0f);
	}

	private VerticalLayout getNavigationVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(false);
		vl.setMargin(false);
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidth("100%");
		hl1.setSpacing(false);
		hl1.setMargin(false);
		HorizontalLayout hl11 = new HorizontalLayout();
		hl11.setWidth("100%");
		hl11.setSpacing(false);
		hl11.setMargin(false);
		hl11.addComponents(getBackToModelBtn(),getNewRateButton(),getImportRatesButton());
		hl1.addComponents(hl11, infoPopup, getInfoButton());
		hl1.setExpandRatio(hl11, 1f);
		vl.addComponents(hl1);

		return vl;
	}

	private Button getBackToModelBtn() {
		Button btn = new Button("Back to Model Editor");
		btn.setWidth("100%");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				modelEditorVL.backToModel();
			}
		});
		return btn;
	}

	private Layout getFormulaLayout(Formula f, boolean toFocus) {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setData(f);
		// TextField formulaIdTF = getFormulaIdTextField(f);
		// formulaIdTF.setWidth("50px");
		TextField formulaNameTF = getFormulaNameTextField(f);
		TextField formulaTF = getFormulaTextField(f);
		Button restrictionBtn = getEditButton(f);
		Button removeBtn = getRemoveFormulaButton(f);
		hl.addComponents(formulaNameTF, formulaTF, restrictionBtn, removeBtn);
		hl.setExpandRatio(formulaNameTF, 1f);
		hl.setExpandRatio(formulaTF, 5f);
		if (toFocus)
			formulaTF.focus();

		return hl;
	}

	private TextField getFormulaIdTextField(Formula f) {
		TextField tf = new TextField();
		tf.setData(f);
		tf.setWidth("100%");
		tf.setValue("F" + f.getIdJava());
		tf.setReadOnly(true);
		return tf;
	}

	private TextField getFormulaNameTextField(Formula f) {
		TextField tf = new TextField();
		// tf.setInputPrompt("Name");
		tf.setWidth("100%");
		tf.setData(f);
		tf.setId("tfFormulaName" + f.getIdJava());
		tf.setValue(f.getNameRaw());
		tf.setReadOnly(true);
		// tf.addBlurListener(getFormulaNameTFBlurListener());
		return tf;
	}

	private BlurListener getFormulaNameTFBlurListener() {
		return new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(BlurEvent event) {
				String newText = ((TextField) event.getComponent()).getValue();
				((Formula) ((TextField) event.getComponent()).getData()).setName(newText);
			}
		};
	}

	private TextField getFormulaTextField(Formula f) {
		TextField tf = new TextField();
		tf.setPlaceholder("Formula");
		tf.setWidth("100%");
		tf.setData(f);
		tf.setId("tfFormula" + f.getIdJava());
		tf.setValue(f.getFormulaDef());
		tf.setReadOnly(true);
		// tf.addTextChangeListener(getFormulaTFTextChangeListener());
		// tf.setTextChangeEventMode(TextChangeEventMode.EAGER);
		// tf.addBlurListener(getFormulaTFBlurListener());
		// tf.addShortcutListener(new ShortcutListener("Shortcut enter",
		// ShortcutAction.KeyCode.ENTER, null) {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void handleAction(Object sender, Object target) {
		// try {
		// TextField eventTF = (TextField) target;
		// if ((Formula) eventTF.getData() == (Formula) ((HorizontalLayout)
		// formulaListVL
		// .getComponent(formulaListVL.getComponentCount() - 1)).getData())
		// addFormula();
		// } catch (Exception e) {
		// }
		// }
		// });
		// setTextFieldStyle(f.getFormula(), tf);
		return tf;
	}

	// BUTTONS
	private Button getEditButton(Formula f) {
		Button btn = new Button();
		btn.setDescription("Edit Rate");
		btn.setStyleName("editBtn");
		btn.setWidth("32px");
		btn.setHeight("32px");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				FormulaEditWindow few = new FormulaEditWindow(f);
				few.addCloseListener(new CloseListener() {
					@Override
					public void windowClose(CloseEvent e) {
						if ((Boolean) e.getWindow().getData()) {
							updateDisplayContent();
						}
					}
				});
				UI.getCurrent().addWindow(few);
			}
		});
		return btn;
	}

	private Button getRemoveFormulaButton(Formula f) {
		Button btn = new Button();
		btn.setDescription("Remove Rate (only owned rates can be deleted from database)");
		btn.setStyleName("remove");
		btn.setWidth("32px");
		btn.setHeight("32px");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				selModel.removeFormula(f);
				sessionData.getModels().removeFormulaFromReactions(f);
				Notification.show("Rate " + f.getNameToShow() + " has been unlinked from all reactions",
						Type.TRAY_NOTIFICATION);
				grid.setItems(selModel.getFormulas());
//				updateDisplayContent();
			}
		});
		return btn;
	}

	private Button getNewRateButton() {
		Button btn = new Button("New Rate");
		btn.setDescription("Add a new rate");
		btn.setWidth("100%");
		btn.setId("addRateButton");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				FormulaEditWindow few = new FormulaEditWindow(null);
				few.addCloseListener(new CloseListener() {
					@Override
					public void windowClose(CloseEvent e) {
						if ((Boolean) e.getWindow().getData()) {
							updateDisplayContent();
						}
					}
				});
				UI.getCurrent().addWindow(few);
			}
		});
		return btn;
	}
	private Button getImportRatesButton() {
		Button btn = new Button("Import Rates");
		btn.setDescription("Import predifined rates");
		btn.setWidth("100%");
		btn.setId("importRateButton");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				ImportPredefinedFormulasWindow window = new ImportPredefinedFormulasWindow(selModel);
				window.addCloseListener(new CloseListener() {
					@Override
					public void windowClose(CloseEvent e) {
						if ((Boolean) e.getWindow().getData()) {
							updateDisplayContent();
						}
					}
				});
				UI.getCurrent().addWindow(window);
			}
		});
		return btn;
	}
}