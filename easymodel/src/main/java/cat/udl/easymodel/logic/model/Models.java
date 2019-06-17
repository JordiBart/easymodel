package cat.udl.easymodel.logic.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import com.vaadin.ui.UI;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.p;

public class Models extends ArrayList<Model> {
	private static final long serialVersionUID = 1L;
	private SessionData sessionData;

	public Models() {
		this.sessionData = (SessionData) UI.getCurrent().getData();
	}

	public Model getModelByName(String name) {
		if (name != null) {
			for (Model mod : this)
				if (name.equals(mod.getName()))
					return mod;
		}
		return null;
	}

	public Model getModelById(int id) {
		for (Model mod : this)
			if (mod.getId() == id)
				return mod;
		return null;
	}

	public Model getModelByIdJava(int id) {
		for (Model mod : this)
			if (mod.getIdJava() == id)
				return mod;
		return null;
	}

	public boolean addModel(Model m) {
		if (m != null && !this.contains(m)) {
			m.setIdJava(this.size() + 1);
			return this.add(m);
		}
		return false;
	}

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

	public void resetModels() {
		for (Model mod : this)
			mod.resetReactions();
		this.clear();
		// modelsAutoIncrement = 1;
	}

	public void removeFormulaFromReactions(Formula f) {
		for (Model mod : this) {
			mod.removeFormula(f);
		}
	}

	public void reset() {
		this.clear();
	}

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
				Model m = new Model();
				m.setId(rs.getInt("id"));
				m.setUser(sharedData.getUsers().getUserById(rs.getInt("id_user")));
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

	public Model getPrivateModelCopy(Model m, User user) throws SQLException {
		if (m == null || m.getId() == null || user == null)
			return null;
		Model copy = new Model(m);
		copy.loadDB();
		copy.setId(null);
		copy.setParent(null);
		copy.setName(getNextModelName(copy.getName(), user.getName()));
		copy.setRepositoryType(RepositoryType.PRIVATE);
		copy.setUser(user);
		return copy;
	}
	
	public String getNextModelName(String modelName, String userName) {
		int nextNum=1;
		String nextModelNameBase=modelName+" "+userName+"-variant#";
		boolean found = true;
		while (found) {
			found = false;
			for (Model m : this) {
				if (m.getName() != null && m.getName().equals(nextModelNameBase+nextNum)) {
					found = true;
					nextNum++;
					break;
				}
			}
		}
		return nextModelNameBase+nextNum;
	}
}
