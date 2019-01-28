package cat.udl.easymodel.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaImpl;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.logic.user.UserImpl;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.BCrypt;
import cat.udl.easymodel.utils.Utils;

public class DBManagerImpl implements DBManager {
	private Connection con = null;

	public DBManagerImpl() {
	}

	@Override
	public boolean open() throws SQLException {
		try {
			if (con == null || con.isClosed() || !con.isValid(3)) {
				SharedData sharedData = SharedData.getInstance();
				Properties properties = sharedData.getProperties();
				Class.forName("com.mysql.jdbc.Driver");
				con = DriverManager.getConnection("jdbc:mysql://" + properties.getProperty("mySqlHost") + "/"
						+ properties.getProperty("mySqlDb") + "?" + "user=" + properties.getProperty("mySqlUser")
						+ "&password=" + properties.getProperty("mySqlPass"));
			}
		} catch (ClassNotFoundException | SQLException e1) {
			e1.printStackTrace();
			throw new SQLException("MYSQL OPEN ERROR");
		}
		return true;
	}

	@Override
	public void close() {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
				System.out.println("MYSQL CLOSED!");
			}
		} catch (SQLException e) {
			System.out.println("CANNOT CLOSE MYSQL");
			e.printStackTrace();
		}
	}

	@Override
	public Connection getCon() throws SQLException {
		this.open();
		return con;
	}

	@Override
	public ArrayList<User> getAllUsers() throws SQLException {
		Connection con = this.getCon();
		Statement stmt = null;
		String query = "select id,name,password,usertype from user";
		ArrayList<User> res = new ArrayList<>();
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				User u = new UserImpl(rs.getInt("id"), rs.getString("name"), rs.getString("password"),
						UserType.fromInt(rs.getInt("usertype")));
				res.add(u);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			res.clear();
		}
		return res;
	}

	@Override
	public ArrayList<Formula> getAllFormulas() throws SQLException {
		Connection con = this.getCon();
		Statement stmt = null;
		String query = "SELECT `id`, `id_user`, `name`, `formula`, `onesubstrateonly`, `noproducts`, `onemodifieronly`, `formulatype`, `repositorytype` FROM `formula`";
		ArrayList<Formula> res = new ArrayList<>();
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				Formula f = new FormulaImpl(rs.getString("name"), rs.getString("formula"),
						FormulaType.fromInt(rs.getInt("formulatype")),
						SharedData.getInstance().getUserById(rs.getInt("id_user")),
						RepositoryType.fromInt(rs.getInt("repositorytype")));
				f.setId(rs.getInt("id"));
				f.setOneSubstrateOnly(Utils.intToBool(rs.getInt("onesubstrateonly")));
				f.setNoProducts(Utils.intToBool(rs.getInt("noproducts")));
				f.setOneModifierOnly(Utils.intToBool(rs.getInt("onemodifieronly")));
				res.add(f);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			res.clear();
		}
		return res;
	}

	@Override
	public void autoConvertPrivateToPublic() throws SQLException {
		Connection con = this.getCon();
		LocalDate limitDate = LocalDate.now().minus(SharedData.privateWeeks, ChronoUnit.WEEKS);
		PreparedStatement preparedStatement;
		int p;
		try {
			// FORMULA TABLE
			preparedStatement = con
					.prepareStatement("UPDATE formula SET repositorytype=? WHERE repositorytype=? AND modified < ?");
			p = 1;
			preparedStatement.setInt(p++, RepositoryType.PUBLIC.getValue());
			preparedStatement.setInt(p++, RepositoryType.PRIVATE.getValue());
			preparedStatement.setString(p++, limitDate.toString());
			preparedStatement.executeUpdate();
			preparedStatement.close();
			// MODEL TABLE
			preparedStatement = con
					.prepareStatement("UPDATE model SET repositorytype=? WHERE repositorytype=? AND modified < ?");
			p = 1;
			preparedStatement.setInt(p++, RepositoryType.PUBLIC.getValue());
			preparedStatement.setInt(p++, RepositoryType.PRIVATE.getValue());
			preparedStatement.setString(p++, limitDate.toString());
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveUsersAdmin(ArrayList<HashMap<String, Object>> valuesToSave) throws SQLException {
		Connection con = this.getCon();
		int p;
		PreparedStatement preparedStatement;
		String table = "user";
		HashMap<String, Object> hm;
		Integer id;
		try {
			for (int i = 0; i < valuesToSave.size(); i++) {
				hm = valuesToSave.get(i);
				id = (Integer) hm.get("id");
				if (id == null && !(Boolean) hm.get("delete")) {
					preparedStatement = con.prepareStatement(
							"INSERT INTO " + table + " (`id`, `name`, `password`, `usertype`) VALUES (NULL,?,?,?)");
					p = 1;
					preparedStatement.setString(p++, ((String) hm.get("name")));
					preparedStatement.setString(p++, BCrypt.hashpw((String) hm.get("password"), BCrypt.gensalt()));
					preparedStatement.setInt(p++, ((UserType) hm.get("usertype")).getValue());
					preparedStatement.executeUpdate();
					preparedStatement.close();
				} else if (id != null && (Boolean) hm.get("delete")) {
					preparedStatement = con.prepareStatement("DELETE FROM " + table + " WHERE id=?");
					p = 1;
					preparedStatement.setInt(p++, id);
					preparedStatement.executeUpdate();
					preparedStatement.close();
				} else if (id != null && !(Boolean) hm.get("delete")) {
					preparedStatement = con.prepareStatement("UPDATE " + table + " SET name=?,usertype=?"
							+ (((String) hm.get("password")).equals("") ? "" : ",password=?") + " WHERE id=?");
					p = 1;
					preparedStatement.setString(p++, ((String) hm.get("name")));
					preparedStatement.setInt(p++, ((UserType) hm.get("usertype")).getValue());
					if (!((String) hm.get("password")).equals(""))
						preparedStatement.setString(p++, BCrypt.hashpw((String) hm.get("password"), BCrypt.gensalt()));
					preparedStatement.setInt(p++, id);
					preparedStatement.executeUpdate();
					preparedStatement.close();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void saveModelsAdmin(ArrayList<HashMap<String, Object>> valuesToSave) throws SQLException {
		Connection con = this.getCon();
		int p;
		PreparedStatement preparedStatement;
		String table = "model";
		HashMap<String, Object> hm;
		Integer id;
		try {
			for (int i = 0; i < valuesToSave.size(); i++) {
				hm = valuesToSave.get(i);
				id = (Integer) hm.get("id");
				if (id != null && (Boolean) hm.get("delete")) {
					preparedStatement = con.prepareStatement("DELETE FROM " + table + " WHERE id=?");
					p = 1;
					preparedStatement.setInt(p++, id);
					preparedStatement.executeUpdate();
					preparedStatement.close();
				} else if (id != null && !(Boolean) hm.get("delete")) {
					preparedStatement = con.prepareStatement("UPDATE " + table
							+ " SET `name`=?,`description`=?,`repositorytype`=?,`modified`=DATE(NOW()) WHERE id=?");
					p = 1;
					preparedStatement.setString(p++, ((String) hm.get("name")));
					preparedStatement.setString(p++, ((String) hm.get("description")));
					preparedStatement.setInt(p++, ((RepositoryType) hm.get("repositorytype")).getValue());
					preparedStatement.setInt(p++, id);
					preparedStatement.executeUpdate();
					preparedStatement.close();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void saveFormulasAdmin(ArrayList<HashMap<String, Object>> valuesToSave) throws SQLException {
		Connection con = this.getCon();
		int p;
		PreparedStatement preparedStatement;
		String table = "formula";
		HashMap<String, Object> hm;
		Integer id;
		try {
			for (int i = 0; i < valuesToSave.size(); i++) {
				hm = valuesToSave.get(i);
				id = (Integer) hm.get("id");
				if (id == null && !(Boolean) hm.get("delete")) {
					preparedStatement = con.prepareStatement("INSERT INTO " + table
							+ " (`id`, `id_user`, `name`, `formula`, `onesubstrateonly`, `noproducts`, `onemodifieronly`, `formulatype`, `repositorytype`, `modified`) VALUES (NULL,NULL,?,?,?,?,?,?,?,DATE(NOW()))");
					p = 1;
					preparedStatement.setString(p++, ((String) hm.get("name")));
					preparedStatement.setString(p++, ((String) hm.get("formula")));
					preparedStatement.setInt(p++, Utils.boolToInt(((Boolean) hm.get("onesubstrateonly"))));
					preparedStatement.setInt(p++, Utils.boolToInt(((Boolean) hm.get("noproducts"))));
					preparedStatement.setInt(p++, Utils.boolToInt(((Boolean) hm.get("onemodifieronly"))));
					preparedStatement.setInt(p++, ((FormulaType) hm.get("formulatype")).getValue());
					preparedStatement.setInt(p++, ((RepositoryType) hm.get("repositorytype")).getValue());
					preparedStatement.executeUpdate();
					preparedStatement.close();
				} else if (id != null && (Boolean) hm.get("delete")) {
					preparedStatement = con.prepareStatement("DELETE FROM " + table + " WHERE id=?");
					p = 1;
					preparedStatement.setInt(p++, id);
					preparedStatement.executeUpdate();
					preparedStatement.close();
				} else if (id != null && !(Boolean) hm.get("delete")) {
					preparedStatement = con.prepareStatement("UPDATE " + table
							+ " SET `name`=?,`formula`=?,`onesubstrateonly`=?,`noproducts`=?,`onemodifieronly`=?,`formulatype`=?,`repositorytype`=?,`modified`=DATE(NOW()) WHERE id=?");
					p = 1;
					preparedStatement.setString(p++, ((String) hm.get("name")));
					preparedStatement.setString(p++, ((String) hm.get("formula")));
					preparedStatement.setInt(p++, Utils.boolToInt(((Boolean) hm.get("onesubstrateonly"))));
					preparedStatement.setInt(p++, Utils.boolToInt(((Boolean) hm.get("noproducts"))));
					preparedStatement.setInt(p++, Utils.boolToInt(((Boolean) hm.get("onemodifieronly"))));
					preparedStatement.setInt(p++, ((FormulaType) hm.get("formulatype")).getValue());
					preparedStatement.setInt(p++, ((RepositoryType) hm.get("repositorytype")).getValue());
					preparedStatement.setInt(p++, id);
					preparedStatement.executeUpdate();
					preparedStatement.close();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public User insertGuestUserDB() {
		User guestUser = null;
		try {
			guestUser = new UserImpl(null, "guest", "", UserType.GUEST);
			Connection con = SharedData.getInstance().getDbManager().getCon();
			PreparedStatement preparedStatment = null;
			int p = 1;
			preparedStatment = con.prepareStatement(
					"INSERT INTO `user`(`id`, `name`, `password`, `usertype`) VALUES (NULL,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			preparedStatment.setString(p++, guestUser.getName());
			preparedStatment.setString(p++, guestUser.getEncPassword());
			preparedStatment.setInt(p++, guestUser.getUserType().getValue());
			int affectedRows = preparedStatment.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating guestUser failed, no rows affected.");
			}
			try (ResultSet generatedKeys = preparedStatment.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					guestUser.setId(generatedKeys.getInt(1));
				} else {
					throw new SQLException("Creating guestUser failed, no ID obtained.");
				}
				generatedKeys.close();
			}
			preparedStatment.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return guestUser;
	}

	@Override
	public void insertNewUserDB(User newUser) throws SQLException {
		if (newUser == null)
			throw new SQLException("insertNewUserDB(User newUser): newUser==null");
		Connection con = SharedData.getInstance().getDbManager().getCon();
		PreparedStatement preparedStatment = null;
		int p = 1;
		preparedStatment = con.prepareStatement(
				"INSERT INTO `user`(`id`, `name`, `password`, `usertype`) VALUES (NULL,?,?,?)",
				Statement.RETURN_GENERATED_KEYS);
		preparedStatment.setString(p++, newUser.getName());
		preparedStatment.setString(p++, newUser.getEncPassword());
		preparedStatment.setInt(p++, newUser.getUserType().getValue());
		int affectedRows = preparedStatment.executeUpdate();
		if (affectedRows == 0) {
			throw new SQLException("Creating newUser failed, no rows affected.");
		}
		try (ResultSet generatedKeys = preparedStatment.getGeneratedKeys()) {
			if (generatedKeys.next()) {
				newUser.setId(generatedKeys.getInt(1));
			} else {
				throw new SQLException("Creating newUser failed, no ID obtained.");
			}
			generatedKeys.close();
		}
		preparedStatment.close();
	}
}
