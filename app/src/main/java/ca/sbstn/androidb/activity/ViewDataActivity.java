package ca.sbstn.androidb.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.sql.SQLException;
import java.util.Locale;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.callback.Callback;
import ca.sbstn.androidb.fragment.ViewDataFragment;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.SQLDataSet;
import ca.sbstn.androidb.sql.Table;
import ca.sbstn.androidb.task.ExecuteQueryTask;

/**
 * Created by tyler on 24/04/16.
 */
public class ViewDataActivity extends BaseActivity {
    public static final String DATABASE_PARAM = "DATABASE";
    public static final String TABLE_PARAM = "TABLE";
    public static final String QUERY_PARAM = "QUERY";

    protected Table table;
    protected Database database;
    protected String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if (intent.hasExtra(TABLE_PARAM)) {
            this.table = (Table) intent.getSerializableExtra(TABLE_PARAM);
            this.database = this.table.getDatabase();

            this.setToolbarTitle(this.table.getName());
            this.setToolbarSubtitle(this.database.getName());
        } else if (intent.hasExtra(QUERY_PARAM)) {
            this.query = intent.getStringExtra(QUERY_PARAM);
            this.database = (Database) intent.getSerializableExtra(DATABASE_PARAM);

            this.toolbar.setTitle("Custom Query");
            this.toolbar.setSubtitle(this.query);
        } else {
            finish();
        }

        this.setToolbarColor(this.database.getServer().getColor(), true);
        this.init();
    }

    public void init() {
        String query = this.table == null ? this.query : this.table.getQuery();
        Database database = this.table == null ? this.database : this.table.getDatabase();

        ExecuteQueryTask executeQueryTask = new ExecuteQueryTask(database, this.table, this, new Callback<SQLDataSet>() {
            @Override
            public void onResult(SQLDataSet result) {
                if (this.getTask().hasException()) {
                    Exception exception = this.getTask().getException();

                    String sqlState = (exception instanceof SQLException) ? ((SQLException) exception).getSQLState() : "????";
                    String message = exception.getMessage();

                    new AlertDialog.Builder(ViewDataActivity.this)
                            .setTitle(String.format(Locale.getDefault(), "[%s] Error", sqlState))
                            .setMessage(message)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    finish();
                                }
                            })
                            .create().show();
                } else {
                    ViewDataFragment viewDataFragment = ViewDataFragment.newInstance(result);
                    putContextFragment(viewDataFragment, false);
                }
            }
        });

        executeQueryTask.setExpectResults(true);
        executeQueryTask.execute(query);
    }
}
