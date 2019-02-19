package org.dovnard.exp.db.cachedataset;

public class RowHeaderItem {
    private String columnName;
    private String realTableColumnName;
    private String tableName;

    public RowHeaderItem(String columnName, String realTableColumnName, String tableName) {
        this.columnName = columnName;
        this.realTableColumnName = realTableColumnName;
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getRealTableColumnName() {
        return realTableColumnName;
    }

    public void setRealTableColumnName(String v) {
        this.realTableColumnName = v;
    }

    public String getTableName() {
        return tableName;
    }
}
