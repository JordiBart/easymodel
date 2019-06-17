package cat.udl.easymodel.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.vaadin.ui.Component;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.logic.user.Users;

public interface DBManager {

	Connection getCon() throws SQLException;

	void open() throws SQLException;

	void close() throws SQLException;
	
	void autoConvertPrivateToPublic() throws SQLException;

	ArrayList<Formula> getAllFormulas() throws SQLException;

//	void saveUsersAdmin(ArrayList<HashMap<String, Object>> values) throws SQLException;

//	void saveModelsAdmin(ArrayList<HashMap<String, Object>> valuesToSave) throws SQLException;

//	void saveFormulasAdmin(HashMap<Formula,HashMap<String, Object>> valuesToSave) throws SQLException;
	
//	Users getAllUsers();


//	User insertGuestUserDB();

//	void insertNewUserDB(User newUser) throws SQLException;
}
