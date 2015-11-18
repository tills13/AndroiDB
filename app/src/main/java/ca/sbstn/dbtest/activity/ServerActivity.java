package ca.sbstn.dbtest.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.Server;
import ca.sbstn.dbtest.task.FetchDatabasesTask;

public class ServerActivity extends Activity {
    private final String preferencesKey = "AndroiDB";

    public SwipeRefreshLayout swipeLayout;
    public ListView databaseList;
    private SharedPreferences sharedPreferences;
    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_databases);

        this.sharedPreferences = this.getSharedPreferences("AndroiDB", Context.MODE_PRIVATE);

        this.swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        this.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ServerActivity.this.populateDatabaseList();
            }
        });

        this.server = (Server) getIntent().getSerializableExtra("server");
        this.databaseList = (ListView) this.findViewById(R.id.databases);

        ActionBar ab = this.getActionBar();

        if (ab != null) {
            ab.setTitle(this.server.getName());
        }

        this.databaseList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (databaseList == null || databaseList.getChildCount() == 0) ? 0 : databaseList.getChildAt(0).getTop();
                swipeLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        this.databaseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Database database = (Database) ((ListView) parent).getAdapter().getItem(position);

                Intent intent = new Intent(ServerActivity.this, DatabaseActivity.class);
                intent.putExtra("database", database);
                startActivity(intent);
            }
        });

        ((LinearLayout) ((TextView) this.findViewById(R.id.server_name)).getParent()).setBackgroundColor(Color.parseColor(this.server.getColor()));
        ((TextView) this.findViewById(R.id.server_name)).setText(this.server.getName());
        ((TextView) this.findViewById(R.id.connected_as_info)).setText(String.format("Connected as %s", this.server.getUsername()));

        this.populateDatabaseList();

        boolean autoOpenDefault = this.sharedPreferences.getBoolean("database_open_default", false);
        if (!this.server.getDefaultDatabase().equals("") && autoOpenDefault) {
            //Database database = (Database) ((GridView) parent).getAdapter().getItem(position);

            //Intent intent = new Intent(ServerActivity.this, DatabaseActivity.class);
            //intent.putExtra("database", database);
            //startActivity(intent);
        }
    }

    public void populateDatabaseList() {
        this.swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });

        FetchDatabasesTask fetchTablesTask = new FetchDatabasesTask(this);
        fetchTablesTask.execute(this.server);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
