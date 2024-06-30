package cat.udl.easymodel.logic.user;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.DBException;

public class Users extends ArrayList<User> {
	private static final long serialVersionUID = 1L;

	public Users() {
		super();
	}

	public Users(Users from) {
		super();
		copyFrom(from);
	}

	public void copyFrom(Users from) {
		this.reset();
		for (User u : from) {
			this.add(new User(u));
		}
	}

	public User getUserById(Integer id) {
		if (id != null) {
			for (User u : this) {
				if (u.getId() == id)
					return u;
			}
		}
		return null;
	}

	public User getUserByName(String name) {
		if (name != null) {
			for (User u : this) {
				if (name.equals(u.getName()))
					return u;
			}
		}
		return null;
	}

	public void reset() {
		for (User u : this)
			u.reset();
		this.clear();
	}

	public void updateFrom(Users from) {
		ArrayList<User> usersToDelete = new ArrayList<>();
		// update, delete
		for (User u1 : this) {
			if (u1.getId() == null)
				continue;
			boolean found = false;
			for (User u2 : from) {
				if (u2.getId() == null)
					continue;
				if (u1.getId() == u2.getId()) {
					u1.copyFrom(u2);
					found = true;
					break;
				}
			}
			if (!found)
				usersToDelete.add(u1);
		}
		for (User u1 : usersToDelete)
			this.remove(u1);
		// add new users
		for (User u2 : from)
			if (u2.getId() == null)
				this.add(new User(u2));
	}

	public void loadDB() throws DBException {
		this.reset();
		try {
			Connection con = SharedData.getInstance().getDbManager().getCon();
			Statement stmt = null;
			String query = "select id,name,password,usertype from user";
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				User u = new User(rs.getInt("id"), rs.getString("name"), rs.getString("password"),
						UserType.valueOf(rs.getInt("usertype")));
				u.setDirty(false);
				this.add(u);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveDBAdmin() throws Exception {
		ArrayList<User> usersToDelete = new ArrayList<>();
		for (User u : this) {
			if (u.isDBDelete())
				usersToDelete.add(u);
			if (u.getId() == null) {
				if (!u.isDBDelete())
					u.saveDB();
			} else {
				if (u.isDBDelete())
					u.deleteDB();
				else
					u.saveDB();
			}
		}
		for (User u : usersToDelete)
			this.remove(u);
	}

//	public void saveDB() throws SQLException {
//		for (User u : this)
//			u.saveDB();
//	}
}
