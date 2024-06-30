package cat.udl.easymodel.logic.results;

public class ResultStochasticStatsDataElement {
    private String type = "span"; //span spanBold progressBar
    private String value;
    private boolean isDirty = true;

    ResultStochasticStatsDataElement(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
        isDirty = true;
    }

    public String getType() {
        return type;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean isDirty){
        this.isDirty=isDirty;
    }

    public String getValue() {
        return value;
    }
}
