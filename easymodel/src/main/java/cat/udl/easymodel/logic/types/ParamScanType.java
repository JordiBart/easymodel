package cat.udl.easymodel.logic.types;

public enum ParamScanType {
	PARAMETER(0), IND_VAR(1);

	private int value;

	private ParamScanType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public String getString() {
		switch (value) {
		case (0):
			return "Parameter";
		case (1):
			return "Ind.Var.";
		}
		return "notype";
	}
}
