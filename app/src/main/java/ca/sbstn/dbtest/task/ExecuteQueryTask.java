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
import ca.sbstn.dbtest.sql.Table;
import ca.sbstn.dbtest.view.SQLTableLayout;

/**
 * Created by tills13 on 2015-07-12.
 */
public class ExecuteQueryTask extends AsyncTask<String, Void, SQLResult> {
    private static final String TAG = "EXECUTEQUERYTASK";

    private Context context;
    private Database database;
    private Table table;

    private SQLTableLayout tableLayout;

    private SQLExecuteCallback callback;
    private ProgressBar progressBar;

    public ExecuteQueryTask(Context context, Database database, SQLTableLayout tableLayout) {
        this.context = context;
        this.database = database;
        this.tableLayout = tableLayout;
    }

    public ExecuteQueryTask(Context context, Database database, Table table, SQLTableLayout tableLayout) {
        this.context = context;
        this.database = database;
        this.table = table;
        this.tableLayout = tableLayout;
    }

    public void setCallback(SQLExecuteCallback callback) {
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
            ResultSet results = statement.executeQuery(query);

            if (this.table == null) {
                this.table = new Table(this.database);
            }

            this.table.setData(results);
            SQLResult result = SQLResult.from(results);


            boolean shouldFetchAbsoluteRowCount = context.getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("fetch_absolute_row_count", true);

            if (shouldFetchAbsoluteRowCount) {
                ResultSet rowCount = statement.executeQuery(String.format(
                        "SELECT COUNT(1) AS count FROM \"%s\".\"%s\";",
                        table.getSchema(), table.getName()
                ));

                if (rowCount.next()) {
                    table.setTotalRows(rowCount.getInt("count"));
                }
            }

            connection.close();
            return result;
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(SQLResult sqlResult) {
        super.onPostExecute(sqlResult);

        if (this.progressBar != null) {
            ViewGroup parent = ((ViewGroup) this.progressBar.getParent());
            if (parent != null) parent.removeView(this.progressBar);
        }

        if (this.tableLayout != null) {
            this.tableLayout.setTable(table);
        }

        if (this.callback != null) this.callback.onSuccess(sqlResult);
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
