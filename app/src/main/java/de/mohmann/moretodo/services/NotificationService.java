package de.mohmann.moretodo.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;

import de.mohmann.moretodo.R;
import de.mohmann.moretodo.activities.DetailActivity;
import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.data.TodoStore;
import de.mohmann.moretodo.util.DateFormatter;
import de.mohmann.moretodo.util.Utils;

/**
 * Created by mohmann on 2/8/16.
 */
public class NotificationService extends Service {

    final public static String TAG = "NotificationService";

    final public static String EXTRA_NOTIFICATION_ID =
            "de.mohmann.moretodo.services.NotificationService.NOTIFICATION_ID";

    final public static String ACTION_TODO_MARKED_DONE =
            "de.mohmann.moretodo.services.NotificationService.ACTION_TODO_MARKED_DONE";

    private static int mNotificationId = 0;

    private static PendingIntent sPendingIntent = null;

    private Context mContext;
    private TodoStore mTodoStore;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mTodoStore = TodoStore.getInstance(this);

        if (sPendingIntent == null) {
            sPendingIntent = PendingIntent.getService(this, 0, new Intent(this, NotificationService.class), 0);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, String.format("[%s] %s",
                DateFormatter.getFullDate(System.currentTimeMillis()),
                "background task"));

        for (Todo todo : mTodoStore.getList()) {
            if (!todo.isDone() && todo.getDueDate() != Todo.DATE_UNSET && !todo.isNotified()) {
                if (todo.getDueDate() <= System.currentTimeMillis()) {
                    notify(todo);
                    todo.setNotified(true);
                    mTodoStore.save(todo);
                }
            }
        }

        // I don't want this service to stay in memory, so I stop it
        // immediately after doing what I wanted it to do.
        stopSelf();

        return START_NOT_STICKY;
    }

    private void notify(Todo todo) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        String date = DateFormatter.getFullDate(todo.getDueDate());
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setContentTitle(todo.getTitle());

        if (!todo.getContent().isEmpty()) {
            builder.setContentText(Utils.shorten(todo.getContent(), 140, true));
        }

        builder.setSubText(date);
        builder.setSmallIcon(R.drawable.ic_assignment_white_48dp);

        builder.setTicker(String.format("%s: %s", date, todo.getTitle()));

        builder.setAutoCancel(true);
        builder.setOngoing(false);

        /* set vibration parameters */
        builder.setVibrate(new long[] { 0, 500, 50, 2000 });

        /* create new intent for notification with device and notification id as payload */
        Intent resultIntent = new Intent(mContext, DetailActivity.class);
        resultIntent.putExtra(Todo.EXTRA_TODO, todo);

        /* create stackbuilder, add back stack, and add intent to the top of the stack */
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(DetailActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        /* gets a PendingIntent containing the entire back stack */
        PendingIntent pendingContentIntent =
                stackBuilder.getPendingIntent(mNotificationId, PendingIntent.FLAG_UPDATE_CURRENT);

        /* add intent to notification */
        builder.setContentIntent(pendingContentIntent);

        /* create intent to mark todo as done */
        Intent markIntent = new Intent(ACTION_TODO_MARKED_DONE);
        markIntent.putExtra(Todo.EXTRA_TODO, todo);

        PendingIntent pendingMarkIntent = PendingIntent.getBroadcast(this, 0, markIntent, 0);

        builder.addAction(R.drawable.ic_assignment_white_18dp, getString(R.string.view),
                pendingContentIntent);
        builder.addAction(R.drawable.ic_done_white_18dp, getString(R.string.mark_item_done),
                pendingMarkIntent);

        /* deploy */
        mNotificationManager.notify(mNotificationId++, builder.build());
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
