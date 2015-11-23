package ca.sbstn.dbtest.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.List;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.callback.SQLExecuteCallback;
import ca.sbstn.dbtest.sql.SQLResult;
import ca.sbstn.dbtest.sql.Table;
import ca.sbstn.dbtest.task.ExecuteQueryTask;
import ca.sbstn.dbtest.task.FetchTableKeysTask;
import ca.sbstn.dbtest.view.SQLTableLayout;

public class TableActivity extends Activity {
    public Table table;
    public SQLTableLayout tableLayout;
    public Button nextButton;
    public Button previousButton;

    public ExecuteQueryTask executeQueryTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_activity);

        Intent intent = this.getIntent();
        this.table = (Table) intent.getSerializableExtra("table");
        this.tableLayout = (SQLTableLayout) this.findViewById(R.id.data_view);

        ActionBar ab = this.getActionBar();
        if (ab != null) {
            ab.setTitle(this.table.getName());
            ab.setSubtitle(this.table.getSchema());
        }

        FetchTableKeysTask tableKeysTask = new FetchTableKeysTask(this);
        tableKeysTask.execute(this.table);

        executeQueryTask = new ExecuteQueryTask(this, this.table.getDatabase(), this.table, this.tableLayout);
        executeQueryTask.setCallback(new SetRowCountCallback(ab));
        executeQueryTask.execute(this.table.getQuery());

        this.nextButton = (Button) this.findViewById(R.id.next);
        this.previousButton = (Button) this.findViewById(R.id.previous);

        this.setUpListeners();
    }

    public void setUpListeners() {
        this.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SOMETHING", table.getSchema());
                table.next();

                if (executeQueryTask.getStatus() == AsyncTask.Status.RUNNING) {
                    executeQueryTask.cancel(true);
                }

                executeQueryTask = new ExecuteQueryTask(TableActivity.this, table.getDatabase(), table, tableLayout);
                executeQueryTask.execute(table.getQuery());
            }
        });

        this.previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                table.previous();

                if (executeQueryTask.getStatus() == AsyncTask.Status.RUNNING) {
                    executeQueryTask.cancel(true);
                }

                executeQueryTask = new ExecuteQueryTask(TableActivity.this, table.getDatabase(), table, tableLayout);
                executeQueryTask.execute(table.getQuery());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_table, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, EditTableActivity.class);
            intent.putExtra("table", this.table);

            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    class SetRowCountCallback extends SQLExecuteCallback {
        ActionBar actionBar;

        public SetRowCountCallback(ActionBar actionBar) {
            this.actionBar = actionBar;
        }

        @Override
        public void onResult(List<SQLResult> results) {
            //this.actionBar.setSubtitle(results.getRow(0).getString("count"));
        }

        @Override
        public void onSingleResult(SQLResult result) {

        }
    }
}
