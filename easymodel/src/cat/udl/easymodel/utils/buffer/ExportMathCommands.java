package cat.udl.easymodel.utils.buffer;

public interface ExportMathCommands {

	void reset();

	void addCommand(String com);

	String getString();

	void end();

}