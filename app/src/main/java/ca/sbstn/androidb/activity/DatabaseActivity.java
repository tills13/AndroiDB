package ca.sbstn.androidb.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.callback.Callback;
import ca.sbstn.androidb.fragment.TableListFragment;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.SQLDataSet;
import ca.sbstn.androidb.sql.Table;
import ca.sbstn.androidb.task.ExecuteQueryTask;

/**
 * Created by tyler on 24/04/16.
 */
public class DatabaseActivity extends BaseActivity implements TableListFragment.OnTableSelectedListener{
    public static final String DATABASE_PARAM = "DATABASE";
    protected Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);

        this.database = (Database) getIntent().getSerializableExtra(DATABASE_PARAM);

        this.setToolbarTitle(this.database.getName());
        this.setToolbarSubtitle(this.database.getServer().getName());
        this.setToolbarColor(this.database.getServer().getColor(), true);

        TableListFragment tableListFragment = TableListFragment.newInstance(this.database);
        this.putContextFragment(tableListFragment, false);
    }

    public Database getDatabase() {
        return this.database;
    }

    @Override
    public void onTableSelected(Table table) {
        Intent intent = new Intent(this, ViewDataActivity.class);
        intent.putExtra(ViewDataActivity.TABLE_PARAM, table);
        startActivity(intent);
    }
}
