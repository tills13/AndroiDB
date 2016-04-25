package ca.sbstn.androidb.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.sql.Server;

/**
 * Created by tills13 on 15-06-26.
 */
public class ServerListAdapter extends BaseAdapter {
    public Context context;
    public Map<String, Server> servers;
    public String[] serverKeys;

    public ServerListAdapter(Context context) {
        super();

        this.context = context;
        this.servers = new HashMap<>();
        this.serverKeys = new String[this.servers.size()];
    }

    public void setServers(Map<String, Server> servers) {
        this.servers = servers;
        this.serverKeys = servers.keySet().toArray(this.serverKeys);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Server server = (Server) this.getItem(position);
        LayoutInflater inflater = LayoutInflater.from(this.context);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.server_item, parent, false);
        }

        convertView.findViewById(R.id.color_bar).setBackgroundColor(Color.parseColor(server.getColor()));

        String hostString = server.getHost() + ":" + server.getPort();

        ((TextView) convertView.findViewById(R.id.server_name)).setText(server.getName());
        ((TextView) convertView.findViewById(R.id.host_ip_port)).setText(hostString);
        ((TextView) convertView.findViewById(R.id.host_username)).setText(server.getUsername());

        if (position % 2 == 0) convertView.setBackgroundColor(Color.argb((int) Math.floor(0.05 * 255), 255, 255, 255));

        return convertView;
    }

    @Override
    public int getCount() {
        return this.servers.size();
    }

    @Override
    public Object getItem(int position) {
        return this.servers.get(this.serverKeys[position]);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
