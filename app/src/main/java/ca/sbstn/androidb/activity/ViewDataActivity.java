package ca.sbstn.androidb.activity;

import android.content.Intent;
import android.os.Bundle;

import java.sql.ResultSet;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.fragment.ViewDataFragment;
import ca.sbstn.androidb.query.QueryExecutor;
import ca.sbstn.androidb.query.ServerManager;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Query;
import ca.sbstn.androidb.sql.SQLDataSet;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.sql.Table;
import io.realm.Realm;

public class ViewDataActivity extends BaseActivity {
    public static final String PARAM_SERVER_NAME = "SERVER_NAME";
    public static final String PARAM_DATABASE = "DATABASE";
    public static final String PARAM_TABLE = "TABLE";
    public static final String PARAM_QUERY = "QUERY";

    protected Server server;
    protected Table table;
    protected Database database;
    protected String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        this.server = ServerManager.getServer();
        this.database = ServerManager.getDatabase();
        this.table = ServerManager.getTable();

        if (this.table != null) {
            this.setToolbarTitle(this.table.getName());
            this.setToolbarSubtitle(this.database.getName());
        } else {
            if (intent.hasExtra(PARAM_QUERY)) {
                this.query = intent.getStringExtra(PARAM_QUERY);

                this.toolbar.setTitle("Custom Query");
                this.toolbar.setSubtitle(this.query);
            }
        }

        this.setToolbarColor(this.server.getColor(), true);
        this.init();
    }

    public void init() {
        String query = this.table == null ? this.query : this.table.getQuery();
        Database database = this.table == null ? this.database : this.table.getDatabase();

        QueryExecutor executor = QueryExecutor.forServer(this.server, database);
        executor.execute(query, new QueryExecutor.BaseCallback<SQLDataSet>() {
            @Override
            public SQLDataSet onResultAsync(ResultSet result) {
                return SQLDataSet.from(result);
            }

            @Override
            public void onResultSync(SQLDataSet result) {
                ViewDataFragment viewDataFragment = ViewDataFragment.newInstance(result);
                putContextFragment(viewDataFragment, false);
            }
        });
    }
}
