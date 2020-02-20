package cat.udl.easymodel.mathlink;

import java.util.ArrayList;

import com.wolfram.jlink.MathLinkException;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.CException;
import cat.udl.easymodel.utils.p;

public class MathLinkArray extends ArrayList<MathLinkOp> {
	private static final long serialVersionUID = 1L;

	public MathLinkArray() {
	}

	public MathLinkOp getFreeMathLink() {
		for (MathLinkOp op : this) {
			if (!op.isLocked()) {
				try {
					op.openMathLink(); //for checking
					op.lock();
					return op;
				} catch (MathLinkException e) {
				}
			}
		}
		return null;
	}

	public void openMathLinks() {
		for (int i = 0; i < SharedData.maxMathLinks; i++) {
			try {
				MathLinkOp ml = new MathLinkOp();
				ml.openMathLink();
				this.add(ml);
			} catch (MathLinkException e) {
				break;
			}
		}
	}

	public void closeMathLinks() {
		for (MathLinkOp op : this) {
			op.closeMathLink();
		}
	}
}
