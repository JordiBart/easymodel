package cat.udl.easymodel.logic.stochastic;

import cat.udl.easymodel.utils.p;

public class ReactantModelLevel {
	private String name=null;
	private Integer hor=0; // highest reaction order
	private Integer hsc=0; // highest stiochiometry coefficient (in reactions where it is hor)

	public ReactantModelLevel(String name, Integer hor, Integer hsc){
		this.name=name;
		this.hor=hor;
		this.hsc=hsc;
//		p.p(this.toString());
	}

	@Override
	public String toString() {
		return "name "+name+" HOR "+hor+" HSC "+hsc;
	}
	
	public String getName() {
		return name;
	}

	public Integer getHOR() {
		return hor;
	}

	public Integer getHSC() {
		return hsc;
	}
}
