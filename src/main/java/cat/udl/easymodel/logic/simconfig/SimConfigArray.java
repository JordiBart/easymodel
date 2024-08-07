package cat.udl.easymodel.logic.simconfig;

import java.util.ArrayList;

public class SimConfigArray extends ArrayList<SimConfigEntry> {
	private static final long serialVersionUID = 1L;

	public SimConfigArray() {
		super();
	}

	public SimConfigArray(SimConfigArray from) {
		super();
		for (SimConfigEntry e : from) {
			this.add(new SimConfigEntry(e));
		}
	}

	public SimConfigEntry get(String id) {
		SimConfigEntry res = null;
		for (SimConfigEntry e : this) {
			if (e.getId().equals(id)) {
				res = e;
				break;
			}
		}
		return res;
	}
}
