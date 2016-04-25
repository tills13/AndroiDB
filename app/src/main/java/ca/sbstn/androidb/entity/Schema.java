package ca.sbstn.androidb.entity;

import java.util.ArrayList;
import java.util.List;

import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Table;

/**
 * Created by tyler on 24/04/16.
 */
public class Schema {
    protected String name;
    protected Database database;
    protected List<Table> tables;

    public Schema(String name, Database database) {
        this.name = name;
        this.database = database;
        this.tables = new ArrayList<>();
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public List<Table> getTables() {
        return tables;
    }
}
