package cat.udl.easymodel.logic.results;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import org.vaadin.olli.BrowserOpener;

import java.io.ByteArrayInputStream;

public class ResultDynamicImage implements ResultEntry {
        private byte[] imageByteArray;
        private String filename, alt, imgWidth;

        public ResultDynamicImage(String filename, byte[] imageArray, String alt, String imgWidth) {
            this.filename = filename;
            this.imageByteArray = imageArray;
            this.alt = alt != null ? alt : "Image";
            this.imgWidth = imgWidth;
        }

    @Override
    public Component toComponent() {
        if (filename != null && imageByteArray != null) {
            StreamResource resource = new StreamResource(filename, () -> new ByteArrayInputStream(imageByteArray));
            Image img = new Image(resource, alt);
//            img.setClassName("results");
            img.getStyle().set("box-shadow", "0 0 10px 0 rgba(0, 0, 0, 0.2)");
            if (imgWidth != null)
                img.setWidth(imgWidth);
            BrowserOpener bo = new BrowserOpener();
            bo.setContent(img);
            bo.setUrl(img.getSrc());
            bo.setWindowName(filename);
            return bo;
        }
        return new VerticalLayout();
    }
}
