package cat.udl.easymodel.views.error;

import cat.udl.easymodel.main.SharedData;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.servlet.http.HttpServletResponse;

@PageTitle(SharedData.appName + " | Error 404")
public class RouteNotFoundError extends VerticalLayout
        implements HasErrorParameter<NotFoundException> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event,
                                 ErrorParameter<NotFoundException> parameter) {
        this.setSizeFull();
        this.setPadding(true);
        this.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
//        Paragraph p1 = new Paragraph("Oops could not navigate to '/"
//                + event.getLocation().getPath() + "'.");
        Image imgLogo = new Image("img/easymodel-logo-120.png", SharedData.appName);
        Paragraph p2 = new Paragraph("Error 404 page not found.");
        Button goHomeBtn = new Button("Go to home page");
        goHomeBtn.setIcon(VaadinIcon.HOME.create());
        goHomeBtn.addClickListener(e -> {
            this.getUI().ifPresent(ui -> ui.navigate("/"));
        });
        this.add(imgLogo, p2, goHomeBtn);
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
