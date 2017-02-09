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

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.adapter.TableListAdapter;
import ca.sbstn.androidb.entity.Schema;
import ca.sbstn.androidb.query.QueryExecutor;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.sql.Table;
import io.realm.Realm;

public class TableListFragment extends Fragment {
    public static final String TAG = "TableListFragment";
    public static final String PARAM_DATABASE = "DATABASE";
    public static final String PARAM_SERVER_NAME = "SERVER_NAME";

    private Database database;
    private Server server;
    private TableListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private OnTableSelectedListener mListener;

    public TableListFragment() {}

    public static TableListFragment newInstance(Server server, String database) {
        TableListFragment fragment = new TableListFragment();

        Bundle bundle = new Bundle();
        bundle.putString(PARAM_SERVER_NAME, server.getName());
        bundle.putString(PARAM_DATABASE, database);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);

        if (getArguments() != null) {
            Realm realm = Realm.getDefaultInstance();

            String database = getArguments().getString(PARAM_DATABASE);
            String serverName = getArguments().getString(PARAM_SERVER_NAME);

            this.server = realm.where(Server.class).equalTo("name", serverName).findFirst();

            this.getDatabase(database);

            this.adapter = new TableListAdapter(getActivity());
        }
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
                CreateOrEditDatabaseFragment fragment = CreateOrEditDatabaseFragment.newInstance(this.database);

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
                ((TextView) view.findViewById(R.id.description)).setText(getResources().getString(R.string.dialog_desc_drop_table));

                final EditText editText = ((EditText) view.findViewById(R.id.edit_text));
                editText.setHint(getResources().getString(R.string.dialog_edit_text_hint_drop_table));

                new AlertDialog.Builder(getContext())
                        .setTitle(getResources().getString(R.string.dialog_title_drop_table))
                        .setView(view)
                        .setPositiveButton(getResources().getString(R.string.dialog_edit_text_hint_drop_table), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (editText.getText().toString().toUpperCase().equals("DROP TABLE")) {

                                } else {

                                }
                            }
                        }).show();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.table_list, null);

        ListView listView = (ListView) view.findViewById(R.id.list);
        listView.setAdapter(this.adapter);

        listView.setEmptyView(view.findViewById(R.id.loading));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final TableListAdapter adapter = (TableListAdapter) adapterView.getAdapter();

                if (adapter.isHeader(i)) {
                    final String schema = adapter.getHeader(i);

                    if (adapter.isLoaded(schema)) {
                        adapter.toggleCollapsed(schema);
                        adapter.notifyDataSetChanged();
                    } else {
                        QueryExecutor executor = QueryExecutor.forServer(server);

                        executor.execute(new QueryExecutor.GetResultsCallback() {
                            protected boolean showAll = true;
                            private final String[] simplifiedTableTypes = {"TABLE", "VIEW", "SYSTEM TABLE", "INDEX", "SEQUENCE"};
                            private final String[] allTableTypes = {"TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM", "INDEX", "SEQUENCE"};

                            @Override
                            public ResultSet getResultSet(Connection connection) throws SQLException {
                                DatabaseMetaData databaseMetaData = connection.getMetaData();
                                return databaseMetaData.getTables(null, schema, null, (this.showAll ? this.allTableTypes : this.simplifiedTableTypes));
                            }
                        }, new QueryExecutor.Callback<List<Table>>() {
                            @Override
                            public List<Table> onError(Exception exception) {
                                return new ArrayList<>();
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
                    mListener.onTableSelected(adapter.getItem(i));
                }
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

        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { refresh(); }
        });

        return view;
    }

    public void getDatabase(final String database) {
        String query = this.getResources().getString(R.string.db_query_fetch_database, database);

        QueryExecutor executor = QueryExecutor.forServer(this.server);

        executor.execute(query, new QueryExecutor.Callback<Database>() {
            private Exception exception;

            @Override
            public Database onError(Exception exception) {
                this.exception = exception;
                return null;
            }

            @Override
            public Database onResultAsync(ResultSet results) {
                try {
                    results.beforeFirst();
                    results.next();

                    return new Database(
                        results.getString("name"),
                        results.getString("owner"),
                        results.getString("comment"),
                        results.getString("tablespace_name"),
                        results.getBoolean("is_template")
                    );
                } catch (SQLException e) {
                    Log.e(TableListFragment.TAG, e.getMessage());
                    this.exception = e;
                }

                return null;
            }

            @Override
            public void onResultSync(Database result) {
                if (this.exception != null) {
                    new android.app.AlertDialog.Builder(getContext())
                        .setTitle("Whoops, something went wrong")
                        .setMessage(this.exception.getMessage())
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                getActivity().finish();
                            }
                        }).create().show();

                    return;
                }


                TableListFragment.this.database = result;
                refresh();
            }
        });
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
        executor.execute(query, new QueryExecutor.Callback<List<Schema>>() {
            @Override
            public List<Schema> onError(Exception exception) {
                return new ArrayList<>();
            }

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
}
