package cat.udl.easymodel.logic.simconfig;

public class SimConfigSlider {
	private String value;
	private String min;
	private String max;
	private double scale;
	
	public SimConfigSlider(String value, String min, String max, double scale) {
		this.value=value;
		this.min=min;
		this.max=max;
		this.scale=scale;
	}

	@Override
	public String toString() {
		return value;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}
	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}
}
