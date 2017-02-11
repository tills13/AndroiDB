package ca.sbstn.androidb.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.query.QueryExecutor;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.Query;
import ca.sbstn.androidb.sql.Server;
import io.realm.Realm;

public class CreateOrEditDatabaseFragment extends Fragment {
    public static final String TAG = "CorEDatabase";
    public static final String DATABASE_PARAM = "DATABASE";
    public static final String SERVER_PARAM_NAME = "SERVER_NAME";

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

    @BindView(R.id.container) protected LinearLayout container;
    @BindView(R.id.database_name) protected EditText nameField;
    @BindView(R.id.db_owner_container) protected TextInputLayout ownerContainer;
    @BindView(R.id.database_owner) protected AutoCompleteTextView ownerField;
    @BindView(R.id.database_comment) protected EditText commentField;
    @BindView(R.id.db_tablespace_container) protected TextInputLayout tablespaceContainer;
    @BindView(R.id.database_tablespace) protected AutoCompleteTextView tablespaceField;
    @BindView(R.id.db_template_container) protected TextInputLayout templateContainer;
    @BindView(R.id.database_template) protected AutoCompleteTextView templateField;

    protected QueryExecutor executor;
    protected Realm realm;

    public CreateOrEditDatabaseFragment() {
    }

    public static CreateOrEditDatabaseFragment newInstance(Server server, Database database) {
        CreateOrEditDatabaseFragment fragment = new CreateOrEditDatabaseFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(DATABASE_PARAM, database);
        bundle.putSerializable(SERVER_PARAM_NAME, server.getName());

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);
        this.realm = Realm.getDefaultInstance();

        if (this.getArguments() != null) {
            this.database = (Database) this.getArguments().getSerializable(DATABASE_PARAM);
            this.server = this.realm.where(Server.class).equalTo("name", this.getArguments().getString(SERVER_PARAM_NAME)).findFirst();
            this.executor = QueryExecutor.forServer(this.server);

            if (this.database != null) {
                this.mode = MODE_UPDATE;
                this.originalName = this.database.getName();
                this.originalOwner = this.database.getOwner();
                this.originalComment = this.database.getComment() == null ? "" : this.database.getComment();
                this.originalTableSpace = this.database.getTableSpace();

                ((BaseActivity) getActivity()).setToolbarTitle(this.originalName);
            } else {
                this.mode = MODE_CREATE;
                this.database = new Database("New Database", "postgres");

                ((BaseActivity) getActivity()).setToolbarTitle("New Database");
            }

            this.fetchRoleAutocompleteList();
            this.fetchTablespaceAutocompleteList();

            if (this.mode == MODE_CREATE) {
                this.fetchTemplateList();
            }
        }
    }

    public void saveDatabase() {
        final String newName = this.nameField.getText().toString();
        final String newOwner = this.ownerField.getText().toString();
        final String newComment = this.commentField.getText().toString();
        final String newTableSpace = this.tablespaceField.getText().toString();

        String template = this.templateField.getText().toString();

        if (template.equals("")) template = "DEFAULT";

        if (this.mode == MODE_UPDATE) {
            if (newName.equals(this.originalName) &&
                newTableSpace.equals(this.originalTableSpace)&&
                newOwner.equals(this.originalOwner) &&
                newComment.equals(this.originalComment)) {
                Snackbar.make(this.container, "Nothing to update", Snackbar.LENGTH_SHORT).show();
                return;
            }

            String query = "BEGIN TRANSACTION;";

            if (!newName.equals(this.originalName)) {
                query = query + String.format(
                    Locale.getDefault(),
                    "ALTER DATABASE \"%s\" RENAME TO \"%s\";",
                    this.database.getName(),
                    newName
                );

                this.database.setName(newName);
            }

            if (!newTableSpace.equals(this.originalTableSpace)) {
                query = query + String.format(
                    Locale.getDefault(),
                    "ALTER DATABASE \"%s\" SET TABLESPACE \"%s\";",
                    this.database.getName(),
                    newTableSpace
                );
            }

            if (!newOwner.equals(this.originalOwner)) {
                query = query + String.format(
                    Locale.getDefault(),
                    "ALTER DATABASE \"%s\" OWNER TO \"%s\";",
                    this.database.getName(),
                    newOwner
                );
            }

            if (!newComment.equals(this.originalComment)) {
                query = query + String.format(
                    "COMMENT ON DATABASE \"%s\" IS '%s';",
                    this.database.getName(),
                    newComment
                );
            }

            query = query + "COMMIT;";

            this.executor.execute(query, new QueryExecutor.BaseCallback<Void>() {
                @Override
                public boolean onError(Throwable thrown) {
                    Snackbar.make(
                        container,
                        String.format(
                            Locale.getDefault(),
                            "Something went wrong: %s",
                            thrown.getMessage()
                        ),
                        Snackbar.LENGTH_SHORT
                    ).show();

                    return false;
                }

                @Override
                public void onResultSync(Void result) {
                    Snackbar.make(
                        container,
                        String.format(
                            Locale.getDefault(),
                            "Successfully updated %s",
                            database.getName()
                        ),
                        Snackbar.LENGTH_SHORT
                    ).show();
                }
            });
        } else {
            this.database.setName(newName);
            this.database.setOwner(newOwner);
            this.database.setComment(newComment);
            this.database.setTableSpace(newTableSpace);

            /*ExecuteQueryTask createDatabaseTask = new ExecuteQueryTask(this.server, getContext(), new Callback<SQLDataSet>() {
                @Override
                public void onResult(SQLDataSet result) {
                    if (this.getTask().hasException()) {
                        Snackbar snackbar = Snackbar.make(layout, String.format(Locale.getDefault(), "Could not create database %s: %s", newName, this.getTask().getException().getMessage()), Snackbar.LENGTH_LONG);
                        ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.RED);
                        snackbar.show();
                    } else {
                        Snackbar.make(container, String.format(Locale.getDefault(), "Successfully created database %s", newName), Snackbar.LENGTH_SHORT).show();
            
                        if (database.getComment() != null && !database.getComment().equals("")) {
                            ExecuteQueryTask updateCommentTask = new ExecuteQueryTask(database, getContext(), null);
            
                            updateCommentTask.setExpectResults(false);
                            updateCommentTask.execute(String.format(Locale.getDefault(), "COMMENT ON DATABASE \"%s\" IS '%s';", database.getName(), database.getComment()));
                        }
            
                        mode = MODE_UPDATE;
                    }
                }
            });
            
            String query = "CREATE DATABASE \"%s\" WITH OWNER '%s' TABLESPACE = '%s' TEMPLATE \"%s\";";
            query = String.format(Locale.getDefault(), query, this.database.getName(), this.database.getOwner(), this.database.getTableSpace(), template);
            
            createDatabaseTask.setExpectResults(false);
            createDatabaseTask.execute(query);*/
        }
    }

    public void deleteDatabase() {
        if (this.mode == MODE_CREATE)
            return;

        View view = LayoutInflater.from(getContext()).inflate(R.layout.drop_entity_dialog, null);
        final EditText dialogInput = ((EditText) view.findViewById(R.id.dialog_input));

        ((TextView) view.findViewById(R.id.dialog_message)).setText("Enter \"DROP DATABASE\" below to confirm");
        dialogInput.setHint("DROP DATABASE");

        new AlertDialog.Builder(getContext()).setTitle("DROP DATABASE").setView(view)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        if (dialogInput.getText().toString().equals("DROP DATABASE")) {
                            /*ExecuteQueryTask deleteDatabaseTask = new ExecuteQueryTask(server, getContext(), new Callback<SQLDataSet>() {
                                @Override
                                public void onResult(SQLDataSet result) {
                                    if (getTask().hasException()) {
                                        Exception e = getTask().getException();
                                        String message = e.getMessage();
                            
                                        Snackbar snackbar = Snackbar.make(container, String.format(Locale.getDefault(), "Could not drop database %s: %s", database.getName(), message), Snackbar.LENGTH_SHORT);
                                        snackbar.show();
                                    } else {
                                        Snackbar snackbar = Snackbar.make(container, String.format(Locale.getDefault(), "Successfully dropped database %s", database.getName()), Snackbar.LENGTH_SHORT);
                                        snackbar.show();
                            
                                        finish();
                                    }
                                }
                            });
                            
                            deleteDatabaseTask.setExpectResults(false);
                            deleteDatabaseTask.execute(String.format(Locale.getDefault(), "DROP DATABASE \"%s\";", database.getName()));*/
                        } else {

                        }
                    }
                }).setNegativeButton("cancel", null).create().show();
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
        this.executor.execute("SELECT rolname AS role FROM pg_roles;", new QueryExecutor.BaseCallback<List<String>>() {
            @Override
            public List<String> onResultAsync(ResultSet result) {
                List<String> roles = new ArrayList<>();

                try {
                    result.beforeFirst();
                    while (result.next()) roles.add(result.getString("role"));
                } catch (SQLException exception) {
                    Log.d(TAG, String.format(Locale.getDefault(), "Error fetching roles: %s", exception.getMessage()));
                }

                return roles;
            }

            @Override
            public void onResultSync(List<String> result) {
                String mRoles[] = new String[result.size()];
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, result.toArray(mRoles));
                ownerField.setAdapter(adapter);
            }
        });
    }

    public void fetchTablespaceAutocompleteList() {
        this.executor.execute("SELECT spcname as tablespace FROM pg_tablespace;", new QueryExecutor.BaseCallback<List<String>>() {
            @Override
            public List<String> onResultAsync(ResultSet result) {
                List<String> tableSpaces = new ArrayList<>();

                try {
                    result.beforeFirst();
                    while (result.next()) tableSpaces.add(result.getString("tablespace"));
                } catch (SQLException exception) {
                    Log.d(TAG, String.format(Locale.getDefault(), "Error fetching tablespaces: %s", exception.getMessage()));
                }

                return tableSpaces;
            }

            @Override
            public void onResultSync(List<String> result) {
                String [] mTableSpaces = new String[result.size()];
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, result.toArray(mTableSpaces));

                if (tablespaceField.getText().toString().equals("")) {
                    tablespaceField.setText(mTableSpaces[0]);
                }

                tablespaceField.setAdapter(adapter);
            }
        });
    }

    public void fetchTemplateList() {
        this.executor.execute(
            "SELECT datname AS template FROM pg_database pgd WHERE pgd.datistemplate IS TRUE;",
            new QueryExecutor.BaseCallback<List<String>>() {
            @Override
            public List<String> onResultAsync(ResultSet result) {
                List<String> templates = new ArrayList<>();

                try {
                    result.beforeFirst();
                    while (result.next()) templates.add(result.getString("template"));
                } catch (SQLException exception) {
                    Log.d(TAG, String.format(Locale.getDefault(), "Error fetching templates: %s", exception.getMessage()));
                }

                return templates;
            }

            @Override
            public void onResultSync(List<String> result) {
                String [] mTemplates = new String[result.size()];
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    result.toArray(mTemplates)
                );

                templateField.setAdapter(adapter);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.layout = (ScrollView) inflater.inflate(R.layout.edit_database, container, false);
        ButterKnife.bind(this, this.layout);

        this.nameField.setText(this.database.getName());
        this.ownerField.setText(this.database.getOwner());
        this.commentField.setText(this.database.getComment());
        this.tablespaceField.setText(this.database.getTableSpace());

        if (this.mode != MODE_CREATE) {
            this.templateContainer.setEnabled(false);
            this.templateContainer.setHint("Only available when creating DBs");
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

    public interface OnDatabaseSavedInterface {
        void onDatabaseSaved(Database database);
    }
}
