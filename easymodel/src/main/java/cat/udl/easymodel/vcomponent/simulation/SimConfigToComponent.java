package cat.udl.easymodel.vcomponent.simulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Set;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import cat.udl.easymodel.logic.formula.FormulaUtils;
import cat.udl.easymodel.logic.model.Model;
import cat.udl.easymodel.logic.simconfig.CellSizes;
import cat.udl.easymodel.logic.simconfig.CellSizes.CellSize;
import cat.udl.easymodel.logic.simconfig.SimConfigEntry;
import cat.udl.easymodel.logic.simconfig.SimConfigSlider;
import cat.udl.easymodel.logic.types.InputType;
import cat.udl.easymodel.utils.Utils;
import cat.udl.easymodel.utils.ToolboxVaadin;

public class SimConfigToComponent {

	public static Component convert(SimConfigEntry en, Set<String> stringSet) {
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
			comp = getCheckBoxGroup(en, stringSet);
			break;
		case NATURAL:
			comp = getNatural(en);
			break;
		case SELECT:
			comp = getSelect(en);
			break;
		}
		return comp;
	}

	private static HorizontalLayout getString(SimConfigEntry en) {
		TextField parValueTF = new TextField();
		HorizontalLayout hl = getComponentHL(en, parValueTF);
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
		HorizontalLayout hl = getComponentHL(en, parValueTF);
		parValueTF.addBlurListener(new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(BlurEvent event) {
				String newText = ((TextField) event.getComponent()).getValue();
				String newVal=newText;
				try {
					Utils.evalMathExpr(newText);
					//((TextField) event.getComponent()).setValue(newVal);
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
		HorizontalLayout hl = getComponentHL(en, parValueTF);
		parValueTF.addBlurListener(new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(BlurEvent event) {
				String newText = ((TextField) event.getComponent()).getValue();
				String newVal = newText;
				try {
					BigDecimal testBigDec = new BigDecimal(newVal);
//					if (minVal != null && testBigDec.compareTo(minVal) < 0)
//						throw new Exception("");
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
		parValueTF.setEnabled(en.isEnabled());
		return hl;
	}
	
	private static HorizontalLayout getComponentHL(SimConfigEntry en, Component comp) {
		HorizontalLayout line = new HorizontalLayout();
		line.setSpacing(false);
		line.setMargin(false);
//		line.setImmediate(true);
		line.setWidth("100%");
		TextField parNameTF = new TextField();
		parNameTF.setValue(en.getCaption());
		parNameTF.setWidth("100%");
		parNameTF.setReadOnly(true);

		comp.setSizeFull();
		comp.setId(en.getId());
//		if (parValueTF instanceof TextField)
//		((TextField) parValueTF).setResponsive(true);

		line.addComponents(parNameTF, comp);
		line.setExpandRatio(parNameTF, 1.0f);
		line.setExpandRatio(comp, 2.0f);
		return line;
	}

	private static Slider getSlider(SimConfigEntry en) {
		SimConfigSlider sliderConf = (SimConfigSlider) en.getValue();
		Slider s = new Slider(Double.valueOf(sliderConf.getMin()), Double.valueOf(sliderConf.getMax()), 0);
		s.setCaption(en.getCaption());
		s.setValue(Double.valueOf(sliderConf.getValue()) * sliderConf.getScale());
		s.addValueChangeListener(new ValueChangeListener<Double>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent<Double> event) {
				Double newVal = event.getValue();
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
		cb.addValueChangeListener(new ValueChangeListener<Boolean>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent<Boolean> event) {
				Boolean newVal = (Boolean) event.getValue();
				en.setValue(newVal);
			}
		});
		return cb;
	}

	@SuppressWarnings("unchecked")
	private static VerticalLayout getCheckBoxGroup(SimConfigEntry en, Set<String> setOfStringForArrayCheckbox) {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(false);
		vl.setMargin(false);
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
		cb.addValueChangeListener(new ValueChangeListener<Boolean>() {
			@Override
			public void valueChange(ValueChangeEvent<Boolean> event) {
				Boolean newVal = (Boolean) event.getValue();
				if (newVal && !arrDepVar.contains(depVar))
					arrDepVar.add(depVar);
				else if (!newVal && arrDepVar.contains(depVar))
					arrDepVar.remove(depVar);
			}
		});
		return cb;
	}

	private static HorizontalLayout getNatural(SimConfigEntry en) {
		TextField parValueTF = new TextField();
		HorizontalLayout hl = getComponentHL(en, parValueTF);
		parValueTF.addBlurListener(new BlurListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void blur(BlurEvent event) {
				String newText = ((TextField) event.getComponent()).getValue();
				String newVal = newText;
				try {
					Integer testNatural = Integer.valueOf(newVal);
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

	private static HorizontalLayout getSelect(SimConfigEntry en) {
		NativeSelect<String> ns = new NativeSelect<>();
		Set<String> stringSet = CellSizes.getInstance().getCellSizeNames();
		ns.setItems(stringSet);
		ns.setEmptySelectionAllowed(false);
		ns.addValueChangeListener(new ValueChangeListener<String>() {
			@Override
			public void valueChange(ValueChangeEvent<String> event) {
				String newVal = event.getValue();
				en.setValue(newVal);
			}
		});
		// Load previous value
		String prevValue = (String) en.getValue();
		if (prevValue != null)
			ns.setValue(prevValue);
		HorizontalLayout hl = getComponentHL(en, ns);
		return hl;
	}
}
