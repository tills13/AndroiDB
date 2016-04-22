package ca.sbstn.androidb.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.fragment.CreateOrEditDatabaseFragment;
import ca.sbstn.androidb.fragment.CreateOrEditServerFragment;
import ca.sbstn.androidb.fragment.DatabaseListFragment;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;

/**
 * Created by tyler on 21/04/16.
 */
public class ServerActivity extends BaseActivity implements DatabaseListFragment.OnDatabaseSelectedListener {
    public static final String SERVER_PARAM = "SERVER";
    protected Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        this.toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(this.toolbar);

        this.server = (Server) getIntent().getSerializableExtra(SERVER_PARAM);

        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setShowHideAnimationEnabled(true);
        }

        DatabaseListFragment databaseListFragment = DatabaseListFragment.newInstance(this.server);
        this.putContextFragment(databaseListFragment, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Fragment currentFragment = this.fragmentManager.findFragmentById(R.id.context_fragment);

                if (currentFragment instanceof CreateOrEditServerFragment) {
                    this.fragmentManager.popBackStack();
                    return true;
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
        }

        return super.onOptionsItemSelected(item);
    }

    public Server getServer() {
        return this.getServer(true);
    }

    public Server getServer(boolean reload) {
        if (reload) {
            try {
                String serverJsonString = this.sharedPreferences.getString(AndroiDB.SHARED_PREFS_SERVER_PREFIX + this.server.getId(), "");
                JSONObject serverJson = new JSONObject(serverJsonString);

                return new Server(
                    serverJson.getString("id"),
                    serverJson.getString("name"),
                    serverJson.getString("host"),
                    serverJson.getInt("port"),
                    serverJson.getString("user"),
                    serverJson.getString("password"),
                    serverJson.getString("db"),
                    serverJson.getString("color")
                );
            } catch (JSONException e) {
                return null;
            }
        }

        return this.server;
    }

    @Override
    public void onDatabaseSelected(Database database) {

    }
}
