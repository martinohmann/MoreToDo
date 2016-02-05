package de.mohmann.moretodo.activities;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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
        DialogInterface.OnClickListener,
        ViewPager.OnPageChangeListener {

    final public static String TAG = "MainActivity";

    private TodoStore mTodoStore;
    private AlertDialog mDeleteDialog;

    private TabLayout mTabLayout;

    final private int[] mTabIcons = {
            R.drawable.ic_list_white_18dp,
            R.drawable.ic_schedule_white_18dp,
            R.drawable.ic_done_white_18dp
    };

    final private int[] mTabLabels = {
            R.string.label_tab_all,
            R.string.label_tab_pending,
            R.string.label_tab_done
    };

    final private String[] mTabFilters = {
            TodoListAdapter.FILTER_ALL,
            TodoListAdapter.FILTER_PENDING,
            TodoListAdapter.FILTER_DONE
    };

    final public static String EXTRA_NOTIFICATION_ID =
            "de.mohmann.moretodo.activities.MainActivity.NOTIFICATION_ID";
    private static int mNotificationId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTodoStore = TodoStore.getInstance();
        mTodoStore.setPreferences(getPreferences(Context.MODE_PRIVATE));
        mTodoStore.load();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.addOnPageChangeListener(this);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
        setTabAlpha(0);

        buildDeleteDialog();
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
                                DateFormatter.getFullDate(System.currentTimeMillis()),
                                "background task"));

                        for (Todo todo : mTodoStore.getList()) {
                            if (!todo.isDone() && todo.getDueDate() != Todo.NO_DUEDATE && !todo.isNotified()) {
                                if (todo.getDueDate() <= System.currentTimeMillis()) {
                                    sendNotification(todo);
                                    todo.setNotified(true);
                                    mTodoStore.persist();
                                }
                            }
                        }
                    }
                });
            }
        }, 0, 30000);
    }

    private void setTabAlpha(int position) {
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab == null)
                continue;
            View view = tab.getCustomView();
            if (view == null)
                continue;
            view.setAlpha(i == position ? 1f : 0.8f);
        }
    }

    private void setupTabIcons() {
        TextView tabView;
        TabLayout.Tab tab;

        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            tabView = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            tabView.setText(mTabLabels[i]);
            tabView.setCompoundDrawablesWithIntrinsicBounds(mTabIcons[i], 0, 0, 0);
            tab = mTabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(tabView);
            }
        }
    }

    private void setupViewPager(final ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        final Resources res = getResources();
        for (int i = 0; i < mTabLabels.length; i++) {
            adapter.addFragment(TodoListFragment.newInstance(mTabFilters[i]),
                    res.getString(mTabLabels[i]));
        }
        viewPager.setAdapter(adapter);
        mTodoStore.setOnTodoListUpdateListener(adapter);
    }

    private void buildDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.question_delete_items_long);
        builder.setTitle(R.string.alert_delete_items);
        builder.setPositiveButton(R.string.yes, this);
        builder.setNegativeButton(R.string.cancel, this);
        mDeleteDialog = builder.create();
    }


    private void sendNotification(Todo todo) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        String date = DateFormatter.getFullDate(todo.getDueDate());
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setContentTitle(todo.getTitle());
        if (!todo.getContent().isEmpty()) {
            builder.setContentText(todo.getContent());
        }
        builder.setSubText(date);
        builder.setSmallIcon(R.drawable.ic_assignment_white_48dp);

        builder.setTicker(String.format("%s: %s", date, todo.getTitle()));

        builder.setAutoCancel(false);
        builder.setOngoing(false);

        /* set vibration parameters */
        builder.setVibrate(new long[]{0, 500, 50, 2000});

        /* create new intent for notification with device and notification id as payload */
        Intent resultIntent = new Intent(this, DetailActivity.class);
        resultIntent.putExtra(MainActivity.EXTRA_NOTIFICATION_ID, mNotificationId);
        resultIntent.putExtra(Todo.EXTRA_TODO, todo);

        /* create stackbuild, add back stack, and add intent to the top of the stack */
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(DetailActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        /* gets a PendingIntent containing the entire back stack */
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(mNotificationId, PendingIntent.FLAG_UPDATE_CURRENT);

        /* add intent to notification */
        builder.setContentIntent(resultPendingIntent);

        /* deploy */
        notificationManager.notify(mNotificationId++, builder.build());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        /* unused */
    }

    @Override
    public void onPageSelected(int position) {
        setTabAlpha(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        /* unused */
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

        /* if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else */
        if (id == R.id.action_clear_done) {
            mDeleteDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
