package de.mohmann.moretodo.services;

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
        Todo todo = intent.getParcelableExtra(Todo.EXTRA_TODO);

        Log.d("NFR", "0 received");

        if (todo == null)
            return;

        Log.d("NFR", "1 todo: " + todo.toString());

        TodoStore todoStore = TodoStore.getInstance(context);
        todo = todoStore.getById(todo.getId());

        if (todo == null)
            return;

        Log.d("NFR", "2 todo: " + todo.toString());

        todo.setDone(true);
        todoStore.save(todo);
    }
}
