package ca.sbstn.androidb.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Random;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.application.AndroiDB;
import ca.sbstn.androidb.sql.Server;

/**
 * Created by tills13 on 2015-11-23.
 */
public class CreateOrEditServerFragment extends Fragment {
    public static final String PARAM_SERVER = "server";

    private Server server;
    private View view;
    private int selectedColorIndex;

    protected SharedPreferences sharedPreferences;

    private GridLayout colorChooser;

    public CreateOrEditServerFragment() {}

    public static CreateOrEditServerFragment newInstance(@Nullable Server server) {
        CreateOrEditServerFragment createOrEditServerFragment = new CreateOrEditServerFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_SERVER, server);

        createOrEditServerFragment.setArguments(bundle);
        return createOrEditServerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);
        this.sharedPreferences = getActivity().getSharedPreferences(AndroiDB.SHARED_PREFS_KEY, Context.MODE_PRIVATE);

        if (this.getArguments() != null) {
            this.server = (Server) this.getArguments().getSerializable(PARAM_SERVER);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        this.refreshColorChooser();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.create_edit_server, null);

        this.colorChooser = (GridLayout) this.view.findViewById(R.id.colors);

        if (this.server != null) {
            ((EditText) this.view.findViewById(R.id.server_name)).setText(this.server.getName());
            ((EditText) this.view.findViewById(R.id.server_host)).setText(this.server.getHost());
            ((EditText) this.view.findViewById(R.id.server_port)).setText(this.server.getPort() + "");
            ((EditText) this.view.findViewById(R.id.server_username)).setText(this.server.getUsername());
            ((EditText) this.view.findViewById(R.id.server_password)).setText(this.server.getPassword());

            this.selectedColorIndex = Arrays.asList(Server.colors).indexOf(server.getColor());
        } else {
            this.selectedColorIndex = 0;
        }

        ((EditText) this.view.findViewById(R.id.server_name)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                ((BaseActivity) getActivity()).setToolbarTitle(text);
            }
        });

        ((Button) this.view.findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveServer();
            }
        });

        ((Button) this.view.findViewById(R.id.test)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testConnection();
            }
        });

        return this.view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((BaseActivity) getActivity()).setToolbarTitle(this.server == null ? "New Server" : this.server.getName());

        this.refreshColorChooser();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_create_edit_server, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_delete: {
                this.deleteServer();
                break;
            }

            case R.id.action_clone: {
                if (this.server == null) return true;

                this.server.setId(this.generateId());
                this.saveServer();
                break;
            }

            case R.id.action_done: {
                if (this.saveServer()) {
                    getActivity().getSupportFragmentManager().popBackStack();
                    getActivity().getSupportFragmentManager().findFragmentById(R.id.context_fragment).onResume();
                }

                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshColorChooser() {
        this.colorChooser.removeAllViews();

        int mColor = Color.parseColor(Server.colors[this.selectedColorIndex]);
        ((BaseActivity) getActivity()).setToolbarColor(mColor, true, true);

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        for (int i = 0; i < Server.colors.length; i++) {
            String color = Server.colors[i];

            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.color_selection, null);
            layout.setBackgroundColor(Color.parseColor(color));

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

            this.colorChooser.addView(layout);
        }
    }

    public boolean saveServer() {
        JSONObject mServer = new JSONObject();

        String id = (this.server == null) ? null : this.server.getId();

        // TODO: 2015-11-26 MAKE SURE NO COLLISIONS
        while (id == null) {
            id = this.generateId();
        }

        String name = ((EditText) this.view.findViewById(R.id.server_name)).getText().toString();
        String host = ((EditText) this.view.findViewById(R.id.server_host)).getText().toString();
        String mPort = ((EditText) this.view.findViewById(R.id.server_port)).getText().toString();
        String defaultDatabase = ((EditText) this.view.findViewById(R.id.server_default_db)).getText().toString();

        int port = (mPort.equals("") ? 5432 : Integer.parseInt(mPort));
        defaultDatabase = (defaultDatabase.equals("") ? "postgres" : defaultDatabase);

        String user = ((EditText) this.view.findViewById(R.id.server_username)).getText().toString();
        String password = ((EditText) this.view.findViewById(R.id.server_password)).getText().toString();

        user = user.equals("") ? "postgres" : user;

        String color = Server.colors[this.selectedColorIndex];

        if (name.equals("")) return false;

        try {
            mServer.put("id", id)
                .put("name", name)
                .put("host", host)
                .put("port", port)
                .put("db", defaultDatabase)
                .put("user", user)
                .put("password", password)
                .put("color", color);
        } catch (JSONException e) {
            Log.d("EDITSERVER", e.getMessage());
            return false;
        }

        if (this.server == null) {
            this.server = new Server(id, name, host, port, user, password, defaultDatabase, color);
        } else {
            this.server.setName(name);
            this.server.setHost(host);
            this.server.setPort(port);
            this.server.setUsername(user);
            this.server.setPassword(password);
            this.server.setDefaultDatabase(defaultDatabase);
            this.server.setColor(color);
        }

        String prefsKeyForId = this.getPrefsKeyForId(id);


        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(prefsKeyForId, mServer.toString()).commit();

        return true;
    }

    public void deleteServer() {
        new AlertDialog.Builder(getActivity())
            .setMessage(String.format("Are you sure you want to delete %s?", this.server.getName()))
            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    sharedPreferences.edit().remove(getPrefsKeyForId(server.getId())).commit();
                    getActivity().getSupportFragmentManager().popBackStack();
                    getActivity().getSupportFragmentManager().findFragmentById(R.id.context_fragment).onResume();
                }
            })
            .setNegativeButton("no", null)
            .create().show();
    }

    public void testConnection() {
        final String host = ((EditText) this.view.findViewById(R.id.server_host)).getText().toString();
        final String mPort = ((EditText) this.view.findViewById(R.id.server_port)).getText().toString();
        final String defaultDatabase = ((EditText) this.view.findViewById(R.id.server_default_db)).getText().toString();

        final String user = ((EditText) this.view.findViewById(R.id.server_username)).getText().toString();
        final String password = ((EditText) this.view.findViewById(R.id.server_password)).getText().toString();

        AsyncTask<String, Void, Boolean> testConnectionTask = new AsyncTask<String, Void, Boolean>() {
            private Exception exception;

            @Override
            protected Boolean doInBackground(String... params) {
                try {
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

                String message = result ? "Successfully connected" : "Connection failed: " + this.exception.getMessage();

                (new AlertDialog.Builder(getActivity())).setTitle(result ? "Success" : "Failed").setMessage(message).setPositiveButton("ok", null).show();
            }
        };

        testConnectionTask.execute("");
    }

    private String generateId() {
        String id = "";
        String [] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

        for (int i = 0; i < 8; i++) {
            id = id + alphabet[new Random().nextInt(alphabet.length)];
        }

        return id;
    }

    private String getPrefsKeyForId(String id) {
        return AndroiDB.SHARED_PREFS_SERVER_PREFIX + id;
    }
}
