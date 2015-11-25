package ca.sbstn.dbtest.task;

import android.content.Context;
import android.os.AsyncTask;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import ca.sbstn.dbtest.callback.SQLExecuteCallback;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.SQLDataSet;
import ca.sbstn.dbtest.sql.Table;
import ca.sbstn.dbtest.view.SQLTableLayout;

/**
 * Created by tills13 on 2015-07-12.
 */
public class ExecuteQueryTask extends AsyncTask<String, Void, List<SQLDataSet>> {
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
    protected List<SQLDataSet> doInBackground(String ... queries) {
        List<SQLDataSet> results = new ArrayList<>();

        /*Server server = this.database.getServer();
        String url = String.format("jdbc:postgresql://%s:%d/%s", server.getHost(), server.getPort(), this.database.getName());

        try {
            for (String query : queries) {
                Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet results = statement.executeQuery(query);


            }


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
        }*/

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
