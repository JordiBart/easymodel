package cat.udl.easymodel.mathlink;

import java.io.IOException;

import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.PacketArrivedEvent;
import com.wolfram.jlink.PacketListener;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;

public class MathPacketListenerOp implements PacketListener {
	public static final String printPrefix = "MSG::";
	public static final String percentagePrefix = "PGS::";
	public static final String gridStochasticStatisticsPrefix = "GRSTST::";
	public static final String gridCaptionPrefix = "GRCAP::";

	private SessionData sessionData = null;

	public MathPacketListenerOp() {
	}

	public void setSessionData(SessionData sessionData) {
		this.sessionData = sessionData;
	}

	@Override
	public boolean packetArrived(PacketArrivedEvent evt) throws MathLinkException {
		KernelLink ml = (KernelLink) evt.getSource();
		String msg = ml.getString().trim(); // remove leading and trailing spaces
		boolean isDebug = SharedData.getInstance().isDebug();
		if (evt.getPktType() == MathLink.TEXTPKT) {// Print[] and Warnings
			if (sessionData != null) {
				if (msg.startsWith(printPrefix)) {
					msg = mathPrintToOneLine(msg);
					sessionData.getOutVL().out(msg.substring(printPrefix.length()));
				} else if (msg.startsWith(percentagePrefix)) {
					String vals[] = msg.substring(percentagePrefix.length()).split(":");
					sessionData.getOutVL().updateStochasticProgressBar(Integer.valueOf(vals[0]), Float.valueOf(vals[1]));
				} else if (msg.startsWith(gridStochasticStatisticsPrefix)) {
					String vals[] = msg.substring(gridStochasticStatisticsPrefix.length()).split(":");
					sessionData.getOutVL().updateStochasticStatistics(vals);
				} else if (msg.startsWith(gridCaptionPrefix)) {
						msg = mathPrintToOneLine(msg);
						sessionData.getOutVL().setCaptionToGridLayout(msg.substring(gridCaptionPrefix.length()));
				} else if (isDebug) {
					// sessionData.getOutVL().out(msg);
					System.err.println("dbgMath:" + msg);
				}
				if (sessionData.getBioModelsLogs() != null) {
					try {
						sessionData.getBioModelsLogs().isLastSimError = true;
						sessionData.getBioModelsLogs().simLogFile.append(msg + "\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else if (isDebug) {
				System.err.println("dbgMath:" + msg);
			}
		}
		return true;
	}
	
	private String mathPrintToOneLine(String msg) {
		return msg.replaceAll("\\\\?(\\n\\s*)+>\\s*", "");
	}
}
