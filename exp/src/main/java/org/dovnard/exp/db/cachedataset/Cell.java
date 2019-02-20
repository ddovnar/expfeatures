package org.dovnard.exp.db.cachedataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cell {
    private static Logger logger = LoggerFactory.getLogger(Cell.class);

    private Object value;
    private boolean isChanged = false;
    public Cell(Object val) {
        value = val;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object val) {
        //value = val;
        setValue(val, false, null, "unknown");
    }
    public void setValue(Object val, boolean forceChange, Row refRow, String cellInfo) {
        if (forceChange) {
            isChanged = true;
        } else {
            if (value != null && val != null) {
                if (value instanceof String && val instanceof String) {
                    isChanged = !value.equals(val);
                }
            } else {
                if (value != null && val == null)
                    isChanged = true;
                else if (value == null && val != null)
                    isChanged = true;
                else {
                    logger.info("Detect cell changed by otherwise!!");
                    isChanged = (value != val);
                }
            }
        }
        if (refRow != null) {
            refRow.setChanged(true);
        }
        if (isChanged) {
            logger.info("cell <" + cellInfo + "> change detected");
        }
        value = val;
    }
    public void setChanged(boolean changed) { this.isChanged = changed; }
    public boolean isChanged() { return this.isChanged; }
}
