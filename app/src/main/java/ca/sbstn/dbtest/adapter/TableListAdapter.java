package ca.sbstn.dbtest.adapter;

import android.content.Context;
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
    public List<String> headers;

    public Map<String, Boolean> isCollapsed;
    public Map<String, List<Table>> schemas;

    public boolean showTables;
    public boolean showViews;
    public boolean showIndexes;
    public boolean showSequences;

    public int sortType; // 0 = name, 1 = type, 2 = schema

    public TableListAdapter(Context context) {
        super();

        this.schemas = new HashMap<>();
        this.isCollapsed = new HashMap<>();

        this.context = context;

        this.showTables = true;
        this.showViews = true;
        this.showIndexes = true;
        this.showSequences = true;

        this.sortType = 1;
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
            List<Table> tablesUnderHeader = this.schemas.get(headerKey);

            LinearLayout header = (LinearLayout) inflater.inflate(R.layout.table_header, null);

            ((TextView) header.findViewById(R.id.primary_title)).setText(this.getHeader(position));

            if (tablesUnderHeader == null) {
                ((ImageView) header.findViewById(R.id.icon)).setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_indeterminate_check_box_white_24dp));

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

        for (String key : this.schemas.keySet()) {
            List<Table> tablesInSchema = this.schemas.get(key);

            if (tablesInSchema == null || this.isCollapsed.get(key)) {
                size = size + 1; // just the header
            } else {
                size = size + (this.isCollapsed.get(key) ? 1 : (this.schemas.get(key).size()) + 1);
            }
        }

        return size;
    }

    public String getHeader(int position) {
        for (String key : this.schemas.keySet()) {
            List<Table> mTables = this.schemas.get(key);

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
        for (String key : this.schemas.keySet()) {
            List<Table> mTables = this.schemas.get(key);

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
        return (this.schemas.get(header) != null);
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
        this.schemas.put(schema, tables);
        this.setIsCollapsed(schema, false);
    }

    @Override
    public Table getItem(int position) {
        for (String key : this.schemas.keySet()) {
            List<Table> mTables = this.schemas.get(key);

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
        //return this.applyFilters().get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas.clear();
        this.isCollapsed.clear();

        for (String schema : schemas) {
            Log.d("blah", schema);
            this.schemas.put(schema, null);
            this.isCollapsed.put(schema, true);
        }
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
}
