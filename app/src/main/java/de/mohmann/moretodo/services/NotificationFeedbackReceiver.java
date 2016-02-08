package de.mohmann.moretodo.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.mohmann.moretodo.data.Todo;
import de.mohmann.moretodo.data.TodoStore;

/**
 * Created by mohmann on 2/8/16.
 */
public class NotificationFeedbackReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Todo todo = intent.getParcelableExtra(Todo.EXTRA_TODO);

        if (todo == null)
            return;

        TodoStore todoStore = TodoStore.getInstance(context);
        todo = todoStore.getById(todo.getId());

        if (todo == null)
            return;

        todo.setDone(true);
        todoStore.save(todo);
    }
}
