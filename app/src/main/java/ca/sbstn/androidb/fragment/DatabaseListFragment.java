package ca.sbstn.androidb.fragment;


import android.content.Context;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.AndroiDB;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.activity.ServerActivity;
import ca.sbstn.androidb.adapter.DatabaseListAdapter;
import ca.sbstn.androidb.callback.SQLExecuteCallback;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.SQLDataSet;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.task.FetchDatabasesTask;
import ca.sbstn.androidb.util.Colours;

public class DatabaseListFragment extends Fragment {
    public static final String SERVER_PARAM = "SERVER";

    private Server server;

    private View internalView;

    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseListAdapter adapter;
    private LinearLayout connectionInfo;

    //private FetchDatabasesTask fetchDatabasesTask;

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);

        if (getArguments() != null) {
            this.server = (Server) getArguments().getSerializable(SERVER_PARAM);
            this.adapter = new DatabaseListAdapter(getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        this.refresh(false);
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
                ((ServerActivity) getActivity()).putDetailsFragment(createOrEditServerFragment, true);
                break;
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

        int mColor = Color.parseColor(this.server.getColor());
        mColor = Colours.darken(mColor);

        this.connectionInfo = (LinearLayout) this.internalView.findViewById(R.id.connection_info_container);
        this.connectionInfo.setBackgroundColor(mColor);

        ((TextView) this.internalView.findViewById(R.id.connected_as_info)).setText(String.format("Connected as %s", this.server.getUsername()));

        this.listView = (ListView) this.internalView.findViewById(R.id.list);
        this.listView.setAdapter(this.adapter);
        this.listView.setEmptyView(this.internalView.findViewById(R.id.loading));

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
                refresh(false);
            }
        });
        this.refresh(false);

        return this.internalView;
    }

    public void refresh(final boolean openDefault) {
        this.server = ((ServerActivity) getActivity()).getServer(); // reload the server from prefs
        this.swipeRefreshLayout.setRefreshing(true);

        if (this.server == null) {
            getActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        ((BaseActivity) getActivity()).setToolbarTitle(this.server.getName());
        ((BaseActivity) getActivity()).setToolbarColor(Color.parseColor(this.server.getColor()));

        FetchDatabasesTask fetchDatabasesTask = new FetchDatabasesTask(this.getActivity(), this.adapter);
        fetchDatabasesTask.setCallback(new SQLExecuteCallback() {
            @Override
            public void onResult(List<SQLDataSet> results) {}

            @Override
            public void onSingleResult(SQLDataSet result) {
                swipeRefreshLayout.setRefreshing(false);

                //if (result.getError() != null) {
                    /*new AlertDialog.Builder(getContext())
                            .setTitle("Warning")
                            .setMessage(result.getError().getMessage())
                            .create().show();*/
                //}

                if (server.getDefaultDatabase() != "" && openDefault) {
                    Database database = (Database) adapter.getByName(server.getDefaultDatabase());

                    if (database != null) {
                    //    mListener.onDatabaseSelected(database);
                    }
                }
            }
        });

        fetchDatabasesTask.execute(this.server);
    }

    public interface OnDatabaseSelectedListener {
        public void onDatabaseSelected(Database database);
    }
}
