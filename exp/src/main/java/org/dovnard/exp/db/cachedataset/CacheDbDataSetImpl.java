package org.dovnard.exp.db.cachedataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

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
    private String tableName;
    private int rowIdColumnIndex = 0;
    private String rowIdColumnName;
    private List<String> realColNames;

    public CacheDbDataSetImpl() {
        header = new RowHeader();
        dataSet = new Vector<Row>();
        params = new LinkedHashMap<String, Object>();
        realColNames = new ArrayList<String>();

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
        realColNames.clear();
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

    public ArrayList<String> getColumnNames() {
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

    public int getActiveRecordIndex() {
        return activeRowIndex;
    }

    public void addParameter(String name, int v) {
        params.put(name, v);
    }

    public void addParameter(String name, String v) {
        params.put(name, v);
    }
    public boolean executeCommand(String cmd, Map<String, Object> cmdParams) {
        //if (1 == 1) return true;

        boolean res = false;
        logger.info("Invoke command: " + cmd);
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
            statement = conn.prepareStatement(cmd, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            fillParameters(statement, cmdParams);
            int cmdRes = statement.executeUpdate();
            logger.info("Command result: " + cmdRes);
            res = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            res = false;
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
            }
        }
        return res;
    }

    public boolean add() {
        Row row = new Row();
        row.setMarkedNew(true);
        for (String cn : header.getColumnNames()) {
            row.addCellData(null);
        }
        dataSet.add(row);
        activeRowIndex = dataSet.size() - 1;
        return true;
    }

    public boolean save() {
        StringBuilder sql = new StringBuilder();
        StringBuilder sqlCols = new StringBuilder();
        Map<String, Object> cmdParams = new LinkedHashMap<String, Object>();
        //cmdParams.put("id", "");
        Row row = dataSet.get(activeRowIndex);
        if (row.isMarkedNew()) {
            sql.append("insert into ");
            sql.append(tableName);
            sql.append("(");
        } else {
            sql.append("update ");
            sql.append(tableName);
            sql.append(" set ");
        }
        RowHeader header = getRowHeader();
        int colIdx = 0;
        for (RowHeaderItem item : header.getHeaderItems()) {
            if (row.isMarkedNew()) {
                sql.append(item.getRealTableColumnName());
                sqlCols.append("?");
            } else {
                if (item.getRealTableColumnName().equalsIgnoreCase(rowIdColumnName)) {
                    colIdx++;
                    continue;
                }
                sql.append(item.getRealTableColumnName());
                sql.append("=?");
            }
            cmdParams.put(item.getRealTableColumnName(), row.getCell(colIdx).getValue());
            if (colIdx < header.getHeaderItems().size() - 1) {
                if (row.isMarkedNew()) {
                    sqlCols.append(",");
                }
                sql.append(",");
            }
            colIdx++;
        }
        if (row.isMarkedNew()) {
            sql.append(")");
            sql.append(" values(");
            sql.append(sqlCols.toString());
            sql.append(")");
        } else {
            sql.append(" where ");
            sql.append(rowIdColumnName);
            sql.append("=?");
            cmdParams.put("row_id", row.getCell(rowIdColumnIndex).getValue());
        }
        logger.info(sql.toString());
        if (executeCommand(sql.toString(), cmdParams)) {
            return true;
        }
        return false;
    }

    public boolean delete() {
        if (activeRowIndex > -1 && dataSet.size() > 0) {
            Map<String, Object> cmdParams = new LinkedHashMap<String, Object>();
            cmdParams.put("id", dataSet.get(activeRowIndex).getCell(rowIdColumnIndex).getValue());
            if (executeCommand("delete from " + tableName + " where " + rowIdColumnName + "=?", cmdParams)) {
                dataSet.remove(activeRowIndex);
                lastReadedPageRows--;
                cursorPos--;
                if (activeRowIndex > 0) {
                    activeRowIndex--;
                } else {
                    if (dataSet.size() == 0) {
                    //    activeRowIndex++;
                    //} else {
                        if (nextPage()) {
                            activeRowIndex = 0;
                        } else {
                            activeRowIndex = -1;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void setRowIdColumnIndex(int colIdx) {
        rowIdColumnIndex = colIdx;
    }

    public void setRowIdColumnName(String name) {
        rowIdColumnName = name;
    }

    public void setRealColumnNames(List<String> colNames) {
        realColNames = colNames;
        /*int idx = 0;
        for (String col : colNames) {
            header.getHeaderItem(idx).setRealTableColumnName(col);
            idx++;
        }*/
//        Iterator<Map.Entry<String, String>> it = colNames.entrySet().iterator();
//        int idx = 0;
//        while (it.hasNext()) {
//            Map.Entry<String, String> pair = it.next();
//            header.getHeaderItem(idx).setRealTableColumnName(pair.getValue());
//        }
    }

    public boolean setValue(String colName, String value) {
        //Cell cell = dataSet.get(activeRowIndex).getCellByColumnName(colName);
//        for (Cell cell : dataSet.get(activeRowIndex).getRowCells()) {
//            if (cell.)
//        }
        int colCnt = 0;
        int colIdx = -1;
        for (String cn : header.getColumnNames()) {
            if (cn.equalsIgnoreCase(colName)) {
                colIdx = colCnt;
                break;
            }
            colCnt++;
        }
        if (colIdx == -1) {
            throw new RuntimeException("Column <" + colName + "> not founded in dataset");
        }
        Row row = dataSet.get(activeRowIndex);
        //row.setChanged(true); change detector by Cell
        Cell cell = row.getRowCells().get(colIdx);
        cell.setValue(value, false, row, colName);
        return true;
    }

    public String getString(int colIndex) {
        if (activeRowIndex < 0 || dataSet.size() == 0) {
            throw new RuntimeException("Empty dataset");
        }
        return dataSet.get(activeRowIndex).getCell(colIndex).getValue().toString();
    }

    private void fillParameters(PreparedStatement stat, Map<String, Object> statParams) throws SQLException {
        logger.info("Process parameters");
        Iterator<Map.Entry<String, Object>> it = statParams.entrySet().iterator();
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
    private void fillParameters(PreparedStatement stat) throws SQLException {
        fillParameters(stat, params);
        /*logger.info("Process parameters");
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
        }*/
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

                tableName = meta.getTableName(rowIdColumnIndex + 1);

                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    logger.info("Column: " + meta.getColumnName(i) + "|" + meta.getColumnLabel(i) + "|" + meta.getCatalogName(i) + "|" + meta.getColumnClassName(i) + "|" + meta.getSchemaName(i));
                    //header.addColumnName(meta.getColumnLabel(i));
                    if (realColNames.size() > i - 1) {
                        header.addHeaderItem(new RowHeaderItem(meta.getColumnName(i), realColNames.get(i - 1), meta.getTableName(i)));
                    } else {
                        header.addHeaderItem(new RowHeaderItem(meta.getColumnName(i), null, meta.getTableName(i)));
                    }
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
