package cat.udl.easymodel.logic.simconfig;

import cat.udl.easymodel.logic.types.InputType;

public class SimConfigEntry {

	private String id;
	private Object value;
	private InputType type;
	private String caption;
	private String description;
	private boolean mandatory=true;

	public SimConfigEntry(String id, Object value, InputType type, String caption, String description) {
		this.id = id;
		this.value = value;
		this.type = type;
		this.caption = caption;
		this.description = description;
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

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
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
}
