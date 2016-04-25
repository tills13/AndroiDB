package ca.sbstn.androidb.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ca.sbstn.androidb.callback.Callback;
import ca.sbstn.androidb.callback.SQLExecuteCallback;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.SQLDataSet;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.sql.Table;
import ca.sbstn.androidb.view.SQLTableLayout;

/**
 * Created by tills13 on 2015-07-12.
 */
public class ExecuteQueryTask extends BaseTask<String, Void, SQLDataSet> {
    private static final String TAG = "EXECUTEQUERYTASK";

    protected Database database;
    protected Table table;
    protected boolean expectResults;



    public ExecuteQueryTask(Database database, Context context, Callback<SQLDataSet> callback) {
        super(context, callback);

        this.database = database;
        this.expectResults = true;
    }

    public ExecuteQueryTask(Database database, Table table, Context context, Callback<SQLDataSet> callback) {
        this(database, context, callback);

        this.table = table;
    }

    public void setExpectResults(boolean expectResults) {
        this.expectResults = expectResults;
    }

    public boolean isExpectResults() {
        return expectResults;
    }

    @Override
    protected SQLDataSet doInBackground(String ... queries) {
        String query = queries[0];

        Server server = this.database.getServer();
        String url = String.format(Locale.getDefault(), "jdbc:postgresql://%s:%d/%s", server.getHost(), server.getPort(), this.database.getName());

        SQLDataSet sqlDataSet = new SQLDataSet();
        sqlDataSet.setQuery(query);

        try {
            Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            if (this.isExpectResults()) {
                ResultSet resultSet = statement.executeQuery(query);
                sqlDataSet = SQLDataSet.from(resultSet);

                if (this.table != null) {
                    sqlDataSet.setTable(this.table);
                }
            } else {
                statement.executeUpdate(query);
            }
        } catch (Exception e) {
            Log.d(ExecuteQueryTask.TAG, e.getMessage());
            this.setException(e);
        }

        return sqlDataSet;
    }
}
