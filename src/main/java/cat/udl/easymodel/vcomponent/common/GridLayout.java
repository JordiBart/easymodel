package cat.udl.easymodel.vcomponent.common;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;

public class GridLayout extends VerticalLayout {
    private int currentCol=0;
    private int totalCol=0;
    private int currentRow=0;
    private int totalRow=0;
    private ArrayList<ArrayList<GridLayoutElement>> matrix = new ArrayList<>();

    public GridLayout(int cols, int rows) {
        super();
        this.setSpacing(false);
        this.setPadding(false);
//        this.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        totalCol=cols;
        totalRow=rows;
        for (int i=0;i<totalRow;i++){
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(false);
            hl.setPadding(false);
            hl.setWidthFull();
//            hl.setDefaultVerticalComponentAlignment(Alignment.CENTER);
            matrix.add(new ArrayList<>());
            for (int j=0;j<totalCol;j++){
                VerticalLayout vl = new VerticalLayout();
                vl.setSpacing(false);
                vl.setPadding(false);
                vl.setWidth("200px");
                vl.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
                Component comp = new Span("");
                vl.add(comp);
                hl.add(vl);
                matrix.get(i).add(new GridLayoutElement(vl,comp));
            }
            this.add(hl);
        }
        resetPosition();
    }

    public void setComponent(Component component){
        this.setComponent(component, currentCol, currentRow);
    }

    public void setComponent(Component component, int col, int row){
        try {
            matrix.get(row).get(col).getVerticalLayout().removeAll();
            matrix.get(row).get(col).getVerticalLayout().add(component);
            matrix.get(row).get(col).setComponent(component);

            currentCol=col;
            currentRow=row;
            advancePosition();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private void advancePosition(){
        currentCol++;
        if (currentCol>=totalCol){
            currentRow++;
            currentCol=0;
        }
        if (currentRow >= totalRow)
            resetPosition();
    }
    private void resetPosition(){
        currentCol=0;
        currentRow=0;
    }
    public void setWidthCol(Integer col, String width){
        if (col==null)
            this.setWidth(width);
        else {
            for (int i=0;i<totalRow;i++){
                matrix.get(i).get(col).getVerticalLayout().setWidth(width);
            }
        }
    }
    public int getCols() {
        return totalCol;
    }
    public int getRows() {
        return totalRow;
    }
    public Component getComponent(int col, int row){
        return matrix.get(row).get(col).getComponent();
    }
    public GridLayoutElement getElement(int col, int row){
        return matrix.get(row).get(col);
    }
}
