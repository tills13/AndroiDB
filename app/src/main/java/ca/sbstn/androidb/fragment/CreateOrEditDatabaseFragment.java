package ca.sbstn.androidb.fragment;

import android.app.AlertDialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.activity.ServerActivity;
import ca.sbstn.androidb.callback.Callback;
import ca.sbstn.androidb.callback.SQLExecuteCallback;
import ca.sbstn.androidb.sql.Database;
import ca.sbstn.androidb.sql.SQLDataSet;
import ca.sbstn.androidb.task.ExecuteQueryTask;

/**
 * Created by tills13 on 2015-11-22.
 */
public class CreateOrEditDatabaseFragment extends Fragment {
    public static final String DATABASE_PARAM = "DATABASE";
    public static final int MODE_CREATE = 0;
    public static final int MODE_UPDATE = 1;

    private Database database;
    protected int mode;

    private String originalName;
    private String originalOwner;
    private String originalComment;

    private LinearLayout layout;
    private EditText nameField;
    private AutoCompleteTextView ownerField;
    private EditText commentField;

    public CreateOrEditDatabaseFragment() {}

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

        if (this.getArguments() != null) {
            this.database = (Database) this.getArguments().getSerializable(DATABASE_PARAM);

            if (this.database != null) {
                this.mode = MODE_UPDATE;
                this.originalName = this.database.getName();
                this.originalOwner = this.database.getOwner();
                this.originalComment = this.database.getComment() == null ? "" : this.database.getComment();

                ((BaseActivity) getActivity()).setToolbarTitle(this.originalName);
            } else {
                this.mode = MODE_CREATE;
                this.database = new Database();

                ((BaseActivity) getActivity()).setToolbarTitle("New Database");
            }

            this.fetchRoleAutocompleteList();
        }
    }

    public void saveDatabase() {
        if (this.mode == MODE_UPDATE) {
            String newName = this.nameField.getText().toString();
            String newOwner = this.ownerField.getText().toString();
            String newComment = this.commentField.getText().toString();

            String baseQuery = String.format("ALTER DATABASE %s", this.database.getName());
            String commentQuery = String.format("COMMENT ON DATABASE \"%s\" IS '%s'", this.database.getName(), newComment);

            ExecuteQueryTask executeQueryTask;
            if (!newOwner.equals(this.originalOwner)) {
                ProgressBar updateOwnerBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
                this.layout.addView(updateOwnerBar, this.layout.indexOfChild(this.nameField) + 1);

                ExecuteQueryTask updateOwnerTask = new ExecuteQueryTask(this.database, getContext(), new Callback<SQLDataSet>() {
                    @Override
                    public void onResult(SQLDataSet result) {
                        Snackbar.make(getView(), "Successfully updated owner", Snackbar.LENGTH_SHORT).show();
                    }
                });

                updateOwnerTask.setProgressBar(updateOwnerBar);
                updateOwnerTask.execute(baseQuery + String.format(" OWNER TO %s;", newOwner));
            }

            if (!newName.equals(this.originalName)) {
                // queries.add(query + String.format(" RENAME TO %s;", newName));
                // doesn't work, yet
            }

            if (!this.originalComment.equals(newComment)) {
                ProgressBar updateCommentBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
                this.layout.addView(updateCommentBar, this.layout.indexOfChild(this.commentField) + 1);

                ExecuteQueryTask updateCommentTask = new ExecuteQueryTask(this.database, getContext(), new Callback<SQLDataSet>() {
                    @Override
                    public void onResult(SQLDataSet result) {
                        Snackbar.make(getView(), "Successfully updated comment", Snackbar.LENGTH_SHORT).show();
                    }
                });

                updateCommentTask.setProgressBar(updateCommentBar);
                updateCommentTask.execute(commentQuery);
            }
        } else {
            //Server server = getActivity()
            //ExecuteQueryTask createDatabaseTask = new ExecuteQueryTask();
            String query = "BEGIN TRANSACTION; CREATE DATABASE %s WITH OWNER '%s'; COMMENT ON DATABASE \"%s\" IS '%s'; COMMIT;";
        }
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
        if (this.mode == MODE_CREATE) return;

        ExecuteQueryTask fetchRolesTask = new ExecuteQueryTask(this.database, getContext(), new Callback<SQLDataSet>() {
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
        });

        fetchRolesTask.execute("SELECT rolname FROM pg_roles;");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.layout = (LinearLayout) inflater.inflate(R.layout.edit_database, null);

        this.nameField = ((EditText) this.layout.findViewById(R.id.database_name));
        this.ownerField = ((AutoCompleteTextView) this.layout.findViewById(R.id.database_owner));
        this.commentField = ((EditText) this.layout.findViewById(R.id.database_comment));

        this.nameField.setText(this.database.getName());
        this.ownerField.setText(this.database.getOwner());
        this.commentField.setText(this.database.getComment());

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
        }

        return super.onOptionsItemSelected(item);
    }

    public interface OnDatabaseSavedInterface {}
}
