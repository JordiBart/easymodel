package cat.udl.easymodel.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.vaadin.ui.Component;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.user.User;

public interface DBManager {

	Connection getCon() throws SQLException;

	boolean open() throws SQLException;

	void close();
	
	void autoConvertPrivateToPublic() throws SQLException;

	void saveUsersAdmin(ArrayList<HashMap<String, Object>> values) throws SQLException;

	ArrayList<User> getAllUsers() throws SQLException;

	void saveModelsAdmin(ArrayList<HashMap<String, Object>> valuesToSave) throws SQLException;

	void saveFormulasAdmin(ArrayList<HashMap<String, Object>> valuesToSave) throws SQLException;

	ArrayList<Formula> getAllFormulas() throws SQLException;

	User insertGuestUserDB();

	void insertNewUserDB(User newUser) throws SQLException;

}
