package ca.sbstn.dbtest.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import ca.sbstn.dbtest.R;
import ca.sbstn.dbtest.sql.Table;

public class EditTableActivity extends Activity {

    private Table table;
    private LinearLayout tableInfoContainer;

    //private Map<String, String> originalValues;
    private String originalName;
    private String[] originalColumns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_table);

        Intent intent = this.getIntent();

        this.tableInfoContainer = (LinearLayout) this.findViewById(R.id.table_info_container);
        this.table = (Table) intent.getSerializableExtra("table");
        //this.originalValues = new HashMap<>();
        this.originalColumns = new String[this.table.getColumns().length];

        ActionBar ab = this.getActionBar();

        if (ab != null) {
            ab.setTitle(String.format("Editing table %s", this.table.getName()));
        }

        int padding = (int) getResources().getDimension(R.dimen.container_padding);

        ViewGroup.MarginLayoutParams editTextLayoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editTextLayoutParams.setMargins(0, 0, 0, 0);

        LinearLayout.LayoutParams editTextContainerLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < this.table.getColumns().length; i++) {
            String column = this.table.getColumns()[i];

            /*TextView title = (TextView) divider.findViewById(R.id.title);
            title.setText(column);

            this.tableInfoContainer.addView(divider);

            EditText editText = new EditText(this);
            editText.setPadding(0, padding, 0, padding);
            editText.setLayoutParams(editTextLayoutParams);
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) editText.getLayoutParams();
            mlp.setMargins(0, 0, 0, 0);
            editText.setHint(column);
            editText.setText(column);

            LinearLayout editTextContainer = new LinearLayout(this);
            editTextContainer.setLayoutParams(editTextContainerLayoutParams);
            editTextContainer.setPadding(padding, 0, padding, 0);
            editTextContainer.addView(editText);

            this.tableInfoContainer.addView(editTextContainer);*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_table, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String generatePatchSQL() {
        return "SELECT * FROM soething;";
    }
}
