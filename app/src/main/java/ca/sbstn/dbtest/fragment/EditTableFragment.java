package ca.sbstn.dbtest.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.sql.Table;

/**
 * Created by tills13 on 2015-11-23.
 */
public class EditTableFragment extends Fragment {
    private static final String TABLE_PARAM = "table";
    private Table table;

    private View view;
    private ViewPager viewPager;

    public EditTableFragment() {}

    public EditTableFragment newInstance(Table table) {
        EditTableFragment fragment = new EditTableFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(TABLE_PARAM, table);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getArguments() != null) {
            this.table = (Table) this.getArguments().getSerializable(TABLE_PARAM);

            this.viewPager = new ViewPager(getActivity());
            //this.getActivity().getSupportActionbar()//.setNavigationMode();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.edit_table, null);




        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
