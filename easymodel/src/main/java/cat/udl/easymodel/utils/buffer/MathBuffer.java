package cat.udl.easymodel.utils.buffer;

public class MathBuffer {
	private StringBuffer buf;

	public MathBuffer() {
		buf = new StringBuffer();
	}

	public void reset() {
		buf.delete(0, buf.length());
	}

	public void addCommand(String com) {
		buf.append(com + ";\n");
	}

	public void addCommandRaw(String com) {
		buf.append(com + "\n");
	}
	
	public String getString() {
		if (buf.length() - 1 >= 0)
			buf.deleteCharAt(buf.length() - 1);
		return buf.toString();
	}
}
