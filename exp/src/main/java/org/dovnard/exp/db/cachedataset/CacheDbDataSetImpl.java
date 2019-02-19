package org.dovnard.exp.db.cachedataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class CacheDbDataSetImpl implements CacheDataSet {
    private static Logger logger = LoggerFactory.getLogger(CacheDbDataSetImpl.class);
    private RowHeader header;
    private Vector<Row> dataSet;
    private transient String username;
    private transient String password;
    private String url;
    private String cmd;
    private int pageSize;
    private int cursorPos;
    private int lastReadedPageRows = 0;
    private int activeRowIndex = -1;
    private Map<String, Object> params;

    public CacheDbDataSetImpl() {
        header = new RowHeader();
        dataSet = new Vector<Row>();
        params = new LinkedHashMap<String, Object>();

        pageSize = 5;
        cursorPos = 0;
    }

    public void release() {
        for (Row row : dataSet) {
            row.release();
        }
        dataSet.clear();
        header.release();
        params.clear();
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCommand(String cmd) {
        this.cmd = cmd;
    }

    public RowHeader getRowHeader() {
        return header;
    }

    public Vector<String> getColumnNames() {
        return header.getColumnNames();
    }

    public void setPageSize(int ps) {
        pageSize = ps;
    }

    public void execute() {
        if (cmd == null || cmd.equals("")) {
            throw new RuntimeException("Command not set");
        }
        readData(true);
    }

    public boolean nextPage() {
        int needDelete = lastReadedPageRows;
        boolean res = readData(false);
        if (res) {
            logger.info("Load next page");
            for (int i = 0; i < needDelete; i++) {
                dataSet.remove(0);
            }
            activeRowIndex = 0;
        }
        return res;
    }

    public boolean prevPage() {
        boolean res = false;
        if (cursorPos - lastReadedPageRows - pageSize >= 0) {
            cursorPos = cursorPos - lastReadedPageRows - pageSize;
            int needDelete = lastReadedPageRows;
            res = readData(false);
            if (res) {
                logger.info("Load previous page");
                for (int i = 0; i < needDelete; i++) {
                    dataSet.remove(0);
                }
                activeRowIndex = dataSet.size()-1;
            }
        }
        return res;
    }

    public boolean next(boolean withGoToNextPage) {
        if (cursorPos > 0 && activeRowIndex + 1 < dataSet.size()) {
            activeRowIndex++;
            return true;
        }
        if (withGoToNextPage)
            return nextPage();
        return false;
    }

    public boolean previous(boolean withGoToPreviousPage) {
        if (activeRowIndex - 1 > -1 && cursorPos > 0) {
            activeRowIndex--;
            return true;
        }
        if (withGoToPreviousPage)
            return prevPage();
        return false;
    }

    public boolean next() {
        return next(true);
    }

    public boolean previous() {
        return previous(true);
    }

    public int getLoadedRecords() {
        return dataSet.size();
    }

    public boolean first() {
        if (dataSet.size() > 0) {
            activeRowIndex = 0;
            return true;
        }
        return false;
    }
    public boolean last() {
        if (dataSet.size() > 0) {
            activeRowIndex = dataSet.size() - 1;
            return true;
        }
        return false;
    }

    public void addParameter(String name, int v) {
        params.put(name, v);
    }

    public void addParameter(String name, String v) {
        params.put(name, v);
    }

    public String getString(int colIndex) {
        if (activeRowIndex < 0 || dataSet.size() == 0) {
            throw new RuntimeException("Empty dataset");
        }
        return dataSet.get(activeRowIndex).getCell(colIndex).getValue().toString();
    }

    private void fillParameters(PreparedStatement stat) throws SQLException {
        logger.info("Process parameters");
        Iterator<Map.Entry<String, Object>> it = params.entrySet().iterator();
        int idx = 1;
        while (it.hasNext()) {
            Map.Entry<String, Object> pair = it.next();
            if (pair.getValue() instanceof Integer) {
                stat.setInt(idx, ((Integer) pair.getValue()).intValue());
            } else if (pair.getValue() instanceof String) {
                stat.setString(idx, (String) pair.getValue());
            }
            idx++;
            logger.info("Parameter: " + pair.getKey() + "=" + pair.getValue());
        }
    }

    private boolean readData(boolean onExecute) {
        boolean res = false;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, username, password);

            //Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            //ResultSet result = statement.executeQuery(cmd);
            PreparedStatement statement = conn.prepareStatement(cmd, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            fillParameters(statement);
            ResultSet result = statement.executeQuery();

            if (onExecute) {
                ResultSetMetaData meta = result.getMetaData();

                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    header.addColumnName(meta.getColumnLabel(i));
                }
            }

            boolean needReadFirst = false;
            if (cursorPos == 0) {
                cursorPos = 1;
                needReadFirst = true;
            }
            if (result.absolute(cursorPos)) {
                int fetched = 0;
                if (needReadFirst) {
                    fetched = 1;
                    readRowData(result);
                }
                while (fetched < pageSize && result.next()) {
                    fetched++;
                    cursorPos++;
                    readRowData(result);
                }
                if (fetched > 0) {
                    res = true;
                    lastReadedPageRows = fetched;
                }
            }
            result.close();
            statement.close();
        } catch(SQLException ex) {
            ex.printStackTrace();
            res = false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch(SQLException ex) {
            }
        }
        return res;
    }
    private void readRowData(ResultSet rs) throws SQLException {
        Row row = new Row();
        for (String c : header.getColumnNames()) {
            row.addCellData(rs.getString(c));
        }
        dataSet.add(row);
    }
}
