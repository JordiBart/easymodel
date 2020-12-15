package cat.udl.easymodel.utils;

import java.util.Map;
import java.util.SortedMap;

import cat.udl.easymodel.logic.formula.FormulaValue;
import cat.udl.easymodel.logic.types.FormulaValueType;

public class p {
	static public void p(Object msg) {
		if (msg == null)
			System.out.println("null");
		else
			System.out.println(msg);
	}

	public static void m(Map<String, FormulaValueType> map) {
		System.out.println("Print map");
		for (String key : map.keySet()) {
			System.out.println(key+" - ");
		}
	}

	public static void m2(SortedMap<String, FormulaValue> map) {
		System.out.println("Print map");
		for (String key : map.keySet()) {
			System.out.println(key+" - ");
		}
	}
}
