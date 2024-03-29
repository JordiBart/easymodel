package cat.udl.easymodel.vcomponent.reaction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.MarginInfo;
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
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.app.AppPanel;
import cat.udl.easymodel.vcomponent.common.InfoWindowButton;
import cat.udl.easymodel.vcomponent.formula.FormulasEditorVL;
import cat.udl.easymodel.vcomponent.reaction.window.DescriptionEditWindow;
import cat.udl.easymodel.vcomponent.reaction.window.LinkReactionFormulaWindow;
import cat.udl.easymodel.vcomponent.reaction.window.SpeciesWindow;

public class ReactionEditorVL extends VerticalLayout {
	private static final long serialVersionUID = 1L;
	private SessionData sessionData;
	private Panel reactionsPanel;
	private VerticalLayout reactionsVL=new VerticalLayout();
	private ArrayList<TextField> reactionsTFList = new ArrayList<>();

	private Model selectedModel;
	private AppPanel mainPanel;
	private HashMap<Reaction, Button> linkFormulaButtons = new HashMap<>();

	private Button addReactionBtn = null;
	private TextField nameTF = null;
	private Button validateBtn=null;

	public ReactionEditorVL(Model selectedModel, AppPanel mainPanel) {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.selectedModel = selectedModel;
		this.mainPanel = mainPanel;

		addReactionBtn = getAddReactionButton();
		
		this.setWidth("100%");
		this.setHeight("100%");
		this.setMargin(true);
		this.setSpacing(true);

		reactionsVL.setWidth("100%");
		reactionsVL.setSpacing(false);
		reactionsVL.setMargin(true);
		updateReactionsVL();

		reactionsPanel = new Panel();
		reactionsPanel.setSizeFull();
		reactionsPanel.setContent(reactionsVL);
		reactionsPanel.setCaption("Reactions");

		Panel headerPanel = new Panel();
		headerPanel.setWidth("100%");
		headerPanel.setHeight("175px");
		headerPanel.setContent(getHeaderVL());
		headerPanel.setCaption("Model");
		
		VerticalLayout footerVL=getFooterVL();
		
		this.addComponents(headerPanel,reactionsPanel,footerVL);
		this.setExpandRatio(reactionsPanel, 1f);
		
		if (SharedData.getInstance().isDebug())
			validateBtn.focus();
		else if (nameTF.getValue().isEmpty())
			nameTF.focus();
	}

	private void updateReactionsVL() {
		reactionsVL.removeAllComponents();
		reactionsTFList.clear();
		Model mod = selectedModel;
		for (Reaction react : mod)
			reactionsVL.addComponent(getReactionRowHL(react, false));
//		reactionsVL.addComponent(addReactionBtn);
	}
	
	private VerticalLayout getHeaderVL() {
//		hl.setHeight("37px");
//		FileResource resource = new FileResource(
//				new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()
//						+ "/VAADIN/themes/easymodel/img/easymodel-logo-120.png"));
//		Image image = new Image(null, resource);
//		image.setHeight("36px");
//		head.addComponent(image);
//		VerticalLayout rightVL = new VerticalLayout();
//		rightVL.setMargin(new MarginInfo(true, true, false, false));
//		rightVL.setWidth("50px");
//		rightVL.setDefaultComponentAlignment(Alignment.TOP_LEFT);
		InfoWindowButton infoBtn = new InfoWindowButton("How to use "+SharedData.appName, "How to use EasyModel\r\n" + 
				"1. Define processes\r\n" + 
				"    Reaction definition: Substrates -> Products ; Modifiers\r\n" + 
				"    How to write: n1*A1+n2*A2+...->m1*B1+m2*B2+...;M1;M2;...\r\n" + 
				"    Legend: nX,mX: coefficient; AX,BX: species; MX: modifier\r\n" + 
				"2. Define model rates\r\n" + 
				"3. Select a rate for every reaction\r\n" + 
				"	Press the \"Rate\" button\r\n" + 
				"4. Define initial conditions (Species button)\r\n" + 
				"5. Validate model\r\n" + 
				"6. Run Simulation", 600,400);
		nameTF = new TextField();
		nameTF.setPlaceholder("Model Name");
		nameTF.setDescription("Model Name");
		nameTF.setValue(selectedModel.getName());
		nameTF.setWidth("100%");
		nameTF.addBlurListener(event-> {
			String newName = ((TextField)event.getComponent()).getValue();
			newName = newName.substring(0, Math.min(newName.length(), 300));
			selectedModel.setName(newName);
		});

		Button descBtn = new Button();
		descBtn.setCaption("Description");
		descBtn.setIcon(VaadinIcons.BOOK);
		descBtn.setWidth("150px");
		//descBtn.setId("editDescription");
		descBtn.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ToolboxVaadin.removeAllWindows();
				DescriptionEditWindow window = new DescriptionEditWindow();
				UI.getCurrent().addWindow(window);
			}
		});
		
		HorizontalLayout hl11 = new HorizontalLayout();
		hl11.setMargin(false);
		hl11.setSpacing(false);
		hl11.setWidth("100%");
		hl11.addComponents(nameTF,descBtn);
		hl11.setExpandRatio(nameTF, 1f);
		
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setMargin(false);
		hl1.setSpacing(true);
		hl1.setWidth("100%");
		hl1.addComponents(hl11,infoBtn);
		hl1.setExpandRatio(hl11, 1f);
		
		HorizontalLayout hl2 = new HorizontalLayout();
		hl2.setCaption("Model Options");
		hl2.setMargin(false);
		hl2.setSpacing(false);
		hl2.setWidth("100%");
		hl2.addComponents(addReactionBtn,getSpeciesButton());
		
		VerticalLayout spacer = new VerticalLayout();
		VerticalLayout vl = new VerticalLayout();
		vl.setMargin(true);
		vl.setSpacing(true);
		vl.setDefaultComponentAlignment(Alignment.TOP_CENTER);
		vl.setSizeFull();
		vl.addComponents(hl1,hl2,spacer);
		vl.setExpandRatio(spacer,1f);
		return vl;
	}

	private VerticalLayout getFooterVL() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(false);
		vl.setMargin(false);
		vl.setWidth("100%");

		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setSpacing(false);
		hl1.setMargin(false);
		hl1.setWidth("100%");
		validateBtn = getValidateModelButton();
		hl1.addComponent(validateBtn);
		vl.addComponents(hl1);
		return vl;
	}

	private Button getValidateModelButton() {
		Button btn = new Button("Validate Model");
		btn.setIcon(VaadinIcons.PLAY);
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

//	private Component getFormulaEditorButton() {
//		Button btn = new Button("Define Rates");
//		btn.setIcon(VaadinIcons.FUNCION);
//		btn.setDescription("Open Rates editor");
//		btn.setWidth("100%");
//		btn.addClickListener(new ClickListener() {
//			@Override
//			public void buttonClick(ClickEvent event) {
//				reactionsPanel.setContent(formulasEditorVL);
//				reactionsPanel.setScrollTop(0);
//			}
//		});
//		return btn;
//	}

	private Button getSpeciesButton() {
		Button btn = new Button("Species");
		btn.setIcon(VaadinIcons.PILL);
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

	private Button getAddReactionButton() {
		Button btn = new Button("Add Reaction");
		btn.setIcon(VaadinIcons.PLUS);
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
		
//		reactionsVL.removeComponent(addReactionBtn);
		reactionsVL.addComponent(getReactionRowHL(newR, true));
//		reactionsVL.addComponent(addReactionBtn);
		reactionsPanel.setScrollTop(900000000);
	}

	private HorizontalLayout getReactionRowHL(Reaction react, boolean toFocus) {
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

		if (toFocus) {
			reactTF.focus();
		}
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
					updateReactionsVL();
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
		tf.setWidth("60px");
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
			Notification.show("No compatible rates found.\nTip: go to \"Define Rates\" and create or import new rates.", Type.WARNING_MESSAGE);
			return;
		}
		// If all checks are ok show the window
		LinkReactionFormulaWindow rLinkWindow = new LinkReactionFormulaWindow(react, compatibleFormulas);
		rLinkWindow.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				refreshLinkFormulaButton(react);
			}
		});
		UI.getCurrent().addWindow(rLinkWindow);
	}

	public void refreshAllLinkFormulaButtons() {
		for (Reaction r : selectedModel) {
			refreshLinkFormulaButton(r);
		}
	}
	
	private void refreshLinkFormulaButton(Reaction r) {
		Button btn = linkFormulaButtons.get(r);
		if (r.isAllReactionDataFullfiled()) {
			btn.setStyleName("linkFnDone");
			btn.setDescription("Selected rate: "+r.getFormula().getNameToShow());
		} else {
			btn.setStyleName("linkFn");
			btn.setDescription("Select Rate");
		}
	}
}
