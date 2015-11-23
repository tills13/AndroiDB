package ca.sbstn.dbtest.fragment;


import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.activity.CreateOrEditServerActivity;
import ca.sbstn.dbtest.adapter.ServerListAdapter;
import ca.sbstn.dbtest.sql.Server;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ServerListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServerListFragment extends Fragment {
    private List<Server> servers;
    private OnServerSelectedListener mListener;
    private ListView listView;
    private ServerListAdapter adapter;

    public ServerListFragment() {}

    public static ServerListFragment newInstance() {
        ServerListFragment fragment = new ServerListFragment();

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String name = getActivity().getResources().getString(R.string.app_name);
        ActionBar ab = this.getActivity().getActionBar();

        if (ab != null) {
            ab.setTitle(name);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = new ServerListAdapter(getActivity());
        this.adapter.setServers(this.loadServers());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.setHasOptionsMenu(true);

        try {
            this.mListener = (OnServerSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnServerSelectedListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        this.adapter.setServers(this.loadServers());
        this.adapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.server_list, null);

        this.listView = (ListView) view.findViewById(R.id.list);
        this.listView.setAdapter(this.adapter);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener.onServerSelected((Server) adapter.getItem(i));
            }
        });

        this.listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                ServerListAdapter serverListAdapter = (ServerListAdapter) adapterView.getAdapter();

                Intent intent = new Intent(getActivity(), CreateOrEditServerActivity.class);
                intent.putExtra("server", (Server) serverListAdapter.getItem(i));

                startActivity(intent);
                return true; // handled
            }
        });

        this.listView.setEmptyView(view.findViewById(R.id.no_servers_warning));

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_servers, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_new: {
                Intent intent = new Intent(getActivity(), CreateOrEditServerActivity.class);
                startActivity(intent);
            }
        }

        return true;
    }

    public List<Server> loadServers() {
        Map<String, ?> preferences = this.getActivity().getSharedPreferences("AndroiDB", Context.MODE_PRIVATE).getAll();
        List<Server> servers = new ArrayList<>();

        try {
            for (String key : preferences.keySet()) {
                if (key.startsWith("db")) {
                    Object something = preferences.get(key);
                    JSONObject mServer = new JSONObject(something.toString());
                    Server server = new Server(
                            mServer.getString("name"),
                            mServer.getString("host"),
                            mServer.getInt("port"),
                            mServer.getString("user"),
                            mServer.getString("password"),
                            mServer.getString("db"),
                            mServer.getString("color")
                    );

                    servers.add(server);
                }
            }
        } catch (JSONException e) {
            Log.d("AS", e.getMessage());
            // show dialog
        }

        return servers;
    }

    public interface OnServerSelectedListener {
        public void onServerSelected(Server server);
    }

}
