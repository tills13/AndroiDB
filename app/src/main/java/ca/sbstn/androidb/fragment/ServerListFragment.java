package ca.sbstn.androidb.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.MainActivity;
import ca.sbstn.androidb.adapter.ServerListAdapter;
import ca.sbstn.androidb.sql.Server;
import io.realm.Realm;

public class ServerListFragment extends Fragment {
    private OnServerSelectedListener mListener;
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
        this.update();
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

        ((MainActivity) getActivity()).setToolbarTitle(getResources().getString(R.string.app_name));
        ((MainActivity) getActivity()).setToolbarColor(getResources().getColor(R.color.colorPrimary), true, true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.server_list, null);

        ListView listView = (ListView) view.findViewById(R.id.list);
        listView.setAdapter(this.adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener.onServerSelected((Server) adapter.getItem(i));
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                ServerListAdapter serverListAdapter = (ServerListAdapter) adapterView.getAdapter();
                Server server = (Server) serverListAdapter.getItem(i);

                CreateOrEditServerFragment createOrEditServerFragment = CreateOrEditServerFragment.newInstance(server);
                ((MainActivity) getActivity()).putDetailsFragment(createOrEditServerFragment, true);

                return true;
            }
        });

        listView.setEmptyView(view.findViewById(R.id.no_servers_warning));

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
                CreateOrEditServerFragment createOrEditServerFragment = CreateOrEditServerFragment.newInstance(null);
                ((MainActivity) getActivity()).putDetailsFragment(createOrEditServerFragment, true);
            }
        }

        return true;
    }

    public void update() {
        if (this.adapter == null) return;

        Realm realm = Realm.getDefaultInstance();
        List<Server> servers = realm.where(Server.class).findAll();
        this.adapter.setServers(servers);
        this.adapter.notifyDataSetChanged();
    }

    public interface OnServerSelectedListener {
        void onServerSelected(Server server);
    }
}
