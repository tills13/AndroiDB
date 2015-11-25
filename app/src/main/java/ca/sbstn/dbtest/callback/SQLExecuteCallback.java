package ca.sbstn.dbtest.callback;

import java.util.List;

import ca.sbstn.dbtest.sql.SQLDataSet;

/**
 * Created by tills13 on 2015-07-13.
 */
public abstract class SQLExecuteCallback {
    public SQLExecuteCallback() {}

    public abstract void onResult(List<SQLDataSet>  results);
    public abstract void onSingleResult(SQLDataSet result);
}
