package ca.sbstn.androidb.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ca.sbstn.androidb.adapter.TableListAdapter;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.sql.Table;

/**
 * Created by tills13 on 15-06-26.
 */
public class FetchTablesTask extends AsyncTask<Database, Void, List<Table>> {
    public static String TAG = "FETCHTABLESTASK";

    public Context context;
    private Database database;
    private TableListAdapter adapter;

    private String schema;

    public FetchTablesTask(Context context, TableListAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    public FetchTablesTask forSchema(String schema) {
        this.schema = schema;

        return this;
    }

    @Override
    protected List<Table> doInBackground(Database ... databases) {
        List<Table> tables = new ArrayList<>();

        Database database = databases[0];
        Server server = database.getServer();

        String url = database.getConnectionString();

        try {
            Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
            ResultSet results = connection.getMetaData().getTables(null, this.schema, null, null);

            while (results.next()) {
                Table table = Table.from(results, database);
                tables.add(table);
            }
        } catch (Exception e) {
            Log.d(FetchTablesTask.TAG, e.getMessage());
        }

        return tables;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(List<Table> results) {
        super.onPostExecute(results);

        this.adapter.setItems(this.schema, results);
        this.adapter.notifyDataSetChanged();
    }
}
