package ca.sbstn.dbtest.callback;

import java.util.List;

import ca.sbstn.dbtest.sql.SQLResult;

/**
 * Created by tills13 on 2015-07-13.
 */
public abstract class SQLExecuteCallback {
    public SQLExecuteCallback() {}

    public abstract void onResult(List<SQLResult>  results);
    public abstract void onSingleResult(SQLResult result);
}
