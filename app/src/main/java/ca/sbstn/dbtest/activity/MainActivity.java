package ca.sbstn.dbtest.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.fragment.DatabaseListFragment;
import ca.sbstn.dbtest.fragment.ServerListFragment;
import ca.sbstn.dbtest.fragment.SplashScreenFragment;
import ca.sbstn.dbtest.fragment.TableListFragment;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.Server;


public class MainActivity extends Activity implements
        ServerListFragment.OnServerSelectedListener,
        DatabaseListFragment.OnDatabaseSelectedListener {
    public static String TAG = "MAINACTIVITY";

    private SharedPreferences sharedPreferences;
    private ListView serverList;

    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_pane);

        this.fm = this.getFragmentManager();

        this.fm.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .add(R.id.context_fragment, SplashScreenFragment.newInstance())
                .commit();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fm.beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.context_fragment, ServerListFragment.newInstance())
                        .commit();
            }
        }, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();

        this.fm.findFragmentById(R.id.context_fragment).onResume();
    }

    @Override
    public void onServerSelected(Server server) {
        this.invalidateOptionsMenu();

        if (this.findViewById(R.id.details_fragment) != null) {
            this.fm.beginTransaction().replace(R.id.details_fragment, DatabaseListFragment.newInstance(server)).commit();
        } else {
            DatabaseListFragment fragment = DatabaseListFragment.newInstance(server);
            this.fm.beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.context_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onDatabaseSelected(Database database) {
        Fragment details = this.fm.findFragmentById(R.id.context_fragment);
        TableListFragment fragment = TableListFragment.newInstance(database);
        this.fm.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.context_fragment, fragment)
                .addToBackStack(null)
                .commit();

        /*if (contextFragment != null) {
            this.fm.beginTransaction(
                    .remove(contextFragment)
                    .add()

        }*/
    }
}
