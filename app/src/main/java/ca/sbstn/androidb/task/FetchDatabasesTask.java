package ca.sbstn.androidb.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.adapter.DatabaseListAdapter;
import ca.sbstn.androidb.callback.SQLExecuteCallback;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;

/**
 * Created by tills13 on 15-06-26.
 */
public class FetchDatabasesTask extends AsyncTask<Server, Void, List<Database>> {
    public static String TAG = "FETCHDATABASESTASK";

    private Context context;
    private SQLExecuteCallback sqlExecuteCallback;
    private DatabaseListAdapter mAdapter;

    public FetchDatabasesTask(Context context, DatabaseListAdapter adapter) {
        this.context = context;
        this.mAdapter = adapter;
    }

    public void setCallback(SQLExecuteCallback sqlExecuteCallback) {
        this.sqlExecuteCallback = sqlExecuteCallback;
    }

    @Override
    protected List<Database> doInBackground(Server ... servers) {
        Server server = servers[0];
        List<Database> databases = new ArrayList<>();

        try {
            Class.forName("org.postgresql.Driver").newInstance();

            String url = String.format("jdbc:postgresql://%s:%d/%s",
                    server.getHost(),
                    server.getPort(),
                    server.getDefaultDatabase()
            );

            String query = this.context.getResources().getString(R.string.db_query_fetch_databases);

            Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet results = stmt.executeQuery();

            while (results.next()) {
                Database database = Database.from(results, server);
                databases.add(database);
            }
        } catch (Exception e) {
            Log.d(FetchDatabasesTask.TAG, e.getMessage());
        }

        return databases;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(List<Database> databases) {
        super.onPostExecute(databases);

        this.mAdapter.setDatabases(databases);
        this.mAdapter.notifyDataSetChanged();

        if (this.sqlExecuteCallback != null) {
            this.sqlExecuteCallback.onSingleResult(null);
        }
    }
}
