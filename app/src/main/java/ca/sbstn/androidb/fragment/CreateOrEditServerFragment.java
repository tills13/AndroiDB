package ca.sbstn.androidb.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.application.AndroiDB;
import ca.sbstn.androidb.database.RealmUtils;
import ca.sbstn.androidb.entity.Server;
import ca.sbstn.androidb.util.Utils;
import io.realm.Realm;

/**
 * Created by tills13 on 2015-11-23.
 */
public class CreateOrEditServerFragment extends Fragment {
    public static final String PARAM_SERVER_ID = "SERVER_ID";

    private Server server;
    private View view;
    private int selectedColorIndex;

    protected GridLayout colorChooser;
    protected EditText nameEditText;
    protected EditText hostEditText;
    protected EditText portEditText;
    protected EditText usernameEditText;
    protected EditText passwordEditText;

    protected SharedPreferences sharedPreferences;
    protected Realm realm;

    public CreateOrEditServerFragment() {}

    public static CreateOrEditServerFragment newInstance(@Nullable Server server) {
        CreateOrEditServerFragment createOrEditServerFragment = new CreateOrEditServerFragment();

        if (server != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(PARAM_SERVER_ID, server.getId());

            createOrEditServerFragment.setArguments(bundle);
        }

        return createOrEditServerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);
        this.sharedPreferences = getActivity().getSharedPreferences(AndroiDB.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        this.realm = RealmUtils.getRealm(getContext());
        this.realm.beginTransaction();

        if (this.getArguments() != null) {
            int serverId = this.getArguments().getInt(PARAM_SERVER_ID);
            this.server = this.realm.where(Server.class).equalTo("id", serverId).findFirst();
        } else {
            int nextKey = this.realm.where(Server.class).max("id").intValue() + 1;
            this.server = new Server();
            this.server.setId(nextKey);
            this.server = this.realm.copyToRealm(this.server);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        this.refresh();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.realm.cancelTransaction();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.create_edit_server, null);

        this.colorChooser = (GridLayout) this.view.findViewById(R.id.colors);
        this.nameEditText = (EditText) this.view.findViewById(R.id.server_name);
        this.hostEditText = (EditText) this.view.findViewById(R.id.server_host);
        this.portEditText = (EditText) this.view.findViewById(R.id.server_port);
        this.usernameEditText = (EditText) this.view.findViewById(R.id.server_username);
        this.passwordEditText = (EditText) this.view.findViewById(R.id.server_password);

        this.nameEditText.addTextChangedListener(new TextWatcher() {
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
                saveServer(null);
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

        this.refresh();
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
                this.cloneServer();
                break;
            }

            case R.id.action_done: {
                if (this.saveServer(null)) {
                    this.realm.commitTransaction();

                    getActivity().getSupportFragmentManager().popBackStack();
                    getActivity().getSupportFragmentManager().findFragmentById(R.id.context_fragment).onResume();
                }

                break;
            }

            case android.R.id.home: {
                this.realm.cancelTransaction();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        this.nameEditText.setText(this.server.getName());
        this.hostEditText.setText(this.server.getHost());
        this.portEditText.setText(String.format(Locale.ENGLISH, "%d", this.server.getPort()));
        this.usernameEditText.setText(this.server.getUsername());
        this.passwordEditText.setText(this.server.getPassword());

        if (this.server.getColor() == null || this.server.getColor().equals("")) {
            this.selectedColorIndex = new Random().nextInt(Server.colors.length);
        } else {
            this.selectedColorIndex = Arrays.asList(Server.colors).indexOf(server.getColor());
        }

        this.refreshColorChooser();
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

        ((HorizontalScrollView) this.colorChooser.getParent()).smoothScrollTo((int) Utils.dpToPixels(getResources(), 48) * selectedColorIndex, 0);
    }

    public boolean saveServer(String message) {
        String name = ((EditText) this.view.findViewById(R.id.server_name)).getText().toString();
        String host = ((EditText) this.view.findViewById(R.id.server_host)).getText().toString();
        String mPort = ((EditText) this.view.findViewById(R.id.server_port)).getText().toString();
        String defaultDatabase = ((EditText) this.view.findViewById(R.id.server_default_db)).getText().toString();
        String user = ((EditText) this.view.findViewById(R.id.server_username)).getText().toString();
        String password = ((EditText) this.view.findViewById(R.id.server_password)).getText().toString();
        String color = Server.colors[this.selectedColorIndex];

        if (name.equals("")) {
            Snackbar snackbar = Snackbar.make(this.view, "Name cannot be blank", Snackbar.LENGTH_SHORT);
            snackbar.show();
            return false;
        }

        defaultDatabase = (defaultDatabase.equals("") ? "postgres" : defaultDatabase);
        int port = (mPort.equals("") ? 5432 : Integer.parseInt(mPort));
        user = user.equals("") ? "postgres" : user;

        this.server.setName(name);
        this.server.setHost(host);
        this.server.setPort(port);
        this.server.setDefaultDatabase(defaultDatabase);
        this.server.setUsername(user);
        this.server.setPassword(password);
        this.server.setColor(color);

        this.realm.commitTransaction();
        //this.realm.beginTransaction();

        message = message == null ? String.format(Locale.getDefault(), "Successfully saved %s", this.server.getName()) : message;
        Snackbar snackbar = Snackbar.make(this.view, message, Snackbar.LENGTH_SHORT);
        snackbar.show();

        return true;
    }

    public void deleteServer() {
        new AlertDialog.Builder(getActivity())
            .setMessage(String.format("Are you sure you want to delete %s?", this.server.getName()))
            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    server.deleteFromRealm();
                    realm.commitTransaction();

                    getActivity().getSupportFragmentManager().popBackStack();
                    getActivity().getSupportFragmentManager().findFragmentById(R.id.context_fragment).onResume();
                }
            })
            .setNegativeButton("no", null)
            .create().show();
    }

    public void cloneServer() {
        if (this.server == null) return;

        new AlertDialog.Builder(getContext())
            .setMessage("Are you sure you want to clone this server?")
            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //selectedColorIndex = new Random().nextInt(Server.colors.length - 1);

                    //String originalName = server.getName();

                    //server.setName("New Server");
                    //server.setColor(Server.colors[selectedColorIndex]);
                    //refresh();

                    //saveServer(String.format(Locale.getDefault(), "Successfully cloned %s", originalName));
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
}
