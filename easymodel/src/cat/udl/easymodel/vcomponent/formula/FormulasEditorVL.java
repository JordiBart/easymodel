package cat.udl.easymodel.vcomponent.formula;

import java.sql.SQLException;
import java.util.HashMap;

import com.google.gwt.layout.client.Layout.Alignment;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaImpl;
import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Models;
import cat.udl.easymodel.logic.model.ModelsImpl;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.utils.VaadinUtils;
import cat.udl.easymodel.vcomponent.formula.window.FormulaEditWindow;
import cat.udl.easymodel.vcomponent.model.ModelEditorVL;

import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class FormulasEditorVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;

	// External resources
	private SessionData sessionData = null;

	private Table table;
	private VerticalLayout thisVL = null;
	private VerticalLayout formulaListVL = null;
	private PopupView infoPopup = new PopupView(null, getInfoLayout());

	private ModelEditorVL modelEditorVL;

	public FormulasEditorVL(ModelEditorVL modelEditorVL) {
		super();

		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.modelEditorVL = modelEditorVL;

		thisVL = this;
		thisVL.addStyleName("formulasVL");
		thisVL.setSpacing(true);
		thisVL.setMargin(false);
		thisVL.setSizeFull();
		updateDisplayContent();
	}

	private VerticalLayout getFormulaButtonsVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setMargin(false);
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidth("100%");
		hl1.addComponent(getNewFormulaButton());
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
		vlt.addComponent(new Label("i - How to define rate expressions"));
		vl11.addComponents(new Label(
				"Mathematica functions: Sum, Product, Length, Sin, Cos, Tan, ArcSin, ArcCos, ArcTan, Exp, Log, UnitStep"),
				new Label("Usable Mathematica function indexes: i, j, l"), new Label("Mathematica constants: E, Pi"),
				new Label("Special variables: t (time)"), new Label("X[]: Mathematica substrate list"),
				new Label("A[]: Mathematica substrate coefficient list"), new Label("M[]: Mathematica modifier list"),
				new Label("XF: first substrate"), new Label("MF: first modifier"));
		vl1.addComponents(new Label("Usable operators: +-/*^()"), new Label("Reserved symbols:"),
				VaadinUtils.getIndentedVLLayout(vl11), new Label("Example: Product[X[[j]]^g[[j]],{j,1,Length[X]}]"));
		vlt.addComponent(VaadinUtils.getIndentedVLLayout(vl1));
		vlt.addComponent(new Label("ii - Defining a new Rate expression"));
		VerticalLayout vl2 = new VerticalLayout();
		vl2.addComponents(new Label("1. Press \"Add Rate\" button"),
				new Label("2. Write the Mathematica expression for the Rate"));
		vlt.addComponent(VaadinUtils.getIndentedVLLayout(vl2));
		vlt.addComponent(new Label("iii - Modifying preexistent Rate expressions"));
		HorizontalLayout hl3 = new HorizontalLayout();
		hl3.setSpacing(true);
		Button setBtn = new Button();
		setBtn.setWidth("37px");
		setBtn.setStyleName("restriction");
		hl3.addComponents(new Label("Press"), setBtn);
		vlt.addComponent(VaadinUtils.getIndentedVLLayout(hl3));

		return vlt;
	}

	private VerticalLayout getTableVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		vl.setSizeFull();
		table = new Table();
		table.setWidth("100%");
		table.setHeight("100%");
		table.addContainerProperty("Name", TextField.class, null);
		table.addContainerProperty("Rate Definition", TextField.class, null);
		table.addContainerProperty("Private", Label.class, null);
		table.addContainerProperty("Edit", Button.class, null);
		table.addContainerProperty("Rem.", Button.class, null);
		table.setColumnAlignments(Align.CENTER,Align.CENTER,Align.CENTER,Align.CENTER,Align.CENTER);
		for (Formula f : sessionData.getCustomFormulas()) {
			Label privLabel = new Label(f.getRepositoryType() == RepositoryType.PRIVATE && f.getUser() == sessionData.getUser() ? "YES" : "");
			privLabel.setSizeUndefined();
			table.addItem(
					new Object[] { getFormulaNameTextField(f),
							getFormulaTextField(f),
							privLabel,
							getEditButton(f),
							getRemoveFormulaButton(f)
							},
					f);
		}
//		for (Formula f : sessionData.getPredefinedFormulas()) {
//			table.addItem(
//					new Object[] { getFormulaNameTextField(f),
//							getFormulaTextField(f),
//							new Label(""),
//							new Label("Predefined")
//							},
//					f);
//		}
		vl.addComponent(table);
		return vl;
	}
	
	
	private void updateDisplayContent() {
		thisVL.removeAllComponents();

		formulaListVL = getTableVL();
//		formulaListVL.setSpacing(false);
//		for (Formula f : sessionData.getCustomFormulas())
//			formulaListVL.addComponent(getFormulaLayout(f, false));

		VerticalLayout formulasPanelVL = new VerticalLayout();
		formulasPanelVL.setSizeFull();
		formulasPanelVL.addComponent(formulaListVL);
		//formulasPanelVL.addComponent(getFormulaButtonsVL());
		formulasPanelVL.setExpandRatio(formulaListVL, 1.0f);

		Panel formulasPanel = new Panel();
		formulasPanel.setStyleName("withoutborder");
		formulasPanel.setSizeFull();
		formulasPanel.setContent(formulasPanelVL);

		thisVL.addComponent(getNavigationVL());
		thisVL.addComponent(formulasPanel);
		thisVL.setExpandRatio(formulasPanel, 1.0f);
	}

	private VerticalLayout getNavigationVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidth("100%");
		HorizontalLayout hl11 = new HorizontalLayout();
		hl11.setWidth("100%");
		hl11.addComponents(getBackToModelBtn(),getNewFormulaButton());
		hl1.addComponents(hl11, infoPopup, getInfoButton());
		hl1.setExpandRatio(hl11, 1f);
		vl.addComponents(hl1);

		return vl;
	}

	private Button getBackToModelBtn() {
		Button btn = new Button("Back to Model editor");
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
		tf.setValue(f.getName());
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
		tf.setInputPrompt("Formula");
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

	private TextChangeListener getFormulaTFTextChangeListener() {
		return new TextChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void textChange(TextChangeEvent event) {
				// Change TextField style
				String newText = event.getText();
				TextField eventTF = (TextField) event.getComponent();
				setTextFieldStyle(newText, eventTF);
			}
		};
	}

	private BlurListener getFormulaTFBlurListener() {
		return new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(BlurEvent event) {
				Formula f = ((Formula) ((TextField) event.getComponent()).getData());
				String newText = ((TextField) event.getComponent()).getValue();
				if (!newText.equals(f.getFormulaDef())) {
					if (!"".equals(f.getFormulaDef())) { // old value != ""
						Notification.show(
								"WARNING: formula " + f.getNameToShow() + " has been unlinked from all reactions",
								Type.WARNING_MESSAGE);
					}
					sessionData.getModels().removeFormulaFromReactions(f);
					f.setFormula(newText);
				}
			}
		};
	}

	private void setTextFieldStyle(String formulaStr, TextField tf) {
		if (FormulaUtils.isBlank(formulaStr))
			tf.setStyleName("");
		else if (FormulaUtils.isValid(formulaStr))
			tf.setStyleName("greenBG");
		else
			tf.setStyleName("redBG");
	}

	// BUTTONS
	private Button getEditButton(Formula f) {
		Button btn = new Button();
		btn.setDescription("Edit Rate");
		btn.setStyleName("editBtn");
		btn.setWidth("37px");
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
		btn.setWidth("37px");
		btn.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				sessionData.getCustomFormulas().removeFormula(f);
				sessionData.getModels().removeFormulaFromReactions(f);
				Notification.show("Rate " + f.getNameToShow() + " has been unlinked from all reactions",
						Type.TRAY_NOTIFICATION);
				updateDisplayContent();
			}
		});
		return btn;
	}

	private Button getNewFormulaButton() {
		Button btn = new Button("New Rate");
		btn.setDescription("Add a new rate");
		btn.setWidth("100%");
		btn.setId("addFormulaButton");
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

	private void addFormula() {
		Formula newForm = new FormulaImpl("", "", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE);
		sessionData.getCustomFormulas().addFormula(newForm);
		formulaListVL.addComponent(getFormulaLayout(newForm, true));
	}

}
