package cat.udl.easymodel.logic.formula;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.utils.p;

public class Formulas extends ArrayList<Formula> {
	private FormulaType formulaType=null; // null: mixed types of formulas 
	private Model model = null;
	// dynamic data
	private ArrayList<Integer> formulaIdsToDeleteFromDB = new ArrayList<>();

	public Formulas(FormulaType ft) {
		super();
//		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.formulaType = ft;
	}

	public Formulas(Model model) {
		super();
//		this.sessionData = (SessionData) UI.getCurrent().getData();
		this.formulaType = FormulaType.MODEL;
		this.model = model;
	}
	
	public Formulas(Formulas from) {
		super();
		formulaType=from.getFormulaType(); 
		model = null;
		for (Formula f : from)
			this.addFormula(new Formula(f));
	}

	public void reset() {
		for (Formula f : this)
			f.reset();
		this.clear();
		formulaIdsToDeleteFromDB.clear();
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public boolean addFormula(Formula f) {
		if (f != null && getFormulaByName(f.getNameRaw()) == null) {
			boolean isDirty = f.isDirty();
			f.setIdJava(this.size() + 1);
			f.setModel(this.model);
			f.setDirty(isDirty);
			return add(f);
		}
		return false;
	}

	public boolean removeFormula(Formula f) {
		if (f != null && remove(f)) {
			if (f.getId() != null)
				formulaIdsToDeleteFromDB.add(f.getId());
			int i = 1;
			for (Formula f2 : this)
				f2.setIdJava(i++);
			return true;
		}
		return false;
	}

	public Formulas getFormulasCompatibleWithReaction(Reaction r) {
		if (model == null)
			return null;
		Formulas res = new Formulas(model);
		for (Formula f : this)
			if (f.parse() && f.isCompatibleWithReaction(r))
				res.add(f);
		return res;
	}

	public Formula getFormulaByName(String fName) {
		if (fName != null) {
			for (Formula f : this)
				if (f.getNameRaw() != null && f.getNameRaw().equals(fName))
					return f;
		}
		return null;
	}

	public String getNextFormulaNameByModelShortName() {
		String modelShortName = null;
		if (model == null)
			modelShortName = "EZM";
		else
			modelShortName = model.getNameShort();
		int num, res = 0;
		String numSeparator = FormulaUtils.modelShortNameSeparator;
		for (Formula f : this) {
			int splitIndex = f.getNameRaw().lastIndexOf(numSeparator);
			if (splitIndex != -1) {
				String relModel = f.getNameRaw().substring(0, splitIndex);
				try {
					num = Integer.valueOf(f.getNameRaw().substring(splitIndex + numSeparator.length()));
					if (relModel.equals(modelShortName) && num>res)
						res = num;
				} catch (Exception e) {
					continue;
				}
			}
		}
		return modelShortName + numSeparator + (res + 1);
	}

	public Formula getFormulaById(int id) {
		for (Formula f : this)
			if (f.getId() == id)
				return f;
		return null;
	}

	public FormulaType getFormulaType() {
		return formulaType;
	}

	public void setFormulaType(FormulaType formulaType) {
		this.formulaType = formulaType;
	}

	/**
	 * only callable from a GENERIC type Formulas
	 */
	public void mergeGenericFormulasFrom(ArrayList<Formula> from) {
		if (this.formulaType != FormulaType.GENERIC)
			return;
		for (Formula f1 : from) {
			if (f1.getFormulaType() != FormulaType.MODEL || f1.getFormulaDef().contains(":"))
				continue;
			boolean found = false;
			for (Formula f2 : this) {
				if (f1.isEquivalentTo(f2)) {
					found = true;
					break;
				}
			}
			if (!found) {
				Formula f3 = new Formula(f1);
//				System.out.println(f3.getFormulaType().toString());
				f3.setName(getNextGenericFormulaName());
				f3.setFormulaDef(f3.getGenericFormulaDef());
				f3.setId(null);
				f3.setFormulaType(FormulaType.GENERIC);
				f3.setModel(null);
				f3.setDirty(true);
				this.addFormula(f3);
//				System.out.println(f1.getFormulaDef()+" -> "+f3.getFormulaDef());
			}
		}
//		p.p("original: " + from.size() + " compacted: " + this.size());
//		int numOfEq = 0;
//		try {
//			BufferedWriter bw = new BufferedWriter(new FileWriter(SharedData.appDir + "/compacted_formulas.txt"));
//			for (Formula f2 : this) {
//				bw.write("ORIGINAL " + f2.getFormulaDef() + "\n");
//				bw.write("TRANSFORMED " + f2.getGenericFormulaDef() + "\n");
//				Formula eF = new Formula(f2);
//				eF.setFormulaDef(eF.getGenericFormulaDef());
//				if (eF.isEquivalentTo(f2))
//					numOfEq++;
//			}
//			bw.write("TOTAL NUMBER OF ORIGINAL=" + this.size() + "\n");
//			bw.write("TOTAL NUMBER OF EQUIVALENT TRANSFORMED=" + numOfEq + "\n");
//			bw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	private String getNextGenericFormulaName() {
		String genericFormulaPrefix = FormulaUtils.genericFormulaPrefix;
		int num, res = 0;
		for (Formula f : this) {
			int splitIndex = f.getNameRaw().lastIndexOf(genericFormulaPrefix);
			if (splitIndex != -1) {
				try {
					num = Integer.valueOf(f.getNameRaw().substring(splitIndex + genericFormulaPrefix.length()));
					res = num;
				} catch (Exception e) {
					continue;
				}
			}
		}
		return genericFormulaPrefix + (res + 1);
	}
	
	public void saveDB() throws SQLException {
//		for (Formula f : this)
//			p.p(f.getModel());
//		if (!(getFormulaType() == FormulaType.PREDEFINED || getFormulaType() == FormulaType.MODEL && model != null
//				&& model.getId() != null && model.getRepositoryType() == RepositoryType.PRIVATE))
//			return;
		SharedData sharedData = SharedData.getInstance();
		Connection con = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement, pre2;
		int p;
		try {
			// DELETE USER DELETED FORMULAS
			for (Integer idDel : formulaIdsToDeleteFromDB) {
				pre2 = con.prepareStatement("DELETE FROM formula WHERE id=?");
				pre2.setInt(1, idDel);
				pre2.executeUpdate();
				pre2.close();
			}
			// UPDATE/INSERT
			for (Formula f : this) {
				if (!f.isValid() || !f.isDirty())
					continue;
				p = 1;
				if (f.getId() != null) {
//					System.out.println("db f update");
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
//					System.out.println("db f insert model "+f.getModel()+" name"+f.getNameToShow());
					preparedStatement = con.prepareStatement(
							"insert into formula (id,id_model,name,formula,onesubstrateonly,noproducts,onemodifieronly,formulatype)"
									+ " values (NULL, ?, ?, ?, ?, ?,?,?)",
							Statement.RETURN_GENERATED_KEYS);
					if (f.getModel() != null)
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
//				System.out.println("db f id:"+f.getId());
	
				f.setDirty(false);
			}
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	public void loadDB() throws SQLException {
		if (getFormulaType() == FormulaType.MODEL && (getModel() == null || getModel().getId() == null))
			return;
		SharedData sharedData = SharedData.getInstance();
		Connection con = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement = null, pre2;
		String query = null;
		try {
			if (getFormulaType() == FormulaType.MODEL)
				query = "SELECT id,name,formula,onesubstrateonly,noproducts,onemodifieronly,formulatype FROM formula WHERE formulatype=? AND id_model=?";
			else
				query = "SELECT id,name,formula,onesubstrateonly,noproducts,onemodifieronly,formulatype FROM formula WHERE formulatype=?";
			preparedStatement = con.prepareStatement(query);
			int p = 1;
			preparedStatement.setInt(p++, this.getFormulaType().getValue());
			if (getFormulaType() == FormulaType.MODEL)
				preparedStatement.setInt(p++, getModel().getId());
			ResultSet rs = preparedStatement.executeQuery();
			this.clear();
			while (rs.next()) {
				Formula f = new Formula(rs.getString("name"), rs.getString("formula"),
						FormulaType.fromInt(rs.getInt("formulatype")), model);
				f.setId(rs.getInt("id"));
				f.setOneSubstrateOnly(Utils.intToBool(rs.getInt("onesubstrateonly")));
				f.setNoProducts(Utils.intToBool(rs.getInt("noproducts")));
				f.setOneModifierOnly(Utils.intToBool(rs.getInt("onemodifieronly")));

				this.addFormula(f);
				f.setDirty(false);
			}
			rs.close();
			preparedStatement.close();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	public void resetAllIds() {
		for (Formula f : this) {
			f.setId(null);
			f.setDirty(true);
		}
	}
}
