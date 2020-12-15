package cat.udl.easymodel.logic.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.BCrypt;
import cat.udl.easymodel.utils.ToolboxVaadin;

public class User {
	private Integer id = null;
	private String name = null;
	private String encPassword = null;
	private String pass = null;
	private String retypePass = null;
	private UserType userType = null;
	private boolean isDBDelete = false;
	private boolean isDirty = true;

	public User(Integer id, String name, String encPass, UserType type) {
		this.id = id;
		this.name = name;
		this.encPassword = encPass;
		this.userType = type;
	}

	public User(Integer id) {
		this.id = id;
	}

	public User(User from) {
		copyFrom(from);
	}
	
	public void copyFrom(User from) {
		this.reset();
		id = from.getId();
		name = from.getName();
		encPassword = from.getEncryptedPassword();
		pass = from.getPassForRegister();
		retypePass = from.getRetypePassForRegister();
		userType = from.getUserType();
		isDBDelete = from.isDBDelete();
		isDirty = from.isDirty();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEncryptedPassword() {
		return encPassword;
	}

	public void setEncryptedPassword(String encPass) {
		this.encPassword = encPass;
	}

	public void encryptPassword() {
		// JBcrypt uses 2a prefix!!!
		// gen: https://asecuritysite.com/encryption/bcrypt
		if (pass != null && pass.matches(ToolboxVaadin.passwordRegex)) {
			this.encPassword = BCrypt.hashpw(pass, BCrypt.gensalt());
			setPassForRegister(null);
			setRetypePassForRegister(null);
		}
	}

	public boolean matchLogin(String name2, String pass2) {
		if (this.name != null && this.name.equals(name2) && this.encPassword != null
				&& BCrypt.checkpw(pass2, this.encPassword))
			return true;
		else
			return false;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType type) {
		this.userType = type;
	}

	public void loadDB() throws SQLException {
		if (this.id == null)
			return;
		Connection con = SharedData.getInstance().getDbManager().getCon();
		PreparedStatement stmt = null;
		String query = "SELECT `id`, `name`, `password`, `usertype` FROM `user` WHERE `id`=?";
		try {
			stmt = con.prepareStatement(query);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				this.name = rs.getString("name");
				this.encPassword = rs.getString("password");
				this.setUserType(UserType.fromInt(rs.getInt("usertype")));
				this.setDirty(false);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveDB() throws SQLException {
		encryptPassword();
		SharedData sharedData = SharedData.getInstance();
		Connection con = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement;
		int p;
		try {
			if (getId() == null) {
				preparedStatement = con.prepareStatement(
						"INSERT INTO user (`id`, `name`, `password`, `usertype`) VALUES (NULL,?,?,?)",
						Statement.RETURN_GENERATED_KEYS);
				p = 1;
				preparedStatement.setString(p++, getName());
				preparedStatement.setString(p++, getEncryptedPassword());
				preparedStatement.setInt(p++, getUserType().getValue());
				int affectedRows = preparedStatement.executeUpdate();
				if (affectedRows == 0)
					throw new SQLException("Creating user failed, no rows affected.");
				try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						this.setId(generatedKeys.getInt(1));
					} else {
						throw new SQLException("Creating user failed, no ID obtained.");
					}
					generatedKeys.close();
				}
				preparedStatement.close();
			} else {
				preparedStatement = con.prepareStatement("UPDATE user SET name=?,"
						+ (getEncryptedPassword() != null ? "password=?," : "") + "usertype=? WHERE id=?");
				p = 1;
				preparedStatement.setString(p++, getName());
				if (getEncryptedPassword() != null)
					preparedStatement.setString(p++, getEncryptedPassword());
				preparedStatement.setInt(p++, getUserType().getValue());
				preparedStatement.setInt(p++, id);
				preparedStatement.executeUpdate();
				preparedStatement.close();
			}
			setDirty(false);
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void saveDBAdmin() throws SQLException {
		if (getId() == null) {
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
		SharedData sharedData = SharedData.getInstance();
		Connection con = sharedData.getDbManager().getCon();
		PreparedStatement preparedStatement;
		if (getId() != null) {
			try {
				preparedStatement = con.prepareStatement("DELETE FROM user WHERE id=?");
				preparedStatement.setInt(1, getId());
				preparedStatement.executeUpdate();
				preparedStatement.close();

				this.reset();
			} catch (SQLException e) {
				System.err.println(e.getMessage());
				throw e;
			}
		}
	}

	public void reset() {
		id = null;
		name = null;
		encPassword = null;
		userType = null;
		isDBDelete = false;
		isDirty = true;
	}

	public boolean isDBDelete() {
		return isDBDelete;
	}

	public void setDBDelete(boolean isDBDelete) {
		this.isDBDelete = isDBDelete;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public String getPassForRegister() {
		return pass;
	}

	public void setPassForRegister(String pass) {
		this.pass = pass;
	}

	public String getRetypePassForRegister() {
		return retypePass;
	}

	public void setRetypePassForRegister(String retypePass) {
		this.retypePass = retypePass;
	}

	public void validateForRegister(ArrayList<User> allUsers) throws Exception {
		if (name == null || !name.matches(ToolboxVaadin.usernameRegex)) {
			throw new Exception("Invalid " + ToolboxVaadin.usernameRegexInfo);
		}
		if (allUsers != null) {
			for (User u : allUsers)
				if (u.getName().equals(name))
					throw new Exception("Username " + name + " already exists");
		}
		if (pass == null || !pass.matches(ToolboxVaadin.passwordRegex))
			throw new Exception("Invalid " + ToolboxVaadin.passwordRegexInfo);
		if (retypePass == null || !retypePass.equals(pass))
			throw new Exception("Password fields don't match");
	}

	public void validateForAdmin() throws Exception {
		if (name == null || !name.matches(ToolboxVaadin.usernameRegex)) {
			throw new Exception("Invalid " + ToolboxVaadin.usernameRegexInfo);
		}
		if ((id != null && pass != null && !pass.isEmpty() && !pass.matches(ToolboxVaadin.passwordRegex))
				|| (id == null && (pass == null || !pass.matches(ToolboxVaadin.passwordRegex))))
			throw new Exception("Invalid " + ToolboxVaadin.passwordRegexInfo);
	}
}
