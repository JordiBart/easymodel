package cat.udl.easymodel.logic.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import com.vaadin.ui.UI;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.types.SpeciesVarTypeType;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;

public class ModelsImpl extends ArrayList<Model> implements Models {
	private static final long serialVersionUID = 1L;
	private SessionData sessionData;

	public ModelsImpl() {
		this.sessionData = (SessionData) UI.getCurrent().getData();
	}

	@Override
	public Model getModelByName(String name) {
		if (name != null) {
			for (Model mod : this)
				if (name.equals(mod.getName()))
					return mod;
		}
		return null;
	}

	@Override
	public Model getModelByIdJava(int id) {
		for (Model mod : this)
			if (mod.getIdJava() == id)
				return mod;
		return null;
	}

	@Override
	public boolean addModel(Model m) {
		if (m != null && !this.contains(m)) {
			m.setIdJava(this.size() + 1);
			return this.add(m);
		}
		return false;
	}

	@Override
	public boolean removeModel(Model mod) {
		if (mod != null && remove(mod)) {
			mod.resetReactions();
			int i = 1;
			for (Model m2 : this)
				m2.setIdJava(i++);
			return true;
		}
		return false;
	}

	@Override
	public void resetModels() {
		for (Model mod : this)
			mod.resetReactions();
		this.clear();
		// modelsAutoIncrement = 1;
	}

	@Override
	public void removeFormulaFromReactions(Formula f) {
		for (Model mod : this) {
			mod.removeFormula(f);
		}
	}

	@Override
	public void reset() {
		this.clear();
	}

	@Override
	public void semiLoadDB() throws SQLException {
		// gets basic data only to perform full load later
		SharedData sharedData = SharedData.getInstance();
		Connection conn = sharedData.getDbManager().getCon();
		PreparedStatement pre;
		ResultSet rs;
		String qry;
		try {
			qry = "SELECT `id`, `id_user`, `name`, `description`, `repositorytype` FROM model";
			pre = conn.prepareStatement(qry);
//			int p = 1;
//			pre.setInt(p++, RepositoryType.PUBLIC.getValue());
			rs = pre.executeQuery();
			this.reset();
			while (rs.next()) {
				Model m = new ModelImpl();
				m.setId(rs.getInt("id"));
				m.setUser(sharedData.getUserById(rs.getInt("id_user")));
				m.setName(rs.getString("name"));
				m.setDescription(rs.getString("description"));
				m.setRepositoryType(RepositoryType.fromInt(rs.getInt("repositorytype")));
				this.addModel(m);
			}
			rs.close();
			pre.close();
			Collections.sort(this, new ModelComparator());
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}
}
