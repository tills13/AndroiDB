package ca.sbstn.androidb.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import ca.sbstn.androidb.sql.Table;


public class ViewTableFragment extends Fragment {
    public static final String PARAM_TABLE = "TABLE";
    private Table table;

    public ViewTableFragment getInstance(Table table) {
        ViewTableFragment viewTableFragment = new ViewTableFragment();

        Bundle arguments = new Bundle();
        arguments.putSerializable(PARAM_TABLE, table);

        viewTableFragment.setArguments(arguments);
        return viewTableFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.table = (Table) this.getArguments().getSerializable(PARAM_TABLE);
    }
}
