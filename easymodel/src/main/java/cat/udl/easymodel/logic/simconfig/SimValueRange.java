package cat.udl.easymodel.logic.simconfig;

public class SimValueRange {
	private String min;
	private String max;

	public SimValueRange(String min, String max) {
		this.min=min;
		this.max=max;
	}
	
	public String getMin() {
		return min;
	}

	public String getMax() {
		return max;
	}
}
