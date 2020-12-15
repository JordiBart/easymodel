package cat.udl.easymodel.main;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.Cookie;

import com.vaadin.server.VaadinService;
import com.vaadin.ui.UI;
import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.controller.BioModelsLogs;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Models;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.logic.user.UserCookie;
import cat.udl.easymodel.mathlink.MathLinkOp;
import cat.udl.easymodel.thread.SimulationCancelThread;
import cat.udl.easymodel.thread.SimulationManagerThread;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.app.AppPanel;
import cat.udl.easymodel.vcomponent.common.InfoWindow;
import cat.udl.easymodel.vcomponent.results.OutVL;
import cat.udl.easymodel.vcomponent.results.SimStatusHL;

public class SessionData {
	private UI ui = null;
	private VaadinService vaadinService = null;
	private BioModelsLogs bioModelsLogs = null;
	private Models models = null;
	private RepositoryType modelsRepo = null;
	private Model selectedModel;
	private User user;
	private UserCookie userCookie = null;
	private InfoWindow infoWindow = null;
	private OutVL outVL = null;
	private AppPanel appPanel = null;
	private SimStatusHL simStatusHL = null;
	private MathLinkOp mathLinkOp = null;
	private SimulationManagerThread simulationManager = null;

	public SessionData(UI ui) {
		this.ui = ui;
		// DO NOT DO ANYTHING HERE!!
		// DO IN INIT()!!
	}

	public void init() {
		vaadinService = VaadinService.getCurrent(); // within servlets, this is set to null
		models = new Models();
		outVL = new OutVL(ui);
		simStatusHL = new SimStatusHL(this);
		infoWindow = new InfoWindow(this);
		respawnSimulationManager();
		loadUserFromCookies();
	}

	public void clear() {
		setUser(null);
		setRepository(null);
		setSelectedModel(null);
		models.resetModels();
		outVL.reset();
		closeMathLinkOp();
		respawnSimulationManager();
		userCookie = null;
	}

	public Models getModels() {
		return models;
	}

	public boolean isUserSet() {
		return user != null;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;

		if (user != null) {
			if (userCookie != null) {
				userCookie.resetCookieValues();
			} else {
				userCookie = new UserCookie(user);
				SharedData.getInstance().getUserCookies().add(userCookie);
			}
			userCookie.saveCookieInClient();
		} else {
			if (userCookie != null) {
				userCookie.clearCookieInClient();
				SharedData.getInstance().getUserCookies().remove(userCookie);
			}
		}
	}

	public void loadUserFromCookies() {
		Cookie clientUserCookie = ToolboxVaadin.getClientCookieByName("user");
		if (clientUserCookie != null && clientUserCookie.getValue() != null) {
			String token = clientUserCookie.getValue();
			for (UserCookie sharedDataUserCookie : SharedData.getInstance().getUserCookies()) {
				if (sharedDataUserCookie.getToken().equals(token)) {
					if (!sharedDataUserCookie.hasExpired()) {
						setUserCookie(sharedDataUserCookie);
						setUser(sharedDataUserCookie.getUser());
					}
					break;
				}
			}
		}
	}

	public UserCookie getUserCookie() {
		return userCookie;
	}

	public void setUserCookie(UserCookie userCookie) {
		this.userCookie = userCookie;
	}

	public Model getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(Model selectedModel) {
		this.selectedModel = selectedModel;
		closeMathLinkOp();
	}

	public RepositoryType getRepository() {
		return modelsRepo;
	}

	public void setRepository(RepositoryType modelsRepo) {
		this.modelsRepo = modelsRepo;
	}

//	public void loadPredefinedFormulas() throws SQLException {
//			predefinedFormulas.loadDB();
//			if (predefinedFormulas.size() == 0) {
//				Formula f = null;
//				f = new FormulaImpl("Power Laws",
//						"a*Product[X[[i]]^g[[i]],{i,1,Length[X]}]*Product[M[[i]]^g[[i+Length[X]]],{i,1,Length[M]}]",
//						FormulaType.PREDEFINED, null, RepositoryType.PUBLIC);
//				f.setTypeOfGenericParameter("a", FormulaValueType.CONSTANT);
//				predefinedFormulas.addFormula(f);
//				f = new FormulaImpl("Saturating Cooperative",
//						"(v*Product[X[[i]]^g[[i]],{i,1,Length[X]}]*Product[M[[i]]^g[[i+Length[X]]],{i,1,Length[M]}])/(Product[k[[i]]+X[[i]]^g[[i]],{i,1,Length[X]}]*Product[k[[i+Length[X]]]+M[[i]]^g[[i+Length[X]]],{i,1,Length[M]}])",
//						FormulaType.PREDEFINED, null, RepositoryType.PUBLIC);
//				f.setTypeOfGenericParameter("v", FormulaValueType.CONSTANT);
//				predefinedFormulas.addFormula(f);
//				f = new FormulaImpl("Saturating",
//						"(v*Product[X[[i]],{i,1,Length[X]}]*Product[M[[i]],{i,1,Length[M]}])/(Product[k[[i]]+X[[i]],{i,1,Length[X]}]*Product[k[[i+Length[X]]]+M[[i]],{i,1,Length[M]}])",
//						FormulaType.PREDEFINED, null, RepositoryType.PUBLIC);
//				f.setTypeOfGenericParameter("v", FormulaValueType.CONSTANT);
//				predefinedFormulas.addFormula(f);
//				f = new FormulaImpl("Mass action", "a*Product[X[[i]]^A[[i]],{i,1,Length[X]}]",
//						FormulaType.PREDEFINED, null, RepositoryType.PUBLIC);
//				f.setTypeOfGenericParameter("a", FormulaValueType.CONSTANT);
//				predefinedFormulas.addFormula(f);
//				// Special case formulas
//				f = new FormulaImpl("Henri-Michaelis menten", "(v*XF)/(k+XF)", FormulaType.PREDEFINED, null,
//						RepositoryType.PUBLIC);
//				f.setOneSubstrateOnly(true);
//				f.setTypeOfGenericParameter("v", FormulaValueType.CONSTANT);
//				f.setTypeOfGenericParameter("k", FormulaValueType.CONSTANT);
//				predefinedFormulas.addFormula(f);
//				f = new FormulaImpl("Hill Cooperativity", "(v*(XF^n))/(k^n+XF^n)", FormulaType.PREDEFINED, null,
//						RepositoryType.PUBLIC);
//				f.setOneSubstrateOnly(true);
//				f.setTypeOfGenericParameter("v", FormulaValueType.CONSTANT);
//				f.setTypeOfGenericParameter("k", FormulaValueType.CONSTANT);
//				f.setTypeOfGenericParameter("n", FormulaValueType.CONSTANT);
//				predefinedFormulas.addFormula(f);
//				f = new FormulaImpl("Catalytic activation", "(v*XF*MF)/((k+XF)*(k2+MF))", FormulaType.PREDEFINED, null,
//						RepositoryType.PUBLIC);
//				f.setOneSubstrateOnly(true);
//				f.setNoProducts(true);
//				f.setOneModifierOnly(true);
//				f.setTypeOfGenericParameter("v", FormulaValueType.CONSTANT);
//				f.setTypeOfGenericParameter("k", FormulaValueType.CONSTANT);
//				f.setTypeOfGenericParameter("k2", FormulaValueType.CONSTANT);
//				predefinedFormulas.addFormula(f);
//				f = new FormulaImpl("Competititve inhibition", "(v*XF*MF)/((k+XF)+(k+MF/k2))", FormulaType.PREDEFINED,
//						null, RepositoryType.PUBLIC);
//				f.setOneSubstrateOnly(true);
//				f.setNoProducts(true);
//				f.setOneModifierOnly(true);
//				f.setTypeOfGenericParameter("v", FormulaValueType.CONSTANT);
//				f.setTypeOfGenericParameter("k", FormulaValueType.CONSTANT);
//				f.setTypeOfGenericParameter("k2", FormulaValueType.CONSTANT);
//				predefinedFormulas.addFormula(f);
//
//				predefinedFormulas.saveDB();
//				System.out.println("Predefined formulas saved");
//			}
//	}

	public OutVL getOutVL() {
		return outVL;
	}

	public BioModelsLogs getBioModelsLogs() {
		return bioModelsLogs;
	}

	public void setBioModelsLogs(BioModelsLogs bioModelsLogs) {
		this.bioModelsLogs = bioModelsLogs;
	}

	public UI getUI() {
		return ui;
	}

///////////////////////////////////
	public MathLinkOp getMathLinkOp() {
		return mathLinkOp;
	}

	public boolean createMathLinkOp() {
		if (mathLinkOp != null)
			return false;
		try {
			MathLinkOp ml = new MathLinkOp();
			ml.openMathLink();
			ml.getCustomPacketListener().setSessionData(this);
			mathLinkOp = ml;
			SharedData.getInstance().getMathLinkArray().addMathLink(mathLinkOp);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void closeMathLinkOp() {
		if (mathLinkOp != null) {
			mathLinkOp.closeMathLink();
			mathLinkOp = null;
			SharedData.getInstance().getMathLinkArray().cleanMathLinks();
		}
	}

///////////////////////////////////
	public void respawnSimulationManager() {
		simulationManager = new SimulationManagerThread(this);
	}

	public boolean isSimulating() {
		return this.simulationManager.isAlive();
	}

	public void launchSimulation() {
		this.simulationManager.start();
	}

	public void cancelSimulationByUser() {
		if (isSimulating())
			new SimulationCancelThread(this.simulationManager).start();
	}

	public void cancelSimulationByCode() {
		if (isSimulating())
			this.simulationManager.interrupt();
	}

////////////////////////////////////
	public SimStatusHL getSimStatusHL() {
		return simStatusHL;
	}

	public void setSimStatusHL(SimStatusHL simStatusHL) {
		this.simStatusHL = simStatusHL;
	}

	public void showInfoWindow(String tittle, String message, int w, int h) {
		if (!infoWindow.isAttached()) {
			infoWindow.updateContent(tittle, message, w, h);
			UI.getCurrent().addWindow(infoWindow);
			infoWindow.focus();
		}
	}

	public VaadinService getVaadinService() {
		return vaadinService;
	}

	public AppPanel getAppPanel() {
		return appPanel;
	}

	public void setAppPanel(AppPanel appPanel) {
		this.appPanel = appPanel;
	}
}
