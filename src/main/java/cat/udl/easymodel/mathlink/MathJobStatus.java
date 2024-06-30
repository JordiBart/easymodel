package cat.udl.easymodel.mathlink;

public enum MathJobStatus {
    PENDING(0), RUNNING(1), FINISHED(2);
    private Integer value;

    MathJobStatus(int val) {
        value = val;
    }

    @Override
    public String toString() {
        switch (value) {
            case 0:
                return "Pending";
            case 1:
                return "Running";
            case 2:
                return "Finished";
        }
        return "error";
    }
}
