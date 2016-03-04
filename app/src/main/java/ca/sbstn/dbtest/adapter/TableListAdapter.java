package ca.sbstn.dbtest.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.sql.Table;

/**
 * Created by tills13 on 15-06-26.
 */
public class TableListAdapter extends BaseAdapter {
    public Context context;
    public List<Table> tables;
    //public List<String> headers;

    public Map<String, Boolean> isCollapsed;
    public Map<String, List<Table>> headers;
    public Map<String, List<Table>> finalHeaders;

    public boolean showTables;
    public boolean showViews;
    public boolean showIndexes;
    public boolean showSequences;

    public int sortType; // 0 = name, 1 = type, 2 = schema

    public TableListAdapter(Context context) {
        super();

        this.headers = new HashMap<>();
        this.finalHeaders = new HashMap<>();
        this.isCollapsed = new HashMap<>();

        this.context = context;

        this.showTables = true;
        this.showViews = false;
        this.showIndexes = false;
        this.showSequences = false;

        this.sortType = 1;
        this.applyFilters();
    }

    public void sort() {
        final int finalSortType = sortType;

        Table[] tablesArray = new Table[this.tables.size()];
        tablesArray = this.tables.toArray(tablesArray);

        Arrays.sort(tablesArray, new Comparator<Table>() {
            @Override
            public int compare(Table t1, Table t2) {
                if (finalSortType == 0) {
                    return t1.getName().compareTo(t2.getName());
                } else if (finalSortType == 1) {
                    return t1.getTypeString().compareTo(t2.getTypeString());
                } else {
                    return t1.getSchema().compareTo(t2.getSchema());
                }
            }
        });

        this.tables = Arrays.asList(tablesArray);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.context);

        if (this.isHeader(position)) {
            String headerKey = this.getHeader(position);
            List<Table> tablesUnderHeader = this.finalHeaders.get(headerKey);

            LinearLayout header = (LinearLayout) inflater.inflate(R.layout.table_header, null);
            ((TextView) header.findViewById(R.id.primary_title)).setText(this.getHeader(position));

            if (tablesUnderHeader == null) {
                header.findViewById(R.id.icon).setVisibility(View.GONE);
                //((ImageView) header.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_indeterminate_check_box_white_24dp));
                ((TextView) header.findViewById(R.id.secondary_title)).setText("Click to Load");
            } else {
                if (this.isCollapsed.get(headerKey)) {
                    ((ImageView) header.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_white_48dp));
                } else {
                    ((ImageView) header.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_white_48dp));
                }

                ((TextView) header.findViewById(R.id.secondary_title)).setText(String.format("%d items", tablesUnderHeader.size()));
            }

            return header;
        } else {
            Table table = this.getItem(position);

            if (convertView == null || convertView.findViewById(R.id.tableName) == null) {
                convertView = inflater.inflate(R.layout.table_item, parent, false);
            }

            TextView tableType = ((TextView) convertView.findViewById(R.id.table_type));
            tableType.setText(table.getTypeString());

            /*if (table.is(Table.Type.TABLE)) {
                tableType.setText(table.getType().);
                ((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_view_module_white_48dp));
            } else if (table.is(Table.Type.VIEW) || table.is(Table.Type.SYSTEM_VIEW)) {
                ((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_visibility_white_48dp));
            } else if (table.is(Table.Type.INDEX) || table.is(Table.Type.SYSTEM_INDEX) || table.is(Table.Type.SYSTEM_TOAST_INDEX)) {
                ((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_info_outline_white_48dp));
            } else if (table.is(Table.Type.SEQUENCE)) {
                //((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_view_list_white_48dp));
            }*/

            ((TextView) convertView.findViewById(R.id.tableName)).setText(table.getName());

            if (position % 2 == 0) convertView.setBackgroundColor(Color.argb((int) Math.floor(0.05 * 255), 255, 255, 255));
            else convertView.setBackground(null);

            return convertView;
        }
    }

    public void applyFilters() {
        Log.e("BLAH", "APPLY FIlTER");
        this.finalHeaders.clear();

        for (String key : this.headers.keySet()) {
            List<Table> tables = this.headers.get(key);

            if (tables != null) {
                List<Table> mTables = new ArrayList<>();

                for (Table table : tables) {
                    Log.d("type", table.getTypeString());
                    if (table.getType() == Table.Type.INDEX && this.showIndexes) {
                        mTables.add(table);
                    } else if (table.getType() == Table.Type.TABLE && this.showTables) {
                        mTables.add(table);
                    } else if (table.getType() == Table.Type.VIEW && this.showViews) {
                        mTables.add(table);
                    } else if (table.getType() == Table.Type.SEQUENCE && this.showSequences) {
                        mTables.add(table);
                    }
                }

                this.finalHeaders.put(key, mTables);
            } else {
                this.finalHeaders.put(key, null);
            }
        }
    }

    @Override
    public int getCount() {
        int size = 0;

        for (String key : this.finalHeaders.keySet()) {
            List<Table> tablesInSchema = this.finalHeaders.get(key);

            if (tablesInSchema == null || this.isCollapsed.get(key)) {
                size = size + 1; // just the header
            } else {
                size = size + (this.isCollapsed.get(key) ? 1 : (this.finalHeaders.get(key).size()) + 1);
            }
        }

        return size;
    }

    public String getHeader(int position) {
        for (String key : this.finalHeaders.keySet()) {
            List<Table> mTables = this.finalHeaders.get(key);

            if (position == 0) { // header
                return key;
            } else {
                if (this.isCollapsed.get(key) || mTables == null) {
                    position = position - 1;
                } else {
                    if (position > mTables.size()) {
                        position = position - mTables.size() - 1;
                    } else {
                        return key;
                    }
                }
            }
        }

        return null;
    }

    public boolean isHeader(int position) {
        for (String key : this.finalHeaders.keySet()) {
            List<Table> mTables = this.finalHeaders.get(key);

            if (position == 0) { // header
                return true;
            } else {
                if (this.isCollapsed.get(key) || mTables == null) {
                    position = position - 1;
                } else {
                    if (position > mTables.size()) {
                        position = position - mTables.size() - 1;
                    } else {
                        return false;
                    }
                }
            }
        }

        return false;
    }

    public boolean isLoaded(String header) {
        return (this.finalHeaders.get(header) != null);
    }

    public void toggleCollapsed(String header) {
        if (this.isCollapsed.containsKey(header)) {
            this.isCollapsed.put(header, !this.isCollapsed.get(header));
        }
    }

    public void setIsCollapsed(String header, boolean isCollapsed) {
        this.isCollapsed.put(header, isCollapsed);
    }

    public void setItems(String schema, List<Table> tables) {
        this.headers.put(schema, tables);
        this.setIsCollapsed(schema, false);
        this.applyFilters();
    }

    @Override
    public Table getItem(int position) {
        for (String key : this.finalHeaders.keySet()) {
            List<Table> mTables = this.finalHeaders.get(key);

            if (position == 0) { // header
                return null;
            } else {
                if (this.isCollapsed.get(key) || mTables == null) {
                    position = position - 1;
                } else {
                    if (position > mTables.size()) {
                        position = position - mTables.size() - 1;
                    } else {
                        return mTables.get(position - 1);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setSchemas(List<String> schemas) {
        this.headers.clear();
        this.isCollapsed.clear();

        for (String schema : schemas) {
            this.headers.put(schema, null);
            this.isCollapsed.put(schema, true);
        }

        this.applyFilters();
    }

    public void setShowTables(boolean showTables) {
        this.showTables = showTables;
        this.applyFilters();
    }

    public boolean getShowTables() {
        return this.showTables;
    }

    public void setShowViews(boolean showViews) {
        this.showViews = showViews;
        this.applyFilters();
    }

    public boolean getShowViews() {
        return this.showViews;
    }

    public void setShowIndexes(boolean showIndexes) {
        this.showIndexes = showIndexes;
        this.applyFilters();
    }

    public boolean getShowIndexes() {
        return this.showIndexes;
    }

    public void setShowSequences(boolean showSequences) {
        this.showSequences = showSequences;
        this.applyFilters();
    }

    public boolean getShowSequences() {
        return this.showSequences;
    }
}
