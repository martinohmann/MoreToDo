package de.mohmann.moretodo.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.data.TodoStore;

/**
 * Created by mohmann on 2/8/16.
 */
public class NotificationFeedbackReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final long todoId = intent.getLongExtra(Todo.EXTRA_ID, Todo.ID_UNSET);
        final int notificationId = intent.getIntExtra(BackgroundService.EXTRA_NOTIFICATION_ID, -1);

        if (notificationId > -1) {
            final NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }

        if (todoId == Todo.ID_UNSET)
            return;

        TodoStore todoStore = TodoStore.getInstance(context);
        Todo todo = todoStore.getById(todoId);

        if (todo == null)
            return;

        todo.setDone(true);
        todoStore.save(todo);
    }
}
