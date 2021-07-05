//package cat.udl.easymodel.persistence;
//
//import java.sql.SQLException;
//
//public class HardCodedModels {
//
//	private SessionData sessionData=null;
//	
//	public HardCodedModels(SessionData sessionData) {
//		this.sessionData = sessionData;
//	}
//	
//	public void loadModels() {
//		int modelId, r;
//		FormulaValue fv;
//		try {
//			sessionData.getCustomFormulas().loadDB();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		// Load of predefined model example for Sensitivities + Stability
//		sessionData.getModels().addModel(new ModelImpl());
//		modelId = sessionData.getModels().size();
//		sessionData.getModels().getModelByIdJava(modelId).setUser(sessionData.getUser());
//		sessionData.getModels().getModelByIdJava(modelId).setName("Test Gains/Sens.");
//		sessionData.getModels().getModelByIdJava(modelId).setDescription("Test Gains/Sensitivities + Stability");
//		
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl());
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl());
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl());
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl());
//		sessionData.getModels().getModelByIdJava(modelId).get(0).setReactionStr("X0 -> X1;-X3");
//		sessionData.getModels().getModelByIdJava(modelId).get(1).setReactionStr("X1 -> X2");
//		sessionData.getModels().getModelByIdJava(modelId).get(2).setReactionStr("X2 -> X3");
//		sessionData.getModels().getModelByIdJava(modelId).get(3).setReactionStr("X3 ->");
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("X1").setConcentration((Double) 0.5d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("X2").setConcentration((Double) 0.5d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("X3").setConcentration((Double) 0.5d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("X0").setConcentration((Double) 1.0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("X0").setVarType(SpeciesVarTypeType.INDEP);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("X3").setVarType(SpeciesVarTypeType.TIMEDEP);
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("fs1", "k1*X0^g10*(X3)^g13", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("fs2", "k2*(X1)^g21", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("fs3", "k3*(X2)^g32", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("fs4", "k4*(X3)^g43", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
//		r = 0;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("fs1"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k1", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1.5));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("X0", new FormulaValueImpl(FormulaValueType.MODIFIER, "X0"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("g10", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("X3", new FormulaValueImpl(FormulaValueType.MODIFIER, "X3"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("g13", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) (-1.5d)));
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("fs2"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k2", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("X1", new FormulaValueImpl(FormulaValueType.SUBSTRATE, "X1"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("g21", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1d));
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("fs3"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k3", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("X2", new FormulaValueImpl(FormulaValueType.SUBSTRATE, "X2"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("g32", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1d));
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("fs4"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k4", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("X3", new FormulaValueImpl(FormulaValueType.SUBSTRATE, "X3"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("g43", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1d));
//		
//		sessionData.getModels().getModelByIdJava(modelId).getSimDynConfig().put("Tf", (Double) 100d);
//		sessionData.getModels().getModelByIdJava(modelId).getSimDynConfig().put("TSteps", (Double) 0.1d);
//		sessionData.getModels().getModelByIdJava(modelId).getSimSSConfig().put("Tf", (Double) 100d);
//		sessionData.getModels().getModelByIdJava(modelId).getSimSSConfig().put("TSteps", (Double) 0.1d);
//		// Load of predefined model example for dyn. sim. and steady state
//		sessionData.getModels().addModel(new ModelImpl());
//		modelId = sessionData.getModels().size();
//		sessionData.getModels().getModelByIdJava(modelId).setUser(sessionData.getUser());
//		sessionData.getModels().getModelByIdJava(modelId).setName("Test dynamic/ss");
//		sessionData.getModels().getModelByIdJava(modelId).setDescription("Test dynamic and steady state simulations");
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl());
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl());
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl());
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl());
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl());
//		sessionData.getModels().getModelByIdJava(modelId).get(0).setReactionStr("->X1");
//		sessionData.getModels().getModelByIdJava(modelId).get(1).setReactionStr("X2->");
//		sessionData.getModels().getModelByIdJava(modelId).get(2).setReactionStr("2 X1 -> X2");
//		sessionData.getModels().getModelByIdJava(modelId).get(3).setReactionStr("X2 -> X3");
//		sessionData.getModels().getModelByIdJava(modelId).get(4).setReactionStr("X3 -> 2 X1");
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("X1").setConcentration((Double) 0.2d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("X2").setConcentration((Double) 0.4d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("X3").setConcentration((Double) 0.4d);
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("", "k0", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("", "k4*X2", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("", "k1*X1^2", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("", "k2*X2", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("", "k3*X3", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
//		r = 0;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("d1"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k0", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1d));
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("d2"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k4", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("X2", new FormulaValueImpl(FormulaValueType.SUBSTRATE, "X2"));
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("d3"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k1", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("X1", new FormulaValueImpl(FormulaValueType.SUBSTRATE, "X1"));
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("d4"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k2", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("X2", new FormulaValueImpl(FormulaValueType.SUBSTRATE, "X2"));
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("d5"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k3", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("X3", new FormulaValueImpl(FormulaValueType.SUBSTRATE, "X3"));
//		
//		// Load of predefined model brusselator (COPASI)
//		sessionData.getModels().addModel(new ModelImpl());
//		modelId = sessionData.getModels().size();
//		sessionData.getModels().getModelByIdJava(modelId).setUser(sessionData.getUser());
//		sessionData.getModels().getModelByIdJava(modelId).setName("Brusselator");
//		sessionData.getModels().getModelByIdJava(modelId).setDescription("Based on COPASI example");
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("AA -> XX"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("2 * XX + Y -> 3 * XX"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("XX + B -> Y + D"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("XX -> F"));
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("AA").setConcentration((Double) 0.4999998755d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("AA").setVarType(SpeciesVarTypeType.INDEP);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("B").setConcentration((Double) 2.999995932d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("B").setVarType(SpeciesVarTypeType.INDEP);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("D").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("D").setVarType(SpeciesVarTypeType.INDEP);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("F").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("F").setVarType(SpeciesVarTypeType.INDEP);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("XX").setConcentration((Double) 2.999995932d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("Y").setConcentration((Double) 2.999995932d);
//		
//		sessionData.getModels().getModelByIdJava(modelId).getSimDynConfig().put("Tf", (Double) 50d);
//		sessionData.getModels().getModelByIdJava(modelId).getSimDynConfig().put("TSteps", (Double) 0.1d);
//		sessionData.getModels().getModelByIdJava(modelId).getSimSSConfig().put("Tf", (Double) 50d);
//		sessionData.getModels().getModelByIdJava(modelId).getSimSSConfig().put("TSteps", (Double) 0.1d);
//		for (int i = 0; i < 4; i++) {
//			sessionData.getModels().getModelByIdJava(modelId).get(i)
//					.setFormula(sessionData.getPredefinedFormulas().getFormulaByName("Mass action functional form"));
//			fv = new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1.0);
//			sessionData.getModels().getModelByIdJava(modelId).get(i)
//					.getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("a", fv);
//		}
//		// Load of predefined model peroxidase (COPASI)
//		sessionData.getModels().addModel(new ModelImpl());
//		modelId = sessionData.getModels().size();
//		sessionData.getModels().getModelByIdJava(modelId).setUser(sessionData.getUser());
//		sessionData.getModels().getModelByIdJava(modelId).setName("Peroxidase");
//		sessionData.getModels().getModelByIdJava(modelId).setDescription("Based on COPASI example");
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("NADH+O2->H2O2+NAD"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("per3+H2O2->coI"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("ArH+coI->Ar+coII"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("coII+ArH->per3+Ar"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("NADrad+O2->NAD+super"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("per3+super->coIII"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("2*super->H2O2+O2"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("NADrad+coIII->NAD+coI"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("2*NADrad->NAD2"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("per3+NADrad->per2+NAD"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("per2+O2->coIII"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("NADHres->NADH"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("O2g->O2"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("O2->O2g"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("NADH+Ar->NADrad+ArH"));
//		
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("H2O2+NAD->NADH+O2"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("coI->per3+H2O2"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("Ar+coII->ArH+coI"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("per3+Ar->coII+ArH"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("NAD+super->NADrad+O2"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("coIII->per3+super"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("H2O2+O2->2*super"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("NAD+coI->NADrad+coIII"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("NAD2->2*NADrad"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("per2+NAD->per3+NADrad"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("coIII->per2+O2"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("NADH->NADHres"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("O2->O2g"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("O2g->O2"));
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl("NADrad+ArH->NADH+Ar"));
//		
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("Ar").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("ArH").setConcentration((Double) 500d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("coI").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("coII").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("coIII").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("H2O2").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("NAD").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("NAD").setVarType(SpeciesVarTypeType.INDEP);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("NAD2").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("NAD2").setVarType(SpeciesVarTypeType.INDEP);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("NADH").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("NADHres").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("NADHres")
//				.setVarType(SpeciesVarTypeType.INDEP);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("NADrad").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("O2").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("O2g").setConcentration((Double) 12d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("O2g").setVarType(SpeciesVarTypeType.INDEP);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("per2").setConcentration((Double) 0d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("per3").setConcentration((Double) 1.4d);
//		sessionData.getModels().getModelByIdJava(modelId).getAllSpecies().get("super").setConcentration((Double) 0d);
//		
//		sessionData.getModels().getModelByIdJava(modelId).getSimDynConfig().put("Tf", (Double) 500d);
//		sessionData.getModels().getModelByIdJava(modelId).getSimDynConfig().put("TSteps", (Double) 10d);
//		sessionData.getModels().getModelByIdJava(modelId).getSimSSConfig().put("Tf", (Double) 500d);
//		sessionData.getModels().getModelByIdJava(modelId).getSimSSConfig().put("TSteps", (Double) 10d);
//		
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v1", "k1*NADH*O2", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v2", "k2*H2O2*per3", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v3", "k3*coI*ArH", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v4", "k4*coII*ArH", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v5", "k5*NADrad*O2", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v6", "k6*super*per3", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v7", "k7*super*super", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v8", "k8*coIII*NADrad", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v9", "k9*NADrad*NADrad", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v10", "k10*per3*NADrad", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v11", "k11*per2*O2", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v12", "k12", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v131", "k13f*O2g", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v132", "k13b*O2", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_4_v14", "k14*Ar*NADH", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v1", "k1", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v2", "k2", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v3", "k3", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v4", "k4", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v5", "k5", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v6", "k6", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v7", "k7", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v8", "k8", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v9", "k9", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v10", "k10", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v11", "k11", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v12", "k12", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v131", "k13f", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v132", "k13b", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
////		sessionData.getCustomFormulas().addFormula(new FormulaImpl("function_42_v14", "k14", FormulaType.CUSTOM, sessionData.getUser(), RepositoryType.PRIVATE));
//		
//		r = 0;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v1"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k1", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0.000003));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("NADH", new FormulaValueImpl(FormulaValueType.MODIFIER, "NADH"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("O2", new FormulaValueImpl(FormulaValueType.MODIFIER, "O2"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v2"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k2", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 18d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("H2O2", new FormulaValueImpl(FormulaValueType.MODIFIER, "H2O2"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("per3", new FormulaValueImpl(FormulaValueType.MODIFIER, "per3"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v3"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k3", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0.15d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("coI", new FormulaValueImpl(FormulaValueType.MODIFIER, "coI"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("ArH", new FormulaValueImpl(FormulaValueType.MODIFIER, "ArH"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v4"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k4", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0.0052));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("coII", new FormulaValueImpl(FormulaValueType.MODIFIER, "coII"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("ArH", new FormulaValueImpl(FormulaValueType.MODIFIER, "ArH"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v5"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k5", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 20d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("NADrad", new FormulaValueImpl(FormulaValueType.MODIFIER, "NADrad"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("O2", new FormulaValueImpl(FormulaValueType.MODIFIER, "O2"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v6"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k6", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 17d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("super", new FormulaValueImpl(FormulaValueType.MODIFIER, "super"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("per3", new FormulaValueImpl(FormulaValueType.MODIFIER, "per3"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v7"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k7", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 20d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("super", new FormulaValueImpl(FormulaValueType.MODIFIER, "super"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v8"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k8", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 40d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("coIII", new FormulaValueImpl(FormulaValueType.MODIFIER, "coIII"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("NADrad", new FormulaValueImpl(FormulaValueType.MODIFIER, "NADrad"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v9"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k9", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 60d));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("NADrad", new FormulaValueImpl(FormulaValueType.MODIFIER, "NADrad"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v10"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k10", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1.8));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("per3", new FormulaValueImpl(FormulaValueType.MODIFIER, "per3"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("NADrad", new FormulaValueImpl(FormulaValueType.MODIFIER, "NADrad"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v11"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k11", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0.1));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("per2", new FormulaValueImpl(FormulaValueType.MODIFIER, "per2"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("O2", new FormulaValueImpl(FormulaValueType.MODIFIER, "O2"));
//
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v12"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k12", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0.08));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v131"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k13f", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0.006));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("O2g", new FormulaValueImpl(FormulaValueType.MODIFIER, "O2g"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v132"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k13b", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0.006));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("O2", new FormulaValueImpl(FormulaValueType.MODIFIER, "O2"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_4_v14"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k14", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0.7));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("Ar", new FormulaValueImpl(FormulaValueType.MODIFIER, "Ar"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("NADH", new FormulaValueImpl(FormulaValueType.MODIFIER, "NADH"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v1"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k1", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("NADH", new FormulaValueImpl(FormulaValueType.MODIFIER, "NADH"));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("O2", new FormulaValueImpl(FormulaValueType.MODIFIER, "O2"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v2"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k2", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("H2O2", new FormulaValueImpl(FormulaValueType.MODIFIER, "H2O2"));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("per3", new FormulaValueImpl(FormulaValueType.MODIFIER, "per3"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v3"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k3", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("coI", new FormulaValueImpl(FormulaValueType.MODIFIER, "coI"));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("ArH", new FormulaValueImpl(FormulaValueType.MODIFIER, "ArH"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v4"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k4", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("coII", new FormulaValueImpl(FormulaValueType.MODIFIER, "coII"));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("ArH", new FormulaValueImpl(FormulaValueType.MODIFIER, "ArH"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v5"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k5", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("NADrad", new FormulaValueImpl(FormulaValueType.MODIFIER, "NADrad"));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("O2", new FormulaValueImpl(FormulaValueType.MODIFIER, "O2"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v6"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k6", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("super", new FormulaValueImpl(FormulaValueType.MODIFIER, "super"));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("per3", new FormulaValueImpl(FormulaValueType.MODIFIER, "per3"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v7"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k7", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("super", new FormulaValueImpl(FormulaValueType.MODIFIER, "super"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v8"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k8", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("coIII", new FormulaValueImpl(FormulaValueType.MODIFIER, "coIII"));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("NADrad", new FormulaValueImpl(FormulaValueType.MODIFIER, "NADrad"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v9"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k9", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("NADrad", new FormulaValueImpl(FormulaValueType.MODIFIER, "NADrad"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v10"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k10", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("per3", new FormulaValueImpl(FormulaValueType.MODIFIER, "per3"));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("NADrad", new FormulaValueImpl(FormulaValueType.MODIFIER, "NADrad"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v11"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k11", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("per2", new FormulaValueImpl(FormulaValueType.MODIFIER, "per2"));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("O2", new FormulaValueImpl(FormulaValueType.MODIFIER, "O2"));
//
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v12"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k12", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v131"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k13f", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("O2g", new FormulaValueImpl(FormulaValueType.MODIFIER, "O2g"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v132"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k13b", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("O2", new FormulaValueImpl(FormulaValueType.MODIFIER, "O2"));
//		
//		r++;
//		sessionData.getModels().getModelByIdJava(modelId).get(r).setFormula(sessionData.getCustomFormulas().getFormulaByName("function_42_v14"));
//		sessionData.getModels().getModelByIdJava(modelId).get(r).getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("k14", new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 0d));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("Ar", new FormulaValueImpl(FormulaValueType.MODIFIER, "Ar"));
////		sessionData.getModels().getModelById(modelId).get(r).getFormulaValues(sessionData.getModels().getModelById(modelId)).put("NADH", new FormulaValueImpl(FormulaValueType.MODIFIER, "NADH"));
//		// Create new Model!
//		sessionData.getModels().addModel(new ModelImpl());
//		modelId = sessionData.getModels().size();
//		sessionData.getModels().getModelByIdJava(modelId).setRepositoryType(RepositoryType.PRIVATE);
//		sessionData.getModels().getModelByIdJava(modelId).setUser(sessionData.getUser());
//		sessionData.getModels().getModelByIdJava(modelId).setName("New model");
//		sessionData.getModels().getModelByIdJava(modelId).setDescription("An empty model");
//		sessionData.getModels().getModelByIdJava(modelId).addReaction(new ReactionImpl(""));
//		
//////		sessionData.getModels().getModelByIdJava(modelId).getSimDynConfig().put("Tf", (Double) 50d);
//////		sessionData.getModels().getModelByIdJava(modelId).getSimDynConfig().put("TSteps", (Double) 0.1d);
////		sessionData.getModels().getModelByIdJava(modelId).getSimSSConfig().put("Tf", (Double) 50d);
////		sessionData.getModels().getModelByIdJava(modelId).getSimSSConfig().put("TSteps", (Double) 0.1d);
////		for (int i = 0; i < 4; i++) {
////			sessionData.getModels().getModelByIdJava(modelId).get(i)
////					.setFormula(sessionData.getPredefinedFormulas().getFormula("Mass action functional form"));
////			fv = new FormulaValueImpl(FormulaValueType.CONSTANT, (Double) 1.0);
////			sessionData.getModels().getModelByIdJava(modelId).get(i)
////					.getFormulaValues(sessionData.getModels().getModelByIdJava(modelId)).put("a", fv);
////		}
////		try {
////			sessionData.getCustomFormulas().saveDB();
////		} catch (SQLException e) {
////			
////		}
//	}
//
//}
