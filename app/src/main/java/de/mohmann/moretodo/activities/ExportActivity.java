package de.mohmann.moretodo.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.data.DatabaseHelper;
import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.util.Utils;

public class ExportActivity extends AppCompatActivity {

    final private static String CLIP_LABEL =
            "de.mohmann.moretodo.activities.ExportActivity.CLIP_LABEL";

    private TextView mJsonTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        setupActionBar();

        final DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        final Type type = new TypeToken<List<Todo>>(){}.getType();
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        mJsonTextView = (TextView) findViewById(R.id.json_export);
        mJsonTextView.setText(gson.toJson(dbHelper.getTodos(), type));
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar == null)
            return;

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        /* add back button and bind onBackPressed action to it */
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_export, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_copy) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(CLIP_LABEL, mJsonTextView.getText());
            clipboard.setPrimaryClip(clip);
            Utils.toast(this, R.string.message_copied_to_clipboard);
        }

        return super.onOptionsItemSelected(item);
    }
}
