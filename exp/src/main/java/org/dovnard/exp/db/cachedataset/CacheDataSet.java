package org.dovnard.exp.db.cachedataset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public interface CacheDataSet {
    public void release();
    public void setURL(String url);
    public void setUsername(String username);
    public void setPassword(String password);
    public void setCommand(String cmd);
    public RowHeader getRowHeader();
    public Vector<String> getColumnNames();
    public void setPageSize(int ps);
    public void execute();
    public boolean nextPage();
    public boolean prevPage();
    public boolean next();
    public boolean previous();
    public boolean next(boolean withGoToNextPage);
    public boolean previous(boolean withGoToPreviousPage);
    public int getLoadedRecords();
    public boolean first();
    public boolean last();
    public int getActiveRecordIndex();
    public void setRowIdColumnIndex(int colIdx);
    public void setRowIdColumnName(String name);

    public void addParameter(String name, int v);
    public void addParameter(String name, String v);

    public boolean executeCommand(String cmd, Map<String, Object> cmdParams);
    public boolean delete();
    public boolean add();
    public void setRealColumnNames(List<String> colNames);

    public String getString(int colIndex);
    public boolean setValue(String colName, String value);
}
