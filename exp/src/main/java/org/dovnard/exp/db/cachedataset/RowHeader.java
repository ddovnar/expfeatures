package org.dovnard.exp.db.cachedataset;

import java.util.ArrayList;

public class RowHeader {
    private ArrayList<RowHeaderItem> headerItems;
    public RowHeader() {
        headerItems = new ArrayList<RowHeaderItem>();
    }
    /*public RowHeader(String[] cols) {
        for (String c: cols) {
            columnNames.add(c);
        }
    }*/
    public void addHeaderItem(RowHeaderItem item) {
        headerItems.add(item);
    }
    public ArrayList<String> getColumnNames() {
        ArrayList<String> cols = new ArrayList<String>();
        for (RowHeaderItem item : headerItems) {
            cols.add(item.getColumnName());
        }
        return cols;
    }
    public ArrayList<RowHeaderItem> getHeaderItems() {
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
