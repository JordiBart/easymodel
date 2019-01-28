package cat.udl.easymodel.vcomponent.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Set;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.simconfig.SimConfigEntry;
import cat.udl.easymodel.logic.simconfig.SimConfigSlider;
import cat.udl.easymodel.logic.types.InputType;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.utils.VaadinUtils;

public class SimConfigToComponent {

	public static Component convert(SimConfigEntry en, Set<String> setOfStringForArrayCheckbox) {
		Component comp = null;
		switch (en.getType()) {
		case STRING:
			comp = getString(en);
			break;
		case MATHEXPRESSION:
			comp = getMathExpression(en);
			break;
		case DECIMAL:
			comp = getDecimal(en);
			break;
		case CHECKBOX:
			comp = getCheckBox(en);
			break;
		case SLIDER:
			comp = getSlider(en);
			break;
		case ARRAYCHECKBOX:
			comp = getCheckBoxGroup(en,setOfStringForArrayCheckbox);
			break;
		}
		return comp;
	}

	private static HorizontalLayout getString(SimConfigEntry en) {
		TextField parValueTF = new TextField();
		HorizontalLayout hl = getTextFieldHL(en, parValueTF);
		parValueTF.addBlurListener(new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(BlurEvent event) {
				String newText = ((TextField) event.getComponent()).getValue();
				String newVal = newText;
				en.setValue(newVal);
			}
		});
		// Load previous value
		String prevValue = (String) en.getValue();
		if (prevValue != null)
			parValueTF.setValue(prevValue);
		return hl;
	}

	private static HorizontalLayout getMathExpression(SimConfigEntry en) {
		TextField parValueTF = new TextField();
		HorizontalLayout hl = getTextFieldHL(en, parValueTF);
		parValueTF.addBlurListener(new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(BlurEvent event) {
				String newText = ((TextField) event.getComponent()).getValue();
				String newVal = newText;
				try {
					newVal = Utils.evalMathExpr(newText);
					((TextField) event.getComponent()).setValue(newVal);
				} catch (Exception e) {
					newVal = null;
					((TextField) event.getComponent()).setValue("");
				} finally {
					en.setValue(newVal);
				}
			}
		});
		// Load previous value
		String prevValue = (String) en.getValue();
		if (prevValue != null)
			parValueTF.setValue(prevValue);
		return hl;
	}

	private static HorizontalLayout getDecimal(SimConfigEntry en) {
		TextField parValueTF = new TextField();
		HorizontalLayout hl = getTextFieldHL(en, parValueTF);
		parValueTF.addBlurListener(new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(BlurEvent event) {
				String newText = ((TextField) event.getComponent()).getValue();
				String newVal = newText;
				try {
					BigDecimal testBigDec = new BigDecimal(newVal);
				} catch (Exception e) {
					newVal = null;
					((TextField) event.getComponent()).setValue("");
				} finally {
					en.setValue(newVal);
				}
			}
		});
		// Load previous value
		String prevValue = (String) en.getValue();
		if (prevValue != null)
			parValueTF.setValue(prevValue);
		return hl;
	}

	private static HorizontalLayout getTextFieldHL(SimConfigEntry en, TextField parValueTF) {
		HorizontalLayout line = new HorizontalLayout();
//		line.setImmediate(true);
		line.setWidth("100%");
		TextField parNameTF = new TextField();
		parNameTF.setValue(en.getCaption());
		parNameTF.setWidth("100%");
		parNameTF.setReadOnly(true);

//		parValueTF.setInputPrompt(caption);
		parValueTF.setWidth("100%");
		parValueTF.setId(en.getId());
		parValueTF.setImmediate(true);

		line.addComponents(parNameTF, parValueTF);
		line.setExpandRatio(parNameTF, 1.0f);
		line.setExpandRatio(parValueTF, 2.0f);
		return line;
	}

	private static Slider getSlider(SimConfigEntry en) {
		SimConfigSlider sliderConf = (SimConfigSlider) en.getValue();
		Slider s = new Slider(Double.valueOf(sliderConf.getMin()), Double.valueOf(sliderConf.getMax()), 0);
		s.setCaption(en.getCaption());
		s.setValue(Double.valueOf(sliderConf.getValue()) * sliderConf.getScale());
		s.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Double newVal = (Double) event.getProperty().getValue();
				sliderConf.setValue(String.valueOf(newVal / sliderConf.getScale()));
			}
		});
		return s;
	}

	private static CheckBox getCheckBox(SimConfigEntry en) {
		CheckBox cb = new CheckBox(en.getCaption());
//		cb.setRequired(required);
		cb.setDescription(en.getDescription());
		cb.setValue((Boolean) en.getValue());
		cb.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Boolean newVal = (Boolean) event.getProperty().getValue();
				en.setValue(newVal);
			}
		});
		return cb;
	}
	
	@SuppressWarnings("unchecked")
	private static VerticalLayout getCheckBoxGroup(SimConfigEntry en, Set<String> setOfStringForArrayCheckbox) {
		VerticalLayout vl = new VerticalLayout();
		vl.setCaption(en.getCaption());
		vl.setDescription(en.getDescription());
		for (String depVar : setOfStringForArrayCheckbox) {
			vl.addComponent(getDepVarToShowCheckBox(depVar, (ArrayList<String>) en.getValue()));
		}
		return vl;
	}
	
	private static CheckBox getDepVarToShowCheckBox(String depVar, ArrayList<String> arrDepVar) {
		CheckBox cb = new CheckBox(depVar);
		cb.setData(depVar);
		if (arrDepVar.contains(depVar))
			cb.setValue(true);
		cb.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Boolean newVal = (Boolean) event.getProperty().getValue();
				if (newVal && !arrDepVar.contains(depVar))
					arrDepVar.add(depVar);
				else if (!newVal && arrDepVar.contains(depVar))
					arrDepVar.remove(depVar);
			}
		});
		return cb;
	}
}