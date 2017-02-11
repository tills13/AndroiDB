package ca.sbstn.androidb.fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.adapter.TableListAdapter;
import ca.sbstn.androidb.entity.Schema;
import ca.sbstn.androidb.query.QueryExecutor;
import ca.sbstn.androidb.query.ServerManager;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.sql.Table;
import io.realm.Realm;

public class TableListFragment extends Fragment implements ServerManager.OnDatabaseChangedListener  {
    public static final String TAG = "TableListFragment";
    public static final String PARAM_DATABASE = "DATABASE";
    public static final String PARAM_SERVER_NAME = "SERVER_NAME";

    private Database database;
    private Server server;

    private TableListAdapter adapter;
    @BindView(R.id.swipe_container) protected SwipeRefreshLayout swipeRefreshLayout;

    private OnTableSelectedListener mListener;

    public TableListFragment() {}

    public static TableListFragment newInstance() {
        return new TableListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);

        ServerManager.subscribe(this);
        this.server = ServerManager.getServer();
        this.database = ServerManager.getDatabase();

        this.adapter = new TableListAdapter(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        ServerManager.reloadDatabase(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ServerManager.unsubscribe(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.menu_database, menu);

        menu.findItem(R.id.action_show_indexes).setChecked(this.adapter.getShowIndexes());
        menu.findItem(R.id.action_show_tables).setChecked(this.adapter.getShowTables());
        menu.findItem(R.id.action_show_views).setChecked(this.adapter.getShowViews());
        menu.findItem(R.id.action_show_sequences).setChecked(this.adapter.getShowSequences());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.mListener = (OnTableSelectedListener) context;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case (R.id.action_edit): {
                CreateOrEditDatabaseFragment fragment = CreateOrEditDatabaseFragment.newInstance(this.server, this.database);

                ((BaseActivity) getActivity()).putDetailsFragment(fragment, true);
                break;
            }

            case R.id.action_query_runner: {
                QueryRunnerFragment queryRunnerFragment = QueryRunnerFragment.newInstance(this.database);
                ((BaseActivity) getActivity()).putDetailsFragment(queryRunnerFragment, true);

                break;
            }

            case R.id.action_show_tables: {
                boolean checked = item.isChecked();

                this.adapter.setShowTables(!checked);
                this.adapter.notifyDataSetChanged();

                item.setChecked(!checked);

                break;
            }

            case R.id.action_show_indexes: {
                boolean checked = item.isChecked();

                this.adapter.setShowIndexes(!checked);
                this.adapter.notifyDataSetChanged();

                item.setChecked(!checked);

                break;
            }

            case R.id.action_show_views: {
                boolean checked = item.isChecked();

                this.adapter.setShowViews(!checked);
                this.adapter.notifyDataSetChanged();

                item.setChecked(!checked);

                break;
            }

            case R.id.action_show_sequences: {
                boolean checked = item.isChecked();

                this.adapter.setShowSequences(!checked);
                this.adapter.notifyDataSetChanged();

                item.setChecked(!checked);

                break;
            }

            case R.id.action_drop_table: {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_text, null);
                TextView description = ((TextView) view.findViewById(R.id.description));

                final EditText editText = ((EditText) view.findViewById(R.id.edit_text));
                editText.setHint(getResources().getString(R.string.dialog_edit_text_hint_drop_table));

                new AlertDialog.Builder(getContext())
                    .setTitle(getResources().getString(R.string.dialog_title_drop_table))
                    .setView(view)
                    .setPositiveButton(getResources().getString(R.string.dialog_edit_text_hint_drop_table), (dialog, which) -> {
                        if (editText.getText().toString().toUpperCase().equals("DROP TABLE")) {

                        } else {

                        }
                    }).show();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.table_list, null);
        ButterKnife.bind(this, layout);

        ListView listView = (ListView) layout.findViewById(R.id.list);

        listView.setAdapter(this.adapter);
        listView.setEmptyView(layout.findViewById(R.id.loading));

        listView.setOnItemClickListener((adapterView, view, index, id) -> {

            final TableListAdapter adapter = (TableListAdapter) adapterView.getAdapter();

            if (adapter.isHeader(index)) {
                final String schema = adapter.getHeader(index);

                if (adapter.isLoaded(schema)) {
                    adapter.toggleCollapsed(schema);
                    adapter.notifyDataSetChanged();
                } else {
                    QueryExecutor executor = QueryExecutor.forServer(server, database);

                    executor.execute(new GetTablesResultsCallback(schema), new QueryExecutor.Callback<List<Table>>() {
                        @Override
                        public boolean onError(Throwable thrown) {
                            return false;
                        }

                        @Override
                        public List<Table> onResultAsync(ResultSet result) {
                            List<Table> tables = new ArrayList<>();

                            try {
                                while (result.next()) {
                                    tables.add(Table.from(result, database));
                                }
                            } catch (SQLException e) {
                                Log.e(TableListFragment.TAG, e.getMessage());
                            }

                            return tables;
                        }

                        @Override
                        public void onResultSync(List<Table> result) {
                            adapter.setItems(schema, result);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            } else {
                mListener.onTableSelected(adapter.getItem(index));
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                TableListAdapter adapter = ((TableListAdapter) absListView.getAdapter());
                if (adapter != null) {
                    String header = adapter.getHeader(firstVisibleItem);

                    ((BaseActivity) getActivity()).setToolbarSubtitle(firstVisibleItem > 0 ? header : "");
                }
            }
        });

        this.swipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipe_container);
        this.swipeRefreshLayout.setOnRefreshListener(this::refresh);

        return layout;
    }

    @Override
    public void onDatabaseChanged(Database database) {
        this.database = database;
        refresh();
    }

    public void refresh() {
        if (this.swipeRefreshLayout != null) this.swipeRefreshLayout.setRefreshing(true);

        String query = "SELECT " +
            "   oid, " +
            "   nspname AS name, " +
            "   nspname = ANY (current_schemas(true)) AS is_on_search_path, " +
            "   oid = pg_my_temp_schema() AS is_my_temp_schema, " +
            "   pg_is_other_temp_schema(oid) AS is_other_temp_schema " +
            "FROM pg_namespace";

        QueryExecutor executor = QueryExecutor.forServer(this.server);
        executor.execute(query, new QueryExecutor.BaseCallback<List<Schema>>() {
            @Override
            public List<Schema> onResultAsync(ResultSet result) {
                List<Schema> schemas = new ArrayList<>();

                try {
                    while (result.next()) {
                        Schema schema = new Schema(result.getString("name"), database);
                        schemas.add(schema);
                    }
                } catch (SQLException e) {
                    Log.e(TableListFragment.TAG, e.getMessage());
                }

                return schemas;
            }

            @Override
            public void onResultSync(List<Schema> result) {
                adapter.setSchemas(result);
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public interface OnTableSelectedListener {
        void onTableSelected(Table table);
    }

    protected static class GetTablesResultsCallback implements QueryExecutor.GetResultsCallback {
        private String schema;
        private boolean showAll = true;
        private final String[] simplifiedTableTypes = {"TABLE", "VIEW", "SYSTEM TABLE", "INDEX", "SEQUENCE"};
        private final String[] allTableTypes = {"TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM", "INDEX", "SEQUENCE"};

        GetTablesResultsCallback(String schema) {
            this.schema = schema;
        }

        @Override
        public ResultSet getResultSet(Connection connection) throws SQLException {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            return databaseMetaData.getTables(null, this.schema, null, (this.showAll ? this.allTableTypes : this.simplifiedTableTypes));
        }
    }
}
