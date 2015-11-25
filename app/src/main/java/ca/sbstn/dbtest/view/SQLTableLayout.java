package ca.sbstn.dbtest.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.sql.SQLDataSet;
import ca.sbstn.dbtest.util.Colours;

/**
 * Created by tills13 on 2015-07-13.
 */
public class SQLTableLayout extends LinearLayout {
    public static String TAG = "SQLTableLayout";

    private SQLDataSet data;

    private ScrollView verticalScrollContainer;
    private HorizontalScrollView horizontalScrollContainer;
    private TableLayout headerView;
    private TableLayout dataView;

    private OnRowClickListener mRowClickListener;
    private OnHeaderClickListener mHeaderClickListener;

    public SQLTableLayout(Context context) {
        super(context);
        init();
    }

    public SQLTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SQLTableLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        inflate(this.getContext(), R.layout.sql_table_layout, this);

        this.headerView = (TableLayout) this.findViewById(R.id.header_view);
        this.dataView = (TableLayout) this.findViewById(R.id.data_view);
        this.verticalScrollContainer = (ScrollView) this.findViewById(R.id.vertical_scroll_container);
        this.horizontalScrollContainer = (HorizontalScrollView) this.findViewById(R.id.horizontal_scroll_container);
    }

    public void scrollToRow(int row) {
        //int location [] = {0, 0};
        //this.dataView.getChildAt(row).getLocationInWindow(location);
        //this.verticalScrollContainer.scrollTo(0, location[1]);
    }

    public OnRowClickListener getOnRowSelectedListener() {
        return this.mRowClickListener;
    }

    public OnHeaderClickListener getOnHeaderClickListener() {
        return this.mHeaderClickListener;
    }

    public SQLDataSet getData() {
        return this.data;
    }

    public void setOnRowSelectedListener(OnRowClickListener mRowClickListener) {
        this.mRowClickListener = mRowClickListener;
    }

    public void setOnHeaderClickListener(OnHeaderClickListener mHeaderClickListener) {
        this.mHeaderClickListener = mHeaderClickListener;
    }

    public void setData(SQLDataSet data) {
        this.data = data;

        this.invalidate();
        //this.requestLayout();
    }

    @Override
    public void invalidate() {
        this.dataView.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this.getContext());

        TableRow mHeaderRow = new TableRow(getContext());
        mHeaderRow.setBackgroundColor(Colours.darken(Color.parseColor("#2b303b")));

        for (SQLDataSet.Column column : this.data.getColumns()) {
            LinearLayout cell = (LinearLayout) inflater.inflate(R.layout.table_cell, null);
            TextView cellText = (TextView) cell.findViewById(R.id.cell_text);
            cellText.setText(column.getName());
            cellText.setTypeface(null, Typeface.BOLD);

            mHeaderRow.addView(cell);
        }

        this.dataView.addView(mHeaderRow);

        for (final SQLDataSet.Row row : this.data) {
            TableRow tableRow = new TableRow(getContext());

            for (int i = 0; i < this.data.getColumnCount(); i++) {
                LinearLayout cell = (LinearLayout) inflater.inflate(R.layout.table_cell, null);
                TextView cellText = (TextView) cell.findViewById(R.id.cell_text);

                cellText.setText(row.getString(i));
                tableRow.addView(cell);
            }

            tableRow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mRowClickListener != null) {
                        mRowClickListener.onRowClicked(row);
                    }
                }
            });

            this.dataView.addView(tableRow);
        }

        this.buildHeader();

        super.invalidate();
    }

    public void buildHeader() {
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        TableRow headerRow = new TableRow(getContext());

        for (SQLDataSet.Column column : this.data.getColumns()) {
            LinearLayout cell = (LinearLayout) inflater.inflate(R.layout.table_cell, null);
            TextView cellText = (TextView) cell.findViewById(R.id.cell_text);
            cellText.setText(column.getName());

            //headerRow.addView(cell);
        }

        //this.dataView.addView(headerRow, 0);
        //this.headerView.addView(headerRow);
    }

    public interface OnRowClickListener {
        public void onRowClicked(SQLDataSet.Row row);
    }

    public interface OnHeaderClickListener {
        public void onHeaderClicked(int index);
    }
}
