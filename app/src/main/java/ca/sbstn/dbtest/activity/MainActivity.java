package ca.sbstn.dbtest.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.adapter.ServerListAdapter;
import ca.sbstn.dbtest.sql.Server;
import ca.sbstn.dbtest.util.Utils;


public class MainActivity extends Activity {
    public static String TAG = "MAINACTIVITY";

    private SharedPreferences sharedPreferences;
    private ListView serverList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servers);

        this.sharedPreferences = this.getSharedPreferences("AndroiDB", MODE_PRIVATE);
        this.serverList = (ListView) this.findViewById(R.id.server_list);

        this.registerClickListeners();

        ServerListAdapter adapter = new ServerListAdapter(this);
        this.serverList.setAdapter(adapter);

        List<Server> servers = this.loadServers();

        if (servers.size() == 0) {
            this.findViewById(R.id.no_servers_warning).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.no_servers_warning).setVisibility(View.GONE);
        }

        adapter.setServers(servers);
        adapter.notifyDataSetChanged();

        this.serverList.invalidate();
        this.serverList.requestLayout();
    }

    public void registerClickListeners() {
        this.serverList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Server server = (Server) ((ListView) parent).getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, ServerActivity.class);

                intent.putExtra("server", server);
                startActivity(intent);
            }
        });

        this.serverList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Server server = (Server) ((ListView) parent).getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, NewServerActivity.class);

                intent.putExtra("server", server);
                startActivity(intent);

                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        List<Server> servers = this.loadServers();
        ((ServerListAdapter) this.serverList.getAdapter()).setServers(servers);
        ((ServerListAdapter) this.serverList.getAdapter()).notifyDataSetChanged();
        this.serverList.requestLayout();

        if (servers.size() == 0) {
            this.findViewById(R.id.no_servers_warning).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.no_servers_warning).setVisibility(View.GONE);
        }
    }

    public List<Server> loadServers() {
        Map<String, ?> preferences = this.sharedPreferences.getAll();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_new) {
            Intent intent = new Intent(this, NewServerActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
