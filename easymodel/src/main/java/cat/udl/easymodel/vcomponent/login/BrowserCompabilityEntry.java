package cat.udl.easymodel.vcomponent.login;

public class BrowserCompabilityEntry {
	private String osName;
	private String osVersion;
	private String chrome;
	private String firefox;
	private String edge;
	private String safari;
	
	public BrowserCompabilityEntry(String osName, String osVersion, String chrome, String firefox, String edge, String safari) {
		this.osName=osName;
		this.osVersion=osVersion;
		this.chrome=chrome;
		this.firefox=firefox;
		this.edge=edge;
		this.safari=safari;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getChrome() {
		return chrome;
	}

	public void setChrome(String chrome) {
		this.chrome = chrome;
	}

	public String getFirefox() {
		return firefox;
	}

	public void setFirefox(String firefox) {
		this.firefox = firefox;
	}

	public String getEdge() {
		return edge;
	}

	public void setEdge(String edge) {
		this.edge = edge;
	}

	public String getSafari() {
		return safari;
	}

	public void setSafari(String safari) {
		this.safari = safari;
	}
}
