package ca.sbstn.androidb.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.sql.Database;

public class DatabaseListAdapter extends BaseAdapter {
    private Context context;
    private List<Database> databases;

    public DatabaseListAdapter(Context context) {
        super();

        this.context = context;
        this.databases = new ArrayList<>();
    }

    public void setDatabases(List<Database> databases) {
        this.databases = databases;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        Database database = (Database) this.getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.database_item, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.database_name)).setText(database.getName());
        ((TextView) convertView.findViewById(R.id.database_owner)).setText(database.getOwner());

        if (database.getComment() != null && database.getComment().equals("")) {
            convertView.findViewById(R.id.database_comment).setVisibility(View.GONE);
        } else {
            convertView.findViewById(R.id.database_comment).setVisibility(View.VISIBLE);
            ((TextView) convertView.findViewById(R.id.database_comment)).setText(database.getComment());
        }

        int colorEven, colorOdd;
        if (Build.VERSION.SDK_INT >= 23) {
            colorEven = this.context.getColor(R.color.table_row_even);
            colorOdd = this.context.getColor(R.color.table_row_odd);
        } else {
            colorEven = this.context.getResources().getColor(R.color.table_row_even, null);
            colorOdd = this.context.getResources().getColor(R.color.table_row_odd, null);
        }

        convertView.setBackgroundColor(position % 2 == 0 ? colorEven : colorOdd);

        return convertView;
    }

    @Override
    public int getCount() {
        return this.databases.size();
    }

    @Override
    public Object getItem(int position) {
        return this.databases.get(position);
    }

    public Object getByName(String name) {
        for (Database database : this.databases) {
            if (database.getName().toLowerCase().equals(name.toLowerCase())) {
                return database;
            }
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
