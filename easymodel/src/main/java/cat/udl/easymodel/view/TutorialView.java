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
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;

public class TutorialView extends CustomComponent implements View {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "tutorial";
	private HorizontalLayout slidesHL;
	private HorizontalLayout footerHL;
	private int curSlide = 1;
	private int totalSlide = 8;
	private HashMap<Integer, String> captionMap = new HashMap<>();

	private SharedData sharedData = SharedData.getInstance();
	private SessionData sessionData;

	public TutorialView() {
		this.sessionData = (SessionData) UI.getCurrent().getData();
		int i = 1;
		captionMap.put(i++, "");
		captionMap.put(i++, "Create a new model");
		captionMap.put(i++, "Define the model name and reactions");
		captionMap.put(i++, "Define the kinetic rates to be used in the model");
		captionMap.put(i++,
				"Select a kinetic rate for each reaction, set species configuration and validate the model");
		captionMap.put(i++, "Configure the simulation to be performed");
		captionMap.put(i++, "Get the simulation results");
		captionMap.put(i++, "");

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
		headerHL.setDefaultComponentAlignment(Alignment.TOP_CENTER);
		headerHL.setMargin(false);
		headerHL.setSpacing(false);
		FileResource resource = new FileResource(
				new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()
						+ "/VAADIN/themes/easymodel/img/easymodel-logo-120.png"));
		Image emLogo = new Image(null, resource);
		emLogo.setHeight("60px");
		headerHL.addComponents(emLogo, getSkipBtn());
		headerHL.setExpandRatio(emLogo, 1f);
		vl.addComponent(headerHL);

		slidesHL = new HorizontalLayout();
		slidesHL.setSizeFull();
		slidesHL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		slidesHL.setMargin(false);
		slidesHL.setSpacing(false);
		HorizontalLayout spacer = new HorizontalLayout();
		slidesHL.addComponents(getPrevBtn(), spacer, getNextBtn());
		slidesHL.setExpandRatio(spacer, 1f);
		vl.addComponent(slidesHL);

		footerHL = new HorizontalLayout();
		footerHL.setWidth("100%");
		footerHL.setHeight("30px");
		footerHL.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		footerHL.setMargin(false);
		footerHL.setSpacing(false);
		vl.addComponent(footerHL);

		vl.setExpandRatio(slidesHL, 1f);
		viewLayout.addComponents(vl);
		this.setSizeFull();
		this.setCompositionRoot(viewLayout);
		updateSlide();
	}

	private void skip() {
		getUI().getNavigator().removeView(TutorialView.NAME);
		getUI().getNavigator().addView(AppView.NAME, AppView.class);
		getUI().getNavigator().navigateTo(AppView.NAME);
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
			sessionData.clear();
			UI.getCurrent().getNavigator().removeView(TutorialView.NAME);
			UI.getCurrent().getNavigator().navigateTo(LoginView.NAME);
		}
	}

	private void next() {
		if (curSlide < totalSlide) {
			curSlide++;
			updateSlide();
		} else {
			UI.getCurrent().getNavigator().removeView(TutorialView.NAME);
			UI.getCurrent().getNavigator().addView(AppView.NAME, AppView.class);
			UI.getCurrent().getNavigator().navigateTo(AppView.NAME);
		}
	}

	private void updateSlide() {
		slidesHL.setStyleName("tutorialSlides tutorial" + curSlide);
		footerHL.removeAllComponents();
		Label lbl = new Label(captionMap.get(curSlide));
		lbl.setStyleName("bold");
		footerHL.addComponents(lbl);
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
