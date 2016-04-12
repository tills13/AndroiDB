package ca.sbstn.androidb.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.AndroiDB;
import ca.sbstn.androidb.sql.Table;

/**
 * Created by tills13 on 2015-11-23.
 */
public class CreateOrEditTableFragment extends Fragment {
    public static final String PARAM_TABLE = "table";

    private Table table;
    private View view;

    public CreateOrEditTableFragment() {}

    public static CreateOrEditTableFragment newInstance(@Nullable Table table) {
        CreateOrEditTableFragment createOrEditTableFragment = new CreateOrEditTableFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_TABLE, table);

        createOrEditTableFragment.setArguments(bundle);
        return createOrEditTableFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);

        if (this.getArguments() != null) {
            this.table = (Table) this.getArguments().getSerializable(PARAM_TABLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.create_edit_server, null);

        if (this.table != null) {
            //((EditText) this.view.findViewById(R.id.server_name)).setText(this.server.getName());
            //((EditText) this.view.findViewById(R.id.server_host)).setText(this.server.getHost());
            //((EditText) this.view.findViewById(R.id.server_port)).setText(this.server.getPort() + "");
            //((EditText) this.view.findViewById(R.id.server_username)).setText(this.server.getUsername());
            //((EditText) this.view.findViewById(R.id.server_password)).setText(this.server.getPassword());

            //this.selectedColorIndex = Arrays.asList(Server.colors).indexOf(server.getColor());
        } else {
            //this.selectedColorIndex = 0;
        }

        ((Button) this.view.findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTable();
            }
        });

        return this.view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((AndroiDB) getActivity()).setToolbarTitle(this.table == null ? "New Table" : this.table.getName());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_create_edit_table, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_delete: {
                this.deleteTable();
                break;
            }

            case R.id.action_done: {
                if (this.saveTable()) {
                    getActivity().getSupportFragmentManager().popBackStack();
                    getActivity().getSupportFragmentManager().findFragmentById(R.id.context_fragment).onResume();
                }

                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean saveTable() {
        return true;
    }

    public void deleteTable() {
        new AlertDialog.Builder(getActivity())
            .setMessage(String.format("Are you sure you want to delete %s?", this.table.getName()))
            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getActivity().getSupportFragmentManager().popBackStack();
                    getActivity().getSupportFragmentManager().findFragmentById(R.id.context_fragment).onResume();
                }
            })
            .setNegativeButton("no", null)
            .create().show();
    }
}
