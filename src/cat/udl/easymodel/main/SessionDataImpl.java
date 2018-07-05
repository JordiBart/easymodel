package cat.udl.easymodel.main;

import java.sql.SQLException;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaImpl;
import cat.udl.easymodel.logic.formula.Formulas;
import cat.udl.easymodel.logic.formula.FormulasImpl;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Models;
import cat.udl.easymodel.logic.model.ModelsImpl;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.mathlink.MathLinkOp;
import cat.udl.easymodel.mathlink.MathLinkOpImpl;
import cat.udl.easymodel.vcomponent.results.OutVL;

public class SessionDataImpl implements SessionData {
	private Models models = null;
	private Formulas predefinedFormulas;
	private Formulas customFormulas;
	private OutVL outVL = null;
	private RepositoryType modelsRepo;
	private Model selectedModel;
	private User user;
	private MathLinkOp mathLinkOp;

	public SessionDataImpl() {
		// DO NOT DO ANYTHING HERE!!
		// DO IN INIT()!!
	}

	@Override
	public void init() {
		predefinedFormulas = new FormulasImpl(FormulaType.PREDEFINED);
		customFormulas = new FormulasImpl(FormulaType.CUSTOM);
		loadPredefinedFormulas();
		models = new ModelsImpl();
		outVL = new OutVL();
		mathLinkOp = new MathLinkOpImpl(outVL);
	}

	@Override
	public Models getModels() {
		return models;
	}

	@Override
	public Formulas getPredefinedFormulas() {
		return predefinedFormulas;
	}

	@Override
	public void setPredefinedFormulas(Formulas predefinedFormulas) {
		this.predefinedFormulas = predefinedFormulas;
	}

	@Override
	public Formulas getCustomFormulas() {
		return customFormulas;
	}

	@Override
	public void setCustomFormulas(Formulas formulas) {
		this.customFormulas = formulas;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public Model getSelectedModel() {
		return selectedModel;
	}

	@Override
	public void setSelectedModel(Model selectedModel) {
		this.selectedModel = selectedModel;
	}

	@Override
	public RepositoryType getRepository() {
		return modelsRepo;
	}

	@Override
	public void setModelsRepo(RepositoryType modelsRepo) {
		this.modelsRepo = modelsRepo;
	}

	@Override
	public void loadPredefinedFormulas() {
		try {
			predefinedFormulas.loadDB();
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
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Formula getFormulaById(int id) {
		for (Formula f : getCustomFormulas())
			if (f.getId() != null && f.getId() == id)
				return f;
		for (Formula f : getPredefinedFormulas())
			if (f.getId() != null && f.getId() == id)
				return f;
		return null;
	}

	@Override
	public OutVL getOutVL() {
		return outVL;
	}

	@Override
	public MathLinkOp getMathLinkOp() {
		return mathLinkOp;
	}

	@Override
	public void reset() {
		setUser(null);
		setModelsRepo(null);
		setSelectedModel(null);
		models.resetModels();
		customFormulas.reset();
	}
}
