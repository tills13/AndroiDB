package ca.sbstn.dbtest.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.sql.Database;

/**
 * Created by tills13 on 15-06-26.
 */
public class DatabaseListAdapter extends BaseAdapter {
    public Context context;
    public List<Database> databases;

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

        if (position % 2 == 0) convertView.setBackgroundColor(Color.argb((int) Math.floor(0.05 * 255), 255, 255, 255));
        //else convertView.setBackgroundColor(colorOdd);

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
            Log.d("asdasd", database.getName() + " " + name);
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
