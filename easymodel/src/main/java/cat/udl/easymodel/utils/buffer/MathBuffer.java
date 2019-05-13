package cat.udl.easymodel.utils.buffer;

public interface MathBuffer {

	void reset();

	void addCommand(String com);

	String getString();

}