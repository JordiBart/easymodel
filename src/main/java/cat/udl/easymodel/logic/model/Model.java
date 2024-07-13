package cat.udl.easymodel.logic.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaArrayValue;
import cat.udl.easymodel.logic.formula.FormulaValue;
import cat.udl.easymodel.logic.formula.Formulas;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.stochastic.ReactantReactionLevel;
import cat.udl.easymodel.logic.stochastic.ReactantModelLevel;
import cat.udl.easymodel.logic.types.*;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathLinkOp;
import cat.udl.easymodel.utils.*;
import com.vaadin.flow.component.Html;

public class Model extends ArrayList<Reaction> {
	private int idJava;
	private Integer id = null;
	// private int reactionsAutoIncrement = 1;
	private User user;
	private Formulas formulas;
	private SortedMap<String, Species> speciesConfigMap = new TreeMap<>();
	private SimConfig simConfig = new SimConfig();
	private String name = "";
	private String description = "";
	private RepositoryType repositoryType = RepositoryType.TEMP;
	private StochasticGradeType stochasticGradeType = StochasticGradeType.UNCHECKED;

	private boolean isDBDelete = false;
	private Model parent = null;

	public Model() {
		super();
		this.reset();
	}

	public Model(Model from) {
		// basic model copy
		super();
		setParent(from);
		setId(from.getId());
		setUser(from.getUser());
		setName(from.getName());
		setDescription(from.getDescription());
		setRepositoryType(from.getRepositoryType());
		setStochasticGradeType(from.stochasticGradeType);
	}

	public Model(Model from, int forModelFullCopy) throws Exception {
		super();
		parent = from;
		idJava = from.idJava;
		id = from.id;
		user = from.user;
		name = from.name;
		description = from.description;
		repositoryType = from.repositoryType;
		stochasticGradeType = from.stochasticGradeType;
		isDBDelete = from.isDBDelete;
		// FORMULAS
		formulas = new Formulas(from.formulas,this);
		// FORMULA LINKING
//		TreeMap<Integer, Integer> reactionToFormulaMap = new TreeMap<>();
//		for(Reaction fromReaction : from)
//			reactionToFormulaMap.put(fromReaction.getIdJava(), fromReaction.getFormula().getIdJava());
		// ADD REACTIONS WITH FORMULA AND FORMULA VALUES
//		for(Reaction fromReaction : from)
//			this.addReaction(new Reaction(fromReaction,this.formulas.getFormulaByIdJava(reactionToFormulaMap.get(fromReaction.getIdJava())),1));
		// ADD REACTIONS WITH FORMULA AND FORMULA VALUES
		for(Reaction fromReaction : from)
			this.addReaction(new Reaction(fromReaction,this,1));
//		for(Reaction r : this)
//			for (String key : r.getFormulaGeneralParameters().keySet())
//				p.p(key+" "+r.getFormulaGeneralParameters().get(key));
		speciesConfigMap=from.getAllSpeciesCopy();
		simConfig = new SimConfig(from.getSimConfig(),this);
		this.checkValidModel();
	}

	public void initNewModel(){
		setRepositoryType(RepositoryType.TEMP);
		setStochasticGradeType(StochasticGradeType.UNCHECKED);
		addReaction(new Reaction());
	}

	public String getUserName() {
		if (getUser() != null) {
			return getUser().getName();
		} else {
			return "unknown";
		}
	}

	public boolean equals(Object in) {
		if (in != null && in instanceof Model && System.identityHashCode(this) == System.identityHashCode(in))
			return true;
		return false;
	}

	public void reset() {
		this.clear();
//		parent = null;
		speciesConfigMap.clear();
		formulas = new Formulas(this);
		user = null;
		isDBDelete = false;
		simConfig.reset();
		name = "";
		description = "";
		repositoryType = RepositoryType.TEMP;
		stochasticGradeType = StochasticGradeType.UNCHECKED;
	}

	public String getName() {
		return name;
	}

	public String getNameShort() {
		int numChars = 8;
		if (getName().length() >= numChars)
			return getName().replaceAll("\\s", "_").substring(0, numChars);
		else
			return getName().replaceAll("\\s", "_");
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getIdJava() {
		return idJava;
	}

	public void setIdJava(int id) {
		this.idJava = id;
	}

	public boolean addReaction(Reaction react) {
		if (react != null) {
			react.setIdJava(this.size() + 1);
			return this.add(react);
		}
		return false;
	}

	public ArrayList<Reaction> getValidReactions() {
		ArrayList<Reaction> l = new ArrayList<>();
		for (Reaction react : this)
			if (react.isValid())
				l.add(react);
		Collections.sort(l);
		return l;
	}

	public boolean removeReaction(Reaction react) {
		if (react != null && remove(react)) {
			int i = 1;
			for (Reaction r2 : this)
				r2.setIdJava(i++);
			return true;
		}
		return false;
	}

	public void resetReactions() {
		this.clear();
		// reactionsAutoIncrement = 1;
	}

	public void checkReactions() throws Exception {
		String err = "Invalid reactions: ";
		int numErr = 0;
		for (Reaction react : this)
			if (!react.isValid() && !react.isBlank()) {
				if (numErr > 0)
					err += ", " + react.getIdJavaStr();
				else
					err += react.getIdJavaStr();
				numErr++;
			}
		if (numErr > 0)
			throw new Exception(err);
	}

	public ArrayList<String> getAllUsedFormulaStringsWithContext() {
		ArrayList<String> res = new ArrayList<>();
		for (Reaction r : this) {
			if (r.getFormula() != null)
				res.add(r.getFormula().getMathematicaReadyFormula(r.getMathematicaContext(), this));
		}
		return res;
	}

	public SortedMap<String, Species> getAllSpeciesTimeDependent() {
		SortedMap<String, Species> retMap = new TreeMap<>();
		for (String name : getAllSpecies().keySet()) {
			Species sp = getAllSpecies().get(name);
			if (sp.getVarType() == SpeciesVarTypeType.TIME_DEP)
				retMap.put(name, sp);
		}
		return retMap;
	}

	public SortedMap<String, Species> getAllSpeciesConstant() {
		SortedMap<String, Species> retMap = new TreeMap<>();
		for (String name : getAllSpecies().keySet()) { // modifiers can be indep vars
			Species sp = getAllSpecies().get(name);
			if (sp.getVarType() == SpeciesVarTypeType.INDEPENDENT)
				retMap.put(name, sp);
		}
		return retMap;
	}

	public SortedMap<String, Species> getAllSpeciesExceptModifiers() {
		SortedMap<String, Species> allSpecies = getAllSpecies();
		SortedMap<String, Species> allParticipants = new TreeMap<>();
		for (Reaction r : this)
			for (String sp : r.getBothSides().keySet())
				allParticipants.put(sp, allSpecies.get(sp));
		return allParticipants;
	}

	public SortedMap<String, Species> getAllSubstrates() {
		SortedMap<String, Species> allSpecies = getAllSpecies();
		SortedMap<String, Species> allSubstrates = new TreeMap<>();
		for (Reaction r : this)
			for (String sp : r.getLeftPartSpecies().keySet())
				allSubstrates.put(sp, allSpecies.get(sp));
		return allSubstrates;
	}

	public SortedMap<String, Species> getAllProducts() {
		SortedMap<String, Species> allSpecies = getAllSpecies();
		SortedMap<String, Species> allProducts = new TreeMap<>();
		for (Reaction r : this)
			for (String sp : r.getRightPartSpecies().keySet())
				allProducts.put(sp, allSpecies.get(sp));
		return allProducts;
	}

	public SortedMap<String, Species> getAllModifiers() {
		SortedMap<String, Species> allSpecies = getAllSpecies();
		SortedMap<String, Species> allModifiers = new TreeMap<>();
		for (Reaction r : this)
			for (String sp : r.getModifiers().keySet())
				allModifiers.put(sp, allSpecies.get(sp));
		return allModifiers;
	}

	private SortedMap<String, Species> getAllSpeciesCopy(){
		SortedMap<String, Species> in = getAllSpecies();
		SortedMap<String, Species> out = new TreeMap<>();
		for (String key: in.keySet()){
			out.put(key, new Species(in.get(key)));
		}
		return out;
	}

	public SortedMap<String, Species> getAllSpecies() {
		// Update concentrations species
		// Delete unused values
		List<String> speciesToDelete = new ArrayList<>();
		for (String sp : speciesConfigMap.keySet()) {
			boolean found = false;
			for (Reaction react : this) {
				if (react.getBothSides().containsKey(sp) || react.getModifiers().containsKey(sp)) {
					found = true;
					break;
				}
			}
			if (!found)
				speciesToDelete.add(sp);
		}
		for (String sp : speciesToDelete)
			speciesConfigMap.remove(sp);
		// Put new species
		for (Reaction react : this) {
			for (String sp : react.getBothSides().keySet())
				if (!speciesConfigMap.containsKey(sp))
					speciesConfigMap.put(sp, new Species(sp));
			for (String sp : react.getModifiers().keySet())
				if (!speciesConfigMap.containsKey(sp))
					speciesConfigMap.put(sp, new Species(sp));
		}
		// Old species values are still there
		return speciesConfigMap;
	}

	public boolean isAllSpeciesSet() {
		for (String sp : getAllSpecies().keySet())
			if (getAllSpecies().get(sp) == null || !getAllSpecies().get(sp).isSet())
				return false;
		return true;
	}

	public String getStoichiometricMatrix() throws Exception {
		// first index is for species, second for reaction
		checkReactions();
		if (getValidReactions().isEmpty())
			throw new Exception("All reactions are empty");
		StringBuilder matrix = new StringBuilder("{");
		boolean first1 = true;
		boolean first2;
		for (String species : getAllSpeciesTimeDependent().keySet()) {
			matrix.append(first1 ? "" : ",");
			matrix.append("{");
			first2 = true;
			for (Reaction react : getValidReactions()) {
				matrix.append(first2 ? "" : ",");
				Integer val = react.getSpeciesValuesForStMatrix().get(species);
				matrix.append(val != null ? val : "0");
				first2 = false;
			}
			matrix.append("}");
			first1 = false;
		}
		matrix.append("}");
		return matrix.toString();
	}

	public Html getDisplayStoichiometricMatrix() {
		ArrayList<Reaction> reactions = this;
		HtmlTableBuilder tb = new HtmlTableBuilder("matrix");
		tb.addCell("",1);
		for (Reaction react : reactions)
			tb.addCell( react.getIdJavaStr(),1);
		tb.setCols();
		for (String species : getAllSpeciesTimeDependent().keySet()) {
			tb.addCell(species,1);
			for (Reaction react : reactions) {
				Integer val = react.getSpeciesValuesForStMatrix().get(species);
				if (val != null) {
					tb.addCell(String.valueOf(val),0);
				}
				else {
					tb.addCell("0",0);
				}
			}
		}
		tb.finish();
		Html table = new Html(tb.getHtmlCode());
		return table;
	}

	public String getRegulatoryMatrix() throws Exception {
		// not used yet
		checkReactions();
		if (getValidReactions().isEmpty())
			throw new Exception("All reactions are empty");
		StringBuilder matrix = new StringBuilder("{");
		boolean first1 = true;
		boolean first2;
		for (String mod : getAllSpecies().keySet()) {
			matrix.append(first1 ? "" : ",");
			matrix.append("{");
			first2 = true;
			for (Reaction react : getValidReactions()) {
				matrix.append(first2 ? "" : ",");
				Integer val = react.getModifiers().get(mod);
				if (val != null)
					matrix.append(val == 1 ? "1" : "-1");
				else
					matrix.append("0");
				first2 = false;
			}
			matrix.append("}");
			first1 = false;
		}
		matrix.append("}");
		return matrix.toString();
	}

	public Html getDisplayRegulatoryMatrix() {
		ArrayList<Reaction> reactions = this;
		HtmlTableBuilder tableBuilder = new HtmlTableBuilder("matrix");
		tableBuilder.addCell("",1);
		for (Reaction react : reactions)
			tableBuilder.addCell(react.getIdJavaStr(),1);
		tableBuilder.setCols();
		for (String species : getAllSpecies().keySet()) {
			tableBuilder.addCell( species,1);
			for (Reaction react : reactions) {
				Integer val = react.getModifiers().get(species);
				if (val != null) {
					tableBuilder.addCell((val == 1 ? "1" : "-1"),0);
				}
				else {
					tableBuilder.addCell("0",0);
				}
			}
		}
		tableBuilder.finish();
		Html table = new Html(tableBuilder.getHtmlCode());
		return table;
	}

	public void unlinkFormulaFromReactions(Formula f) {
		for (Reaction r : this) {
			if (r.getFormula() == f)
				r.setFormula(null);
		}
	}

	public void checkAndAdaptToSimulate() throws CException {
		try {
			simConfig.checkAndPrepareToSimulate(this);
			checkValidModel();
		} catch (Exception e) {
			throw new CException(e.getMessage());
		}
	}

	public void checkValidModel() throws Exception {
		cleanModel();
		String err = "";
		int numErr = 0;
		if (this.name == null || this.name.equals("")) {
			if (this.getRepositoryType()==RepositoryType.PRIVATE) {
				err += "Model must have a name\n";
				numErr++;
			}else{
				this.name="untitled";
			}
		}
		if (this.size() == 0) {
			err += "Model must have at least one reaction\n";
			numErr++;
		}
		for (String sp : getAllSpecies().keySet())
			if (getAllSpecies().get(sp) == null || !getAllSpecies().get(sp).isSet()) {
				err += "Species " + sp + ": Concentration missing\n";
				numErr++;
			}
		for (Reaction r : this) {
			if (r.isBlank()) {
				err += "Reaction " + r.getIdJavaStr() + ": Blank reaction\n";
				numErr++;
			} else if (!r.isValid()) {
				err += "Reaction " + r.getIdJavaStr() + ": Invalid reaction\n";
				numErr++;
			}
			if (r.getFormula() == null) {
				err += "Reaction " + r.getIdJavaStr() + ": No rate is bound\n";
				numErr++;
			}
			if (!r.isFormulaAndParametersSet()) {
				err += "Reaction " + r.getIdJavaStr() + ": Missing rate values\n";
				numErr++;
			}
		}
		if (numErr > 0)
			throw new Exception(err);
	}

	public ArrayList<String> getDepVarArrayList() {
		ArrayList<String> res = new ArrayList<String>();
		for (String dv : getAllSpeciesTimeDependent().keySet())
			res.add(dv);
		return res;
	}

	private void cleanModel() {
		// vars only modifiers can't be time dependent
		for (String sp : this.getAllSpecies().keySet()) {
			if (!this.getAllSpeciesExceptModifiers().containsKey(sp)) {
				this.getAllSpecies().get(sp).setVarType(SpeciesVarTypeType.INDEPENDENT);
			}
		}
	}

	public Reaction getReactionByIdJava(int idJava) {
		for (Reaction r: this)
			if (r.getIdJava() == idJava)
				return r;
		return null;
	}

	public SimConfig getSimConfig() {
		return simConfig;
	}

	public RepositoryType getRepositoryType() {
		return repositoryType;
	}

	public void setRepositoryType(RepositoryType repositoryType) {
		this.repositoryType = repositoryType;
	}

	public StochasticGradeType getStochasticGradeType() {
		return stochasticGradeType;
	}

	public void setStochasticGradeType(StochasticGradeType stochasticGradeType) {
		this.stochasticGradeType = stochasticGradeType;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void saveDB() throws DBException {
//		System.out.println("model save");
		if (getRepositoryType()!=RepositoryType.PRIVATE)
			setRepositoryType(RepositoryType.PRIVATE);
		if (getParent()!=null&&getParent().getRepositoryType()!=RepositoryType.PUBLIC){
			getParent().setName(this.getName());
			getParent().setDescription(this.getDescription());
		}
		SharedData sharedData = SharedData.getInstance();
		Connection conn = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement = null;
		String table;
		Integer tmpId = 0;
		try {
			// REMOVE FULL MODEL+RATES (INNODB CASCADING APPLIED)
			if (this.id != null) {
				preparedStatement = conn.prepareStatement("DELETE FROM model WHERE id=?");
				preparedStatement.setInt(1, this.id);
				preparedStatement.executeUpdate();
			}
			// INSERT FULL MODEL
			// insert to model table
			table = "model";
			preparedStatement = conn.prepareStatement("insert into " + table
					+ " (id, id_user, name, description, repositorytype, stochasticgrade, modified) values (?, ?, ?, ?, ?, ?, DATE(NOW()))",
					Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			if (this.id != null)
				preparedStatement.setInt(p++, this.id);
			else
				preparedStatement.setNull(p++, java.sql.Types.INTEGER);
			preparedStatement.setInt(p++, user.getId());
			preparedStatement.setString(p++, getName());
			preparedStatement.setString(p++, getDescription());
			preparedStatement.setInt(p++, getRepositoryType().getValue());
			preparedStatement.setInt(p++, getStochasticGradeType().getValue());
			int affectedRows = preparedStatement.executeUpdate();
			if (affectedRows == 0)
				throw new SQLException("Creating " + table + " failed, no rows affected.");
			try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
				if (generatedKeys.next())
					this.setId(generatedKeys.getInt(1));
				else
					throw new SQLException("Creating " + table + " failed, no ID obtained.");
				generatedKeys.close();
			}
			preparedStatement.close();
			// save to species table
			for (String sp : speciesConfigMap.keySet()) {
				table = "species";
				preparedStatement = conn.prepareStatement("insert into " + table
						+ " (`id`, `id_model`, `species`, `concentration`, `vartype`, `amount`) values (NULL, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
				p = 1;
				preparedStatement.setInt(p++, this.getId());
				preparedStatement.setString(p++, sp);
				preparedStatement.setString(p++, speciesConfigMap.get(sp).getConcentration());
				preparedStatement.setInt(p++, speciesConfigMap.get(sp).getVarType().getValue());
				preparedStatement.setString(p++, speciesConfigMap.get(sp).getAmount());
				affectedRows = preparedStatement.executeUpdate();
				if (affectedRows == 0)
					throw new SQLException("Creating " + table + " failed, no rows affected.");
			}
			preparedStatement.close();
			// save formulas
			this.getFormulas().resetAllIds();
			this.getFormulas().saveDB();
//			if (true)
//				return;
			// save to reaction table
			for (Reaction r : this) {
//				System.out.println("r to save "+r.getReactionStr());
				table = "reaction";
				preparedStatement = conn.prepareStatement(
						"insert into " + table + " (`id`, `id_model`, `id_formula`, `reaction`) values (NULL, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
				p = 1;
				preparedStatement.setInt(p++, this.getId());
				preparedStatement.setInt(p++, r.getFormula().getId());
				preparedStatement.setString(p++, r.getReactionStr());
//				System.out.println("r "+r.getReactionStr()+" f id: "+r.getFormula().getId());
				affectedRows = preparedStatement.executeUpdate();
				if (affectedRows == 0)
					throw new SQLException("Creating " + table + " failed, no rows affected.");
				try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
					if (generatedKeys.next())
						r.setId(generatedKeys.getInt(1));
					else
						throw new SQLException("Creating " + table + " failed, no ID obtained.");
					generatedKeys.close();
				}
				preparedStatement.close();
				// save to formulavalue table
				for (String constName : r.getFormulaGeneralParameters().keySet()) {
					table = "formulavalue";
					FormulaValue fv = r.getFormulaGeneralParameters().get(constName);
					preparedStatement = conn.prepareStatement("insert into " + table
							+ " (`id`, `id_reaction`, `constant`, `formulavaluetype`, `constantvalue`, `substratevalue`, `modifiervalue`) values (NULL, ?, ?, ?, ?,?,?)",
							Statement.RETURN_GENERATED_KEYS);
					p = 1;
					preparedStatement.setInt(p++, r.getId());
					preparedStatement.setString(p++, constName);
					preparedStatement.setInt(p++, fv.getType().getValue());
					if (fv.getType() == FormulaValueType.CONSTANT) {
						preparedStatement.setString(p++, fv.getStringValue());
						preparedStatement.setNull(p++, Types.VARCHAR);
						preparedStatement.setNull(p++, Types.VARCHAR);
					} else if (fv.getType() == FormulaValueType.SUBSTRATE) {
						preparedStatement.setNull(p++, Types.VARCHAR);
						preparedStatement.setString(p++, fv.getStringValue());
						preparedStatement.setNull(p++, Types.VARCHAR);
					} else if (fv.getType() == FormulaValueType.MODIFIER) {
						preparedStatement.setNull(p++, Types.VARCHAR);
						preparedStatement.setNull(p++, Types.VARCHAR);
						preparedStatement.setString(p++, fv.getStringValue());
					}
					affectedRows = preparedStatement.executeUpdate();
					if (affectedRows == 0)
						throw new SQLException("Creating " + table + " failed, no rows affected.");
					preparedStatement.close();
				}
				// save to formulasubstratesarray/value table
				for (String constName : r.getFormulaSubstratesArrayParameters().keySet()) {
					SortedMap<String, FormulaArrayValue> valuesMap = r.getFormulaSubstratesArrayParameters()
							.get(constName);
					table = "formulasubstratesarray";
					preparedStatement = conn.prepareStatement(
							"insert into " + table + " (`id`, `id_reaction`, `constant`) values (NULL, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);
					p = 1;
					preparedStatement.setInt(p++, r.getId());
					preparedStatement.setString(p++, constName);
					affectedRows = preparedStatement.executeUpdate();
					if (affectedRows == 0)
						throw new SQLException("Creating " + table + " failed, no rows affected.");
					try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
						if (generatedKeys.next())
							tmpId = generatedKeys.getInt(1);
						else
							throw new SQLException("Creating " + table + " failed, no ID obtained.");
						generatedKeys.close();
					}
					preparedStatement.close();
					for (String sp : valuesMap.keySet()) {
						table = "formulasubstratesarrayvalue";
						String val = valuesMap.get(sp).getValue();
						preparedStatement = conn.prepareStatement("insert into " + table
								+ " (`id`, `id_formulasubstratesarray`, `species`, `value`) values (NULL, ?, ?, ?)",
								Statement.RETURN_GENERATED_KEYS);
						p = 1;
						preparedStatement.setInt(p++, tmpId);
						preparedStatement.setString(p++, sp);
						preparedStatement.setString(p++, val);
						affectedRows = preparedStatement.executeUpdate();
						if (affectedRows == 0)
							throw new SQLException("Creating " + table + " failed, no rows affected.");
						preparedStatement.close();
					}
				}
				// save to formulamodifiersarray/value table
				for (String constName : r.getFormulaModifiersArrayParameters().keySet()) {
					SortedMap<String, FormulaArrayValue> valuesMap = r.getFormulaModifiersArrayParameters()
							.get(constName);
					table = "formulamodifiersarray";
					preparedStatement = conn.prepareStatement(
							"insert into " + table + " (`id`, `id_reaction`, `constant`) values (NULL, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);
					p = 1;
					preparedStatement.setInt(p++, r.getId());
					preparedStatement.setString(p++, constName);
					affectedRows = preparedStatement.executeUpdate();
					if (affectedRows == 0)
						throw new SQLException("Creating " + table + " failed, no rows affected.");
					try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
						if (generatedKeys.next())
							tmpId = generatedKeys.getInt(1);
						else
							throw new SQLException("Creating " + table + " failed, no ID obtained.");
						generatedKeys.close();
					}
					preparedStatement.close();
					for (String mod : valuesMap.keySet()) {
						table = "formulamodifiersarrayvalue";
						String val = valuesMap.get(mod).getValue();
						preparedStatement = conn.prepareStatement("insert into " + table
								+ " (`id`, `id_formulamodifiersarray`, `modifier`, `value`) values (NULL, ?, ?,?)",
								Statement.RETURN_GENERATED_KEYS);
						p = 1;
						preparedStatement.setInt(p++, tmpId);
						preparedStatement.setString(p++, mod);
						preparedStatement.setString(p++, val);
						affectedRows = preparedStatement.executeUpdate();
						if (affectedRows == 0)
							throw new SQLException("Creating " + table + " failed, no rows affected.");
						preparedStatement.close();
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DBException(e.getMessage());
		}
	}

	public void saveBasicDB() throws DBException {
		if (id == null)
			return;
		SharedData sharedData = SharedData.getInstance();
		Connection conn = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement = null;
		int p = 1;
		try {
			preparedStatement = conn.prepareStatement(
					"UPDATE model SET `name`=?,`description`=?,`repositorytype`=?,`stochasticgrade`=?,`modified`=DATE(NOW()) WHERE id=?");
			preparedStatement.setString(p++, getName());
			preparedStatement.setString(p++, getDescription());
			preparedStatement.setInt(p++, getRepositoryType().getValue());
			preparedStatement.setInt(p++, getStochasticGradeType().getValue());
			preparedStatement.setInt(p++, id);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new DBException(e.getMessage());
		}
	}

	public void saveStochasticGradeDB() throws DBException {
		if (id == null)
			return;
		SharedData sharedData = SharedData.getInstance();
		Connection conn = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement = null;
		int p = 1;
		try {
			preparedStatement = conn.prepareStatement(
					"UPDATE model SET `stochasticgrade`=? WHERE id=?");
			preparedStatement.setInt(p++, getStochasticGradeType().getValue());
			preparedStatement.setInt(p++, id);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new DBException(e.getMessage());
		}
	}

	public void saveDBAdmin() throws DBException {
		if (getId() != null) {
			if (isDBDelete())
				deleteDB();
			else
				saveBasicDB();
		}
	}

	public void loadDB() throws DBException {
		// load model by id
		if (this.id == null)
			throw new DBException("Model has no id");
		SharedData sharedData = SharedData.getInstance();
		Connection conn = sharedData.getDbManager().getCon();
		PreparedStatement pre, pre2, pre3;
		ResultSet rs, rs2, rs3;
		String table;
		Integer tmpId = 0;
		try {
			// model table
			table = "model";
			pre = conn.prepareStatement(
					"SELECT `id`, `id_user`, `name`, `description`, `repositorytype`, `stochasticgrade`, `modified` FROM " + table
							+ " WHERE id=?");
			pre.setInt(1, this.id);
			rs = pre.executeQuery();
			this.reset(); // if id found, reset this model to fill
			while (rs.next()) {
				setId(rs.getInt("id"));
				setUser(sharedData.getUsers().getUserById(rs.getInt("id_user")));
				setName(rs.getString("name"));
				setDescription(rs.getString("description"));
				setRepositoryType(RepositoryType.valueOf(rs.getInt("repositorytype")));
				setStochasticGradeType(StochasticGradeType.valueOf(rs.getInt("stochasticgrade")));
			}
			rs.close();
			pre.close();
			// species table
			table = "species";
			pre = conn.prepareStatement(
					"SELECT `id`, `id_model`, `species`, `concentration`, `vartype`, `amount` FROM "
							+ table + " WHERE id_model=?");
			pre.setInt(1, this.id);
			rs = pre.executeQuery();
			while (rs.next()) {
				Species spObj = new Species(rs.getString("species"));
				spObj.setConcentration(rs.getString("concentration"));
				spObj.setVarType(SpeciesVarTypeType.valueOf(rs.getInt("vartype")));
				spObj.setAmount(rs.getString("amount"));
				this.speciesConfigMap.put(spObj.getName(), spObj);
			}
			rs.close();
			pre.close();
			// load formulas
			this.formulas.loadDB();
			// reaction table
			table = "reaction";
			pre = conn.prepareStatement(
					"SELECT `id`, `id_model`, `id_formula`, `reaction` FROM " + table + " WHERE id_model=?");
			pre.setInt(1, this.id);
			rs = pre.executeQuery();
			while (rs.next()) {
				Reaction r = new Reaction();
				r.setId(rs.getInt("id"));
				r.setReactionStr(ReactionUtils.convertV1FormatToV2(rs.getString("reaction")));
				r.setFormula(this.getFormulas().getFormulaById(rs.getInt("id_formula")));

				table = "formulavalue";
				pre2 = conn.prepareStatement(
						"SELECT `id`, `id_reaction`, `constant`, `formulavaluetype`, `constantvalue`, `substratevalue`, `modifiervalue` FROM "
								+ table + " WHERE id_reaction=?");
				pre2.setInt(1, r.getId());
				rs2 = pre2.executeQuery();
				while (rs2.next()) {
					FormulaValue fv = new FormulaValue(rs2.getString("constant"));
					FormulaValueType fvt = FormulaValueType.valueOf(rs2.getInt("formulavaluetype"));
					fv.setType(fvt);
					switch (fv.getType()) {
						case CONSTANT:
							fv.setValue(rs2.getString("constantvalue"));
							break;
						case SUBSTRATE:
							fv.setValue(rs2.getString("substratevalue"));
							break;
						case MODIFIER:
							fv.setValue(rs2.getString("modifiervalue"));
							break;
					}
					r.getFormulaGenParsRAW().put(rs2.getString("constant"), fv);
				}
				rs2.close();
				pre2.close();

				table = "formulasubstratesarray";
				pre2 = conn.prepareStatement(
						"SELECT `id`, `id_reaction`, `constant` FROM " + table + " WHERE id_reaction=?");
				pre2.setInt(1, r.getId());
				rs2 = pre2.executeQuery();
				while (rs2.next()) {
					tmpId = rs2.getInt("id");
					table = "formulasubstratesarrayvalue";
					pre3 = conn.prepareStatement("SELECT `id`, `id_formulasubstratesarray`, `species`, `value` FROM "
							+ table + " WHERE id_formulasubstratesarray=?");
					pre3.setInt(1, tmpId);
					rs3 = pre3.executeQuery();
					SortedMap<String, FormulaArrayValue> arrVals = new TreeMap<>();
					while (rs3.next()) {
						arrVals.put(rs3.getString("species"), new FormulaArrayValue(rs3.getString("value")));
					}
					rs3.close();
					pre3.close();
					r.getFormulaSubstratesArrayParametersRAW().put(rs2.getString("constant"), arrVals);
				}
				rs2.close();
				pre2.close();

				table = "formulamodifiersarray";
				pre2 = conn.prepareStatement(
						"SELECT `id`, `id_reaction`, `constant` FROM " + table + " WHERE id_reaction=?");
				pre2.setInt(1, r.getId());
				rs2 = pre2.executeQuery();
				while (rs2.next()) {
					tmpId = rs2.getInt("id");
					table = "formulamodifiersarrayvalue";
					pre3 = conn.prepareStatement("SELECT `id`, `id_formulamodifiersarray`, `modifier`, `value` FROM "
							+ table + " WHERE id_formulamodifiersarray=?");
					pre3.setInt(1, tmpId);
					rs3 = pre3.executeQuery();
					SortedMap<String, FormulaArrayValue> arrVals = new TreeMap<>();
					while (rs3.next()) {
						arrVals.put(rs3.getString("modifier"), new FormulaArrayValue(rs3.getString("value")));
					}
					rs3.close();
					pre3.close();
					r.getFormulaModifiersArrayParametersRAW().put(rs2.getString("constant"), arrVals);
				}
				rs2.close();
				pre2.close();

				this.addReaction(r);
			}
			rs.close();
			pre.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new DBException(e.getMessage());
		}
	}

	public void basicLoadDB() throws DBException {
		// gets basic data only to perform full load later
		if (this.id == null)
			throw new DBException("Model has no id");
		SharedData sharedData = SharedData.getInstance();
		Connection conn = sharedData.getDbManager().getCon();
		PreparedStatement pre;
		ResultSet rs;
		String table;
		try {
			table = "model";
			pre = conn.prepareStatement(
					"SELECT `id`, `id_user`, `name`, `description`, `repositorytype`, `stochasticgrade`, `modified` FROM " + table
							+ " WHERE id=?");
			pre.setInt(1, this.id);
			rs = pre.executeQuery();
			this.reset(); // if id found, reset this model to fill
			while (rs.next()) {
				setId(rs.getInt("id"));
				setUser(sharedData.getUsers().getUserById(rs.getInt("id_user")));
				setName(rs.getString("name"));
				setDescription(rs.getString("description"));
				setRepositoryType(RepositoryType.valueOf(rs.getInt("repositorytype")));
				setStochasticGradeType(StochasticGradeType.valueOf(rs.getInt("stochasticgrade")));
			}
			rs.close();
			pre.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw new DBException(e.getMessage());
		}
	}

	public void deleteDB() throws DBException {
		if (this.id == null)
			throw new DBException("Model has no id");
		SharedData sharedData = SharedData.getInstance();
		Connection con = sharedData.getDbManager().getCon();
		int p;
		PreparedStatement preparedStatement;
		try {
			preparedStatement = con.prepareStatement("DELETE FROM model WHERE id=?");
			p = 1;
			preparedStatement.setInt(p++, this.id);
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DBException(e.getMessage());
		}
	}

	public SortedMap<String, FormulaValue> getAllFormulaParameters() {
		SortedMap<String, FormulaValue> res = new TreeMap<>();
		for (Reaction r : this) {
			for (String parName : r.getFormulaGeneralParameters().keySet()) {
				if (r.getFormulaGeneralParameters().get(parName) != null && r.getFormulaGeneralParameters().get(parName).isFilled()) {
					res.put(r.getIdJavaStr() + parName, r.getFormulaGeneralParameters().get(parName));
				}
			}
		}
		return res;
	}

	public Map<String, SortedMap<String, FormulaArrayValue>> getAllFormulaSubstratesArrayValues() {
		Map<String, SortedMap<String, FormulaArrayValue>> res = new HashMap<>();
		for (Reaction r : this) {
			for (String parName : r.getFormulaSubstratesArrayParameters().keySet()) {
				for (String sp : r.getFormulaSubstratesArrayParameters().get(parName).keySet()) {
					if (r.getFormulaSubstratesArrayParameters().get(parName).get(sp).isFilled()) {
						res.put(r.getIdJavaStr() + parName, r.getFormulaSubstratesArrayParameters().get(parName));
					}
				}
			}
		}
		return res;
	}

	public Map<String, SortedMap<String, FormulaArrayValue>> getAllFormulaModifiersArrayValues() {
		Map<String, SortedMap<String, FormulaArrayValue>> res = new HashMap<>();
		for (Reaction r : this) {
			for (String parName : r.getFormulaModifiersArrayParameters().keySet()) {
				for (String sp : r.getFormulaModifiersArrayParameters().get(parName).keySet()) {
					if (r.getFormulaModifiersArrayParameters().get(parName).get(sp).isFilled()) {
						res.put(r.getIdJavaStr() + parName, r.getFormulaModifiersArrayParameters().get(parName));
					}
				}
			}
		}
		return res;
	}

	public void checkMathExpressions(MathLinkOp mathLinkOp) throws Exception, CException {
//		String newVal = null;
		mathLinkOp.checkMultiMathCommands(this.getAllUsedFormulaStringsWithContext());
		try {
			for (String calculatedValue : this.simConfig.getAllMathExpressions()) {
				Utils.evalMathExpr(calculatedValue);
			}
		} catch (Exception e) {
			throw new CException("Sim config");
		}
//		for (String name : speciesConfigMap.keySet()) {
//			Species sp = speciesConfigMap.get(name);
//			newVal = mathLinkOp.checkMathCommand(sp.getConcentration());
//			if (newVal!=null)
//				sp.setCalculatedConcentration(newVal);
//			else
//				throw new CException("Error: Initial concentration of species " + name + " : " + newVal);
//		}
//		for (Reaction r : this) {
//			for (String key : r.getFormulaValues().keySet()) {
//				FormulaValue fv = r.getFormulaValues().get(key);
//				if (fv != null && fv.isFilled() && fv.getType() == FormulaValueType.CONSTANT) {
//					newVal = mathLinkOp.checkMathCommand(fv.getConstantValue());
//					if (newVal !=null)
//						fv.setCalculatedConstantValue(newVal);
//					else
//						throw new CException("Error: Reaction " + r.getIdJavaStr() + " formula paremeter " + key
//								+ " in " + r.getFormula().getFormulaDef() + " : treated val " + newVal + " raw val "+fv.getConstantValue());
//				}
//			}
//		}

//		for (Reaction r : this) {
//			for (String parName : r.getFormulaSubstratesArrayParameters().keySet()) {
//				for (String sp : r.getFormulaSubstratesArrayParameters().get(parName).keySet()) {
//					FormulaArrayValue fav = r.getFormulaSubstratesArrayParameters().get(parName).get(sp);
//					if (fav != null && fav.isFilled()) {
//						newVal = mathLinkOp.checkMathCommand(fav.getValue());
//						if (newVal != null)
//							fav.setCalculatedValue(newVal);
//						else
//							throw new CException("Error: Reaction " + r.getIdJavaStr() + " formula parameter array "
//									+ parName + " substrate " + sp + " in " + r.getFormula().getFormulaDef() + " : "
//									+ newVal);
//					}
//				}
//			}
//		}
//		for (Reaction r : this) {
//			for (String parName : r.getFormulaModifiersArrayParameters().keySet()) {
//				for (String sp : r.getFormulaModifiersArrayParameters().get(parName).keySet()) {
//					FormulaArrayValue fav = r.getFormulaModifiersArrayParameters().get(parName).get(sp);
//					if (fav != null && fav.isFilled()) {
//						newVal = mathLinkOp.checkMathCommand(fav.getValue());
//						if (newVal != null)
//							fav.setCalculatedValue(newVal);
//						else
//							throw new CException("Error: Reaction " + r.getIdJavaStr() + " formula parameter array "
//									+ parName + " modifier " + sp + " in " + r.getFormula().getFormulaDef() + " : "
//									+ newVal);
//					}
//				}
//			}
//		}
	}

//	private String fixStringNumber(String newVal) {
//		if (newVal.endsWith("."))
//			newVal += "0";
//		String res=newVal;
//		try {
//			@SuppressWarnings("unused")
//			BigDecimal bigDec = new BigDecimal(newVal);
//		} catch (Exception e) {
//			return null;
//		}
//		return res;
//	}

	public Formulas getAllUsedFormulas() {
		Formulas res = new Formulas((FormulaType) null);
		for (Reaction r : this) {
			if (r.getFormula() != null)
				res.addFormula(r.getFormula());
		}
		return res;
	}

	public boolean isDBDelete() {
		return isDBDelete;
	}

	public void setDBDelete(boolean isDBDelete) {
		this.isDBDelete = isDBDelete;
	}

	public Formulas getFormulas() {
		return formulas;
	}

	public SortedMap<String, ReactantModelLevel> getReactantsAtModelLevel() {
		// HOR highest order reaction, HSC highest Stoichiometry Coefficient
		SortedMap<String, ReactantModelLevel> res = new TreeMap<>();
		for (Reaction r : this) {
			SortedMap<String, ReactantReactionLevel> reactantsMapByReaction = r.getReactants();
			for (String reactantName : reactantsMapByReaction.keySet()) {
				if (!getAllSpeciesTimeDependent().containsKey(reactantName))
					continue;
				Integer order = reactantsMapByReaction.get(reactantName).getOrder();
				Integer stCoef = reactantsMapByReaction.get(reactantName).getStCoef();
				ReactantModelLevel currentReactantData = res.get(reactantName);
				if ((currentReactantData == null) || currentReactantData.getHOR() < order
						|| (currentReactantData.getHOR() == order && currentReactantData.getHSC() < stCoef))
					res.put(reactantName, new ReactantModelLevel(reactantName, order, stCoef));
			}
		}
		return res;
	}

	public Model getParent() {
		return parent;
	}

	public void setParent(Model parent) {
		this.parent = parent;
	}
}
