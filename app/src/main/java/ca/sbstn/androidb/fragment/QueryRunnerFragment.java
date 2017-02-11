package ca.sbstn.androidb.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.BaseActivity;
import ca.sbstn.androidb.activity.ViewDataActivity;
import ca.sbstn.androidb.sql.Database;

public class QueryRunnerFragment extends Fragment {
    public static final String PARAM_DATABASE = "DATABASE";
    public static final String SHARED_PREFS_QUERY_HISTORY_KEY = "AndroiDB_Query_History";

    private Database database;
    private QueryHistoryAdapter historyAdapter;
    private OnHistoryButtonClickListener onHistoryButtonClickListener;

    @BindView(R.id.query_field) protected EditText queryField;
    @BindView(R.id.query_status) protected TextView statusField;
    @BindView(R.id.loading_bar) protected ProgressBar loadingBar;

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
        View layout = inflater.inflate(R.layout.query_runner, container, false);
        ButterKnife.bind(this, layout);

        this.queryField.addTextChangedListener(new QueryFormatter(this.queryField));

        layout.findViewById(R.id.query_runner_next).setOnClickListener((view) ->
            onHistoryButtonClickListener.onClickNext()
        );

        layout.findViewById(R.id.query_runner_next).setOnClickListener((view) ->
            onHistoryButtonClickListener.onClickPrevious()
        );

        layout.findViewById(R.id.query_runner_run).setOnClickListener((view) -> runQuery());

        return layout;
    }

    public void runQuery() {
        this.loadingBar.setVisibility(View.VISIBLE);
        String query = this.queryField.getText().toString();

        this.historyAdapter.addItem(query);
        ((View) statusField.getParent()).setVisibility(View.GONE);

        Intent intent = new Intent(getContext(), ViewDataActivity.class);
        intent.putExtra(ViewDataActivity.PARAM_QUERY, query);
        intent.putExtra(ViewDataActivity.PARAM_DATABASE, this.database);
        startActivity(intent);
    }

    public void readHistory() {
        SharedPreferences sharedPreferences = ((BaseActivity) getActivity()).getSharedPreferences();
        Set<String> mSet = sharedPreferences.getStringSet(SHARED_PREFS_QUERY_HISTORY_KEY, new HashSet<>());

        this.historyAdapter.setItems(new ArrayList<>(mSet));
    }

    public void writeHistory(boolean immediate) {
        SharedPreferences sharedPreferences = ((BaseActivity) getActivity()).getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putStringSet(SHARED_PREFS_QUERY_HISTORY_KEY, new HashSet<>(this.historyAdapter.getHistory()));

        if (immediate) editor.apply();
        else editor.apply();
    }

    private interface OnHistoryButtonClickListener {
        void onClickNext();
        void onClickPrevious();
    }

    private static class QueryFormatter implements TextWatcher {
        private EditText editText;

        QueryFormatter(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //this.cursorIndex = queryField.getSelectionStart();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable editable) {
            this.editText.removeTextChangedListener(this);

            String fieldValue = editable.toString();

            int selectionStart = this.editText.getSelectionStart();
            int selectionEnd = this.editText.getSelectionEnd();

            fieldValue = fieldValue
                .replaceAll("'(.*?)'", "<font color='orange'>'$1'</font>")
                .replaceAll("\\.([^ ]+)", "<font color='purple'>.$1</font>")
                .replaceAll("(?i)(analyze|inspect)", "<font color='blue'>$1</font>")
                .replaceAll("(?i)(select|insert|create table|update)", "<font color='blue'>$1</font>")
                .replaceAll("(?i) (from|as|insert|where|join|on|natural|public|limit|offset|update|delete)", "<font color='blue'> $1</font>")
                .replaceAll("(?i) (order by|group by)", "<font color='blue'> $1</font>");

            if (Build.VERSION.SDK_INT >= 24) {
                this.editText.setText(Html.fromHtml(fieldValue, Html.FROM_HTML_MODE_COMPACT));
            } else {
                this.editText.setText(Html.fromHtml(fieldValue));
            }

            this.editText.setSelection(selectionStart, selectionEnd);

            this.editText.addTextChangedListener(this);
        }

    }
    private static class QueryHistoryAdapter {
        private List<String> queryHistory;
        private int position;

        QueryHistoryAdapter() {
            this.queryHistory = new ArrayList<>();
            this.position = 0;
        }

        String getQuery(int position) {
            if (this.getHistorySize() == 0) return "";
            if (position >= this.queryHistory.size()) position = this.queryHistory.size() - 1;
            if (position < 0) position = 0;

            return this.queryHistory.get(position);
        }

        void setItems(List<String> history) {
            this.queryHistory = history;
        }

        void addItem(String item) {
            this.queryHistory.add(0, item);
        }

        String getNext() {
            if (this.position != (this.getHistorySize() - 1)) {
                this.position++;
            }

            return this.getQuery(this.position);
        }

        String getPrevious() {
            if (this.position != 0) {
                this.position--;
            }

            return this.getQuery(this.position);
        }

        int getHistorySize() {
            return this.queryHistory.size();
        }

        List<String> getHistory() {
            return this.queryHistory;
        }
    }
}
