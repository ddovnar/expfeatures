package org.dovnard.exp.db.cachedataset;

import java.util.Vector;

public class Row {
    private Vector<Cell> rowData;
    public Row() {
        rowData = new Vector<Cell>();
    }
    public void addCellData(Object data) {
        rowData.add(new Cell(data));
    }
    public void release() {
        rowData.clear();
    }
    public Cell getCell(int index) {
        return rowData.get(index);
    }
}
