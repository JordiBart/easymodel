package cat.udl.easymodel.logic.model;

import java.util.Comparator;

public class ModelComparator implements Comparator<Model> {
    @Override
    public int compare(Model o1, Model o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
