package cat.udl.easymodel.logic.results;

import cat.udl.easymodel.utils.Utils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;
import org.vaadin.olli.FileDownloadWrapper;

import java.io.ByteArrayInputStream;

public class ResultDownloadButton implements ResultEntry {
    private String caption;
    private String filename;
    private String content;

    public ResultDownloadButton(String content, String caption, String filename) {
        this.caption = caption;
        this.filename = filename;
        this.content=content;
    }

    @Override
    public Component toComponent() {
        Button btn = new Button();
        btn.setIcon(VaadinIcon.DOWNLOAD.create());
        btn.setText(caption);
        FileDownloadWrapper buttonWrapper = new FileDownloadWrapper(
                new StreamResource(Utils.curateForURLFilename(filename), () -> new ByteArrayInputStream(content.getBytes())));
        buttonWrapper.wrapComponent(btn);
        return buttonWrapper;
    }
}
