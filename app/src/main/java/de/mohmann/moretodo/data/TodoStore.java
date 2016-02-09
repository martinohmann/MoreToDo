package de.mohmann.moretodo.data;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mohmann on 2/3/16.
 */
public class TodoStore {

    final public static String TAG = "TodoStore";

    final public static String FILTER_NONE = "";

    private static TodoStore sInstance;

    private DatabaseHelper mDbHelper;
    private List<OnTodoListUpdateListener> mListListeners = new ArrayList<>();
    private List<OnTodoListFilterListener> mFilterListeners = new ArrayList<>();
    private List<Todo> mTodoList = new ArrayList<>();
    private String mFilter = FILTER_NONE;

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

    public void filterBy(String filterString, boolean enabled) {
        mFilter = filterString;
        for (OnTodoListFilterListener listener : mFilterListeners) {
            if (listener != null)
                listener.onTodoListFilter(mFilter, enabled);
        }
        load();
    }

    public void filterBy(String filterString) {
        filterBy(filterString, true);
    }

    public void clearFilter() {
        filterBy(FILTER_NONE, false);
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
        if (mFilter.equals(FILTER_NONE)) {
            mTodoList = mDbHelper.getTodos();
        } else {
            mTodoList = mDbHelper.getTodosByFilter(mFilter);
        }
        Log.i(TAG, String.format("loaded %d todos", mTodoList.size()));
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        for (OnTodoListUpdateListener listener : mListListeners) {
            if (listener != null)
                listener.onTodoListUpdate();
        }
    }

    public void addOnTodoListUpdateListener(OnTodoListUpdateListener listener) {
        mListListeners.add(listener);
    }

    public void removeOnTodoListUpdateListener(OnTodoListUpdateListener listener) {
        mListListeners.remove(listener);
    }

    public void addOnTodoListFilterListener(OnTodoListFilterListener listener) {
        mFilterListeners.add(listener);
    }

    public void removeOnTodoListFilterListener(OnTodoListFilterListener listener) {
        mFilterListeners.remove(listener);
    }

    public interface OnTodoListUpdateListener {
        void onTodoListUpdate();
    }

    public interface OnTodoListFilterListener {
        void onTodoListFilter(String filter, boolean enabled);
    }
}
