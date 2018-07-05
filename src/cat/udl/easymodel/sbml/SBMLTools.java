package cat.udl.easymodel.sbml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.SortedMap;
import java.util.TreeMap;

import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Creator;
import org.sbml.jsbml.History;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.xml.XMLNode;

import com.sun.jna.platform.win32.WTypes.VARTYPE;
import com.vaadin.ui.UI;

import cat.udl.easymodel.logic.formula.Formula;
import cat.udl.easymodel.logic.formula.FormulaImpl;
import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.model.FormulaValue;
import cat.udl.easymodel.logic.model.FormulaValueImpl;
import cat.udl.easymodel.logic.model.ReactionImpl;
import cat.udl.easymodel.logic.types.FormulaType;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.logic.types.RepositoryType;
import cat.udl.easymodel.logic.types.SpeciesVarTypeType;
import cat.udl.easymodel.logic.user.User;
import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.MathLinkOp;
import cat.udl.easymodel.utils.p;

public class SBMLTools {

	public static final String keywordFixSufix = "2";
	
	private SBMLTools() {
	}

	public static String exportSBML(cat.udl.easymodel.logic.model.Model m, MathLinkOp mathLinkOp) throws Exception {
		SBMLDocument doc = new SBMLDocument(SharedData.sbmlLevel, SharedData.sbmlVersion);
//		doc.addTreeNodeChangeListener(new SBMLTreeNodeChangeListener());

		// Create a new SBML model, and add a compartment to it.
		Model model = doc.createModel("model");
		model.setName(m.getName());
		model.setNotes(m.getDescription());
		Unit uM2 = new Unit();
		uM2.setKind(Kind.METRE);
		uM2.setScale(0);
		uM2.setExponent(2d);
		uM2.setMultiplier(1d);
		UnitDefinition udM2 = model.createUnitDefinition("m2");
		udM2.setName("m2");
		udM2.addUnit(uM2);

		model.setExtentUnits("mole");
		model.setLengthUnits("metre");
		model.setAreaUnits(udM2);
		model.setVolumeUnits("litre");
		model.setTimeUnits("second");
		model.setSubstanceUnits("mole");
		
		Compartment compartment = model.createCompartment("compartment");
		compartment.setName("compartment");
		compartment.setSize(100d);
		compartment.setConstant(true); // constant size
		compartment.setSpatialDimensions(3);

		// Create a model history object and add author information to it.
//		History hist = model.getHistory(); // Will create the History, if it does not exist
//		Creator creator = new Creator("", "", "", "");
//		hist.addCreator(creator);

		SortedMap<String, Species> speciesMap = new TreeMap<>();
		for (String spKey : m.getAllSpecies().keySet()) {
			cat.udl.easymodel.logic.model.Species sp = m.getAllSpecies().get(spKey);
			speciesMap.put(spKey, model.createSpecies(spKey, compartment));
			speciesMap.get(spKey).setName(speciesMap.get(spKey).getId());
			speciesMap.get(spKey).setHasOnlySubstanceUnits(false); // if true, can only contain amount, not
																	// concentration
			if (sp.getVarType() == SpeciesVarTypeType.INDEP) {
				speciesMap.get(spKey).setBoundaryCondition(true);
				speciesMap.get(spKey).setConstant(true);
			} else {
				speciesMap.get(spKey).setBoundaryCondition(false);
				speciesMap.get(spKey).setConstant(false);
			}
			speciesMap.get(spKey).setInitialConcentration(Double.valueOf(sp.getConcentration()));
		}

		for (cat.udl.easymodel.logic.model.Reaction r : m) {
			Reaction rsb = model.createReaction(r.getIdJavaStr());
			rsb.setName(rsb.getId());
			rsb.setReversible(false); // substrates <-> products ?
			for (String sp : r.getLeftPartSpecies().keySet()) {
				SpeciesReference sr = rsb.createReactant(speciesMap.get(sp));
				sr.setStoichiometry(r.getLeftPartSpecies().get(sp));
				sr.setConstant(speciesMap.get(sp).isConstant());
			}
			for (String sp : r.getRightPartSpecies().keySet()) {
				SpeciesReference sr = rsb.createProduct(speciesMap.get(sp));
				sr.setStoichiometry(r.getRightPartSpecies().get(sp));
				sr.setConstant(speciesMap.get(sp).isConstant());
			}
			for (String sp : r.getModifiers().keySet()) {
				ModifierSpeciesReference msr = rsb.createModifier(speciesMap.get(sp));
			}

			KineticLaw kl = new KineticLaw(rsb);
			kl.setId("KL"+r.getIdJavaStr());
			kl.setName(r.getFormula().getNameToShow());
			for (String par : r.getFormulaValues().keySet()) {
				FormulaValue fv = r.getFormulaValues().get(par);
				if (fv.getType() == FormulaValueType.CONSTANT) {
					LocalParameter lp = new LocalParameter(r.getIdJavaStr()+par);
					lp.setName(lp.getId());
					lp.setValue(Double.valueOf(fv.getConstantValue()));
					kl.addLocalParameter(lp);
				}
			}
			for (String par : r.getFormulaSubstratesArrayParameters().keySet()) {
				SortedMap<String,String> parMap = r.getFormulaSubstratesArrayParameters().get(par);
				for (String parBySpecies : parMap.keySet()) {
					Double val = Double.valueOf(parMap.get(parBySpecies));
					LocalParameter lp = new LocalParameter(r.getIdJavaStr()+par+parBySpecies);
					lp.setName(lp.getId());
					lp.setValue(val);
					kl.addLocalParameter(lp);
				}
			}
			for (String par : r.getFormulaModifiersArrayParameters().keySet()) {
				SortedMap<String,String> parMap = r.getFormulaModifiersArrayParameters().get(par);
				for (String parBySpecies : parMap.keySet()) {
					Double val = Double.valueOf(parMap.get(parBySpecies));
					LocalParameter lp = new LocalParameter(r.getIdJavaStr()+par+parBySpecies);
					lp.setName(lp.getId());
					lp.setValue(val);
					kl.addLocalParameter(lp);
				}
			}
			String formula = mathLinkOp.evaluateToString("InputForm[ReleaseHold[Hold["+r.getFormula().getFormulaWithAddedPrefix("", r.getIdJavaStr())+"]/.SubsFormVars/.ParArrayInVars/.ParVarVals]]");
//			p.p("REMOVE F: "+formula);
			kl.setFormula(formula);
		}

		// CHECK SBML
		doc.checkConsistency();
		SBMLErrorLog errorLog = doc.getListOfErrors();
		if (errorLog.getErrorCount() > 0) {
			String excMsg = "SBML check errors: " + errorLog.getErrorCount() + "\n";
			for (int i = 0; i < errorLog.getErrorCount(); i++) {
				SBMLError err = errorLog.getError(i);
				excMsg += err.getMessage() + "\n";
			}
			throw new Exception(excMsg);
		}

		// GENERATE SBML
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SBMLWriter.write(doc, baos, SharedData.appName, SharedData.appVersion);
		return baos.toString("UTF-8");
	}

	public static cat.udl.easymodel.logic.model.Model importSBML(ByteArrayInputStream bais) throws Exception {
		SessionData sessionData = (SessionData) UI.getCurrent().getData(); 
		SBMLDocument doc = SBMLReader.read(bais);
		cat.udl.easymodel.logic.model.Model m = new cat.udl.easymodel.logic.model.ModelImpl();
		m.setRepositoryType(RepositoryType.SBML);
		m.setUser(sessionData.getUser());
		
		Model model = doc.getModel();
		m.setName(model.getName());
		m.setDescription(getBodyContent(model.getNotes(),""));
		
		for (Species sp : model.getListOfSpecies()) {
			sp.setName(getFixedSpeciesName(sp.getName()));
		}
		for (Reaction rs : model.getListOfReactions()) {
			cat.udl.easymodel.logic.model.Reaction r = new ReactionImpl(getReactionStringFromReactionSBML(rs));
			m.addReaction(r);
//			p.p(FormulaUtils.getSBMLFormulaKeywordFix(rs.getKineticLaw().getFormula(), keywordFixSufix));
			// add formula
			Formula f = new FormulaImpl(sessionData.getCustomFormulas().getNextFormulaNameByModelShortName(m.getNameShort()), FormulaUtils.getSBMLFormulaKeywordFix(rs.getKineticLaw().getFormula()), FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.SBML);
			sessionData.getCustomFormulas().addFormula(f);
			r.setFormula(f);
			// set formula parameter values
			for (String par : r.getFormulaValues().keySet()) {
				LocalParameter lp = findLocalPar(rs, par);
				if (lp != null) {
					r.getFormulaValues().put(par,new FormulaValueImpl(FormulaValueType.CONSTANT, String.valueOf(lp.getValue())));
				} else {
					if (r.getLeftPartSpecies().containsKey(par)) {
						r.getFormulaValues().put(par,new FormulaValueImpl(FormulaValueType.SUBSTRATE, par));
					} else if (r.getModifiers().containsKey(par)) {
						r.getFormulaValues().put(par,new FormulaValueImpl(FormulaValueType.MODIFIER, par));
					}
				}
			}
		}
		for (Species sp : model.getListOfSpecies()) {
			m.getAllSpecies().get(sp.getName()).setConcentration(String.valueOf(sp.getInitialConcentration()));
			m.getAllSpecies().get(sp.getName()).setVarType(sp.isConstant() ? SpeciesVarTypeType.INDEP : SpeciesVarTypeType.TIMEDEP);
		}
		return m;
	}
	
	private static LocalParameter findLocalPar(Reaction rs, String searchPar) {
		LocalParameter res=null;
		for (LocalParameter lp : rs.getKineticLaw().getListOfLocalParameters()) {
			if (getFixedSpeciesName(lp.getName()).equals(searchPar)) {
				res=lp;
				break;
			}
		}
		return res;
	}
	
	private static String getBodyContent(XMLNode node, String parentTag) {
		if ("".equals(node.getName()) && "p".equals(parentTag)) {
			return node.getCharacters();
		} else {
			for (int i=0; i<node.getChildCount();i++) {
				XMLNode child = node.getChild(i);
				String chars = getBodyContent(child, node.getName());
				if (!chars.equals(""))
					return chars;
			}
		}
		return "";
	}
	
	public static String getFixedSpeciesName(String name) {
		if (name == null)
			return null;
		String res = name;
		if (name.matches(FormulaUtils.getInstance().getKeywordsRegEx()))
			res = res+keywordFixSufix;
		return res;
	}
	
	private static String getReactionStringFromReactionSBML(Reaction r) {
		String res = "";
		for (int i=0; i<r.getReactantCount();i++) {
			SpeciesReference sr = r.getReactant(i);
			if (i>0)
				res += " + ";
			res += String.format("%.0f", sr.getStoichiometry())+"*"+sr.getSpeciesInstance().getName();
		}
		res += " -> ";
		for (int i=0; i<r.getProductCount();i++) {
			SpeciesReference sr = r.getProduct(i);
			if (i>0)
				res += " + ";
			res += String.format("%.0f", sr.getStoichiometry())+"*"+sr.getSpeciesInstance().getName();
		}
		for (int i=0; i<r.getModifierCount();i++) {
			ModifierSpeciesReference sr = r.getModifier(i);
			res += ";"+sr.getSpeciesInstance().getName();
		}
		return res;
	}
	
	private static void updateSpeciesReferencesId(Reaction r, String oldId, String newId) {
		for (int i=0; i<r.getReactantCount();i++) {
			SpeciesReference sr = r.getReactant(i);
			if (sr.getId().equals(oldId))
				sr.setId(newId);
		}
		for (int i=0; i<r.getProductCount();i++) {
			SpeciesReference sr = r.getProduct(i);
			if (sr.getId().equals(oldId))
				sr.setId(newId);
		}
		for (int i=0; i<r.getModifierCount();i++) {
			ModifierSpeciesReference sr = r.getModifier(i);
			if (sr.getId().equals(oldId))
				sr.setId(newId);
		}
	}
}
