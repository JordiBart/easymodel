package cat.udl.easymodel.utils;

public class HtmlTableBuilder {
    private StringBuilder sb = new StringBuilder();
    private boolean isInProgress = true;
    private boolean isColsSet = false;
    private int cols = 0;
    private int currentCol = 0;

    public HtmlTableBuilder(String css_class) {
        sb.append("<table");
        if (css_class != null)
            sb.append(" class=\"" + css_class + "\"");
        sb.append("><tbody>");
    }

    public void addCell(String val, int isHeader) {
        String cellCode = isHeader != 0 ? "th" : "td";
        if (isColsSet && currentCol == 0 || !isColsSet && cols == 0)
            sb.append("<tr>");
        sb.append("<" + cellCode + ">" + val + "</" + cellCode + ">");
        if (isColsSet) {
            currentCol++;
            if (currentCol == cols) {
                sb.append("</tr>");
                currentCol = 0;
            }
        } else
            cols++;
    }

    public void setCols() {
        isColsSet = true;
        sb.append("</tr>");
    }

    public void finish() {
        if (currentCol != 0)
            System.err.println("Warning: Html table finished with unmatched columns");
        sb.append("</tbody></table>");
        isInProgress = false;
    }

    public String getHtmlCode() {
        if (!isInProgress)
            return sb.toString();
        else
            return "<span>Table error</span>";
    }
}
