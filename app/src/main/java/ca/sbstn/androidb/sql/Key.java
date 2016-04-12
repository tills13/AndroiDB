package ca.sbstn.androidb.sql;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by tills13 on 2015-07-14.
 */
public class Key implements Serializable {
    public Type type;

    public String name;

    public String catalog;
    public String schema;
    public String table;
    public String column;

    public String fCatalog; // foreign catalog
    public String fSchema; // foreign schema
    public String fTable; // foreign table
    public String fColumn; // foreign column

    public Key(Type type) {
        this.type = type;
    }

    public static Key from(Type type, ResultSet resultSet) {
        Key key = new Key(type);

        try {
            if (key.type == Type.PRIMARY_KEY) { // primary key
                key.name = resultSet.getString("PK_NAME");

                key.catalog = resultSet.getString("TABLE_CAT");
                key.schema = resultSet.getString("TABLE_SCHEM");
                key.table = resultSet.getString("TABLE_NAME");
                key.column = resultSet.getString("COLUMN_NAME");
            } else { // foreign key
                key.name = resultSet.getString("FK_NAME");

                key.catalog = resultSet.getString("PKTABLE_CAT");
                key.schema = resultSet.getString("PKTABLE_SCHEM");
                key.table = resultSet.getString("PKTABLE_NAME");
                key.column = resultSet.getString("PKCOLUMN_NAME");

                key.fCatalog = resultSet.getString("FKTABLE_CAT");
                key.fSchema = resultSet.getString("FKTABLE_SCHEM");
                key.fTable = resultSet.getString("FKTABLE_NAME");
                key.fColumn = resultSet.getString("FKCOLUMN_NAME");
            }
        } catch (SQLException e) {
        }

        return key;
    }

    public String getName() {
        return this.name;
    }

    public String getCatalog() {
        return this.catalog;
    }

    public String getForeignCatalog() {
        return this.fCatalog;
    }

    public String getSchema() {
        return this.schema;
    }

    public String getForeignSchema() {
        return this.fSchema;
    }

    public String getTable() {
        return this.table;
    }

    public String getForeignTable() {
        return this.fTable;
    }

    public String getColumn() {
        return this.column;
    }

    public String getForeignColumn() {
        return this.fColumn;
    }

    public enum Type {
        PRIMARY_KEY, FOREIGN_KEY
    }
}
