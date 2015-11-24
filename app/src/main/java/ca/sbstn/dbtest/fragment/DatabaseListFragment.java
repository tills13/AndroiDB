package ca.sbstn.dbtest.fragment;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.activity.AndroiDB;
import ca.sbstn.dbtest.adapter.DatabaseListAdapter;
import ca.sbstn.dbtest.callback.SQLExecuteCallback;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.SQLResult;
import ca.sbstn.dbtest.sql.Server;
import ca.sbstn.dbtest.task.FetchDatabasesTask;

public class DatabaseListFragment extends Fragment {
    public static final String SERVER_PARAM = "SERVER";

    private Server server;

    private View internalView;

    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseListAdapter adapter;

    private OnDatabaseSelectedListener mListener;

    public DatabaseListFragment() {}

    public static DatabaseListFragment newInstance(Server server) {
        DatabaseListFragment fragment = new DatabaseListFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(SERVER_PARAM, server);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((AndroiDB) getActivity()).setToolbarColor(this.server.getColor(), true, true);
        ((AndroiDB) getActivity()).setToolbarTitle(this.server.getName());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);

        if (getArguments() != null) {
            this.server = (Server) getArguments().getSerializable(SERVER_PARAM);
            this.adapter = new DatabaseListAdapter(getActivity());

            this.refresh();
        }
    }

    public void refresh() {
        FetchDatabasesTask fetchDatabasesTask = new FetchDatabasesTask(this.getActivity(), this.adapter);
        fetchDatabasesTask.setCallback(new SQLExecuteCallback() {
            @Override
            public void onResult(List<SQLResult> results) {}

            @Override
            public void onSingleResult(SQLResult result) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        fetchDatabasesTask.execute(this.server);
    }

    @Override
    public void onResume() {
        super.onResume();

        //((LinearLayout) ((TextView) this.internalView.findViewById(R.id.server_name)).getParent()).setBackgroundColor(Color.parseColor(this.server.getColor()));
        this.refresh();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_server, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_edit: {
                CreateOrEditServerFragment createOrEditServerFragment = CreateOrEditServerFragment.newInstance(this.server);

                ((AndroiDB) getActivity()).putDetailsFragment(createOrEditServerFragment, true);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            this.mListener = (OnDatabaseSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnDatabaseSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.internalView = inflater.inflate(R.layout.database_list, null);

        this.swipeRefreshLayout = (SwipeRefreshLayout) this.internalView.findViewById(R.id.swipe_container);

        ((LinearLayout) ((TextView) this.internalView.findViewById(R.id.server_name)).getParent()).setBackgroundColor(Color.parseColor(this.server.getColor()));
        ((TextView) this.internalView.findViewById(R.id.server_name)).setText(this.server.getName());
        ((TextView) this.internalView.findViewById(R.id.connected_as_info)).setText(String.format("Connected as %s", this.server.getUsername()));

        this.listView = (ListView) this.internalView.findViewById(R.id.list);
        this.listView.setAdapter(this.adapter);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener.onDatabaseSelected((Database) adapter.getItem(i));
            }
        });

        this.listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        return this.internalView;
    }

    public interface OnDatabaseSelectedListener {
        public void onDatabaseSelected(Database database);
    }
}