package ca.sbstn.dbtest.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.activity.DatabaseActivity;
import ca.sbstn.dbtest.adapter.TableListAdapter;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.Server;
import ca.sbstn.dbtest.sql.Table;

/**
 * Created by tills13 on 15-06-26.
 */
public class FetchTablesTask extends AsyncTask<Database, Void, List<Table>> {
    public static String TAG = "FETCHTABLESTASK";
    public Context context;

    public FetchTablesTask(Context context) {
        this.context = context;
    }

    @Override
    protected List<Table> doInBackground(Database... databases) {
        List<Table> tables = new ArrayList<>();

        Database database = databases[0];
        Server server = database.getServer();

        String url = database.getConnectionString();

        try {
            Class.forName("org.postgresql.Driver").newInstance();

            Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
            ResultSet results = connection.getMetaData().getTables(null, null, null, null);

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

        ListView tableList = (ListView) ((DatabaseActivity) this.context).findViewById(R.id.table_list);
        ((DatabaseActivity) this.context).swipeLayout.setRefreshing(false);

        if (tableList.getAdapter() == null) {
            tableList.setAdapter(new TableListAdapter(this.context));
        }

        ((TableListAdapter) tableList.getAdapter()).setItems(results);
        ((TableListAdapter) tableList.getAdapter()).notifyDataSetChanged();

        tableList.requestLayout();
        tableList.invalidate();
    }
}
