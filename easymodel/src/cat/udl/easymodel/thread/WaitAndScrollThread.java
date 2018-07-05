package cat.udl.easymodel.thread;

import com.vaadin.ui.Panel;

public class WaitAndScrollThread extends Thread {
	private Panel outPanelConsole = null;

	public WaitAndScrollThread(Panel outPanelConsole) {
		this.outPanelConsole = outPanelConsole;
	}

	@Override
	public void run() {
		if (outPanelConsole != null) {
			try {
				Thread.sleep(5000);
				outPanelConsole.setScrollTop(2147483000);
			} catch (InterruptedException e) {
			}
		}
	}
}
