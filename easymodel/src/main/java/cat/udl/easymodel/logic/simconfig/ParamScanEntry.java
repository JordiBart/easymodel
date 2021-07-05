package cat.udl.easymodel.logic.simconfig;

import cat.udl.easymodel.controller.ContextUtils;
import cat.udl.easymodel.logic.model.Reaction;
import cat.udl.easymodel.logic.types.ParamScanType;
import cat.udl.easymodel.logic.types.SpeciesType;
import cat.udl.easymodel.utils.Utils;

public class ParamScanEntry {
	private ParamScanType type;
	private String parameterName;
	private String parameterValue;
	
	private boolean isArrayParameter=false;
	private SpeciesType speciesType;
	private String parameterSpeciesName;

	private Reaction reaction;

	private String beginVal = null;
	private String endVal = null;
	private String numIntervals = null;
	private boolean isLogarithmic = false;

	private final Double origValMultiplier = 10d;

	public ParamScanEntry(Reaction reaction, String parameterName, String parameterValue) {
		type = ParamScanType.PARAMETER;
		this.reaction = reaction;
		this.parameterName=parameterName;
		this.parameterValue=parameterValue;
		setInitValues();
	}
	
	public ParamScanEntry(Reaction reaction, String parameterName, String parameterValue, SpeciesType speciesType, String parameterSpeciesName) {
		isArrayParameter=true;
		type = ParamScanType.PARAMETER;
		this.reaction = reaction;
		this.parameterName=parameterName;
		this.parameterValue=parameterValue;
		this.speciesType=speciesType;
		this.parameterSpeciesName=parameterSpeciesName;
		setInitValues();
	}

	public ParamScanEntry(String indVarName, String indVarConcentration) {
		type = ParamScanType.IND_VAR;
		parameterValue=indVarConcentration;
		parameterName=indVarName;
		setInitValues();
	}

	public void copyFrom(ParamScanEntry from) {
		type=from.type;
		parameterName=from.parameterName;
		parameterValue=from.getParameterValue();
		parameterSpeciesName=from.parameterSpeciesName;
		if (from.reaction != null)
			reaction=new Reaction(from.reaction);
		beginVal=from.beginVal;
		endVal = from.endVal;
		numIntervals = from.numIntervals;
		isLogarithmic = from.isLogarithmic;
	}
	
	public Boolean equals(ParamScanEntry e2) {
		return type == e2.getType() && parameterName.equals(e2.parameterName)
				&& getParameterValue().equals(e2.getParameterValue())
				&& (type == ParamScanType.PARAMETER && getReactionId().equals(e2.getReactionId()) && isArrayParameter==e2.isArrayParameter && (!isArrayParameter || (speciesType==e2.speciesType && parameterSpeciesName.equals(e2.parameterSpeciesName)))
						|| type == ParamScanType.IND_VAR);
	}

	private void setInitValues() {
		Double origVal = Double.valueOf(getParameterValue());
		beginVal = Utils.doubleToString(origVal - (Math.abs(origVal) * origValMultiplier));
		if (origVal > 0 && Double.valueOf(beginVal) < 0)
			beginVal = "0";
		endVal = Utils.doubleToString(origVal + (Math.abs(origVal) * origValMultiplier));
		if (origVal < 0 && Double.valueOf(endVal) > 0)
			endVal = "0";
		numIntervals = "10";
	}
	
	public String getReactionId() {
		return reaction.getIdJavaStr();
	}

	public String getFormulaDef() {
		return reaction.getFormula().getFormulaDef();
	}

	public String getParameterValue() {
		return parameterValue;
	}

	public boolean isLogarithmic() {
		return isLogarithmic;
	}

	public void setLogarithmic(boolean isLogarithmic) {
		this.isLogarithmic = isLogarithmic;
	}

	public ParamScanType getType() {
		return type;
	}

	public Reaction getReaction() {
		return reaction;
	}

	public String getMathematicaParamName() {
		switch (type) {
		case PARAMETER:
			if (!isArrayParameter)
				return reaction.getMathematicaContext() + parameterName;
			else
				return reaction.getMathematicaContext() + ContextUtils.arrayContext + parameterName + "`"+ (speciesType==SpeciesType.REACTIVE? ContextUtils.substrateContext:ContextUtils.modifierContext) + parameterSpeciesName;
		case IND_VAR:
			return ContextUtils.modelContext + parameterName;
		default:
			return "no-param";
		}
	}
	
	public String getShowParameterName() {
		switch (type) {
		case PARAMETER:
			if (parameterSpeciesName == null)
				return reaction.getMathematicaContext()+parameterName;
			else
				return reaction.getMathematicaContext()+parameterName + "`"+ (speciesType==SpeciesType.REACTIVE?ContextUtils.substrateContext:ContextUtils.modifierContext) + parameterSpeciesName;
		case IND_VAR:
			return parameterName;
		default:
			return "no-param";
		}
	}
	
	public String getParameterName() {
		return parameterName;
	}
	
	public String getBeginVal() {
		return beginVal;
	}

	public void setBeginVal(String beginVal) {
		this.beginVal = beginVal;
	}

	public String getEndVal() {
		return endVal;
	}

	public void setEndVal(String endVal) {
		this.endVal = endVal;
	}

	public String getNumIntervals() {
		return numIntervals;
	}

	public void setNumIntervals(String numIntervals) {
		this.numIntervals = numIntervals;
	}

	public boolean isArrayParameter() {
		return isArrayParameter;
	}

	public SpeciesType getSpeciesType() {
		return speciesType;
	}

	public String getParameterSpeciesName() {
		return parameterSpeciesName;
	}
}
