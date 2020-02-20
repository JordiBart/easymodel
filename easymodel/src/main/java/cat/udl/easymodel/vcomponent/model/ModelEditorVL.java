package cat.udl.easymodel.vcomponent.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import cat.udl.easymodel.logic.formula.Formulas;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.model.ReactionUtils;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.app.AppPanel;
import cat.udl.easymodel.vcomponent.formula.FormulasEditorVL;
import cat.udl.easymodel.vcomponent.model.window.DescriptionEditWindow;
import cat.udl.easymodel.vcomponent.model.window.LinkReactionFormulaWindow;
import cat.udl.easymodel.vcomponent.model.window.SpeciesWindow;

public class ModelEditorVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	private SessionData sessionData;
	private Panel conPanel;
	private VerticalLayout reactionsVL;
	private ArrayList<TextField> reactionsTFList = new ArrayList<>();

	private Model selectedModel;
	private AppPanel mainPanel;
	private HashMap<Reaction, Button> linkFormulaButtons = new HashMap<>();

	private VerticalLayout modelVL;
	private FormulasEditorVL formulasEditorVL;
	private Button validateBtn = null;
	private TextField nameTF = null;

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
		modelVL.addComponent(getNameDescVL());
		reactionsVL = getReactionsVL();
		modelVL.addComponent(reactionsVL);
		modelVL.addComponent(getModelButtons());
		modelVL.setExpandRatio(reactionsVL, 1.0f);
		if (SharedData.getInstance().isDebug())
			validateBtn.focus();
		else if (nameTF.getValue().isEmpty())
			nameTF.focus();
	}

	private HorizontalLayout getHeaderHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setHeight("37px");
		FileResource resource = new FileResource(
				new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()
						+ "/VAADIN/themes/easymodel/img/easymodel-logo-120.png"));
		Image image = new Image(null, resource);
		image.setHeight("36px");
		HorizontalLayout head = new HorizontalLayout();
		head.setWidth("100%");
		head.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		head.addComponent(image);
		hl.addComponents(head, getInfoButton());
		hl.setExpandRatio(head, 1f);
		return hl;
	}

	private VerticalLayout getReactionsVL() {
		VerticalLayout reactionsVL = new VerticalLayout();
		reactionsVL.setCaption("Reactions");
		reactionsVL.setSpacing(false);
		reactionsVL.setMargin(false);
		reactionsTFList.clear();
		Model mod = selectedModel;
		for (Reaction react : mod)
			reactionsVL.addComponent(getReactionLayout(react, false));

		return reactionsVL;
	}

	private Component getModelButtons() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(false);
		vl.setMargin(false);
		vl.setWidth("100%");
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setSpacing(false);
		hl1.setMargin(false);
		hl1.setSizeFull();
		hl1.addComponent(getAddReactionButton());
		hl1.addComponent(getFormulaEditorButton());
		hl1.addComponent(getSpeciesButton());

		HorizontalLayout hl2 = new HorizontalLayout();
		hl2.setSpacing(false);
		hl2.setMargin(false);
		hl2.setWidth("100%");
		validateBtn = getValidateModelButton();
		hl2.addComponent(validateBtn);
		vl.addComponents(hl1, hl2);
		return vl;
	}

	private Button getValidateModelButton() {
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
				sessionData.showInfoWindow("How to use EasyModel\r\n" + 
						"1. Define processes\r\n" + 
						"    Reaction definition: Substrates -> Products ; Modifiers\r\n" + 
						"    How to write: n1*A1+n2*A2+...->m1*B1+m2*B2+...;M1;M2;...\r\n" + 
						"    Legend: nX,mX: coefficient; AX,BX: species; MX: modifier\r\n" + 
						"2. Define model rates\r\n" + 
						"3. Select a rate for every reaction\r\n" + 
						"	Press \"Rate\" button\r\n" + 
						"4. Define initial conditions (Species button)\r\n" + 
						"5. Validate model\r\n" + 
						"6. Run Simulation", 600,400);
			}
		});
		return btn;
	}

	private Button getDescriptionHtmlButton() {
		Button btn = new Button();
		btn.setDescription("Render model description as HTML (be aware of malicious HTML code)");
		btn.setWidth("36px");
		btn.setStyleName("renderHtmlBtn");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			public void buttonClick(ClickEvent event) {
				DescriptionEditWindow window = new DescriptionEditWindow();
				UI.getCurrent().addWindow(window);
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
		Reaction newR = new Reaction();
		selectedModel.addReaction(newR);
		reactionsVL.addComponent(getReactionLayout(newR, true));
	}

	private HorizontalLayout getReactionLayout(Reaction react, boolean toFocus) {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(false);
		hl.setMargin(false);
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
		tf.setPlaceholder("Reaction");
		tf.setWidth("100%");
		tf.setData(react);
		tf.setValue(react.getReactionStr());
		tf.addValueChangeListener(getReactionTFValueChangeListener());
		tf.addBlurListener(getReactionTFBlurListener());
		tf.addShortcutListener(new ShortcutListener("reaction enter", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = -1923563074056157344L;

			@Override
			public void handleAction(Object sender, Object target) {
				if (reactionsTFList.isEmpty())
					return;
				TextField eventTF = (TextField) target;
				if (reactionsTFList.get(reactionsTFList.size() - 1) == eventTF) // last of the list
					addReaction();
				else if (reactionsTFList.indexOf(eventTF) != -1)
					reactionsTFList.get(reactionsTFList.indexOf(eventTF) + 1).focus();
			}
		});
		setTextFieldStyle(react.getReactionStr(), tf);
		reactionsTFList.add(tf);
		return tf;
	}

	private ValueChangeListener<String> getReactionTFValueChangeListener() {
		return new ValueChangeListener<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent<String> event) {
				// Change TextField style
				String newText = event.getValue();
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
		markRateButton(linkFormulaButtons.get(r), r, r.areAllFormulaParValuesValid());
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
		// tf.setId("idTF");
		tf.setWidth("50px");
		tf.setValue("R" + r.getIdJava());
		tf.setReadOnly(true);
		return tf;
	}

	private CustomLayout getDescriptionHtmlLayout() {
		return new CustomLayout();
//		try {
//			String html = VaadinUtils.sanitizeHTML(selectedModel.getDescription());
//			cl = new CustomLayout(new ByteArrayInputStream(html.getBytes()));
//		} catch (IOException e) {
//			e.printStackTrace();
//			cl = new CustomLayout();
//		}
//		return cl;
	}

	private VerticalLayout getInfoLayout() {
		VerticalLayout vlt = new VerticalLayout();
		vlt.addComponent(new Label("How to use " + SharedData.appName));
		vlt.addComponent(new Label("1. Define processes"));
		VerticalLayout vl1 = new VerticalLayout();
		vl1.setMargin(false);
		vl1.addComponents(new Label("Reaction definition: Substrates -> Products ; Modifiers"),
				new Label("How to write: n1*A1+n2*A2+...->m1*B1+m2*B2+...;M1;M2;..."),
				new Label("Legend: nX,mX: coefficient; AX,BX: species; MX: modifier"));
		vlt.addComponent(ToolboxVaadin.getIndentedVLLayout(vl1));
		vlt.addComponent(new Label("2. Define model rates"));
		vlt.addComponent(new Label("3. Select a rate for every reaction"));
		HorizontalLayout hl2 = new HorizontalLayout();
		hl2.setSpacing(true);
		hl2.setMargin(false);
		Button linkBtn = new Button();
		linkBtn.setWidth("50px");
		linkBtn.setStyleName("linkFn");
		hl2.addComponents(new Label("Press"), linkBtn);
		vlt.addComponent(ToolboxVaadin.getIndentedVLLayout(hl2));

		vlt.addComponent(new Label("4. Define initial conditions (Species button)"));
		vlt.addComponent(new Label("5. Validate model"));
		vlt.addComponent(new Label("6. Run Simulation"));
		return vlt;
	}

	private VerticalLayout getNameDescVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSizeFull();
		vl.setSpacing(false);
		vl.setMargin(false);
//		vl.setCaption("Name");
		HorizontalLayout hl = new HorizontalLayout();
		hl.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);
		hl.setWidth("100%");
		hl.setSpacing(false);
		hl.setMargin(false);
		
		nameTF = new TextField();
		nameTF.setValue(selectedModel.getName());
		nameTF.setPlaceholder("Name");
		nameTF.setWidth("100%");
		nameTF.addValueChangeListener(new ValueChangeListener<String>() {
			@Override
			public void valueChange(ValueChangeEvent<String> event) {
				selectedModel.setName(event.getValue());
			}
		});

		Button descBtn = new Button();
		descBtn.setCaption("Description");
		descBtn.setWidth("120px");
		//descBtn.setId("editDescription");
		descBtn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				DescriptionEditWindow window = new DescriptionEditWindow();
				UI.getCurrent().addWindow(window);
			}
		});
		
		hl.addComponents(nameTF, descBtn);
		hl.setExpandRatio(nameTF, 1f);
		vl.addComponent(hl);
		return vl;
	}

//	private Component getDescriptionHL() {
//		VerticalLayout hl = new VerticalLayout();
//		hl.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);
//		hl.setWidth("100%");
//		hl.setSpacing(true);
//		hl.setMargin(false);
//		Button editDescBtn = new Button();
//		editDescBtn.setCaption(selectedModel.getDescription());
//		editDescBtn.setWidth("100%");
//		editDescBtn.setId("editDescription");
//		editDescBtn.addClickListener(new ClickListener() {
//
//			@Override
//			public void buttonClick(ClickEvent event) {
//				DescriptionEditWindow window = new DescriptionEditWindow();
//				window.addCloseListener(new CloseListener() {
//					@Override
//					public void windowClose(CloseEvent e) {
//						event.getButton().setCaption(selectedModel.getDescription());
//					}
//				});
//				UI.getCurrent().addWindow(window);
//			}
//		});
//		Label btnLabel = new Label("Description");
//		btnLabel.setStyleName("labelCaption");
//		hl.addComponents(btnLabel, editDescBtn);
//		return hl;
//	}

	private void openLinkFormulaWindow(Reaction react, Button btn) {
		if (!react.isValid()) {
			Notification.show("Invalid reaction", Type.WARNING_MESSAGE);
			return;
		}
		if (!react.parse()) {
			Notification.show("Reaction cannot use Rate reserved words", Type.WARNING_MESSAGE);
			return;
		}
		Formulas compatibleFormulas = selectedModel.getFormulas().getFormulasCompatibleWithReaction(react);
		if (compatibleFormulas.isEmpty()) {
			Notification.show("No compatible rates found", Type.WARNING_MESSAGE);
			return;
		}
		// If all checks are ok show the window
		LinkReactionFormulaWindow rLinkWindow = new LinkReactionFormulaWindow(react, compatibleFormulas);
		rLinkWindow.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				markRateButton(btn, react, react.areAllFormulaParValuesValid());
			}
		});
		UI.getCurrent().addWindow(rLinkWindow);
	}

	private void markRateButton(Button btn, Reaction react, boolean done) {
		if (done) {
			btn.setStyleName("linkFnDone");
			btn.setDescription(react.getFormula().getNameToShow());
		} else {
			btn.setStyleName("linkFn");
			btn.setDescription("Select Kinetics");
		}
	}
}
