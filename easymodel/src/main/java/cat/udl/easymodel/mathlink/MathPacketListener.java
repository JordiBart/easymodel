package cat.udl.easymodel.mathlink;

import java.io.IOException;

import com.vaadin.ui.UI;
import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.PacketArrivedEvent;
import com.wolfram.jlink.PacketListener;

import cat.udl.easymodel.main.SessionData;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.p;
import cat.udl.easymodel.vcomponent.results.OutVL;

public class MathPacketListener implements PacketListener{

	private SessionData sessionData;
	
	public MathPacketListener(SessionData sessionData) {
		this.sessionData = sessionData;
	}
	
	@Override
	public boolean packetArrived(PacketArrivedEvent evt) throws MathLinkException {
	    if (evt.getPktType() == MathLink.TEXTPKT) { //Print[] and Warnings
	    	KernelLink ml = (KernelLink) evt.getSource();
	    	String msg = ml.getString();
	    	if (sessionData.getBioModelsLogs() != null) {
	    		try {
					sessionData.getBioModelsLogs().simLogFile.append(msg+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    	if (msg.startsWith(SharedData.mathPrintPrefix))
	    		sessionData.getOutVL().out(msg.substring(SharedData.mathPrintPrefix.length()));
	    	else if (SharedData.getInstance().isDebug())
	    		sessionData.getOutVL().out(msg);
	    	return true;
	    }
	    return false;
	}
}
