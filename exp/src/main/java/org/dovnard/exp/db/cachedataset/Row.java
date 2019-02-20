package org.dovnard.exp.db.cachedataset;

import java.util.ArrayList;

public class Row {
    private ArrayList<Cell> rowData;
    private boolean isNewFlag = false;

    public Row() {
        rowData = new ArrayList<Cell>();
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
    public ArrayList<Cell> getRowCells() {
        return rowData;
    }
    public void setMarkedNew(boolean isNewFlag) {
        this.isNewFlag = isNewFlag;
    }
    public boolean isMarkedNew() { return isNewFlag; }
//    public Cell getCellByColumnName(String colName) {
//        for (Cell cell : rowData) {
//            if (cell.)
//        }
//    }
}
