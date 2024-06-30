package cat.udl.easymodel.views.simulationlauncher;

import cat.udl.easymodel.logic.simconfig.SimConfigEntry;
import cat.udl.easymodel.utils.Utils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.math.BigDecimal;
import java.util.Set;

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
        parValueTF.setWidthFull();
        HorizontalLayout hl = getComponentHL(en, parValueTF);
        // Load previous value
        String prevValue = (String) en.getValue();
        if (prevValue != null)
            parValueTF.setValue(prevValue);
        parValueTF.setValueChangeMode(ValueChangeMode.ON_BLUR);
        parValueTF.addValueChangeListener(event -> {
            String eventValue = event.getValue();
            en.setValue(eventValue);
        });
        return hl;
    }

    private static HorizontalLayout getMathExpression(SimConfigEntry en) {
        TextField parValueTF = new TextField();
        parValueTF.setWidthFull();
        HorizontalLayout hl = getComponentHL(en, parValueTF);
        // Load previous value
        String prevValue = (String) en.getValue();
        if (prevValue != null)
            parValueTF.setValue(prevValue);
        parValueTF.setValueChangeMode(ValueChangeMode.ON_BLUR);
        parValueTF.addValueChangeListener(event -> {
            String eventVal = event.getValue();
            String newVal = eventVal;
            try {
                Utils.evalMathExpr(newVal);
                //((TextField) event.getComponent()).setValue(newVal);
            } catch (Exception e) {
                newVal = en.getOriginalValue();
            } finally {
                en.setValue(newVal);
                if (newVal != eventVal)
                    parValueTF.setValue(newVal);
            }
        });
        return hl;
    }

    private static HorizontalLayout getDecimal(SimConfigEntry en) {
        TextField parValueTF = new TextField();
        parValueTF.setWidthFull();
        HorizontalLayout hl = getComponentHL(en, parValueTF);
        // Load previous value
        String prevValue = (String) en.getValue();
        if (prevValue != null)
            parValueTF.setValue(prevValue);
        parValueTF.setValueChangeMode(ValueChangeMode.ON_BLUR);
        parValueTF.addValueChangeListener(event -> {
            String eventVal = event.getValue();
            String newVal = eventVal;
            try {
                BigDecimal newBigDecVal = new BigDecimal(newVal);
                if (en.getMinNumericValue() != null) {
                    BigDecimal bdMin = new BigDecimal(en.getMinNumericValue());
                    if (bdMin.compareTo(newBigDecVal) > 0)
                        newVal = en.getMinNumericValue();
                }
                if (en.getMaxNumericValue() != null) {
                    BigDecimal bdMax = new BigDecimal(en.getMaxNumericValue());
                    if (bdMax.compareTo(newBigDecVal) < 0)
                        newVal = en.getMaxNumericValue();
                }
            } catch (Exception e) {
                newVal = en.getOriginalValue();
            } finally {
                en.setValue(newVal);
                if (newVal != eventVal)
                    parValueTF.setValue(newVal);
            }
        });
        parValueTF.setEnabled(en.isEnabled());
        String helperText="";
        if (en.getMinNumericValue() != null)
            helperText+="Min="+en.getMinNumericValue();
        if (en.getMaxNumericValue() != null)
            helperText+=(!helperText.isEmpty()?" ; ":"")+"Max="+en.getMaxNumericValue();
        parValueTF.setHelperText(helperText);
        return hl;
    }

    private static HorizontalLayout getNatural(SimConfigEntry en) {
        TextField parValueTF = new TextField();
        parValueTF.setWidthFull();
        HorizontalLayout hl = getComponentHL(en, parValueTF);
        // Load previous value
        String prevValue = (String) en.getValue();
        if (prevValue != null)
            parValueTF.setValue(prevValue);
        parValueTF.setValueChangeMode(ValueChangeMode.ON_BLUR);
        parValueTF.addValueChangeListener(event -> {
            String eventVal = event.getValue();
            String newVal = eventVal;
            try {
                Integer newIntVal = Integer.valueOf(newVal);
                if (en.getMinNumericValue() != null && Integer.valueOf(en.getMinNumericValue()) > newIntVal)
                    newVal = en.getMinNumericValue();
                else if (en.getMaxNumericValue() != null && Integer.valueOf(en.getMaxNumericValue()) < newIntVal)
                    newVal = en.getMaxNumericValue();
            } catch (Exception e) {
                newVal = en.getOriginalValue();
            } finally {
                en.setValue(newVal);
                if (newVal != eventVal)
                    parValueTF.setValue(newVal);
            }
        });
        String helperText="";
        if (en.getMinNumericValue() != null)
            helperText+="Min="+en.getMinNumericValue();
        if (en.getMaxNumericValue() != null)
            helperText+=(!helperText.isEmpty()?" ; ":"")+"Max="+en.getMaxNumericValue();
        parValueTF.setHelperText(helperText);
        return hl;
    }

    private static HorizontalLayout getSelect(SimConfigEntry en) {
        Select<String> ns = new Select<>();
        ns.setItems(en.getOptionSet());
        ns.setEmptySelectionAllowed(false);
        // Load previous value
        String prevValue = (String) en.getValue();
        if (prevValue != null)
            ns.setValue(prevValue);
        ns.addValueChangeListener(event -> {
            String newVal = event.getValue();
            en.setValue(newVal);
        });
        HorizontalLayout hl = getComponentHL(en, ns);
        return hl;
    }

    private static Checkbox getCheckBox(SimConfigEntry en) {
        Checkbox cb = new Checkbox(en.getCaption());
        cb.setValue(en.getValue().equals("1"));
        cb.addValueChangeListener(event -> {
            String newVal = event.getValue()?"1":"0";
            en.setValue(newVal);
        });
        return cb;
    }

    private static HorizontalLayout getComponentHL(SimConfigEntry en, Component comp) {
        HorizontalLayout line = new HorizontalLayout();
        line.setSpacing(false);
        line.setMargin(false);
        line.setWidth("100%");
        TextField parNameTF = new TextField();
        parNameTF.setValue(en.getCaption());
        parNameTF.setReadOnly(true);

        comp.setId(en.getId());

        line.add(parNameTF, comp);
        line.setFlexGrow(1, parNameTF);
        line.setFlexGrow(2, comp);
        return line;
    }
}
