package cat.udl.easymodel.views.simulationresults.dialog;

import cat.udl.easymodel.logic.results.ResultStochasticStatsDataElement;
import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.mathlink.SimJob;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import cat.udl.easymodel.views.simulationresults.SimulationResultsView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouteConfiguration;
import org.vaadin.olli.ClipboardHelper;

public class ResultsShareDialog extends Dialog {
	private SimJob simJob = null;
	private VerticalLayout centerVL;

	public ResultsShareDialog(SimJob simJob) {
        super();

        this.simJob = simJob;
        this.setWidth("600px");
        this.setHeight("400px");
        this.setModal(true);
        this.setResizable(true);
        setDraggable(true);

        VerticalLayout globalVL = new VerticalLayout();
        globalVL.setSpacing(true);
        globalVL.setPadding(false);
        globalVL.setSizeFull();
        globalVL.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
        globalVL.add(ToolboxVaadin.getDialogHeader(this,"Share simulation results",new InfoDialogButton("Share simulation results", "Select the hyperlink then copy it.",
				"400px", "300px")));

        centerVL = new VerticalLayout();
        centerVL.setSizeFull();
		centerVL.setSpacing(false);
		centerVL.setPadding(false);

        globalVL.add(centerVL);
        globalVL.expand(centerVL);

        this.add(globalVL);

		displayDialogContent();
	}

	private void displayDialogContent() {
		centerVL.removeAll();

		Span title = new Span("Share the SimId "+simJob.getJobId()+ " results using this link.");
		title.getStyle().setFontWeight(600);
		String link = SharedData.getInstance().getProperties().getProperty("web-protocol")+ SharedData.getInstance().getProperties().getProperty("hostname") + "/" + RouteConfiguration.forSessionScope().getUrl(SimulationResultsView.class, simJob.getJobId());
		TextField shareLinkTF = new TextField();
		shareLinkTF.setWidthFull();
		shareLinkTF.setValue(link);
		shareLinkTF.setReadOnly(true);

		Button button = new Button("");
		button.setIcon(VaadinIcon.LINK.create());
		button.addClickListener(e->{
			ToolboxVaadin.showSuccessNotification("URL copied to clipboard!");
		});
		ClipboardHelper clipboardHelper = new ClipboardHelper(link, button);
		HorizontalLayout hl = new HorizontalLayout(shareLinkTF,clipboardHelper);
		hl.expand(shareLinkTF);
		hl.setSpacing(false);
		hl.setPadding(false);
		hl.setWidthFull();
		centerVL.add(title,hl);
	}

	private HorizontalLayout getBottomButtonsHL() {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth("100%");
		hl.setSpacing(true);
		HorizontalLayout spacer = new HorizontalLayout();
		hl.add(spacer, getCloseButton());
		hl.expand(spacer);
		return hl;
	}

	private Button getCloseButton() {
		Button btn = new Button("Close");
		btn.setWidth("150px");
		btn.addClickListener(event-> {
				close();
		});
		return btn;
	}
}
