package ca.sbstn.androidb.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.fragment.CreateOrEditDatabaseFragment;
import ca.sbstn.androidb.fragment.CreateOrEditServerFragment;
import ca.sbstn.androidb.fragment.DatabaseListFragment;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;
import io.realm.Realm;

public class ServerActivity extends BaseActivity implements DatabaseListFragment.OnDatabaseSelectedListener {
    public static final String SERVER_PARAM_NAME = "PARAM_NAME";
    protected Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);

        Realm realm = Realm.getDefaultInstance();
        String serverName = getIntent().getStringExtra(SERVER_PARAM_NAME);

        this.server = realm.where(Server.class).equalTo("name", serverName).findFirst();
        this.setToolbarColor(this.server.getColor(), true);

        DatabaseListFragment databaseListFragment = DatabaseListFragment.newInstance(this.server);
        this.putContextFragment(databaseListFragment, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(SERVER_PARAM_NAME, this.server.getName());

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
                CreateOrEditDatabaseFragment createOrEditDatabaseFragment = CreateOrEditDatabaseFragment.newInstance(this.server);
                this.putContextFragment(createOrEditDatabaseFragment, true);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public Server getServer() {
        return this.getServer(true);
    }

    public Server getServer(boolean reload) {
        return this.server;
    }

    @Override
    public void onDatabaseSelected(Database database) {
        Intent intent = new Intent(this, DatabaseActivity.class);
        intent.putExtra(DatabaseActivity.SERVER_PARAM_NAME, this.server.getName());
        intent.putExtra(DatabaseActivity.DATABASE_PARAM, database.getName());
        startActivity(intent);
    }

    @Override
    public void onDatabaseLongPressed(Database database) {
        CreateOrEditDatabaseFragment createOrEditDatabaseFragment = CreateOrEditDatabaseFragment.newInstance(database);
        this.putDetailsFragment(createOrEditDatabaseFragment, true);
    }
}
