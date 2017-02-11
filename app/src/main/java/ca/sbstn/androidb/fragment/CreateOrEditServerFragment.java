package ca.sbstn.androidb.fragment;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import java.sql.ResultSet;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;

import ca.sbstn.androidb.query.QueryExecutor;
import ca.sbstn.androidb.query.ServerManager;
import ca.sbstn.androidb.sql.Server;
import ca.sbstn.androidb.view.ColorChooser;
import io.realm.Realm;

import io.realm.exceptions.RealmPrimaryKeyConstraintException;

public class CreateOrEditServerFragment extends Fragment implements ServerManager.OnServerChangedListener {
    private View layout;

    @BindView(R.id.color_chooser) protected ColorChooser colorChooser;
    @BindView(R.id.server_name) protected TextInputEditText nameEditText;
    @BindView(R.id.server_host) protected TextInputEditText hostEditText;
    @BindView(R.id.server_port) protected TextInputEditText portEditText;
    @BindView(R.id.server_default_db) protected TextInputEditText defaultDatabaseEditText;
    @BindView(R.id.server_username) protected TextInputEditText usernameEditText;
    @BindView(R.id.server_password) protected TextInputEditText passwordEditText;

    protected Server server;
    protected Realm realm;

    public CreateOrEditServerFragment() {
    }

    public static CreateOrEditServerFragment newInstance() {
        return new CreateOrEditServerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);
        this.realm = Realm.getDefaultInstance();
        this.server = ServerManager.getServer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.layout = inflater.inflate(R.layout.create_edit_server, container, false);
        ButterKnife.bind(this, this.layout);

        // TODO: 2/9/17 change actionbar title based on serverName EditText content

        Button saveButton = (Button) this.layout.findViewById(R.id.save_server);
        Button testConnectionButton = (Button) this.layout.findViewById(R.id.test_connection);

        saveButton.setOnClickListener((view) -> onSave(null));
        testConnectionButton.setOnClickListener((view) -> testConnection());

        this.colorChooser.setOnColorSelectedListener((color) -> {
            int mColor = Color.parseColor(color);
            ((BaseActivity) getActivity()).setToolbarColor(mColor, true, true);
        });

        return this.layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((BaseActivity) getActivity()).setToolbarTitle(this.server == null ? "New Server" : this.server.getName());
    }

    @Override
    public void onResume() {
        super.onResume();
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
                Server server = this.onSave(null);

                if (server != null) {
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                    // getActivity().getSupportFragmentManager().findFragmentById(R.id.context_fragment).onResume();
                }

                break;
            }

            case android.R.id.home: {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServerChanged(Server server) {
        this.server = server;
        refresh();
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

            this.colorChooser.setSelectedColor(this.server.getColor());
        } else {
            this.colorChooser.setSelectedColor(0);
        }
    }

    public Server onSave(String message) {
        Realm realm = Realm.getDefaultInstance();
        String primaryKey = this.nameEditText.getText().toString();

        if (primaryKey.equals("")) {
            Snackbar.make(this.layout, "CI server name cannot be blank", Snackbar.LENGTH_LONG).show();
            return null;
        }

        realm.beginTransaction();

        this.server = (this.server != null) ? this.server : realm.createObject(Server.class, primaryKey);

        this.server.setHost(this.hostEditText.getText().toString());
        this.server.setPort(this.portEditText.getText().toString());
        this.server.setDefaultDatabase(this.defaultDatabaseEditText.getText().toString());
        this.server.setUsername(this.usernameEditText.getText().toString());
        this.server.setPassword(this.passwordEditText.getText().toString());
        this.server.setColor(this.colorChooser.getSelectedColor());

        realm.commitTransaction();
        ServerManager.setServer(this.server);

        message = message == null ?
            String.format(Locale.getDefault(), "Successfully saved %s", this.server.getName()) :
            message;

        Snackbar.make(this.layout, message, Snackbar.LENGTH_LONG).show();

        return this.server;
    }

    public void deleteServer() {
        new AlertDialog.Builder(getActivity())
            .setMessage(String.format("Are you sure you want to delete %s?", this.server.getName()))
            .setPositiveButton("yes", (dialogInterface, index) -> {
                realm.beginTransaction();
                this.server.deleteFromRealm();
                ServerManager.setServer(null);
                realm.commitTransaction();

                getActivity().getSupportFragmentManager().popBackStackImmediate();
                getActivity().getSupportFragmentManager().findFragmentById(R.id.context_fragment).onResume();
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
            .setPositiveButton("continue", (dialogInterface, index) -> {
                    EditText editText = ((EditText) ((AlertDialog) dialogInterface).findViewById(R.id.new_server_name));
                String name = editText.getText().toString();
                colorChooser.setSelectedColor(0);

                realm.beginTransaction();

                try {
                    Server newServer = realm.createObject(Server.class, name);

                    newServer.setHost(server.getHost());
                    newServer.setPort(server.getPort());
                    newServer.setDefaultDatabase(server.getDefaultDatabase());
                    newServer.setUsername(server.getUsername());
                    newServer.setPassword(server.getPassword());
                    newServer.setColor(colorChooser.getSelectedColor());

                    realm.commitTransaction();

                    newServer = onSave(String.format(Locale.getDefault(), "Successfully cloned %s into %s", server.getName(), name));

                    ServerManager.setServer(newServer);
                } catch (RealmPrimaryKeyConstraintException exception) {
                    String message = "A server by that name already exists...";
                    Snackbar snackbar = Snackbar.make(layout, message, Snackbar.LENGTH_SHORT);
                    snackbar.show();

                    realm.cancelTransaction();
                } finally {
                    refresh();
                }
            }).setNegativeButton("cancel", null).create().show();
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

    private class TestConnectionCallback extends QueryExecutor.BaseCallback<Void> {
        @Override
        public boolean onError(Throwable thrown) {
            String message = "Connection failed: " + thrown.getMessage();
            new AlertDialog.Builder(getActivity())
                .setTitle("Failed")
                .setMessage(message)
                .setPositiveButton("ok", null)
                .show();

            return false;
        }

        @Override
        public Void onResultAsync(ResultSet result) {
            String message = "Connection successful";

            new AlertDialog.Builder(getActivity())
                .setTitle("Success")
                .setMessage(message)
                .setPositiveButton("ok", null)
                .show();

            return null;
        }
    }
}
