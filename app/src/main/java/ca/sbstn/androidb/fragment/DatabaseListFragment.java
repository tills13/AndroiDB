package ca.sbstn.androidb.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.activity.ServerActivity;
import ca.sbstn.androidb.adapter.DatabaseListAdapter;
import ca.sbstn.androidb.query.QueryExecutor;
import ca.sbstn.androidb.query.ServerManager;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.util.Colours;
import io.realm.Realm;

public class DatabaseListFragment extends Fragment {
    public static final String TAG = "DatabaseListFragment";
    public static final String SERVER_PARAM_NAME = "PARAM_NAME";

    private Server server;

    private View layout;

    @BindView(R.id.list) protected ListView listView;
    @BindView(R.id.swipe_container) protected SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.connection_info_container) protected LinearLayout connectionInfo;

    private DatabaseListAdapter adapter;

    protected OnDatabaseSelectedListener mListener;

    public DatabaseListFragment() {}

    public static DatabaseListFragment newInstance(Server server) {
        DatabaseListFragment fragment = new DatabaseListFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(SERVER_PARAM_NAME, server.getName());

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

        this.server = ServerManager.getServer();
        this.adapter = new DatabaseListAdapter(getActivity());
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
                CreateOrEditServerFragment createOrEditServerFragment = CreateOrEditServerFragment.newInstance();
                ((ServerActivity) getActivity()).putDetailsFragment(createOrEditServerFragment, true);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.mListener = (OnDatabaseSelectedListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.layout = inflater.inflate(R.layout.database_list, container, false);
        ButterKnife.bind(this, this.layout);

        this.swipeRefreshLayout = (SwipeRefreshLayout) this.layout.findViewById(R.id.swipe_container);

        int mColor = Color.parseColor(this.server.getColor());
        mColor = Colours.darken(mColor);

        this.connectionInfo.setBackgroundColor(mColor);

        ((TextView) this.layout.findViewById(R.id.connected_as_info)).setText(String.format("Connected as %s", this.server.getUsername()));

        this.listView = (ListView) this.layout.findViewById(R.id.list);
        this.listView.setAdapter(this.adapter);
        this.listView.setEmptyView(this.layout.findViewById(R.id.loading));

        this.listView.setOnItemClickListener((adapterView, view, index, id) ->
            mListener.onDatabaseSelected((Database) adapter.getItem(index))
        );

        this.listView.setOnItemLongClickListener((adapterView, view, index, id) -> {
            mListener.onDatabaseLongPressed((Database) adapter.getItem(index));
            return true;
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

        this.swipeRefreshLayout.setOnRefreshListener(() -> refresh(false));
        this.refresh(false);

        return this.layout;
    }

    public void refresh(final boolean openDefault) {
        this.server = ((ServerActivity) getActivity()).getServer(); // reload the server from prefs

        if (this.server == null) {
            getActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        ((BaseActivity) getActivity()).setToolbarTitle(this.server.getName());
        ((BaseActivity) getActivity()).setToolbarColor(this.server.getColor());

        this.connectionInfo.setBackgroundColor(Color.parseColor(this.server.getColor()));

        ((TextView) this.layout.findViewById(R.id.connected_as_info))
            .setText(String.format("Connected as %s", this.server.getUsername()));

        String query = this.getResources().getString(R.string.db_query_fetch_databases);

        QueryExecutor executor = QueryExecutor.forServer(this.server);

        executor.execute(query, new QueryExecutor.Callback<List<Database>>() {
             @Override
            public boolean onError(Throwable thrown) {
                new AlertDialog.Builder(getContext())
                    .setTitle("Whoops, something went wrong")
                    .setMessage(thrown.getMessage())
                    .setOnDismissListener((dialogInterface) -> getActivity().finish())
                    .create().show();

                return false;
            }

            @Override
            public List<Database> onResultAsync(ResultSet results) {
                List<Database> databases = new ArrayList<>();

                try {
                    while (results.next()) {
                        Database database = new Database(
                            results.getString("name"),
                            results.getString("owner"),
                            results.getString("comment"),
                            results.getString("tablespace_name"),
                            results.getBoolean("is_template")
                        );

                        databases.add(database);
                    }
                } catch (SQLException exception) {
                    Log.e(DatabaseListFragment.TAG, exception.getMessage());
                }

                return databases;
            }

            @Override
            public void onResultSync(List<Database> result) {
                adapter.setDatabases(result);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public interface OnDatabaseSelectedListener {
        void onDatabaseSelected(Database database);
        void onDatabaseLongPressed(Database database);
    }
}
