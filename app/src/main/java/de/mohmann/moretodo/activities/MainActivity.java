package de.mohmann.moretodo.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.adapters.TodoListAdapter;
import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.data.TodoStore;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, View.OnClickListener,
        DialogInterface.OnClickListener {

    final public static String TAG = "MainActivity";

    private ListView mTodoListView;
    private FloatingActionButton mFab;
    private TodoListAdapter mTodoListAdapter;

    private TodoStore mTodoStore;

    private AlertDialog mDeleteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTodoListView = (ListView) findViewById(R.id.todo_list);
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        mTodoStore = TodoStore.getInstance();
        mTodoStore.setPreferences(getPreferences(Context.MODE_PRIVATE));
        mTodoStore.load();

        mTodoListAdapter = new TodoListAdapter(this, R.layout.todo_list_item, mTodoStore.getList());
        mTodoListView.setAdapter(mTodoListAdapter);
        mTodoListView.setEmptyView(findViewById(R.id.todo_list_empty));
        registerForContextMenu(mTodoListView);

        mTodoListView.setOnItemClickListener(this);
        mFab.setOnClickListener(this);

        buildDialogs();
    }

    private void buildDialogs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.question_delete_items_long)
                .setTitle(R.string.alert_delete_items)
                .setPositiveButton(R.string.yes, this)
                .setNegativeButton(R.string.cancel, this);

        mDeleteDialog = builder.create();
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, EditActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        if (dialog != mDeleteDialog)
            return;

        if (id == DialogInterface.BUTTON_POSITIVE) {
            Resources res = getResources();
            mTodoStore.removeDone();
            mTodoStore.persist();
            mTodoListAdapter.notifyDataSetChanged();

            String message = res.getString(R.string.message_todos_deleted);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            dialog.dismiss();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Todo.EXTRA_TODO, mTodoStore.get(position));
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Resources res = getResources();
        if (v.getId() == R.id.todo_list) {
            String[] menuItems = res.getStringArray(R.array.todo_list_context_menu);
            menu.setHeaderTitle(res.getString(R.string.title_todo_list_context_menu));

            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        int position = info.position;
        int viewId = info.targetView.getId();

        if (viewId == R.id.todo_list_item) {
            switch (menuItemIndex) {
                case 0: // edit
                    Intent intent = new Intent(this, EditActivity.class);
                    intent.putExtra(Todo.EXTRA_TODO, mTodoStore.get(position));
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    break;
                case 1: // delete
                    Todo todo = mTodoStore.get(position);

                    Toast.makeText(this, String.format("Deleted %s", todo.getTitle()),
                            Toast.LENGTH_SHORT).show();

                    mTodoStore.remove(position);
                    mTodoStore.persist();
                    mTodoListAdapter.notifyDataSetChanged();
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else if (id == R.id.action_clear_done) {
            mDeleteDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTodoStore.sort();
        mTodoListAdapter.notifyDataSetChanged();
    }
}
