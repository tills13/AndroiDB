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

import ca.sbstn.dbtest.callback.SQLExecuteCallback;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.SQLResult;
import ca.sbstn.dbtest.sql.Server;

/**
 * Created by tills13 on 2015-07-12.
 */
public class ExecuteQueryWithCallbackTask extends AsyncTask<String, Void, SQLResult> {
    private static final String TAG = "EXECUTEQUERYTASK";

    private Context context;
    private Database database;

    private SQLExecuteCallback callback;
    private ProgressBar progressBar;

    public ExecuteQueryWithCallbackTask(Context context, Database database, SQLExecuteCallback callback) {
        this.context = context;
        this.database = database;
        this.callback = callback;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    protected SQLResult doInBackground(String... queries) {
        String query = queries[0];

        Server server = this.database.getServer();
        String url = String.format("jdbc:postgresql://%s:%d/%s", server.getHost(), server.getPort(), this.database.getName());

        try {
            Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            ResultSet result = statement.executeQuery(query);
            return SQLResult.from(result);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            if (this.callback != null) this.callback.onError(e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(final SQLResult sqlResult) {
        super.onPostExecute(sqlResult);

        if (this.progressBar != null) {
            ViewGroup parent = ((ViewGroup) this.progressBar.getParent());
            if (parent != null) parent.removeView(this.progressBar);
        }

        if (this.callback != null) {
            if (sqlResult != null) {
                callback.onSuccess(sqlResult);
            }
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
