package ca.sbstn.dbtest.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.callback.SQLExecuteCallback;
import ca.sbstn.dbtest.sql.Key;
import ca.sbstn.dbtest.sql.SQLResult;
import ca.sbstn.dbtest.sql.SQLUtils;
import ca.sbstn.dbtest.sql.Table;
import ca.sbstn.dbtest.task.ExecuteQueryTask;
import ca.sbstn.dbtest.task.ExecuteQueryWithCallbackTask;
import ca.sbstn.dbtest.task.FetchTableKeysTask;
import ca.sbstn.dbtest.view.SQLTableLayout;

public class RowInspectorActivity extends Activity {
    private int index;

    private Table table;

    private LinearLayout pkeysContainer;
    private LinearLayout fkeysContainer;
    private LinearLayout columnsContainer;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_row_inspector);

        this.index = getIntent().getIntExtra("rowIndex", 0);
        this.table = (Table) getIntent().getSerializableExtra("table");

        this.columnsContainer = (LinearLayout) this.findViewById(R.id.columns_container);
        this.fkeysContainer = (LinearLayout) this.findViewById(R.id.fkeys_container);
        this.pkeysContainer = (LinearLayout) this.findViewById(R.id.primary_keys);
        this.swipeRefreshLayout = (SwipeRefreshLayout) this.findViewById(R.id.swipe_container);

        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setTitle(this.table.getName().equals("") ? "Row Inspector" : this.table.getName());
        }

        this.refresh();
    }

    public void refresh() {
        this.columnsContainer.removeAllViews();
        this.pkeysContainer.removeAllViews();
        this.fkeysContainer.removeAllViews();

        List<Key> primaryKeys = this.table.getPrimaryKeys();

        // kind of "fail out" nicely
        // both tables with no primary keys
        // and table returned via custom queries
        if (primaryKeys.size() == 0) {
            //this.generateStaticLayout();
            return;
        }

        // generate SQL to fetch data from row
        String query = String.format("SELECT * FROM \"%s\".\"%s\" a WHERE ", this.table.getSchema(), this.table.getName());
        List<String> mRow = Arrays.asList(this.table.getRow(index));
        List<String> mColumns = Arrays.asList(this.table.getColumns());

        for (int i = 0; i < primaryKeys.size(); i++) {
            Key key = primaryKeys.get(i);

            String keyValue = mRow.get(mColumns.indexOf(key.getColumn()));
            query = query + SQLUtils.format(String.format("a.\"%s\" = $1", key.getColumn()), keyValue);

            if (i != (primaryKeys.size() - 1)) {
                query = query + " AND ";
            }
        }

        // generate callback to build data after row is fetched
        SQLExecuteCallback callback = new SQLExecuteCallback() {
            @Override
            public void onSuccess(SQLResult results) {
                try {
                    LayoutInflater inflater = LayoutInflater.from(RowInspectorActivity.this);

                    List<SQLResult.Column> columns = results.getColumns();
                    SQLResult.Row row = results.getRow(0); // there should only be one row

                    for (SQLResult.Column column : columns) {
                        String name = column.getName();
                        String type = column.getType();

                        View view = inflater.inflate(R.layout.row_inspector_column_container, null);

                        ((TextView) view.findViewById(R.id.column_name)).setText(name);
                        ((TextView) view.findViewById(R.id.column_type)).setText(type);
                        ((TextView) view.findViewById(R.id.column_value)).setText(row.getString(name));

                        columnsContainer.addView(view);
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    this.onError(e);
                }

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(Exception e) {
                Log.e("callback", e.getMessage() == null ? "no message provided" : e.getMessage());
            }
        };

        ProgressBar loadingBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        loadingBar.setIndeterminate(true);

        this.columnsContainer.addView(loadingBar);

        ExecuteQueryWithCallbackTask queryTask = new ExecuteQueryWithCallbackTask(this, this.table.getDatabase(), callback);
        queryTask.setProgressBar(loadingBar);
        queryTask.execute(query);

        FetchTableKeysTask fetchTableKeysTask = new FetchTableKeysTask(this, new SQLExecuteCallback() {
            @Override
            public void onSuccess(SQLResult results) {
                showPrimaryKeys();
                showForeignKeys();
            }

            @Override
            public void onError(Exception e) {}
        });

        fetchTableKeysTask.execute(this.table);
    }

    public void showPrimaryKeys() {
        LayoutInflater inflater = LayoutInflater.from(this);

        if (this.table.getPrimaryKeys().size() == 0) {
            int padding = (int) getResources().getDimension(R.dimen.container_padding);
            TextView notice = new TextView(this);
            notice.setText("No primary keys for table");
            notice.setPadding(padding, 0, padding, padding);
            pkeysContainer.addView(notice);
        }

        int mIndex = 0;
        for (Key primaryKey : table.getPrimaryKeys()) {
            LinearLayout row = (LinearLayout) inflater.inflate(R.layout.row_inspector_key_container, null);

            ((TextView) row.findViewById(R.id.key_name)).setText(primaryKey.getName());
            ((TextView) row.findViewById(R.id.key_column)).setText(primaryKey.getColumn());

            pkeysContainer.addView(row);

            if (mIndex++ % 2 == 0) row.setBackgroundColor(Color.argb(10, 0, 0, 0));
        }
    }

    public void showForeignKeys() {
        LayoutInflater inflater = LayoutInflater.from(this);

        if (table.getForeignKeys().size() == 0) {
            int padding = (int) getResources().getDimension(R.dimen.container_padding);
            TextView notice = new TextView(this);
            notice.setText("No foreign keys for table");
            notice.setPadding(padding, 0, padding, padding);
            fkeysContainer.addView(notice);
        }

        for (Key foreignKey : this.table.getForeignKeys()) {
            String name = foreignKey.getName();

            String refTable = foreignKey.getTable();
            String refColumnName = foreignKey.getColumn();

            String fkeyTable = foreignKey.getForeignTable();
            String fkeyColumnName = foreignKey.getForeignColumn();

            int padding = (int) getResources().getDimension(R.dimen.container_padding);
            TextView label = new TextView(this);
            label.setText(name);
            label.setPadding(padding, padding, padding, padding);
            fkeysContainer.addView(label);

            HorizontalScrollView hsv = new HorizontalScrollView(this);
            ProgressBar loadingBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            loadingBar.setIndeterminate(true);

            SQLTableLayout tableLayout = new SQLTableLayout(this);
            hsv.addView(tableLayout);

            fkeysContainer.addView(hsv);

            int loadingIndex = ((ViewGroup) hsv.getParent()).indexOfChild(hsv);
            fkeysContainer.addView(loadingBar, loadingIndex);

            int mIndex = java.util.Arrays.asList(table.getColumns()).indexOf(fkeyColumnName);
            if (mIndex >= 0) {
                ExecuteQueryTask executeQueryTask = new ExecuteQueryTask(RowInspectorActivity.this, table.getDatabase(), tableLayout);
                executeQueryTask.setProgressBar(loadingBar);
                executeQueryTask.execute(String.format(getResources().getString(R.string.db_query_fetch_all_fkey2), refTable, refColumnName, this.table.getRow(index)[mIndex]));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_row_inspector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
