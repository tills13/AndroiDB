package ca.sbstn.androidb.task;

import android.content.Context;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

import ca.sbstn.androidb.callback.Callback;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.SQLDataSet;
import ca.sbstn.androidb.entity.Server;
import ca.sbstn.androidb.sql.Table;

/**
 * Created by tills13 on 2015-07-12.
 */
public class ExecuteQueryTask extends BaseTask<String, Void, SQLDataSet> {
    private static final String TAG = "EXECUTEQUERYTASK";

    protected Server server;
    protected Database database;
    protected Table table;
    protected boolean expectResults;

    public ExecuteQueryTask(Server server, Context context, Callback<SQLDataSet> callback) {
        super(context, callback);

        this.server = server;
        this.expectResults = true;
    }

    public ExecuteQueryTask(Database database, Context context, Callback<SQLDataSet> callback) {
        super(context, callback);

        this.database = database;
        this.expectResults = true;
    }

    public ExecuteQueryTask(Database database, Table table, Context context, Callback<SQLDataSet> callback) {
        this(database, context, callback);

        this.table = table;
        this.expectResults = true;
    }

    public ExecuteQueryTask setExpectResults(boolean expectResults) {
        this.expectResults = expectResults;

        return this;
    }

    public boolean isExpectResults() {
        return expectResults;
    }

    @Override
    protected SQLDataSet doInBackground(String ... queries) {
        String query = queries[0];

        if (this.server == null && this.database == null) return null;

        Server server = this.server == null ? this.database.getServer() : this.server;
        String database = this.database == null ? this.server.getDefaultDatabase() : this.database.getName();

        String url = String.format(Locale.getDefault(), "jdbc:postgresql://%s:%d/%s", server.getHost(), server.getPort(), database);

        SQLDataSet sqlDataSet = new SQLDataSet();
        sqlDataSet.setQuery(query);

        /*try {
            Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());

            try {

            } finally {
                connection.close();
            }
        } catch (Exception e) {

        }*/


        try {
            Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            if (this.isExpectResults()) {
                ResultSet resultSet = statement.executeQuery(query);
                sqlDataSet = SQLDataSet.from(resultSet);

                if (this.table != null && sqlDataSet != null) sqlDataSet.setTable(this.table);
            } else {
                statement.executeUpdate(query);
            }

            connection.close();
        } catch (Exception e) {
            Log.d(ExecuteQueryTask.TAG, e.getMessage());
            this.setException(e);
        }

        return sqlDataSet;
    }
}
