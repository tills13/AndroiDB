package ca.sbstn.dbtest.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.Table;
import ca.sbstn.dbtest.task.ExecuteQueryTask;
import ca.sbstn.dbtest.view.SQLTableLayout;


public class ViewDataActivity extends Activity {
    private Table table;
    private Database database;

    private String query;

    private SQLTableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        Intent intent = this.getIntent();

        this.tableLayout = (SQLTableLayout) findViewById(R.id.table);

        if (intent.hasExtra("table")) {
            this.table = (Table) intent.getSerializableExtra("table");
            this.database = (Database) intent.getSerializableExtra("database");
        } else {
            this.query = intent.getStringExtra("query");
        }

        ActionBar ab = getActionBar();

        if (ab != null) {
            ab.setTitle("Results");
            ab.setSubtitle(this.table.getName());
        }

        this.init();
        this.populateTable();
    }

    public void init() {
        if (this.table != null) {
            ((Button) findViewById(R.id.next)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Table table = ViewDataActivity.this.table;
                    table.offset += table.limit;
                    ViewDataActivity.this.populateTable();
                }
            });

            ((Button) findViewById(R.id.previous)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Table table = ViewDataActivity.this.table;
                    table.offset -= table.limit;
                    ViewDataActivity.this.populateTable();
                }
            });
        } else {
            // hide bar
        }
    }

    public void populateTable() {
        ExecuteQueryTask fetchTablesTask = new ExecuteQueryTask(this, this.database, this.tableLayout);

        if (this.table != null) {
            fetchTablesTask.execute(String.format(table.getQuery()));
        } else {
            fetchTablesTask.execute(String.format(this.query));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_table, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
