package cat.udl.easymodel.main;

import java.sql.SQLException;

import cat.udl.easymodel.controller.BioModelsLogs;
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
import cat.udl.easymodel.mathlink.MathLink;
import cat.udl.easymodel.mathlink.MathLinkImpl;
import cat.udl.easymodel.vcomponent.results.OutVL;

public class SessionData {
	private Models models = null;
	private Formulas predefinedFormulas;
	private Formulas customFormulas;
	private Formulas tempFormulas;
	private OutVL outVL = null;
	private RepositoryType modelsRepo;
	private Model selectedModel;
	private User user;
	private MathLink mathLinkOp;
	private BioModelsLogs bioModelsLogs=null;

	public SessionData() {
		// DO NOT DO ANYTHING HERE!!
		// DO IN INIT()!!
	}

	public void init() {
		predefinedFormulas = new FormulasImpl(FormulaType.PREDEFINED);
		customFormulas = new FormulasImpl(FormulaType.CUSTOM);
		setTempFormulas(new FormulasImpl(FormulaType.TEMP));
		loadPredefinedFormulas();
		models = new ModelsImpl();
		outVL = new OutVL();
		mathLinkOp = new MathLinkImpl(outVL);
	}

	public boolean isUserSet() {
		return user != null;
	}

	public Models getModels() {
		return models;
	}

	public Formulas getPredefinedFormulas() {
		return predefinedFormulas;
	}

	public void setPredefinedFormulas(Formulas predefinedFormulas) {
		this.predefinedFormulas = predefinedFormulas;
	}

	public Formulas getCustomFormulas() {
		return customFormulas;
	}

	public void setCustomFormulas(Formulas formulas) {
		this.customFormulas = formulas;
	}

	public Formulas getTempFormulas() {
		return tempFormulas;
	}

	public void setTempFormulas(Formulas tempFormulas) {
		this.tempFormulas = tempFormulas;
	}

	public Formulas getAllFormulas() {
		Formulas allFormulas = new FormulasImpl();
		for (Formula f : customFormulas)
			allFormulas.add(f);
		for (Formula f : predefinedFormulas)
			allFormulas.add(f);
		for (Formula f : tempFormulas)
			allFormulas.add(f);
		return allFormulas;
	}
	
	public Formulas getCustomAndTempFormulas() {
		Formulas allFormulas = new FormulasImpl();
		for (Formula f : customFormulas)
			allFormulas.add(f);
		for (Formula f : tempFormulas)
			allFormulas.add(f);
		return allFormulas;
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

	public void setModelsRepo(RepositoryType modelsRepo) {
		this.modelsRepo = modelsRepo;
	}

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

	public Formula getFormulaById(int id) {
		for (Formula f : getCustomFormulas())
			if (f.getId() != null && f.getId() == id)
				return f;
		for (Formula f : getPredefinedFormulas())
			if (f.getId() != null && f.getId() == id)
				return f;
		return null;
	}

	public OutVL getOutVL() {
		return outVL;
	}

	public MathLink getMathLinkOp() {
		return mathLinkOp;
	}

	public void reset() {
		setUser(null);
		setModelsRepo(null);
		setSelectedModel(null);
		models.resetModels();
		customFormulas.reset();
	}

	public BioModelsLogs getBioModelsLogs() {
		return bioModelsLogs;
	}

	public void setBioModelsLogs(BioModelsLogs bioModelsLogs) {
		this.bioModelsLogs = bioModelsLogs;
	}
}
