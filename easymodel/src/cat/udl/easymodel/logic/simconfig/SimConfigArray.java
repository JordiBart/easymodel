package cat.udl.easymodel.logic.simconfig;

import java.util.ArrayList;

public class SimConfigArray extends ArrayList<SimConfigEntry> {

	public SimConfigArray() {
		super();
	}
	
//	@Override
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
