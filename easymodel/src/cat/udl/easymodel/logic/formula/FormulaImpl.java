package cat.udl.easymodel.logic.formula;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dev.ModuleTabPanel.Session;
import com.vaadin.ui.UI;

import cat.udl.easymodel.controller.ContextUtils;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathLink;
import cat.udl.easymodel.utils.Utils;

public class FormulaImpl implements Formula {

	private Integer id = null;
	private int idJava;
	private String name = "";
	private String loadedFormula = "";
	private String formula = "";
	private List<String> keyWords = new ArrayList<>();
	private Map<String, FormulaValueType> genParams = new HashMap<>();
	private List<String> paramsBySubsAndModif = new ArrayList<>();
	private FormulaType formulaType;
	private boolean deletable = true;
	private User user;
	private RepositoryType repositoryType;
	private boolean isDirty=false;

	// Restriction
	private boolean oneSubstrateOnly = false;
	private boolean noProducts = false;
	private boolean oneModifierOnly = false;

	public FormulaImpl(String fName, String fExpression, FormulaType fType, User user, RepositoryType repType) {
		setName(fName);
		setFormulaDef(fExpression);
		setFormulaType(fType);
		setUser(user);
		setRepositoryType(repType);
		setDirty(true);
	}

	public FormulaImpl(Formula from) {
		this.id = from.getId();
		this.idJava = from.getIdJava();
		this.name = from.getName();
		this.setFormulaType(from.getFormulaType());
		this.deletable = from.isDeletable();
		setUser(from.getUser());
		setRepositoryType(from.getRepositoryType());
		setDirty(from.isDirty());

		// Restriction
		oneSubstrateOnly = from.isOneSubstrateOnly();
		noProducts = from.isNoProducts();
		oneModifierOnly = from.isOneModifierOnly();

		this.setFormulaDef(from.getFormulaDef());
	}

	@Override
	public boolean isBlank() {
		return formula.isEmpty();
	}

	@Override
	public boolean isValid() {
		return parse();
	}

	@Override
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
		genParams.clear();
		paramsBySubsAndModif.clear();
		resetRestrictions();
		String part = "";
		String currentChar = null;
		for (int i = 0; i <= formula.length(); i++) {
			if (i < formula.length())
				currentChar = String.valueOf(formula.charAt(i));
			if (!currentChar.matches("\\w|:") || i == formula.length()) {
				if (!part.isEmpty()) {
					if (!part.matches(FormulaUtils.getInstance().getToNotParseRegEx())) {
						// var names
						if (!formula.substring(i).matches("\\[\\[.+\\]\\].*")) {
							genParams.put(part, null);
						} else if (!paramsBySubsAndModif.contains(part))
							paramsBySubsAndModif.add(part);
						// restrictions
					} else {
						if (part.matches("XF")) {
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
			} else
				part += currentChar;
		}
		loadedFormula = formula;
		setDirty(true);
		return true;
	}

	@Override
	public int getIdJava() {
		return idJava;
	}

	@Override
	public String getIdJavaStr() {
		return "F" + idJava;
	}

	@Override
	public void setIdJava(int id) {
		this.idJava = id;
	}

	@Override
	public String getFormulaDef() {
		return formula;
	}

	@Override
	public void setFormulaDef(String formula) {
		this.formula = formula;
		this.parse();
	}

	@Override
	public Map<String, FormulaValueType> getGenericParameters() {
		return genParams;
	}

	@Override
	public void setTypeOfGenericParameter(String genPar, FormulaValueType fvt) {
		if (genParams.containsKey(genPar)) {
			genParams.put(genPar, fvt);
			setDirty(true);
		}
	}

	@Override
	public String getNameToShow() {
		if (name.equals(""))
			return getIdJavaStr();
		return name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		setDirty(true);
		this.name = name;
	}

	@Override
	public boolean isDeletable() {
		return deletable;
	}

	@Override
	public void setDeletable(boolean deletable) {
		setDirty(true);
		this.deletable = deletable;
	}

	@Override
	public List<String> getParametersBySubsAndModif() {
		return paramsBySubsAndModif;
	}

	// Restriction
	@Override
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

	@Override
	public List<String> getKeyWords() {
		return keyWords;
	}

	@Override
	public boolean isOneSubstrateOnly() {
		return oneSubstrateOnly;
	}

	@Override
	public void setOneSubstrateOnly(boolean oneReactiveOnly) {
		setDirty(true);
		this.oneSubstrateOnly = oneReactiveOnly;
	}

	@Override
	public boolean isNoProducts() {
		return noProducts;
	}

	@Override
	public void setNoProducts(boolean noProducts) {
		setDirty(true);
		this.noProducts = noProducts;
	}

	@Override
	public boolean isOneModifierOnly() {
		return oneModifierOnly;
	}

	@Override
	public void setOneModifierOnly(boolean oneModifierOnly) {
		setDirty(true);
		this.oneModifierOnly = oneModifierOnly;
	}

	@Override
	public String getMathematicaReadyFormula(String reactionContext, Model m) {
		String newStr = "";
		String part = "";
		String currentChar = null;
		for (int i = 0; i <= formula.length(); i++) {
			if (i < formula.length())
				currentChar = String.valueOf(formula.charAt(i));
			if (!currentChar.matches("\\w|:") || i == formula.length()) {
				if (!part.isEmpty()) {
					if (m.getAllSpecies().containsKey(part))
						newStr += ContextUtils.modelContext + part;
					else if (part.matches(FormulaUtils.getInstance().getGeneralVarsRegEx())) // time
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
//						System.out.println(part);
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

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		setDirty(true);
		this.id = id;
	}

	@Override
	public FormulaType getFormulaType() {
		return formulaType;
	}

	@Override
	public void setFormulaType(FormulaType formulaType) {
		setDirty(true);
		this.formulaType = formulaType;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public void setUser(User user) {
		setDirty(true);
		this.user = user;
	}

	@Override
	public RepositoryType getRepositoryType() {
		return repositoryType;
	}

	@Override
	public void setRepositoryType(RepositoryType repositoryType) {
		setDirty(true);
		this.repositoryType = repositoryType;
	}

	@Override
	public void clear() {
		setDirty(true);
		id = null;
		name = null;
		loadedFormula = null;
		formula = null;
		keyWords.clear();
		keyWords = null;
		genParams.clear();
		genParams = null;
		paramsBySubsAndModif.clear();
		paramsBySubsAndModif = null;
		formulaType = null;
		user = null;
		repositoryType = null;
	}

	@Override
	public void saveDB() throws SQLException {
		Formula f = this;
		SharedData sharedData = SharedData.getInstance();
		Connection con = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement;
		int p;
		try {
			if (!f.isValid())
				throw new SQLException("Invalid formula "+f.getFormulaDef());
			// UPDATE/INSERT
			p = 1;
			if (f.getId() != null) {
				preparedStatement = con.prepareStatement(
						"UPDATE formula SET `name`=?, `formula`=?, `onesubstrateonly`=?, `noproducts`=?, `onemodifieronly`=?, `modified`=DATE(NOW()) WHERE id=?");
				preparedStatement.setString(p++, f.getName());
				preparedStatement.setString(p++, f.getFormulaDef());
				preparedStatement.setInt(p++, Utils.boolToInt(f.isOneSubstrateOnly()));
				preparedStatement.setInt(p++, Utils.boolToInt(f.isNoProducts()));
				preparedStatement.setInt(p++, Utils.boolToInt(f.isOneModifierOnly()));
				preparedStatement.setInt(p++, f.getId());
				preparedStatement.executeUpdate();
				preparedStatement.close();
			} else {
				preparedStatement = con.prepareStatement(
						"insert into formula (id,id_user,name,formula,onesubstrateonly,noproducts,onemodifieronly,formulatype,repositorytype,modified)"
								+ " values (NULL, ?, ?, ?, ?, ?,?,?,?,DATE(NOW()))",
						Statement.RETURN_GENERATED_KEYS);
				if (f.getUser() != null)
					preparedStatement.setInt(p++, f.getUser().getId());
				else
					preparedStatement.setNull(p++, Types.INTEGER);
				preparedStatement.setString(p++, f.getName());
				preparedStatement.setString(p++, f.getFormulaDef());
				preparedStatement.setInt(p++, Utils.boolToInt(f.isOneSubstrateOnly()));
				preparedStatement.setInt(p++, Utils.boolToInt(f.isNoProducts()));
				preparedStatement.setInt(p++, Utils.boolToInt(f.isOneModifierOnly()));
				preparedStatement.setInt(p++, f.getFormulaType().getValue());
				preparedStatement.setInt(p++, f.getRepositoryType().getValue());
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
	@Override
	public boolean isDirty() {
		return isDirty;
	}
	@Override
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
}
