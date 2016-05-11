package ca.sbstn.androidb.fragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.callback.Callback;
import ca.sbstn.androidb.database.RealmUtils;
import ca.sbstn.androidb.entity.Server;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.SQLDataSet;
import ca.sbstn.androidb.task.ExecuteQueryTask;
import io.realm.Realm;

/**
 * Created by tills13 on 2015-11-22.
 */
public class CreateOrEditDatabaseFragment extends Fragment {
    public static final String DATABASE_PARAM = "DATABASE";
    public static final String SERVER_PARAM_ID = "SERVER_ID";
    public static final int MODE_CREATE = 0;
    public static final int MODE_UPDATE = 1;

    protected Server server;
    protected Database database;
    protected int mode;

    private String originalName;
    private String originalOwner;
    private String originalComment;
    protected String originalTableSpace;

    protected ScrollView layout;
    protected LinearLayout container;
    protected EditText nameField;
    protected AutoCompleteTextView ownerField;
    protected EditText commentField;
    protected AutoCompleteTextView tableSpaceField;
    protected AutoCompleteTextView templateField;

    protected Realm realm;

    public CreateOrEditDatabaseFragment() {}

    public static CreateOrEditDatabaseFragment newInstance(Server server) {
        CreateOrEditDatabaseFragment fragment = new CreateOrEditDatabaseFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(SERVER_PARAM_ID, server.getId());

        fragment.setArguments(bundle);
        return fragment;
    }

    public static CreateOrEditDatabaseFragment newInstance(Database database) {
        CreateOrEditDatabaseFragment fragment = new CreateOrEditDatabaseFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(DATABASE_PARAM, database);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);
        this.realm = RealmUtils.getRealm(getContext());

        if (this.getArguments() != null) {
            this.database = (Database) this.getArguments().getSerializable(DATABASE_PARAM);

            if (this.database != null) {
                this.mode = MODE_UPDATE;
                this.server = this.database.getServer();
                this.originalName = this.database.getName();
                this.originalOwner = this.database.getOwner();
                this.originalComment = this.database.getComment() == null ? "" : this.database.getComment();
                this.originalTableSpace = this.database.getTableSpace();

                ((BaseActivity) getActivity()).setToolbarTitle(this.originalName);
            } else {
                this.mode = MODE_CREATE;
                int serverId = this.getArguments().getInt(SERVER_PARAM_ID);
                this.server = this.realm.where(Server.class).equalTo("id", serverId).findFirst();
                this.database = new Database(this.server, "New Database", "postgres");

                ((BaseActivity) getActivity()).setToolbarTitle("New Database");
            }

            this.fetchRoleAutocompleteList();
            this.fetchTablespaceAutocompleteList();

            if (this.mode == MODE_CREATE) this.fetchTemplateList();
        }
    }

    public void saveDatabase() {
        final String newName = this.nameField.getText().toString();
        final String newOwner = this.ownerField.getText().toString();
        final String newComment = this.commentField.getText().toString();
        final String newTableSpace = this.tableSpaceField.getText().toString();
        String template = this.templateField.getText().toString();

        if (this.mode == MODE_UPDATE) {
            if (newName.equals(this.originalName) &&
                newTableSpace.equals(this.originalTableSpace) &&
                newOwner.equals(this.originalOwner) &&
                newComment.equals(this.originalComment)) {
                return;
            }

            String query = "BEGIN TRANSACTION;";

            if (!newName.equals(this.originalName)) {
                query = query + String.format(Locale.getDefault(), "ALTER DATABASE \"%s\" RENAME TO \"%s\";", this.database.getName(), newName);
                this.database.setName(newName);
            }

            if (!newTableSpace.equals(this.originalTableSpace)) query = query + String.format(Locale.getDefault(), "ALTER DATABASE \"%s\" SET TABLESPACE \"%s\";", this.database.getName(), newTableSpace);
            if (!newOwner.equals(this.originalOwner)) query = query + String.format(Locale.getDefault(), "ALTER DATABASE \"%s\" OWNER TO \"%s\";",this.database.getName(), newOwner);
            if (!newComment.equals(this.originalComment)) query = query + String.format("COMMENT ON DATABASE \"%s\" IS '%s';", this.database.getName(), newComment);
            query = query + "COMMIT;";

            ExecuteQueryTask updateDatabaseTask = new ExecuteQueryTask(this.server, getContext(), new Callback<SQLDataSet>() {
                @Override
                public void onResult(SQLDataSet result) {
                    if (!this.getTask().hasException()) {
                        if (!newName.equals(originalName)) database.setName(newName);
                        if (!newTableSpace.equals(originalTableSpace)) database.setTableSpace(newTableSpace);
                        if (!newOwner.equals(originalOwner)) database.setOwner(newOwner);

                        Snackbar.make(getView(), String.format(Locale.getDefault(), "Successfully updated %s", database.getName()), Snackbar.LENGTH_SHORT).show();
                    } else {
                        Exception exception = this.getTask().getException();
                        Snackbar.make(getView(), String.format(Locale.getDefault(), "Something went wrong: %s", exception.getMessage()), Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            updateDatabaseTask.setExpectResults(false).execute(query);
        } else {
            this.database.setName(newName);
            this.database.setOwner(newOwner);
            this.database.setComment(newComment);
            this.database.setTableSpace(newTableSpace);

            ExecuteQueryTask createDatabaseTask = new ExecuteQueryTask(this.server, getContext(), new Callback<SQLDataSet>() {
                @Override
                public void onResult(SQLDataSet result) {
                    if (this.getTask().hasException()) {
                        Snackbar snackbar = Snackbar.make(getView(), String.format(Locale.getDefault(), "Could not create database %s: %s", newName, this.getTask().getException().getMessage()), Snackbar.LENGTH_LONG);
                        ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.RED);
                        snackbar.show();
                    } else {
                        Snackbar.make(getView(), String.format(Locale.getDefault(), "Successfully created database %s", newName), Snackbar.LENGTH_SHORT).show();

                        if (database.getComment() != "" && database.getComment() != null) {
                            ExecuteQueryTask updateCommentTask = new ExecuteQueryTask(database, getContext(), null);

                            updateCommentTask.setExpectResults(false);
                            updateCommentTask.execute(String.format(Locale.getDefault(), "COMMENT ON DATABASE \"%s\" IS '%s';", database.getName(), database.getComment()));
                        }

                        mode = MODE_UPDATE;
                    }
                }
            });

            String query = "CREATE DATABASE \"%s\" WITH OWNER '%s' TABLESPACE = '%s' TEMPLATE \"%s\";";
            query = String.format(Locale.getDefault(), query, this.database.getName(), this.database.getOwner(), this.database.getTableSpace(), template == null ? "DEFAULT" : template);

            createDatabaseTask.setExpectResults(false);
            createDatabaseTask.execute(query);
        }
    }

    public void deleteDatabase() {
        if (this.mode == MODE_CREATE) return;

        View view = LayoutInflater.from(getContext()).inflate(R.layout.drop_entity_dialog, null);
        final EditText dialogInput = ((EditText) view.findViewById(R.id.dialog_input));

        ((TextView) view.findViewById(R.id.dialog_message)).setText("Enter \"DROP DATABASE\" below to confirm");
        dialogInput.setHint("DROP DATABASE");

        new AlertDialog.Builder(getContext())
            .setTitle("DROP DATABASE")
            .setView(view)
            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    if (dialogInput.getText().toString().equals("DROP DATABASE")) {
                        ExecuteQueryTask deleteDatabaseTask = new ExecuteQueryTask(server, getContext(), new Callback<SQLDataSet>() {
                            @Override
                            public void onResult(SQLDataSet result) {
                                if (getTask().hasException()) {
                                    Exception e = getTask().getException();
                                    String message = e.getMessage();

                                    Snackbar snackbar = Snackbar.make(getView(), String.format(Locale.getDefault(), "Could not drop database %s: %s", database.getName(), message), Snackbar.LENGTH_SHORT);
                                    snackbar.show();
                                } else {
                                    Snackbar snackbar = Snackbar.make(getView(), String.format(Locale.getDefault(), "Successfully dropped database %s", database.getName()), Snackbar.LENGTH_SHORT);
                                    snackbar.show();

                                    finish();
                                }
                            }
                        });

                        deleteDatabaseTask.setExpectResults(false);
                        deleteDatabaseTask.execute(String.format(Locale.getDefault(), "DROP DATABASE \"%s\";", database.getName()));
                    } else {

                    }
                }
            }).setNegativeButton("cancel", null)
            .create().show();
    }

    public void finish() {
        this.getActivity().getFragmentManager().popBackStack();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.getActivity().invalidateOptionsMenu();
    }

    public void fetchRoleAutocompleteList() {
        Callback<SQLDataSet> callback = new Callback<SQLDataSet>() {
            @Override
            public void onResult(SQLDataSet result) {
                List<String> roles = new ArrayList<>();

                for (SQLDataSet.Row row : result) {
                    roles.add(row.getString("rolname"));
                }

                String [] mRoles = new String[roles.size()];
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, roles.toArray(mRoles));
                ownerField.setAdapter(adapter);
            }
        };

        ExecuteQueryTask fetchRolesTask = this.mode == MODE_CREATE ? new ExecuteQueryTask(this.server, getContext(), callback) :
                                                                     new ExecuteQueryTask(this.database, getContext(), callback);

        fetchRolesTask.execute("SELECT rolname FROM pg_roles;");
    }

    public void fetchTablespaceAutocompleteList() {
        Callback<SQLDataSet> callback = new Callback<SQLDataSet>() {
            @Override
            public void onResult(SQLDataSet result) {
                List<String> tableSpaces = new ArrayList<>();

                for (SQLDataSet.Row row : result) {
                    tableSpaces.add(row.getString("spcname"));
                }

                String [] mTableSpaces = new String[tableSpaces.size()];
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, tableSpaces.toArray(mTableSpaces));

                if (tableSpaceField.getText().toString().equals("")) {
                    tableSpaceField.setText(mTableSpaces[0]);
                }

                tableSpaceField.setAdapter(adapter);
            }
        };

        ExecuteQueryTask fetchRolesTask = this.mode == MODE_CREATE ? new ExecuteQueryTask(this.server, getContext(), callback) :
                                                                     new ExecuteQueryTask(this.database, getContext(), callback);

        fetchRolesTask.execute("SELECT * FROM pg_tablespace;");
    }

    public void fetchTemplateList() {
        Callback<SQLDataSet> callback = new Callback<SQLDataSet>() {
            @Override
            public void onResult(SQLDataSet result) {
                List<String> templateDatabases = new ArrayList<>();

                for (SQLDataSet.Row row : result) {
                    templateDatabases.add(row.getString("name"));
                }

                String [] mTemplateDatabases = new String[templateDatabases.size()];
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, templateDatabases.toArray(mTemplateDatabases));

                if (templateField.getText().toString().equals("")) {
                    templateField.setText(mTemplateDatabases[0]);
                }

                templateField.setAdapter(adapter);
            }
        };

        ExecuteQueryTask fetchRolesTask = new ExecuteQueryTask(this.server, getContext(), callback);
        fetchRolesTask.execute("SELECT datname AS name FROM pg_database pgd WHERE pgd.datistemplate IS TRUE;");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.layout = (ScrollView) inflater.inflate(R.layout.edit_database, null);
        this.container = (LinearLayout) this.layout.findViewById(R.id.container);

        this.nameField = ((EditText) this.layout.findViewById(R.id.database_name));
        this.ownerField = ((AutoCompleteTextView) this.layout.findViewById(R.id.database_owner));
        this.commentField = ((EditText) this.layout.findViewById(R.id.database_comment));
        this.tableSpaceField = ((AutoCompleteTextView) this.layout.findViewById(R.id.database_tablespace));
        this.templateField = ((AutoCompleteTextView) this.layout.findViewById(R.id.database_template));

        this.nameField.setText(this.database.getName());
        this.ownerField.setText(this.database.getOwner());
        this.commentField.setText(this.database.getComment());
        this.tableSpaceField.setText(this.database.getTableSpace());

        if (this.mode == MODE_CREATE) {
            this.nameField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ((BaseActivity) getActivity()).setToolbarTitle(editable.toString());
                }
            });
        } else {
            this.templateField.setEnabled(false);
            this.templateField.setHint("Only available when creating DBs");
        }

        return this.layout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.menu_create_edit_database, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case android.R.id.home: {
                this.finish();
                return true;
            }

            case R.id.action_save: {
                this.saveDatabase();
                break;
            }

            case R.id.action_delete: {
                this.deleteDatabase();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public interface OnDatabaseSavedInterface {}
}
