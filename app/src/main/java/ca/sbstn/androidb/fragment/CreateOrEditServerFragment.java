package ca.sbstn.androidb.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.query.QueryExecutor;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.util.Utils;
import io.realm.Realm;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;

public class CreateOrEditServerFragment extends Fragment {
    public static final String TAG = "COrEServerFragment";
    public static final String PARAM_SERVER_NAME = "SERVER_NAME";

    private View layout;
    private int selectedColorIndex;
    private Server server;

    protected GridLayout colorChooser;
    protected EditText nameEditText;
    protected EditText hostEditText;
    protected EditText portEditText;
    protected EditText defaultDatabaseEditText;
    protected EditText usernameEditText;
    protected EditText passwordEditText;

    protected Realm realm;

    public CreateOrEditServerFragment() {
    }

    public static CreateOrEditServerFragment newInstance(@Nullable Server server) {
        CreateOrEditServerFragment createOrEditServerFragment = new CreateOrEditServerFragment();

        if (server != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(PARAM_SERVER_NAME, server.getName());

            createOrEditServerFragment.setArguments(bundle);
        }

        return createOrEditServerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);
        this.realm = Realm.getDefaultInstance();

        if (this.getArguments() != null) {
            String serverName = this.getArguments().getString(PARAM_SERVER_NAME);
            this.server = this.realm.where(Server.class).equalTo("name", serverName).findFirst();
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.layout = inflater.inflate(R.layout.create_edit_server, null);

        this.colorChooser = (GridLayout) this.layout.findViewById(R.id.colors);
        this.nameEditText = (EditText) this.layout.findViewById(R.id.server_name);
        this.hostEditText = (EditText) this.layout.findViewById(R.id.server_host);
        this.portEditText = (EditText) this.layout.findViewById(R.id.server_port);
        this.defaultDatabaseEditText = (EditText) this.layout.findViewById(R.id.server_default_db);
        this.usernameEditText = (EditText) this.layout.findViewById(R.id.server_username);
        this.passwordEditText = (EditText) this.layout.findViewById(R.id.server_password);

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

        this.layout.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveServer(null);
            }
        });

        this.layout.findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testConnection();
            }
        });

        return this.layout;
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
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                    getActivity().getSupportFragmentManager().findFragmentById(R.id.context_fragment).onResume();
                }

                break;
            }

            case android.R.id.home: {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        if (this.server != null) {
            this.nameEditText.setText(this.server.getName());
            this.nameEditText.setEnabled(false);
            this.hostEditText.setText(this.server.getHost());
            this.portEditText.setText(String.format(Locale.ENGLISH, "%d", this.server.getPort()));
            this.defaultDatabaseEditText.setText(this.server.getDefaultDatabase());
            this.usernameEditText.setText(this.server.getUsername());
            this.passwordEditText.setText(this.server.getPassword());

            if (this.server.getColor() == null || this.server.getColor().equals("")) {
                this.selectedColorIndex = new Random().nextInt(Server.colors.length);
            } else {
                this.selectedColorIndex = Arrays.asList(Server.colors).indexOf(this.server.getColor());
            }
        } else {
            this.selectedColorIndex = new Random().nextInt(Server.colors.length);
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

        ((HorizontalScrollView) this.colorChooser.getParent())
                .smoothScrollTo((int) Utils.dpToPixels(getResources(), 48) * selectedColorIndex, 0);
    }

    public boolean saveServer(String message) {
        String name = this.nameEditText.getText().toString();
        String host = this.hostEditText.getText().toString();
        String mPort = this.portEditText.getText().toString();
        String defaultDatabase = this.defaultDatabaseEditText.getText().toString();
        String user = ((EditText) this.layout.findViewById(R.id.server_username)).getText().toString();
        String password = ((EditText) this.layout.findViewById(R.id.server_password)).getText().toString();
        String color = Server.colors[this.selectedColorIndex];

        if (name.equals("")) {
            Snackbar snackbar = Snackbar.make(this.layout, "Name cannot be blank", Snackbar.LENGTH_SHORT);
            snackbar.show();
            return false;
        }

        defaultDatabase = defaultDatabase.equals("") ? "postgres" : defaultDatabase;
        int port = mPort.equals("") ? 5432 : Integer.parseInt(mPort);
        user = user.equals("") ? "postgres" : user;

        this.realm.beginTransaction();

        if (this.server == null) {
            this.server = this.realm.createObject(Server.class, name);
        }

        this.server.setHost(host);
        this.server.setPort(port);
        this.server.setDefaultDatabase(defaultDatabase);
        this.server.setUsername(user);
        this.server.setPassword(password);
        this.server.setColor(color);

        this.realm.commitTransaction();

        message = message == null ?
            String.format(Locale.getDefault(), "Successfully saved %s", server.getName()) :
            message;

        Snackbar snackbar = Snackbar.make(this.layout, message, Snackbar.LENGTH_SHORT);
        snackbar.show();

        return true;
    }

    public void deleteServer() {
        new AlertDialog.Builder(getActivity())
            .setMessage(String.format("Are you sure you want to delete %s?", this.server.getName()))
            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    realm.beginTransaction();
                    server.deleteFromRealm();
                    realm.commitTransaction();

                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                    getActivity().getSupportFragmentManager().findFragmentById(R.id.context_fragment).onResume();
                }
            })
            .setNegativeButton("no", null)
            .create().show();
    }

    public void cloneServer() {
        if (this.server == null) {
            return;
        }

        new AlertDialog.Builder(getContext())
            .setView(R.layout.clone_server_dialog)
            .setTitle("New server name")
            .setPositiveButton("continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EditText editText = ((EditText)((AlertDialog) dialogInterface).findViewById(R.id.new_server_name));
                    String name = editText.getText().toString();
                    selectedColorIndex = new Random().nextInt(Server.colors.length - 1);

                    realm.beginTransaction();

                    try {
                        Server newServer = realm.createObject(Server.class, name);

                        newServer.setHost(server.getHost());
                        newServer.setPort(server.getPort());
                        newServer.setDefaultDatabase(server.getDefaultDatabase());
                        newServer.setUsername(server.getUsername());
                        newServer.setPassword(server.getPassword());
                        newServer.setColor(Server.colors[selectedColorIndex]);

                        realm.commitTransaction();

                        saveServer(String.format(Locale.getDefault(), "Successfully cloned %s into %s", server.getName(), name));

                        server = newServer;
                    } catch (RealmPrimaryKeyConstraintException e) {
                        String message = "A server by that name already exists...";
                        Snackbar snackbar = Snackbar.make(layout, message, Snackbar.LENGTH_SHORT);
                        snackbar.show();

                        realm.cancelTransaction();
                    } finally {
                        refresh();
                    }
                }
            })
            .setNegativeButton("cancel", null)
            .create().show();
    }

    public void testConnection() {
        final String host = this.hostEditText.getText().toString();
        final String port = this.portEditText.getText().toString();
        final String defaultDatabase = this.defaultDatabaseEditText.getText().toString();

        final String user = this.usernameEditText.getText().toString();
        final String password = this.passwordEditText.getText().toString();

        QueryExecutor executor = QueryExecutor.forServer(host, Integer.parseInt(port), defaultDatabase, user, password);
        executor.execute("", new TestConnectionCallback());
    }

    private class TestConnectionCallback implements QueryExecutor.Callback<Boolean> {
        private Exception exception;

        @Override
        public Boolean onError(Exception exception) {
            this.exception = exception;
            return false;
        }

        @Override
        public Boolean onResultAsync(ResultSet result) {
            return true;
        }

        @Override
        public void onResultSync(Boolean success) {
            String message = success ? "Successfully connected" : "Connection failed: " + this.exception.getMessage();

            (new AlertDialog.Builder(getActivity()))
                .setTitle(success ? "Success" : "Failed")
                .setMessage(message)
                .setPositiveButton("ok", null).show();
        }
    }
}
