package cat.udl.easymodel.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.Formulas;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.user.UserCookie;
import cat.udl.easymodel.logic.user.Users;
import cat.udl.easymodel.persistence.DBManager;
import cat.udl.easymodel.persistence.DBManagerImpl;
import cat.udl.easymodel.thread.visitcounter.VisitCounterRunnable;

public class SharedData {
    private static SharedData thisSingleton = new SharedData();
    private DBManager dbManager;
    private Properties properties;
    private DecimalFormat decimalFormat;
    private Users users = null;
    private Formulas predefinedFormulas = null;
    private Formulas genericFormulas = null;
    private VisitCounterRunnable visitCounterRunnable = null;
    private final HashMap<String, String> mathematicaCodeMap = new HashMap<>(); // key: txt filename, value: txt file content
    private ArrayList<UserCookie> userCookies = new ArrayList<>();
    // constants
    public static final String appDir = "easymodel-appdata";
    public static final String propertiesFilePath = appDir + "/easymodel.properties";
    public static final String visitCounterFilePath = appDir + "/visitcounter.jo";
    public static final String tempDir = appDir + "/tmp";
    public static final String appName = "EasyModel";
    public static final String appVersion = "2.4";
    public static final String fullAppName = appName + " " + appVersion;
    public static final String dbError = "DATABASE CONNECTION ERROR";
    public static final int daysToExpireSimResults = 45;
    // default
    public static final String defaultInitialConcentration = "1";
    public static final String defaultParameterValue = "0.1";
    // mathLink
    public static final int maxMathLinks = 1;
    // SBML
    public static final int sbmlLevel = 3;
    public static final int sbmlVersion = 2;
    // user
    public static final long userCookiesExpireDays = 2;

    private SharedData() {
        createDir(appDir);
//		createDir(jLinkLibDir);
        createDir(tempDir);
        cleanTempDir();
//		ImageIO.setCacheDirectory(new File(SharedData.tempDir));

        visitCounterRunnable = new VisitCounterRunnable();

        properties = new Properties();
        try {
            InputStream input = new FileInputStream(propertiesFilePath);
            properties.load(input);
            input.close();
            System.out.println("CONFIG FILE LOADED " + propertiesFilePath);
        } catch (Exception e) {
            System.out.println("COULDN'T READ PROPERTIES FILE " + propertiesFilePath);
        }
        if (properties.getProperty("mathKernelCmdLine") == null) {
            if (isDialogsSystem())
                properties.setProperty("mathKernelCmdLine",
                        "-linkmode launch -linkname 'C:\\Program Files\\Wolfram Research\\Mathematica\\12.2\\MathKernel.exe'");
            else
                properties.setProperty("mathKernelCmdLine", "-linkmode launch -linkname 'math -mathlink'");
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

        if (properties.getProperty("hostname") == null)
            properties.setProperty("hostname", "");
        if (properties.getProperty("web-protocol") == null)
            properties.setProperty("web-protocol", "https://");
        if (properties.getProperty("contactMails") == null)
            properties.setProperty("contactMails", ""); // separate fields with ':' char

        if (properties.getProperty("reCaptcha-public-key") == null)
            properties.setProperty("reCaptcha-public-key", "");
        if (properties.getProperty("reCaptcha-private-key") == null)
            properties.setProperty("reCaptcha-private-key", "");

        if (properties.getProperty("privateWeeks") == null || !properties.getProperty("privateWeeks").matches("\\d+"))
            properties.setProperty("privateWeeks", "2");
        if (properties.getProperty("maxOnMemorySimResults") == null || !properties.getProperty("maxOnMemorySimResults").matches("\\d+"))
            properties.setProperty("maxOnMemorySimResults", "200");
        if (properties.getProperty("maxSimJobsPerUser") == null || !properties.getProperty("maxSimJobsPerUser").matches("\\d+"))
            properties.setProperty("maxSimJobsPerUser", "3");

        if (properties.getProperty("simulationTimeoutMinutes") == null
                || !properties.getProperty("simulationTimeoutMinutes").matches("\\d+"))
            properties.setProperty("simulationTimeoutMinutes", "30");

        if (properties.getProperty("debugMode").equals("1")) {
            System.out.println("WARNING: DEBUG MODE ON");
//			properties.setProperty("mySqlHost", "127.0.0.1:3306");
//			properties.setProperty("mySqlDb", "debug");
//			properties.setProperty("mySqlUser", "root");
//			properties.setProperty("mySqlPass", "");
        }
        // if (properties.getProperty("waitMathKernelSecs") == null)
        // properties.setProperty("waitMathKernelSecs", "60");
        try {
            OutputStream out = new FileOutputStream(propertiesFilePath);
            String propComments = appName
                    + " CONFIG FILE\nmathKernelCmdLine\n  WINDOWS default: -linkmode launch -linkname 'C\\:\\\\Program Files\\\\Wolfram Research\\\\Mathematica\\\\14.0\\\\MathKernel.exe'\n"
                    + "  LINUX default: -linkmode launch -linkname 'math -mathlink'\n"
                    + "contactMails are separated by a '\\:' character";
            // \nwaitMathKernelSecs: time in seconds to wait until mathKernel is free again
            // from doing tasks";
            properties.store(out, propComments);
            out.close();
        } catch (Exception e) {
            System.out.println("COULDN'T WRITE CONFIG FILE " + propertiesFilePath);
        }

        dbManager = new DBManagerImpl();
        users = new Users();
        predefinedFormulas = new Formulas(FormulaType.PREDEFINED);
        genericFormulas = new Formulas(FormulaType.GENERIC);

        DecimalFormatSymbols dfSymbols = new DecimalFormatSymbols();
        dfSymbols.setDecimalSeparator('.');
        dfSymbols.setGroupingSeparator(',');
        decimalFormat = new DecimalFormat("0.#", dfSymbols);
        decimalFormat.setMaximumFractionDigits(340);
        // decimalFormat = new DecimalFormat("0",
        // DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        // decimalFormat.setMaximumFractionDigits(340);
        loadMathematicaCodeMap();
    }

    private void loadMathematicaCodeMap() {
        // DO NOT CHANGE CODE TO SCAN DIR AUTOMATICALLY
        String dir = "mathematica-code";
        ArrayList<String> fileNames = new ArrayList<>();
        fileNames.add("param-scan-dyn.txt");
        fileNames.add("param-scan-steady-state.txt");
        fileNames.add("quickStochasticGradeCheck.txt");
        fileNames.add("steady-state.txt");
        fileNames.add("stochastic-ssa.txt");
        fileNames.add("stochastic-tau-leaping.txt");
        for (String fn : fileNames) {
            try {
                mathematicaCodeMap.put(fn, new String(getClass().getClassLoader().getResourceAsStream(dir+"/"+fn).readAllBytes()));
            } catch (IOException e) {
                System.err.println("CRITICAL: COULD NOT LOAD MATHEMATICA CODE: " + fn);
                System.exit(1);
            }
        }
    }

    private void createDir(String dir) {
        if (!new File(dir).exists() && !new File(dir).mkdirs()) {
            System.err.println("EXIT: COULD NOT CREATE DIRECTORY: " + dir);
            System.exit(1);
        }
    }

    public static SharedData getInstance() {
        return thisSingleton;
    }

    public static boolean isDialogsSystem() {
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

    public boolean isDebug() {
        return "1".equals(getProperties().getProperty("debugMode"));
    }

    public void dbgPrint(String msg) {
        if (isDebug())
            System.out.println(msg);
    }

    public Users getUsers() {
        return users;
    }

//	private void updateUsers() throws SQLException {
//		guestUser = null;
//		// add missing
//		Connection con = SharedData.getInstance().getDbManager().getCon();
//		Statement stmt = null;
//		String query = "select id,name,password,usertype from user";
//		boolean found;
//		ArrayList<User> usersToDelete = new ArrayList<>();
//		for (User u : this.allUsers)
//			usersToDelete.add(u);
//		stmt = con.createStatement();
//		ResultSet rs = stmt.executeQuery(query);
//		while (rs.next()) {
//			found = false;
//			for (User u : this.allUsers) {
//				if (u.getId() == rs.getInt("id")) {
//					u.setName(rs.getString("name"));
//					u.setEncPassword(rs.getString("password"));
//					u.setUserType(UserType.fromInt(rs.getInt("usertype")));
//					usersToDelete.remove(u);
//					found = true;
//					break;
//				}
//			}
//			if (!found) {
//				User u = new UserImpl(rs.getInt("id"), rs.getString("name"), rs.getString("password"),
//						UserType.fromInt(rs.getInt("usertype")));
//				this.allUsers.add(u);
//			}
//		}
//		for (User u : usersToDelete)
//			this.allUsers.remove(u);
//		for (User u : allUsers) {
//			if (u.getUserType() == UserType.GUEST) {
//				guestUser = u;
//				break;
//			}
//		}
//	}

    public VisitCounterRunnable getVisitCounterRunnable() {
        return visitCounterRunnable;
    }

    public Formulas getPredefinedFormulas() {
        return predefinedFormulas;
    }

    public Formulas getGenericFormulas() {
        return genericFormulas;
    }

    public void removeExpiredUserCookies() {
        ArrayList<UserCookie> toRemove = new ArrayList<UserCookie>();
        for (UserCookie userCookie : userCookies)
            if (userCookie.hasExpired())
                toRemove.add(userCookie);
        for (UserCookie userCookie : toRemove)
            userCookies.remove(userCookie);
    }

    public Formulas getPredefinedPlusGenericFormulas() {
        Formulas res = new Formulas((FormulaType) null);
        for (Formula f : predefinedFormulas)
            res.addFormula(f);
        for (Formula f : genericFormulas)
            res.addFormula(f);
        return res;
    }

    public HashMap<String, String> getMathematicaCodeMap() {
        return mathematicaCodeMap;
    }

    public ArrayList<UserCookie> getUserCookies() {
        return userCookies;
    }
}
