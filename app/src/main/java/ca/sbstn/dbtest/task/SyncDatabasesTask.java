package ca.sbstn.dbtest.task;

import android.content.Context;
import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.util.List;

import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.Server;

/**
 * Created by tills13 on 15-06-26.
 */
public class SyncDatabasesTask extends AsyncTask<Database, Void, List<Database>> {
    public static String TAG = "SyncDatabasesTask";
    private Context context;

    public SyncDatabasesTask(Context context) {
        this.context = context;
    }

    @Override
    protected List<Database> doInBackground(Database ... databases) {
        for (Database database : databases) {
            try {
                String url = database.getConnectionString();
                Server server = database.getServer();

                Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
                DatabaseMetaData databaseMetaData = connection.getMetaData();
            } catch (Exception e) {

            }
        }



        /*try {


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
            Log.d(SyncDatabasesTask.TAG, e.getMessage());
        }

        return databases;*/

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(List<Database> databases) {
        super.onPostExecute(databases);
    }
}
