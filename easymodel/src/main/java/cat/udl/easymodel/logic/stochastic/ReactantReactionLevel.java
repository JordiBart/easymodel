package cat.udl.easymodel.logic.stochastic;

public class ReactantReactionLevel {
	private String name=null;
	private Integer order=0;
	private Integer stCoef=0; // Stoichiometry coefficient
	
	public ReactantReactionLevel(String name, Integer order, Integer stCoef){
		this.name=name;
		this.order=order;
		this.stCoef=stCoef;
	}

	@Override
	public String toString() {
		return "name "+name+" order "+order+" coef "+stCoef;
	}
	
	public String getName() {
		return name;
	}

	public Integer getOrder() {
		return order;
	}

	public Integer getStCoef() {
		return stCoef;
	}
}
