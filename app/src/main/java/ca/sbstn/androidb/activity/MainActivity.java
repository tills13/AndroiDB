package ca.sbstn.androidb.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.fragment.ServerListFragment;
import ca.sbstn.androidb.query.ServerManager;
import ca.sbstn.androidb.sql.Server;

public class MainActivity extends BaseActivity implements ServerListFragment.OnServerSelectedListener {
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.layout_main);
    	super.onCreate(savedInstanceState);

        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setShowHideAnimationEnabled(true);
        }

        ServerListFragment serverListFragment = ServerListFragment.newInstance();
        this.putContextFragment(serverListFragment, false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ServerManager.setServer(null);
    }

    @Override
    public void onServerSelected(Server server) {
        Intent intent = new Intent(this, ServerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        ServerManager.setServer(server);

        startActivity(intent);
    }
}