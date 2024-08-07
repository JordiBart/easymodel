package cat.udl.easymodel.mathlink;

import java.io.IOException;

import cat.udl.easymodel.controller.BioModelsLogs;
import cat.udl.easymodel.logic.results.ResultList;
import cat.udl.easymodel.logic.results.ResultText;
import cat.udl.easymodel.views.simulationresults.OutVL;
import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.PacketArrivedEvent;
import com.wolfram.jlink.PacketListener;

import cat.udl.easymodel.main.SharedData;

public class MathPacketListenerOp implements PacketListener {
	public static final String printPrefix = "MSG::";
	public static final String percentagePrefix = "PGS::";
	public static final String gridStochasticStatisticsPrefix = "GRSTST::";
	public static final String gridCaptionPrefix = "GRCAP::";
	public static final String debugPrefix = "DBG::";
	private boolean isDebug = SharedData.getInstance().isDebug();
	private ResultList resultList;
	private BioModelsLogs bioModelsLogs;

	public MathPacketListenerOp(ResultList resultList, BioModelsLogs bioModelsLogs) {
		this.resultList=resultList;
		this.bioModelsLogs=bioModelsLogs;
	}

	@Override
	public boolean packetArrived(PacketArrivedEvent evt) throws MathLinkException {
		KernelLink ml = (KernelLink) evt.getSource();
		String msg = ml.getString().trim(); // remove leading and trailing spaces
		if (evt.getPktType() == MathLink.TEXTPKT) {// Print[] and Warnings
			if (resultList != null) {
				if (msg.startsWith(printPrefix)) {
					msg = mathPrintToOneLine(msg);
					resultList.add(new ResultText(msg.substring(printPrefix.length()),null));
				} else if (msg.startsWith(percentagePrefix)) {
					String vals[] = msg.substring(percentagePrefix.length()).split(":");
					resultList.updateStochasticProgressBar(Integer.valueOf(vals[0]), vals[1]);
				} else if (msg.startsWith(gridStochasticStatisticsPrefix)) {
					String vals[] = msg.substring(gridStochasticStatisticsPrefix.length()).split(":");
					resultList.updateStochasticStatistics(vals);
				} else if (msg.startsWith(gridCaptionPrefix)) {
						msg = mathPrintToOneLine(msg);
//						sessionData.getOutVL().setCaptionToGridLayout(msg.substring(gridCaptionPrefix.length()));
				}
			}
			if (bioModelsLogs != null) {
				try {
					bioModelsLogs.isLastSimError = true;
					bioModelsLogs.simLogFile.append(msg + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (isDebug) {
				if (msg.startsWith(debugPrefix)) {
					msg = mathPrintToOneLine(msg);
					System.err.println(debugPrefix + msg);
				}
				else
					System.err.println("dbgMath:: " + msg);
			}
		}
		return true;
	}

	public void setBioModelsLogs(BioModelsLogs bioModelsLogs) {
		this.bioModelsLogs = bioModelsLogs;
	}

	private String mathPrintToOneLine(String msg) {
		return msg.replaceAll("\\\\?(\\n\\s*)+>\\s*", "");
	}
}
