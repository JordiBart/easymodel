package cat.udl.easymodel.logic.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dev.ModuleTabPanel.Session;
import com.vaadin.ui.UI;

import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathLinkOp;

public class FormulaImpl implements Formula {

	private Integer id=null;
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
	
	// Constants
	public static final String toNotParseRegEx = FormulaUtils.getInstance().getKeywordsRegEx()+"|(\\d+(.\\d+)?)";
	
	// Restriction
	private boolean oneSubstrateOnly = false;
	private boolean noProducts = false;
	private boolean oneModifierOnly = false;

	public FormulaImpl(String fName, String fExpression, FormulaType fType, User user, RepositoryType repType) {
		setName(fName);
		setFormula(fExpression);
		setFormulaType(fType);
		setUser(user);
		setRepositoryType(repType);
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
			if (!currentChar.matches("\\w") || i == formula.length()) {
				if (!part.isEmpty()) {
					if (!part.matches(toNotParseRegEx)) {
						if (!formula.substring(i).matches("\\[\\[.+\\]\\].*"))
							genParams.put(part, null);
						else if (!paramsBySubsAndModif.contains(part))
							paramsBySubsAndModif.add(part);
						// restrictions
					} else {
						if (part.matches("XF")) {
							setOneSubstrateOnly(true);
						} else if (part.matches("MF")) {
							setOneModifierOnly(true);
						}
						if (!keyWords.contains(part))
							keyWords.add(part);
					}
				}
				part = "";
			} else
				part += currentChar;
		}
		loadedFormula = formula;
		return true;
	}

	@Override
	public int getIdJava() {
		return idJava;
	}

	@Override
	public String getIdJavaStr() {
		return "F"+idJava;
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
	public void setFormula(String formula) {
		this.formula = formula;
		this.parse();
	}

	@Override
	public Map<String, FormulaValueType> getGenericParameters() {
		return genParams;
	}

	@Override
	public void setTypeOfGenericParameter(String genPar, FormulaValueType fvt) {
		if (genParams.containsKey(genPar))
			genParams.put(genPar, fvt);
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
		this.name = name;
	}

	@Override
	public boolean isDeletable() {
		return deletable;
	}

	@Override
	public void setDeletable(boolean deletable) {
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
		this.oneSubstrateOnly = oneReactiveOnly;
	}

	@Override
	public boolean isNoProducts() {
		return noProducts;
	}

	@Override
	public void setNoProducts(boolean noProducts) {
		this.noProducts = noProducts;
	}

	@Override
	public boolean isOneModifierOnly() {
		return oneModifierOnly;
	}

	@Override
	public void setOneModifierOnly(boolean oneModifierOnly) {
		this.oneModifierOnly = oneModifierOnly;
	}

	@Override
	public String getFormulaWithAddedPrefix(String sessionId, String reactionId) {
		String newStr = "";
		String part = "";
		String currentChar = null;
		for (int i = 0; i <= formula.length(); i++) {
			if (i < formula.length())
				currentChar = String.valueOf(formula.charAt(i));
			if (!currentChar.matches("\\w") || i == formula.length()) {
				if (!part.isEmpty()) {
					if (!part.matches(toNotParseRegEx)) {
						newStr += sessionId+reactionId+part;
						// restrictions
					} else {
						if (part.matches(FormulaUtils.getInstance().getBuiltInVarsRegEx())) {
							newStr += sessionId+reactionId+part;
						} else {
							newStr += part;
						}
					}
				}
				part = "";
				if (i < formula.length())
					newStr += currentChar;
			} else
				part += currentChar;
		}
		return newStr;
	}
	
	@Override
	public Integer getId() {
		return id;
	}
	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	@Override
	public FormulaType getFormulaType() {
		return formulaType;
	}
	@Override
	public void setFormulaType(FormulaType formulaType) {
		this.formulaType = formulaType;
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
	public RepositoryType getRepositoryType() {
		return repositoryType;
	}
	@Override
	public void setRepositoryType(RepositoryType repositoryType) {
		this.repositoryType = repositoryType;
	}
}
