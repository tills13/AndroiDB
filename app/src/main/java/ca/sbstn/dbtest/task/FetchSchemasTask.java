package ca.sbstn.dbtest.task;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ca.sbstn.dbtest.callback.SQLExecuteCallback;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.SQLDataSet;
import ca.sbstn.dbtest.sql.Server;

/**
 * Created by tills13 on 2015-07-12.
 */
public class FetchSchemasTask extends AsyncTask<Database, Void, SQLDataSet> {
    private SQLExecuteCallback callback;

    public FetchSchemasTask(SQLExecuteCallback callback) {
        this.callback = callback;
    }

    @Override
    protected SQLDataSet doInBackground(Database ... databases) {
        Database database = databases[0];
        Server server = database.getServer();

        String query = "SELECT oid, nspname AS name, nspname = ANY (current_schemas(true)) AS is_on_search_path, oid = pg_my_temp_schema() AS is_my_temp_schema, pg_is_other_temp_schema(oid) AS is_other_temp_schema FROM pg_namespace";

        try {
            Connection connection = DriverManager.getConnection(database.getConnectionString(), server.getUsername(), server.getPassword());
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet results = statement.executeQuery();

            return SQLDataSet.from(results);
        } catch (SQLException e) {
            Log.d("FETCHSCHEMASTASK", e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(SQLDataSet sqlDataSet) {
        super.onPostExecute(sqlDataSet);

        if (this.callback != null) {
            this.callback.onSingleResult(sqlDataSet);
        }
    }
}
