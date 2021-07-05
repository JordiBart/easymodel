package cat.udl.easymodel.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;

public class DownFileStreamSource implements StreamResource.StreamSource {
	private static final long serialVersionUID = 1L;
	private String fileString = null;

	public DownFileStreamSource(String fileString) {
		this.fileString = fileString;
	}
	
	@Override
	public InputStream getStream() {
		if (fileString != null)
			return new ByteArrayInputStream(fileString.getBytes(StandardCharsets.UTF_8));
		return null;
	}
}
