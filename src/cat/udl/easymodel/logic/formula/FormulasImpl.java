package cat.udl.easymodel.logic.formula;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import com.vaadin.ui.UI;

import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.utils.p;

public class FormulasImpl extends ArrayList<Formula> implements Formulas {
	private static final long serialVersionUID = 4969553820024899827L;

	private FormulaType formulaType;
	private SessionData sessionData;

	public FormulasImpl(FormulaType fType) {
		super();
		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.formulaType = fType;
	}

	@Override
	public void reset() {
		clear();
	}
	
	@Override
	public boolean addFormula(Formula f) {
		if (f != null) {
			f.setIdJava(this.size() + 1);
			return add(f);
		}
		return false;
	}

	@Override
	public boolean removeFormula(Formula f) {
		if (f != null && remove(f)) {
			int i = 1;
			for (Formula f2 : this)
				f2.setIdJava(i++);
			return true;
		}
		return false;
	}

	@Override
	public boolean hasAnyCompatibleFormula(Reaction r) {
		for (Formula f : this) {
			if (f.isCompatibleWithReaction(r))
				return true;
		}
		return false;
	}

	@Override
	public Formula getFormulaByName(String fName) {
		if (fName != null) {
			for (Formula f : this)
				if (f.getName() != null && f.getName().equals(fName))
					return f;
		}
		return null;
	}
	
	@Override
	public String getNextFormulaNameByModelShortName(String modelShortName) {
		int num, res=0;
		String numSeparator=FormulaUtils.modelShortNameSeparator;
		for (Formula f : this) {
			int splitIndex = f.getName().lastIndexOf(numSeparator);
			if (splitIndex != -1) {
				String relModel = f.getName().substring(0,splitIndex);
				try {
					num = Integer.valueOf(f.getName().substring(splitIndex+numSeparator.length()));
					if (relModel.equals(modelShortName))
						res = num;
				} catch (Exception e) {
					continue;
				}
			}
		}
		return modelShortName+numSeparator+(res+1);
	}

	@Override
	public Formula getFormulaById(int id) {
		for (Formula f : this)
			if (f.getId() == id)
				return f;
		return null;
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
	public void saveDB() throws SQLException {
		if (sessionData.getUser() == null)
			return;
		SharedData sharedData = SharedData.getInstance();
		Connection con = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement, pre2;
		int p;
		boolean found;
		try {
			// DELETE USER DELETED FORMULAS
			preparedStatement = con
					.prepareStatement("SELECT id FROM formula WHERE formulatype=? AND repositorytype=? AND id_user=?");
			p = 1;
			preparedStatement.setInt(p++, FormulaType.CUSTOM.getValue());
			preparedStatement.setInt(p++, RepositoryType.PRIVATE.getValue());
			preparedStatement.setInt(p++, sessionData.getUser().getId());
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				found = false;
				for (Formula f : this) {
					if (f.getId() != null && f.getId() == rs.getInt("id")) {
						found = true;
						break;
					}
				}
				if (!found) {
					pre2 = con.prepareStatement("DELETE FROM formula WHERE id=?");
					pre2.setInt(1, rs.getInt("id"));
					pre2.executeUpdate();
					pre2.close();
				}
			}
			rs.close();
			preparedStatement.close();
			// UPDATE/INSERT
			for (Formula f : this) {
				if (!f.isValid())
					continue;
				if (f.getFormulaType() == FormulaType.PREDEFINED || f.getFormulaType() == FormulaType.CUSTOM
						&& f.getUser() == sessionData.getUser() && f.getRepositoryType() == RepositoryType.PRIVATE) {
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
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	@Override
	public void loadDB() throws SQLException {
		SharedData sharedData = SharedData.getInstance();
		Connection con = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement = null, pre2;
		String query;
		try {
			if (sessionData.getUser() != null)
				query = "SELECT id,id_user,name,formula,onesubstrateonly,noproducts,onemodifieronly,formulatype,repositorytype FROM formula WHERE formulatype=? AND (repositorytype=? OR repositorytype=? AND id_user=?)";
			else
				query = "SELECT id,id_user,name,formula,onesubstrateonly,noproducts,onemodifieronly,formulatype,repositorytype FROM formula WHERE formulatype=? AND repositorytype=?";
			preparedStatement = con.prepareStatement(query);
			int p = 1;
			preparedStatement.setInt(p++, this.getFormulaType().getValue());
			preparedStatement.setInt(p++, RepositoryType.PUBLIC.getValue());
			if (sessionData.getUser() != null) {
				preparedStatement.setInt(p++, RepositoryType.PRIVATE.getValue());
				preparedStatement.setInt(p++, sessionData.getUser().getId());
			}
			ResultSet rs = preparedStatement.executeQuery();
			this.clear();
			while (rs.next()) {
				Formula f = new FormulaImpl(rs.getString("name"), rs.getString("formula"),
						FormulaType.fromInt(rs.getInt("formulatype")), sharedData.getUserById(rs.getInt("id_user")),
						RepositoryType.fromInt(rs.getInt("repositorytype")));
				f.setId(rs.getInt("id"));
				f.setOneSubstrateOnly(Utils.intToBool(rs.getInt("onesubstrateonly")));
				f.setNoProducts(Utils.intToBool(rs.getInt("noproducts")));
				f.setOneModifierOnly(Utils.intToBool(rs.getInt("onemodifieronly")));
				
				// GENERIC PARAMETERS TYPES
				pre2 = con.prepareStatement(
						"SELECT `id`, `id_formula`, `genparam`, `formulavaluetype` FROM `formulagenparam` WHERE id_formula=?");
				pre2.setInt(1, f.getId());
				ResultSet rs2 = pre2.executeQuery();
				while (rs2.next()) {
					f.setTypeOfGenericParameter(rs2.getString("genparam"), FormulaValueType.fromInt(rs2.getInt("formulavaluetype")));
				}
				rs2.close();
				pre2.close();
				
				this.addFormula(f);
			}
			rs.close();
			preparedStatement.close();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}
}
