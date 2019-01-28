package cat.udl.easymodel.logic.formula;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.user.User;

public interface Formula {

	public abstract boolean parse();

	public abstract int getIdJava();

	public abstract void setIdJava(int id);

	public abstract String getFormulaDef();

	public abstract void setFormulaDef(String formula);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract boolean isDeletable();

	public abstract void setDeletable(boolean deletable);

	public abstract boolean isBlank();

	public abstract Map<String, FormulaValueType> getGenericParameters();

	public abstract List<String> getParametersBySubsAndModif();

	public abstract boolean isCompatibleWithReaction(Reaction r);

	public abstract List<String> getKeyWords();

	public abstract boolean isOneSubstrateOnly();

	public abstract void setOneSubstrateOnly(boolean oneReactiveOnly);

	public abstract boolean isNoProducts();

	public abstract void setNoProducts(boolean noProductives);

	public abstract boolean isOneModifierOnly();

	public abstract void setOneModifierOnly(boolean oneModifierOnly);

	public abstract String getIdJavaStr();

	public abstract String getNameToShow();

	boolean isValid();

	Integer getId();

	void setId(Integer id);

	FormulaType getFormulaType();

	void setFormulaType(FormulaType formulaType);

	User getUser();

	void setUser(User user);

	RepositoryType getRepositoryType();

	void setRepositoryType(RepositoryType repositoryType);

	void setTypeOfGenericParameter(String genPar, FormulaValueType fvt);

	String getMathematicaReadyFormula(String reactionContext, Model m);
	
	void clear();

	void saveDB() throws SQLException;

	boolean isDirty();

	void setDirty(boolean isDirty);
}
