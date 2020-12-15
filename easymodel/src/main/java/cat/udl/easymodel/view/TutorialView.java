package cat.udl.easymodel.view;

import java.io.File;
import java.util.HashMap;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.ui.MainUI;
import cat.udl.easymodel.ui.TutorialUI;
import cat.udl.easymodel.utils.ToolboxVaadin;

public class TutorialView extends CustomComponent implements View {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "tutorial";
	private HorizontalLayout slidesHL;
	private HorizontalLayout titleHL;
	private HorizontalLayout centerHL;
	private HorizontalLayout progressHL;
	private int curSlide = 1;
	private int totalSlide = 9;
	private HashMap<Integer, String> titleMap = new HashMap<>();

	private SharedData sharedData = SharedData.getInstance();
	private SessionData sessionData;

	public TutorialView() {
		this.sessionData = (SessionData) UI.getCurrent().getData();
		int i = 1;
		titleMap.put(i++, "");
		titleMap.put(i++, "Create or select a model");
		titleMap.put(i++, "Define the model reactions and rates I");
		titleMap.put(i++, "Define the model reactions and rates II");
		titleMap.put(i++, "Configure Deterministic Simulation I");
		titleMap.put(i++, "Configure Deterministic Simulation II");
		titleMap.put(i++, "Configure Stochastic Simulation");
		titleMap.put(i++, "Simulation results");
		titleMap.put(i++, "");

		addShortcuts();

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setStyleName("tutorialView");
		viewLayout.setSizeFull();
		viewLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		viewLayout.setMargin(false);
		viewLayout.setSpacing(false);

		VerticalLayout vl = new VerticalLayout();
		vl.setStyleName("tutorialMain");
		vl.setSizeFull();
		vl.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		vl.setMargin(false);
		vl.setSpacing(false);

		HorizontalLayout headerHL = new HorizontalLayout();
		headerHL.setWidth("100%");
		headerHL.setHeight("60px");
		headerHL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		headerHL.setMargin(false);
		headerHL.setSpacing(false);
		FileResource resource = new FileResource(
				new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()
						+ "/VAADIN/themes/easymodel/img/easymodel-logo-120.png"));
		Image emLogo = new Image(null, resource);
		emLogo.setHeight("60px");
		emLogo.setStyleName("tutorialLogo");
		titleHL = new HorizontalLayout();
		titleHL.setSizeFull();
		titleHL.setMargin(false);
		titleHL.setSpacing(false);
		titleHL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		headerHL.addComponents(emLogo, titleHL, getSkipBtn());
		headerHL.setExpandRatio(titleHL, 1f);
		vl.addComponent(headerHL);

		slidesHL = new HorizontalLayout();
		slidesHL.setSizeFull();
		slidesHL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		slidesHL.setMargin(false);
		slidesHL.setSpacing(false);
		centerHL = ToolboxVaadin.getRawHL(null);
		slidesHL.addComponents(getPrevBtn(), centerHL, getNextBtn());
		slidesHL.setExpandRatio(centerHL, 1f);
		vl.addComponent(slidesHL);

		HorizontalLayout footerHL = new HorizontalLayout();
		footerHL.setWidth("100%");
		footerHL.setHeight("5px");
		footerHL.setStyleName("tutorialFooter");
		footerHL.setMargin(false);
		footerHL.setSpacing(false);
		progressHL = new HorizontalLayout();
		progressHL.setSpacing(false);
		progressHL.setMargin(false);
		progressHL.setStyleName("tutorialProgress");
		progressHL.setWidth("0%");
		progressHL.setHeight("100%");
		footerHL.addComponents(progressHL);
		vl.addComponent(footerHL);

		vl.setExpandRatio(slidesHL, 1f);
		viewLayout.addComponents(vl);
		this.setSizeFull();
		this.setCompositionRoot(viewLayout);
		updateSlide();
	}

	private void skip() {
		if (getUI() instanceof MainUI) {
			getUI().getNavigator().navigateTo(AppView.NAME);
		} else
			showSwitchBackToApp();
	}
	
	private void showSwitchBackToApp() {
		Notification.show("Please switch back to the "+SharedData.appName+" window", Type.WARNING_MESSAGE);
	}
	
	private Component getSkipBtn() {
		Button btn = new Button("SKIP");
		btn.setStyleName("tutorialSkip");
		btn.setWidth("100px");
		btn.setHeight("100%");
		btn.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				skip();
			}
		});
		return btn;
	}

	private Component getNextBtn() {
		Button btn = new Button("&#10095;");
		btn.setCaptionAsHtml(true);
		btn.setStyleName("tutorialNext");
		btn.setWidth("100px");
		btn.setHeight("100%");
		btn.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				next();
			}
		});
		return btn;
	}

	private Component getPrevBtn() {
		Button btn = new Button("&#10094;");
		btn.setCaptionAsHtml(true);
		btn.setStyleName("tutorialPrev");
		btn.setWidth("100px");
		btn.setHeight("100%");//
		btn.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				prev();
			}
		});
		return btn;
	}

	private void prev() {
		if (curSlide > 1) {
			curSlide--;
			updateSlide();
		} else {
			if (getUI() instanceof MainUI) {
				sessionData.clear();
				UI.getCurrent().getNavigator().navigateTo(CoverView.NAME);
			} else
				showSwitchBackToApp();
		}
	}

	private void next() {
		if (curSlide < totalSlide) {
			curSlide++;
			updateSlide();
		} else {
			if (getUI() instanceof MainUI) {
				UI.getCurrent().getNavigator().navigateTo(AppView.NAME);
			} else
				showSwitchBackToApp();
		}
	}

	private void updateSlide() {
		titleHL.removeAllComponents();
		Label titleLabel = new Label(titleMap.get(curSlide));
		titleLabel.setStyleName("tutorialTitle");
		titleHL.addComponent(titleLabel);

		centerHL.removeAllComponents();
		Label numSlideLabel = new Label(String.valueOf(curSlide));
		numSlideLabel.setStyleName("tutorialNumSlide");
		centerHL.addComponent(numSlideLabel);

		slidesHL.setStyleName("tutorialSlides tutorial" + curSlide);
		progressHL.setWidth(String.format("%.0f", ((double) curSlide / totalSlide) * 100) + "%");
	}

	private void addShortcuts() {
		this.addShortcutListener(new ShortcutListener("esc", ShortcutAction.KeyCode.ESCAPE, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				skip();
			}
		});
		this.addShortcutListener(new ShortcutListener("prev", ShortcutAction.KeyCode.ARROW_LEFT, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				prev();
			}
		});
		this.addShortcutListener(new ShortcutListener("next", ShortcutAction.KeyCode.ARROW_RIGHT, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				next();
			}
		});
		this.addShortcutListener(new ShortcutListener("space", ShortcutAction.KeyCode.SPACEBAR, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				next();
			}
		});
		if (sharedData.isDebug())
			this.addShortcutListener(new ShortcutListener("enter", ShortcutAction.KeyCode.ENTER, null) {
				@Override
				public void handleAction(Object sender, Object target) {
					skip();
				}
			});
	}
}
