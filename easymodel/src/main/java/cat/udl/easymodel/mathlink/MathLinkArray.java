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

	public void cleanMathLinks() {
		ArrayList<MathLinkOp> toRemove = new ArrayList<MathLinkOp>();
		for (MathLinkOp op : this) {
			op.tryTimeout();
			if (op.isClosed()) {
				toRemove.add(op);
			}
		}
		for (MathLinkOp op : toRemove)
			this.remove(op);
	}
	
	public void addMathLink(MathLinkOp op) {
		cleanMathLinks();
		this.add(op);
	}
	
	public void closeMathLinks() {
		for (MathLinkOp op : this) {
			op.closeMathLink();
		}
	}
	
//	public MathLinkOp getFreeMathLink() {
//		for (MathLinkOp op : this) {
//			if (op.isReadyToUse()) {
//				op.lock();
//				return op;
//			}
//		}
//		return null;
//	}
//
//	public void openMathLinks() {
//		for (int i = 0; i < SharedData.maxMathLinks; i++) {
//			try {
//				MathLinkOp ml = new MathLinkOp();
//				ml.openMathLink();
//				this.add(ml);
//			} catch (MathLinkException e) {
//				break;
//			}
//		}
//	}
}
