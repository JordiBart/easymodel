package cat.udl.easymodel.mathlink;

import java.util.HashSet;

import cat.udl.easymodel.main.SessionData;

public class MathLinkFactory {
	HashSet<MathLinkOp> mlSet = new HashSet<>();

	public MathLinkFactory() {
	}

	public MathLinkOp getFreeMathLink(SessionData sessionData) {
		try {
			MathLinkOp ml = new MathLinkOp(sessionData);
			addMathLink(ml);
			return ml;
		} catch (Exception e) {
			return null;
		}
	}
	
	public void addMathLink(MathLinkOp op) {
		cleanMathLinks();
		mlSet.add(op);
	}
	
	public void cleanMathLinks() {
		HashSet<MathLinkOp> toRemove = new HashSet<MathLinkOp>();
		for (MathLinkOp op : mlSet) {
			op.tryTimeout();
			if (!op.isOpen()) {
				toRemove.add(op);
			}
		}
		for (MathLinkOp op : toRemove)
			mlSet.remove(op);
	}
	
	public void closeMathLinks() {
		for (MathLinkOp op : mlSet) {
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
