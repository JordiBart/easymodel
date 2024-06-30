package cat.udl.easymodel.views.simulationresults.dialog;

import cat.udl.easymodel.logic.model.Species;
import cat.udl.easymodel.mathlink.SimJob;
import cat.udl.easymodel.utils.ToolboxVaadin;
import cat.udl.easymodel.vcomponent.common.InfoDialogButton;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.util.ArrayList;

public class RunStatsDialog extends Dialog {
	private SimJob simJob = null;
	private VerticalLayout centerVL;

	public RunStatsDialog(SimJob simJob) {
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
        globalVL.add(ToolboxVaadin.getDialogHeader(this,"Simulation Run Stats",new InfoDialogButton("Simulation Run Stats", "Simulation Run Stats",
				"400px", "300px")));

        centerVL = new VerticalLayout();
        centerVL.setSizeFull();
        centerVL.setClassName("scroll");
		centerVL.setSpacing(false);
		centerVL.setPadding(false);

//        HorizontalLayout headHL = new HorizontalLayout();
//        headHL.setWidth("100%");
//        VerticalLayout spacer = new VerticalLayout();
//        headHL.add(spacer);
//        headHL.add();
//        headHL.expand(spacer);

        globalVL.add(centerVL);
        globalVL.expand(centerVL);

        this.add(globalVL);
//		p.p("dbg-end linkreaction create");

		displayDialogContent();
	}

	private void displayDialogContent() {
		centerVL.removeAll();

		TextField creationTFCaption = new TextField();
		creationTFCaption.setWidth("150px");
		creationTFCaption.setValue("Creation time");
		creationTFCaption.setReadOnly(true);
		TextField creationTFValue = new TextField();
		creationTFValue.setWidthFull();
		creationTFValue.setValue(simJob.getCreationDate().toString());
		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setWidthFull();
		hl1.setSpacing(false);
		hl1.add(creationTFCaption,creationTFValue);

		TextField startTFCaption = new TextField();
		startTFCaption.setWidth("150px");
		startTFCaption.setValue("Start time");
		startTFCaption.setReadOnly(true);
		TextField startTFValue = new TextField();
		startTFValue.setWidthFull();
		startTFValue.setValue(simJob.getStartDate()!=null?simJob.getStartDate().toString():"");
		HorizontalLayout hl2 = new HorizontalLayout();
		hl2.setWidthFull();
		hl2.setSpacing(false);
		hl2.add(startTFCaption,startTFValue);

		TextField finishTFCaption = new TextField();
		finishTFCaption.setWidth("150px");
		finishTFCaption.setValue("Finish time");
		finishTFCaption.setReadOnly(true);
		TextField finishTFValue = new TextField();
		finishTFValue.setWidthFull();
		finishTFValue.setValue(simJob.getFinishDate()!=null?simJob.getFinishDate().toString():"");
		HorizontalLayout hl3 = new HorizontalLayout();
		hl3.setWidthFull();
		hl3.setSpacing(false);
		hl3.add(finishTFCaption,finishTFValue);

		centerVL.add(hl1,hl2,hl3);
//		centerVL.expand(grid);
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
