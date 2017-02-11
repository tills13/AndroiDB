package ca.sbstn.androidb.fragment;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.sql.SQLDataSet;
import ca.sbstn.androidb.sql.Table;
import ca.sbstn.androidb.view.SQLTableLayout;

public class ViewDataFragment extends Fragment {
    public static final String PARAM_SQL_RESULT = "SQL_RESULT";

    private SQLDataSet sqlDataSet;

    @BindView(R.id.sql_table_layout) protected SQLTableLayout sqlTableLayout;
    @BindView(R.id.data_next) protected Button next;
    @BindView(R.id.data_previous) protected Button previous;

    public ViewDataFragment() {}

    public static ViewDataFragment newInstance(SQLDataSet sqlDataSet) {
        ViewDataFragment viewDataFragment = new ViewDataFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_SQL_RESULT, sqlDataSet);

        viewDataFragment.setArguments(bundle);

        return viewDataFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHasOptionsMenu(true);

        if (this.getArguments() != null) {
            this.sqlDataSet = (SQLDataSet) this.getArguments().getSerializable(PARAM_SQL_RESULT);
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
                ((BaseActivity) getActivity()).putDetailsFragment(createOrEditTableFragment, true);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.data_view, container, false);
        ButterKnife.bind(this, layout);

        this.sqlTableLayout.setData(sqlDataSet);
        this.sqlTableLayout.setOnRowSelectedListener((row) -> {
            RowInspectorFragment fragment = RowInspectorFragment.newInstance(row);
            ((BaseActivity) getActivity()).putDetailsFragment(fragment, true);
        });

        this.sqlTableLayout.setOnHeaderClickListener((index) -> {
            Table table = sqlDataSet.getTable();

            if (table.getOrderBy() == index) table.toggleOrderByDirection();
            else table.setOrderBy(index);

            /*ExecuteQueryTask executeQueryTask = new ExecuteQueryTask(table.getDatabase(), table, getContext(), new Callback<SQLDataSet>() {
                @Override
                public void onResult(SQLDataSet result) {
                    sqlTableLayout.setData(result);
                }
            });

            executeQueryTask.setExpectResults(true);
            executeQueryTask.execute(table.getQuery());*/

        });

        if (this.sqlDataSet.getTable() != null) {
            this.next = ((Button) layout.findViewById(R.id.data_next));
            this.previous  = ((Button) layout.findViewById(R.id.data_previous));

            this.next.setOnClickListener((view) -> {
                Table table = sqlDataSet.getTable();
                table.next();

                /*ExecuteQueryTask executeQueryTask = new ExecuteQueryTask(table.getDatabase(), table, getContext(), new Callback<SQLDataSet>() {
                    @Override
                    public void onResult(SQLDataSet result) {
                        sqlTableLayout.setData(result);
                    }
                });

                executeQueryTask.setExpectResults(true);
                executeQueryTask.execute(table.getQuery());*/
            });

            this.previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Table table = sqlDataSet.getTable();
                    table.previous();

                    /*ExecuteQueryTask executeQueryTask = new ExecuteQueryTask(table.getDatabase(), table, getContext(), new Callback<SQLDataSet>() {
                        @Override
                        public void onResult(SQLDataSet result) {
                            sqlTableLayout.setData(result);
                        }
                    });

                    executeQueryTask.setExpectResults(true);
                    executeQueryTask.execute(table.getQuery());*/
                }
            });
        } else {
            layout.findViewById(R.id.paging_buttons).setVisibility(View.GONE);
        }

        return layout;
    }
}
