package cat.udl.easymodel.logic.simconfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CellSizes {
	public class CellSize {
		private String name;
		private Double num;

		public CellSize(String name, Double num) {
			this.name = name;
			this.num = num;
		}

		public String getName() {
			return name;
		}

		public Double getNum() {
			return num;
		}
	}

	private Set<CellSize> cellSizeSet = new HashSet<>();
	private static CellSizes thisSingleton = new CellSizes();

	public CellSizes() {
		cellSizeSet.add(new CellSize("Prokaryotic Cell", 602.41)); // (3.*10^6)/4980 a.k.a. Escherichia coli 
		cellSizeSet.add(new CellSize("Unicellular Eukaryotic", 120482d)); // Prokaryotic * 200
		cellSizeSet.add(new CellSize("Multicellular Eukaryote", 602410d)); // Prokaryotic * 1000
	}

	public static CellSizes getInstance() {
		return thisSingleton;
	}

	public Set<String> getCellSizeNames() {
		Set<String> ret = new HashSet<>();
		for (CellSize cs : this.cellSizeSet)
			ret.add(cs.getName());
		return ret;
	}

	public Double nameToNum(String name) {
		for (CellSize cs : this.cellSizeSet) {
			if (cs.getName().equals(name))
				return cs.getNum();
		}
		return null;
	}
}
