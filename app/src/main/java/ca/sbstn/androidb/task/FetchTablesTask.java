package ca.sbstn.androidb.task;

import android.content.Context;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ca.sbstn.androidb.callback.Callback;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.entity.Server;
import ca.sbstn.androidb.sql.Table;

/**
 * Created by tills13 on 15-06-26.
 */
public class FetchTablesTask extends BaseTask<Database, Void, List<Table>> {
    public static String TAG = "FETCHTABLESTASK";

    private final String [] simplifiedTableTypes = {"TABLE", "VIEW", "SYSTEM TABLE", "INDEX", "SEQUENCE"};
    private final String [] allTableTypes = {"TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM", "INDEX", "SEQUENCE"};

    protected String schema;
    protected boolean showAll = false;

    public FetchTablesTask(Context context, Callback<List<Table>> callback) {
        super(context, callback);
    }

    public FetchTablesTask forSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
    }

    @Override
    protected List<Table> doInBackground(Database ... databases) {
        List<Table> tables = new ArrayList<>();

        Database database = databases[0];
        Server server = database.getServer();

        String url = database.getConnectionString();

        try {
            Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
            ResultSet results = connection.getMetaData().getTables(null, this.schema, null, (this.showAll ? this.allTableTypes : this.simplifiedTableTypes));

            while (results.next()) {
                Table table = Table.from(results, database);
                tables.add(table);
            }

            connection.close();
        } catch (Exception e) {
            Log.d(FetchTablesTask.TAG, e.getMessage());
        }

        return tables;
    }
}
