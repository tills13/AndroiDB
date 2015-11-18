package ca.sbstn.dbtest.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.GridView;
import android.widget.ListView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.activity.ServerActivity;
import ca.sbstn.dbtest.adapter.DatabaseListAdapter;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.Server;

/**
 * Created by tills13 on 15-06-26.
 */
public class FetchDatabasesTask extends AsyncTask<Server, Void, List<Database>> {
    public static String TAG = "FETCHDATABASESTASK";
    public Context context;

    public FetchDatabasesTask(Context context) {
        this.context = context;
    }

    @Override
    protected List<Database> doInBackground(Server... servers) {
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

        ListView databaseList = (ListView) ((ServerActivity) this.context).findViewById(R.id.databases);
        ((ServerActivity) this.context).swipeLayout.setRefreshing(false);

        if (databaseList.getAdapter() == null) {
            databaseList.setAdapter(new DatabaseListAdapter(this.context));
        }

        ((DatabaseListAdapter) databaseList.getAdapter()).setDatabases(databases);
        ((DatabaseListAdapter) databaseList.getAdapter()).notifyDataSetChanged();

        databaseList.invalidate();
        databaseList.requestLayout();
    }
}
