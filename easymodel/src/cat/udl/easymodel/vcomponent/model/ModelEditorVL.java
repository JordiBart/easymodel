package cat.udl.easymodel.vcomponent.model;

import java.util.HashMap;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.model.ReactionImpl;
import cat.udl.easymodel.logic.model.ReactionUtils;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.VaadinUtils;
import cat.udl.easymodel.vcomponent.AppPanel;
import cat.udl.easymodel.vcomponent.formula.FormulasEditorVL;
import cat.udl.easymodel.vcomponent.model.window.LinkReactionFormulaWindow;
import cat.udl.easymodel.vcomponent.model.window.SpeciesWindow;
import cat.udl.easymodel.vcomponent.simulation.SimulationVL;

import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class ModelEditorVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	private SessionData sessionData;
	private Panel conPanel;
	private VerticalLayout reactionsVL;
	private PopupView infoPopup = new PopupView(null, getInfoLayout());

	private Model selectedModel;
	private AppPanel mainPanel;
	private HashMap<Reaction, Button> linkFormulaButtons = new HashMap<>();

	private VerticalLayout modelVL;
	private FormulasEditorVL formulasEditorVL;

	public ModelEditorVL(Model selectedModel, AppPanel mainPanel) {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.selectedModel = selectedModel;
		this.mainPanel = mainPanel;
		formulasEditorVL = new FormulasEditorVL(this);

		// if (selectedModel.getId() == null) {
		// selectedModel.setName("");
		// selectedModel.setDescription("");
		// }

		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(true);

		modelVL = new VerticalLayout();
		modelVL.setMargin(true);
		modelVL.setSpacing(true);

		conPanel = new Panel();
		conPanel.setStyleName("reactionsPanel");
		conPanel.setHeight("100%");
		conPanel.setContent(modelVL);

		this.addComponent(conPanel);
		this.setComponentAlignment(conPanel, Alignment.MIDDLE_CENTER);

		updateConPanel();
	}

	private void updateConPanel() {
		modelVL.removeAllComponents();
		modelVL.addComponent(getHeaderHL());
		modelVL.addComponent(getNameLayout());
		reactionsVL = getReactionsVL();
		modelVL.addComponent(reactionsVL);
		modelVL.addComponent(getModelButtons());
		modelVL.setExpandRatio(reactionsVL, 1.0f);
	}

	private HorizontalLayout getHeaderHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		HorizontalLayout spacer = new HorizontalLayout();
		hl.addComponents(spacer, infoPopup, getInfoButton());
		hl.setExpandRatio(spacer, 1f);
		return hl;
	}

	private VerticalLayout getReactionsVL() {
		VerticalLayout reactionsVL = new VerticalLayout();
		reactionsVL.setCaption("Reactions");
		reactionsVL.setSpacing(false);
		Model mod = selectedModel;
		for (Reaction react : mod)
			reactionsVL.addComponent(getReactionLayout(react, false));

		return reactionsVL;
	}

	private Component getModelButtons() {
		VerticalLayout vl = new VerticalLayout();
		vl.setWidth("100%");
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidth("100%");
		HorizontalLayout hl11 = new HorizontalLayout();
		hl11.setWidth("100%");
		hl11.addComponent(getAddReactionButton());
		hl11.addComponent(getFormulaEditorButton());
		hl11.addComponent(getSpeciesButton());

		hl1.addComponents(hl11);
		hl1.setExpandRatio(hl11, 1f);
		HorizontalLayout hl2 = new HorizontalLayout();
		hl2.setWidth("100%");
		hl2.addComponent(getValidateModelButton());
		vl.addComponents(hl1, hl2);
		return vl;
	}

	private Component getValidateModelButton() {
		Button btn = new Button("Validate");
		btn.setDescription("Validate the model");
		btn.setWidth("100%");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				mainPanel.validateModel();
			}
		});
		return btn;
	}

	private Component getFormulaEditorButton() {
		Button btn = new Button("Define Rates");
		btn.setDescription("Open Rates editor");
		btn.setWidth("100%");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				conPanel.setContent(formulasEditorVL);
				conPanel.setScrollTop(0);
			}
		});
		return btn;
	}

	public void backToModel() {
		for (Reaction r : selectedModel)
			refreshLinkFormulaButton(r);
		conPanel.setContent(modelVL);
		conPanel.setScrollTop(0);
	}

	private Button getSpeciesButton() {
		Button btn = new Button("Species");
		btn.setDescription("Set initial conditions");
		btn.setWidth("100%");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				Model model = selectedModel;
				try {
					model.checkReactions();
					UI.getCurrent().addWindow(new SpeciesWindow(model));
				} catch (Exception e) {
					Notification.show(e.getMessage(), Type.WARNING_MESSAGE);
				}
			}
		});
		return btn;
	}

	private Button getInfoButton() {
		Button btn = new Button();
		btn.setDescription("How to use " + SharedData.appName);
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

	private Button getAddReactionButton() {
		Button btn = new Button("Add Reaction");
		btn.setDescription("Add a new Reaction or Process");
		btn.setWidth("100%");
		btn.setId("addButton");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				addReaction();
			}
		});
		return btn;
	}

	private void addReaction() {
		Reaction newR = new ReactionImpl();
		selectedModel.addReaction(newR);
		reactionsVL.addComponent(getReactionLayout(newR, true));
	}

	private HorizontalLayout getReactionLayout(Reaction react, boolean toFocus) {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setData(react);
		TextField reactIdTF = getReactionIdTextField(react);
		Button linkFormulaBtn = getLinkFormulaButton(react);
		linkFormulaButtons.put(react, linkFormulaBtn);
		refreshLinkFormulaButton(react);
		TextField reactTF = getReactionTextField(react);
		Button reactDelBtn = getReactionRemoveButton(react);
		hl.addComponents(reactIdTF, linkFormulaBtn, reactTF, reactDelBtn);
		hl.setExpandRatio(reactTF, 8.0f);

		if (toFocus)
			reactTF.focus();
		return hl;
	}

	private Button getReactionRemoveButton(Reaction react) {
		Button btn = new Button();
		btn.setDescription("Remove Reaction");
		btn.setStyleName("remove");
		btn.setWidth("37px");
		btn.setData(react);
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				// Button src = (Button) event.getSource();
				if (selectedModel.size() > 1) {
					selectedModel.removeReaction(react);
					linkFormulaButtons.remove(react);
					updateConPanel();
				}
			}
		});
		return btn;
	}

	private TextField getReactionTextField(Reaction react) {
		TextField tf = new TextField();
		tf.setInputPrompt("Reaction");
		tf.setWidth("100%");
		tf.setData(react);
		tf.setValue(react.getReactionStr());
		tf.addTextChangeListener(getReactionTFTextChangeListener());
		tf.setTextChangeEventMode(TextChangeEventMode.EAGER);
		tf.addBlurListener(getReactionTFBlurListener());
		tf.addShortcutListener(new ShortcutListener("Shortcut enter", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 1L;

			@Override
			public void handleAction(Object sender, Object target) {
				try {
					TextField eventTF = (TextField) target;
					if ((Reaction) eventTF.getData() == (Reaction) ((HorizontalLayout) reactionsVL
							.getComponent(reactionsVL.getComponentCount() - 1)).getData())
						addReaction();
				} catch (Exception e) {
				}
			}
		});
		setTextFieldStyle(react.getReactionStr(), tf);
		return tf;
	}

	private TextChangeListener getReactionTFTextChangeListener() {
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

	private BlurListener getReactionTFBlurListener() {
		return new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(BlurEvent event) {
				// Change reaction object
				TextField eventTF = (TextField) event.getComponent();
				Reaction r = (Reaction) eventTF.getData();
				String newText = ((TextField) event.getComponent()).getValue();
				r.setReactionStr(newText);
				refreshLinkFormulaButton(r);
			}
		};
	}

	private void setTextFieldStyle(String reactionStr, TextField tf) {
		if (ReactionUtils.isBlank(reactionStr))
			tf.setStyleName("");
		else if (ReactionUtils.isValid(reactionStr)) {
			tf.setStyleName("greenBG");
		} else
			tf.setStyleName("redBG");
	}

	private void refreshLinkFormulaButton(Reaction r) {
		if (r.areFormulaValuesValid())
			linkFormulaButtons.get(r).setStyleName("linkFnDone");
		else
			linkFormulaButtons.get(r).setStyleName("linkFn");
	}

	private Button getLinkFormulaButton(Reaction react) {
		Button btn = new Button();
		btn.setDescription("Select Kinetics");
		btn.setWidth("50px");
		btn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				openLinkFormulaWindow(react, event.getButton());
			}
		});
		return btn;
	}

	private TextField getReactionIdTextField(Reaction r) {
		TextField tf = new TextField();
		tf.setData(r);
//		tf.setId("idTF");
		tf.setWidth("50px");
		tf.setValue("R" + r.getIdJava());
		tf.setReadOnly(true);
		return tf;
	}

	private VerticalLayout getInfoLayout() {
		VerticalLayout vlt = new VerticalLayout();
		vlt.addComponent(new Label("How to use " + SharedData.appName));
		vlt.addComponent(new Label("1. Define processes"));
		VerticalLayout vl1 = new VerticalLayout();
		vl1.addComponents(new Label("Reaction definition: Substrates -> Products ; Modifiers"),
				new Label("How to write: n1*A1+n2*A2+...->m1*B1+m2*B2+...;M1;M2;..."),
				new Label("Legend: nX,mX: coefficient; AX,BX: species; MX: modifier"));
		vlt.addComponent(VaadinUtils.getIndentedVLLayout(vl1));

		vlt.addComponent(new Label("2. Select Kinetics"));
		HorizontalLayout hl2 = new HorizontalLayout();
		hl2.setSpacing(true);
		Button linkBtn = new Button();
		linkBtn.setWidth("50px");
		linkBtn.setStyleName("linkFn");
		hl2.addComponents(new Label("Press"), linkBtn);
		vlt.addComponent(VaadinUtils.getIndentedVLLayout(hl2));
		
		vlt.addComponent(new Label("3. Define initial conditions"));
		vlt.addComponent(new Label("4. Validate model"));
		vlt.addComponent(new Label("5. Run Simulation"));
		return vlt;
	}

	private VerticalLayout getNameLayout() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSizeFull();
		TextField nameTF = new TextField();
		nameTF.setValue(selectedModel.getName());
		nameTF.setCaption("Name");
		nameTF.setWidth("100%");
		nameTF.addTextChangeListener(new TextChangeListener() {
			@Override
			public void textChange(TextChangeEvent event) {
				selectedModel.setName(event.getText());
			}
		});
		TextField descTF = new TextField();
		descTF.setValue(selectedModel.getDescription());
		descTF.setCaption("Description");
		descTF.setWidth("100%");
		descTF.addTextChangeListener(new TextChangeListener() {
			@Override
			public void textChange(TextChangeEvent event) {
				selectedModel.setDescription(event.getText());
			}
		});
		vl.addComponents(nameTF, descTF);
		return vl;
	}

	private void openLinkFormulaWindow(Reaction react, Button btn) {
		if (sessionData.getCustomFormulas().isEmpty() && sessionData.getPredefinedFormulas().isEmpty()) {
			Notification.show("No rate found", Type.WARNING_MESSAGE);
			return;
		}
		if (!react.isValid()) {
			Notification.show("Invalid reaction", Type.WARNING_MESSAGE);
			return;
		}
		if (!react.parse()) {
			Notification.show("Reaction cannot use Rate reserved words", Type.WARNING_MESSAGE);
			return;
		}
		if (!sessionData.getCustomFormulas().hasAnyCompatibleFormula(react)
				&& !sessionData.getPredefinedFormulas().hasAnyCompatibleFormula(react)) {
			Notification.show("No compatible rates found", Type.WARNING_MESSAGE);
			return;
		}
		// If all checks are ok show the window
		LinkReactionFormulaWindow rLinkWindow = new LinkReactionFormulaWindow(sessionData, react);
		rLinkWindow.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				if (react.areFormulaValuesValid())
					btn.setStyleName("linkFnDone");
				else
					btn.setStyleName("linkFn");
			}
		});
		UI.getCurrent().addWindow(rLinkWindow);
	}
}
