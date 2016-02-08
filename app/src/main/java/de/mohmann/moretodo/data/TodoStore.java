package de.mohmann.moretodo.data;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import de.mohmann.moretodo.database.DatabaseHelper;

/**
 * Created by mohmann on 2/3/16.
 */
public class TodoStore {

    final public static String TAG = "TodoStore";

    private static TodoStore sInstance;

    private List<Todo> mTodoList = new ArrayList<>();

    private OnTodoListUpdateListener mListener = null;

    private DatabaseHelper mDbHelper;

    private Comparator<Todo> mObjComparator = new Comparator<Todo>() {
        public int compare(Todo t1, Todo t2) {
            return t2.compareTo(t1);
        }
    };

    public static synchronized TodoStore getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TodoStore(context.getApplicationContext());
        }
        return sInstance;
    }

    private TodoStore(Context context) {
        mDbHelper = DatabaseHelper.getInstance(context);
        load();
    }

    public void add(Todo todo) {
        mTodoList.add(todo);
        save(todo);
    }

    public void save(Todo todo) {
        mDbHelper.saveTodo(todo);
        notifyDataSetChanged();
    }

    public void removeById(final long id) {
        for (Iterator<Todo> iterator = mTodoList.iterator(); iterator.hasNext();) {
            Todo todo = iterator.next();
            if (todo.getId() == id) {
                iterator.remove();
                mDbHelper.deleteTodo(todo);
                notifyDataSetChanged();
                return;
            }
        }
    }

    public void removeDone() {
        boolean changed = false;

        for (Iterator<Todo> iterator = mTodoList.iterator(); iterator.hasNext();) {
            Todo todo = iterator.next();
            if (todo.isDone()) {
                iterator.remove();
                mDbHelper.deleteTodo(todo);
                changed = true;
            }
        }
        if (changed) {
            notifyDataSetChanged();
        }
    }

    public Todo get(final int position) {
        return mTodoList.get(position);
    }

    public boolean contains(final Todo todo) {
        if (todo == null)
            return false;

        for (Todo item : mTodoList) {
            if (item.getId() == todo.getId())
                return true;
        }
        return false;
    }

    public Todo getById(final long id) {
        for (Todo item : mTodoList) {
            if (item.getId() == id)
                return item;
        }
        return null;
    }

    public List<Todo> getList() {
        return mTodoList;
    }

    public void load() {
        mTodoList = mDbHelper.getTodos();
        Log.d(TAG, mTodoList.toString());
        Log.i(TAG, String.format("loaded %d todos", mTodoList.size()));
        notifyDataSetChanged();
    }

    public void sort() {
        Collections.sort(mTodoList, mObjComparator);
    }

    public void notifyDataSetChanged() {
        if (mListener != null)
            mListener.onTodoListUpdate();
    }

    public void setOnTodoListUpdateListener(OnTodoListUpdateListener listener) {
        mListener = listener;
    }

    public interface OnTodoListUpdateListener {
        void onTodoListUpdate();
    }
}
