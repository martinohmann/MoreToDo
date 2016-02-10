package de.mohmann.moretodo.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.activities.DetailActivity;
import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.data.TodoStore;
import de.mohmann.moretodo.util.DateFormatter;
import de.mohmann.moretodo.util.Preferences;
import de.mohmann.moretodo.util.Utils;

/**
 * Created by mohmann on 2/8/16.
 */
public class BackgroundService extends Service {

    final public static String TAG = "BackgroundService";

    final public static String EXTRA_NOTIFICATION_ID =
            "de.mohmann.moretodo.services.BackgroundService.NOTIFICATION_ID";

    final public static String ACTION_TODO_MARKED_DONE =
            "de.mohmann.moretodo.services.BackgroundService.ACTION_TODO_MARKED_DONE";

    private static PendingIntent sPendingIntent = null;

    private Context mContext;
    private TodoStore mTodoStore;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        /* wrap context with application theme for toast messages */
        mContext = new ContextThemeWrapper(getApplicationContext(), R.style.AppTheme);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mTodoStore = TodoStore.getInstance(this);

        if (sPendingIntent == null) {
            sPendingIntent = PendingIntent.getService(this, 0, new Intent(this, BackgroundService.class), 0);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Preferences.isNotificationsEnabled(mContext)) {
            Log.d(TAG, String.format("[%s] %s", new Date(), "notification background task"));

            for (Todo todo : mTodoStore.getList()) {
                if (todo.isDone() || todo.getDueDate() == Todo.DATE_UNSET || todo.isNotified()) {
                    continue;
                }
                if (todo.getDueDate() <= System.currentTimeMillis()) {
                    notify(todo);
                    todo.setNotified(true);
                    mTodoStore.save(todo);
                }
            }
        }

        if (Preferences.isAutoremoveEnabled(mContext)) {
            Log.d(TAG, String.format("[%s] %s", new Date(), "autoremoval background task"));

            final long interval = Preferences.getAutoremoveInterval(mContext) * 1000;
            int removed = 0;

            /* iterate over a copy of the list to avoid concurrent modification */
            List<Todo> todoList = new ArrayList<>(mTodoStore.getList());

            for (Todo todo : todoList) {
                if (!todo.isDone())
                    continue;

                if (System.currentTimeMillis() - todo.getFinishDate() >= interval) {
                    mTodoStore.remove(todo);
                    removed++;
                }
            }

            if (removed > 0) {
                if (removed == 1) {
                    Utils.toast(mContext, R.string.message_todo_autoremoval, removed);
                } else {
                    Utils.toast(mContext, R.string.message_todo_autoremoval_plural, removed);
                }
            }
        }

        // I don't want this service to stay in memory, so I stop it
        // immediately after doing what I wanted it to do.
        stopSelf();

        return START_NOT_STICKY;
    }

    private void notify(final Todo todo) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        String date = DateFormatter.getFullDate(todo.getDueDate());
        builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
        builder.setContentTitle(todo.getTitle());
        int notificationId = 0;

        try {
            notificationId = Utils.safeLongToInt(todo.getId());
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "error while setting notificationId: unable to " +
                    "convert todo id to int, using 0 as fallback");
        }

        if (!todo.getContent().isEmpty()) {
            builder.setContentText(Utils.shorten(todo.getContent(), 140));
        }

        builder.setSubText(date);
        builder.setSmallIcon(R.drawable.ic_assignment_white_48dp);

        builder.setTicker(String.format("%s: %s", date, todo.getTitle()));

        builder.setAutoCancel(true);
        builder.setOngoing(false);

        if (Preferences.isVibrateEnabled(mContext)) {
            /* set vibration parameters */
            builder.setVibrate(new long[] { 0l, 500l });
        } else {
            builder.setVibrate(null);
        }

        /* create new intent for notification with device and notification id as payload */
        Intent resultIntent = new Intent(mContext, DetailActivity.class);
        resultIntent.putExtra(Todo.EXTRA_ID, todo.getId());
        resultIntent.putExtra(BackgroundService.EXTRA_NOTIFICATION_ID, notificationId);

        /* create stackbuilder, add back stack, and add intent to the top of the stack */
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(DetailActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        /* gets a PendingIntent containing the entire back stack */
        PendingIntent pendingContentIntent =
                stackBuilder.getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT);

        /* add intent to notification */
        builder.setContentIntent(pendingContentIntent);

        /* create intent to mark as done */
        Intent markIntent = new Intent(ACTION_TODO_MARKED_DONE);
        markIntent.putExtra(Todo.EXTRA_ID, todo.getId());
        markIntent.putExtra(BackgroundService.EXTRA_NOTIFICATION_ID, notificationId);

        PendingIntent pendingMarkIntent = PendingIntent.getBroadcast(this, 0, markIntent,
                Intent.FILL_IN_DATA);

        builder.addAction(R.drawable.ic_assignment_white_18dp, getString(R.string.view),
                pendingContentIntent);
        builder.addAction(R.drawable.ic_done_white_18dp, getString(R.string.mark_item_done),
                pendingMarkIntent);

        /* deploy */
        mNotificationManager.notify(notificationId, builder.build());
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        /* restart this service again on the next full minute */
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, (calendar.get(Calendar.MINUTE) + 1) % 60);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sPendingIntent);
    }
}
