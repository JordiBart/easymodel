package cat.udl.easymodel.main;

import java.util.ArrayList;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.Formulas;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Models;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.mathlink.MathLinkOp;
import cat.udl.easymodel.vcomponent.results.OutVL;

public interface SessionData {

	Models getModels();

	Formulas getPredefinedFormulas();

	void setPredefinedFormulas(Formulas predefinedFormulas);

	Formulas getCustomFormulas();

	void setCustomFormulas(Formulas formulas);

	Model getSelectedModel();

	void setSelectedModel(Model selectedModel);

	RepositoryType getRepository();

	void setModelsRepo(RepositoryType modelsRepo);

	void setUser(User user);

	User getUser();

	void loadPredefinedFormulas();

	OutVL getOutVL();

	MathLinkOp getMathLinkOp();

	Formula getFormulaById(int id);

	void init();

	void reset();
}