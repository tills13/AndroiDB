package ca.sbstn.dbtest.adapter;

import android.content.Context;
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
    public List<String> headers;

    public Map<String, Boolean> isCollapsed;
    public Map<String, List<Table>> sections;

    public boolean showTables;
    public boolean showViews;
    public boolean showIndexes;
    public boolean showSequences;

    public int sortType; // 0 = name, 1 = type, 2 = schema

    public TableListAdapter(Context context) {
        super();

        this.sections = new HashMap<>();
        this.isCollapsed = new HashMap<>();

        this.context = context;
        this.tables = new ArrayList<>();

        this.showTables = true;
        this.showViews = true;
        this.showIndexes = true;
        this.showSequences = true;

        this.sortType = 0;
    }

    public void divide() {
        this.sections = new HashMap<>();
        this.isCollapsed = new HashMap<>();

        for (Table table : this.tables) {
            String label = null;

            if (this.sortType == 0) { // name
                label = table.getName().substring(0, 1);
            } else if (this.sortType == 1) { // type
                label = table.getTypeString();
            } else {
                label = table.getSchema();
            }

            if (!this.sections.containsKey(label)) {
                this.sections.put(label, new ArrayList<Table>());
            }

            if (!this.isCollapsed.containsKey(label)) {
                this.isCollapsed.put(label, false);
            }

            List<Table> mTables = this.sections.get(label);
            mTables.add(table);

            this.sections.put(label, mTables);
        }
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

        int colorEven = this.context.getResources().getColor(R.color.table_row_even);
        int colorOdd = this.context.getResources().getColor(R.color.table_row_odd);

        if (this.isHeader(position)) {
            String headerKey = this.getHeader(position);

            LinearLayout header = (LinearLayout) inflater.inflate(R.layout.table_header, null);

            if (this.isCollapsed.get(headerKey)) {
                ((ImageView) header.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_white_48dp));
            } else {
                ((ImageView) header.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_white_48dp));
            }

            ((TextView) header.findViewById(R.id.primary_title)).setText(this.getHeader(position));
            ((TextView) header.findViewById(R.id.secondary_title)).setText(String.format("%d items", this.sections.get(this.getHeader(position)).size()));

            return header;
        } else {
            Table table = this.getItem(position);

            if (convertView == null || (convertView != null && convertView.findViewById(R.id.tableName) == null)) {
                convertView = inflater.inflate(R.layout.table_item, parent, false);
            }

            if (table.is(Table.Type.TABLE)) {
                ((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_view_module_white_48dp));
            } else if (table.is(Table.Type.VIEW) || table.is(Table.Type.SYSTEM_VIEW)) {
                ((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_visibility_white_48dp));
            } else if (table.is(Table.Type.INDEX) || table.is(Table.Type.SYSTEM_INDEX) || table.is(Table.Type.SYSTEM_TOAST_INDEX)) {
                ((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_info_outline_white_48dp));
            } else if (table.is(Table.Type.SEQUENCE)) {
                ((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_list_white_48dp));
            }

            ((TextView) convertView.findViewById(R.id.tableName)).setText(table.getName());
            ((TextView) convertView.findViewById(R.id.schema)).setText(table.getSchema());

            if (position % 2 == 0) convertView.setBackgroundColor(colorEven);
            else convertView.setBackgroundColor(colorOdd);

            return convertView;
        }
    }

    public List<Table> applyFilters() {
        List<Table> finalList = new ArrayList<>();

        for (Table table : this.tables) {
            if (table.getType() == Table.Type.INDEX && this.showIndexes) {
                finalList.add(table);
            } else if (table.getType() == Table.Type.TABLE && this.showTables) {
                finalList.add(table);
            } else if (table.getType() == Table.Type.VIEW && this.showViews) {
                finalList.add(table);
            } else if (table.getType() == Table.Type.SEQUENCE && this.showSequences) {
                finalList.add(table);
            }
        }

        return finalList;
    }

    @Override
    public int getCount() {
        int size = 0;

        String[] keys = new String[this.sections.keySet().size()];
        keys = this.sections.keySet().toArray(keys);
        Arrays.sort(keys);

        for (String key : keys) {
            size = size + (this.isCollapsed.get(key) ? 1 : this.sections.get(key).size());
        }

        return size;
    }

    public String getHeader(int position) {
        String[] keys = new String[this.sections.keySet().size()];
        keys = this.sections.keySet().toArray(keys);
        Arrays.sort(keys);

        for (String key : keys) {
            List<Table> mTables = this.sections.get(key);

            if (position == 0) { // header
                return key;
            } else {
                if (this.isCollapsed.get(key)) {
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
        String[] keys = new String[this.sections.keySet().size()];
        keys = this.sections.keySet().toArray(keys);
        Arrays.sort(keys);

        for (String key : keys) {
            List<Table> mTables = this.sections.get(key);

            if (position == 0) { // header
                return true;
            } else {
                if (this.isCollapsed.get(key)) {
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

    public void toggleCollapsed(String header) {
        if (this.isCollapsed.containsKey(header)) {
            this.isCollapsed.put(header, !this.isCollapsed.get(header));
        }
    }

    public void setItems(List<Table> tables) {
        this.tables = tables;
        this.divide();
        //this.sort();
    }

    public void setItems(String section, List<Table> tables) {

    }

    @Override
    public Table getItem(int position) {
        String[] keys = new String[this.sections.keySet().size()];
        keys = this.sections.keySet().toArray(keys);
        Arrays.sort(keys);

        for (String key : keys) {
            List<Table> mTables = this.sections.get(key);

            if (position == 0) { // header
                return null;
            } else {
                if (this.isCollapsed.get(key)) {
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
        //return this.applyFilters().get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setShowTables(boolean showTables) {
        this.showTables = showTables;
    }

    public void setShowViews(boolean showViews) {
        this.showViews = showViews;
    }

    public void setShowIndexes(boolean showIndexes) {
        this.showIndexes = showIndexes;
    }

    public void setShowSequences(boolean showSequences) {
        this.showSequences = showSequences;
    }

    public void setSortType(int sortType) {
        this.sortType = sortType;
        this.divide();
        //this.sort();
    }
}
