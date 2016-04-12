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

import ca.sbstn.androidb.callback.SQLExecuteCallback;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.SQLDataSet;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.sql.Table;

/**
 * Created by tills13 on 2015-07-12.
 */
public class ExecuteQueryWithCallbackTask extends AsyncTask<String, Void, List<SQLDataSet>> {
    private static final String TAG = "EXECUTEQUERYTASK";

    private Context context;
    private Database database;
    private Table table;
    private boolean usePostgres;
    private boolean expectResult;

    private SQLExecuteCallback callback;
    private ProgressBar progressBar;

    public ExecuteQueryWithCallbackTask(Context context, Database database, SQLExecuteCallback callback) {
        this.context = context;
        this.database = database;
        this.callback = callback;

        this.usePostgres = false;
    }

    public ExecuteQueryWithCallbackTask(Context context, Table table, SQLExecuteCallback callback) {
        this(context, table.getDatabase(), callback);
        this.table = table;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setUsePostgres(boolean usePostgres) {
        this.usePostgres = usePostgres;
    }

    public void setExpectResult(boolean expectResult) {
        this.expectResult = expectResult;
    }

    @Override
    protected List<SQLDataSet> doInBackground(String ... queries) {
        List<SQLDataSet> results = new ArrayList<>();

        //Connection connection;
        if (this.database == null && this.table != null) {
            this.database = this.table.getDatabase();
        }

        Server server = this.database.getServer();
        String url = String.format("jdbc:postgresql://%s:%d/%s", server.getHost(), server.getPort(), usePostgres ? "postgres" : this.database.getName());


        for (String query : queries) {
            SQLDataSet sqlDataSet = new SQLDataSet();
            sqlDataSet.setQuery(query);

            try {
                Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                if (expectResult) {
                    ResultSet resultSet = statement.executeQuery(query);
                    sqlDataSet = SQLDataSet.from(resultSet);

                    if (sqlDataSet != null) {
                        sqlDataSet.setTable(table);
                    } else {
                        throw new Exception("result expected");
                    }
                } else {
                    statement.executeUpdate(query);
                }
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
                sqlDataSet = new SQLDataSet();
                sqlDataSet.setError(e);
            }

            results.add(sqlDataSet);
        }

        return results;
    }

    @Override
    protected void onPostExecute(List<SQLDataSet> sqlDataSet) {
        super.onPostExecute(sqlDataSet);

        if (this.progressBar != null) {
            ViewGroup parent = ((ViewGroup) this.progressBar.getParent());
            if (parent != null) parent.removeView(this.progressBar);
        }

        if (this.callback != null) {
            if (sqlDataSet.size() == 1) callback.onSingleResult(sqlDataSet.get(0));
            else callback.onResult(sqlDataSet);
        }
    }

    @Override
    protected void onPreExecute() {
        if (this.progressBar != null) {
            if (!this.progressBar.isIndeterminate()) {
                this.progressBar.setIndeterminate(true);
            }
        }
    }
}
