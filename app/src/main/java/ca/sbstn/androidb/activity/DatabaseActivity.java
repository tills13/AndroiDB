package ca.sbstn.androidb.activity;

import android.content.Intent;
import android.os.Bundle;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.fragment.TableListFragment;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.sql.Table;
import io.realm.Realm;

public class DatabaseActivity extends BaseActivity implements TableListFragment.OnTableSelectedListener{
    public static final String DATABASE_PARAM = "DATABASE";
    public static final String SERVER_PARAM_NAME = "SERVER_NAME";

    protected Server server;
    protected Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);

        Realm realm = Realm.getDefaultInstance();

        String databaseName = getIntent().getStringExtra(DATABASE_PARAM);
        String serverName = getIntent().getStringExtra(SERVER_PARAM_NAME);

        this.server = realm.where(Server.class).equalTo("name", serverName).findFirst();

        this.setToolbarTitle(databaseName);
        this.setToolbarSubtitle(server.getName());
        this.setToolbarColor(server.getColor(), true);

        TableListFragment tableListFragment = TableListFragment.newInstance(server, databaseName);
        this.putContextFragment(tableListFragment, false);
    }

    public Database getDatabase() {
        return this.database;
    }

    @Override
    public void onTableSelected(Table table) {
        Intent intent = new Intent(this, ViewDataActivity.class);
        intent.putExtra(ViewDataActivity.PARAM_SERVER_NAME, this.server.getName());
        intent.putExtra(ViewDataActivity.PARAM_TABLE, table);
        startActivity(intent);
    }
}
