package ca.sbstn.androidb.sql;

import android.util.Log;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// implement Parcelable instead of Serializable
public class Table implements Serializable {
    private static final String TAG = "TABLE";
    public int totalRows;
    public int limit = 30;
    public int offset = 0;
    //public String orderBy = "1";
    public int orderBy = 1;
    public String orderByDirection = "ASC";
    private String name;
    private Table.Type type;
    private Database database;
    private String[][] data;
    private String[] columns;
    private String[] catalogs;
    private String[] schemas;
    private String[] tables;
    private Map<String, Key> foreignKeys;
    private List<Key> primaryKeys;

    public Table(Database database) {
        this.database = database;

        this.foreignKeys = new HashMap<>();
        this.primaryKeys = new ArrayList<>();//null;//new ArrayList<>();
    }

    public static Table from(ResultSet results, Database database) {
        Table table = new Table(database);

        try {
            Statement statement = results.getStatement();
            String type = results.getString("table_type");

            if (type == null) type = "UNKNOWN";

            table.name = results.getString("table_name");
            table.schemas = new String[]{results.getString("table_schem")};

            if (type.equals("SEQUENCE")) table.type = Type.SEQUENCE;
            else if (type.equals("INDEX")) table.type = Type.INDEX;
            else if (type.equals("VIEW")) table.type = Type.VIEW;
            else if (type.equals("TABLE")) table.type = Type.TABLE;
            else if (type.equals("SYSTEM VIEW")) table.type = Type.SYSTEM_VIEW;
            else if (type.equals("SYSTEM TABLE")) table.type = Type.SYSTEM_TABLE;
            else if (type.equals("SYSTEM INDEX")) table.type = Type.SYSTEM_INDEX;
            else if (type.equals("SYSTEM TOAST INDEX")) table.type = Type.SYSTEM_TOAST_INDEX;
            else table.type = Type.UNKNOWN;
        } catch (SQLException e) {
            Log.e(Table.TAG, e.getMessage());
            return null;
        }

        return table;
    }

    public void setData(ResultSet results) {
        List<String[]> rows = new ArrayList<>();

        try {
            //Statement statement = results.getStatement();
            //Connection connection = statement.getConnection();
            ResultSetMetaData rsmd = results.getMetaData();

            int numColumns = rsmd.getColumnCount();

            this.columns = new String[numColumns];
            //this.catalogs = new String[numColumns];
            //this.schemas = new String[numColumns];
            //this.tables = new String[numColumns];

            for (int i = 0; i < numColumns; i++) {
                this.columns[i] = rsmd.getColumnName(i + 1);
                //this.catalogs[i] = rsmd.getCatalogName(i + 1);
                //this.schemas[i] = rsmd.getSchemaName(i + 1);
                //this.tables[i] = rsmd.getTableName(i + 1);

                //Log.d("TABLE", String.format("%s %s %s %s", this.columns[i], this.catalogs[i], this.schemas[i], this.tables[i]));
            }

            while (results.next()) {
                String[] mRow = new String[numColumns];

                for (int colIndex = 0; colIndex < numColumns; colIndex++) {
                    mRow[colIndex] = results.getString(colIndex + 1);
                }

                rows.add(mRow);
            }

            this.data = new String[rows.size()][numColumns];
            for (int i = 0; i < rows.size(); i++) this.data[i] = rows.get(i);
        } catch (SQLException e) {
            Log.d("TABLE", e.getMessage());
        }
    }

    public int getTotalRows() {
        return this.totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getLimit() {
        return this.limit;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOrderBy(int orderBy) {
        this.orderBy = orderBy;
    }

    public int getOrderBy() {
        return this.orderBy;
    }

    public String getOrderByString() {
        if (this.columns != null) {
            try {
                return this.columns[this.getOrderBy() - 1];
            } catch (NumberFormatException e) {}
        }

        return this.getOrderBy() + "";
    }

    public void setOrderByDirection(String orderByDirection) {
        this.orderByDirection = orderByDirection;
    }

    public void toggleOrderByDirection() {
        if (this.orderByDirection.toUpperCase().equals("DESC")) this.setOrderByDirection("ASC");
        else this.setOrderByDirection("DESC");

        this.setOffset(0);
    }

    public String getOrderByDirection() {
        return this.orderByDirection;
    }

    public void next() {
        this.offset = this.offset + this.limit;
    }

    public void previous() {
        this.offset = Math.max(this.offset - this.limit, 0);
    }

    public String getName() {
        return this.name;
    }

    public String[] getColumns() {
        return this.columns;
    }

    public String[] getCatalogs() {
        return this.catalogs;
    }

    public String[] getSchemas() {
        return this.schemas;
    }

    public String getSchema() {
        //Log.d("TABLE", String.format("%s %s", this.name, this.schemas[0]));
        if (this.schemas == null) return "";
        else return this.schemas[0];
    }

    public String[] getTables() {
        return this.tables;
    }

    public String[] getRow(int row) {
        return this.data[row];
    }

    public String[] getColumn(int column) {
        //return this.col
        return null;
    }

    public List<Key> getPrimaryKeys() {
        return this.primaryKeys;
    }

    public void setPrimaryKeys(List<Key> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public List<Key> getForeignKeys() {
        return new ArrayList<>(this.foreignKeys.values());
    }

    public Key getForeignKey(String column) {
        return this.foreignKeys.get(column);
    }

    public void setForeignKeys(List<Key> foreignKeys) {
        this.foreignKeys.clear();

        for (Key key : foreignKeys) {
            this.foreignKeys.put(key.getForeignColumn(), key);
        }
    }

    public boolean columnIsForeignKey(String column) {
        for (Key key : this.getForeignKeys()) {
            if (key.getForeignColumn().equals(column)) return true;
        }

        return false;
    }

    public int getColumnCount() {
        if (this.data == null) return 0;

        return this.data[0].length;
    }

    public int getRowCount() {
        if (this.data == null) return 0;

        return this.data.length;
    }

    public Database getDatabase() {
        return this.database;
    }

    public String getCellContent(int row, int col) {
        return this.data[row][col];
    }

    public boolean is(Type type) {
        return this.getType().equals(type);
    }

    public Type getType() {
        return this.type;
    }

    public String getTypeString() {
        if (this.type == Type.SEQUENCE) return "SEQUENCE";
        else if (this.type == Type.INDEX) return "INDEX";
        else if (this.type == Type.VIEW) return "VIEW";
        else if (this.type == Type.TABLE) return "TABLE";
        else if (this.type == Type.SYSTEM_VIEW) return "SYSTEM VIEW";
        else if (this.type == Type.SYSTEM_TABLE) return "SYSTEM TABLE";
        else if (this.type == Type.SYSTEM_INDEX) return "SYSTEM INDEX";
        else if (this.type == Type.SYSTEM_TOAST_INDEX) return "SYSTEM TOAST INDEX";
        else return "UNKWOWN";
    }

    public String getQuery() {
        return String.format(
            Locale.getDefault(),
            "SELECT * FROM %s.%s ORDER BY %s %s OFFSET %d LIMIT %d",
            this.getSchema(),
            this.getName(),
            this.getOrderByString(),
            this.getOrderByDirection(),
            this.getOffset(),
            this.getLimit()
        );
    }

    public enum Type {TABLE, VIEW, INDEX, SEQUENCE, SYSTEM_INDEX, SYSTEM_TABLE, SYSTEM_TOAST_INDEX, SYSTEM_VIEW, UNKNOWN}
}
