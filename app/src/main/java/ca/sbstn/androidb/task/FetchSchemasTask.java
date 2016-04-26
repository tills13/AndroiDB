package ca.sbstn.androidb.task;

import android.content.Context;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ca.sbstn.androidb.callback.Callback;
import ca.sbstn.androidb.entity.Schema;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;

/**
 * Created by tills13 on 2015-07-12.
 */
public class FetchSchemasTask extends BaseTask<Database, Void, List<Schema>> {
    public FetchSchemasTask(Context context, Callback<List<Schema>> callback) {
        super(context, callback);
    }

    @Override
    protected List<Schema> doInBackground(Database ... databases) {
        Database database = databases[0];
        Server server = database.getServer();
        List<Schema> schemas = new ArrayList<>();

        String query = "SELECT oid, nspname AS name, nspname = ANY (current_schemas(true)) AS is_on_search_path, oid = pg_my_temp_schema() AS is_my_temp_schema, pg_is_other_temp_schema(oid) AS is_other_temp_schema FROM pg_namespace";

        try {
            Connection connection = DriverManager.getConnection(database.getConnectionString(), server.getUsername(), server.getPassword());
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                Schema schema = new Schema(results.getString("name"), database);
                schemas.add(schema);
            }

            connection.close();
        } catch (SQLException e) {
            this.setException(e);
        }

        return schemas;
    }
}
