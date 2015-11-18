package ca.sbstn.dbtest.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.callback.SQLExecuteCallback;
import ca.sbstn.dbtest.sql.Key;
import ca.sbstn.dbtest.sql.SQLUtils;
import ca.sbstn.dbtest.sql.Table;
import ca.sbstn.dbtest.task.ExecuteQueryTask;
import ca.sbstn.dbtest.task.ExecuteQueryWithCallbackTask;
import ca.sbstn.dbtest.view.SQLTableLayout;

public class RowInspectorActivity extends Activity {
    private int index;

    private Table table;

    private TableLayout pkeysTable;
    private LinearLayout fkeysContainer;
    private LinearLayout columnsContainer;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_row_inspector);

        this.index = getIntent().getIntExtra("rowIndex", 0);
        this.table = (Table) getIntent().getSerializableExtra("table");

        this.columnsContainer = (LinearLayout) this.findViewById(R.id.columns_container);
        this.fkeysContainer = (LinearLayout) this.findViewById(R.id.fkeys_container);
        this.pkeysTable = (TableLayout) this.findViewById(R.id.primary_keys);
        this.swipeRefreshLayout = (SwipeRefreshLayout) this.findViewById(R.id.swipe_container);

        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        this.handler = new Handler(Looper.getMainLooper());

        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setTitle(this.table.getName().equals("") ? "Row Inspector" : this.table.getName());
        }

        //this.refresh();
        this.generateStaticLayout();
    }

    public void refresh() {
        this.columnsContainer.removeAllViews();
        this.pkeysTable.removeAllViews();
        this.fkeysContainer.removeAllViews();

        List<Key> primaryKeys = this.table.getPrimaryKeys();

        // kind of "fail out" nicely
        // both tables with no primary keys
        // and table returned via custom queries
        if (primaryKeys.size() == 0) {
            this.generateStaticLayout();
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
            public void onSuccess(ResultSet results) {
                try {
                    Statement statement = results.getStatement();
                    Connection connection = statement.getConnection();
                    ResultSetMetaData rsmd = results.getMetaData();
                    DatabaseMetaData dbmd = connection.getMetaData();

                    //dbmd.getPrimaryKeys(null, null, table.getName());

                    if (!results.next()) {
                        this.onError(new Exception("no results returned"));
                    }

                    int columnCount = rsmd.getColumnCount();
                    LayoutInflater inflater = LayoutInflater.from(RowInspectorActivity.this);

                    for (int i = 1; i < (columnCount + 1); i++) {
                        String name = rsmd.getColumnName(i);
                        Log.d("col", name);
                        String type = rsmd.getColumnTypeName(i);
                        Log.d("col type", type);
                        String value = results.getString(i);

                        View view = inflater.inflate(R.layout.row_inspector_column_container, null);

                        ((TextView) view.findViewById(R.id.column_name)).setText(name);
                        ((TextView) view.findViewById(R.id.column_type)).setText(type);
                        ((TextView) view.findViewById(R.id.column_value)).setText("");

                        columnsContainer.addView(view);
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    this.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("callback", e.getMessage() == null ? "no message provided" : e.getMessage());
            }
        };

        ProgressBar loadingBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        loadingBar.setIndeterminate(true);

        this.columnsContainer.addView(loadingBar);

        ExecuteQueryWithCallbackTask queryTask = new ExecuteQueryWithCallbackTask(this, this.handler, this.table.getDatabase(), callback);
        queryTask.setProgressBar(loadingBar);
        queryTask.execute(query);
    }

    public void generateStaticLayout() {
        // lmfao
        if (this.table.getPrimaryKeys().size() == 0) {
            int padding = (int) getResources().getDimension(R.dimen.container_padding);
            TextView notice = new TextView(this);
            notice.setText("No primary keys for table");
            notice.setPadding(padding, 0, padding, padding);
            pkeysTable.addView(notice);
        }


        LayoutInflater inflater = LayoutInflater.from(this);

        int mIndex = 0;
        for (Key primaryKey : this.table.getPrimaryKeys()) {

            TableRow row = new TableRow(this);
            LinearLayout keyNameContainer = (LinearLayout) inflater.inflate(R.layout.table_cell, null);

            TextView keyNameTextView = (TextView) keyNameContainer.findViewById(R.id.cell_text);
            keyNameTextView.setText(primaryKey.getName());

            LinearLayout keyColumnsContainer = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.table_cell, null);

            TextView keyColumnTextView = (TextView) keyColumnsContainer.findViewById(R.id.cell_text);
            keyColumnTextView.setText(primaryKey.getColumn());
            keyColumnTextView.setGravity(Gravity.END);

            row.addView(keyNameContainer);
            row.addView(keyColumnsContainer);

            this.pkeysTable.addView(row);

            if (mIndex++ % 2 == 0) row.setBackgroundColor(Color.argb(10, 0, 0, 0));
        }

        for (int i = 0; i < this.table.getColumns().length; i++) {
            LinearLayout container = (LinearLayout) inflater.inflate(R.layout.row_inspector_column_container, null);

            String name = this.table.getColumns()[i];
            String value = this.table.getRow(this.index)[i];

            ((TextView) container.findViewById(R.id.column_name)).setText(name);
            ((TextView) container.findViewById(R.id.column_type)).setText("");
            ((TextView) container.findViewById(R.id.column_value)).setText(value);

            this.columnsContainer.addView(container);
        }

        if (this.table.getForeignKeys().size() == 0) {
            int padding = (int) getResources().getDimension(R.dimen.container_padding);
            TextView notice = new TextView(this);
            notice.setGravity(Gravity.END);
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
            TextView label = new TextView(RowInspectorActivity.this);
            label.setText(name);
            label.setPadding(padding, padding, padding, padding);
            fkeysContainer.addView(label);

            HorizontalScrollView hsv = new HorizontalScrollView(RowInspectorActivity.this);
            ProgressBar loadingBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            loadingBar.setIndeterminate(true);

            SQLTableLayout tableLayout = new SQLTableLayout(RowInspectorActivity.this);
            hsv.addView(tableLayout);

            fkeysContainer.addView(hsv);

            int loadingIndex = ((ViewGroup) hsv.getParent()).indexOfChild(hsv);
            fkeysContainer.addView(loadingBar, loadingIndex);

            int index = java.util.Arrays.asList(this.table.getColumns()).indexOf(fkeyColumnName);
            if (index >= 0) {
                ExecuteQueryTask executeQueryTask = new ExecuteQueryTask(this, this.table.getDatabase(), tableLayout);
                executeQueryTask.setProgressBar(loadingBar);
                executeQueryTask.execute(String.format(getResources().getString(R.string.db_query_fetch_all_fkey2), refTable, refColumnName, this.table.getRow(this.index)[index]));
            }
        }

        this.swipeRefreshLayout.setRefreshing(false);
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
