package ca.sbstn.androidb.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.MainActivity;
import ca.sbstn.androidb.application.AndroiDB;
import ca.sbstn.androidb.adapter.ServerListAdapter;
import ca.sbstn.androidb.sql.Server;

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.adapter = new ServerListAdapter(getActivity());
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
        ((MainActivity) getActivity()).setToolbarTitle(getResources().getString(R.string.app_name));
        ((MainActivity) getActivity()).setToolbarColor(getResources().getColor(R.color.colorPrimary), true, true);
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
                Server server = (Server) serverListAdapter.getItem(i);

                CreateOrEditServerFragment createOrEditServerFragment = CreateOrEditServerFragment.newInstance(server);
                ((MainActivity) getActivity()).putDetailsFragment(createOrEditServerFragment, true);

                return true;
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
                Fragment fragment = CreateOrEditServerFragment.newInstance(null);
                ((MainActivity) getActivity()).putDetailsFragment(fragment, true);
            }
        }

        return true;
    }

    public List<Server> loadServers() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(AndroiDB.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        Map<String, ?> preferences = sharedPreferences.getAll();
        List<Server> servers = new ArrayList<>();

        try {
            for (String key : preferences.keySet()) {
                if (key.startsWith(AndroiDB.SHARED_PREFS_SERVER_PREFIX)) {
                    Object something = preferences.get(key);
                    JSONObject mServer = new JSONObject(something.toString());
                    Server server = new Server(
                            mServer.getString("id"),
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
        }

        return servers;
    }

    public interface OnServerSelectedListener {
        public void onServerSelected(Server server);
    }
}
