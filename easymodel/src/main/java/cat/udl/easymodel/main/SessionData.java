package cat.udl.easymodel.main;

import com.vaadin.server.VaadinService;
import com.vaadin.ui.UI;

import cat.udl.easymodel.controller.BioModelsLogs;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Models;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.mathlink.MathLinkOp;
import cat.udl.easymodel.thread.SimulationCancelThread;
import cat.udl.easymodel.thread.SimulationManagerThread;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.common.InfoWindow;
import cat.udl.easymodel.vcomponent.results.OutVL;
import cat.udl.easymodel.vcomponent.results.SimStatusHL;

public class SessionData {
	private UI ui = null;
	private VaadinService vaadinService = null;
	private BioModelsLogs bioModelsLogs = null;
	private Models models = null;
	private RepositoryType modelsRepo;
	private Model selectedModel;
	private User user;
	private InfoWindow infoWindow = null;
	private OutVL outVL = null;
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
	}

	public void clear() {
		setUser(null);
		setRepository(null);
		setSelectedModel(null);
		models.resetModels();
		outVL.reset();
		freeMathLinkOp();
		respawnSimulationManager();
	}

	public boolean isUserSet() {
		return user != null;
	}

	public Models getModels() {
		return models;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Model getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(Model selectedModel) {
		this.selectedModel = selectedModel;
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

	public UI getUi() {
		return ui;
	}
///////////////////////////////////
	public boolean loadMathLinkOpFromShared() {
		if (mathLinkOp != null && mathLinkOp.isLocked())
			return false;
		this.mathLinkOp = SharedData.getInstance().getMathLinkArray().getFreeMathLink();
		if (mathLinkOp != null)
			mathLinkOp.getCustomPacketListener().setSessionData(this);
		return (mathLinkOp != null);
	}

	public MathLinkOp getMathLinkOp() {
		return mathLinkOp;
	}

	public void freeMathLinkOp() {
		if (mathLinkOp != null) {
			mathLinkOp.resetMathLink();
			mathLinkOp = null;
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

	public void cancelSimulation() {
		if (isSimulating())
			new SimulationCancelThread(this.simulationManager).start();
	}
////////////////////////////////////
	public SimStatusHL getSimStatusHL() {
		return simStatusHL;
	}

	public void setSimStatusHL(SimStatusHL simStatusHL) {
		this.simStatusHL = simStatusHL;
	}

	public void showInfoWindow(String message, int w, int h) {
		if (!infoWindow.isAttached()) {
			infoWindow.updateContent(message, w, h);
			UI.getCurrent().addWindow(infoWindow);
			infoWindow.focus();
		}
	}

	public VaadinService getVaadinService() {
		return vaadinService;
	}
}
