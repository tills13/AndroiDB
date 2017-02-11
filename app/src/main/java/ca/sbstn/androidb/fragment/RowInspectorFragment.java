package ca.sbstn.androidb.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.query.QueryExecutor;
import ca.sbstn.androidb.query.ServerManager;
import ca.sbstn.androidb.sql.Key;
import ca.sbstn.androidb.sql.SQLDataSet;
import ca.sbstn.androidb.sql.SQLUtils;
import ca.sbstn.androidb.sql.Table;
import ca.sbstn.androidb.view.SQLTableLayout;

public class RowInspectorFragment extends Fragment {
    public static final String PARAM_ROW = "ROW";

    private SQLDataSet.Row row;

    private SwipeRefreshLayout layout;
    @BindView(R.id.columns_container) protected LinearLayout columnsContainer;
    @BindView(R.id.primary_keys) protected LinearLayout pkeysContainer;

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
                ((BaseActivity) getActivity()).setToolbarTitle(table.getName());
                ((BaseActivity) getActivity()).setToolbarSubtitle("");
            }
        }

        this.refresh();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.layout = (SwipeRefreshLayout) inflater.inflate(R.layout.row_inspector, container, false);
        ButterKnife.bind(this, this.layout);

        this.layout.setOnRefreshListener(() -> {
            refresh();
            this.layout.setRefreshing(false);
        });

        return this.layout;
    }

    public void refresh() {
        this.columnsContainer.removeAllViews();
        this.pkeysContainer.removeAllViews();

        Table table = this.row.getDataSet().getTable();

        if (table != null) {
            ServerManager.getExecutor().execute((connection) -> {
                ResultSet pkeysResults = connection.getMetaData().getPrimaryKeys(null, table.getSchema(), table.getName());
                ResultSet fkeysResults = connection.getMetaData().getImportedKeys(null, table.getSchema(), table.getName());

                List<Key> primaryKeys = new ArrayList<>();
                List<Key> foreignKeys = new ArrayList<>();

                while (pkeysResults.next()) {
                    primaryKeys.add(Key.from(Key.Type.PRIMARY_KEY, pkeysResults));
                }

                while (fkeysResults.next()) {
                    foreignKeys.add(Key.from(Key.Type.FOREIGN_KEY, fkeysResults));
                }

                table.setForeignKeys(foreignKeys);
                table.setPrimaryKeys(primaryKeys);

                return null;
            }, new QueryExecutor.BaseCallback<Void>() {
                @Override
                public void onResultSync(Void result) {
                    showColumns();
                    showPrimaryKeys();
                }
            });
        } else {
            this.showPrimaryKeys();
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
            this.columnsContainer.addView(loadingBar);

            ServerManager.getExecutor().execute(query, new QueryExecutor.BaseCallback<SQLDataSet>() {
                @Override
                public SQLDataSet onResultAsync(ResultSet result) {
                    return SQLDataSet.from(result);
                }

                @Override
                public void onResultSync(SQLDataSet result) {
                    columnsContainer.removeView(loadingBar);
                    buildColumns(result.getRow(0));
                    layout.setRefreshing(false);
                }
            });
        }
    }

    public void buildColumns(SQLDataSet.Row row) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        List<SQLDataSet.Column> columns = row.getDataSet().getColumns();
        Table table = ServerManager.getTable();

        for (SQLDataSet.Column column : columns) {
            String name = column.getName();
            String type = column.getType();

            LinearLayout view;

            if (table.columnIsForeignKey(name)) {
                view = (LinearLayout) inflater.inflate(R.layout.row_inspector_fkey_container, columnsContainer, false);

                SQLTableLayout tableLayout = (SQLTableLayout) view.findViewById(R.id.sql_table_layout);
                ProgressBar loadingBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
                loadingBar.setBackgroundColor(getResources().getColor(R.color.colorAccent, null));
                loadingBar.setIndeterminate(true);

                view.addView(loadingBar);

                Key foreignKey = table.getForeignKey(name);

                String refTable = foreignKey.getTable();
                String refColumnName = foreignKey.getColumn();

                String fkeyColumnName = foreignKey.getForeignColumn();

                String query = getResources().getString(R.string.db_query_fetch_all_fkey2);
                query = String.format(query, refTable, refColumnName, this.row.getString(fkeyColumnName));

                ServerManager.getExecutor().execute(query, new QueryExecutor.BaseCallback<SQLDataSet>() {
                    @Override
                    public SQLDataSet onResultAsync(ResultSet result) {
                        return SQLDataSet.from(result);
                    }

                    @Override
                    public void onResultSync(SQLDataSet result) {
                        view.removeView(loadingBar);
                        tableLayout.setData(result);
                        tableLayout.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                view = (LinearLayout) inflater.inflate(R.layout.row_inspector_column_container, columnsContainer, false);

                ((TextView) view.findViewById(R.id.column_value)).setText(row.getString(name));
            }

            ((TextView) view.findViewById(R.id.column_name)).setText(name);
            ((TextView) view.findViewById(R.id.column_type)).setText(type);

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
            notice.setTextColor(getResources().getColor(R.color.grey_400, null));
            this.pkeysContainer.addView(notice);

            return;
        }

        int mIndex = 0;
        for (Key primaryKey : table.getPrimaryKeys()) {
            LinearLayout row = (LinearLayout) inflater.inflate(R.layout.row_inspector_key_container, pkeysContainer, false);

            ((TextView) row.findViewById(R.id.key_name)).setText(primaryKey.getName());
            ((TextView) row.findViewById(R.id.key_column)).setText(primaryKey.getColumn());

            this.pkeysContainer.addView(row);

            if (mIndex++ % 2 == 0) row.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark, null));
        }
    }
}
