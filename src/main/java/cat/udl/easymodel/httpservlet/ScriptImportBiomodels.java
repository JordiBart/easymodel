//package cat.udl.easymodel.httpservlet;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.util.ArrayList;
//
//import cat.udl.easymodel.controller.BioModelsLogs;
//import cat.udl.easymodel.controller.SimulationCtrl;
//import cat.udl.easymodel.logic.formula.Formula;
//import cat.udl.easymodel.logic.formula.Formulas;
//import cat.udl.easymodel.logic.model.Model;
//import cat.udl.easymodel.logic.types.FormulaType;
//import cat.udl.easymodel.logic.types.RepositoryType;
//import cat.udl.easymodel.main.SessionData;
//import cat.udl.easymodel.main.SharedData;
//import cat.udl.easymodel.mathlink.CheckSystemJob;
//import cat.udl.easymodel.mathlink.MathJobStatus;
//import cat.udl.easymodel.mathlink.MathQueue;
//import cat.udl.easymodel.sbml.SBMLMan;
//import cat.udl.easymodel.utils.CException;
//import cat.udl.easymodel.utils.P;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//@WebServlet(urlPatterns = "/importbio", name = "importbio")
//public class ScriptImportBiomodels extends HttpServlet {
//	public void init() throws ServletException {
//	}
//
//	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		if (true)
//			return;
//		response.setContentType("text/html");
//		PrintWriter out = response.getWriter();
//		P.p("Script+Batch start!");
//		SharedData sharedData = SharedData.getInstance();
//		SessionData sessionData = new SessionData();
//		sessionData.init();
//
//		sessionData.setBioModelsLogs(new BioModelsLogs());
//		try {
//      // XXX REBUILD WITH MATHJOB LIKE ScriptCheckSystem and CheckSystemJob
//			sessionData.respawnMathLinkOp();
//		} catch (CException e1) {
//			out.print("Can't open mathlink!");
//			return;
//		}
//		SimulationCtrl simCtrl = new SimulationCtrl(sessionData);
//		int totalLoadOK = 0, totalLoadKO = 0, totalSimOK = 0;
//		sessionData.setUser(sharedData.getUsers().getUserByName("admin"));
//		try {
//			FileInputStream fileInputStream = null;
//			byte[] bFile;
//			File sbmlDir = new File(SharedData.appDir + "/biomodels");
//			deleteAllBioModelsDB();
//			for (File file : sbmlDir.listFiles()) {
////				if (!file.getName().equals("BIOMD0000000244.xml"))
////					continue;
//				StringBuilder report = new StringBuilder();
//				try {
//					bFile = Files.readAllBytes(file.toPath());
//					ByteArrayInputStream bais = new ByteArrayInputStream(bFile);
//					SBMLMan sbmlMan = new SBMLMan();
//					Model m = sbmlMan.importSBML(bais, report, "BIOMD" + file.getName().substring(12, 15) + " ", true);
//					m.checkValidModel();
//					totalLoadOK++;
//					sessionData.getBioModelsLogs().loadLogFile.write(file.getName() + "\n");
//					if (report.length() > 0)
//						sessionData.getBioModelsLogs().loadLogFile.write(report.toString());
//
//					// simulate
//					// skip models
//					if (file.getName().equals("BIOMD0000000015.xml") || file.getName().equals("BIOMD0000000235.xml"))
//						continue;
//					System.out.println("Simulating " + file.getName());
//					sessionData.getBioModelsLogs().simLogFile
//							.append(file.getName() + " ############################################\n");
//					m.getSimConfig().getDynamic().get("Ti").setValue("0");
//					m.getSimConfig().getDynamic().get("Tf").setValue("1");
//					m.getSimConfig().getDynamic().get("TStep").setValue("0.5");
//					sessionData.setSelectedModel(m);
//					try {
//						simCtrl.simulate();
//						if (!sessionData.getBioModelsLogs().isLastSimError) {
//							totalSimOK++;
//							// store to db
//							m.saveDB();
//						}
//						sessionData.getBioModelsLogs().isLastSimError = false;
//					} catch (Exception e) {
//						System.out.println("sim exception");
//						e.printStackTrace();
//					}
//					finally {
//						sessionData.respawnMathLinkOp();
//					}
//				} catch (Exception e) {
//					// P.p(file.getName() + " : " + e.getMessage());
//					// if (!e.getMessage().startsWith("C ")) {
//					sessionData.getBioModelsLogs().errorLogFile.write(file.getName() + "\n");
//					sessionData.getBioModelsLogs().errorLogFile.write(e.getMessage() + "\n");
//					if (report.length() > 0)
//						sessionData.getBioModelsLogs().errorLogFile.write("report:\n" + report.toString());
//					totalLoadKO++;
//					// }
//					if (e.getMessage() == null) {
//						e.printStackTrace();
//						break;
//					}
//				}
//				//break; // first model only
//			}
//			sessionData.getBioModelsLogs().loadLogFile.write("total " + totalLoadOK + "\n");
//			sessionData.getBioModelsLogs().errorLogFile.write("total " + totalLoadKO + "\n");
//			sessionData.getBioModelsLogs().simLogFile.write("total " + totalSimOK + "\n");
//		} catch (IOException e) {
//			P.p("error IO file/SQL " + e.getMessage());
//		} finally {
//			sessionData.getBioModelsLogs().close();
//			sessionData.closeMathLinkOp();
//			P.p("SBML batch finish");
//		}
//		P.p("updateGenericFormulas");
//		updateAllGenericFormulas();
//		publishAdminModels();
//		out.print("import biomodels done");
//		P.p("SCRIPT FINISHED");
//	}
//
//	private void publishAdminModels() {
//		try {
//			SharedData sharedData = SharedData.getInstance();
//			Connection con = sharedData.getDbManager().getCon();
//			PreparedStatement ps= con.prepareStatement("UPDATE model SET `repositorytype`=?");
//			ps.setInt(1, RepositoryType.PUBLIC.getValue());
//			ps.executeUpdate();
//			ps.close();
//		} catch (Exception e) {
//		}
//	}
//
//	private void deleteAllBioModelsDB() {
//		try {
//			SharedData sharedData = SharedData.getInstance();
//			Connection con = sharedData.getDbManager().getCon();
//			PreparedStatement ps= con.prepareStatement("DELETE FROM model WHERE name LIKE 'BIOMD%'");
//			ps.executeUpdate();
//			ps.close();
//		} catch (Exception e) {
//		}
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
//		// save different formulas?
//		// XXX DELETE?
////		try {
////			Models models = new Models();
////			models.semiLoadDB();
////			for (Model m : models) {
////				m.loadDB();
////				ArrayList<Formula> modelFs = new ArrayList<Formula>();
////				for (Reaction r : m) {
////					if (r.getId() != null) {
////						Connection con = sharedData.getDbManager().getCon();
////						PreparedStatement ps = null, ps2;
////						String query = "SELECT f.id, f.id_model, f.name, f.formula, f.onesubstrateonly, f.noproducts, f.onemodifieronly, f.formulatype FROM reaction r, formula f WHERE r.id=? AND r.id_formula=f.id";
////						ps = con.prepareStatement(query);
////						int p = 1;
////						ps.setInt(p++, r.getId());
////						ResultSet rs = ps.executeQuery();
////						if (rs.next()) {
////							Formula f = new Formula(rs.getString("name"), rs.getString("formula"),
////									FormulaType.fromInt(rs.getInt("formulatype")), m);
////							f.setOneSubstrateOnly(Utils.intToBool(rs.getInt("onesubstrateonly")));
////							f.setNoProducts(Utils.intToBool(rs.getInt("noproducts")));
////							f.setOneModifierOnly(Utils.intToBool(rs.getInt("onemodifieronly")));
////							f.setFormulaType(FormulaType.MODEL);
////							f.setDirty(true);
////
////							Formula sameFormula = null;
////							for (Formula f2 : modelFs) {
////								if (f2.getNameRaw().equals(f.getNameRaw()))
////									sameFormula = f2;
////							}
////							if (sameFormula == null) {
////								f.saveDB();
////								modelFs.add(f);
////							} else
////								f = sameFormula;
////							ps2 = con.prepareStatement("UPDATE reaction SET `id_formula`=? WHERE id=?");
////							p = 1;
////							ps2.setInt(p++, f.getId());
////							ps2.setInt(p++, r.getId());
////							ps2.executeUpdate();
////							ps2.close();
////						}
////						rs.close();
////						ps.close();
////					}
////				}
////			}
////		} catch (SQLException e) {
////			e.printStackTrace();
////		}
//	}
//
//	public void destroy() {
//	}
//}
