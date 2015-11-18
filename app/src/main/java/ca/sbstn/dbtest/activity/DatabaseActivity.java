package ca.sbstn.dbtest.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.adapter.TableListAdapter;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.Table;
import ca.sbstn.dbtest.task.FetchTablesTask;


public class DatabaseActivity extends Activity {
    public SwipeRefreshLayout swipeLayout;
    public ListView tableList;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tables);

        this.swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        this.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DatabaseActivity.this.populateTables();
            }
        });

        this.database = (Database) getIntent().getSerializableExtra("database");
        this.tableList = (ListView) this.findViewById(R.id.table_list);

        final ActionBar ab = this.getActionBar();
        if (ab != null) ab.setTitle(this.database.getName());

        this.tableList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TableListAdapter adapter = ((TableListAdapter) ((ListView) parent).getAdapter());

                if (adapter.isHeader(position)) {
                    adapter.toggleCollapsed(adapter.getHeader(position));
                    adapter.notifyDataSetChanged();
                    tableList.invalidate();
                } else {
                    Table table = (Table) ((ListView) parent).getAdapter().getItem(position);

                    Intent intent = new Intent(DatabaseActivity.this, TableActivity.class);
                    intent.putExtra("table", table);

                    startActivity(intent);
                }
            }
        });

        this.tableList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                TableListAdapter adapter = ((TableListAdapter) ((ListView) view).getAdapter());
                if (adapter != null) {
                    String header = adapter.getHeader(firstVisibleItem);

                    if (firstVisibleItem > 0) {
                        ab.setSubtitle(header);
                    } else {
                        ab.setSubtitle("");
                    }
                }
            }
        });

        this.populateTables();
    }

    public void populateTables() {
        this.swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(true);
            }
        });

        FetchTablesTask fetchTablesTask = new FetchTablesTask(this);
        fetchTablesTask.execute(this.database);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_database, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        TableListAdapter adapter = (TableListAdapter) this.tableList.getAdapter();

        if (id == R.id.action_query_runner) {
            Intent intent = new Intent(this, QueryRunnerActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.show_tables) {
            boolean checked = item.isChecked();
            adapter.setShowTables(!checked);
            item.setChecked(!checked);

            adapter.notifyDataSetChanged();
        } else if (id == R.id.show_indexes) {
            boolean checked = item.isChecked();
            adapter.setShowIndexes(!checked);
            item.setChecked(!checked);

            adapter.notifyDataSetChanged();
        } else if (id == R.id.show_views) {
            boolean checked = item.isChecked();
            adapter.setShowViews(!checked);
            item.setChecked(!checked);

            adapter.notifyDataSetChanged();
        } else if (id == R.id.show_sequences) {
            boolean checked = item.isChecked();
            adapter.setShowSequences(!checked);
            item.setChecked(!checked);

            adapter.notifyDataSetChanged();
        } else if (id == R.id.sort_name) {
            Log.d("asdasdasd", "here name");
            ((TableListAdapter) this.tableList.getAdapter()).setSortType(0);
            ((TableListAdapter) this.tableList.getAdapter()).notifyDataSetChanged();

            this.tableList.invalidate();
        } else if (id == R.id.sort_type) {
            Log.d("asdasdasd", "here type");

            ((TableListAdapter) this.tableList.getAdapter()).setSortType(1);
            ((TableListAdapter) this.tableList.getAdapter()).notifyDataSetChanged();

            this.tableList.invalidate();
        } else if (id == R.id.sort_schema) {
            Log.d("asdasdasd", "here sceham");

            ((TableListAdapter) this.tableList.getAdapter()).setSortType(2);
            ((TableListAdapter) this.tableList.getAdapter()).notifyDataSetChanged();

            this.tableList.invalidate();
        }

        return super.onOptionsItemSelected(item);
    }
}
