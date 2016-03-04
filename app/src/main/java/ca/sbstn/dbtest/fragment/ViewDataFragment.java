package ca.sbstn.dbtest.fragment;

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

import java.util.List;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.activity.AndroiDB;
import ca.sbstn.dbtest.callback.SQLExecuteCallback;
import ca.sbstn.dbtest.sql.SQLDataSet;
import ca.sbstn.dbtest.sql.Table;
import ca.sbstn.dbtest.task.ExecuteQueryWithCallbackTask;
import ca.sbstn.dbtest.view.SQLTableLayout;

/**
 * Created by tills13 on 2015-11-23.
 */
public class ViewDataFragment extends Fragment {
    public static final String PARAM_SQLRESULT = "sqlResult";

    private SQLDataSet sqlDataSet;
    private SQLTableLayout sqlTableLayout;

    private Button next;
    private Button previous;

    public ViewDataFragment() {}

    public static ViewDataFragment newInstance(SQLDataSet sqlDataSet) {
        ViewDataFragment viewDataFragment = new ViewDataFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_SQLRESULT, sqlDataSet);

        viewDataFragment.setArguments(bundle);

        return viewDataFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);

        if (this.getArguments() != null) {
            this.sqlDataSet = (SQLDataSet) this.getArguments().getSerializable(PARAM_SQLRESULT);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_table, menu);

        if (this.sqlDataSet.getTable() == null) {
            menu.removeItem(R.id.action_edit);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_edit: {
                CreateOrEditTableFragment createOrEditTableFragment = CreateOrEditTableFragment.newInstance(this.sqlDataSet.getTable());
                ((AndroiDB) getActivity()).putDetailsFragment(createOrEditTableFragment, true);

                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.data_view, null);

        this.sqlTableLayout = (SQLTableLayout) view.findViewById(R.id.sql_table_layout);
        this.sqlTableLayout.setData(sqlDataSet);
        this.sqlTableLayout.setOnRowSelectedListener(new SQLTableLayout.OnRowClickListener() {
            @Override
            public void onRowClicked(SQLDataSet.Row row) {
                RowInspectorFragment fragment = RowInspectorFragment.newInstance(row);
                ((AndroiDB) getActivity()).putDetailsFragment(fragment, true);
            }
        });

        this.sqlTableLayout.setOnHeaderClickListener(new SQLTableLayout.OnHeaderClickListener() {
            @Override
            public void onHeaderClicked(int index) {
                Table table = sqlDataSet.getTable();

                if (table.getOrderBy() == index) table.toggleOrderByDirection();
                else table.setOrderBy(index);

                ExecuteQueryWithCallbackTask executeQueryWithCallbackTask = new ExecuteQueryWithCallbackTask(getContext(), table, new SQLExecuteCallback() {
                    @Override
                    public void onResult(List<SQLDataSet> results) {}

                    @Override
                    public void onSingleResult(SQLDataSet result) {
                        sqlTableLayout.setData(result);
                    }
                });

                executeQueryWithCallbackTask.setExpectResult(true);
                executeQueryWithCallbackTask.execute(table.getQuery());
            }
        });

        if (this.sqlDataSet.getTable() != null) {
            this.next = ((Button) view.findViewById(R.id.data_next));
            this.previous  = ((Button) view.findViewById(R.id.data_previous));

            this.next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Table table = sqlDataSet.getTable();
                    table.next();

                    ExecuteQueryWithCallbackTask executeQueryWithCallbackTask = new ExecuteQueryWithCallbackTask(getContext(), table, new SQLExecuteCallback() {
                        @Override
                        public void onResult(List<SQLDataSet> results) {}

                        @Override
                        public void onSingleResult(SQLDataSet result) {
                            sqlTableLayout.setData(result);
                        }
                    });

                    executeQueryWithCallbackTask.setExpectResult(true);
                    executeQueryWithCallbackTask.execute(table.getQuery());
                }
            });

            this.previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Table table = sqlDataSet.getTable();
                    table.previous();

                    ExecuteQueryWithCallbackTask executeQueryWithCallbackTask = new ExecuteQueryWithCallbackTask(getContext(), table, new SQLExecuteCallback() {
                        @Override
                        public void onResult(List<SQLDataSet> results) {}

                        @Override
                        public void onSingleResult(SQLDataSet result) {
                            sqlTableLayout.setData(result);
                        }
                    });

                    executeQueryWithCallbackTask.setExpectResult(true);
                    executeQueryWithCallbackTask.execute(table.getQuery());
                }
            });
        } else {
            view.findViewById(R.id.paging_buttons).setVisibility(View.GONE);
        }

        return view;
    }
}
