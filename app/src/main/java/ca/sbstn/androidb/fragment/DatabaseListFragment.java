package ca.sbstn.androidb.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.activity.ServerActivity;
import ca.sbstn.androidb.adapter.DatabaseListAdapter;
import ca.sbstn.androidb.query.QueryExecutor;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.util.Colours;
import io.realm.Realm;

public class DatabaseListFragment extends Fragment {
    public static final String TAG = "DatabaseListFragment";
    public static final String SERVER_PARAM_NAME = "PARAM_NAME";

    private Server server;

    private View internalView;

    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseListAdapter adapter;
    private LinearLayout connectionInfo;

    protected OnDatabaseSelectedListener mListener;
    protected Realm realm;

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
        this.realm = Realm.getDefaultInstance();

        if (getArguments() != null) {
            String serverName = getArguments().getString(SERVER_PARAM_NAME);

            this.server = this.realm.where(Server.class).equalTo("name", serverName).findFirst();
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

        this.mListener = (OnDatabaseSelectedListener) context;
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

        this.listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener.onDatabaseLongPressed((Database) adapter.getItem(i));
                return true;
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

        if (this.server == null) {
            getActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        ((BaseActivity) getActivity()).setToolbarTitle(this.server.getName());
        ((BaseActivity) getActivity()).setToolbarColor(Color.parseColor(this.server.getColor()));

        this.connectionInfo.setBackgroundColor(Color.parseColor(this.server.getColor()));

        ((TextView) this.internalView.findViewById(R.id.connected_as_info))
            .setText(String.format("Connected as %s", this.server.getUsername()));

        String query = this.getResources().getString(R.string.db_query_fetch_databases);

        QueryExecutor executor = QueryExecutor.forServer(this.server);

        executor.execute(query, new QueryExecutor.Callback<List<Database>>() {
            private Exception exception;

            @Override
            public List<Database> onError(Exception exception) {
                this.exception = exception;
                return new ArrayList<>();
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
                } catch (SQLException e) {
                    Log.e(DatabaseListFragment.TAG, e.getMessage());
                    this.exception = e;
                }

                return databases;
            }

            @Override
            public void onResultSync(List<Database> result) {
                if (this.exception != null) {
                    new AlertDialog.Builder(getContext())
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
