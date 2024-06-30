package cat.udl.easymodel.logic.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public enum StochasticMethod {
	SSA(0), TAU_LEAPING(1);
	private final int value;

	StochasticMethod(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static StochasticMethod valueOf(int value) {
		for (StochasticMethod v : values()) {
			if (v.value == value)
				return v;
		}
		return null;
	}
	@Override
	public String toString() {
		switch (value) {
			case 0:
				return "SSA";
			case 1:
				return "Tau-leaping";
		}
		return "Error sim type";
	}
	public static Set<String> getSet(){
		Set<String> ret = new HashSet<>();
		for (StochasticMethod v : values()) {
			ret.add(v.toString());
		}
		return ret;
	}
}
