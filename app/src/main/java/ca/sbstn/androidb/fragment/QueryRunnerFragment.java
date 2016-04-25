package ca.sbstn.androidb.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.activity.ViewDataActivity;
import ca.sbstn.androidb.sql.Database;

/**
 * Created by tills13 on 2015-11-26.
 */
public class QueryRunnerFragment extends Fragment {
    public static final String PARAM_DATABASE = "DATABASE";
    public static final String SHARED_PREFS_QUERY_HISTORY_KEY = "AndroiDB_Query_History";

    private Database database;
    private QueryHistoryAdapter historyAdapter;
    private OnHistoryButtonClickListener onHistoryButtonClickListener;

    private EditText queryField;
    private TextView statusField;
    private ProgressBar loadingBar;

    public QueryRunnerFragment() {}

    public static QueryRunnerFragment newInstance(Database database) {
        QueryRunnerFragment queryRunnerFragment = new QueryRunnerFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_DATABASE, database);

        queryRunnerFragment.setArguments(bundle);
        return queryRunnerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getArguments() != null) {
            this.database = (Database) this.getArguments().getSerializable(PARAM_DATABASE);
        }

        this.historyAdapter = new QueryHistoryAdapter();
        this.onHistoryButtonClickListener = new OnHistoryButtonClickListener() {
            @Override
            public void onClickNext() {
                queryField.setText(historyAdapter.getNext());
            }

            @Override
            public void onClickPrevious() {
                queryField.setText(historyAdapter.getPrevious());
            }
        };

        this.readHistory();
    }

    @Override
    public void onResume() {
        super.onResume();

        this.readHistory();
    }

    @Override
    public void onPause() {
        super.onPause();

        this.writeHistory(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_query_runner, null);

        this.queryField = (EditText) view.findViewById(R.id.query_field);
        this.statusField = (TextView) view.findViewById(R.id.query_status);
        this.loadingBar = (ProgressBar) view.findViewById(R.id.loading_bar);

        ((Button) view.findViewById(R.id.query_runner_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHistoryButtonClickListener.onClickNext();
            }
        });

        ((Button) view.findViewById(R.id.query_runner_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHistoryButtonClickListener.onClickPrevious();
            }
        });

        ((Button) view.findViewById(R.id.query_runner_run)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runQuery();
            }
        });

        return view;
    }

    public void runQuery() {
        this.loadingBar.setVisibility(View.VISIBLE);
        String query = this.queryField.getText().toString();

        this.historyAdapter.addItem(query);
        ((View) statusField.getParent()).setVisibility(View.GONE);

        Intent intent = new Intent(getContext(), ViewDataActivity.class);
        intent.putExtra(ViewDataActivity.QUERY_PARAM, query);
        intent.putExtra(ViewDataActivity.DATABASE_PARAM, this.database);
        startActivity(intent);
    }

    public void readHistory() {
        SharedPreferences sharedPreferences = ((BaseActivity) getActivity()).getSharedPreferences();
        Set<String> mSet = sharedPreferences.getStringSet(SHARED_PREFS_QUERY_HISTORY_KEY, new HashSet<String>());

        this.historyAdapter.setItems(new ArrayList<>(mSet));
    }

    public void writeHistory(boolean immediate) {
        SharedPreferences sharedPreferences = ((BaseActivity) getActivity()).getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putStringSet(SHARED_PREFS_QUERY_HISTORY_KEY, new HashSet<>(this.historyAdapter.getHistory()));

        if (immediate) editor.commit();
        else editor.apply();
    }

    private interface OnHistoryButtonClickListener {
        public void onClickNext();
        public void onClickPrevious();
    }

    private static class QueryHistoryAdapter {
        private List<String> queryHistory;
        private int position;

        public QueryHistoryAdapter() {
            this.queryHistory = new ArrayList<>();
            this.position = 0;
        }

        public String getQuery(int position) {
            if (this.getHistorySize() == 0) return "";
            if (position >= this.queryHistory.size()) position = this.queryHistory.size() - 1;
            if (position < 0) position = 0;

            return this.queryHistory.get(position);
        }

        public void setItems(List<String> history) {
            this.queryHistory = history;
        }

        public void addItem(String item) {
            this.queryHistory.add(0, item);
        }

        public String getNext() {
            if (this.position != (this.getHistorySize() - 1)) {
                this.position++;
            }

            return this.getQuery(this.position);
        }

        public String getPrevious() {
            if (this.position != 0) {
                this.position--;
            }

            return this.getQuery(this.position);
        }

        public int getHistorySize() {
            return this.queryHistory.size();
        }

        public List<String> getHistory() {
            return this.queryHistory;
        }
    }
}
