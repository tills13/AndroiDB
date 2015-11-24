package ca.sbstn.dbtest.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.sql.SQLResult;
import ca.sbstn.dbtest.view.SQLTableLayout;

/**
 * Created by tills13 on 2015-11-23.
 */
public class ViewDataFragment extends Fragment {
    public static final String PARAM_SQLRESULT = "sqlResult";

    private SQLResult sqlResult;

    public ViewDataFragment() {}

    public static ViewDataFragment newInstance(SQLResult sqlResult) {
        ViewDataFragment viewDataFragment = new ViewDataFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_SQLRESULT, sqlResult);

        return viewDataFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getArguments() != null) {
            this.sqlResult = (SQLResult) this.getArguments().getSerializable(PARAM_SQLRESULT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.data_view, null);

        SQLTableLayout sqlTableLayout = (SQLTableLayout) view.findViewById(R.id.data_view);
        ((Button) view.findViewById(R.id.next)).setOnClickListener(null);
        ((Button) view.findViewById(R.id.previous)).setOnClickListener(null);

        return view;
    }
}
