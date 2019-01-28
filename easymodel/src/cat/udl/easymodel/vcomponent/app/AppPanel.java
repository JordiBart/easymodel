package cat.udl.easymodel.vcomponent.app;

import java.io.File;
import java.util.HashMap;

import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.controller.SimulationCtrl;
import cat.udl.easymodel.controller.SimulationCtrlImpl;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.types.WStatusType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.thread.SimulationThread;
import cat.udl.easymodel.utils.CException;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.model.ModelEditorVL;
import cat.udl.easymodel.vcomponent.model.window.ValidateModelWindow;
import cat.udl.easymodel.vcomponent.results.ResultsVL;
import cat.udl.easymodel.vcomponent.selectmodel.window.ImportSBMLWindow;
import cat.udl.easymodel.vcomponent.selectmodel.window.SelectModelRepositoryWindow;
import cat.udl.easymodel.vcomponent.selectmodel.window.SelectModelWindow;
import cat.udl.easymodel.vcomponent.simulation.SimulationVL;
import cat.udl.easymodel.view.AdminView;
import cat.udl.easymodel.view.AppView;
import cat.udl.easymodel.view.LoginView;

public class AppPanel extends Panel {
	private static final long serialVersionUID = 1L;

	private Panel conPanel = null;
	private AppIntroVL appIntroVL = new AppIntroVL(getSelectModelClickListener());
	private ModelEditorVL modelEditorVL = null;
	private SimulationVL simulationVL = null;
	private ResultsVL resultsVL = null;
	private HashMap<String, Button> stepButtons = new HashMap<>();
	private Button selectedButton;
	private AppPanel globalThis = this;

	private SessionData sessionData = null;
	private SharedData sharedData = SharedData.getInstance();

	private HorizontalLayout inputTitleHL = new HorizontalLayout();

	public AppPanel() {
		super();

		this.sessionData = (SessionData) UI.getCurrent().getData();
		resultsVL = new ResultsVL(globalThis);

		HorizontalLayout headerHL = new HorizontalLayout();
		headerHL.setMargin(false);
		headerHL.setSpacing(true);
		headerHL.setWidth("100%");
		headerHL.setHeight("35px");
		headerHL.setStyleName("panelHeader");
		headerHL.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		setInputTitle("Editor - Models");

		HorizontalLayout stepButtonsHL = new HorizontalLayout();
		stepButtonsHL.setMargin(false);
		stepButtonsHL.setSpacing(false);
		stepButtonsHL.setHeight("30px");
		stepButtonsHL.setWidth("100%");
		stepButtonsHL.setStyleName("panelButtons");
		// stepButtonsHL.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		stepButtons.put("Select Model", getSelectModelButton());
		stepButtons.put("Model", getModelButton());
		stepButtons.put("Simulation", getSimulationButton());
		stepButtons.put("Results", getResultsButton());
		stepButtonsHL.addComponent(getLogoImage());
		stepButtonsHL.addComponent(stepButtons.get("Select Model"));
		stepButtonsHL.addComponent(stepButtons.get("Model"));
		stepButtonsHL.addComponent(stepButtons.get("Simulation"));
		stepButtonsHL.addComponent(stepButtons.get("Results"));
		HorizontalLayout spacer = new HorizontalLayout();
		stepButtonsHL.addComponent(spacer);
		stepButtonsHL.setExpandRatio(spacer, 1);
		if (sessionData.getUser().getUserType() == UserType.ADMIN)
			stepButtonsHL.addComponent(getSwitchAdminAppButton());
		stepButtonsHL.addComponents(getUserButton(), getExitButton());

		// VerticalLayout conPanelVL = new VerticalLayout();
		// conPanelVL.setMargin(false);
		// conPanelVL.setSpacing(false);
		// conPanelVL.setSizeFull();

		conPanel = new Panel();
		conPanel.setSizeFull();
		conPanel.setStyleName("withoutborder");
		conPanel.setContent(appIntroVL);

		VerticalLayout mainPanelVL = new VerticalLayout();
		mainPanelVL.setMargin(false);
		mainPanelVL.setSpacing(false);
		mainPanelVL.setSizeFull();
		mainPanelVL.addComponents(stepButtonsHL, conPanel);
		mainPanelVL.setExpandRatio(conPanel, 1.0f);

		this.setId("mainPanel");
		this.setContent(mainPanelVL);
		this.setSizeFull();

		selectedButton = stepButtons.get("Select Model");
		updateStepButtonsStyle();
		SelectModelRepositoryWindow selectMRW = getSelectModelRepositoryWindow();
		UI.getCurrent().addWindow(selectMRW);
	}

	private Component getLogoImage() {
		FileResource resource = new FileResource(
				new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath()
						+ "/VAADIN/themes/easymodel/img/easymodel-logo-30.png"));
		Image image = new Image(null, resource);
		return image;
	}

	private SelectModelRepositoryWindow getSelectModelRepositoryWindow() {
		SelectModelRepositoryWindow selectMRW = new SelectModelRepositoryWindow();
		selectMRW.addCloseListener(new CloseListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void windowClose(CloseEvent e) {
				if (sessionData.getRepository() != null && (boolean) e.getWindow().getData()) {
					if (sessionData.getRepository() == RepositoryType.TEMP && sessionData.getSelectedModel() != null) {
						goToEditModel();
//						ImportSBMLWindow importSBMLW = new ImportSBMLWindow();
//						importSBMLW.addCloseListener(new CloseListener() {
//							private static final long serialVersionUID = 1L;
//
//							@Override
//							public void windowClose(CloseEvent e) {
//								if ((boolean) e.getWindow().getData())
//									goToEditModel();
//							}
//						});
//						UI.getCurrent().addWindow(importSBMLW);
					} else {
						SelectModelWindow smw = new SelectModelWindow();
						smw.addCloseListener(new CloseListener() {
							private static final long serialVersionUID = 1L;

							@Override
							public void windowClose(CloseEvent e) {
								if (sessionData.getSelectedModel() != null
										&& (WStatusType) e.getWindow().getData() == WStatusType.OK) {
									sessionData.getTempFormulas().reset();
									goToEditModel();
								} else if ((WStatusType) e.getWindow().getData() == WStatusType.BACK) {
									selectMRW.reset();
									UI.getCurrent().addWindow(selectMRW);
								}
							}
						});
						UI.getCurrent().addWindow(smw);
					}
				}
			}
		});
		return selectMRW;
	}

	private void goToEditModel() {
		modelEditorVL = new ModelEditorVL(sessionData.getSelectedModel(), globalThis);
		conPanel.setContent(modelEditorVL);

		selectedButton = stepButtons.get("Model");
		selectedButton.setEnabled(true);
		stepButtons.get("Simulation").setEnabled(false);
		stepButtons.get("Results").setEnabled(false);
		updateStepButtonsStyle();
	}

	private void updateStepButtonsStyle() {
		for (Button btn : stepButtons.values()) {
			if (btn == selectedButton) {
				btn.setStyleName("stepSelected");
			} else {
				btn.setStyleName("stepNotSelected");
			}
		}
	}

	private Button getUserButton() {
		Button btn = new Button();
		btn.setCaption(sessionData.getUser().getName());
		btn.setHeight("30px");
		btn.setStyleName("stepNotSelected");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
			}
		});
		return btn;
	}

	private Button getSelectModelButton() {
		Button btn = new Button();
		btn.setCaption("Select Model");
		btn.setHeight("30px");
		btn.addClickListener(getSelectModelClickListener());
		return btn;
	}

	private ClickListener getSelectModelClickListener() {
		return new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				// selectedButton = stepButtons.get("Select Model");
				// updateStepButtonsStyle();
				SelectModelRepositoryWindow selectMRW = getSelectModelRepositoryWindow();
				UI.getCurrent().addWindow(selectMRW);
			}
		};
	}

	private Button getModelButton() {
		Button btn = new Button();
		btn.setCaption("Model");
		btn.setEnabled(false);
		btn.setHeight("30px");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				selectedButton = stepButtons.get("Model");
				updateStepButtonsStyle();
				conPanel.setContent(modelEditorVL);
			}

		});
		return btn;
	}

	private Button getSimulationButton() {
		Button btn = new Button();
		btn.setCaption("Simulation");
		btn.setHeight("30px");
		btn.setEnabled(false);
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					sessionData.getSelectedModel().checkValidModel();
					// if pass then:
					changeToSimulation();
				} catch (Exception e) {
					validateModel();
				}
			}
		});
		return btn;
	}

	public void changeToSimulation() {
		selectedButton = stepButtons.get("Simulation");
		updateStepButtonsStyle();
		conPanel.setContent(simulationVL);
	}

	private Button getResultsButton() {
		Button btn = new Button();
		btn.setCaption("Results");
		btn.setHeight("30px");
		btn.setEnabled(false);
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				selectedButton = stepButtons.get("Results");
				updateStepButtonsStyle();
				conPanel.setContent(resultsVL);
			}
		});
		return btn;
	}

	private Button getSwitchAdminAppButton() {
		Button btn = new Button();
		btn.setCaption("Go Admin");
		btn.setHeight("30px");
		btn.setStyleName("stepNotSelected");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().getNavigator().removeView(AppView.NAME);
				UI.getCurrent().getNavigator().addView(AdminView.NAME, AdminView.class);
				UI.getCurrent().getNavigator().navigateTo(AdminView.NAME);
			}

		});
		return btn;
	}

	private Button getExitButton() {
		Button btn = new Button();
		btn.setStyleName("exitBtn");
		btn.setHeight("30px");
		btn.setWidth("30px");
		btn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				sessionData.reset();
				UI.getCurrent().getNavigator().removeView(AppView.NAME);
				UI.getCurrent().getNavigator().navigateTo(LoginView.NAME);
			}
		});
		return btn;
	}

	private void setInputTitle(String title) {
		inputTitleHL.removeAllComponents();
		inputTitleHL.addComponent(new Label(title));
		inputTitleHL.markAsDirty();
	}

	public void validateModel() {
		ValidateModelWindow vmw = new ValidateModelWindow(sessionData.getSelectedModel());
		vmw.addCloseListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				if ((Boolean) e.getWindow().getData()) {
					selectedButton = stepButtons.get("Simulation");
					selectedButton.setEnabled(true);
					updateStepButtonsStyle();
					simulationVL = new SimulationVL(sessionData.getSelectedModel(), globalThis);
					conPanel.setContent(simulationVL);
				} else {
					stepButtons.get("Simulation").setEnabled(false);
					stepButtons.get("Results").setEnabled(false);
				}
			}
		});
		UI.getCurrent().addWindow(vmw);
	}

	public void showResults() {
		SimulationThread thread = new SimulationThread(sessionData);
		try {
			thread.start();
			thread.join(SharedData.simulationTimeoutMinutes * 60 * 1000); // ms
			if (thread.getInterruptReason() != null) {
				Notification.show(thread.getInterruptReason(), Type.WARNING_MESSAGE);
				sessionData.getOutVL().reset();
			} else if (thread.isAlive()) {
				thread.interrupt();
				Notification.show(
						"Simulation timeout of " + SharedData.simulationTimeoutMinutes + " minutes was reached",
						Type.WARNING_MESSAGE);
//				sessionData.getOutVL().reset();
			} else {
				// simulation finished normally
				selectedButton = stepButtons.get("Results");
				selectedButton.setEnabled(true);
				updateStepButtonsStyle();
				conPanel.setContent(resultsVL);
			}
		} catch (InterruptedException e) {
			// uncontrolled interruption
			e.printStackTrace();
		}
	}
}
