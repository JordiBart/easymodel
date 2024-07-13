package cat.udl.easymodel.logic.types;

import java.util.HashSet;
import java.util.Set;

public enum SimStochasticMethodType {
    SSA(1), TAU_LEAPING(2);
    private final int value;

    SimStochasticMethodType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static SimStochasticMethodType valueOf(int value) {
        for (SimStochasticMethodType v : values()) {
            if (v.value == value)
                return v;
        }
        return null;
    }
    @Override
    public String toString() {
        switch (value) {
            case 1:
                return "SSA";
            case 2:
                return "Tau-leaping";
        }
        return "Error sim type";
    }
    public static Set<String> getSet(){
        Set<String> ret = new HashSet<>();
        for (SimStochasticMethodType v : values()) {
            ret.add(v.toString());
        }
        return ret;
    }
}
