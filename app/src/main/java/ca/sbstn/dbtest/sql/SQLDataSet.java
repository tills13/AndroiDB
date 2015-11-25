package ca.sbstn.dbtest.sql;

import android.util.Log;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tills13 on 2015-11-18.
 */
public class SQLDataSet implements Iterable<SQLDataSet.Row>, Serializable {
    private String query;
    private List<Column> columns;
    private List<Row> rows;
    private Exception error;

    private Table table;

    public SQLDataSet() {
        this.columns = new ArrayList<>(); // column definitions
        this.rows = new ArrayList<>();
    }

    public static SQLDataSet from(ResultSet results) {
        SQLDataSet sqlDataSet = new SQLDataSet();

        try {
            ResultSetMetaData resultSetMetaData = results.getMetaData();
            //sqlDataSet.query = results.getStatement().toString();

            int numColumns = resultSetMetaData.getColumnCount();

            for (int i = 0; i < numColumns; i++) {
                String type = resultSetMetaData.getColumnTypeName(i + 1);
                String name = resultSetMetaData.getColumnName(i + 1);

                sqlDataSet.columns.add(new Column(type, name));
            }

            while (results.next()) sqlDataSet.rows.add(Row.from(sqlDataSet, results));
        } catch (SQLException e) {
            Log.d("SQLResult", e.getMessage());
            return null;
        }

        return sqlDataSet;
    }

    public Row getRow(int index) {
        return this.rows.get(index);
    }

    public List<Column> getColumns() {
        return this.columns;
    }

    public int getColumnIndex(String column) {
        for (int i = 0; i < this.columns.size(); i++) {
            Column mColumn = this.columns.get(i);
            if (mColumn.getName().equals(column)) return i;
        }

        return -1;
    }

    public int getColumnCount() {
        return this.columns.size();
    }

    public int getRowCount() {
        return this.rows.size();
    }

    public String getQuery() {
        return this.query;
    }

    public Exception getError() {
        return this.error;
    }

    public Table getTable() {
        return this.table;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setError(Exception e) {
        this.error = e;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Iterator<Row> iterator() {
        return new SQLResultIterator(this);
    }

    public static class Column implements Serializable {
        private String type;
        private String name;

        public Column(String type, String name) {
            this.type = type;
            this.name = name;
        }

        public String getType() {
            return this.type;
        }

        public String getName() {
            return this.name;
        }
    }

    public static class Row implements Serializable {
        private SQLDataSet sqlDataSet;
        private List<Object> data;

        public Row(SQLDataSet sqlDataSet) {
            this.sqlDataSet = sqlDataSet;
            this.data = new ArrayList<>();
        }

        public static Row from(SQLDataSet sqlDataSet, ResultSet results) {
            Row row = new Row(sqlDataSet);

            try {
                for (int i = 1; i < (row.sqlDataSet.getColumnCount() + 1); i++) {
                    row.data.add(results.getObject(i));
                }
            } catch (SQLException e) {
                Log.d("something", e.getMessage());
                return null;
            }

            return row;
        }

        public Object getColumn(int index) {
            return this.data.get(index);
        }

        public Object getColumn(String column) {
            return this.data.get(this.sqlDataSet.getColumnIndex(column));
        }

        public String getString(int index) {
            Object object = this.getColumn(index);
            return object == null ? "" : object.toString();
        }

        public String getString(String column) {
            Object object = this.getColumn(column);
            return object == null ? "" : object.toString();
        }

        public SQLDataSet getDataSet() {
            return this.sqlDataSet;
        }
    }

    class SQLResultIterator implements Iterator<SQLDataSet.Row> {
        private SQLDataSet result;
        private int cursor;

        public SQLResultIterator(SQLDataSet result) {
            this.result = result;
            this.cursor = 0;
        }

        @Override
        public boolean hasNext() {
            return this.cursor < this.result.getRowCount();
        }

        @Override
        public Row next() {
            if (this.hasNext()) {
                return this.result.getRow(this.cursor++);
            }

            return null;
        }

        @Override
        public void remove() {}
    }
}
