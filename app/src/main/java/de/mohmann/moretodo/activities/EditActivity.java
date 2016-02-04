package de.mohmann.moretodo.activities;

import android.content.res.Resources;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.data.TodoStore;

public class EditActivity extends AppCompatActivity {

    final public static String TAG = "EditActivity";

    private EditText mInputTitle;
    private EditText mInputContent;

    private Todo mTodo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setupActionBar();

        mInputTitle = (EditText) findViewById(R.id.input_title);
        mInputContent = (EditText) findViewById(R.id.input_content);

        /* get intent data */
        mTodo = getIntent().getParcelableExtra(Todo.EXTRA_TODO);

        populateViews();
    }

    private void populateViews() {
        if (mTodo == null) {
            setTitle(getResources().getString(R.string.title_activity_new));
        } else {
            mInputTitle.setText(mTodo.getTitle());
            mInputContent.setText(mTodo.getContent());
        }
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_save) {
            Resources res = getResources();
            TodoStore todoStore = TodoStore.getInstance();
            String title = mInputTitle.getText().toString().trim();
            String content = mInputContent.getText().toString().trim();
            String message;

            if (title.equals("")) {
                message = res.getString(R.string.message_todo_enter_title);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                return true;
            }

            if (mTodo == null) {
                mTodo = new Todo(title, content);
                todoStore.add(mTodo);
                message = res.getString(R.string.message_todo_created);
            } else {
                mTodo = todoStore.getById(mTodo.getId());
                mTodo.setTitle(title);
                mTodo.setContent(content);
                message = res.getString(R.string.message_todo_updated);
            }
            todoStore.persist();

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}
