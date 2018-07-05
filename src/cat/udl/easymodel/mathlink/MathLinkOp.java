package cat.udl.easymodel.mathlink;

import java.util.ArrayList;

import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.utils.CException;

public interface MathLinkOp {

	public void openMathLink() throws MathLinkException;

	public abstract void closeMathLink();

	public abstract void evaluate(String mlCmd) throws MathLinkException;

	public abstract Integer evaluateToInt(String mlCmd) throws MathLinkException;
	
	public abstract Boolean evaluateToBoolean(String mlCmd) throws MathLinkException;
	
	public abstract String evaluateToString(String mlCmd) throws MathLinkException;

	public abstract byte[] evaluateToImage(String mlCmd) throws MathLinkException;

	void checkMathCode(String mlCmd) throws CException, MathLinkException;

	void checkMultiCommands(ArrayList<String> cmds) throws CException, MathLinkException;

	boolean isOpen();

}