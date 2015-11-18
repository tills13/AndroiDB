package ca.sbstn.dbtest.task;

import android.os.AsyncTask;

/**
 * Created by tills13 on 2015-07-12.
 */
public class FetchSchemasTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... params) {
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        String query = "SELECT oid, nspname, nspname = ANY (current_schemas(true)) AS is_on_search_path, oid = pg_my_temp_schema() AS is_my_temp_schema, pg_is_other_temp_schema(oid) AS is_other_temp_schema FROM pg_namespace";
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
