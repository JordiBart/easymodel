//package cat.udl.easymodel.httpservlet;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.util.ArrayList;
//
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import cat.udl.easymodel.logic.formula.Formula;
//import cat.udl.easymodel.logic.formula.Formulas;
//import cat.udl.easymodel.logic.types.FormulaType;
//import cat.udl.easymodel.main.SharedData;
//
//@WebServlet(urlPatterns = { "/updategeneric" })
//public class ScriptUpdateAllGenericFormulas extends HttpServlet {
//	public void init() throws ServletException {
//	}
//
//	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
////		if (true)
////			return;
//		response.setContentType("text/plain");
//		PrintWriter out = response.getWriter();
//		updateAllGenericFormulas();
//		out.print("update done");
//	}
//
//	private void deleteAllGenericFormulasDB() {
//		try {
//			SharedData sharedData = SharedData.getInstance();
//			Connection con = sharedData.getDbManager().getCon();
//			PreparedStatement ps= con.prepareStatement("DELETE FROM formula WHERE formulatype=?");
//			ps.setInt(1, FormulaType.GENERIC.getValue());
//			ps.executeUpdate();
//			ps.close();
//		} catch (Exception e) {
//		}
//	}
//
//	private void updateAllGenericFormulas() {
//		try {
//			deleteAllGenericFormulasDB();
//			SharedData sharedData = SharedData.getInstance();
//			Formulas newGenericRates = new Formulas(FormulaType.GENERIC);
//			ArrayList<Formula> allFormulas1 = sharedData.getDbManager().getAllFormulas();
//			newGenericRates.mergeGenericFormulasFrom(allFormulas1);
//			newGenericRates.saveDB();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void destroy() {
//	}
//}
