package cat.udl.easymodel.logic.formula;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.vaadin.ui.UI;

import cat.udl.easymodel.controller.ContextUtils;
import cat.udl.easymodel.logic.model.FormulaArrayValue;
import cat.udl.easymodel.logic.model.FormulaValue;
import cat.udl.easymodel.logic.model.FormulaValueImpl;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.model.Species;
import cat.udl.easymodel.logic.types.FormulaElemType;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.types.SpeciesVarTypeType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.Utils;

public class Formula {

	private Integer id;
	private int idJava;
	private String name;
	private String formula;
	private FormulaType formulaType;
	private Model model;
	private Map<String, FormulaValueType> genParams;

	// DB related
	private boolean isDBDelete;
	private boolean isDirty;

	// Restriction
	private boolean oneSubstrateOnly;
	private boolean noProducts;
	private boolean oneModifierOnly;

	// calculated data after parse
	private String loadedFormula;
	private List<String> keyWords;
	private ArrayList<FormulaElem> formulaElements;
	private List<String> paramsBySubsAndModif;

	public Formula(String fName, String fExpression, FormulaType formulaType, Model model) {
		reset();
		setName(fName);
		setFormulaDef(fExpression);
		setFormulaType(formulaType);
		setModel(model);
	}

	public Formula(Formula from) {
		reset();
		this.id = from.getId();
		this.idJava = from.getIdJava();
		this.name = from.getNameRaw();
		this.setFormulaType(from.getFormulaType());
		this.setModel(from.getModel());
		this.setFormulaDef(from.getFormulaDef()); //parse
		this.oneSubstrateOnly = from.isOneSubstrateOnly();
		this.noProducts = from.isNoProducts();
		this.oneModifierOnly = from.isOneModifierOnly();
		this.genParams.putAll(from.getGenericParameters());
		this.isDBDelete = from.isDBDelete();
		this.setDirty(from.isDirty());
	}

	public void reset() {
		id = null;
		idJava = -1;
		name = "";
		loadedFormula = "";
		formula = "";
		model = null;
		keyWords = new ArrayList<>();
		genParams = new LinkedHashMap<>();
		formulaElements = new ArrayList<>();
		paramsBySubsAndModif = new ArrayList<>();
		formulaType = null;
		isDBDelete = false;
		isDirty = true;
		oneSubstrateOnly = false;
		noProducts = false;
		oneModifierOnly = false;
	}

	public boolean isBlank() {
		return formula.isEmpty();
	}

	public boolean isValid() {
		return parse();
	}

	public boolean parse() {
		if (!FormulaUtils.isBlank(formula) && formula.equals(loadedFormula))
			return true;
		if (!FormulaUtils.isValid(formula)) {
			loadedFormula = "";
			return false;
		}
//		MathLinkOp mlo = ((SessionData) UI.getCurrent().getData()).getMathLinkOp();
//		try {
//			mlo.openMathLink();
//			if (mlo.evaluateToString(formula).equals("$Failed"))
//				throw new Exception("parser error");
//			mlo.closeMathLink();
//		} catch (Exception e) {
//			loadedFormula = "";
//			return false;
//		}

		// get constants
		formulaElements.clear();
		genParams.clear();
		paramsBySubsAndModif.clear();
		resetRestrictions();
		String part = "";
		String currentChar = null;
		for (int i = 0; i <= formula.length(); i++) {
			if (i < formula.length())
				currentChar = String.valueOf(formula.charAt(i));
			if (!currentChar.matches("\\w|:|\\.") || i == formula.length()) {
				if (!part.isEmpty()) {
					if (!part.matches(FormulaUtils.getInstance().getToNotParseRegEx())) {
						// var names
						if (!formula.substring(i).matches("\\[\\[.+\\]\\].*")) {
							genParams.put(part, null);
							formulaElements.add(new FormulaElem(FormulaElemType.GENPARAM, part));
						} else if (!paramsBySubsAndModif.contains(part)) {
							paramsBySubsAndModif.add(part);
							formulaElements.add(new FormulaElem(FormulaElemType.PARAMBYSUBSANDMODS, part));
						}
					} else {
						if (part.matches(FormulaUtils.realNumberRegex))
							formulaElements.add(new FormulaElem(FormulaElemType.NUMBER, part));
						// restrictions
						else if (part.matches("XF")) {
							setOneSubstrateOnly(true);
						} else if (part.matches("MF")) {
							setOneModifierOnly(true);
						}
						if (!keyWords.contains(part)) {
							keyWords.add(part);
							// System.out.println(part);
						}
					}
				}
				part = "";
				if (currentChar.matches("[\\+\\-\\*\\/^\\(\\)]") && i != formula.length())
					formulaElements.add(new FormulaElem(FormulaElemType.OPERATOR, currentChar.toString()));
			} else
				part += currentChar;
		}
		loadedFormula = formula;
		setDirty(true);
		return true;
	}

	public ArrayList<FormulaElem> getFormulaElements() {
		return formulaElements;
	}

	public boolean isEquivalentTo(Formula f2) {
		if (getFormulaElements().size() != f2.getFormulaElements().size())
			return false;
		for (int i = 0; i < getFormulaElements().size(); i++) {
			if (getFormulaElements().get(i).getFormulaElemType() != f2.getFormulaElements().get(i).getFormulaElemType())
				return false;
		}
		return true;
	}

	public String getFormulaElementsString() {
		String res = "Elements: ";
		for (FormulaElem fe : getFormulaElements()) {
			res += fe.getFormulaElemType() + ":" + fe.getValue() + " ";
		}
		return res;
	}

	public String getGenericFormulaDef() {
		LinkedHashMap<String, String> genParamsMap = new LinkedHashMap<>();
		int numVars = 0, numKVars = 0, numExpVars = 0;
		String newParName;
		String lastOperator = null;
		for (FormulaElem fe : getFormulaElements()) {
			if (fe.getFormulaElemType() == FormulaElemType.OPERATOR) {
				lastOperator = fe.getValue();
			} else if (fe.getFormulaElemType() == FormulaElemType.GENPARAM && genParamsMap.get(fe.getValue()) == null) {
				if ("^".equals(lastOperator)) {
					numExpVars++;
					newParName = "g" + numExpVars;
				} else if (fe.getValue().startsWith("k") || fe.getValue().startsWith("K")) {
					numKVars++;
					newParName = "k" + numKVars;
				} else {
					numVars++;
					newParName = "x" + numVars;
				}
				genParamsMap.put(fe.getValue(), newParName);
			}
		}
		String newFormulaDef = "";
		for (FormulaElem fe : getFormulaElements()) {
			if (fe.getFormulaElemType() == FormulaElemType.GENPARAM) {
				newFormulaDef += genParamsMap.get(fe.getValue());
			} else
				newFormulaDef += fe.getValue();
		}
		return newFormulaDef;
	}

	public int getIdJava() {
		return idJava;
	}

	public String getIdJavaStr() {
		return "F" + idJava;
	}

	public void setIdJava(int id) {
		this.idJava = id;
	}

	public String getFormulaDef() {
		return formula;
	}

	public void setFormulaDef(String formula) {
		this.formula = formula;
		this.parse();
	}

	public Map<String, FormulaValueType> getGenericParameters() {
		return genParams;
	}

	public void setTypeOfGenericParameter(String genPar, FormulaValueType fvt) {
		if (genParams.containsKey(genPar)) {
			genParams.put(genPar, fvt);
			setDirty(true);
		}
	}

	public String getNameToShow() {
		if (name.equals(""))
			return getIdJavaStr();
		return name;
	}

	public String getNameRaw() {
		return name;
	}

	public void setName(String name) {
		setDirty(true);
		this.name = name;
	}

	public boolean isDBDelete() {
		return isDBDelete;
	}

	public void setDBDelete(boolean isDBDelete) {
		setDirty(true);
		this.isDBDelete = isDBDelete;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public List<String> getParametersBySubsAndModif() {
		return paramsBySubsAndModif;
	}

	// Restriction

	public boolean isCompatibleWithReaction(Reaction r) {
		if (isOneSubstrateOnly() && r.getLeftPartSpecies().size() != 1)
			return false;
		if (isNoProducts() && r.getRightPartSpecies().size() != 0)
			return false;
		if (isOneModifierOnly() && r.getModifiers().size() != 1)
			return false;
		// check number of species between formula and reaction
		// a formula must use all the reaction substrates and modifiers to be compatible
//		if (this.getParametersBySubsAndModif().isEmpty()) {
//			int totalReactionSubstAndModToCheck = r.getLeftPartSpecies().size() + r.getModifiers().size();
//			// substrates
//			if (this.getKeyWords().contains("X") || this.getKeyWords().contains("A"))
//				totalReactionSubstAndModToCheck -= r.getLeftPartSpecies().size();
//			else if (this.getKeyWords().contains("XF"))
//				totalReactionSubstAndModToCheck--;
//			// modifiers
//			if (this.getKeyWords().contains("M"))
//				totalReactionSubstAndModToCheck -= r.getModifiers().size();
//			else if (this.getKeyWords().contains("MF"))
//				totalReactionSubstAndModToCheck--;
//			// check
//			if (totalReactionSubstAndModToCheck > this.getGenericParameters().size())
//				return false;
//		}
		return true;
	}

	private void resetRestrictions() {
		setOneSubstrateOnly(false);
		setNoProducts(false);
		setOneModifierOnly(false);
	}

	public List<String> getKeyWords() {
		return keyWords;
	}

	public boolean isOneSubstrateOnly() {
		return oneSubstrateOnly;
	}

	public void setOneSubstrateOnly(boolean oneReactiveOnly) {
		setDirty(true);
		this.oneSubstrateOnly = oneReactiveOnly;
	}

	public boolean isNoProducts() {
		return noProducts;
	}

	public void setNoProducts(boolean noProducts) {
		setDirty(true);
		this.noProducts = noProducts;
	}

	public boolean isOneModifierOnly() {
		return oneModifierOnly;
	}

	public void setOneModifierOnly(boolean oneModifierOnly) {
		setDirty(true);
		this.oneModifierOnly = oneModifierOnly;
	}

	public String getMathematicaReadyFormula(String reactionContext, Model m) {
		String newStr = "";
		String part = "";
		String currentChar = null;
		for (int i = 0; i <= formula.length(); i++) {
			if (i < formula.length())
				currentChar = String.valueOf(formula.charAt(i));
			if (!currentChar.matches("\\w|:") || i == formula.length()) {
				if (!part.isEmpty()) {
					if (part.matches(FormulaUtils.getInstance().getGeneralVarsRegEx())) // time
						newStr += ContextUtils.generalContext
								+ part.replaceAll(FormulaUtils.mathematicaBuiltInPrefix, ContextUtils.generalContext);
					else if (part.matches(FormulaUtils.getInstance().getMathematicaSymbolRegex()))
						newStr += ContextUtils.systemContext
								+ part.replaceAll(FormulaUtils.mathematicaSymbolPrefix, "");
					else if (part.matches(FormulaUtils.getInstance().getMathematicaIndexRegex()))
						newStr += ContextUtils.indexContext + part.replaceAll(FormulaUtils.mathematicaIndexPrefix, "");
					else if (!part.matches(FormulaUtils.getInstance().getToNotParseRegEx())
							|| part.matches(FormulaUtils.getInstance().getBuiltInVarsRegEx()))
						newStr += reactionContext
								+ part.replaceAll(FormulaUtils.mathematicaBuiltInPrefix, ContextUtils.builtInContext);
					else {
						newStr += part;
//						System.out.println("raw part "+part);
					}
				}
				part = "";
				if (i < formula.length())
					newStr += currentChar;
			} else
				part += currentChar;
		}
//		System.out.println(newStr);
		return newStr;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		setDirty(true);
		this.id = id;
	}

	public FormulaType getFormulaType() {
		return formulaType;
	}

	public void setFormulaType(FormulaType formulaType) {
		setDirty(true);
		this.formulaType = formulaType;
	}

	public void loadDB() throws SQLException {
		if (this.id == null)
			throw new SQLException("Can't load formula: id=null");
		Integer idToLoad = this.id;
		this.reset();
		setId(idToLoad);
		SessionData sessionData = (SessionData) UI.getCurrent().getData();
		SharedData sharedData = SharedData.getInstance();
		Connection con = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement;
		ResultSet rs;
		try {
			// model table
			String query = "SELECT f.id, f.id_model, f.name, f.formula, f.onesubstrateonly, f.noproducts, f.onemodifieronly, f.formulatype FROM formula f WHERE f.id=?";
			preparedStatement = con.prepareStatement(query);
			int p = 1;
			preparedStatement.setInt(p++, this.id);
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				this.setName(rs.getString("name"));
				this.setModel(sessionData.getModels().getModelById(rs.getInt("id_model")));
				this.setFormulaDef(rs.getString("formula"));
				this.setFormulaType(FormulaType.fromInt(rs.getInt("formulatype")));
				this.setOneSubstrateOnly(Utils.intToBool(rs.getInt("onesubstrateonly")));
				this.setNoProducts(Utils.intToBool(rs.getInt("noproducts")));
				this.setOneModifierOnly(Utils.intToBool(rs.getInt("onemodifieronly")));
			}
			rs.close();
			preparedStatement.close();
			preparedStatement = con.prepareStatement(
					"SELECT `id`, `id_formula`, `genparam`, `formulavaluetype` FROM `formulagenparam` WHERE id_formula=?");
			p = 1;
			preparedStatement.setInt(p++, this.id);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				this.getGenericParameters().put(rs.getString("genparam"),FormulaValueType.fromInt(rs.getInt("formulavaluetype")));
			}
			rs.close();
			preparedStatement.close();
			setDirty(false);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}
	
	public void saveDB() throws SQLException {
		Formula f = this;
		SharedData sharedData = SharedData.getInstance();
		Connection con = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement;
		int p;
		try {
			if (!f.isValid())
				throw new SQLException("Invalid formula " + f.getFormulaDef());
			// UPDATE/INSERT
			p = 1;
			if (f.getId() != null) {
				preparedStatement = con.prepareStatement(
						"UPDATE formula SET `name`=?, `formula`=?, `onesubstrateonly`=?, `noproducts`=?, `onemodifieronly`=? WHERE id=?");
				preparedStatement.setString(p++, f.getNameRaw());
				preparedStatement.setString(p++, f.getFormulaDef());
				preparedStatement.setInt(p++, Utils.boolToInt(f.isOneSubstrateOnly()));
				preparedStatement.setInt(p++, Utils.boolToInt(f.isNoProducts()));
				preparedStatement.setInt(p++, Utils.boolToInt(f.isOneModifierOnly()));
				preparedStatement.setInt(p++, f.getId());
				preparedStatement.executeUpdate();
				preparedStatement.close();
			} else {
				preparedStatement = con.prepareStatement(
						"insert into formula (id,id_model,name,formula,onesubstrateonly,noproducts,onemodifieronly,formulatype)"
								+ " values (NULL,?,?,?,?,?,?,?)",
						Statement.RETURN_GENERATED_KEYS);
				if (f.getModel() != null && f.getModel().getId() != null)
					preparedStatement.setInt(p++, f.getModel().getId());
				else
					preparedStatement.setNull(p++, Types.INTEGER);
				preparedStatement.setString(p++, f.getNameRaw());
				preparedStatement.setString(p++, f.getFormulaDef());
				preparedStatement.setInt(p++, Utils.boolToInt(f.isOneSubstrateOnly()));
				preparedStatement.setInt(p++, Utils.boolToInt(f.isNoProducts()));
				preparedStatement.setInt(p++, Utils.boolToInt(f.isOneModifierOnly()));
				preparedStatement.setInt(p++, f.getFormulaType().getValue());
				int affectedRows = preparedStatement.executeUpdate();
				if (affectedRows == 0) {
					throw new SQLException("Creating formula failed, no rows affected.");
				}
				try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						f.setId(generatedKeys.getInt(1));
					} else {
						throw new SQLException("Creating formula failed, no ID obtained.");
					}
					generatedKeys.close();
				}
				preparedStatement.close();
			}
			// GENERIC PARAMETERS TYPES (we define fixed formulavaluetype for parameters)
			if (f.getId() != null) {
				// DELETE OLD VALUES
				preparedStatement = con.prepareStatement("DELETE FROM formulagenparam WHERE id_formula=?");
				preparedStatement.setInt(1, f.getId());
				preparedStatement.executeUpdate();
				preparedStatement.close();
				// ADD NEW ENTRIES
				for (String genParam : f.getGenericParameters().keySet()) {
					if (f.getGenericParameters().get(genParam) != null) {
						preparedStatement = con.prepareStatement(
								"INSERT INTO `formulagenparam`(`id`, `id_formula`, `genparam`, `formulavaluetype`) VALUES (NULL,?,?,?)");
						p = 1;
						preparedStatement.setInt(p++, f.getId());
						preparedStatement.setString(p++, genParam);
						preparedStatement.setInt(p++, f.getGenericParameters().get(genParam).getValue());
						preparedStatement.executeUpdate();
						preparedStatement.close();
					}
				}
			}
			setDirty(false);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	public void saveDBAdmin() throws SQLException {
		Formula f = this;
		if (f.getId() == null) {
			if (isDBDelete())
				return;
			else
				saveDB();
		} else {
			if (isDBDelete())
				deleteDB();
			else
				saveDB();
		}
	}

	public void deleteDB() throws SQLException {
		Formula f = this;
		SharedData sharedData = SharedData.getInstance();
		Connection con = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement;
		if (f.getId() != null) {
			try {
				preparedStatement = con.prepareStatement("DELETE FROM formula WHERE id=?");
				preparedStatement.setInt(1, f.getId());
				preparedStatement.executeUpdate();
				preparedStatement.close();
				// GENERIC PARAMETERS TYPES (we define fixed formulavaluetype for parameters)
				// DELETE OLD VALUES
				preparedStatement = con.prepareStatement("DELETE FROM formulagenparam WHERE id_formula=?");
				preparedStatement.setInt(1, f.getId());
				preparedStatement.executeUpdate();
				preparedStatement.close();
				f.reset();
			} catch (SQLException e) {
				System.err.println(e.getMessage());
				throw e;
			}
		}
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

}
