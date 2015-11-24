package ca.sbstn.dbtest.fragment;

import android.app.AlertDialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.callback.SQLExecuteCallback;
import ca.sbstn.dbtest.sql.Database;
import ca.sbstn.dbtest.sql.SQLResult;
import ca.sbstn.dbtest.task.ExecuteQueryWithCallbackTask;

/**
 * Created by tills13 on 2015-11-22.
 */
public class CreateOrEditDatabaseFragment extends Fragment {
    public static final String DATABASE_PARAM = "database";

    private Database database;

    private String originalName;
    private String originalOwner;
    private String originalComment;

    private LinearLayout layout;
    private EditText nameField;
    private EditText ownerField;
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
                this.originalName = this.database.getName();
                this.originalOwner = this.database.getOwner();
                this.originalComment = this.database.getComment() == null ? "" : this.database.getComment();
            } else {
                finish();
            }
        }
    }

    public void saveDatabase() {
        ProgressBar loadingBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
        loadingBar.setIndeterminate(true);

        this.layout.addView(loadingBar, 0);
        this.layout.invalidate();

        String newName = this.nameField.getText().toString();
        String newOwner = this.ownerField.getText().toString();
        String newComment = this.commentField.getText().toString();

        String baseQuery = String.format("ALTER DATABASE %s", this.database.getName());
        String commentQuery = String.format("COMMENT ON DATABASE \"%s\" IS '%s'", this.database.getName(), newComment);

        List<String> queries = new ArrayList<>();

        if (!newOwner.equals(this.originalOwner)) {
            queries.add(baseQuery + String.format(" OWNER TO %s;", newOwner));
        }

        if (!newName.equals(this.originalName)) {
            // queries.add(query + String.format(" RENAME TO %s;", newName));
            // doesn't work, yet
        }

        if (!this.originalComment.equals(newComment)) {
            queries.add(commentQuery);
        }

        SQLExecuteCallback sqlExecuteCallback = new SQLExecuteCallback() {
            @Override
            public void onResult(List<SQLResult> results) {
                boolean errored = false;
                String error = "";

                for (SQLResult result : results) {
                    if (result.getError() != null) {
                        errored = true;
                        error = result.getError().getMessage();
                        break;
                    }
                }

                if (errored) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Query Failed")
                            .setMessage(error)
                            .setPositiveButton("ok", null)
                            .create()
                            .show();
                } else {
                    finish();
                }
            }

            @Override
            public void onSingleResult(SQLResult result) {
                boolean errored = false;
                String error = "";

                if (result.getError() != null) {
                    errored = true;
                    error = result.getError().getMessage();
                }

                if (errored) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Query Failed")
                            .setMessage(error)
                            .setPositiveButton("ok", null)
                            .create()
                            .show();
                } else {
                    finish();
                }
            }
        };

        ExecuteQueryWithCallbackTask executeQueryWithCallbackTask = new ExecuteQueryWithCallbackTask(getActivity(), this.database, sqlExecuteCallback);
        executeQueryWithCallbackTask.setUsePostgres(true);
        executeQueryWithCallbackTask.setProgressBar(loadingBar);

        String [] mQueries = new String[queries.size()];
        mQueries = queries.toArray(mQueries);

        executeQueryWithCallbackTask.execute(mQueries);
    }

    public void finish() {
        this.getActivity().getFragmentManager().popBackStack();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.getActivity().invalidateOptionsMenu();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.layout = (LinearLayout) inflater.inflate(R.layout.edit_database, null);

        this.nameField = ((EditText) this.layout.findViewById(R.id.database_name));
        this.ownerField = ((EditText) this.layout.findViewById(R.id.database_owner));
        this.commentField = ((EditText) this.layout.findViewById(R.id.database_comment));

        this.nameField.setText(this.database.getName());
        this.ownerField.setText(this.database.getOwner());
        this.commentField.setText(this.database.getComment());

        return this.layout;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.menu_edit_database, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_save: {
                this.saveDatabase();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public interface OnDatabaseSavedInterface {}
}
