package ca.sbstn.dbtest.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.activity.AndroiDB;
import ca.sbstn.dbtest.sql.SQLDataSet;
import ca.sbstn.dbtest.view.SQLTableLayout;

/**
 * Created by tills13 on 2015-11-23.
 */
public class ViewDataFragment extends Fragment {
    public static final String PARAM_SQLRESULT = "sqlResult";

    private SQLDataSet sqlDataSet;
    private SQLTableLayout sqlTableLayout;

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

        if (this.getArguments() != null) {
            this.sqlDataSet = (SQLDataSet) this.getArguments().getSerializable(PARAM_SQLRESULT);
        }
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

        //((Button) view.findViewById(R.id.next)).setOnClickListener(null);
        //((Button) view.findViewById(R.id.previous)).setOnClickListener(null);
        view.findViewById(R.id.paging_buttons).setVisibility(View.GONE);

        return view;
    }
}
