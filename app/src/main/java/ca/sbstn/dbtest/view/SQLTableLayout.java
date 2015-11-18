package ca.sbstn.dbtest.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.activity.RowInspectorActivity;
import ca.sbstn.dbtest.sql.Table;

/**
 * Created by tills13 on 2015-07-13.
 */
public class SQLTableLayout extends TableLayout {
    public static String TAG = "SQLTableLayout";

    private Table table;

    public SQLTableLayout(Context context) {
        super(context);
    }

    public SQLTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SQLTableLayout(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public Table getTable() {
        return this.table;
    }

    public void setTable(Table table) {
        this.table = table;

        this.invalidate();
    }

    @Override
    public void invalidate() {
        this.removeAllViews();

        TableRow header = new TableRow(this.getContext());

        for (String colName : this.table.getColumns()) {
            LinearLayout cell = (LinearLayout) LayoutInflater.from(this.getContext()).inflate(R.layout.table_cell, null);
            TextView cellText = (TextView) cell.findViewById(R.id.cell_text);

            if (this.table.getOrderBy().equals(colName)) {
                colName += "*";
            }

            cellText.setTypeface(null, Typeface.BOLD);
            cellText.setText(colName);

            header.addView(cell);
        }

        header.setBackgroundColor(Color.argb(50, 0, 0, 0));
        this.addView(header);
        //((ViewGroup) ((ViewGroup) this.getParent()).getParent()).addView(header); // parent = hsv

        int colorEven = getResources().getColor(R.color.table_row_even);
        int colorOdd = getResources().getColor(R.color.table_row_odd);

        for (int rowIndex = 0; rowIndex < this.table.getRowCount(); rowIndex++) {
            final String[] row = this.table.getRow(rowIndex);
            TableRow mRow = new TableRow(this.getContext());

            for (String col : row) {
                LinearLayout cell = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.table_cell, null);
                TextView cellText = (TextView) cell.findViewById(R.id.cell_text);
                cellText.setText(col);

                mRow.addView(cell);
            }

            if (rowIndex % 2 != 0) mRow.setBackgroundColor(colorOdd);
            else mRow.setBackgroundColor(colorEven);

            final int index = rowIndex;
            mRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), RowInspectorActivity.class);

                    intent.putExtra("rowIndex", index);
                    intent.putExtra("table", table);
                    getContext().startActivity(intent);
                }
            });

            this.addView(mRow);
        }

        super.invalidate();
    }
}
