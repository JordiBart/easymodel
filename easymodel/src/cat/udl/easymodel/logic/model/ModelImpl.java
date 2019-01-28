package cat.udl.easymodel.logic.model;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.Formulas;
import cat.udl.easymodel.logic.formula.FormulasImpl;
import cat.udl.easymodel.logic.simconfig.SimConfig;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.types.SpeciesVarTypeType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathLink;
import cat.udl.easymodel.utils.CException;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.common.SpacedLabel;
import javafx.beans.binding.IntegerExpression;

public class ModelImpl extends ArrayList<Reaction> implements Model {
	private static final long serialVersionUID = 1L;

	private int idJava;
	private Integer id = null;
	// private int reactionsAutoIncrement = 1;
	// Species Values (concentrations)
	private SortedMap<String, Species> speciesConfigMap = new TreeMap<>();
	private SimConfig simConfig = new SimConfig();
	private String name = "";
	private String description = "";
	private RepositoryType repositoryType = RepositoryType.PRIVATE;
	private User user;
	private SessionData sessionData;

	public ModelImpl() {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.reset();
	}

	// @Override
	// public String toString() {
	// return String.valueOf(System.identityHashCode(this));
	// }

	@Override
	public String getUserName() {
		if (getUser() != null) {
			return getUser().getName();
		} else {
			return "unknown";
		}
	}

	@Override
	public boolean equals(Object in) {
		if (in != null && in instanceof Model && System.identityHashCode(this) == System.identityHashCode(in))
			return true;
		return false;
	}

	@Override
	public void reset() {
		this.clear();
		speciesConfigMap.clear();
		simConfig.reset();
		name = "";
		description = "";
		repositoryType = RepositoryType.PRIVATE;
		user = null;

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getNameShort() {
		int numChars = 8;
		if (getName().length() >= numChars)
			return getName().replaceAll("\\s", "_").substring(0, numChars);
		else
			return getName().replaceAll("\\s", "_");
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int getIdJava() {
		return idJava;
	}

	@Override
	public void setIdJava(int id) {
		this.idJava = id;
	}

	@Override
	public boolean addReaction(Reaction react) {
		if (react != null) {
			react.setIdJava(this.size() + 1);
			return this.add(react);
		}
		return false;
	}

	@Override
	public ArrayList<Reaction> getValidReactions() {
		ArrayList<Reaction> l = new ArrayList<>();
		for (Reaction react : this)
			if (react.isValid())
				l.add(react);
		Collections.sort(l);
		return l;
	}

	@Override
	public boolean removeReaction(Reaction react) {
		if (react != null && remove(react)) {
			int i = 1;
			for (Reaction r2 : this)
				r2.setIdJava(i++);
			return true;
		}
		return false;
	}

	@Override
	public void resetReactions() {
		this.clear();
		// reactionsAutoIncrement = 1;
	}

	@Override
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

	@Override
	public ArrayList<String> getAllUsedFormulaStringsWithContext() {
		ArrayList<String> res = new ArrayList<>();
		for (Reaction r : this) {
			if (r.getFormula() != null)
				res.add(r.getFormula().getMathematicaReadyFormula(r.getMathematicaContext(), this));
		}
		return res;
	}

	@Override
	public SortedMap<String, String> getAllSpeciesTimeDependent() {
		// if (!checkReactions())
		// return null;
		SortedMap<String, String> allParticipants = new TreeMap<String, String>();
		for (Reaction r : this)
			for (String sp : r.getBothSides().keySet())
				if (getAllSpecies().get(sp).getVarType() == SpeciesVarTypeType.TIMEDEP)
					allParticipants.put(sp, null);
		return allParticipants;
	}

	@Override
	public SortedMap<String, String> getAllSpeciesConstant() {
		SortedMap<String, String> indSpecies = new TreeMap<String, String>();
		for (String sp : getAllSpecies().keySet()) // modifiers can be indep vars
			if (getAllSpecies().get(sp).getVarType() == SpeciesVarTypeType.INDEP)
				indSpecies.put(sp, null);
		return indSpecies;
	}

	@Override
	public SortedMap<String, String> getAllSpeciesExceptModifiers() {
		// if (!checkReactions())
		// return null;
		SortedMap<String, String> allParticipants = new TreeMap<String, String>();
		for (Reaction r : this)
			for (String sp : r.getBothSides().keySet())
				allParticipants.put(sp, null);
		return allParticipants;
	}

	@Override
	public SortedMap<String, String> getAllSubstrates() {
		// if (!checkReactions())
		// return null;
		SortedMap<String, String> allSubstrates = new TreeMap<String, String>();
		for (Reaction r : this)
			for (String sp : r.getLeftPartSpecies().keySet())
				allSubstrates.put(sp, null);
		return allSubstrates;
	}

	@Override
	public SortedMap<String, String> getAllProducts() {
		// if (!checkReactions())
		// return null;
		SortedMap<String, String> allProducts = new TreeMap<String, String>();
		for (Reaction r : this)
			for (String sp : r.getRightPartSpecies().keySet())
				allProducts.put(sp, null);
		return allProducts;
	}

	@Override
	public SortedMap<String, String> getAllModifiers() {
		// if (!checkReactions())
		// return null;
		SortedMap<String, String> allModifiers = new TreeMap<String, String>();
		for (Reaction r : this)
			for (String sp : r.getModifiers().keySet())
				allModifiers.put(sp, null);
		return allModifiers;
	}

	@Override
	public SortedMap<String, Species> getAllSpecies() {
		// if (!checkReactions())
		// return null;
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
					speciesConfigMap.put(sp, new SpeciesImpl());
			for (String sp : react.getModifiers().keySet())
				if (!speciesConfigMap.containsKey(sp))
					speciesConfigMap.put(sp, new SpeciesImpl());
		}
		// Old species values are still there
		return speciesConfigMap;
	}

	@Override
	public boolean isAllSpeciesSet() {
		for (String sp : getAllSpecies().keySet())
			if (getAllSpecies().get(sp) == null || !getAllSpecies().get(sp).isSet())
				return false;
		return true;
	}

	@Override
	public String getStoichiometricMatrix() throws Exception {
		checkReactions();
		if (getValidReactions().isEmpty())
			throw new Exception("All reactions are empty");
		String matrix = "{";
		boolean first1 = true;
		boolean first2;
		for (String species : getAllSpeciesTimeDependent().keySet()) {
			matrix += (first1 ? "" : ",");
			matrix += "{";
			first2 = true;
			for (Reaction react : getValidReactions()) {
				matrix += (first2 ? "" : ",");
				matrix += (react.getSpeciesValuesForStMatrix().containsKey(species)
						? String.valueOf(react.getSpeciesValuesForStMatrix().get(species))
						: "0");
				first2 = false;
			}
			matrix += "}";
			first1 = false;
		}
		matrix += "}";
		return matrix;
	}

	@Override
	public GridLayout getDisplayStoichiometricMatrix() throws Exception {
		checkReactions();
		if (getValidReactions().isEmpty())
			throw new Exception("All reactions are empty");
		GridLayout gl = new GridLayout(getValidReactions().size() + 1, getAllSpeciesTimeDependent().size() + 1);
		gl.addStyleName("table");
		gl.setSpacing(false);
		gl.setMargin(false);
		if (gl.getColumns() > 12)
			gl.setSizeFull();
		gl.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		gl.addComponent(new SpacedLabel(""), 0, 0);
		int i = 1, j = 0;
		for (Reaction react : getValidReactions()) {
			gl.addComponent(new SpacedLabel(react.getIdJavaStr()), i++, 0);
		}
		i = 0;
		j++;
		for (String species : getAllSpeciesTimeDependent().keySet()) {
			gl.addComponent(new SpacedLabel(species), i++, j);
			for (Reaction react : getValidReactions()) {
				gl.addComponent(new SpacedLabel((react.getSpeciesValuesForStMatrix().containsKey(species)
						? String.valueOf(react.getSpeciesValuesForStMatrix().get(species))
						: "0")), i++, j);
			}
			i = 0;
			j++;
		}
		return gl;
	}

	@Override
	public String getRegulatoryMatrix() throws Exception {
		checkReactions();
		if (getValidReactions().isEmpty())
			throw new Exception("All reactions are empty");
		String matrix = "{";
		boolean first1 = true;
		boolean first2;
		for (String mod : getAllSpeciesTimeDependent().keySet()) {
			matrix += (first1 ? "" : ",");
			matrix += "{";
			first2 = true;
			for (Reaction react : getValidReactions()) {
				matrix += (first2 ? "" : ",");
				if (react.getModifiers().keySet().contains(mod)) {
					if (react.getModifiers().get(mod) != null && react.getModifiers().get(mod) == 1)
						matrix += "1";
					else
						matrix += "-1";
				} else
					matrix += "0";
				first2 = false;
			}
			matrix += "}";
			first1 = false;
		}
		for (String mod : getAllModifiers().keySet()) {
			if (!getAllSpeciesTimeDependent().containsKey(mod)) {
				matrix += (first1 ? "" : ",");
				matrix += "{";
				first2 = true;
				for (Reaction react : getValidReactions()) {
					matrix += (first2 ? "" : ",");
					if (react.getModifiers().keySet().contains(mod)) {
						if (react.getModifiers().get(mod) != null && react.getModifiers().get(mod) == 1)
							matrix += "1";
						else
							matrix += "-1";
					} else
						matrix += "0";
					first2 = false;
				}
				matrix += "}";
				first1 = false;
			}
		}
		matrix += "}";
		return matrix;
	}

	private int getDisplayRegulatoryMatrixNumOfRows() {
		int total = getAllSpeciesTimeDependent().size() + 1;
		for (String mod : getAllModifiers().keySet())
			if (!getAllSpeciesTimeDependent().containsKey(mod))
				total++;
		return total;
	}

	@Override
	public GridLayout getDisplayRegulatoryMatrix() throws Exception {
		checkReactions();
		if (getValidReactions().isEmpty())
			throw new Exception("All reactions are empty");
		// if (getAllModifiers().isEmpty())
		// throw new Exception("Empty regulatory matrix (there are no modifiers)");
		GridLayout gl = new GridLayout(getValidReactions().size() + 1, getDisplayRegulatoryMatrixNumOfRows());
		gl.addStyleName("table");
		gl.setSpacing(false);
		gl.setMargin(false);
		if (gl.getColumns() > 12)
			gl.setSizeFull();
		gl.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		gl.addComponent(new SpacedLabel(""), 0, 0);
		int i = 1, j = 0;
		for (Reaction react : getValidReactions())
			gl.addComponent(new SpacedLabel(react.getIdJavaStr()), i++, 0);
		i = 0;
		j++;
		for (String mod : getAllSpeciesTimeDependent().keySet()) {
			Label modLabel = new SpacedLabel(mod);
			gl.addComponent(modLabel, i++, j);
			gl.setComponentAlignment(modLabel, Alignment.MIDDLE_LEFT);
			for (Reaction react : getValidReactions()) {
				if (react.getModifiers().keySet().contains(mod)) {
					if (react.getModifiers().get(mod) != null && react.getModifiers().get(mod) == 1)
						gl.addComponent(new SpacedLabel("1"), i++, j);
					else
						gl.addComponent(new SpacedLabel("-1"), i++, j);
				} else
					gl.addComponent(new SpacedLabel("0"), i++, j);
			}
			i = 0;
			j++;
		}
		for (String mod : getAllModifiers().keySet()) {
			if (!getAllSpeciesTimeDependent().containsKey(mod)) {
				Label modLabel = new SpacedLabel(mod);
				gl.addComponent(modLabel, i++, j);
				gl.setComponentAlignment(modLabel, Alignment.MIDDLE_LEFT);
				for (Reaction react : getValidReactions()) {
					if (react.getModifiers().keySet().contains(mod)) {
						if (react.getModifiers().get(mod) != null && react.getModifiers().get(mod) == 1)
							gl.addComponent(new SpacedLabel("1"), i++, j);
						else
							gl.addComponent(new SpacedLabel("-1"), i++, j);
					} else
						gl.addComponent(new SpacedLabel("0"), i++, j);
				}
				i = 0;
				j++;
			}
		}
		return gl;
	}

	@Override
	public void removeFormula(Formula f) {
		for (Reaction r : this) {
			if (r.getFormula() == f)
				r.setFormula(null);
		}
	}

	@Override
	public void checkIfReadyToSimulate() throws CException {
		try {
			checkValidModel();
			simConfig.checkSimConfigs();
		} catch (Exception e) {
			throw new CException(e.getMessage());
		}
	}

	@Override
	public void checkValidModel() throws Exception {
		cleanModel();
		String err = "";
		int numErr = 0;
		if (this.name == null || this.name.equals("")) {
			err += "Model must have a name\n";
			numErr++;
		}
		if (this.getRepositoryType() != RepositoryType.TEMP && sessionData.getModels().getModelByName(this.name) != null
				&& sessionData.getModels().getModelByName(this.name) != this) {
			err += "Model name is already in use\n";
			numErr++;
		}
		if (this.user == null || getUser().getId() == null) {
			err += "Model must have a valid user\n";
			numErr++;
		}
		if (this.size() == 0) {
			err += "Model must have reactions\n";
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
				err += "Reaction " + r.getIdJavaStr() + ": No Rate is bound\n";
				numErr++;
			}
			if (!r.areFormulaValuesValid()) {
				err += "Reaction " + r.getIdJavaStr() + ": Missing Rate values\n";
				numErr++;
			}
		}
		if (numErr > 0)
			throw new Exception(err);
	}

	private void cleanModel() {
		// vars only modifiers can't be time dependent
		for (String sp : this.getAllSpecies().keySet()) {
			if (!this.getAllSpeciesExceptModifiers().containsKey(sp)) {
				this.getAllSpecies().get(sp).setVarType(SpeciesVarTypeType.INDEP);
			}
		}
	}

	@Override
	public SimConfig getSimConfig() {
		return simConfig;
	}

	@Override
	public RepositoryType getRepositoryType() {
		return repositoryType;
	}

	@Override
	public void setRepositoryType(RepositoryType repositoryType) {
		this.repositoryType = repositoryType;
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
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public void saveDB() throws SQLException {
		SharedData sharedData = SharedData.getInstance();
		Connection conn = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement = null;
		String table;
		Integer tmpId = 0;
		try {
			// REMOVE FULL MODEL (INNODB CASCADING APPLIED)
			if (this.id != null) {
				preparedStatement = conn.prepareStatement("DELETE FROM model WHERE id=?");
				preparedStatement.setInt(1, this.id);
				preparedStatement.executeUpdate();
			}
			// INSERT FULL MODEL
			// insert to model table
			table = "model";
			preparedStatement = conn.prepareStatement("insert into " + table
					+ " (id, id_user, name, description, repositorytype, modified) values (?, ?, ?, ?, ?, DATE(NOW()))",
					Statement.RETURN_GENERATED_KEYS);
			int p = 1;
			if (this.id != null)
				preparedStatement.setInt(p++, this.id);
			else
				preparedStatement.setNull(p++, java.sql.Types.INTEGER);
			preparedStatement.setInt(p++, user.getId());
			preparedStatement.setString(p++, getName());
			preparedStatement.setString(p++, getDescription());
			preparedStatement.setInt(p++, RepositoryType.PRIVATE.getValue());
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
						+ " (`id`, `id_model`, `species`, `concentration`, `vartype`, `stochastic`, `amount`) values (NULL, ?, ?, ?, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
				p = 1;
				preparedStatement.setInt(p++, this.getId());
				preparedStatement.setString(p++, sp);
				preparedStatement.setString(p++, speciesConfigMap.get(sp).getConcentration());
				preparedStatement.setInt(p++, speciesConfigMap.get(sp).getVarType().getValue());
				preparedStatement.setInt(p++, Utils.boolToInt(speciesConfigMap.get(sp).isStochastic()));
				preparedStatement.setString(p++, speciesConfigMap.get(sp).getAmount());
				affectedRows = preparedStatement.executeUpdate();
				if (affectedRows == 0)
					throw new SQLException("Creating " + table + " failed, no rows affected.");
			}
			preparedStatement.close();
			// save to reaction table
			for (Reaction r : this) {
				table = "reaction";
				preparedStatement = conn.prepareStatement(
						"insert into " + table + " (`id`, `id_model`, `id_formula`, `reaction`) values (NULL, ?, ?, ?)",
						Statement.RETURN_GENERATED_KEYS);
				p = 1;
				preparedStatement.setInt(p++, this.getId());
				preparedStatement.setInt(p++, r.getFormula().getId());
				preparedStatement.setString(p++, r.getReactionStr());
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
				for (String constName : r.getFormulaValues().keySet()) {
					table = "formulavalue";
					FormulaValue fv = r.getFormulaValues().get(constName);
					preparedStatement = conn.prepareStatement("insert into " + table
							+ " (`id`, `id_reaction`, `constant`, `formulavaluetype`, `constantvalue`, `substratevalue`, `modifiervalue`) values (NULL, ?, ?, ?, ?,?,?)",
							Statement.RETURN_GENERATED_KEYS);
					p = 1;
					preparedStatement.setInt(p++, r.getId());
					preparedStatement.setString(p++, constName);
					preparedStatement.setInt(p++, fv.getType().getValue());
					if (fv.getType() == FormulaValueType.CONSTANT) {
						preparedStatement.setString(p++, fv.getConstantValue());
						preparedStatement.setNull(p++, Types.VARCHAR);
						preparedStatement.setNull(p++, Types.VARCHAR);
					} else if (fv.getType() == FormulaValueType.SUBSTRATE) {
						preparedStatement.setNull(p++, Types.VARCHAR);
						preparedStatement.setString(p++, fv.getSubstrateValue());
						preparedStatement.setNull(p++, Types.VARCHAR);
					} else if (fv.getType() == FormulaValueType.MODIFIER) {
						preparedStatement.setNull(p++, Types.VARCHAR);
						preparedStatement.setNull(p++, Types.VARCHAR);
						preparedStatement.setString(p++, fv.getModifierValue());
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
								+ " (`id`, `id_formulasubstratesarray`, `species`, `value`) values (NULL, ?, ?,?)",
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
			System.out.println(e.getMessage());
			throw e;
		}
	}

	@Override
	public void loadDB() throws SQLException {
		// load model by id
		if (this.id == null)
			throw new SQLException("Model has no id");
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
					"SELECT `id`, `id_user`, `name`, `description`, `repositorytype`, `modified` FROM " + table
							+ " WHERE id=?");
			pre.setInt(1, this.id);
			rs = pre.executeQuery();
			this.reset(); // if id found, reset this model to fill
			while (rs.next()) {
				setId(rs.getInt("id"));
				setUser(sharedData.getUserById(rs.getInt("id_user")));
				setName(rs.getString("name"));
				setDescription(rs.getString("description"));
				setRepositoryType(RepositoryType.fromInt(rs.getInt("repositorytype")));
			}
			rs.close();
			pre.close();
			// species table
			table = "species";
			pre = conn.prepareStatement("SELECT `id`, `id_model`, `species`, `concentration`, `vartype`, `stochastic`, `amount` FROM "+table+" WHERE id_model=?");
			pre.setInt(1, this.id);
			rs = pre.executeQuery();
			while (rs.next()) {
				Species spObj = new SpeciesImpl();
				spObj.setConcentration(rs.getString("concentration"));
				spObj.setVarType(SpeciesVarTypeType.fromInt(rs.getInt("vartype")));
				spObj.setStochastic(Utils.intToBool(rs.getInt("stochastic")));
				spObj.setAmount(rs.getString("amount"));
				this.speciesConfigMap.put(rs.getString("species"), spObj);
			}
			rs.close();
			pre.close();
			// reaction table
			table = "reaction";
			pre = conn.prepareStatement(
					"SELECT `id`, `id_model`, `id_formula`, `reaction` FROM " + table + " WHERE id_model=?");
			pre.setInt(1, this.id);
			rs = pre.executeQuery();
			while (rs.next()) {
				Reaction r = new ReactionImpl();
				r.setId(rs.getInt("id"));
				r.setReactionStr(rs.getString("reaction"));
				r.setFormula(sessionData.getFormulaById(rs.getInt("id_formula")));

				table = "formulavalue";
				pre2 = conn.prepareStatement(
						"SELECT `id`, `id_reaction`, `constant`, `formulavaluetype`, `constantvalue`, `substratevalue`, `modifiervalue` FROM "
								+ table + " WHERE id_reaction=?");
				pre2.setInt(1, r.getId());
				rs2 = pre2.executeQuery();
				while (rs2.next()) {
					FormulaValue fv = new FormulaValueImpl();
					FormulaValueType fvt = FormulaValueType.fromInt(rs2.getInt("formulavaluetype"));
					fv.setType(fvt);
					fv.setConstantValue(rs2.getString("constantvalue"));
					fv.setSubstrateValue(rs2.getString("substratevalue"));
					fv.setModifierValue(rs2.getString("modifiervalue"));
					r.getFormulaValuesRAW().put(rs2.getString("constant"), fv);
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
			throw e;
		}
	}

	@Override
	public void deleteDB() throws SQLException {
		if (this.id == null)
			throw new SQLException("Model has no id");
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
			throw e;
		}
	}

	@Override
	public SortedMap<String, FormulaValue> getAllFormulaParameters() {
		SortedMap<String, FormulaValue> res = new TreeMap<>();
		for (Reaction r : this) {
			for (String parName : r.getFormulaValues().keySet()) {
				if (r.getFormulaValues().get(parName) != null && r.getFormulaValues().get(parName).isFilled()) {
					res.put(r.getIdJavaStr() + parName, r.getFormulaValues().get(parName));
				}
			}
		}
		return res;
	}

	@Override
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

	@Override
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

	@Override
	public void checkMathExpressions(MathLink mathLinkOp) throws MathLinkException, CException {
		String newVal = null;
		mathLinkOp.checkMultiMathCommands(this.getAllUsedFormulaStringsWithContext());
		mathLinkOp.checkMultiMathCommands(this.simConfig.getAllMathExpressions());
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
	@Override
	public boolean isStochastic() {
		for (Species sp : speciesConfigMap.values()) {
			if (sp.isStochastic())
				return true;
		}
		return false;
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

	@Override
	public Formulas getAllUsedFormulas() {
		Formulas res = new FormulasImpl();
		for (Reaction r: this) {
			if (r.getFormula() != null)
				res.addFormula(r.getFormula());
		}
		return res;
	}
}
