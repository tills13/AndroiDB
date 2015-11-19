package ca.sbstn.dbtest.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.sql.Server;


/**
 * A login screen that offers login via email/password.
 */
public class NewServerActivity extends Activity {
    private LinearLayout container;

    private EditText nameView;
    private EditText hostView;
    private EditText databaseView;
    private EditText portView;
    private EditText usernameView;
    private EditText passwordView;

    private GridLayout colorsSelector;

    private Server server;
    private ActionBar ab;

    private SharedPreferences prefs;

    private String[] colors = new String[] {
        "#E57373", // red_303
        "#64B5F6", // blue_300
        "#F06292",
        "#BA68C8",
        "#9575CD",
        "#7986CB",
        "#4FC3F7",
        "#4DD0E1",
        "#4DB6AC",
        "#81C784",
        "#AED581",
        "#FFB300",
        "#FF8A65"
    };

    private int selectedColorIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_server);

        this.prefs = this.getSharedPreferences("AndroiDB", MODE_PRIVATE);

        this.container = (LinearLayout) this.findViewById(R.id.new_server_container);
        this.ab = getActionBar();

        this.nameView = (EditText) findViewById(R.id.name);
        this.hostView = (EditText) findViewById(R.id.host);
        this.databaseView = (EditText) findViewById(R.id.default_db);
        this.portView = (EditText) findViewById(R.id.port);

        this.usernameView = (EditText) findViewById(R.id.username);
        this.passwordView = (EditText) findViewById(R.id.password);

        Button saveButton = (Button) findViewById(R.id.save);
        Button testButton = (Button) findViewById(R.id.test);

        this.colorsSelector = (GridLayout) findViewById(R.id.colors);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveServer()) finish();
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testConnection();
            }
        });

        if (this.getIntent().hasExtra("server")) {
            this.server = (Server) this.getIntent().getSerializableExtra("server");
            if (this.ab != null) this.ab.setTitle(server.getName());

            this.nameView.setText(server.getName());
            this.hostView.setText(server.getHost());
            this.databaseView.setText(server.getDefaultDatabase());
            this.portView.setText(server.getPort() + "");
            this.usernameView.setText(server.getUsername());
            this.passwordView.setText(server.getPassword());
            this.selectedColorIndex = Arrays.asList(this.colors).indexOf(server.getColor());
        } else {
            if (this.ab != null) ab.setTitle("New Server");
            this.selectedColorIndex = 0;
        }

        this.nameView.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (ab != null) {
                    String title = s.toString();
                    if (title.equals("")) ab.setTitle("New Server");
                    else ab.setTitle(s.toString());
                }
            }
        });

        this.refreshColorChooser();
    }

    public void refreshColorChooser() {
        this.colorsSelector.removeAllViews();

        ActionBar actionBar = this.getActionBar();

        if (actionBar != null) {
            int mColor = Color.parseColor(this.colors[this.selectedColorIndex]);
            actionBar.setBackgroundDrawable(new ColorDrawable(mColor));
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < this.colors.length; i++) {
            String color = colors[i];

            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.color_selection, null);

            if (i == this.selectedColorIndex) {
                layout.findViewById(R.id.icon).setVisibility(View.VISIBLE);
            }

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewGroup parent = (ViewGroup) v.getParent();
                    selectedColorIndex = parent.indexOfChild(v);

                    refreshColorChooser();
                }
            });

            layout.setBackgroundColor(Color.parseColor(color));
            this.colorsSelector.addView(layout);
        }
    }


    public boolean saveServer() {
        JSONObject mServer = new JSONObject();

        String name = this.nameView.getText().toString();
        String host = this.hostView.getText().toString();
        String mPort = this.portView.getText().toString();
        String defaultDatabase = this.databaseView.getText().toString();

        int port = (mPort.equals("") ? 5432 : Integer.parseInt(mPort));
        defaultDatabase = (defaultDatabase.equals("") ? "postgres" : defaultDatabase);

        String user = this.usernameView.getText().toString();
        String password = this.passwordView.getText().toString();

        user = user.equals("") ? "postgres" : user;

        String color = this.colors[this.selectedColorIndex];

        if (name.equals("")) return false;

        try {
            mServer.put("name", name)
                    .put("host", host)
                    .put("port", port)
                    .put("db", defaultDatabase)
                    .put("user", user)
                    .put("password", password)
                    .put("color", color);
        } catch (JSONException e) {
            return false;
        }

        String key = this.getPrefsKey(name);
        if (this.server == null && this.prefs.contains(key)) { // new server, name already taken

        } else {
            SharedPreferences.Editor editor = this.prefs.edit();
            if (this.server != null && !this.server.getName().equals(this.nameView.getText().toString())) {
                editor.remove(this.getPrefsKey(this.server.getName()));
            }

            editor.putString(key, mServer.toString()).commit();
        }

        return true;
    }

    public void testConnection() {
        final String host = this.hostView.getText().toString();
        final String mPort = this.portView.getText().toString();
        final String defaultDatabase = this.databaseView.getText().toString();

        final String user = this.usernameView.getText().toString();
        final String password = this.passwordView.getText().toString();

        final ProgressBar loadingBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        loadingBar.setIndeterminate(true);

        this.container.addView(loadingBar);

        AsyncTask<String, Void, Boolean> testConnectionTask = new AsyncTask<String, Void, Boolean>() {
            private Exception exception;

            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    Class.forName("org.postgresql.Driver").newInstance();

                    String url = String.format("jdbc:postgresql://%s:%s/%s", host, mPort, defaultDatabase.equals("") ? "postgres" : defaultDatabase);
                    Connection connection = DriverManager.getConnection(url, user, password);
                    connection.close();
                } catch (Exception e) {
                    this.exception = e;
                    return false;
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                container.removeView(loadingBar);

                String message = result ? "Successfully connected" : "Connection failed: " + this.exception.getMessage();


                (new AlertDialog.Builder(NewServerActivity.this)).setTitle(result ? "Success" : "Failed").setMessage(message).setPositiveButton("ok", null).show();
            }
        };

        testConnectionTask.execute("");
    }

    public String getPrefsKey(String name) {
        return "db_" + name.toLowerCase().replaceAll("[^\\w]", "");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_server, menu);

        if (this.server == null) menu.removeItem(R.id.delete_server);
        else {
            //int positionOfMenuItem = 0; // or whatever...
            //MenuItem menuItem = menu.findItem(R.id.delete_server);
           // SpannableString s = new SpannableString("delete");
            //s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
            //menuItem.setTitle(s);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete_server) {
            this.prefs.edit().remove("db_" + this.server.getName()).commit();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}



