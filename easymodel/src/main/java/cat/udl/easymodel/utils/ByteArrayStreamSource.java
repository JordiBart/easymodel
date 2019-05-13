package cat.udl.easymodel.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import com.vaadin.server.StreamResource;

public class ByteArrayStreamSource implements StreamResource.StreamSource {
	private static final long serialVersionUID = 1L;
	private byte[] imageByteArray = null;

    public ByteArrayStreamSource(byte[] newBuffer) {
    	imageByteArray = newBuffer;
    }
    
    public InputStream getStream () {
    	if (imageByteArray != null)
    		return new ByteArrayInputStream(imageByteArray);
    	return null;
    }
}
