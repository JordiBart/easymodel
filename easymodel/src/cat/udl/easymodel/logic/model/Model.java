package cat.udl.easymodel.logic.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.vaadin.ui.GridLayout;
import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.Formulas;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.mathlink.MathLink;
import cat.udl.easymodel.utils.CException;

public interface Model extends List<Reaction> {

	public abstract int getIdJava();

	public abstract void setIdJava(int id);

	public abstract ArrayList<Reaction> getValidReactions();

	public abstract boolean removeReaction(Reaction react);

	public abstract void resetReactions();

	public void checkReactions() throws Exception;

	public abstract SortedMap<String, String> getAllSpeciesTimeDependent();
	
	public abstract SortedMap<String, String> getAllSpeciesConstant();
	
	public abstract SortedMap<String, String> getAllModifiers();
	
	public abstract SortedMap<String, Species> getAllSpecies();
	
	public abstract SortedMap<String, String> getAllSpeciesExceptModifiers();

	public abstract boolean isAllSpeciesSet();

	public abstract String getStoichiometricMatrix() throws Exception;

	public abstract GridLayout getDisplayStoichiometricMatrix() throws Exception;

	public abstract String getRegulatoryMatrix() throws Exception;

	public abstract GridLayout getDisplayRegulatoryMatrix() throws Exception;

	public abstract boolean addReaction(Reaction react);
	
	public abstract void removeFormula(Formula f);

	void checkIfReadyToSimulate() throws CException;

	SortedMap<String, String> getAllSubstrates();

	SortedMap<String, String> getAllProducts();

	void setDescription(String description);

	String getDescription();

	void setName(String name);

	String getName();
	
	String getNameShort();

	void checkValidModel() throws Exception;

	RepositoryType getRepositoryType();

	void setRepositoryType(RepositoryType repositoryType);

	User getUser();

	void setUser(User user);

	Integer getId();

	void setId(Integer idDb);

	void saveDB() throws SQLException;

	void loadDB() throws SQLException;

	void reset();

	ArrayList<String> getAllUsedFormulaStringsWithContext();

	SimConfig getSimConfig();

	Map<String,FormulaValue> getAllFormulaParameters();

	void deleteDB() throws SQLException;

	String getUserName();

	void checkMathExpressions(MathLink mathLinkOp) throws MathLinkException, CException;

	Map<String, SortedMap<String, FormulaArrayValue>> getAllFormulaSubstratesArrayValues();

	Map<String, SortedMap<String, FormulaArrayValue>> getAllFormulaModifiersArrayValues();

	boolean isStochastic();

	public abstract Formulas getAllUsedFormulas();

}
