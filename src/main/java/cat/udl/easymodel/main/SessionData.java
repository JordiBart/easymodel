package cat.udl.easymodel.main;

import cat.udl.easymodel.controller.BioModelsLogs;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Models;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.logic.user.UserCookie;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.common.PendingNotification;
import cat.udl.easymodel.views.simulationresults.SimulationResultsView;
import com.vaadin.flow.server.VaadinService;

import jakarta.servlet.http.Cookie;

public class SessionData {
//    private UI ui = null;
    private VaadinService vaadinService = null;
    private BioModelsLogs bioModelsLogs = null;
    private Models models = null;
    private RepositoryType modelsRepo = null;
    private Model selectedModel;
    private User user;
    private UserCookie userCookie = null;
    private boolean isSimCancel = false;
    private boolean isWelcomeDialogClosed = false;
    private PendingNotification pendingNotification = null;
    private SimulationResultsView.SimulationResultsViewRefreshRunnable simulationResultsViewRunnable = null;
    private Thread simulationResultsViewThread = null;

    public SessionData() {
        // DO NOT DO ANYTHING HERE!!
        // DO IN INIT()!!
    }

    public void init() {
        vaadinService = VaadinService.getCurrent(); // within servlets, this is set to null
        models = new Models();
        loadUserFromCookies();
        selectedModel = new Model();
        selectedModel.initNewModel();
    }

    public void clear() {
        setUser(null);
        setRepository(null);
        setSelectedModel(null);
        models.resetModels();
        userCookie = null;
        pendingNotification = null;
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

    public void unsetUser() {
        setUser(null);
        userCookie = null;
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

    public BioModelsLogs getBioModelsLogs() {
        return bioModelsLogs;
    }

    public void setBioModelsLogs(BioModelsLogs bioModelsLogs) {
        this.bioModelsLogs = bioModelsLogs;
    }

///////////////////////////////////

    public VaadinService getVaadinService() {
        return vaadinService;
    }

    public boolean isSimCancel() {
        return isSimCancel;
    }

    public void setSimCancel(boolean isSimCancel) {
        this.isSimCancel = isSimCancel;
    }

    public boolean isWelcomeDialogClosed() {
        return isWelcomeDialogClosed;
    }

    public void setWelcomeDialogClosed(boolean welcomeDialogClosed) {
        isWelcomeDialogClosed = welcomeDialogClosed;
    }

    public PendingNotification getPendingNotification() {
        return pendingNotification;
    }

    public void setPendingNotification(PendingNotification pendingNotification) {
        this.pendingNotification = pendingNotification;
    }

    public void stopLastWebRefreshThreadAndSaveNewOne(SimulationResultsView.SimulationResultsViewRefreshRunnable simulationResultsViewRunnable) {
        //stop previous thread and save new one
        if (this.simulationResultsViewRunnable != null)
            this.simulationResultsViewRunnable.doStop();
        this.simulationResultsViewRunnable = simulationResultsViewRunnable;
    }

    public SimulationResultsView.SimulationResultsViewRefreshRunnable getSimulationResultsViewRunnable() {
        return simulationResultsViewRunnable;
    }

    public void setSimulationResultsViewRunnable(SimulationResultsView.SimulationResultsViewRefreshRunnable simulationResultsViewRunnable) {
        this.simulationResultsViewRunnable = simulationResultsViewRunnable;
    }

    public Thread getSimulationResultsViewThread() {
        return simulationResultsViewThread;
    }

    public void setSimulationResultsViewThread(Thread simulationResultsViewThread) {
        this.simulationResultsViewThread = simulationResultsViewThread;
    }
}
