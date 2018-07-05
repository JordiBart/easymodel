package cat.udl.easymodel.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import cat.udl.easymodel.logic.types.UserType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.logic.user.UserImpl;
import cat.udl.easymodel.persistence.DBManager;
import cat.udl.easymodel.persistence.DBManagerImpl;
import cat.udl.easymodel.utils.Utils;

public class SharedData {
	private DBManager dbManager = null;
	private Properties properties = null;
	private DecimalFormat decimalFormat = null;
	private ArrayList<User> allUsers = new ArrayList<>();
	private User guestUser;
	private static SharedData thisSingleton = new SharedData();
	// Constants
	public static final String appDir = "easymodel";
	public static final String propertiesFile = "easymodel.properties";
	public static final String propertiesFilePath = appDir+"/"+propertiesFile;
	public static final String tempDir = appDir + "/upload";
	public static final String appName = "EasyModel";
	public static final String appVersion = "1.0b";
	public static final String fullAppName = appName + " " + appVersion;
	public static final int privateWeeks = 2;
	// public static final boolean enableMath = true;
	public static final String dbError = "FATAL ERROR: DATABASE ISSUE";
	// mathLink
	public static final String mathLinkError = "CAN'T OPEN MATHLINK, PLEASE TRY AGAIN LATER";
	public static final String mathPrintPrefix = "MSG::";
	// SBML
	public static final int sbmlLevel = 3;
	public static final int sbmlVersion = 2;

	private SharedData() {
		if (!new File(appDir).exists() && !new File(appDir).mkdirs()) {
			System.err.println("COULD NOT CREATE DIRECTORY: " + appDir);
			System.err.println("THIS IS IMPORTANT FOR WRITING APP FILES!");
			System.exit(1);
		}
		if (!new File(tempDir).exists() && !new File(tempDir).mkdirs()) {
			System.err.println("COULD NOT CREATE DIRECTORY: " + tempDir);
			System.exit(1);
		}
		cleanTempDir();

		properties = new Properties();
		try {
			InputStream input = new FileInputStream(propertiesFilePath);
			properties.load(input);
			input.close();
			System.out.println("PROPERTIES FILE LOADED "+propertiesFilePath);
		} catch (Exception e) {
			System.out.println("COULDN'T READ PROPERTIES FILE "+propertiesFilePath);
		}
		if (properties.getProperty("mathKernelPath") == null) {
			if (isWindowsSystem())
				properties.setProperty("mathKernelPath",
						"-linkmode launch -linkname 'C:\\Program Files\\Wolfram Research\\Mathematica\\10.0\\MathKernel.exe'");
			else
				properties.setProperty("mathKernelPath", "-linkmode launch -linkname 'math -mathlink'");
		}
		if (properties.getProperty("debugMode") == null)
			properties.setProperty("debugMode", "0");
		// DATABASE
		if (properties.getProperty("mySqlHost") == null)
			properties.setProperty("mySqlHost", "127.0.0.1:3306");
		if (properties.getProperty("mySqlDb") == null)
			properties.setProperty("mySqlDb", "easymodel");
		if (properties.getProperty("mySqlUser") == null)
			properties.setProperty("mySqlUser", "root");
		if (properties.getProperty("mySqlPass") == null)
			properties.setProperty("mySqlPass", "");

		if (properties.getProperty("debugMode").equals("1"))
			System.out.println("WARNING: DEBUG MODE ON");
		if (properties.getProperty("waitMathKernelSecs") == null)
			properties.setProperty("waitMathKernelSecs", "60");
		try {
			OutputStream out = new FileOutputStream(propertiesFilePath);
			String propComments = appName+" CONFIG FILE\nWINDOWS mathKernelPath=-linkmode launch -linkname 'C\\:\\\\Program Files\\\\Wolfram Research\\\\Mathematica\\\\10.0\\\\MathKernel.exe'\n"
					+ "LINUX mathKernelPath=-linkmode launch -linkname 'math -mathlink'\nwaitMathKernelSecs: time in seconds to wait until mathKernel is free again from doing tasks";
			properties.store(out, propComments);
			out.close();
		} catch (Exception e) {
			System.out.println("COULDN'T WRITE PROPERTIES FILE "+propertiesFilePath);
		}

		dbManager = new DBManagerImpl();

		DecimalFormatSymbols dfSymbols = new DecimalFormatSymbols();
		dfSymbols.setDecimalSeparator('.');
		dfSymbols.setGroupingSeparator(',');
		decimalFormat = new DecimalFormat("0.#", dfSymbols);
		decimalFormat.setMaximumFractionDigits(340);
		// decimalFormat = new DecimalFormat("0",
		// DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		// decimalFormat.setMaximumFractionDigits(340);
	}

	public static SharedData getInstance() {
		return thisSingleton;
	}

	public boolean isWindowsSystem() {
		return System.getProperty("line.separator").equals("\r\n");
	}

	public String getNewLine() {
		return "\r\n";
	}

	public void cleanTempDir() {
		for (File file : new File(tempDir).listFiles())
			if (!file.isDirectory())
				file.delete();
	}

	public Properties getProperties() {
		return properties;
	}

	public DBManager getDbManager() {
		return dbManager;
	}

	public String doubleToString(Double d) {
		return decimalFormat.format(d);
	}

	public ArrayList<User> getAllUsers() throws SQLException {
		updateUsers();
		return allUsers;
	}

	public User getUserById(Integer id) throws SQLException {
		if (id != null) {
			for (User u : getAllUsers()) {
				if (u.getId() == id)
					return u;
			}
		}
		return null;
	}

	public boolean isDebug() {
		return "1".equals(getProperties().getProperty("debugMode"));
	}

	private void updateUsers() throws SQLException {
		guestUser = null;
		// add missing
		Connection con = SharedData.getInstance().getDbManager().getCon();
		Statement stmt = null;
		String query = "select id,name,password,usertype from user";
		boolean found;
		ArrayList<User> usersToDelete = new ArrayList<>();
		for (User u : this.allUsers)
			usersToDelete.add(u);
		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			found = false;
			for (User u : this.allUsers) {
				if (u.getId() == rs.getInt("id")) {
					u.setName(rs.getString("name"));
					u.setEncPassword(rs.getString("password"));
					u.setType(UserType.fromInt(rs.getInt("usertype")));
					usersToDelete.remove(u);
					found = true;
					break;
				}
			}
			if (!found) {
				User u = new UserImpl(rs.getInt("id"), rs.getString("name"), rs.getString("password"),
						UserType.fromInt(rs.getInt("usertype")));
				this.allUsers.add(u);
			}
		}
		for (User u : usersToDelete)
			this.allUsers.remove(u);
		for (User u : allUsers) {
			if (u.getUserType() == UserType.GUEST) {
				guestUser = u;
				break;
			}
		}
		if (guestUser == null)
			guestUser = getDbManager().createGuestUserDB();
	}

	public User getGuestUser() {
		return guestUser;
	}
}
