package org.dovnard.exp.db.cachedataset;

import java.util.Vector;

public class RowHeader {
    private Vector<String> columnNames;
    public RowHeader() {
        columnNames = new Vector<String>();
    }
    public RowHeader(String[] cols) {
        for (String c: cols) {
            columnNames.add(c);
        }
    }
    public void addColumnName(String name) {
        columnNames.add(name);
    }
    public Vector<String> getColumnNames() {
        return columnNames;
    }
    public String getColumnName(int index) {
        return columnNames.get(index);
    }
    public void release() {
        columnNames.clear();
    }
}
