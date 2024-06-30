package cat.udl.easymodel.logic.simconfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cat.udl.easymodel.logic.types.InputType;

public class SimConfigEntry {

    private String id;
    private String value; //string or boolean
    private String ogValue;
    private InputType type;
    private String caption;
    private String description;
    private boolean mandatory = true;
    private String minNumericValue=null;
    private String maxNumericValue=null;
    private boolean isEnabled = true;
    private Set<String> optionSet = null;

    public SimConfigEntry(String id, String value, InputType type, String caption, String description) {
        this.id = id;
        this.value = value;
        this.ogValue = value;
        this.type = type;
        this.caption = caption;
        this.description = description;
    }

    public SimConfigEntry(SimConfigEntry from) {
        this.id = from.id;
        this.value = from.value;
        this.ogValue = from.ogValue;
        this.type = from.type;
        this.caption = from.caption;
        this.description = from.description;
        this.mandatory = from.mandatory;
        this.minNumericValue= from.minNumericValue;
        this.maxNumericValue=from.maxNumericValue;
        this.isEnabled = from.isEnabled;
        if (from.optionSet != null) {
            this.optionSet = new HashSet<>();
            this.optionSet.addAll(from.optionSet);
        }
    }

    public void checkValue(String exceptionTitle) throws Exception {
        if (this.mandatory && value == null)
            throw new Exception(this.getCaption() + ": missing value");
        if (this.value instanceof String) {
            if (minNumericValue != null && Double.valueOf((String) this.value) < Double.valueOf(minNumericValue))
                throw new Exception(exceptionTitle + ": " + this.getCaption() + ": minimum value: " + minNumericValue);
            if (maxNumericValue != null && Double.valueOf((String) this.value) > Double.valueOf(maxNumericValue))
                throw new Exception(exceptionTitle + ": " + this.getCaption() + ": maximum value: " + maxNumericValue);
        }
    }

    @Override
    public String toString() {
        if (value != null)
            return value.toString();
        else
            return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public boolean getBooleanValue(){
        return value.equals("1");
    }

    public String getOriginalValue() {
        return ogValue;
    }
    public void resetValue() {
        setValue(ogValue);
    }
    public void setValue(String value) {
        this.value = value;
    }

    public InputType getType() {
        return type;
    }

    public void setType(InputType type) {
        this.type = type;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public void setValueRange(String min, String max) {
        this.minNumericValue=min;
        this.maxNumericValue=max;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Set<String> getOptionSet() {
        return optionSet;
    }

    public void setOptionSet(Set<String> optionSet) {
        this.optionSet = optionSet;
    }

    public String getMaxNumericValue() {
        return maxNumericValue;
    }

    public String getMinNumericValue() {
        return minNumericValue;
    }
}
