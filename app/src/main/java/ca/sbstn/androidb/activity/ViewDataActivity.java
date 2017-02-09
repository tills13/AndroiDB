package ca.sbstn.androidb.activity;

import android.content.Intent;
import android.os.Bundle;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.sql.Table;
import io.realm.Realm;

public class ViewDataActivity extends BaseActivity {
    public static final String PARAM_SERVER_NAME = "SERVER_NAME";
    public static final String PARAM_DATABASE = "DATABASE";
    public static final String PARAM_TABLE = "TABLE";
    public static final String PARAM_QUERY = "QUERY";

    protected Server server;
    protected Table table;
    protected Database database;
    protected String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.layout_main);
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Realm realm = Realm.getDefaultInstance();
        String name = intent.getStringExtra(PARAM_SERVER_NAME);

        this.server = realm.where(Server.class).equalTo("name", name).findFirst();

        if (intent.hasExtra(PARAM_TABLE)) {
            this.table = (Table) intent.getSerializableExtra(PARAM_TABLE);
            this.database = this.table.getDatabase();

            this.setToolbarTitle(this.table.getName());
            this.setToolbarSubtitle(this.database.getName());
        } else if (intent.hasExtra(PARAM_QUERY)) {
            this.query = intent.getStringExtra(PARAM_QUERY);
            this.database = (Database) intent.getSerializableExtra(PARAM_DATABASE);

            this.toolbar.setTitle("Custom Query");
            this.toolbar.setSubtitle(this.query);
        } else {
            finish();
        }

        this.setToolbarColor(this.server.getColor(), true);
        this.init();
    }

    public void init() {
        String query = this.table == null ? this.query : this.table.getQuery();
        Database database = this.table == null ? this.database : this.table.getDatabase();

        /*ExecuteQueryTask executeQueryTask = new ExecuteQueryTask(database, this.table, this, new Callback<SQLDataSet>() {
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
        executeQueryTask.execute(query);*/
    }
}
