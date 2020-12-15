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
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
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
import cat.udl.easymodel.vcomponent.common.InfoWindowButton;
import cat.udl.easymodel.vcomponent.formula.FormulasEditorVL;
import cat.udl.easymodel.vcomponent.reaction.ReactionEditorVL;
import cat.udl.easymodel.vcomponent.reaction.window.DescriptionEditWindow;
import cat.udl.easymodel.vcomponent.reaction.window.LinkReactionFormulaWindow;
import cat.udl.easymodel.vcomponent.reaction.window.SpeciesWindow;

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
	private ReactionEditorVL reactionsEditorVL;
	private FormulasEditorVL formulasEditorVL;
	private Button validateBtn = null;
	private TextField nameTF = null;

	public ModelEditorVL(Model selectedModel, AppPanel mainPanel) {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.selectedModel = selectedModel;
		this.mainPanel = mainPanel;
		this.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		reactionsEditorVL = new ReactionEditorVL(selectedModel, mainPanel);
		formulasEditorVL = new FormulasEditorVL(reactionsEditorVL);

		this.setSizeFull();
		this.setMargin(false);
		this.setSpacing(false);
		
		HorizontalSplitPanel hSplitPanel = new HorizontalSplitPanel();
		hSplitPanel.setSizeFull();
		hSplitPanel.setFirstComponent(reactionsEditorVL);
		hSplitPanel.setSecondComponent(formulasEditorVL);
		this.addComponents(hSplitPanel);
	}
}
