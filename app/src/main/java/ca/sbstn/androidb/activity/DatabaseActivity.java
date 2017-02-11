package ca.sbstn.androidb.activity;

import android.content.Intent;
import android.os.Bundle;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.fragment.TableListFragment;
import ca.sbstn.androidb.query.ServerManager;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.sql.Table;
import io.realm.Realm;

public class DatabaseActivity extends BaseActivity implements TableListFragment.OnTableSelectedListener {
    protected Server server;
    protected Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);

        this.server = ServerManager.getServer();
        this.database = ServerManager.getDatabase();

        this.setToolbarTitle(this.database.getName());
        this.setToolbarSubtitle(server.getName());
        this.setToolbarColor(server.getColor(), true);

        TableListFragment tableListFragment = TableListFragment.newInstance();
        this.putContextFragment(tableListFragment, false);
    }

    @Override
    public void onTableSelected(Table table) {
        Intent intent = new Intent(this, ViewDataActivity.class);
        ServerManager.setTable(table);
        startActivity(intent);
    }
}
