package cat.udl.easymodel.logic.formula;

import java.sql.SQLException;
import java.util.List;

import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.types.FormulaType;

public interface Formulas extends List<Formula> {

	public abstract boolean addFormula(Formula f);

	public abstract boolean removeFormula(Formula f);
	
	public abstract boolean hasAnyCompatibleFormula(Reaction r);

	public abstract Formula getFormulaByName(String fName);

	void saveDB() throws SQLException;

	void loadDB() throws SQLException;

	FormulaType getFormulaType();

	void setFormulaType(FormulaType formulaType);

	Formula getFormulaById(int id);

	String getNextFormulaNameByModelShortName(String modelShortName);

	void reset();
}