package cat.udl.easymodel.utils.buffer;

public class MathBufferImpl implements MathBuffer {
	private StringBuffer buf;

	public MathBufferImpl() {
		buf = new StringBuffer();
	}

	@Override
	public void reset() {
		buf.delete(0, buf.length());
	}

	@Override
	public void addCommand(String com) {
		buf.append(com + ";\n");
	}

	@Override
	public String getString() {
		if (buf.length() - 1 >= 0)
			buf.deleteCharAt(buf.length() - 1);
		return buf.toString();
	}
}
