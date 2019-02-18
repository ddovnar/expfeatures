package org.dovnard.exp.db.cachedataset;

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

    public String getString(int colIndex);
}
