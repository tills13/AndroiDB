package ca.sbstn.dbtest.activity;


import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.callback.SQLExecuteCallback;
import ca.sbstn.dbtest.fragment.DatabaseListFragment;
import ca.sbstn.dbtest.fragment.ServerListFragment;
import ca.sbstn.dbtest.fragment.SplashScreenFragment;
import ca.sbstn.dbtest.fragment.TableListFragment;
import ca.sbstn.dbtest.fragment.ViewDataFragment;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.SQLDataSet;
import ca.sbstn.dbtest.sql.Server;
import ca.sbstn.dbtest.sql.Table;
import ca.sbstn.dbtest.task.ExecuteQueryWithCallbackTask;
import ca.sbstn.dbtest.util.Colours;

public class AndroiDB extends AppCompatActivity implements
        ServerListFragment.OnServerSelectedListener,
        DatabaseListFragment.OnDatabaseSelectedListener,
        TableListFragment.OnTableSelectedListener {
    private static final String SHARED_PREFS_KEY = "AndroiDB_Prefs";
    public static final String SHARED_PREFS_SERVER_PREFIX = "AndroiDB_Server_";

    private Toolbar toolbar;
    private TabLayout tabs;

    private FragmentManager fragmentManager;
    private ValueAnimator actionbarAnimator;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!this.attemptInstantiateJDBCDrivers()) {
            finish();
        }

        setContentView(R.layout.layout_main);

        this.toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        this.tabs = (TabLayout) this.findViewById(R.id.tabs);

        this.setSupportActionBar(this.toolbar);

        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setShowHideAnimationEnabled(true);
        }

        this.fragmentManager = this.getSupportFragmentManager();
        this.fragmentManager.beginTransaction()
                .add(R.id.context_fragment, SplashScreenFragment.newInstance())
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .commit();

        this.sharedPreferences = this.getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);

        android.os.Handler handler = new android.os.Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ServerListFragment serverListFragment = ServerListFragment.newInstance();

                putContextFragment(serverListFragment, false);
            }
        }, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        this.tabs = (TabLayout) this.findViewById(R.id.tabs);

        this.setSupportActionBar(this.toolbar);

        Fragment fragment = this.fragmentManager.findFragmentById(R.id.context_fragment);
        fragment.onResume();
    }

    public AndroiDB setToolbarTitle(String title) {
        this.toolbar.setTitle(title);

        return this;
    }

    public AndroiDB setToolbarSubtitle(String subtitle) {
        this.toolbar.setSubtitle(subtitle);

        return this;
    }

    public AndroiDB setShowTabs(boolean showTabs) {
        if (showTabs) {
            this.tabs.setVisibility(View.VISIBLE);
        } else {
            this.tabs.setVisibility(View.GONE);
        }

        return this;
    }

    public AndroiDB setShowActionbar(boolean showActionbar) {
        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar == null) return this;

        if (showActionbar) {
            actionBar.show();
        } else {
            actionBar.hide();
        }

        return this;
    }

    public AndroiDB setToolbarColor(String color, boolean animate, boolean setStatusBar) {
        return this.setToolbarColor(Color.parseColor(color), animate, setStatusBar);
    }

    public AndroiDB setToolbarColor(int color) {
        return this.setToolbarColor(color, false);
    }

    public AndroiDB setToolbarColor(int color, boolean animate) {
        return this.setToolbarColor(color, animate, false);
    }

    public AndroiDB setToolbarColor(int color, boolean animate, final boolean setStatusBar) {
        if (animate) {
            Drawable toolbarBackground = this.toolbar.getBackground();
            int currentColor;

            if (toolbarBackground instanceof ColorDrawable) {
                currentColor = ((ColorDrawable) toolbarBackground).getColor();
            } else {
                this.toolbar.setBackgroundColor(color);
                return this;
            }

            if (this.actionbarAnimator != null && this.actionbarAnimator.isRunning()) {
                currentColor = (Integer) this.actionbarAnimator.getAnimatedValue();
                this.actionbarAnimator.cancel();
            }

            if (currentColor == color) return this;

            this.actionbarAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), currentColor, color);
            this.actionbarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    toolbar.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
                    if (setStatusBar) {
                        getWindow().setStatusBarColor(Colours.darken((Integer) valueAnimator.getAnimatedValue()));
                    }
                }
            });

            this.actionbarAnimator.setDuration(500);
            this.actionbarAnimator.setStartDelay(0);
            this.actionbarAnimator.start();
        } else {
            this.toolbar.setBackgroundColor(color);
        }

        return this;
    }

    public SharedPreferences getSharedPreferences() {
        // dunno why, but it happened
        if (this.sharedPreferences == null) {
            this.sharedPreferences = this.getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);
        }

        return this.sharedPreferences;
    }

    public void putContextFragment(Fragment fragment, boolean addToBackStack, int animIn, int animOut) {
        FragmentTransaction fragmentTransaction = this.fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.context_fragment, fragment);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.setCustomAnimations(animIn, animOut);
        fragmentTransaction.commit();
    }

    public void putContextFragment(Fragment fragment, boolean addToBackStack) {
        this.putContextFragment(fragment, addToBackStack, R.anim.slide_in, R.anim.slide_out);
    }

    public void putDetailsFragment(Fragment fragment, boolean replaceContext) {
        if (this.findViewById(R.id.details_fragment) == null) {
            this.putContextFragment(fragment, true);
        } else {
            Fragment currentDetails = this.fragmentManager.findFragmentById(R.id.details_fragment);

            if (replaceContext) {
                if (currentDetails != null) {
                    this.fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    this.fragmentManager.beginTransaction()
                            .remove(currentDetails)
                            .commit();

                    this.fragmentManager.executePendingTransactions();

                    this.fragmentManager.beginTransaction()
                            .add(R.id.context_fragment, currentDetails)
                            //.addToBackStack(null)
                            .commit();
                }
            }

            this.fragmentManager.beginTransaction()
                    .replace(R.id.details_fragment, fragment)
                    //.addToBackStack(null)
                    .commit();
        }
    }

    public Server getServer(String id) {
        try {
            String serverJsonString = this.getSharedPreferences().getString(SHARED_PREFS_SERVER_PREFIX + id, "");
            JSONObject serverJson = new JSONObject(serverJsonString);

            Server server = new Server(
                    serverJson.getString("id"),
                    serverJson.getString("name"),
                    serverJson.getString("host"),
                    serverJson.getInt("port"),
                    serverJson.getString("user"),
                    serverJson.getString("password"),
                    serverJson.getString("db"),
                    serverJson.getString("color")
            );

            return server;
        } catch (JSONException e) {
            return null;
        }
    }

    private boolean attemptInstantiateJDBCDrivers() {
        try {
            Class.forName("org.postgresql.Driver").newInstance();
        } catch (ClassNotFoundException|InstantiationException|IllegalAccessException e) {
            return false;
        }

        return true;
    }

    @Override
    public void onServerSelected(Server server) {
        this.invalidateOptionsMenu();

        DatabaseListFragment fragment = DatabaseListFragment.newInstance(server);
        if (this.findViewById(R.id.details_fragment) != null) {
            this.fragmentManager.beginTransaction().replace(R.id.details_fragment, fragment).commit();
            this.putDetailsFragment(fragment, true);
        } else {
            this.putContextFragment(fragment, true);
            /*this.fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.context_fragment, fragment)
                    .addToBackStack(null)
                    .commit();*/
        }
    }

    @Override
    public void onDatabaseSelected(Database database) {
        TableListFragment tableListFragment = TableListFragment.newInstance(database);
        this.putDetailsFragment(tableListFragment, true);
    }

    @Override
    public void onTableSelected(final Table table) {
        ExecuteQueryWithCallbackTask executeQueryWithCallbackTask = new ExecuteQueryWithCallbackTask(this, table, new SQLExecuteCallback() {
            @Override
            public void onResult(List<SQLDataSet> results) {}

            @Override
            public void onSingleResult(SQLDataSet result) {
                ViewDataFragment viewDataFragment = ViewDataFragment.newInstance(result);
                putDetailsFragment(viewDataFragment, true);
            }
        });

        executeQueryWithCallbackTask.setExpectResult(true);
        executeQueryWithCallbackTask.execute(table.getQuery());
    }
}




/*



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

        }
    }
}*/
