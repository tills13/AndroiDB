package ca.sbstn.androidb.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;

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
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (!savedInstanceState.containsKey(SERVER_PARAM)) finish();
            this.server = (Server) savedInstanceState.get(SERVER_PARAM);
        } else {
            this.server = (Server) getIntent().getSerializableExtra(SERVER_PARAM);
        }

        this.setToolbarColor(this.server.getColor(), true);

        DatabaseListFragment databaseListFragment = DatabaseListFragment.newInstance(this.server);
        this.putContextFragment(databaseListFragment, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SERVER_PARAM, this.server);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Fragment currentFragment = this.fragmentManager.findFragmentById(R.id.context_fragment);

                if (currentFragment instanceof CreateOrEditServerFragment) {
                    this.fragmentManager.popBackStack();
                    return true;
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
            }

            case R.id.action_new: {
                CreateOrEditDatabaseFragment createOrEditDatabaseFragment = CreateOrEditDatabaseFragment.newInstance(null);
                this.putContextFragment(createOrEditDatabaseFragment, true);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public Server getServer() {
        return this.getServer(true);
    }

    public Server getServer(boolean reload) {
        if (reload) {
            Map<String, Server> servers = this.getServers();
            this.server = servers.get(this.server.getId());
        }

        return this.server;
    }

    public Map<String, Server> getServers() {
        Gson gson = new Gson();
        String serverJson = this.sharedPreferences.getString(ca.sbstn.androidb.application.AndroiDB.PREFERENCES_KEY_SERVERS, "{}");

        Type serverListType = new TypeToken<Map<String,Server>>(){}.getType();
        return gson.fromJson(serverJson, serverListType);
    }

    @Override
    public void onDatabaseSelected(Database database) {
        Intent intent = new Intent(this, DatabaseActivity.class);
        intent.putExtra(DatabaseActivity.DATABASE_PARAM, database);
        startActivity(intent);
    }

    @Override
    public void onDatabaseLongPressed(Database database) {
        CreateOrEditDatabaseFragment createOrEditDatabaseFragment = CreateOrEditDatabaseFragment.newInstance(database);
        this.putDetailsFragment(createOrEditDatabaseFragment, true);
    }
}
