package ca.sbstn.androidb.activity;


import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.fragment.ServerListFragment;
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
    public void onServerSelected(Server server) {
        Intent intent = new Intent(this, ServerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.putExtra(ServerActivity.SERVER_PARAM, server);
        startActivity(intent);
    }
}