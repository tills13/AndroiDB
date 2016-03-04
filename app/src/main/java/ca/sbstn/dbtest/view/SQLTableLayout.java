package ca.sbstn.dbtest.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.sql.SQLDataSet;
import ca.sbstn.dbtest.util.Colours;
import ca.sbstn.dbtest.util.Utils;

/**
 * Created by tills13 on 2015-07-13.
 */
public class SQLTableLayout extends LinearLayout {
    public static String TAG = "SQLTableLayout";

    private SQLDataSet data;

    private LayoutInflater inflater;

    private ScrollView verticalScrollContainer;
    private HorizontalScrollView horizontalScrollContainer;

    private TableLayout dataView;
    private TableLayout headerView;

    //private TableRow stickyHeader;

    private OnRowClickListener mRowClickListener;
    private OnHeaderClickListener mHeaderClickListener;

    private boolean needInvalidateStickyHeader;

    private int textColor;
    private float cellPadding;
    private boolean stickyHeader;

    public SQLTableLayout(Context context) {
        this(context, null);
    }

    public SQLTableLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SQLTableLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        inflate(this.getContext(), R.layout.sql_table_layout, this);

        this.inflater = LayoutInflater.from(this.getContext());
        this.dataView = (TableLayout) this.findViewById(R.id.data_view);
        this.headerView = (TableLayout) this.findViewById(R.id.header_view);
        this.verticalScrollContainer = (ScrollView) this.findViewById(R.id.vertical_scroll_container);
        this.horizontalScrollContainer = (HorizontalScrollView) this.findViewById(R.id.horizontal_scroll_container);
        this.needInvalidateStickyHeader = false;

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SQLTableLayout, 0, 0);

        try {
            this.textColor = array.getColor(R.styleable.SQLTableLayout_textColor, Color.parseColor("#ffffff"));
            this.stickyHeader = array.getBoolean(R.styleable.SQLTableLayout_stickyHeader, false);
            this.cellPadding = array.getDimension(R.styleable.SQLTableLayout_cellPadding, Utils.dpToPixels(getContext().getResources(), 16));
        } finally {
            array.recycle();
        }

        if (!this.stickyHeader) {
            this.headerView.setVisibility(View.GONE);
        }

        this.verticalScrollContainer.setOnScrollChangeListener(new OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY >= 168) { // todo not hardcode
                    headerView.animate().translationY(0).setInterpolator(new AccelerateInterpolator(2));
                } else {
                    headerView.animate().translationY(-168).setInterpolator(new DecelerateInterpolator(2));
                }
            }
        });
    }

    public void setOnRowSelectedListener(OnRowClickListener mRowClickListener) {
        this.mRowClickListener = mRowClickListener;
    }

    public OnRowClickListener getOnRowSelectedListener() {
        return this.mRowClickListener;
    }

    public void setOnHeaderClickListener(OnHeaderClickListener mHeaderClickListener) {
        this.mHeaderClickListener = mHeaderClickListener;
    }

    public OnHeaderClickListener getOnHeaderClickListener() {
        return this.mHeaderClickListener;
    }

    public void setData(SQLDataSet data) {
        this.data = data;
        this.invalidate();
    }

    public SQLDataSet getData() {
        return this.data;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (this.needInvalidateStickyHeader && this.stickyHeader) {
            Log.e("ALERT", "INVALIDATED HEADER");
            this.headerView.removeAllViews();

            TableRow mHeaderRow = new TableRow(getContext());
            TableRow tableHeader = (TableRow) this.dataView.getChildAt(0);

            List<SQLDataSet.Column> columns = this.data.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                SQLDataSet.Column column = columns.get(i);

                LinearLayout cell = (LinearLayout) this.inflater.inflate(R.layout.table_cell, null);
                TextView cellText = (TextView) cell.findViewById(R.id.cell_text);

                cellText.setText(column.getName());
                cellText.setTypeface(null, Typeface.BOLD);

                cell.setMinimumWidth(tableHeader.getChildAt(i).getWidth());

                final int index = i;
                if (this.mHeaderClickListener != null) {
                    cell.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mHeaderClickListener.onHeaderClicked(index);
                        }
                    });
                }

                mHeaderRow.addView(cell);
            }

            this.headerView.setBackgroundColor(Color.argb((int) Math.floor(0.50 * 255), 0, 0, 0));
            this.headerView.addView(mHeaderRow);
            this.needInvalidateStickyHeader = false;
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();

        this.dataView.removeAllViews();

        if (!this.stickyHeader || true) {
            TableRow mHeaderRow = new TableRow(this.getContext());
            mHeaderRow.setBackgroundColor(Colours.darken(Color.parseColor("#2b303b")));

            int i = 1;
            for (SQLDataSet.Column column : this.data.getColumns()) {
                LinearLayout cell = (LinearLayout) this.inflater.inflate(R.layout.table_cell, null);
                TextView cellText = (TextView) cell.findViewById(R.id.cell_text);
                cellText.setText(column.getName());
                cellText.setTypeface(null, Typeface.BOLD);

                final int index = i++;
                cell.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mHeaderClickListener != null) {
                            mHeaderClickListener.onHeaderClicked(index);
                        }

                    }
                });

                mHeaderRow.addView(cell);
            }

            this.dataView.addView(mHeaderRow);
        }

        for (int j = 0; j < this.data.getRowCount(); j++) {
            final SQLDataSet.Row row = this.data.getRow(j);
            TableRow tableRow = new TableRow(this.getContext());
            /*TextView cell = new TextView(this.getContext());
            cell.setTextColor(this.textColor);
            cell.setPadding((int) this.cellPadding, (int) this.cellPadding, (int) this.cellPadding, (int) this.cellPadding);

            cell.setText("ROW " + j);
            tableRow.addView(cell);*/
        //}
        //for (final SQLDataSet.Row row : this.data) {
        //    TableRow tableRow = new TableRow(getContext());

            for (int i = 0; i < this.data.getColumnCount(); i++) {
                TextView cell = new TextView(this.getContext());
                //cell = new TextView(getContext());
                cell.setTextColor(this.textColor);
                cell.setPadding((int) this.cellPadding, (int) this.cellPadding, (int) this.cellPadding, (int) this.cellPadding);

                cell.setText(row.getString(i));
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

            if (j % 2 == 0) {
                //tableRow.setBackgroundColor(Colours.darken(getResources().getColor(R.color.colorPrimary)));
                tableRow.setBackgroundColor(Color.argb((int) Math.floor(0.05 * 255), 255, 255, 255));
            }

            this.dataView.addView(tableRow);
        }

        this.needInvalidateStickyHeader = true;
        this.verticalScrollContainer.smoothScrollTo(0, 0);
        this.horizontalScrollContainer.smoothScrollTo(0, 0);
    }

    public interface OnRowClickListener {
        void onRowClicked(SQLDataSet.Row row);
    }

    public interface OnHeaderClickListener {
        void onHeaderClicked(int index);
    }
}
