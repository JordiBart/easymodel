package cat.udl.easymodel.mathlink;

import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.PacketArrivedEvent;
import com.wolfram.jlink.PacketListener;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.vcomponent.results.OutVL;

public class MathPacketListener implements PacketListener{

	private OutVL outVL;
	
	public MathPacketListener(OutVL outVL2) {
		outVL = outVL2;
	}
	
	@Override
	public boolean packetArrived(PacketArrivedEvent evt) throws MathLinkException {
	    if (evt.getPktType() == MathLink.TEXTPKT) { //Print[] and Warnings
	    	KernelLink ml = (KernelLink) evt.getSource();
	    	String msg = ml.getString();
	    	if (msg.startsWith(SharedData.mathPrintPrefix))
	    		outVL.out(msg.substring(SharedData.mathPrintPrefix.length()));
	    	else if (SharedData.getInstance().isDebug())
	    		outVL.out(msg);
	    	return true;
	    }
	    return false;
	}
}
