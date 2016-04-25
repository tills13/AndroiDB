package ca.sbstn.androidb.task;

import android.content.Context;
import android.os.AsyncTask;
import android.telecom.Call;
import android.util.Log;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.adapter.DatabaseListAdapter;
import ca.sbstn.androidb.callback.Callback;
import ca.sbstn.androidb.callback.SQLExecuteCallback;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;

/**
 * Created by tills13 on 15-06-26.
 */
public class FetchDatabasesTask extends BaseTask<Server, Void, List<Database>> {
    public static String TAG = "FETCHDATABASESTASK";

    public FetchDatabasesTask(Context context) {
        super(context);
    }

    public FetchDatabasesTask(Context context, Callback callback) {
        super(context, callback);
    }

    @Override
    protected List<Database> doInBackground(Server ... servers) {
        Server server = servers[0];
        List<Database> databases = new ArrayList<>();

        try {
            String url = String.format(Locale.getDefault(), "jdbc:postgresql://%s:%d/%s",
                server.getHost(),
                server.getPort(),
                server.getDefaultDatabase()
            );

            String query = this.context.getResources().getString(R.string.db_query_fetch_databases);

            Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet results = stmt.executeQuery();

            while (results.next()) {
                Database database = new Database(server, results.getString("name"), results.getString("owner"), results.getString("comment"));
                databases.add(database);
            }
        } catch (Exception e) {
            Log.d(FetchDatabasesTask.TAG, e.getMessage());
            this.setException(e);
        }

        return databases;
    }
}
