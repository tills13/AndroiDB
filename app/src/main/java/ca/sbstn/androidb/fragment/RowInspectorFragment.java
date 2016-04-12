package ca.sbstn.androidb.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.AndroiDB;
import ca.sbstn.androidb.callback.SQLExecuteCallback;
import ca.sbstn.androidb.sql.Key;
import ca.sbstn.androidb.sql.SQLDataSet;
import ca.sbstn.androidb.sql.SQLUtils;
import ca.sbstn.androidb.sql.Table;
import ca.sbstn.androidb.task.ExecuteQueryWithCallbackTask;
import ca.sbstn.androidb.task.FetchTableKeysTask;
import ca.sbstn.androidb.view.SQLTableLayout;

/**
 * Created by tills13 on 2015-11-24.
 */
public class RowInspectorFragment extends Fragment {
    public static final String PARAM_ROW = "row";

    private SQLDataSet.Row row;
    private LinearLayout columnsContainer;
    private LinearLayout fkeysContainer;
    private LinearLayout pkeysContainer;
    private SwipeRefreshLayout swipeRefreshLayout;

    public RowInspectorFragment() {}

    public static RowInspectorFragment newInstance(SQLDataSet.Row row) {
        RowInspectorFragment rowInspectorFragment = new RowInspectorFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_ROW, row);

        rowInspectorFragment.setArguments(bundle);
        return rowInspectorFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getArguments() != null) {
            this.row = (SQLDataSet.Row) this.getArguments().getSerializable(PARAM_ROW);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (this.row != null) {
            Table table = this.row.getDataSet().getTable();

            if (table != null) {
                ((AndroiDB) getActivity()).setToolbarTitle(table.getName());
                ((AndroiDB) getActivity()).setToolbarSubtitle("");
            } else {
                ((AndroiDB) getActivity()).setToolbarTitle("Row Inspector");
            }
        } else {
            // warn
        }

        this.refresh();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.row_inspector, null);

        this.columnsContainer = (LinearLayout) view.findViewById(R.id.columns_container);
        this.fkeysContainer = (LinearLayout) view.findViewById(R.id.fkeys_container);
        this.pkeysContainer = (LinearLayout) view.findViewById(R.id.primary_keys);
        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    public void refresh() {
        this.columnsContainer.removeAllViews();
        this.pkeysContainer.removeAllViews();
        this.fkeysContainer.removeAllViews();

        Table table = this.row.getDataSet().getTable();

        if (table != null) {
            FetchTableKeysTask fetchTableKeysTask = new FetchTableKeysTask(getContext(), new SQLExecuteCallback() {
                @Override
                public void onResult(List<SQLDataSet> results) {

                }

                @Override
                public void onSingleResult(SQLDataSet result) {
                    showColumns();
                    showPrimaryKeys();
                    showForeignKeys();
                }
            });

            fetchTableKeysTask.execute(table);
        } else {
            this.showPrimaryKeys();
            this.showForeignKeys();
            this.buildColumns(this.row);
        }
    }

    public void showColumns() {
        Table table = this.row.getDataSet().getTable();
        List<Key> primaryKeys = table.getPrimaryKeys();

        if (primaryKeys.size() == 0) {
            this.buildColumns(this.row);
        } else {
            String query = String.format("SELECT * FROM \"%s\".\"%s\" a WHERE ", table.getSchema(), table.getName());

            for (int i = 0; i < table.getPrimaryKeys().size(); i++) {
                Key key = primaryKeys.get(i);

                String keyValue = row.getString(key.getColumn());
                query = query + SQLUtils.format(String.format("a.\"%s\" = $1", key.getColumn()), keyValue);

                if (i != (primaryKeys.size() - 1)) {
                    query = query + " AND ";
                }
            }

            ProgressBar loadingBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
            loadingBar.setIndeterminate(true);

            this.columnsContainer.addView(loadingBar);

            ExecuteQueryWithCallbackTask queryTask = new ExecuteQueryWithCallbackTask(getContext(), table.getDatabase(), new SQLExecuteCallback() {
                @Override
                public void onResult(List<SQLDataSet> results) {}

                @Override
                public void onSingleResult(SQLDataSet sqlResult) {
                    buildColumns(sqlResult.getRow(0));

                    swipeRefreshLayout.setRefreshing(false);
                }
            });

            queryTask.setExpectResult(true);
            queryTask.setProgressBar(loadingBar);
            queryTask.execute(query);
        }
    }

    public void buildColumns(SQLDataSet.Row row) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        List<SQLDataSet.Column> columns = row.getDataSet().getColumns();

        for (SQLDataSet.Column column : columns) {
            String name = column.getName();
            String type = column.getType();

            View view = inflater.inflate(R.layout.row_inspector_column_container, null);

            ((TextView) view.findViewById(R.id.column_name)).setText(name);
            ((TextView) view.findViewById(R.id.column_type)).setText(type);
            ((TextView) view.findViewById(R.id.column_value)).setText(row.getString(name));

            columnsContainer.addView(view);
        }
    }

    public void showPrimaryKeys() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        Table table = this.row.getDataSet().getTable();

        if (table == null || table.getPrimaryKeys().size() == 0) {
            int padding = (int) getResources().getDimension(R.dimen.container_padding);
            TextView notice = new TextView(getContext());
            notice.setText(table == null ? "No primary keys" : "No primary keys for table");
            notice.setPadding(padding, 0, padding, padding);
            notice.setTextColor(getResources().getColor(R.color.grey_400));
            this.pkeysContainer.addView(notice);

            return;
        }

        int mIndex = 0;
        for (Key primaryKey : table.getPrimaryKeys()) {
            LinearLayout row = (LinearLayout) inflater.inflate(R.layout.row_inspector_key_container, null);

            ((TextView) row.findViewById(R.id.key_name)).setText(primaryKey.getName());
            ((TextView) row.findViewById(R.id.key_column)).setText(primaryKey.getColumn());

            this.pkeysContainer.addView(row);

            if (mIndex++ % 2 == 0) row.setBackgroundColor(Color.argb(10, 0, 0, 0));
        }
    }

    public void showForeignKeys() {
        Table table = this.row.getDataSet().getTable();

        if (table == null || table.getForeignKeys().size() == 0) {
            int padding = (int) getResources().getDimension(R.dimen.container_padding);
            TextView notice = new TextView(getContext());
            notice.setText("No foreign keys for table");
            notice.setPadding(padding, 0, padding, padding);
            notice.setTextColor(getResources().getColor(R.color.grey_400));//getColor(R.color.grey_400, getResources().newTheme()));
            fkeysContainer.addView(notice);

            return;
        }

        for (Key foreignKey : table.getForeignKeys()) {
            String name = foreignKey.getName();

            String refTable = foreignKey.getTable();
            String refColumnName = foreignKey.getColumn();

            String fkeyTable = foreignKey.getForeignTable();
            String fkeyColumnName = foreignKey.getForeignColumn();

            int padding = (int) getResources().getDimension(R.dimen.container_padding);
            TextView label = new TextView(getContext());
            label.setText(name);
            label.setPadding(padding, padding, padding, padding);
            label.setTextColor(getResources().getColor(R.color.grey_400));
            fkeysContainer.addView(label);

            ProgressBar loadingBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
            loadingBar.setIndeterminate(true);

            final SQLTableLayout tableLayout = new SQLTableLayout(getContext());

            fkeysContainer.addView(loadingBar);
            fkeysContainer.addView(tableLayout);

            //int mIndex = this.row.getDataSet().getColumnIndex(fkeyColumnName);

            String query = getResources().getString(R.string.db_query_fetch_all_fkey2);
            query = String.format(query, refTable, refColumnName, this.row.getString(fkeyColumnName));

            ExecuteQueryWithCallbackTask executeQueryWithCallbackTask = new ExecuteQueryWithCallbackTask(getContext(), table.getDatabase(), new SQLExecuteCallback() {
                @Override
                public void onResult(List<SQLDataSet> results) {
                    Log.d("here", "here");
                }

                @Override
                public void onSingleResult(SQLDataSet result) {
                    tableLayout.setData(result);
                }
            });

            executeQueryWithCallbackTask.setExpectResult(true);
            executeQueryWithCallbackTask.setProgressBar(loadingBar);
            executeQueryWithCallbackTask.execute(query);
        }
    }
}
