package ca.sbstn.dbtest.callback;

import java.sql.ResultSet;

/**
 * Created by tills13 on 2015-07-13.
 */
public abstract class SQLExecuteCallback {
    private ResultSet results;

    public SQLExecuteCallback() {
        this.results = null;
    }

    public abstract void onSuccess(ResultSet results);

    public abstract void onError(Exception e);
}
