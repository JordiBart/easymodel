package cat.udl.easymodel.utils;

import java.util.Map;
import java.util.SortedMap;

import cat.udl.easymodel.logic.formula.FormulaValue;
import cat.udl.easymodel.logic.types.FormulaValueType;
import cat.udl.easymodel.main.SharedData;

//print class
public class P {
    public static void P(Object msg) {
        p(msg);
    }

    //print
    public static void p(Object msg) {
        if (msg == null)
            msg = "null";
        System.out.println(msg.toString());
    }

    //error
    public static void e(Object msg) {
        if (msg == null)
            msg = "null";
        System.err.println(msg.toString());
    }

    //debug
    public static void d(Object msg) {
        if (msg == null)
            msg = "null";
        if (SharedData.getInstance().isDebug()) {
            System.out.println("dbg:: " + msg.toString());
        }
    }

    public static void m(Map<String, FormulaValueType> map) {
        System.out.println("Print map");
        for (String key : map.keySet()) {
            System.out.println(key + " - ");
        }
    }

    public static void m2(SortedMap<String, FormulaValue> map) {
        System.out.println("Print map");
        for (String key : map.keySet()) {
            System.out.println(key + " - ");
        }
    }
}
