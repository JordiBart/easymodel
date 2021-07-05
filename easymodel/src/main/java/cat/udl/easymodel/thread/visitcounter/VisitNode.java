package cat.udl.easymodel.thread.visitcounter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import org.json.JSONException;
import org.json.JSONObject;

public class VisitNode implements java.io.Serializable {
	private static final long serialVersionUID = 4669989455287395507L;
	public String ip = null;
	public LocalDateTime lastVisit = null;
	public Integer counter = 0;
	public String geoJson = null;

	public VisitNode(String ip) {
		this.ip = ip;
		inc();
	}

	public void inc() {
		if (ip != null) {
			lastVisit = LocalDateTime.now();
			counter++;
			updateFromAPI(ip);
		}
	}

	private void updateFromAPI(String ip) {
		try {
			String urlBase = "http://ip-api.com/json/";
			InputStream is = new URL(urlBase + ip).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			JSONObject json = new JSONObject(readAll(rd));
			this.geoJson = json.toString();
			is.close();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
	}

	private String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
}
