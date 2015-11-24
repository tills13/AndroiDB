package ca.sbstn.dbtest.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.activity.AndroiDB;
import ca.sbstn.dbtest.adapter.TableListAdapter;
import ca.sbstn.dbtest.callback.SQLExecuteCallback;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.SQLResult;
import ca.sbstn.dbtest.sql.Table;
import ca.sbstn.dbtest.task.FetchSchemasTask;
import ca.sbstn.dbtest.task.FetchTablesTask;

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

            case (R.id.action_query_runner): {

            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.table_list, null);

        this.listView = (ListView) view.findViewById(R.id.list);
        this.listView.setAdapter(this.adapter);

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
            public void onResult(List<SQLResult> results) {}

            @Override
            public void onSingleResult(SQLResult sqlResult) {
                List<String> schemas = new ArrayList<>();

                for (SQLResult.Row row : sqlResult) {
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
