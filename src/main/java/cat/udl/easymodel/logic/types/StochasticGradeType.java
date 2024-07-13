package cat.udl.easymodel.logic.types;

public enum StochasticGradeType {
    UNCHECKED(-1), NOT_COMPATIBLE(0), SSA(1), TAU_LEAPING(2);
    private final int value;

    StochasticGradeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static StochasticGradeType valueOf(int value) {
        for (StochasticGradeType v : values()) {
            if (v.value == value)
                return v;
        }
        return null;
    }
    @Override
    public String toString() {
        switch (value) {
            case -1:
                return "Unchecked";
            case 0:
                return "Not possible";
            case 1:
                return "SSA";
            case 2:
                return "Tau-leaping";
        }
        return "Error stochastic grade";
    }
}
