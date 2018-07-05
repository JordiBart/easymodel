package cat.udl.easymodel.logic.model;

import java.sql.SQLException;
import java.util.List;

import cat.udl.easymodel.logic.formula.Formula;

public interface Models extends List<Model> {

	public abstract Model getModelByIdJava(int id);

	public abstract boolean addModel(Model mod);

	public abstract boolean removeModel(Model mod);

	public abstract void resetModels();
	
	public abstract void removeFormulaFromReactions(Formula f);

	Model getModelByName(String name);

	void semiLoadDB() throws SQLException;

	void reset();

//	public abstract void syncAutoIncrement();

}