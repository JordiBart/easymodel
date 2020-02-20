package cat.udl.easymodel.logic.simconfig;

public class ValueRange {
	private String min;
	private String max;

	public ValueRange(String min, String max) {
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
