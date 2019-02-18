package org.dovnard.exp.db.cachedataset;

public class Cell {
    private Object value;
    public Cell(Object val) {
        value = val;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object val) {
        value = val;
    }
}
