package de.mohmann.moretodo.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.adapters.TodoListAdapter;
import de.mohmann.moretodo.adapters.ViewPagerAdapter;
import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.data.TodoStore;
import de.mohmann.moretodo.fragments.TodoListFragment;
import de.mohmann.moretodo.util.DateFormatter;
import de.mohmann.moretodo.util.Utils;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,
        DialogInterface.OnClickListener {

    final public static String TAG = "MainActivity";

    private FloatingActionButton mFab;
    private TodoStore mTodoStore;
    private AlertDialog mDeleteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(this);

        mTodoStore = TodoStore.getInstance();
        mTodoStore.setPreferences(getPreferences(Context.MODE_PRIVATE));
        mTodoStore.load();

        buildDialogs();

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        runBackgroundTasks();
    }

    private void runBackgroundTasks() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, String.format("[%s] %s",
                                DateFormatter.getDateTime(System.currentTimeMillis()),
                                "background task"));

                        for (Todo todo : mTodoStore.getList()) {
                            if (!todo.isDone() && todo.getDueDate() > -1) {
                                Log.d(TAG, todo.toString());
                            }
                        }
                    }
                });
            }
        }, 0, 5000);
    }

    private void setupViewPager(final ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(TodoListFragment.newInstance(TodoListAdapter.FILTER_ALL), "All");
        adapter.addFragment(TodoListFragment.newInstance(TodoListAdapter.FILTER_PENDING), "Pending");
        adapter.addFragment(TodoListFragment.newInstance(TodoListAdapter.FILTER_DONE), "Done");
        viewPager.setAdapter(adapter);
        mTodoStore.setOnTodoListUpdateListener(adapter);
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
            mTodoStore.removeDone();
            mTodoStore.persist();

            Utils.toast(this, R.string.message_todos_deleted);
        } else {
            dialog.dismiss();
        }
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
    }
}
