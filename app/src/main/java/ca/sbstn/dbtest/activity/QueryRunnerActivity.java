package ca.sbstn.dbtest.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.sql.Database;

public class QueryRunnerActivity extends Activity {
    public EditText queryField;
    public Button executeQuery;
    public Button nextQuery;
    public Button previousQuery;

    private SharedPreferences prefs;
    private List<String> history;
    private int historyIndex = 0;

    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_runner);

        this.history = new ArrayList<>();
        this.prefs = this.getSharedPreferences("DB", MODE_PRIVATE);

        this.database = (Database) this.getIntent().getSerializableExtra("database");

        ActionBar ab = this.getActionBar();

        if (ab != null) {
            ab.setTitle(String.format("Querying %s", this.database.getName()));
        }

        history.addAll(this.prefs.getStringSet("query_history", new HashSet<String>()));

        queryField = (EditText) findViewById(R.id.query_field);
        executeQuery = (Button) findViewById(R.id.query_runner_run);
        nextQuery = (Button) findViewById(R.id.query_runner_next);
        previousQuery = (Button) findViewById(R.id.query_runner_previous);

        this.initialize();
    }

    public void initialize() {
        queryField.addTextChangedListener(new TextWatcher() {
            private boolean formatting = false;
            private int cursorIndex = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //this.cursorIndex = queryField.getSelectionStart();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!formatting) {
                    this.formatting = true;

                    String fieldValue = s.toString();

                    fieldValue = fieldValue.replaceAll("('.*?')", "<font color='green'>$1</font>")
                            .replaceAll("\\.([^ ]+)", "<font color='purple'>.$1</font>")
                            .replaceAll("(?i)(select|from|as|insert|where|join|natural|public|limit|offset|update|delete)", "<font color='blue'>$1</font>");

                    queryField.setText(Html.fromHtml(fieldValue));
                    queryField.setSelection(queryField.getText().length());

                    this.formatting = false;
                }
            }
        });

        executeQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                history.add(queryField.getText().toString());

                prefs.edit().putStringSet("query_history", new HashSet<>(history)).apply();
                Intent intent = new Intent(QueryRunnerActivity.this, ViewDataActivity.class);
                intent.putExtra("query", queryField.getText().toString());
                startActivity(intent);
            }
        });

        this.previousQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyIndex = (historyIndex == 0) ? 0 : --historyIndex;

                if (history.size() != 0) {
                    queryField.setText(history.get(historyIndex));
                }
            }
        });

        this.nextQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyIndex = (historyIndex == (history.size() - 1)) ? (history.size() - 1) : ++historyIndex;

                if (history.size() != 0) {
                    queryField.setText(history.get(historyIndex));
                }
            }
        });
    }

    public void runQuery() {
        //FetchDataTask task = new FetchDataTask(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_query_runner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
