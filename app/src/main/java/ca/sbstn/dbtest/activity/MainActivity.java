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
import ca.sbstn.dbtest.fragment.TableListFragment;
import ca.sbstn.dbtest.fragment.ViewDataFragment;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.SQLDataSet;
import ca.sbstn.dbtest.sql.Server;
import ca.sbstn.dbtest.sql.Table;
import ca.sbstn.dbtest.task.ExecuteQueryWithCallbackTask;
import ca.sbstn.dbtest.util.Colours;

public class MainActivity extends AppCompatActivity implements
	ServerListFragment.OnServerSelectedListener,
    DatabaseListFragment.OnDatabaseSelectedListener,
    TableListFragment.OnTableSelectedListener {

    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.layout_main);


    }
}