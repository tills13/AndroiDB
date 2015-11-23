package ca.sbstn.dbtest.task;

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

import ca.sbstn.dbtest.callback.SQLExecuteCallback;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.SQLResult;
import ca.sbstn.dbtest.sql.Server;

/**
 * Created by tills13 on 2015-07-12.
 */
public class ExecuteQueryWithCallbackTask extends AsyncTask<String, Void, List<SQLResult>> {
    private static final String TAG = "EXECUTEQUERYTASK";

    private Context context;
    private Database database;
    private boolean usePostgres;

    private SQLExecuteCallback callback;
    private ProgressBar progressBar;

    public ExecuteQueryWithCallbackTask(Context context, Database database, SQLExecuteCallback callback) {
        this.context = context;
        this.database = database;
        this.callback = callback;

        this.usePostgres = false;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setUsePostgres(boolean usePostgres) {
        this.usePostgres = usePostgres;
    }

    @Override
    protected List<SQLResult> doInBackground(String ... queries) {
        List<SQLResult> results = new ArrayList<>();

        //Connection connection;

        Server server = this.database.getServer();
        String url = String.format("jdbc:postgresql://%s:%d/%s", server.getHost(), server.getPort(), usePostgres ? "postgres" : this.database.getName());

        /*try {
            connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
        } catch (SQLException e) {
            //for (String query : queries) {

            //}
        }*/


        for (String query : queries) {
            SQLResult sqlResult;

            try {
                Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet result = statement.executeQuery(query);
                sqlResult = SQLResult.from(result);
            } catch (Exception e) {
                Log.d("EXECUTEQUERYCALLBACK", e.getMessage());
                sqlResult = new SQLResult();
                sqlResult.setQuery(query);
                sqlResult.setError(e);
            }

            results.add(sqlResult);
        }

        return results;
    }

    @Override
    protected void onPostExecute(List<SQLResult> sqlResult) {
        super.onPostExecute(sqlResult);

        if (this.progressBar != null) {
            ViewGroup parent = ((ViewGroup) this.progressBar.getParent());
            if (parent != null) parent.removeView(this.progressBar);
        }

        if (this.callback != null) {
            if (sqlResult.size() == 1) callback.onSingleResult(sqlResult.get(0));
            else callback.onResult(sqlResult);
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
