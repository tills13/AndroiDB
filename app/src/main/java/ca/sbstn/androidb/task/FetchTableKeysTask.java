package ca.sbstn.androidb.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ca.sbstn.androidb.callback.SQLExecuteCallback;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Key;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.sql.Table;

/**
 * Created by tills13 on 2015-07-12.
 */
public class FetchTableKeysTask extends AsyncTask<Table, Void, Void> {
    public static String TAG = "FETCHTABLEKEYSTASK";

    private Context context;
    private SQLExecuteCallback callback;

    public FetchTableKeysTask(Context context) {
        this.context = context;
    }
    public FetchTableKeysTask(Context context, SQLExecuteCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Table ... tables) {
        Table table = tables[0];
        Database database = table.getDatabase();
        Server server = database.getServer();

        String url = database.getConnectionString();

        try {
            Connection connection = DriverManager.getConnection(url, server.getUsername(), server.getPassword());
            ResultSet pkeysResults = connection.getMetaData().getPrimaryKeys(null, table.getSchema(), table.getName());
            ResultSet fkeysResults = connection.getMetaData().getImportedKeys(null, table.getSchema(), table.getName());

            List<Key> primaryKeys = new ArrayList<>();
            List<Key> foreignKeys = new ArrayList<>();

            while (pkeysResults.next()) {
                primaryKeys.add(Key.from(Key.Type.PRIMARY_KEY, pkeysResults));
            }

            while (fkeysResults.next()) {
                foreignKeys.add(Key.from(Key.Type.FOREIGN_KEY, fkeysResults));
            }

            table.setForeignKeys(foreignKeys);
            table.setPrimaryKeys(primaryKeys);
        } catch (SQLException e) {
            Log.e(FetchTableKeysTask.TAG, e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (this.callback != null) this.callback.onSingleResult(null);
    }
}
