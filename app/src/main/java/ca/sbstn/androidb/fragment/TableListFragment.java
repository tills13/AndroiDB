package ca.sbstn.androidb.fragment;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.List;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.AndroiDB;
import ca.sbstn.androidb.adapter.TableListAdapter;
import ca.sbstn.androidb.callback.SQLExecuteCallback;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.SQLDataSet;
import ca.sbstn.androidb.sql.Table;
import ca.sbstn.androidb.task.FetchSchemasTask;
import ca.sbstn.androidb.task.FetchTablesTask;

public class TableListFragment extends Fragment {
    public static final String DATABASE_PARAM = "DATABASE";

    private Database database;
    private ListView listView;
    private TableListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private OnTableSelectedListener mListener;

    public TableListFragment() {}

    public static TableListFragment newInstance(Database database) {
        TableListFragment fragment = new TableListFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(DATABASE_PARAM, database);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);

        if (getArguments() != null) {
            this.database = (Database) getArguments().getSerializable(DATABASE_PARAM);

            this.adapter = new TableListAdapter(getActivity());
            this.refresh();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((AndroiDB) getActivity()).setToolbarTitle(this.database.getName());
        ((AndroiDB) getActivity()).setToolbarSubtitle(this.database.getServer().getName());
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

        try {
            this.mListener = (OnTableSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnTableSelectedListener");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case (R.id.action_edit): {
                CreateOrEditDatabaseFragment fragment = CreateOrEditDatabaseFragment.newInstance(this.database);

                ((AndroiDB) getActivity()).putDetailsFragment(fragment, true);
                break;
            }

            case R.id.action_query_runner: {
                QueryRunnerFragment queryRunnerFragment = QueryRunnerFragment.newInstance(this.database);
                ((AndroiDB) getActivity()).putDetailsFragment(queryRunnerFragment, true);

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

        this.listView = (ListView) view.findViewById(R.id.list);
        this.listView.setAdapter(this.adapter);

        this.listView.setEmptyView(view.findViewById(R.id.loading));

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TableListAdapter adapter = (TableListAdapter) adapterView.getAdapter();

                if (adapter.isHeader(i)) {
                    String schema = adapter.getHeader(i);
                    if (adapter.isLoaded(schema)) {
                        adapter.toggleCollapsed(schema);
                        adapter.notifyDataSetChanged();
                    } else {
                        FetchTablesTask fetchTablesTask = new FetchTablesTask(getActivity(), adapter).forSchema(schema);
                        fetchTablesTask.execute(database);
                    }
                } else {
                    mListener.onTableSelected(adapter.getItem(i));
                }
            }
        });

        this.listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                TableListAdapter adapter = ((TableListAdapter) ((ListView) absListView).getAdapter());
                if (adapter != null) {
                    String header = adapter.getHeader(firstVisibleItem);

                    ((AndroiDB) getActivity()).setToolbarSubtitle(firstVisibleItem > 0 ? header : "");
                }
            }
        });

        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        return view;
    }

    public void refresh() {
        FetchSchemasTask fetchSchemasTask = new FetchSchemasTask(new SQLExecuteCallback() {
            @Override
            public void onResult(List<SQLDataSet> results) {}

            @Override
            public void onSingleResult(SQLDataSet sqlResult) {
                List<String> schemas = new ArrayList<>();

                for (SQLDataSet.Row row : sqlResult) {
                    schemas.add(row.getString("name"));
                }

                adapter.setSchemas(schemas);
                adapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        fetchSchemasTask.execute(this.database);
    }

    public interface OnTableSelectedListener {
        public void onTableSelected(Table table);
    }
}
