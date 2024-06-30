package cat.udl.easymodel.views.mainlayout.dialog;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.ToolboxVaadin;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class AboutAppDialog extends Dialog {

	public AboutAppDialog() {
		super();

		this.setModal(true);
		this.setResizable(true);
		this.setDraggable(true);
		this.setWidth("770px");
		this.setHeight("770px");

		VerticalLayout winVL=new VerticalLayout();
		winVL.setPadding(false);
		winVL.setSpacing(true);
		winVL.setSizeFull();
		winVL.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

		VerticalLayout mainVL=new VerticalLayout();
		mainVL.setPadding(false);
		mainVL.setSpacing(true);
		mainVL.setSizeFull();
		mainVL.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
		mainVL.setClassName("scroll");

		HorizontalLayout logoLayout = new HorizontalLayout();
		logoLayout.setHeight("80px");
		logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		Image emLogo = new Image("img/easymodel-logo-236.png", SharedData.appName);
		emLogo.setSizeFull();
		logoLayout.add(emLogo);

		StringBuilder sb = new StringBuilder("<div style=\"font-size: 14px;\">"+
				SharedData.appName + " is a user-friendly web server for model building, simulation, and analysis in systems biology.</br>"+
	"Calculus core is relied on Wolfram Mathematica.</br></br>"+
	"It allows to:"+
	"<ul>" +
				"<li>Create or load preexisting models; including the import of SBML file models.</li>"+
	"<li>Define complex kinetic rates or importing them from the predefined rate list.</li>"+
	"<li>Displaying of the Stoichiometry matrix and the Regulatory matrix.</li>"+
	"<li>Performing deterministic simulations including both Dynamic time course simulation and Steady State finding simulation.</li>"+
	"<li>Performing deterministic gains and sensitivities analysis.</li>"+
	"<li>Performing Steady State stability analysis.</li>"+
	"<li>Performing stochastic simulations; including both the Gillespie method and an efficient tau leaping method for time course simulation.</li>"+
	"<li>Performing parameter scan and independent variable scan.</li>"+
	"<li>Exporting the model in the SBML file format.</li>"+
	"<li>Exporting the model and simulation code in a Mathematica nootebook file.</li>"+
	"<li>Store user models</li>"+
	"<li>And many more</li>" +
				"</ul>"+
						"Project started in 2017 in Universitat de Lleida as a PhD project." +
				"</div>");

		VerticalLayout linksVL= newLinksVL();

		mainVL.add(logoLayout, new Html(sb.toString()));
		winVL.add(ToolboxVaadin.getDialogHeader(this,"About "+SharedData.fullAppName,null), mainVL,linksVL);
		winVL.expand(mainVL);
		this.add(winVL);
	}

	private VerticalLayout newLinksVL() {
		VerticalLayout vl=new VerticalLayout();
		vl.setPadding(false);
		vl.setSpacing(false);
		vl.setWidth("100%");
		vl.setHeight("40px");
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setPadding(false);
		hl1.setSpacing(false);
		hl1.setHeight("40px");
		hl1.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
		hl1.add(getUdlLink(),getMathematicaLink(),getSBMLLink(),getGitHubLink(),getBrowserCompatibilityButton(),getContactButton(),getCounterVL());
		vl.add(hl1);
		return vl;
	}

	private Anchor getUdlLink() {
		Anchor anchor = new Anchor("http://www.udl.cat/ca/en/", "");
		anchor.setTitle("Universitat de Lleida");
		anchor.setWidth("77px");
		anchor.setHeight("40px");
		anchor.setTarget("_blank");
		anchor.setClassName("udlLogo");
		return anchor;
	}

	private Anchor getMathematicaLink() {
		Anchor link = new Anchor("https://www.wolfram.com/mathematica/", "");
		link.setTitle("Powered by Wolfram Mathematica");
		link.setWidth("323px");
		link.setHeight("40px");
		link.setTarget("_blank");
		link.setClassName("coverMathLogo");
		return link;
	}

	private Anchor getSBMLLink() {
		Anchor link = new Anchor("http://sbml.org/", "");
		link.setTitle("SBML project");
		link.setWidth("92px");
		link.setHeight("40px");
		link.setTarget("_blank");
		link.setClassName("sbmlLogo");
		return link;
	}

	private Anchor getGitHubLink() {
		Anchor link = new Anchor("https://github.com/jordibart/easymodel/", "");
		link.setWidth("42px");
		link.setHeight("40px");
		link.setTarget("_blank");
		link.setTitle("GitHub project source-code repository");
		link.setClassName("githubLogo");
		return link;
	}

	private Component getBrowserCompatibilityButton() {
		Button btn = new Button();
		btn.setWidth("40px");
		btn.setHeight("40px");
		btn.setClassName("coverBrowserCompatibility");
		btn.setTooltipText("Browser Compatibility Table");
		btn.addClickListener(event -> {
            new BrowserCompabilityDialog().open();
		});
		return btn;
	}

	private Component getContactButton() {
		Button btn = new Button();
		btn.setWidth("50px");
		btn.setHeight("40px");
		btn.setClassName("coverContact");
		btn.setTooltipText("Contact with the authors");
		btn.addClickListener(event -> {
			new ContactDialog().open();
		});
		return btn;
	}
	private Component getCounterVL() {
		Button btn = new Button();
		btn.setWidth("70px");
		btn.setHeight("40px");
		btn.setClassName("visitCounter");
		btn.setTooltipText("Total visits");
		btn.setText(SharedData.getInstance().getVisitCounterRunnable().getTotalCounter().toString());
		btn.addClickListener(event -> {
			ToolboxVaadin.showInfoNotification("Total visits: "+btn.getText());
		});
		return btn;
	}
}
