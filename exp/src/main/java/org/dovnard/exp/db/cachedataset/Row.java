package org.dovnard.exp.db.cachedataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Row {
    private static Logger logger = LoggerFactory.getLogger(Row.class);

    private ArrayList<Cell> rowData;
    private boolean isNewFlag = false;
    private boolean isChanged = false;

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
    public void setChanged(boolean changed) {
        if (changed && !isChanged) {
            logger.info("row change detected");
        }
        this.isChanged = changed;
    }
    public boolean isChanged() { return this.isChanged; }
//    public Cell getCellByColumnName(String colName) {
//        for (Cell cell : rowData) {
//            if (cell.)
//        }
//    }
}
