package org.dovnard.exp.db.cachedataset;

import java.util.Vector;

public class RowHeader {
    private Vector<RowHeaderItem> headerItems;
    public RowHeader() {
        headerItems = new Vector<RowHeaderItem>();
    }
    /*public RowHeader(String[] cols) {
        for (String c: cols) {
            columnNames.add(c);
        }
    }*/
    public void addHeaderItem(RowHeaderItem item) {
        headerItems.add(item);
    }
    public Vector<String> getColumnNames() {
        Vector<String> cols = new Vector<String>();
        for (RowHeaderItem item : headerItems) {
            cols.add(item.getColumnName());
        }
        return cols;
    }
    public Vector<RowHeaderItem> getHeaderItems() {
        return headerItems;
    }
    public String getColumnName(int index) {
        return headerItems.get(index).getColumnName();
    }
    public RowHeaderItem getHeaderItem(int index) {
        return headerItems.get(index);
    }
    public void release() {
        headerItems.clear();
    }
}
