package ca.sbstn.androidb.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.sql.Server;

public class ServerListAdapter extends BaseAdapter {
    private Context context;
    private List<Server> servers;

    public ServerListAdapter(Context context) {
        super();

        this.context = context;
        this.servers = new ArrayList<>();
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Server server = (Server) this.getItem(position);
        LayoutInflater inflater = LayoutInflater.from(this.context);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.server_item, parent, false);
        }

        String color = server.getColor();
        convertView.findViewById(R.id.color_bar).setBackgroundColor(Color.parseColor(color == null ? "#ffffff" : color));

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
        return this.servers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
