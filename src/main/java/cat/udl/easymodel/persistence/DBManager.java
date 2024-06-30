package cat.udl.easymodel.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.logic.user.Users;
import cat.udl.easymodel.utils.DBException;

public interface DBManager {

	Connection getCon() throws DBException;

	void open() throws DBException;

	void close() throws DBException;
	
	void autoConvertPrivateToPublic() throws DBException;

	ArrayList<Formula> getAllFormulas() throws DBException;

}
